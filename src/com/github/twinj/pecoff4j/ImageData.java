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

import com.github.twinj.pecoff4j.util.IntMap;

public class ImageData {
	
	private byte[] headerPadding; // TODO find out what this is
	
	// Data members that correspond to image data directories
	private ExportDir exportTable;
	private ImportDirTable importTable;
	private RCTree resourceTable;
	private byte[] exceptionTable;
	private byte[] certificateTable;
	private byte[] baseRelocationTable;
	private DebugDir debug;
	private byte[] architecture;
	private byte[] globalPtr;
	private byte[] tlsTable;
	private LoadConfigDir loadConfigTable;
	private BoundImportDir boundImports;
	private byte[] iat;
	private byte[] delayImportDescriptor;
	private byte[] clrRuntimeHeader;
	private byte[] reserved;
	
	// Debug type-specific data
	private byte[] debugRawDataPreamble;
	private byte[] debugRawData;
	
	// Any image data preambes
	private IntMap preambles = new IntMap();
	
	// Any trailing data
	private byte[] trailingData;
	
	public byte[] getHeaderPadding() {
		return headerPadding;
	}
	
	public void setHeaderPadding(byte[] headerPadding) {
		this.headerPadding = headerPadding;
	}
	
	public byte[] getPreamble(int directory) {
		return (byte[]) preambles.get(directory);
	}
	
	public void put(int directory, byte[] preamble) {
		preambles.put(directory, preamble);
	}
	
	public ExportDir getExportTable() {
		return exportTable;
	}
	
	public void setExportTable(ExportDir exportTable) {
		this.exportTable = exportTable;
	}
	
	public ImportDirTable getImportTable() {
		return importTable;
	}
	
	public void setImportTable(ImportDirTable importTable) {
		this.importTable = importTable;
	}
	
	public RCTree getResourceTable() {
		return resourceTable;
	}
	
	public void setResourceTable(RCTree resDirList) {
		this.resourceTable = resDirList;
	}
	
	public byte[] getExceptionTable() {
		return exceptionTable;
	}
	
	public void setExceptionTable(byte[] exceptionTable) {
		this.exceptionTable = exceptionTable;
	}
	
	public byte[] getCertificateTable() {
		return certificateTable;
	}
	
	public void setCertificateTable(byte[] certificateTable) {
		this.certificateTable = certificateTable;
	}
	
	public byte[] getBaseRelocationTable() {
		return baseRelocationTable;
	}
	
	public void setBaseRelocationTable(byte[] baseRelocationTable) {
		this.baseRelocationTable = baseRelocationTable;
	}
	
	public DebugDir getDebug() {
		return debug;
	}
	
	public void setDebug(DebugDir debug) {
		this.debug = debug;
	}
	
	public byte[] getArchitecture() {
		return architecture;
	}
	
	public void setArchitecture(byte[] architecture) {
		this.architecture = architecture;
	}
	
	public byte[] getGlobalPtr() {
		return globalPtr;
	}
	
	public void setGlobalPtr(byte[] globalPtr) {
		this.globalPtr = globalPtr;
	}
	
	public byte[] getTlsTable() {
		return tlsTable;
	}
	
	public void setTlsTable(byte[] tlsTable) {
		this.tlsTable = tlsTable;
	}
	
	public LoadConfigDir getLoadConfigTable() {
		return loadConfigTable;
	}
	
	public void setLoadConfigTable(LoadConfigDir loadConfigTable) {
		this.loadConfigTable = loadConfigTable;
	}
	
	public BoundImportDir getBoundImports() {
		return boundImports;
	}
	
	public void setBoundImports(BoundImportDir boundImports) {
		this.boundImports = boundImports;
	}
	
	public byte[] getIat() {
		return iat;
	}
	
	public void setIat(byte[] iat) {
		this.iat = iat;
	}
	
	public byte[] getDelayImportDescriptor() {
		return delayImportDescriptor;
	}
	
	public void setDelayImportDescriptor(byte[] delayImportDescriptor) {
		this.delayImportDescriptor = delayImportDescriptor;
	}
	
	public byte[] getClrRuntimeHeader() {
		return clrRuntimeHeader;
	}
	
	public void setClrRuntimeHeader(byte[] clrRuntimeHeader) {
		this.clrRuntimeHeader = clrRuntimeHeader;
	}
	
	public byte[] getReserved() {
		return reserved;
	}
	
	public void setReserved(byte[] reserved) {
		this.reserved = reserved;
	}
	
	public byte[] getDebugRawData() {
		return debugRawData;
	}
	
	public void setDebugRawData(byte[] debugRawData) {
		this.debugRawData = debugRawData;
	}
	
	public byte[] getTrailingData() {
		return trailingData;
	}
	
	public void setTrailingData(byte[] trailingData) {
		this.trailingData = trailingData;
	}
	
	public byte[] getDebugRawDataPreamble() {
		return debugRawDataPreamble;
	}
	
	public void setDebugRawDataPreamble(byte[] debugRawDataPreamble) {
		this.debugRawDataPreamble = debugRawDataPreamble;
	}
}
