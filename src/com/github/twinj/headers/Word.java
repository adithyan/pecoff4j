package com.github.twinj.headers;

public class Word extends DatumAbstract<Short> {

	public Word(long position, byte[] bytes) {
		super(position, bytes, SIZE_OF);
	}
	
	public final static int SIZE_OF = 2;

	public Word() {
		super(SIZE_OF);
	}
	

	@Override
	public Short valueOf() {
			short v =  (short) (bytes[0] & 0xFF);
			// shifing to fit bit array into an int
			for (int i = 1; i < bytes.length; i++) {
				v = (short) (v | ( bytes[i] & 0xFF) << (i * BYTE_SIZE_IN_BITS));
			}
			return new Short(v);		
	}	
}
