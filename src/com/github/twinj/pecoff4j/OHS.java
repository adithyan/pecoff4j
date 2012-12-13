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

/**
 * NT Optional Header standard COFF fields
 * 
 * @author Daniel Kemp
 * 
 */
public class OHS extends DatumHeader<OHS.Property> {
	
	private static final long serialVersionUID = -3737926994845645936L;
	
	/**
	 * Descriptions are not mone:
	 * http://msdn.microsoft.com/en-us/magazine/bb985997.aspx
	 * 
	 * @author Daniel Kemp
	 * 
	 */
	public static enum Property {
		
		//
		// Standard fields. The standard fields are those common to the Common
		// Object File Format (COFF) This will work for both pe32 and pe32plus.
		// 'Base of Data' field has been moved to OHA32.
		//
		
		/**
		 * A signature WORD, identifying what type of header this is. The two most
		 * common values are IMAGE_NT_OPTIONAL_HDR32_MAGIC 0x10b and
		 * IMAGE_NT_OPTIONAL_HDR64_MAGIC 0x20b.
		 */
		MAGIC(WORD, "Magic"),
		
		/**
		 * The major version of the linker used to build this executable. For PE
		 * files from the Microsoft linker, this version number corresponds to the
		 * Visual Studio version number (for example, version 6 for Visual Studio
		 * 6.0).
		 */
		MAJOR_LINKER_VERSION(BYTE, "MajorLinkerVersion"),
		
		/**
		 * The minor version of the linker used to build this executable.
		 */
		MINOR_LINKER_VERSION(BYTE, "MinorLinkerVersion"),
		
		/**
		 * The combined total size of all sections with the IMAGE_SCN_CNT_CODE
		 * attribute
		 */
		SIZE_OF_CODE(DWORD, "SizeOfCode"),
		
		/**
		 * The combined size of all initialized data sections.
		 */
		SIZE_OF_INITIALIZED_DATA(DWORD, "SizeOfInitializedData"),
		
		/**
		 * The size of all sections with the uninitialised data attributes. This
		 * field will often be 0, since the linker can append uninitialised data to
		 * the end of regular data sections.
		 */
		SIZE_OF_UNINITIALIZED_DATA(DWORD, "SizeOfUninitializedData"),
		
		/**
		 * The RVA of the first code byte in the file that will be executed. For
		 * DLLs, this entry point is called during process initialisation and
		 * shutdown and during thread creations/destructions. In most executables,
		 * this address doesn't directly point to main, WinMain, or DllMain. Rather,
		 * it points to runtime library code that calls the aforementioned
		 * functions. This field can be set to 0 in DLLs, and none of the previous
		 * notifications will be received. The linker /NOENTRY switch sets this
		 * field to 0.
		 */
		ADDRESS_OF_ENTRY_POINT(DWORD, "AddressOfEntryPoint"),
		
		/**
		 * The RVA of the first byte of code when loaded in memory.
		 */
		BASE_OF_CODE(DWORD, "BaseOfCode");
		public int offset;
		int sizeOf;
		String winName;
		Class<? extends DatumAbstract<?>> clazz;
		
		@SuppressWarnings("unchecked")
		Property(DatumAbstract<?> size, int arraySize, String winName) {
			this.sizeOf = size.sizeOf * arraySize;
			this.winName = winName;
			this.clazz = (Class<? extends DatumAbstract<?>>) size.getClass();			
			inc(sizeOf);
		}
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
	static int SIZE_OF = 24;
	
	public static final int MAGIC_PE32 = 0x0b01; // 2817 | 0x010b - 267
	public static final int MAGIC_PE32plus = 0x0b02; // | 0x020b
	
	public OHS(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public OHS() {
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
	
	public boolean isValid() {
		return valueOf(Property.MAGIC).intValue() == MAGIC_PE32
					|| valueOf(Property.MAGIC).intValue() == MAGIC_PE32plus;
	}
	
	public boolean isPE32plus() {
		return valueOf(Property.MAGIC).intValue() == MAGIC_PE32plus;
	}
}
