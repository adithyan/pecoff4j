package com.github.twinj.headers;

import java.nio.ByteBuffer;

public abstract class BlockHeader<B extends Enum<B>, P extends Enum<P>, D extends DatumHeader<P>>
			extends
				Header<B, D> {
	
	private static final long serialVersionUID = 7832404363355804918L;
	
	public BlockHeader(Class<B> clazz, ByteBuffer bytes, int sizeOf, int arraySize) {
		super(clazz, sizeOf, arraySize, bytes);
	}
	
	public BlockHeader(Class<B> clazz, int sizeOf, int arraySize) {
		super(clazz, sizeOf, arraySize);
	}

	@SuppressWarnings("unchecked")
	public <R extends Number> R valueOf(B b, P p) {		
		DatumAbstract<?> d = get(b).get(p);
		return (R) (d.value == null ? d.valueOf() : d.value);
	}
	
	@Override
	public byte[] array() {
		ByteBuffer ret = ByteBuffer.wrap(new byte[sizeOf * arraySize]);
		for (B b : values(clazz)) {
			DatumHeader<P> dh = get(b);
			
			for (P p : values(dh.clazz)) {
				ret.put(dh.get(p).bytes);
			}
		}
		return ret.array();
	}	
}
