/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package com.github.twinj.pecoff4j.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.github.twinj.headers.Header;
import com.github.twinj.pecoff4j.BoundImport;
import com.github.twinj.pecoff4j.BoundImportDir;
import com.github.twinj.pecoff4j.COFFH;
import com.github.twinj.pecoff4j.DOSH;
import com.github.twinj.pecoff4j.DOSStub;
import com.github.twinj.pecoff4j.DebugDir;
import com.github.twinj.pecoff4j.ExportDir;
import com.github.twinj.pecoff4j.ImageData;
import com.github.twinj.pecoff4j.ImportDirTable;
import com.github.twinj.pecoff4j.ImportDirectoryTable;
import com.github.twinj.pecoff4j.ImportEntry;
import com.github.twinj.pecoff4j.LoadConfigDir;
import com.github.twinj.pecoff4j.OH;
import com.github.twinj.pecoff4j.OHA32;
import com.github.twinj.pecoff4j.OHA64;
import com.github.twinj.pecoff4j.OHD;
import com.github.twinj.pecoff4j.OHDD;
import com.github.twinj.pecoff4j.OHS;
import com.github.twinj.pecoff4j.PESig;
import com.github.twinj.pecoff4j.RCDH;
import com.github.twinj.pecoff4j.RCDirEntry;
import com.github.twinj.pecoff4j.RCRoot;
import com.github.twinj.pecoff4j.RCTree;
import com.github.twinj.pecoff4j.SH;
import com.github.twinj.pecoff4j.STable;
import com.github.twinj.pecoff4j.io.PE.SectData;
import com.github.twinj.pecoff4j.util.IntMap;

/**
 * TODO Parsing is not dynamic enough. Change.
 * 
 * @author Daniel Kemp
 * 
 */
public class PEParser {
	
	
	
	static PE current = null;
	
	public static PE parse(InputStream is) throws IOException {
		return read(new DataReader(is));
	}
	
	public static PE parse(String filename) throws IOException {
		return parse(new File(filename));
	}
	
	public static PE parse(File file) throws IOException {
		return read(new DataReader(new FileInputStream(file)));
	}
	
	public static PE read(IDataReader dr) throws IOException {
		PE pe = new PE();
		current = pe;
	
		pe.setDosHeader((DOSH) Header.parse(DOSH.class, dr));
		
		// Check if we have an old file type
		if (pe.lfanew == 0 || pe.lfanew > 8192) {
			return pe;
		}
		
		pe.setStub(readStub(pe.getDosHeader(), dr));
		pe.setSignature(readSignature(dr));
		
		// Check signature to ensure we have a pe/coff file
		if (!pe.getSignature().isValid()) {
			return pe;
		}
		pe.setCoffHeader((COFFH) Header.parse(COFFH.class, dr));
		
		OH o = new OH();
		
		o.setStandard((OHS) Header.parse(OHS.class, dr));
		
		if (pe.is64) {
			o.setAdditional((OHA64) Header.parse(OHA64.class, dr));
		} else {
			o.setAdditional((OHA32) Header.parse(OHA32.class, dr));
		}
		o.setDirectories((OHD) Header.parse(OHD.class, dr));
		
		pe.setOptionalHeader(o);
		pe.setSectionTable(readSectionHeaders(pe.getCoffHeader(), dr));
		
		// Now read the rest of the file
		DataEntry entry = null;
		while ((entry = findNextEntry(pe, dr.getPosition())) != null) {
			if (entry.isSection) {
				readSection(pe, entry, dr);
			} else if (entry.isDebugRawData) {
				readDebugRawData(pe, entry, dr);
			} else {
				readImageData(pe, entry, dr);
			}
		}
		
		// Read any trailing data
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int read = -1;
		while ((read = dr.readByte()) != -1) {
			bos.write(read);
		}
		byte[] tb = bos.toByteArray();
		if (tb.length > 0) {
			pe.getImageData().setTrailingData(tb);
		}
		
		return pe;
	}
	
	public static DOSStub readStub(DOSH header, IDataReader dr) throws IOException {
		DOSStub ds = new DOSStub();
		int pos = dr.getPosition();
		int add = header.valueOf(DOSH.Property.PE_HEADER_OFFSET_BD).intValue();
		byte[] stub = new byte[add - pos];
		dr.read(stub);
		ds.setStub(stub);
		return ds;
	}
	
