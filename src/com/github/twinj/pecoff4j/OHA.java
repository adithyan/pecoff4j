package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;

import com.github.twinj.headers.DatumHeader;

public abstract class OHA<P extends Enum<P>> extends DatumHeader<P> {
	
	private static final long serialVersionUID = -3363053075105692326L;

	public OHA(Class<P> clazz, int sizeOf, ByteBuffer bytes) {
		super(clazz, sizeOf, bytes);
	}
	
	public OHA(Class<P> clazz, int sizeOf) {
		super(clazz, sizeOf);
	}
}
