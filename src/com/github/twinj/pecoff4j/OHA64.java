package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;

import com.github.twinj.headers.DatumAbstract;

public class OHA64 extends OHA<OHA64.Property> {
	
	private static final long serialVersionUID = 4406991552473580400L;
	
	/**
	 * Descriptions are not mone:
	 * http://msdn.microsoft.com/en-us/magazine/bb985997.aspx
	 * 
	 * @author Daniel Kemp
	 * 
	 */
	public static enum Property {
		
		//
		// NT additional fields.
		//
		
		/**
		 * The preferred load address of this file in memory. The loader attempts to
		 * load the PE file at this address if possible (that is, if nothing else
		 * currently occupies that memory, it's aligned properly and at a legal
		 * address, and so on). If the executable loads at this address, the loader
		 * can skip the step of applying base relocations (described in Part 2 of
		 * this article). For EXEs, the default ImageBase is 0x400000. For DLLs,
		 * it's 0x10000000. The ImageBase can be set at link time with the /BASE
		 * switch, or later with the REBASE utility.
		 */
		IMAGE_BASE(ULONGLONG, "ImageBase"),
		
		/**
		 * The alignment of sections when loaded into memory. The alignment must be
		 * greater or equal to the file alignment field (mentioned next). The
		 * default alignment is the page size of the target CPU. For user mode
		 * executables to run under Windows 9x or Windows Me, the minimum alignment
		 * size is a page (4KB). This field can be set with the linker /ALIGN
		 * switch.
		 */
		SECTION_ALIGNMENT(DWORD, "SectionAlignment"),
		
		/**
		 * The alignment of sections within the PE file. For x86 executables, this
		 * value is usually either 0x200 or 0x1000. The default has changed with
		 * different versions of the Microsoft linker. This value must be a power of
		 * 2, and if the SectionAlignment is less than the CPU's page size, this
		 * field must match the SectionAlignment. The linker switch /OPT:WIN98 sets
		 * the file alignment on x86 executables to 0x1000, while /OPT:NOWIN98 sets
		 * the alignment to 0x200.
		 */
		FILE_ALIGNMENT(DWORD, "FileAlignment"),
		
		/**
		 * The major version number of the required operating system. With the
		 * advent of so many versions of Windows, this field has effectively become
		 * irrelevant.
		 */
		MAJOR_OPERATING_SYSTEM_VERSION(WORD, "MajorOperatingSystemVersion"),
		
		/**
		 * The minor version number of the required OS.
		 */
		MINOR_OPERATING_SYSTEM_VERSION(WORD, "MinorOperatingSystemVersion"),
		
		/**
		 * The major version number of this file. Unused by the system and can be 0.
		 * It can be set with the linker /VERSION switch.
		 */
		MAJOR_IMAGE_VERSION(WORD, "MajorImageVersion"),
		
		/**
		 * The minor version number of this file.
		 */
		MINOR_IMAGE_VERSION(WORD, "MinorImageVersion"),
		
		/**
		 * The major version of the operating subsystem needed for this executable.
		 * At one time, it was used to indicate that the newer Windows 95 or Windows
		 * NT 4.0 user interface was required, as opposed to older versions of the
		 * Windows NT interface. Today, because of the proliferation of the various
		 * versions of Windows, this field is effectively unused by the system and
		 * is typically set to the value 4. Set with the linker /SUBSYSTEM switch.
		 */
		MAJOR_SUBSYSTEM_VERSION(WORD, "MajorSubsystemVersion"),
		
		/**
		 * The minor version of the operating subsystem needed for this executable.
		 */
		MINOR_SUBSYSTEM_VERSION(WORD, "MinorSubsystemVersion"),
		
		/**
		 * Another field that never took off. Typically set to 0.
		 */
		WIN32_VERSION_VALUE(DWORD, "Win32VersionValue"),
		
