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

public class RCDataEntry extends RCEntry<RCDataEntry.Property> {
	
	private static final long serialVersionUID = -5303463280352581901L;
	
	public static enum Property {
		
		/**
		 *
		 */
		OFFSET_TO_DATA(DWORD, "OffsetToData"),
		
		/**
		 * 
		 */
		SIZE(DWORD, "Size"),
		
		/**
		 * 
		 */
		CODE_PAGE(DWORD, "CodePage"),
		
		/**
		 * 
		 */
		RESERVED(DWORD, "Reserved");

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
	
	static final int SIZE_OF = 16;
	
	public RCDataEntry(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}	
	
	public RCDataEntry() {
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
	}
	
}
