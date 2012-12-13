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

public class LoadConfigDir extends DatumHeader<LoadConfigDir.Property> {
	
	private static final long serialVersionUID = -5654823843653172223L;
	
	public static enum Property {
		SIZE(DWORD, "Size"),
		TIME_DATE_STAMP(DWORD, "TimeDateStamp"),
		MAJOR_VERSION(WORD, "MajorVersion"),
		MINOR_VERSION(WORD, "MinorVersion"),
		GLOBAL_FLAGS_CLEAR(DWORD, "GlobalFlagsClear"),
		GLOBAL_FLAGS_SET(DWORD, "GlobalFlagsSet"),
		CRITICAL_SECTION_DEFAUL_TTIMEOUT(DWORD, "CriticalSectionDefaultTimeout"),
		DECOMMIT_FREE_BLOCK_THRESHOLD(DWORD, "DeCommitFreeBlockThreshold"),
		DECOMMIT_TOTAL_FREE_THRESHOLD(DWORD, "DeCommitTotalFreeThreshold"),
		LOCK_PREFIX_TABLE(DWORD, "LockPrefixTable"), // VA
		MAXIMUM_ALLOCATION_SIZE(DWORD, "MaximumAllocationSize"),
		VIRTUAL_MEMORY_THRESHOLD(DWORD, "VirtualMemoryThreshold"),
		PROCESS_HEAP_FLAGS(DWORD, "ProcessHeapFlags"),
		PROCESS_AFFINITY_MASK(DWORD, "ProcessAffinityMask"),
		CSD_VERSION(WORD, "CSDVersion"),
		RESERVED1(WORD, "Reserved1"),
		EDIT_LIST(DWORD, "EditList"), // VA
		SECURITY_COOKIE(DWORD, "SecurityCookie"), // VA
		SE_HANDLER_TABLE(DWORD, "SEHandlerTable"), // VA
		SE_HANDLER_COUNT(DWORD, "SEHandlerCount");
		
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
	static int SIZE_OF = 72;
	
	public LoadConfigDir(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public LoadConfigDir() {
		super(Property.class, SIZE_OF);
	}
	
	public void mapProperties(ByteBuffer bytes) {
		byte[] barray;
		for (Property p : Property.values()) {
			if (bytes.position() == bytes.limit()) {
				return;
			}
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
