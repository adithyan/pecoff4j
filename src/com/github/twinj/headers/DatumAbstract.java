package com.github.twinj.headers;

import java.lang.reflect.ParameterizedType;

public abstract class DatumAbstract<R extends Number> {
	
	protected static final int BYTE_SIZE_IN_BITS = 8;
	
	public byte[] bytes;
	public long position;
	R value = null;
	
	public final int sizeOf;
	
	public DatumAbstract(long position, byte[] bytes, int sizeOf) {
		this(sizeOf);		
		this.position = position;
		this.bytes = bytes;
	}
	
	public DatumAbstract(int sizeOf) {
		this.sizeOf = sizeOf;
	}
	
	public static Integer valueOfInteger(byte[] bytes) {
		int v = (int) bytes[0] & 0xFF;
		// shifing to fit bit array into an int
		for (int i = 1; i < bytes.length; i++) {
			v = v | ((int) bytes[i] & 0xFF) << (i * BYTE_SIZE_IN_BITS);
		}
		return new Integer(v);
	}
	
	public static Long valueOfLong(byte[] bytes) {
		long v = (long) bytes[0] & 0xFF;
		// shifing to fit bit array into an int
		for (int i = 1; i < bytes.length; i++) {
			v = v | ((long) bytes[i] & 0xFF) << (i * BYTE_SIZE_IN_BITS);
		}
		return new Long(v);
	}
	
	public abstract R valueOf();
	
	@SuppressWarnings("unchecked")
	public Class<R> get() throws Exception {
		ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
		
		return (Class<R>) superclass.getActualTypeArguments()[0];
	}
}