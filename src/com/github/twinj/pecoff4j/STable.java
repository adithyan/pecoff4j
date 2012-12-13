package com.github.twinj.pecoff4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import com.github.twinj.headers.Header;
import com.github.twinj.pecoff4j.io.IDataReader;

public class STable extends LinkedHashMap<String, SH> {
	
	private static final long serialVersionUID = 7416896221895127810L;
	
	public int numberOfSections;
	
	public static enum Names {
		
		/**
		 * The default code section.
		 */
		TEXT(".text"),
		
		/**
		 * The default read/write data section. Global variables typically go here.
		 */
		DATA(".data"),
		
		/**
		 * The default read-only data section. String literals and C++/COM vtables
		 * are examples of items put into .rdata.
		 */
		RDATA(".rdata"),
		
		/**
		 * The imports table. It has become common practice (either explicitly, or
		 * via linker default behavior) to merge the .idata section into another
		 * section, typically .rdata. By default, the linker only merges the .idata
		 * section into another section when creating a release mode executable.
		 */
		IDATA(".idata"),
		
		/**
		 * The exports table. When creating an executable that exports APIs or data,
		 * the linker creates an .EXP file. The .EXP file contains an .edata section
		 * that's added into the final executable. Like the .idata section, the
		 * .edata section is often found merged into the .text or .rdata sections.
		 */
		EDATA(".edata"),
		
		/**
		 * The resources. This section is read-only. However, it should not be named
		 * anything other than .rsrc, and should not be merged into other sections.
		 */
		RSRC(".rsrc"),
		
		/**
		 * Uninitialized data. Rarely found in executables created with recent
		 * linkers. Instead, the VirtualSize of the executable's .data section is
		 * expanded to make enough room for uninitialized data.
		 */
		BSS(".bss"),
		
		/**
		 * Data added for supporting the C++ runtime (CRT). A good example is the
		 * function pointers that are used to call the constructors and destructors
		 * of static C++ objects. See the January 2001 Under The Hood column for
		 * details on this.
		 */
		CRT(".crt"),
		
		/**
		 * Data for supporting thread local storage variables declared with
		 * __declspec(thread). This includes the initial value of the data, as well
		 * as additional variables needed by the runtime.
		 */
		TLS(".tls"),
		
		/**
		 * The base relocations in an executable. Base relocations are generally
		 * only needed for DLLs and not EXEs. In release mode, the linker doesn't
		 * emit base relocations for EXE files. Relocations can be removed when
		 * linking with the /FIXED switch.
		 */
		REOLC(".reloc"),
		
		/**
		 * "Short" read/write data that can be addressed relative to the global
		 * pointer. Used for the IA-64 and other architectures that use a global
		 * pointer register. Regular-sized global variables on the IA-64 will go in
		 * this section.
		 */
		SDATA(".sdata"),
		
		/**
		 * "Short" read-only data that can be addressed relative to the global
		 * pointer. Used on the IA-64 and other architectures that use a global
		 * pointer register.
		 */
		SRDATA(".srdata"),
		
		/**
		 * The exception table. Contains an array of IMAGE_RUNTIME_FUNCTION_ENTRY
		 * structures, which are CPU-specific. Pointed to by the
		 * IMAGE_DIRECTORY_ENTRY_EXCEPTION slot in the DataDirectory. Used for
		 * architectures with table-based exception handling, such as the IA-64. The
		 * only architecture that doesn't use table-based exception handling is the
		 * x86.
		 */
		PDATA(".pdata"),
		
		/**
		 * Codeview format symbols in the OBJ file. This is a stream of
		 * variable-length CodeView format symbol records.
		 */
		DEBUG_$S(".debug$S"),
		
		/**
		 * Codeview format type records in the OBJ file. This is a stream of
		 * variable-length CodeView format type records.
		 */
		DEBUG_$T(".debug$T"),
		
		/**
		 * Found in the OBJ file when using precompiled headers.
		 */
		DEBUG_$P(".debug$P"),
		
		/**
		 * Contains linker directives and is only found in OBJs. Directives are
		 * ASCII strings that could be passed on the linker command line. For
		 * instance: "-defaultlib:LIBC"Directives are separated by a space
		 * character.
		 */
		DRECTVE(".drectve"),
		
		/**
		 * Delayload import data. Found in executables built in nonrelease mode. In
		 * release mode, the delayload data is merged into another section.
		 */
		DIDAT(".didat");
		
		String name;
		Names(String name) {
			this.name = name;
		}
	}
	
	public STable(int arraySize, IDataReader dr) throws IOException {
		super(arraySize);
		this.numberOfSections = arraySize;
		
		for (int i = 0; i < arraySize; i++) {
			
			SH sh = (SH) Header.parse(SH.class, dr);
			sh.index = i;
			put(sh.toUtfString(SH.Property.NAME), sh);
		}
	}
	
	public STable(int arraySize, ByteBuffer buffer) throws IOException {
		super(arraySize);
		this.numberOfSections = arraySize;
		byte [] barray = null;
		
		for (int i = 0; i < arraySize; i++) {
			buffer.get(barray);
			SH sh = new SH(ByteBuffer.wrap(barray));
			sh.index = i;
			put(sh.toUtfString(SH.Property.NAME), sh);
		}
	}

	public int[] virtualAddress;
	public int[] pointerToRawData;
	
	public int convertVirtualAddressToRawDataPointer(int virtualAddress) {
		for (int i = 0; i < this.virtualAddress.length; i++) {
			if (virtualAddress < this.virtualAddress[i]) {
				if (i > 0) {
					int prd = pointerToRawData[i - 1];
					int vad = this.virtualAddress[i - 1];
					return prd + virtualAddress - vad;
				} else {
					return virtualAddress;
				}
			}
		}
		
		// Hit the last item
		int prd = this.pointerToRawData[this.virtualAddress.length - 1];
		int vad = this.virtualAddress[this.virtualAddress.length - 1];
		return prd + virtualAddress - vad;
	}
	
}
