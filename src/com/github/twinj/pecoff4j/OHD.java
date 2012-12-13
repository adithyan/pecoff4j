package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;

import com.github.twinj.headers.BlockHeader;
import com.github.twinj.pecoff4j.OHD.Directory;
import com.github.twinj.pecoff4j.OHDD;

public class OHD extends BlockHeader<Directory, OHDD.Property, OHDD> {
	
	private static final long serialVersionUID = 7571151114418883048L;
	
	/**
	 * Descriptions are not mone:
	 * http://msdn.microsoft.com/en-us/magazine/bb985997.aspx
	 * 
	 * @author Daniel Kemp
	 * 
	 */
	public static enum Directory {
		
		/**
		 * Export Directory: Points to the exports (an IMAGE_EXPORT_DIRECTORY
		 * structure).
		 */
		IMAGE_DIRECTORY_ENTRY_EXPORT("Export Directory"),
		
		/**
		 * Import Directory: Points to the imports (an array of
		 * IMAGE_IMPORT_DESCRIPTOR structures).
		 */
		IMAGE_DIRECTORY_ENTRY_IMPORT("Import Directory"),
		
		/**
		 * Resource Directory: Points to the resources (an IMAGE_RESOURCE_DIRECTORY
		 * structure.
		 */
		IMAGE_DIRECTORY_ENTRY_RESOURCE("Resource Directory"),
		
		/**
		 * Exception Directory: Points to the exception handler table (an array of
		 * IMAGE_RUNTIME_FUNCTION_ENTRY structures). CPU-specific and for
		 * table-based exception handling. Used on every CPU except the x86.
		 */
		IMAGE_DIRECTORY_ENTRY_EXCEPTION("Exception Directory"),
		
		/**
		 * Security Directory: Points to a list of WIN_CERTIFICATE structures,
		 * defined in WinTrust.H. Not mapped into memory as part of the image.
		 * Therefore, the VirtualAddress field is a file offset, rather than an RVA.
		 */
		IMAGE_DIRECTORY_ENTRY_SECURITY("Security Directory"),
		
		/**
		 * Base Relocation Table: Points to the base relocation information.
		 */
		IMAGE_DIRECTORY_ENTRY_BASERELOC("Base Relocation Table"),
		
		/**
		 * Debug Directory: Points to an array of IMAGE_DEBUG_DIRECTORY structures,
		 * each describing some debug information for the image. Early Borland
		 * linkers set the Size field of this IMAGE_DATA_DIRECTORY entry to the
		 * number of structures, rather than the size in bytes. To get the number of
		 * IMAGE_DEBUG_DIRECTORYs, divide the Size field by the size of an
		 * IMAGE_DEBUG_DIRECTORY.
		 */
		IMAGE_DIRECTORY_ENTRY_DEBUG("Debug Directory"),
		
		/**
		 * Architecture Specific Data: Points to architecture-specific data, which
		 * is an array of IMAGE_ARCHITECTURE_HEADER structures. Not used for x86 or
		 * IA-64, but appears to have been used for DEC/Compaq Alpha.
		 */
		IMAGE_DIRECTORY_ENTRY_ARCHITECTURE("Architecture Specific Data"),
		
		/**
		 * RVA of GP: The VirtualAddress field is the RVA to be used as the global
		 * pointer (gp) on certain architectures. Not used on x86, but is used on
		 * IA-64. The Size field isn't used. See the November 2000 Under The Hood
		 * column for more information on the IA-64 gp.
		 */
		IMAGE_DIRECTORY_ENTRY_GLOBALPTR("RVA of GP"),
		
		/**
		 * TLS Directory: Points to the Thread Local Storage initialisation section.
		 */
		IMAGE_DIRECTORY_ENTRY_TLS("TLS Directory"),
		
