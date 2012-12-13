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
import com.github.twinj.pecoff4j.ExportDir.Property;

/**
 * The export directory table. See section 6.3.1 of the PE/COFF specification
 * v8.
 */
public class ExportDir extends DatumHeader<Property> {
	
	private static final long serialVersionUID = 7272253676433135475L;
	
	public static enum Property {
		//
		// Export Format
		//
		
		/**
		 * Flags for the exports. Currently, none are defined.
		 */
		CHARACTERISTICS(DWORD, "Characteristics"),
		
		/**
		 * The time/date that the exports were created. This field has the same
		 * definition as the IMAGE_NT_HEADERS.FileHeader. TimeDateStamp (number of
		 * seconds since 1/1/1970 GMT).
		 */
		TIME_DATE_STAMP(DWORD, "TimeDateStamp"),
		
		/**
		 * The major version number of the exports. Not used, and set to 0.
		 */
		MAJOR_VERSION(WORD, "MajorVersion"),
		
		/**
		 * The minor version number of the exports. Not used, and set to 0.
		 */
		MINOR_VERSION(WORD, "MinorVersion"),
		
		/**
		 * A relative virtual address (RVA) to an ASCII string with the DLL name
		 * associated with these exports (for example, KERNEL32.DLL).
		 */
		NAME(DWORD, "Name"),
		
		/**
		 * This field contains the starting ordinal value to be used for this
		 * executable's exports. Normally, this value is 1, but it's not required to
		 * be so. When looking up an export by ordinal, the value of this field is
		 * subtracted from the ordinal, with the result used as a zero-based index
		 * into the Export Address Table (EAT).
		 */
		BASE(DWORD, "Base"),
		
		/**
		 * The number of entries in the EAT. Note that some entries may be 0,
		 * indicating that no code/data is exported with that ordinal value.
		 */
		NUMBER_OF_FUNCTIONS(DWORD, "NumberOfFunctions"),
		
		/**
		 * The number of entries in the Export Names Table (ENT). This value will
		 * always be less than or equal to the NumberOf-Functions field. It will be
		 * less when there are symbols exported by ordinal only. It can also be less
		 * if there are numeric gaps in the assigned ordinals. This field is also
		 * the size of the export ordinal table (below).
		 */
		NUMBER_OF_NAMES(DWORD, "NumberOfNames"),
		
		/**
		 * The RVA of the EAT. The EAT is an array of RVAs. Each nonzero RVA in the
		 * array corresponds to an exported symbol.
		 */
		ADDRESS_OF_FUNCTIONS(DWORD, "AddressOfFunctions"),
		
		/**
		 * The RVA of the ENT. The ENT is an array of RVAs to ASCII strings. Each
		 * ASCII string corresponds to a symbol exported by name. This table is
		 * sorted so that the ASCII strings are in order. This allows the loader to
		 * do a binary search when looking for an exported symbol. The sorting of
		 * the names is binary (like the C++ RTL strcmp function provides), rather
		 * than a locale-specific alphabetic ordering.
		 */
		ADDRESS_OF_NAMES(DWORD, "AddressOfNames"),
		
		/**
		 * The RVA of the export ordinal table. This table is an array of WORDs.
		 * This table maps an array index from the ENT into the corresponding export
		 * address table entry.
		 */
		ADDRESS_OF_NAME_ORDINALS(DWORD, "AddressOfNameOrdinals");
		
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
	static int SIZE_OF = 40;
	
	public ExportDir(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public ExportDir() {
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
