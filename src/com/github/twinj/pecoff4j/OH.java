package com.github.twinj.pecoff4j;

public class OH {
	
	private OHS standard;
	
	@SuppressWarnings("rawtypes")
	private OHA additional;
	
	/**
	 * An array of IMAGE_DATA_DIRECTORY structures. Each structure contains the
	 * RVA and size of some important part of the executable (for instance,
	 * imports, exports, resources).
	 */
	private OHD directories;
	
	public OHD getDirectories() {
		return directories;
	}
	
	public void setDirectories(OHD directories) {
		this.directories = directories;
	}
	
	@SuppressWarnings("rawtypes")
	public OHA getAdditional() {
		return additional;
	}
	
	@SuppressWarnings("rawtypes")
	public void setAdditional(OHA additional) {
		this.additional = additional;
	}
	
	public OHS getStandard() {
		return standard;
	}
	
	public void setStandard(OHS standard) {
		this.standard = standard;
	}
}
