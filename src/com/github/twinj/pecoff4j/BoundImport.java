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
import com.github.twinj.pecoff4j.BoundImport.Property;

public class BoundImport extends DatumHeader<Property> {

	private static final long serialVersionUID = 7615482031907300483L;

	public static enum Property {
		
		TIME_DATE_STAMP(DWORD, "TimeDateStamp"),
		OFFSET_MODULE_NAME(WORD, "OffsetModuleName"),
		NUMBER_OF_MODULE_FORWARDER_REFS(WORD, "NumberOfModuleForwarderRefs");
		// Array of zero or more IMAGE_BOUND_FORWARDER_REF follows
		
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
	static int SIZE_OF = 8;
	
	public String moduleName;
	public BoundImport(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public BoundImport() {
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
