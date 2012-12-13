package com.github.twinj.headers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;

import com.github.twinj.pecoff4j.PropertyInterface;
import com.github.twinj.pecoff4j.io.IDataReader;
import com.github.twinj.pecoff4j.io.IDataWriter;

/**
 * 
 * @author Daniel Kemp
 * 
 * @param <T>
 * @param <R>
 */
public abstract class Header<P extends Enum<P>, M> extends EnumMap<P, M> {
	
	private static final long serialVersionUID = 4105920592170545262L;
	
	public final static ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	
	protected static final int BYTE_SIZE_IN_BITS = 8;
	
	protected static final Integer SIZE_OF_PAGE = 512;
	protected static final int SIZE_OF_PARAGRAPH = 16;
	
	//protected static final int BYTE = 1; // unsigned byte
	//protected static final int WORD = 2; // unsigned short
	//protected static final int DWORD = 4; // unsigned int
	//protected static final int ULONGLONG = 8; // unsigned long
	
	protected static Byte BYTE = new Byte();
	protected static Word WORD = new Word();
	protected static DWord DWORD = new DWord();
	protected static ULongLong ULONGLONG = new ULongLong();

	protected Class<P> clazz;
	
	protected PropertyInterface pi;
	
	protected int arraySize;
	protected int sizeOf;
	
	public Header(Class<P> clazz, int sizeOf, ByteBuffer bytes) {
		this(clazz, sizeOf, 1, bytes);
	}
	
	public Header(Class<P> clazz, int sizeOf, int arraySize, ByteBuffer bytes) {
		this(clazz, sizeOf, arraySize);
		mapProperties(bytes);
	}
	
	public Header(Class<P> clazz, int sizeOf) {
		this(clazz, sizeOf, 1);
	}
	
	public Header(Class<P> clazz, int sizeOf, int arraySize) {
		super(clazz);
		this.clazz = clazz;
		this.sizeOf = sizeOf;
		this.arraySize = arraySize;
	}
	
	public void write(IDataWriter dw) throws IOException {
		dw.writeBytes(this.array());
	}
	
	@SuppressWarnings("rawtypes")
	public static Header parse(Class<? extends Header> clazz, IDataReader dr)
				throws IOException {
		try {
			return ((Header) clazz.newInstance()).create(dr);
		} catch (InstantiationException | IllegalAccessException ignore) {}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	protected Header create(IDataReader dr) throws IOException {
		byte[] bytes = new byte[sizeOf * arraySize];
		dr.read(bytes);
		mapProperties(ByteBuffer.wrap(bytes));
		return this;
	}
	
	protected abstract void mapProperties(ByteBuffer bytes);
	
	protected void mapExtraProperties() {}
	
	protected abstract byte[] array();
	
	protected static <P extends Enum<P>> Collection<P> values(Class<P> enumClass) {
		try {
			Method valuesMethod = enumClass.getMethod("values", new Class[0]);
			@SuppressWarnings("unchecked")
			P[] values = (P[]) valuesMethod.invoke(null, new Object[0]);
			return Arrays.asList(values);
		} catch (Exception ex) {
			throw new RuntimeException("Exceptions here should be impossible", ex);
		}
	}

}
