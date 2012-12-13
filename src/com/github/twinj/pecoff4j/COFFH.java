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
 * File header format. Common Object File Format (COFF)
 * 
 * @author Daniel Kemp
 * 
 */
public class COFFH extends DatumHeader<COFFH.Property> {
	
	private static final long serialVersionUID = -6269668502608322698L;
	
	public static enum Property {
		
		/**
		 * Number identifying type of target machine.
		 */
		MACHINE(WORD, "Machine"),
		
		/**
		 * Number of sections; indicates size of the Section Table, which
		 * immediately follows the headers.
		 */
		NUMBER_OF_SECTIONS(WORD, "NumberOfSections"),
		
		/**
		 * Time and date the file was created.
		 */
		TIME_DATE_STAMP(DWORD, "TimeDateStamp"),
		
		/**
		 * Offset, within the COFF file, of the symbol table.
		 */
		POINTER_TO_SYMBOL_TABLE(DWORD, "PointerToSymbolTable"),
		
		/**
		 * Number of entries in the symbol table. This data can be used in locating
		 * the string table, which immediately follows the symbol table.
		 */
		NUMBER_OF_SYMBOLS(DWORD, "NumberOfSymbols"),
		
		/**
		 * Size of the optional header, which is included for executable files but
		 * not object files. An object file should have a value of 0 here.
		 */
		SIZE_OF_OPTIONAL_HEADER(WORD, "SizeOfOptionalHeader"),
		
		/**
		 * Flags indicating attributes of the file.
		 */
		CHARACTERISTICS(WORD, "Characteristics");
		
		int sizeOf;
		String winName;
		Class<? extends DatumAbstract<?>> clazz;
		
		@SuppressWarnings("unchecked")
		Property(DatumAbstract<?> size, int arraySize, String winName) {
			this.sizeOf = size.sizeOf * arraySize;
			this.winName = winName;
			this.clazz = (Class<? extends DatumAbstract<?>>) size.getClass();
			offset = offset();
			inc(sizeOf);
		}
		public int offset;
		static int SIZE_OF = 0;
		
		static int offset() {
			return SIZE_OF;
		}
		static void inc(int sizeOf) {
			SIZE_OF += sizeOf;
		}		
		Property(DatumAbstract<?> size, String winName) {
			this(size, 1, winName);
		}		
	}
	public static final int SIZE_OF = 20;
	
	public COFFH(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public COFFH() {
		super(Property.class, SIZE_OF);
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
