package com.github.twinj.headers;

public class ULongLong extends DatumAbstract<Long> {
	
	public ULongLong(long position, byte[] bytes) {
		super(position, bytes, SIZE_OF);
	}
	public final static int SIZE_OF = 8;
	
	public ULongLong() {
		super(SIZE_OF);		
	}
	
	@Override
	public Long valueOf() {
		int v = bytes[0] & 0xFF;
		// shifing to fit bit array into an int
		for (int i = 1; i < bytes.length; i++) {
			v = v | (bytes[i] & 0xFF) << (i * BYTE_SIZE_IN_BITS);
		}
		return new Long(v);
	}
}
