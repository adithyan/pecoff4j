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

import com.github.twinj.headers.DatumHeader;

public abstract class RCEntry<P extends Enum<P>> extends DatumHeader<P> {
	
	private static final long serialVersionUID = -5303463280352581901L;
	
	public RCEntry(Class<P> clazz, int sizeOf, ByteBuffer bytes) {
		super(clazz, sizeOf, bytes);
	}
	
	public RCEntry(Class<P> clazz, int sizeOf) {
		super(clazz, sizeOf);
	}
	
}
