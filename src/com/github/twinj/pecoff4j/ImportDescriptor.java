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

public class ImportDescriptor extends DatumHeader<ImportDescriptor.Property> {
	
	private static final long serialVersionUID = -5303463280352581901L;
	
	public static enum Property {
		
		/**
		 * 0 for terminating null import descriptor.
		 */
		CHARACTERISTICS(DWORD, "Characteristics"),
		
		/**
		 * RVA to original unbound IAT (PIMAGE_THUNK_DATA)
		 */
		/* ORIGINAL_FIRST_THUNK(DWORD, "OriginalFirstThunk"), */
		
		/**
		 * 0 if not bound, -1 if bound, and real date\time stamp in
		 * IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT (new BIND) O.W. date/time stamp of DLL
		 * bound to (Old BIND)
		 */
		TIME_DATE_STAMP(DWORD, "TimeDateStamp"),
		
		/**
		 * -1 if no forwarders
		 */
		FORWARDER_CHAIN(DWORD, "ForwarderChain"),
		
		/**
		 * 
		 */
		NAME(DWORD, "Name"),
		
		/**
		 * RVA to IAT (if bound this IAT has actual addresses)
		 */
		FIRST_THUNK(DWORD, "FirstThunk"); //
		
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
	static int SIZE_OF = 20;
	
	public ImportDescriptor(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public ImportDescriptor() {
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
