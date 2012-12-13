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
import java.nio.ByteOrder;

import com.github.twinj.headers.DatumAbstract;

public class RCDirEntry extends RCEntry<RCDirEntry.Property> {
	
	private static final long serialVersionUID = -5303463280352581901L;
	
	public static enum Property {
		
		/**
		 * The Name field is used to identify either a type of resource, a resource
		 * name, or a resource's language ID
		 */
		NAME(DWORD, "Name"),
		
		/**
		 * The OffsetToData field is always used to point to a sibling in the tree,
		 * either a directory node or a leaf node.
		 */
		OFFSET_TO_DATA(DWORD, "OffsetToData");
		
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
	private static final int SIZE_OF = 8;
	private static final int IMAGE_RESOURCE_NAME_IS_STRING = 0x80000000;
	private static final int IMAGE_RESOURCE_DATA_IS_DIRECTORY = 0x80000000;
	private static final int NOT_DIRECTORY = 0;
	private static final int NOT_STRING = 0;
	
	private String name = null;
	
	public RCDataEntry entry = null;
	public RCTree dir = null;
	public RCTree parent = null;
	
	public RCDirEntry(ByteBuffer bytes, boolean isNamed, RCTree parent) {
		this(bytes);
		this.parent = parent;
		if (isNamed) setName(bytes);
	}
	
	public RCDirEntry(ByteBuffer bytes) {
		super(RCDirEntry.Property.class, SIZE_OF, bytes);
		readResData(bytes);
	}
	
	public RCDirEntry() {
		super(RCDirEntry.Property.class, SIZE_OF);
	}
	
	public void mapProperties(ByteBuffer bytes) {
		byte[] barray;
		for (RCDirEntry.Property p : RCDirEntry.Property.values()) {
			barray = new byte[p.sizeOf];
			bytes.get(barray);
			DatumAbstract<?> d = null;
			try {
				d = p.clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException ignore) {}
			d.bytes = barray;
			d.position = bytes.position();
			put(p, (DatumAbstract<?>) d);
			System.err.print(p + ": " + (get(p).valueOf().intValue() & 0x7fffffff) + "\n");
		}
	}
	
	public void readResData(ByteBuffer buffer) {
		int offset = valueOf(RCDirEntry.Property.OFFSET_TO_DATA);
		if ((offset & IMAGE_RESOURCE_DATA_IS_DIRECTORY) != NOT_DIRECTORY) {			
		
		} else {
			buffer.position(offset);
			entry = new RCDataEntry(buffer);
			
		}
	}
	
	private void setName(ByteBuffer buffer) {
		int id = valueOf(RCDirEntry.Property.NAME);
		buffer.mark();
		if ((id & IMAGE_RESOURCE_NAME_IS_STRING) != NOT_STRING) { 
			// this is a second name test as the
			// dir table tells us if named
			
			buffer.position(id & 0x7fffffff);
			
			StringBuilder sb = new StringBuilder();
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int length = buffer.getShort();
			while ((length--) > 0) {
				sb.append(buffer.getChar());
			}
			name = sb.toString();
			System.err.println(name);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.reset();
		}
	}
	
	public String getName() {
		return name;
	}
	
	
}
