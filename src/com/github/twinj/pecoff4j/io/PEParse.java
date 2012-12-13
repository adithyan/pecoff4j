package com.github.twinj.pecoff4j.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import com.github.twinj.pecoff4j.OHD;
import com.github.twinj.pecoff4j.SH;

public class PEParse {
	
	private final PEHandle instance;
	
	protected static final int BYTE_SIZE_IN_BITS = 8;
	protected static final int IMAGE_SIZEOF_SHORT_NAME = 8;
	protected static final int DEFAULT_PAGE_SIZE = 512;
	
	public static final int MAGIC_PE32 = 0x0b01; // 2817 | 0x010b - 267
	public static final int MAGIC_PE32plus = 0x0b02; // | 0x020b
	public static final int IMAGE_NT_SIGNATURE = 0x00004550; // 0x50450000 PE00 -
																														// 5260544 |
																														// 0x00004550 000EP
																														// - 17744
	
	/**
	 * Size of COFF Header or PEFile header.
	 */
	public static final int SIZEOF_COFF_HEADER = 20;
	
	/**
	 * Size of Section Header.
	 */
	public static final int SIZEOF_SECTION_HEADER = 40;
	
	/**
	 * Offset to PE file signature e_lfanew
	 */
	public static final int NT_SIGNATURE_FP = 0x3c; // e_lfanew file offset 60
	
	/**
	 * Size of PE signature. Note: others sigs are 4 bytes long. Actual sig.
	 * 'PE\0\0'
	 */
	public static final int SIZEOF_NT_SIGNATURE = 4;
	
	/**
	 * PEFile header offset 2 for word value 'number of sections'.
	 */
	public final static int COFF_HDR_NUMBER_OF_SECTIONS = 2; // + 2
	
	/**
	 * PEFile header offset 16 for word value 'size of pe optional header'.
	 */
	public final static int COFF_HDR_SIZEOF_OPT_HDR = 16; // + 16
	
	private PEParse(Path file) {
		instance = new PEHandle(file);
	}
	
	public static PEParse newParser(Path file) {
		PEParse parser = new PEParse(file);
		return parser;
	}
	
