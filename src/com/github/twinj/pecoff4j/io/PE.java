/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package com.github.twinj.pecoff4j.io;

import com.github.twinj.pecoff4j.COFFH;
import com.github.twinj.pecoff4j.DOSStub;
import com.github.twinj.pecoff4j.DOSH;
import com.github.twinj.pecoff4j.ImageData;
import com.github.twinj.pecoff4j.OH;
import com.github.twinj.pecoff4j.PESig;
import com.github.twinj.pecoff4j.STable;

public class PE {

	DOSH dosHeader;
	DOSStub stub;
	PESig signature;
	COFFH coffHeader;	
	OH optionalHeader;
	STable sectionTable;

	ImageData imageData;
	
	long lfanew;
	int sizeOfSignature;
	int pe_magic;
	boolean is64;
	int numberOfOptDirs;
	int numberOfSections;
	
	public DOSH getDosHeader() {
		return dosHeader;
	}
	
	public DOSStub getStub() {
		return stub;
	}
	
	public PESig getSignature() {
		return signature;
	}
	
	public COFFH getCoffHeader() {
		return coffHeader;
	}

	public STable getSectionTable() {
		return sectionTable;
	}
	
	public void setDosHeader(DOSH dosHeader) {
		this.dosHeader = dosHeader;
		this.lfanew = dosHeader.valueOf(DOSH.Property.PE_HEADER_OFFSET_BD).longValue();
	}
	
	public void setStub(DOSStub stub) {
		this.stub = stub;
	}
	
	public void setSignature(PESig signature) {
		this.signature = signature;
	}
	
	public void setCoffHeader(COFFH coffHeader) {
		this.coffHeader = coffHeader;
	}
	
	public void setSectionTable(STable sectionTable) {
		this.sectionTable = sectionTable;
	}
	
	public ImageData getImageData() {
		return imageData == null ? imageData = new ImageData()  : imageData;
	}
	
	public void setImageData(ImageData imageData) {
		this.imageData = imageData;
	}

	public OH getOptionalHeader() {
		return optionalHeader;
	}

	public void setOptionalHeader(OH optionalHeader) {
		this.optionalHeader = optionalHeader;
	}
	
	public class SectData {
		public byte[] data;
		public byte[] preamble;	
	}
}