		/**
		 * SizeOfImage contains the RVA that would be assigned to the section
		 * following the last section if it existed. This is effectively the amount
		 * of memory that the system needs to reserve when loading this file into
		 * memory. This field must be a multiple of the section alignment.
		 */
		SIZE_OF_IMAGE(DWORD, "SizeOfImage"),
		
		/**
		 * The combined size of the MS-DOS header, PE headers, and section table.
		 * All of these items will occur before any code or data sections in the PE
		 * file. The value of this field is rounded up to a multiple of the file
		 * alignment.
		 */
		SIZE_OF_HEADERS(DWORD, "SizeOfHeaders"),
		
		/**
		 * The checksum of the image. The CheckSumMappedFile API in IMAGEHLP.DLL can
		 * calculate this value. Checksums are required for kernel-mode drivers and
		 * some system DLLs. Otherwise, this field can be 0. The checksum is placed
		 * in the file when the /RELEASE linker switch is used.
		 */
		CHECK_SUM(DWORD, "CheckSum"),
		
		/**
		 * An enum value indicating what subsystem (user interface type) the
		 * executable expects. This field is only important for EXEs. Important
		 * values include: IMAGE_SUBSYSTEM_NATIVE // Image doesn't require a
		 * subsystem IMAGE_SUBSYSTEM_WINDOWS_GUI // Use the Windows GUI
		 * IMAGE_SUBSYSTEM_WINDOWS_CUI // Run as a console mode application // When
		 * run, the OS creates a console // window for it, and provides stdin, //
		 * stdout, and stderr file handles
		 */
		SUBSYSTEM(WORD, "Subsystem"),
		
		/**
		 * Flags indicating characteristics of this DLL. These correspond to the
		 * IMAGE_DLLCHARACTERISTICS_xxx fields #defines. Current values are:
		 * IMAGE_DLLCHARACTERISTICS_NO_BIND // Do not bind this image
		 * IMAGE_DLLCHARACTERISTICS_WDM_DRIVER // Driver uses WDM model
		 * IMAGE_DLLCHARACTERISTICS_TERMINAL_SERVER_AWARE // When the terminal
		 * server loads // an application that is not // Terminal- Services-aware,
		 * it // also loads a DLL that contains // compatibility code
		 */
		DLL_CHARACTERISTICS(WORD, "DllCharacteristics"),
		
		/**
		 * In EXE files, the maximum size the initial thread in the process can grow
		 * to. This is 1MB by default. Not all this memory is committed initially.
		 */
		SIZE_OF_STACK_RESERVE(ULONGLONG, "SizeOfStackReserve"),
		
		/**
		 * In EXE files, the amount of memory initially committed to the stack. By
		 * default, this field is 4KB.
		 */
		SIZE_OF_STACK_COMMIT(ULONGLONG, "SizeOfStackCommit"),
		
		/**
		 * In EXE files, the initial reserved size of the default process heap. This
		 * is 1MB by default. In current versions of Windows, however, the heap can
		 * grow beyond this size without intervention by the user.
		 */
		SIZE_OF_HEAP_RESERVE(ULONGLONG, "SizeOfHeapReserve"),
		
		/**
		 * In EXE files, the size of memory committed to the heap. By default, this
		 * is 4KB.
		 */
		SIZE_OF_HEAP_COMMIT(ULONGLONG, "SizeOfHeapCommit"),
		
		/**
		 * This is obsolete.
		 */
		LOADER_FLAGS(DWORD, "LoaderFlags"),
		
		/**
		 * At the end of the IMAGE_NT_HEADERS structure is an array of
		 * IMAGE_DATA_DIRECTORY structures. This field contains the number of
		 * entries in the array. This field has been 16 since the earliest releases
		 * of Windows NT.
		 */
		NUMBER_OF_RVA_AND_SIZES(DWORD, "NumberOfRvaAndSizes");
		
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
	static int SIZE_OF = 88;
	
	public OHA64(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public OHA64() {
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
