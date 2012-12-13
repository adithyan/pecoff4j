/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Original Contributors:
 *     Peter Smith
 *******************************************************************************/
package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;

import com.github.twinj.headers.DatumAbstract;
import com.github.twinj.headers.DatumHeader;

/**
 * DOS .EXE header
 * 
 * @author Daniel Kemp
 * @param <Datum>
 * 
 */
public class DOSH extends DatumHeader<DOSH.Property> {
	
	private static final long serialVersionUID = 4298603968505136706L;
	
	public static enum Property {
		
		/**
		 * Magic number: 'e_magic'
		 */
		MAGIC(WORD, "e_magic"),
		/**
		 * Bytes on last page of file: 'e_cblap'
		 */
		BYTES_ON_LAST_PAGE(WORD, "e_cblap"),
		
		/**
		 * Pages in file: 'e_cp'
		 */
		PAGES_IN_FILE(WORD, "e_cp"),
		
		/**
		 * Relocations : 'e_crlc'
		 */
		RELOCATIONS(WORD, "e_crlc"),
		
		/**
		 * Size of header in paragraphs: 'e_cparhdr'
		 */
		SIZE_OF_HEADER_IN_PARS(WORD, "e_cparhdr"),
		
		/**
		 * Minimum extra paragraphs needed: 'e_minalloc'
		 */
		MIN_EXTRA_PARS(WORD, "e_minalloc"),
		
		/**
		 * Maximum extra paragraphs needed: 'e_maxalloc'
		 */
		MAX_EXTRA_PARS(WORD, "e_maxalloc"),
		
		/**
		 * Initial (relative) SS value: 'e_ss'
		 */
		SS_INITIAL(WORD, "e_ss"),
		
		/**
		 * Initial SP value: 'e_sp'
		 */
		SP_INITIAL(WORD, "e_sp"),
		
		/**
		 * Checksum: 'e_csum'
		 */
		CHECK_SUM(WORD, "e_csum"),
		
		/**
		 * Initial IP value: 'e_ip'
		 */
		IP_INITIAL(WORD, "e_ip"),
		
		/**
		 * Initial (relative) CS value: 'e_cs'
		 */
		CS_INITIAL(WORD, "e_cs"),
		
		/**
		 * File address of relocation table: 'e_lfarlc'
		 */
		RELOC_TABLE_OFFSET_BD(WORD, "e_lfarlc"),
		
		/**
		 * Overlay number: 'e_ovno'
		 */
		OVERLAY_NUMBER(WORD, "e_ovno"),
		
		/**
		 * Reserved words: 'e_res'
		 */
		RES(WORD, RESERVERD_WORDS, "e_res"),
		
		/**
		 * OEM identifier (for e_oeminfo): 'e_oemid'
		 */
		OEMID(WORD, "e_oemid"),
		
		/**
		 * OEM information; e_oemid specific: 'e_oeminfo'
		 */
		OEMINFO(WORD, "e_oeminfo"),
		
		/**
		 * Reserved words: 'e_res2'
		 */
		RES2(WORD, RESERVERD_WORDS2, "e_res2"),
		
		/**
		 * File address of new exe header: 'e_lfanew'
		 * lives at 0x3c
		 */
		PE_HEADER_OFFSET_BD(DWORD, "e_lfanew"); 
		
		int sizeOf;
		String winName;
		Class<? extends DatumAbstract<?>> clazz;
		
		@SuppressWarnings("unchecked")
		Property(DatumAbstract<?> size, int arraySize, String winName) {
			this.sizeOf = size.sizeOf * arraySize;
			this.winName = winName;
			this.clazz = (Class<? extends DatumAbstract<?>>) size.getClass();
		}
		
		Property(DatumAbstract<?> size, String winName) {
			this(size, 1, winName);
		}
	}
	
	static int SIZE_OF = 64;
	
	// DEBUG private byte[] original;
	
	public static final int IMAGE_DOS_SIGNATURE = 0x54AD; // MZ
	
	public static final int DOS_MAGIC = 0;
	
	private static final int RESERVERD_WORDS = 4;
	private static final int RESERVERD_WORDS2 = 10;
	
	private int stubSize;
	
	public DOSH(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public DOSH() {
		super(Property.class, SIZE_OF);
	}
	
	// public boolean isValidMagic() {
	// return magic == DOS_MAGIC;
	// }
	
	public int getStubSize() {
		return stubSize;
	}
	
	public void setStubSize(int stubSize) {
		this.stubSize = stubSize;
	}
	
	/***
	 * TODO May not be necessary to fully calculate as could just minus header
	 * size from lfanew offset. This was the original contributors algorithm. May
	 * be needed to be this way. For now I do not know so am keeping.
	 */
	private void calculateStubSize() {
		// calc stub size
		
		int stubSize =  valueOf(DOSH.Property.PAGES_IN_FILE).intValue() * SIZE_OF_PAGE
					- (SIZE_OF_PAGE - valueOf(DOSH.Property.BYTES_ON_LAST_PAGE).intValue());
		if (stubSize > valueOf(DOSH.Property.PE_HEADER_OFFSET_BD).intValue()) stubSize = valueOf(DOSH.Property.PE_HEADER_OFFSET_BD).intValue();
		
		stubSize -= valueOf(DOSH.Property.SIZE_OF_HEADER_IN_PARS).intValue() * SIZE_OF_PARAGRAPH;
		
		this.stubSize = stubSize;
	}
	
	@Override
	protected void mapExtraProperties() {
		calculateStubSize();
	}
	
	@Override
	public void mapProperties(ByteBuffer bytes) {
		byte[] barray;
		for (Property p : Property.values()) {
			barray = new byte[p.sizeOf];
			bytes.get(barray);
			DatumAbstract<?> d = null;
			try {
				d = p.clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException ignore) {}
			d.bytes = barray;
			d.position = bytes.position();
			put(p, (DatumAbstract<?>) d);
			System.err.print(p + ": " + get(p).valueOf() + "\n");
		}
	}
	
}
