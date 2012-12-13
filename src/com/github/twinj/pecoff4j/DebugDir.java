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
import com.github.twinj.pecoff4j.DebugDir.Property;
;
/**
 * Encapsulates the Debug Directory (Image Only). Section 6.1.1 of the PE/COFF
 * spec v8.
 */
public class DebugDir extends DatumHeader<Property> {
	
	private static final long serialVersionUID = 8434276451024941950L;
	
	public static enum Property {
		
		/**
		 * Unused and set to 0.
		 */
		CHARACTERISTICS(DWORD, "Characteristics"),
		
		/**
		 * The time/date stamp of this debug information (number of seconds since
		 * 1/1/1970, GMT).
		 */
		TIME_DATE_STAMP(DWORD, "TimeDateStamp"),
		
		/**
		 * The major version of this debug information. Unused.
		 */
		MAJOR_VERSION(WORD, "MajorVersion"),
		
		/**
		 * The minor version of this debug information. Unused.
		 */
		MINOR_VERSION(WORD, "MinorVersion"),
		
		/**
		 * The type of the debug information. The following types are the most
		 * commonly encountered: IMAGE_DEBUG_TYPE_COFF IMAGE_DEBUG_TYPE_CODEVIEW //
		 * Including PDB files IMAGE_DEBUG_TYPE_FPO // Frame pointer omission
		 * IMAGE_DEBUG_TYPE_MISC // IMAGE_DEBUG_MISC IMAGE_DEBUG_TYPE_OMAP_TO_SRC
		 * IMAGE_DEBUG_TYPE_OMAP_FROM_SRC IMAGE_DEBUG_TYPE_BORLAND // Borland format
		 */
		TYPE(DWORD, "Type"),
		
		/**
		 * The size of the debug data in this file. Doesn't count the size of
		 * external debug files such as .PDBs.
		 */
		SIZE_OF_DATA(DWORD, "SizeOfData"),
		
		/**
		 * The RVA of the debug data, when mapped into memory. Set to 0 if the debug
		 * data isn't mapped in.
		 */
		ADDRESS_OF_RAW_DATA(DWORD, "AddressOfRawData"),
		
		/**
		 * The file offset of the debug data (not an RVA).
		 */
		POINTER_TO_RAW_DATA(DWORD, "PointerToRawData");
		
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
	static int SIZE_OF = 28;
	
	public DebugDir(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public DebugDir() {
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
