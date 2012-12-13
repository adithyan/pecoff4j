package com.github.twinj.pecoff4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ImportDirTable extends ArrayList<ImportDescriptor> {
	
	private static final long serialVersionUID = 5543365594109348661L;
	
	public ImportDirTable(ByteBuffer bytes) {
		super();
		byte[] barray;
		
		while (bytes.hasRemaining()) {
			barray = new byte[ImportDescriptor.SIZE_OF];
			bytes.get(barray);
			ImportDescriptor id = new ImportDescriptor(ByteBuffer.wrap(barray));
			this.add(id);
		}
	}
}
