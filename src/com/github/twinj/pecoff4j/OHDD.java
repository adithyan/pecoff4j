package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;

import com.github.twinj.headers.DatumAbstract;
import com.github.twinj.headers.DatumHeader;

public class OHDD extends DatumHeader<OHDD.Property> {
	
	private static final long serialVersionUID = -8717370180744870556L;
	/**
	 * An array of IMAGE_DATA_DIRECTORY structures. Each structure contains the
	 * RVA and size of some important part of the executable (for instance,
	 * imports, exports, resources).
	 * 
	 * @author Daniel Kemp
	 * 
	 */
	public static enum Property {
		VIRTUAL_ADDRESS(DWORD, "VirtualAddress"),
		SIZE(DWORD, "Size");
		
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
	static int SIZE_OF =  8;
	
	public int index;
	public String name;
	public String section;
	
	Class<?> dirClass;
	
	public OHDD(ByteBuffer bytes) {
		super(Property.class, SIZE_OF, bytes);
	}
	
	public OHDD() {
		super(Property.class, SIZE_OF);
	}
	
	public void createTable() {
		// try {
		// dirClass.newInstance();
		// } catch (InstantiationException | IllegalAccessException ex) {
		// // TODO Auto-generated catch block
		// ex.printStackTrace();
		// }
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