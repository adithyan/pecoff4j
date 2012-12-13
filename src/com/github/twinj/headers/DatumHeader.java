package com.github.twinj.headers;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 
 * @author Daniel Kemp
 * 
 * @param <P>
 *          Property enum
 * @param <R>
 *          Datum DataType eg Integer
 */
public abstract class DatumHeader<P extends Enum<P>> extends Header<P, DatumAbstract<?>> {
	private static final long serialVersionUID = 7850398000071329260L;
	
	public DatumHeader(Class<P> clazz, int sizeOf, ByteBuffer bytes) {
		super(clazz, sizeOf, bytes);
	}
	
	public DatumHeader(Class<P> clazz, int sizeOf) {
		super(clazz, sizeOf);
	}

	@SuppressWarnings("unchecked")
	public  <N extends Number> N valueOf(P p) {
		DatumAbstract<?> h = (DatumAbstract<?>) get(p);
		return (N) (h.value == null ? get(p).valueOf() : h.value);
	}
	
	/**
	 * Returns an asci string of the bytes.
	 * 
	 * @param p
	 * @return
	 */
	public String toUtfString(P p) {
		DatumAbstract<?> h = (DatumAbstract<?>) get(p);
		try {
			return new String(h.bytes, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {};
		return null;
	}
	
	/**
	 * Bitmask a value
	 * @param p
	 * @return
	 */
	public int valueOfBM(P p, int bitMask) {
		DatumAbstract<?> h = (DatumAbstract<?>) get(p);
	return ((Integer) (h.valueOf()) & bitMask);
	}
	
	@Override
	public byte[] array() {
		ByteBuffer ret = ByteBuffer.wrap(new byte[sizeOf]);
		for (P p : values(clazz)) {
			ret.put(get(p).bytes);
		}
		return ret.array();
	}
}
