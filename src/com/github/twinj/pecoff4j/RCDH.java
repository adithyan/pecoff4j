/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;

import com.github.twinj.headers.DatumAbstract;
import com.github.twinj.headers.DatumHeader;

public class RCDH extends DatumHeader<RCDH.Property> {
	
	private static final long serialVersionUID = -5303463280352581901L;
	
	public static enum Property {
		
		/**
		 * 
		 */
		CHARACTERISTICS(DWORD, "Characteristics"),		
		/**
		 * RVA to original unbound IAT (PIMAGE_THUNK_DATA)
		 */
		/* ORIGINAL_FIRST_THUNK(DWORD, "OriginalFirstThunk"), Part of a union */
		
		/**
		 * 
		 */
		TIME_DATE_STAMP(DWORD, "TimeDateStamp"),
		
		/**
		 * 
		 */
		MAJOR_VERSION(WORD, "MajorVersion"),
		
		/**
		 * 
		 */
		MINOR_VERSION(WORD, "MinorVersion"),
		
		/**
		 *
		 */
		NUMBER_OF_NAMED_ENTRIES(WORD, "NumberOfNamedEntries"),
		
		/**
		 * 
		 */
		NUMBER_OF_ID_ENTRIES(WORD, "NumberOfIdEntries");
		
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
	static final int SIZE_OF = 16;
	public int numEntries;
	
	public RCDH(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public RCDH() {
		super(Property.class, SIZE_OF);
	}
	
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
		this.numEntries = this.valueOf(Property.NUMBER_OF_ID_ENTRIES).intValue() + this.valueOf(Property.NUMBER_OF_NAMED_ENTRIES).intValue();
	}
}
