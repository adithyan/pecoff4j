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
import java.util.Arrays;

import com.github.twinj.headers.Word;


public class PESig {
	
	public static final int IMAGE_DOS_SIGNATURE = 0x5A4D; // seen as 0x4D5A MZ - 19802 | read as 0x5A4D ZM - 23117
	public static final int IMAGE_OS2_SIGNATURE = 0x454E; // 0x4E45 NE - 20037 | 0x454E EN - 17742 
	public static final int IMAGE_OS2_SIGNATURE_LE = 0x454C; // 0x4C45 LE - 19525 | 0x454C EL 17741
	public static final int IMAGE_NT_SIGNATURE = 0x00004550; // 0x50450000 PE00 - 5260544 | 0x00004550 000EP -  17744
	public static final int IMAGE_NT_SIGNATURE_SIZE_OF = 4; // 0x50450000 PE00 - 5260544 | 0x00004550 000EP -  17744

	private static byte[] expected1 = new byte[]{
				0x50, 0x45, 0x00, 0x00
	};
	private static byte[] expected2 = new byte[]{
				0x50, 0x69, 0x00, 0x00
	};
	public Word signature;
	
	public int valueOf() {
		return signature.valueOf();
	}
	
	public PESig(ByteBuffer bytes, long position) {
		this.signature = new Word(position, bytes.array());
		System.err.print(signature.valueOf() + "\n");
	}
	
	public boolean isValid() {
		return Arrays.equals(expected1, signature.bytes) || Arrays.equals(expected2, signature.bytes);
	}
	
	public boolean isValueOfValid() {
		return signature.valueOf() == IMAGE_NT_SIGNATURE;
	}
}
