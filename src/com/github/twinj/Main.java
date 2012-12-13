package com.github.twinj;

import com.github.twinj.pecoff4j.BoundImport;
import com.github.twinj.pecoff4j.BoundImportDir;
import com.github.twinj.pecoff4j.COFFH;
import com.github.twinj.pecoff4j.ImportDescriptor;
import com.github.twinj.pecoff4j.ImportDirTable;
import com.github.twinj.pecoff4j.OHDD;
import com.github.twinj.pecoff4j.DOSH;
import com.github.twinj.pecoff4j.OHA;
import com.github.twinj.pecoff4j.OHA32;
import com.github.twinj.pecoff4j.OHA64;
import com.github.twinj.pecoff4j.OHD;
import com.github.twinj.pecoff4j.OHS;
import com.github.twinj.pecoff4j.PESig;
import com.github.twinj.pecoff4j.RCDataEntry;
import com.github.twinj.pecoff4j.RCDH;
import com.github.twinj.pecoff4j.RCDirEntry;
import com.github.twinj.pecoff4j.RCTree;
import com.github.twinj.pecoff4j.RCEntry;
import com.github.twinj.pecoff4j.SH;
import com.github.twinj.pecoff4j.STable;
import com.github.twinj.pecoff4j.io.PE;
import com.github.twinj.pecoff4j.io.PEParser;

/**
 * An example demonstrating parsing an executable.
 */
public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		PE pe = PEParser
					.parse("C:/Program Files (x86)/Steam/SteamApps/common/XCom-Enemy-Unknown/Binaries/Win32/XComGame.exe");
		
		System.out
					.println("***************************************************************************************");
		
		DOSH dh = pe.getDosHeader();
		
		for (DOSH.Property p : DOSH.Property.values()) {
			System.out.println(p + ": " + dh.valueOf(p));
		}
		System.out.println(dh.getStubSize());
		
		System.out
					.println("***************************************************************************************");
		
		COFFH ch = pe.getCoffHeader();
		
		for (COFFH.Property p : COFFH.Property.values()) {
			System.out.println(p + ": " + ch.valueOf(p));
		}
		
		System.out
					.println("***************************************************************************************");
		
		PESig sig = pe.getSignature();
		
		System.out.println(": " + sig.valueOf());
		
		System.out.println(": " + sig.isValid());
		
		System.out.println(": " + sig.isValueOfValid());
		
		System.out
					.println("***************************************************************************************");
		
		OHS os = pe.getOptionalHeader().getStandard();
		
		for (OHS.Property p : OHS.Property.values()) {
			System.out.println(p + ": " + os.valueOf(p));
		}
		
		@SuppressWarnings("rawtypes")
		OHA oa = pe.getOptionalHeader().getAdditional();
		
		if (oa instanceof OHA32) {
			for (OHA32.Property p : OHA32.Property.values()) {
				System.out.println(p + ": " + oa.valueOf(p));
			}
		} else {
			for (OHA64.Property p : OHA64.Property.values()) {
				System.out.println(p + ": " + oa.valueOf(p));
			}
		}
		
		System.out
					.println("***************************************************************************************");
		
		OHD od = pe.getOptionalHeader().getDirectories();
		for (OHD.Directory d : OHD.Directory.values()) {
			
			System.out
						.println("---------------------------------------------------------------------------------------");
			System.out.println(d);
			for (OHDD.Property p : OHDD.Property.values()) {
				
				System.out.println(p + ": " + od.valueOf(d, p));
			}
		}
		
		System.out
					.println("***************************************************************************************");
		
		STable sht = pe.getSectionTable();
		for (SH s : sht.values()) {
			System.out
						.println("---------------------------------------------------------------------------------------");
			System.out.println(s.toUtfString(SH.Property.NAME));
			for (SH.Property p : SH.Property.values()) {
				System.out.println(p + ": " + s.valueOf(p));
			}
		}
		
		System.out
					.println("***************************************************************************************");
		
		for (SH s : sht.values()) {
			String name = s.toUtfString(SH.Property.NAME).trim();
			// printing the other sections takes too long test 1
			if (name.equals(".version")) {
				System.out
							.println("---------------------------------------------------------------------------------------");
				System.out.println(s.toUtfString(SH.Property.NAME));
				
				if (s.sd.preamble != null) {
					System.out.println(new String(s.sd.preamble, "UTF-8"));
				}
				if (s.sd.data != null) {
					System.out.println(new String(s.sd.data, "UTF-8"));
				}
			}
		}
		
		System.out
					.println("***************************************************************************************");
		
		ImportDirTable idt = pe.getImageData().getImportTable();
		for (ImportDescriptor i : idt) {
			System.out
						.println("---------------------------------------------------------------------------------------");
			
			for (ImportDescriptor.Property p : ImportDescriptor.Property.values()) {
				// if (p == Import.Property.NAME) {
				// System.out.println(p + ": " + i.toString(p));
				//
				// } else {
				System.out.println(p + ": " + i.valueOf(p));
				// }
			}
		}
		
		System.out
					.println("***************************************************************************************");
		
		BoundImportDir bid = pe.getImageData().getBoundImports();
		if (bid != null) {
			for (BoundImport i : bid) {
				
				System.out
							.println("---------------------------------------------------------------------------------------");
				
				for (BoundImport.Property p : BoundImport.Property.values()) {
					// if (p == Import.Property.NAME) {
					// System.out.println(p + ": " + i.toString(p));
					//
					// } else {
					System.out.println(p + ": " + i.valueOf(p));
					// }
				}
			}
		}
		
		System.out
					.println("***************************************************************************************");
		
		RCTree rdt = pe.getImageData().getResourceTable();
		if (rdt != null) {
			traverseResDirTree(rdt, false);
		}
		
		System.out
					.println("***************************************************************************************");
		
		// PEAssembler.write(pe, new DataWriter(new File("test.log")));
		System.out.println(pe);
	}
	static void traverseResDirTree(RCTree tree, boolean subTree) {
		
		printIfSubTree(subTree);
		
		for (RCDH.Property p : RCDH.Property.values()) {
			System.out.println(p + ": " + tree.header.valueOf(p));
		}
		
		printIfSubTree(subTree);
		
		for (RCDirEntry e : tree.values()) {
			
			if (subTree) {
				System.out
							.println("***************************************************************************************");
			}
			for (RCDirEntry.Property p : RCDirEntry.Property.values()) {
				if (p == RCDirEntry.Property.NAME) {
					if (e.getName() != null) {
						System.out.println(p + ": " + e.getName());
					} else {
						System.out.println(p + ": " + e.valueOf(p));
					}
				}
				if (p == RCDirEntry.Property.OFFSET_TO_DATA) {
					System.out.println(p + ": " + e.valueOfBM(p, 0x7fffffff));
				}
			}
			if (!subTree) {
				System.out
							.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
			
			if (e.dir != null) {
				
				traverseResDirTree(e.dir, true);
				
			} else if (e.entry != null) {
				
				if (subTree) {
					System.out
								.println("---------------------------------------------------------------------------------------");
				}
				RCDataEntry de = (RCDataEntry) e.entry;
				
				for (RCDataEntry.Property p : RCDataEntry.Property.values()) {
					System.out.println(p + ": " + de.valueOf(p));
				}
			}			
		}
	}
	public static void printIfSubTree(boolean subTree) {
		if (!subTree) {
			System.out
						.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		} else {
			System.out
						.println("***************************************************************************************");
		}
	}
	
	static void traverseResDirEntry(RCTree tree) {
		
	}
}