	public static PESig readSignature(IDataReader dr) throws IOException {
		byte[] bytes = new byte[4];
		dr.read(bytes);
		PESig ps = new PESig(ByteBuffer.wrap(bytes), 0L);
		return ps;
	}
	
	public static STable readSectionHeaders(COFFH ch, IDataReader dr) throws IOException {
		int ns = ch.valueOf(COFFH.Property.NUMBER_OF_SECTIONS).intValue();
		
		STable sht = new STable(ns, dr);
		
		SH[] sorted = sht.values().toArray(new SH[ns]);
		int[] virtualAddress = new int[sorted.length];
		int[] pointerToRawData = new int[sorted.length];
		
		for (int i = 0; i < sorted.length; i++) {
			virtualAddress[i] = sorted[i].valueOf(SH.Property.VIRTUAL_ADDRESS).intValue();
			pointerToRawData[i] = sorted[i].valueOf(SH.Property.POINTER_TO_RAW_DATA).intValue();
		}
		
		sht.virtualAddress = virtualAddress;
		sht.pointerToRawData = pointerToRawData;
		return sht;
	}
	
	public static DataEntry findNextEntry(PE pe, int pos) {
		DataEntry de = new DataEntry();
		
		// Check sections first
		for (SH sh : pe.getSectionTable().values()) {
			int prd = sh.valueOf(SH.Property.POINTER_TO_RAW_DATA).intValue();
			
			if (sh.valueOf(SH.Property.SIZE_OF_RAW_DATA).intValue() > 0 && prd >= pos
						&& (de.pointer == 0 || prd < de.pointer)) {
				de.pointer = prd;
				de.name = sh.toUtfString(SH.Property.NAME);
				de.isSection = true;
			}
		}
		
		// Now check image data directories
		STable sht = pe.getSectionTable();
		OHD od = pe.getOptionalHeader().getDirectories();
		
		for (OHD.Directory d : OHD.Directory.values()) {
			if (od.valueOf(d, OHDD.Property.SIZE).intValue() > 0) {
				int prd = od.valueOf(d, OHDD.Property.VIRTUAL_ADDRESS);
				
				// Assume certificate live outside section ?
				if (isInsideSection(pe, d)) {
					prd = sht.convertVirtualAddressToRawDataPointer(prd);
				}
				if (prd >= pos && (de.pointer == 0 || prd < de.pointer)) {
					de.pointer = prd;
					de.name = d.toString();
					de.isSection = false;
				}
			}
		}
		
		// Check debug
		ImageData id = pe.getImageData();
		DebugDir dd = null;
		if (id != null) dd = id.getDebug();
		if (dd != null) {
			int prd = dd.valueOf(DebugDir.Property.POINTER_TO_RAW_DATA).intValue();
			if (prd >= pos && (de.pointer == 0 || prd < de.pointer)) {
				de.pointer = prd;
				de.isDebugRawData = true;
				de.isSection = false;
				de.baseAddress = prd;
			}
		}
		
		if (de.pointer == 0) return null;
		
		return de;
	}
	
	private static boolean isInsideSection(PE pe, OHD.Directory d) {
		OHD od = pe.getOptionalHeader().getDirectories();
		STable sht = pe.getSectionTable();
		
		int prd = od.valueOf(d, OHDD.Property.VIRTUAL_ADDRESS);
		int pex = prd + od.valueOf(d, OHDD.Property.SIZE).intValue();
		
		for (SH sh : sht.values()) {
			int vad = sh.valueOf(SH.Property.VIRTUAL_ADDRESS).intValue();
			int vex = vad + sh.valueOf(SH.Property.VIRTUAL_SIZE).intValue();
			
			if (prd >= vad && prd < vex && pex <= vex) return true;
		}
		return false;
	}
	