	public void parse(Strategy strategy) {
		try (RandomAccessFile raf = instance.get(); FileChannel ch = raf.getChannel()) {
			
			final int bufferSize = DEFAULT_PAGE_SIZE * 2;
			
			instance.raf = raf;
			instance.ch = ch;
			instance.buffer = ByteBuffer.wrap(new byte[bufferSize]);
			instance.buffer.order(ByteOrder.LITTLE_ENDIAN);
			instance.buffer.position(0);
			ch.read(instance.buffer);
			
			strategy.parse(instance);
			
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		instance.raf = null;
		instance.ch = null;
		instance.buffer = null;
	}
	
	public class PEHandle {
		
		private static final int OPT_HDR_DIRECTORES_PE32 = 96;
		private static final int OPT_HDR_DIRECTORES_PE32PLUS = 112;
		private static final int SH_VA_OFFSET = 12;
		private static final int SH_SIZEOFRAWD_OFFSET = 16;
		private static final int SH_RAWD_OFFSET = 20;
		
		private static final int SIZEOF_DIRECTORY = 8;
		
		private static final int OPT_HDR_NUMOF_RVA_ANDSIZES_PE32PLUS = 108;
		private static final int OPT_HDR_NUMOF_RVA_ANDSIZES_PE32 = 92;
		
		protected Path file;
		
		private RandomAccessFile raf = null;
		private FileChannel ch = null;
		ByteBuffer buffer = null;
		
		public PEHandle(Path file) {
			this.file = file;
		}
		
		public RandomAccessFile get() throws FileNotFoundException {
			return new RandomAccessFile(file.toFile(), "rw");
		}
		
		/**
		 * Actual offset to the signature. This is the base offset for the pecoff
		 * headers.
		 */
		private Integer signatureOffset = null;
		
		/**
		 * True if file is in PE format.
		 */
		private Boolean isPE = null;
		
		/**
		 * MS-OS header identifies the NT PEFile signature dword; the PEFILE header
		 * exists just after that dword.
		 */
		private Integer coffHeaderOffset = null;
		
		/**
		 * Number of data sections in the PE.
		 */
		private Integer numSections = null;
		
		/**
		 * The size of the PE file header. Varies between 32/64 machines and
		 * resources.
		 */
		private Integer sizeOfOptionalHeader = null;
		
		/**
		 * PE optional header is immediately after PEFile COFF header.
		 */
		private Integer optionalHeaderOffset = null;
		
		/**
		 * True if 64 bit else false;
		 */
		private Boolean pe32Plus = null;
		
		// private Integer sizeOfPageFile = null;
		
		/**
		 * Number of directories in the PE Header. This maps to the
		 * NumberOfRvaAndSizes field.
		 */
		private Integer numDirectories = null;
		
		/**
		 * Offset to directories[0].
		 */
		private Integer optionalHeaderDirectoriesOffset;
		
		/**
		 * Section headers are immediately after PE optional header. Need to know
		 * the size of the PE optional before this can be calculated.
		 */
		private Integer secHeaderOffset = null;
		
		/***********************************************************************************************
		 * CACHED READERS. These methods return values will be saved as part of the
		 * parser for the file.
		 * 
		 * The values are lazily initialised so as to reduce reading when building a
		 * parse strategy.
		 */
		
		/**
		 * Gets the file offset of the pecoff signature.
		 */
		Integer getSignatureOffset() {
			if (signatureOffset == null) {
				signatureOffset = readWord(NT_SIGNATURE_FP);
			}
			return signatureOffset;
		}
		
		/**
		 * Returns whether the selected file to parse is a PE file.
		 * 
		 * @return
		 */
		public Boolean isPE() {
			if (isPE == null) {
				Integer sig = readDWord(getSignatureOffset());
				isPE = (sig == IMAGE_NT_SIGNATURE);
			}
			return isPE;
		}
		
		/**
		 * Gets the file offset of the pecoff header: which is 4 bytes in front of a
		 * pe file.
		 */
		public Integer getCoffHeaderOffset() {
			if (coffHeaderOffset == null) {
				coffHeaderOffset = getSignatureOffset() + SIZEOF_NT_SIGNATURE;
			}
			return coffHeaderOffset;
		}
		
		/**
		 * Gets the number of sections. Assumes the buffer is at the start location
		 * and contains the file first offset. Cache's the value.
		 * 
		 * @return
		 */
		public Integer getNumSections() {
			if (numSections == null) {
				numSections = readWord(getCoffHeaderOffset() + COFF_HDR_NUMBER_OF_SECTIONS);
			}
			return numSections;
		}
		
		/**
		 * Gets the size of the PE Header. Assumes the buffer includes the start
		 * offset.
		 */
		public Integer getSizeOfOptionalHeader() {
			if (sizeOfOptionalHeader == null) {
				sizeOfOptionalHeader = readWord(getCoffHeaderOffset() + COFF_HDR_SIZEOF_OPT_HDR);
			}
			return sizeOfOptionalHeader;
		}
		
		/**
		 * Gets the file offset of the PE optional header.
		 */
		public Integer getOptionalHeaderOffset() {
			if (optionalHeaderOffset == null) {
				optionalHeaderOffset = getCoffHeaderOffset() + SIZEOF_COFF_HEADER;
			}
			return optionalHeaderOffset;
		}
		
		/**
		 * Returns true if pe file is 64 bit or PE32plus.
		 */
		public Boolean isPE32Plus() {
			if (pe32Plus == null) {
				Integer magic = readWord(getOptionalHeaderOffset());
				pe32Plus = (magic == MAGIC_PE32plus);
			}
			return pe32Plus;
		}
		
		/**
		 * Returns the number of directories in the Optional PE header.
		 */
		public Integer getNumDirectories() {
			if (numDirectories == null) {
				numDirectories = readDWord(getOptionalHeaderOffset()
							+ (isPE32Plus() ? OPT_HDR_NUMOF_RVA_ANDSIZES_PE32PLUS
										: OPT_HDR_NUMOF_RVA_ANDSIZES_PE32));
			}
			return numDirectories;
		}
		
		/**
		 * Gets the directories offset. Allows for 32 bit or 64 bit headers.
		 */
		public Integer getOptionalHeaderDirectoriesOffset() {
			if (optionalHeaderDirectoriesOffset == null) {
				optionalHeaderDirectoriesOffset = getOptionalHeaderOffset()
							+ (isPE32Plus() ? OPT_HDR_DIRECTORES_PE32PLUS : OPT_HDR_DIRECTORES_PE32);
			}
			return optionalHeaderDirectoriesOffset;
		}
		
		/**
		 * Gets the directories offset.
		 */
		public Integer getOptionalHeaderDirectoriesOffset(Integer directory) {
			if (directory > getNumDirectories() - 1 || directory < 0) {
				return null;
			}
			return getOptionalHeaderDirectoriesOffset() + (SIZEOF_DIRECTORY * directory);
		}
		
		/**
		 * Gets the file offset of the first Section Header.
		 */
		public Integer getSectionHeaderOffset() {
			if (secHeaderOffset == null) {
				secHeaderOffset = getSizeOfOptionalHeader() + getOptionalHeaderOffset();
			}
			return secHeaderOffset;
		}
		
		/**
		 * Gets the file offset of the 'int' Section Header.
		 */
		public Integer getSectionHeaderOffset(Integer section) {
			if (section > getNumSections() || section <= 0) {
				return null;
			}
			return getSectionHeaderOffset() + (SIZEOF_SECTION_HEADER * (section - 1));
		}
		
		/***********************************************************************************************
		 * UNCACHED READERS. These methods return values are not cached but may use
		 * cached values to derive their return values.
		 * 
		 * These methods generally involve reads of the initial buffer which is two
		 * PAGES (512d * 2d) long to handle all the headers.
		 */
		
		/**
		 * Gets the directory raw file offset which is calculated from its RVA. Null
		 * if it does not exist. Assumes nothing is known about the directory and
		 * respective sections. Tests all sections.
		 */
		public Integer getDirectoryOffset(Integer directory) {
			Integer position;
			if ((position = getOptionalHeaderDirectoriesOffset(directory)) == null) {
				return null;
			}
			return convertRVAToOffset(readDWord(position));
		}
		
		/**
		 * Gets the file offset of the named Section Header.
		 */
		public Integer getSectionHeaderOffset(String name) {
			for (int i = 1; i <= getNumSections(); i++) {
				if (getSectionHeaderName(i).equals(name)) {
					return getSectionHeaderOffset(i);
				}
			}
			return null;
		}
		
		/**
		 * Gets the first section header name.
		 */
		public String getFirstSectionHeaderName() {
			return getSectionHeaderName(1);
		}
		
		/***********************************************************************************************
		 * RVA to Offset Conversion methods UNCACHED as they require extra reads
		 * through each section to determine addresses. 
		 */
		
		/**
		 * Gets the raw offset address of the RVA inside section.
		 */
		public Integer convertRVAToOffset(Integer rva, int section) {
			Integer psh = getSectionHeaderOffset(section);
			return rvaToOffset(rva, readDWord(psh + SH_VA_OFFSET), readDWord(psh
						+ SH_RAWD_OFFSET));
		}
		
		/**
		 * Gets the RVA address from a raw file offset.
		 */
		public Integer convertOffsetToRVA(Integer offset, int section) {
			Integer psh = getSectionHeaderOffset(section);
			return offsetToRva(offset, readDWord(psh + SH_VA_OFFSET), readDWord(psh
						+ SH_RAWD_OFFSET));
		}
		
		/**
		 * Gets the raw offset address of the RVA inside named section.
		 */
		public Integer convertRVAToOffset(Integer rva, String name) {
			Integer psh = getSectionHeaderOffset(name);
			return rvaToOffset(rva, readDWord(psh + SH_VA_OFFSET), readDWord(psh
						+ SH_RAWD_OFFSET));
		}
		
		/**
		 * Gets the raw offset address of the RVA inside named section.
		 */
		public Integer convertOffsetToRVA(Integer offset, String name) {
			Integer psh = getSectionHeaderOffset(name);
			return offsetToRva(offset, readDWord(psh + SH_VA_OFFSET), readDWord(psh
						+ SH_RAWD_OFFSET));
		}
		
		/**
		 * Gets the raw offset address of the RVA inside unknown section.
		 */
		public Integer convertRVAToOffset(Integer rva) {
			for (int i = 1; i <= getNumSections(); i++) {
				Integer psh = getSectionHeaderOffset(i);
				Integer pshVirtualAddress = readDWord(psh + SH_VA_OFFSET);
				
				if (rvaIsInSection(rva, pshVirtualAddress, readDWord(psh + SH_SIZEOFRAWD_OFFSET))) {
					return rvaToOffset(rva, pshVirtualAddress, readDWord(psh + SH_RAWD_OFFSET));
				}
			}
			return null;
		}
		
		/**
		 * Gets the RVA from a raw offset inside unknown section.
		 */
		public Integer convertOffsetToRVA(Integer offset) {
			for (int i = 1; i <= getNumSections(); i++) {
				Integer psh = getSectionHeaderOffset(i);
				Integer pshVirtualAddress = readDWord(psh + SH_VA_OFFSET);
				
				if (rvaIsInSection(offset, pshVirtualAddress, readDWord(psh
							+ SH_SIZEOFRAWD_OFFSET))) {
					return offsetToRva(offset, pshVirtualAddress, readDWord(psh + SH_RAWD_OFFSET));
				}
			}
			return null;
		}
		
		/**
		 * If rva is within section bounds return true;
		 */
		public boolean rvaIsInSection(int rva, int sectionVA, int sectionRawSize) {
			return sectionVA <= rva && sectionVA + sectionRawSize > rva;
		}
		
		/**
		 * Converts rva to file offset.
		 */
		public int rvaToOffset(int rva, int sectionVA, int sectionRawPointer) {
			return rva - sectionVA + sectionRawPointer;
		}
		
		/**
		 * Converts file offset to rva
		 */
		public int offsetToRva(int offset, int sectionVA, int sectionRawPointer) {
			return offset - sectionRawPointer + sectionVA;
		}
		
		/***********************************************************************************************
		 * 
		 */
		
		
		
		/**
		 * Gets the 8 byte header name and converts it into a string. If section is
		 * over numSections then an invalid String will be read.
		 */
		public String getSectionHeaderName(int section) {
			byte[] barray = new byte[IMAGE_SIZEOF_SHORT_NAME];
			buffer.position(getSectionHeaderOffset(section));
			buffer.get(barray);
			try {
				return new String(barray, "UTF-8").trim();
			} catch (Exception ignore) {}
			return null;
		}
		
		/**
		 * Returns a new SH from the name if exists else null.
		 */
		public SH getSectionHeader(String name) {
			Integer sectionOffset = getSectionHeaderOffset(name);
			if (sectionOffset != null) {
				buffer.position(sectionOffset);
				return new SH(buffer);
			}
			return null;
		}
		
		/**
		 * Returns a file offset to the Section header name.
		 */
		public OHD getDirTable() {
			Integer doh = getOptionalHeaderDirectoriesOffset();
			int nDirs = getNumDirectories();
			buffer.position(doh);
			return new OHD(buffer, nDirs);
		}
		
		/**
		 * 
		 */
		public Integer findResource(Integer type, Integer name, Integer language,
					String section) {
			
			/** Must be 0 thru (NumberOfRvaAndSizes-1). */
			if (type >= getNumDirectories() || type < 0) {
				return null;
			}
			
			/** Retrieve offsets to optional and section headers. */
			Integer psh = this.getSectionHeaderOffset(section);
			
			/** Locate image directory's relative virtual address. */
			Integer dirVirtualAddress = readDWord(getOptionalHeaderDirectoriesOffset(type));
			
			Integer pshVirtualAddress = readDWord(psh + SH_VA_OFFSET);
			Integer pshSizeOfRawData = readDWord(psh + SH_SIZEOFRAWD_OFFSET);
			
			if (pshVirtualAddress <= dirVirtualAddress
						&& pshVirtualAddress + pshSizeOfRawData > dirVirtualAddress) {}
			psh += 40;
			Integer pshPointerToRawData = readDWord(psh + SH_RAWD_OFFSET);
			
			/** Return image directory offset. */
			return dirVirtualAddress - pshVirtualAddress + pshPointerToRawData;
		}
		
		// private Integer readDWord(int position) {
		// return readBuffer(position, 4).intValue();
		// }
		//
		// private Integer readWord(int position) {
		// return readBuffer(position, 2).intValue();
		// }
		
		private Integer readDWord(int position) {
			return readBufferDWord(position).intValue();
		}
		
		private Integer readWord(int position) {
			return readBufferWord(position).intValue();
		}
		
		private Integer readBufferDWord(int position) {
			buffer.position(position);
			return buffer.getInt();
		}
		
		private Short readBufferWord(int position) {
			buffer.position(position);
			return buffer.getShort();
		}
		
		@SuppressWarnings("unused")
		private Number readBuffer(int position, int sizeOf) {
			byte[] barray = new byte[sizeOf];
			buffer.position(position);
			buffer.get(barray);
			if (sizeOf <= 4) {
				return valueOfInteger(barray);
			} else {
				return valueOfLong(barray);
			}
		}
		
		public FileChannel getChannel() {
			return ch;
		}
	}
	
	public static Integer valueOfInteger(byte[] bytes) {
		int v = (int) bytes[0] & 0xFF;
		// shifing to fit bit array into an int
		for (int i = 1; i < bytes.length; i++) {
			v = v | ((int) bytes[i] & 0xFF) << (i * BYTE_SIZE_IN_BITS);
		}
		return new Integer(v);
	}
	
	public static Long valueOfLong(byte[] bytes) {
		long v = (long) bytes[0] & 0xFF;
		// shifing to fit bit array into an int
		for (int i = 1; i < bytes.length; i++) {
			v = v | ((long) bytes[i] & 0xFF) << (i * BYTE_SIZE_IN_BITS);
		}
		return new Long(v);
	}
}
