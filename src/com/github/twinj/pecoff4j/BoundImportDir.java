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
import java.util.ArrayList;

public class BoundImportDir extends ArrayList<BoundImport> {

	private static final long serialVersionUID = 281450863816447210L;

	public BoundImportDir(ByteBuffer buffer) {
		BoundImport bi = null;
		byte[] barray = null;
		while (true) {
			barray = new byte[BoundImport.SIZE_OF];
			buffer.get(barray);
			bi = new BoundImport(ByteBuffer.wrap(barray));
			add(bi);
			if (bi.valueOf(BoundImport.Property.TIME_DATE_STAMP).intValue() == 0
				&& bi.valueOf(BoundImport.Property.OFFSET_MODULE_NAME).intValue() == 0
				&& bi.valueOf(BoundImport.Property.NUMBER_OF_MODULE_FORWARDER_REFS).intValue() == 0) {
				break;
			}
		}
	}
}
