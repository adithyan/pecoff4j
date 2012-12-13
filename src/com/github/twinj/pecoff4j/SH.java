package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;

import com.github.twinj.headers.DatumAbstract;
import com.github.twinj.headers.DatumHeader;
import com.github.twinj.pecoff4j.io.PE.SectData;

public class SH extends DatumHeader<SH.Property> {
	
	private static final long serialVersionUID = 3898815380046915523L;
	
	public static final int IMAGE_SIZEOF_SHORT_NAME = 8;
	
	public enum Property {
		
		/**
		 * The ASCII name of the section. A section name is not guaranteed to be
		 * null-terminated. If you specify a section name longer than eight
		 * characters, the linker truncates it to eight characters in the
		 * executable. A mechanism exists for allowing longer section names in OBJ
		 * files. Section names often start with a period, but this is not a
		 * requirement. Section names with a $ in the name get special treatment
		 * from the linker. Sections with identical names prior to the $ character
		 * are merged. The characters following the $ provide an alphabetic ordering
		 * for how the merged sections appear in the final section. There's quite a
		 * bit more to the subject of sections with $ in the name and how they're
		 * combined, but the details are outside the scope of this article
		 */
		NAME(BYTE , IMAGE_SIZEOF_SHORT_NAME, "Name"),
		
		/**
		 * Indicates the actual, used size of the section. This field may be larger
		 * or smaller than the SizeOfRawData field. If the VirtualSize is larger,
		 * the SizeOfRawData field is the size of the initialized data from the
		 * executable, and the remaining bytes up to the VirtualSize should be
		 * zero-padded. This field is set to 0 in OBJ files
		 * 
		 * This values is a union as defined by Winnt.h. Which gets overwritten when
		 * loaded.
		 * 
		 * PHYSICAL_ADDRESS(DWORD, "PhysicalAddress"),
		 */
		VIRTUAL_SIZE(DWORD, "VirtualSize"),
		
		/**
		 * In executables, indicates the RVA where the section begins in memory.
		 * Should be set to 0 in OBJs.
		 */
		VIRTUAL_ADDRESS(DWORD, "VirtualAddress"),
		
		/**
		 * The size (in bytes) of data stored for the section in the executable or
		 * OBJ. For executables, this must be a multiple of the file alignment given
		 * in the PE header. If set to 0, the section is uninitialized data.
		 */
		SIZE_OF_RAW_DATA(DWORD, "SizeOfRawData"),
		
		/**
		 * The file offset where the data for the section begins. For executables,
		 * this value must be a multiple of the file alignment given in the PE
		 * header.
		 */
		POINTER_TO_RAW_DATA(DWORD, "PointerToRawData"),
		
		/**
		 * The file offset of relocations for this section. This is only used in
		 * OBJs and set to zero for executables. In OBJs, it points to an array of
		 * IMAGE_RELOCATION structures if non-zero.
		 */
		POINTER_TO_RELOCATIONS(DWORD, "PointerToRelocations"),
		
		/**
		 * The file offset for COFF-style line numbers for this section. Points to
		 * an array of IMAGE_LINENUMBER structures if non-zero. Only used when COFF
		 * line numbers are emitted.
		 */
		POINTER_TO_LINE_NUMBERS(DWORD, "PointerToLinenumbers"),
		
		/**
		 * The number of relocations pointed to by the PointerToRelocations field.
		 * Should be 0 in executables.
		 */
		NUMBER_OF_RELOCATIONS(WORD, "NumberOfRelocations"),
		
		/**
		 * The number of line numbers pointed to by the NumberOfRelocations field.
		 * Only used when COFF line numbers are emitted.
		 */
		NUMBER_OF_LINENUMBERS(WORD, "NumberOfLinenumbers"),
		
		/**
		 * Flags OR'ed together, indicating the attributes of this section. Many of
		 * these flags can be set with the linker's /SECTION option. Common values
		 * include those listed in Figure 7.
		 */
		CHARACTERISTICS(DWORD, "Characteristics");
		
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
	
	static int SIZE_OF =  40;
	
	public int index;
	public OHD.Directory dir;
	public SectData sd;
	
	public SH(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public SH() {
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