		/**
		 * Load Configuration Directory: Points to an IMAGE_LOAD_CONFIG_DIRECTORY
		 * structure. The information in an IMAGE_LOAD_CONFIG_DIRECTORY is specific
		 * to Windows NT, Windows 2000, and Windows XP (for example, the GlobalFlag
		 * value). To put this structure in your executable, you need to define a
		 * global structure with the name __load_config_used, and of type
		 * IMAGE_LOAD_CONFIG_DIRECTORY. For non-x86 architectures, the symbol name
		 * needs to be _load_config_used (with a single underscore). If you do try
		 * to include an IMAGE_LOAD_CONFIG_DIRECTORY, it can be tricky to get the
		 * name right in your C++ code. The symbol name that the linker sees must be
		 * exactly: __load_config_used (with two underscores). The C++ compiler adds
		 * an underscore to global symbols. In addition, it decorates global symbols
		 * with type information. So, to get everything right, in your C++ code,
		 * you'd have something like this: extern "C" IMAGE_LOAD_CONFIG_DIRECTORY
		 * _load_config_used = {...}
		 */
		IMAGE_DIRECTORY_ENTRY_LOAD_CONFIG("Load Configuration Directory"),
		
		/**
		 * Bound Import Directory in headers: Points to an array of
		 * IMAGE_BOUND_IMPORT_DESCRIPTORs, one for each DLL that this image has
		 * bound against. The timestamps in the array entries allow the loader to
		 * quickly determine whether the binding is fresh. If stale, the loader
		 * ignores the binding information and resolves the imported APIs normally.
		 */
		IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT(" Bound Import Directory in headers"),
		
		/**
		 * Import Address Table: Points to the beginning of the first Import Address
		 * Table (IAT). The IATs for each imported DLL appear sequentially in
		 * memory. The Size field indicates the total size of all the IATs. The
		 * loader uses this address and size to temporarily mark the IATs as
		 * read-write during import resolution.
		 */
		IMAGE_DIRECTORY_ENTRY_IAT("Import Address Table"),
		
		/**
		 * Delay Load Import Descriptors: Points to the delayload information, which
		 * is an array of CImgDelayDescr structures, defined in DELAYIMP.H from
		 * Visual C++. Delayloaded DLLs aren't loaded until the first call to an API
		 * in them occurs. It's important to note that Windows has no implicit
		 * knowledge of delay loading DLLs. The delayload feature is completely
		 * implemented by the linker and runtime library.
		 */
		IMAGE_DIRECTORY_ENTRY_DELAY_IMPORT("Delay Load Import Descriptors"),
		
		/**
		 * COM Runtime descriptor: This value has been renamed to
		 * IMAGE_DIRECTORY_ENTRY_COMHEADER in more recent updates to the system
		 * header files. It points to the top-level information for .NET information
		 * in the executable, including metadata. This information is in the form of
		 * an IMAGE_COR20_HEADER structure.
		 */
		IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR("COM Runtime descriptor"),
		
		/**
		 * Empty Placeholder
		 */
		IMAGE_DIRECTORY_EMPTY("Empty Placeholder");
		
		String winName;
		
		@SuppressWarnings("rawtypes")
		Class dirClazz;
		
		Directory(String winName) {
			this.winName = winName;
		}
	}
	
	public static final int IMAGE_NUMBEROF_DIRECTORY_ENTRIES = 16;
	private static final int SIZE_OF = 8;
	
	public OHD(ByteBuffer bytes) {
		super(Directory.class, bytes, SIZE_OF, IMAGE_NUMBEROF_DIRECTORY_ENTRIES);
	}
	
	public OHD() {
		super(Directory.class, SIZE_OF, IMAGE_NUMBEROF_DIRECTORY_ENTRIES);
	}
	
	public OHD(ByteBuffer bytes, int numDirs) {
		super(Directory.class, bytes, SIZE_OF, IMAGE_NUMBEROF_DIRECTORY_ENTRIES);
	}
	
	@Override
	// TDOD find a beter way to organize mapProperties in the class heirarchy
	public void mapProperties(ByteBuffer bytes) {
		byte[] barray;
		for (Directory d : Directory.values()) {
			barray = new byte[SIZE_OF];
			bytes.get(barray);
			OHDD dd = new OHDD(ByteBuffer.wrap(barray));
			dd.index = d.ordinal();
			dd.dirClass = d.dirClazz;
			dd.name = d.toString();
			this.put(d, dd);
		}
	}
}