	private static void readImageData(PE pe, DataEntry entry, IDataReader dr)
				throws IOException {
		
		// Read any page padding data
		ImageData id = pe.getImageData();
		byte[] pa = readPagePadding(entry.pointer, dr);
		if (pa != null) {
			id.put(entry.index, pa);
		}
		
		// Read the image data
		OHD od = pe.getOptionalHeader().getDirectories();
		OHDD dd = od.get(OHD.Directory.valueOf(entry.name));
		
		int size = dd.valueOf(OHDD.Property.SIZE).intValue();
		
		byte[] b = new byte[size];
		dr.read(b);
		// dd.createTable();
		
		ByteBuffer buffer = ByteBuffer.wrap(b);
		
		System.err.println(entry.name);
		
		switch (OHD.Directory.valueOf(entry.name)) {
		
			case IMAGE_DIRECTORY_ENTRY_EXPORT :
				id.setExportTable(new ExportDir(buffer));
				break;
			case IMAGE_DIRECTORY_ENTRY_IMPORT :
				id.setImportTable(readImportDirectory(buffer, entry.baseAddress));
				break;
			case IMAGE_DIRECTORY_ENTRY_RESOURCE :
				id.setResourceTable(readResourceDirectoryTree(buffer));
				break;
			case IMAGE_DIRECTORY_ENTRY_EXCEPTION :
				id.setExceptionTable(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_SECURITY :
				id.setCertificateTable(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_BASERELOC :
				id.setBaseRelocationTable(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_DEBUG :
				id.setDebug(new DebugDir(buffer));
			case IMAGE_DIRECTORY_ENTRY_ARCHITECTURE :
				id.setArchitecture(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_GLOBALPTR :
				id.setGlobalPtr(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_TLS :
				id.setTlsTable(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_LOAD_CONFIG :
				id.setLoadConfigTable(new LoadConfigDir(buffer));
				break;
			case IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT :
				id.setBoundImports(readBoundImportDirectoryTable(buffer));
				break;
			case IMAGE_DIRECTORY_ENTRY_IAT :
				id.setIat(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_DELAY_IMPORT :
				id.setDelayImportDescriptor(b);
				break;
			case IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR :
				id.setClrRuntimeHeader(b);
				break;
			case IMAGE_DIRECTORY_EMPTY :
				id.setReserved(b);
				break;
		}
	}
	
	private static byte[] readPagePadding(int pointer, IDataReader dr) throws IOException {
		if (pointer > dr.getPosition()) {
			byte[] pa = new byte[pointer - dr.getPosition()];
			dr.read(pa);
			boolean zeroes = true;
			
			for (int i = 0; i < pa.length; i++) {
				if (pa[i] != 0) {
					zeroes = false;
					break;
				}
			}
			if (!zeroes) return pa;
		}
		
		return null;
	}
	
	private static void readDebugRawData(PE pe, DataEntry entry, IDataReader dr)
				throws IOException {
		// Read any preamble data
		ImageData id = pe.getImageData();
		byte[] pa = readPagePadding(entry.pointer, dr);
		if (pa != null) id.setDebugRawDataPreamble(pa);
		DebugDir dd = id.getDebug();
		byte[] b = new byte[dd.valueOf(DebugDir.Property.SIZE_OF_DATA).intValue()];
		dr.read(b);
		id.setDebugRawData(b);
	}
	
	private static void readSection(PE pe, DataEntry entry, IDataReader dr)
				throws IOException {
		
		STable sht = pe.getSectionTable();
		SH sh = sht.get(entry.name);
		System.err.println(entry.name + " read section");
		SectData sd = pe.new SectData();
		
		// Read any preamble - store if non-zero
		sd.preamble = readPagePadding(sh.valueOf(SH.Property.POINTER_TO_RAW_DATA).intValue(),
					dr);
		
		// Read in the raw data block
		dr.jumpTo(sh.valueOf(SH.Property.POINTER_TO_RAW_DATA).intValue());
		byte[] b = new byte[sh.valueOf(SH.Property.SIZE_OF_RAW_DATA).intValue()];
		dr.read(b);
		sd.data = b;
		sh.sd = sd;
		
		// Check for an directory image within this section
		for (OHDD d : pe.getOptionalHeader().getDirectories().values()) {
			
			if (d.valueOf(OHDD.Property.SIZE).intValue() > 0) {
				
				int vad = sh.valueOf(SH.Property.VIRTUAL_ADDRESS).intValue();
				int vex = vad + sh.valueOf(SH.Property.VIRTUAL_SIZE).intValue();
				int dad = d.valueOf(OHDD.Property.VIRTUAL_ADDRESS).intValue();
				if (dad >= vad && dad < vex) {
					int off = dad - vad;
					IDataReader idr = new ByteArrayDataReader(b, off, d.valueOf(OHDD.Property.SIZE)
								.intValue());
					DataEntry de = new DataEntry(d.index, 0, d.name);
					de.baseAddress = sh.valueOf(SH.Property.VIRTUAL_ADDRESS).intValue();
					
					readImageData(pe, de, idr);
				}
			}
		}
	}
	
	private static BoundImportDir readBoundImportDirectoryTable(ByteBuffer buffer)
				throws IOException {
		BoundImportDir bidt = new BoundImportDir(buffer);
		
		Collections.sort(bidt, new Comparator<BoundImport>() {
			public int compare(BoundImport o1, BoundImport o2) {
				return o1.valueOf(BoundImport.Property.OFFSET_MODULE_NAME).intValue()
							- o2.valueOf(BoundImport.Property.OFFSET_MODULE_NAME).intValue();
			}
		});
		IntMap names = new IntMap();
		BoundImport bi = null;
		for (int i = 0; i < bidt.size(); i++) {
			bi = bidt.get(i);
			int offset = bi.valueOf(BoundImport.Property.OFFSET_MODULE_NAME).intValue();
			String n = (String) names.get(offset);
			
			if (n == null) {
				buffer.position(offset);
				// n = dr.readUtf();
				names.put(offset, n);
			}
			bi.moduleName = n;
		}
		return bidt;
	}
	
	public static ImportDirTable readImportDirectory(ByteBuffer buffer, int baseAddress)
				throws IOException {
		ImportDirTable idt = new ImportDirTable(buffer);
		
		/*
		 * FIXME - name table refer to data outside image directory for (int i = 0;
		 * i < id.size(); i++) { ImportDirectoryEntry e = id.getEntry(i);
		 * dr.jumpTo(e.getNameRVA() - baseAddress); String name = dr.readUtf();
		 * dr.jumpTo(e.getImportLookupTableRVA() - baseAddress);
		 * ImportDirectoryTable nt = readImportDirectoryTable(dr, baseAddress);
		 * dr.jumpTo(e.getImportAddressTableRVA() - baseAddress);
		 * ImportDirectoryTable at = null; // readImportDirectoryTable(dr, //
		 * baseAddress); id.add(name, nt, at); }
		 */
		
		return idt;
	}
	
	public static ImportDirectoryTable readImportDirectoryTable(IDataReader dr,
				int baseAddress) throws IOException {
		ImportDirectoryTable idt = new ImportDirectoryTable();
		ImportEntry ie = null;
		while ((ie = readImportEntry(dr)) != null) {
			idt.add(ie);
		}
		
		for (int i = 0; i < idt.size(); i++) {
			ImportEntry iee = idt.getEntry(i);
			if ((iee.getVal() & 0x80000000) != 0) {
				iee.setOrdinal(iee.getVal() & 0x7fffffff);
			} else {
				dr.jumpTo(iee.getVal() - baseAddress);
				dr.readWord(); // FIXME this is an index into the export table
				iee.setName(dr.readUtf());
			}
		}
		return idt;
	}
	
	public static ImportEntry readImportEntry(IDataReader dr) throws IOException {
		ImportEntry ie = new ImportEntry();
		ie.setVal(dr.readDoubleWord());
		if (ie.getVal() == 0) {
			return null;
		}
		
		return ie;
	}
	
	LinkedList<Integer> queue = new LinkedList<>();
	
	private static RCTree readResourceDirectoryTree(ByteBuffer buffer)
				throws IOException {
		RCTree root = new RCRoot(buffer, new RCDH(buffer));
		
		while (traverseTree(root, buffer)) {}
		return root;
	}
	
	private static boolean traverseTree(RCTree tree, ByteBuffer buffer)
				throws IOException {
		
		for (RCDirEntry e : tree.values()) {
			if (e.dir == null && e.entry == null) {
				buffer.position(e.valueOfBM(RCDirEntry.Property.OFFSET_TO_DATA, 0x7fffffff));
				e.dir = new RCTree(buffer, new RCDH(buffer));
				return true;
			} else if (e.entry == null) {
				traverseTree(e.dir, buffer);
			}
		}
		return false;
	}
	
}
