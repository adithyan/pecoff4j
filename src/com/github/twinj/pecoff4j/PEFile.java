package com.github.twinj.pecoff4j;

import java.util.List;

import com.github.twinj.headers.DWord;

/**
 * Incomplete library planning stage.
 * 
 * @author Daniel Kemp
 *
 */
public interface PEFile {
	
	/**
	 * Retrieve a pointer offset to the MS-DOS MZ header.
	 */
	public int getDosHeader();
	
	/**
	 * Determine the type of an .EXE file.
	 */
	public DWord imageFileType();
	
	/**
	 * Retrieve a pointer offset to the PE file header.
	 */
	public int getPEFileHeader();
	
	/**
	 * Retrieve a pointer offset to the PE optional header .
	 */
	public int getPEOptionalHeader();
	
	/**
	 * Return the address of the module entry point.
	 */
	public int getModuleEntryPoint();
	
	/**
	 * Return a count of the number of sections in the file.
	 */
	public int numOfSections();
	
	/**
	 * Return the desired base address of the executable when it is loaded into a
	 * process's address space.
	 */
	public int getImageBase();
	
	/**
	 * Determine the location within the file of a specific image data directory.
	 */
	public int imageDirectoryOffset();
	
	/**
	 * Function retrieve names of all the sections in the file.
	 */
	public abstract List<String> getSectionNames();
	
	/**
	 * Copy the section header information for a specific section.
	 */
	public SH getSectionHdrByName();
	
	/**
	 * Get null-separated list of import module names.
	 */
	public List<String> getImportModuleNames();
	
	/**
	 * Get null-separated list of import functions for a module.
	 */
	public List<String> getImportFunctionNamesByModule();
	
	/**
	 * Get null-separated list of exported function names.
	 */
	public List<String> getExportFunctionNames();
	
	/**
	 * Get number of exported functions.
	 */
	public int getNumberOfExportedFunctions();
	
	/**
	 * Get list of exported function virtual address entry points.
	 */
	public List<Integer> getExportFunctionEntryPoints();
	
	/**
	 * Get list of exported function ordinal values.
	 */
	public List<Integer> getExportFunctionOrdinals();
	
	/**
	 * Determine total number of resource objects.
	 */
	public int getNumberOfResources();
	
	/**
	 * Return list of all resource object types used in file.
	 */
	public List<RCRoot.RT> getListOfResourceTypes();
	
	/**
	 * Determine if debug information has been removed from file.
	 */
	public boolean isDebugInfoStripped();
	
	/**
	 * Get name of image file.
	 */
	public String retrieveModuleName();
	
	/**
	 * Function determines if the file is a valid debug file.
	 */
	public boolean isDebugFile();
	
	/**
	 * Function returns debug header from debug file.
	 */
	public DebugDir getSeparateDebugHeader();
}
