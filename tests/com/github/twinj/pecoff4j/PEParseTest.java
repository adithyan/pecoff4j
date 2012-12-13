package com.github.twinj.pecoff4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;

import com.github.twinj.pecoff4j.io.PEParse;
import com.github.twinj.pecoff4j.io.Strategy;

public class PEParseTest {
	public static void main(String[] args) throws Exception {
		final String exe = "C:/Program Files (x86)/Steam/SteamApps/common/XCom-Enemy-Unknown/Binaries/Win32/XComGame.exe";
		//final PEParse pe1 = PEParse.newParser(Paths.get(exe));
		final PEParse pe2 = PEParse.newParser(Paths.get(exe));

		Strategy s0 = new Strategy() {
			@Override
			public void parse(PEParse.PEHandle p) {
				System.out.println("SECTION HEADER OFFSET: " + p.getSectionHeaderOffset());
				System.out.println("FIRST SECTION HEADER NAME: " + p.getFirstSectionHeaderName());
				System.out.println("NUMBER OF SECTIONS: " + p.getNumSections());
				System.out.println("PE OPTIONAL HEADER OFFSET: " + p.getOptionalHeaderOffset());
				System.out.println("SIZE OF PE OPTIONAL HEADER: " + p.getSizeOfOptionalHeader());
				System.out.println("COFF HEADER OFFFSET: " + p.getCoffHeaderOffset());
			}
		};		
		
		//pe1.parse(s0);
				
		Strategy s1 = new Strategy() {
			@Override
			public void parse(PEParse.PEHandle parser) {
				System.out.println("IS PE FILE?: " + parser.isPE());
				System.out.println("IS PE 64 bit?: " + parser.isPE32Plus());
				
				System.out.println("PE OPTIONAL HEADER OFFSET: "
							+ parser.getOptionalHeaderOffset());
				System.out.println("SIZE OF PE OPTIONAL HEADER: "
							+ parser.getSizeOfOptionalHeader());
				System.out.println("COFF HEADER OFFFSET: " + parser.getCoffHeaderOffset());
				System.out.println("SECTION HEADER OFFSET: " + parser.getSectionHeaderOffset());
				System.out.println("FIRST SECTION HEADER NAME: "
							+ parser.getFirstSectionHeaderName());
				System.out.println("NUMBER OF SECTIONS: " + parser.getNumSections());
				
				SH data = parser.getSectionHeader(".rdata");
				
				for (SH.Property p : SH.Property.values()) {
					if (p != SH.Property.NAME) {
						System.out.println(p + ": " + data.valueOf(p));
					} else {
						System.out.println(p + ": " + data.toUtfString(SH.Property.NAME));
					}
				}
				
				SH rc = parser.getSectionHeader(".rsrc");
				
				for (SH.Property p : SH.Property.values()) {
					if (p != SH.Property.NAME) {
						System.out.println(p + ": " + rc.valueOf(p));
					} else {
						System.out.println(p + ": " + rc.toUtfString(SH.Property.NAME));
					}
				}
				
				SH re = parser.getSectionHeader(".reloc");
				
				for (SH.Property p : SH.Property.values()) {
					if (p != SH.Property.NAME) {
						System.out.println(p + ": " + re.valueOf(p));
					} else {
						System.out.println(p + ": " + re.toUtfString(SH.Property.NAME));
					}
				}
				
				for (OHD.Directory d : OHD.Directory.values()) {
					System.out.println(d + ": " + parser.getDirectoryOffset(d.ordinal()));
				}
				
				System.out
							.println("***************************************************************************************");
				
				OHD od = parser.getDirTable();
				for (OHD.Directory d : OHD.Directory.values()) {
					
					System.out
								.println("---------------------------------------------------------------------------------------");
					System.out.println(d);
					for (OHDD.Property p : OHDD.Property.values()) {
						System.out.println(p + ": " + od.valueOf(d, p));
					}
				}
				
				System.out
							.println("***************************************************************************************");
				
				int size = od.valueOf(OHD.Directory.IMAGE_DIRECTORY_ENTRY_RESOURCE,
							OHDD.Property.SIZE);
				byte[] array = new byte[size];
				ByteBuffer bytes = ByteBuffer.wrap(array);
				bytes.order(ByteOrder.LITTLE_ENDIAN);
				long pos = parser.getDirectoryOffset(OHD.Directory.IMAGE_DIRECTORY_ENTRY_RESOURCE
							.ordinal());
				try {
					parser.getChannel().read(bytes, pos);
				} catch (IOException ex) {}
				bytes.position(0);
				
				System.out
							.println("***************************************************************************************");
				
				RCRoot root = new RCRoot(bytes, new RCDH(bytes));
				
				try {
					while (traverseTree(root, bytes)) {}
				} catch (IOException ex) {}
				
				RCDataEntry de = root.findResource(RCRoot.RT.RCDATA, "1020", "1033");
				for (RCDataEntry.Property p : RCDataEntry.Property.values()) {
					System.out.println(p + ": " + de.valueOf(p));
				}
				
			}
			
			private boolean traverseTree(RCTree tree, ByteBuffer buffer) throws IOException {
				for (RCDirEntry e : tree.values()) {
					if (e.dir == null && e.entry == null) {
						buffer.position(e.valueOfBM(RCDirEntry.Property.OFFSET_TO_DATA, 0x7fffffff));
						
						e.dir = new RCTree(buffer, new RCDH(buffer));
						return true;
					} else if (e.entry == null) {
						traverseTree(e.dir, buffer);
					}
				}
				return false;
			}
		};
	
		//pe1.parse(s1);
		
		
		Strategy s2 = new Strategy() {
			@Override
			public void parse(PEParse.PEHandle parser) {
				SH rc = parser.getSectionHeader(".rsrc");
				
				for (SH.Property p : SH.Property.values()) {
					if (p != SH.Property.NAME) {
						System.out.println(p + ": " + rc.valueOf(p));
					} else {
						System.out.println(p + ": " + rc.toUtfString(SH.Property.NAME));
					}
				}
				
				System.out.println("SECTION HEADER OFFSET: " + parser.getSectionHeaderOffset(-1));
				System.out.println("SECTION HEADER OFFSET: " + parser.getSectionHeaderOffset(1));
				System.out.println("SECTION HEADER OFFSET: " + parser.getSectionHeaderOffset(6));
				System.out.println("SECTION HEADER OFFSET: " + parser.getSectionHeaderOffset(7));
				System.out.println("SECTION HEADER OFFSET BY NAME .reloc: "
							+ parser.getSectionHeaderOffset(".reloc"));
				
				System.out
							.println("***************************************************************************************");
				
				OHD od = parser.getDirTable();
				for (OHD.Directory d : OHD.Directory.values()) {
					
					System.out
								.println("---------------------------------------------------------------------------------------");
					System.out.println(d);
					for (OHDD.Property p : OHDD.Property.values()) {
						System.out.println(p + ": " + od.valueOf(d, p));
					}
				}
				
				System.out
							.println("***************************************************************************************");
				
				int size = od.valueOf(OHD.Directory.IMAGE_DIRECTORY_ENTRY_RESOURCE,
							OHDD.Property.SIZE);
				byte[] array = new byte[size];
				ByteBuffer bytes = ByteBuffer.wrap(array);
				bytes.order(ByteOrder.LITTLE_ENDIAN);
				long pos = parser.getDirectoryOffset(OHD.Directory.IMAGE_DIRECTORY_ENTRY_RESOURCE
							.ordinal());
				try {
					parser.getChannel().read(bytes, pos);
				} catch (IOException ex) {}
				bytes.position(0);
				
				System.out
							.println("***************************************************************************************");
				
				RCRoot root = new RCRoot(bytes, new RCDH(bytes));
				
				try {
					while (traverseTree(root, bytes)) {}
				} catch (IOException ex) {}
				
				RCDataEntry de = root.findResource(RCRoot.RT.RCDATA, "1020", "1033");
				for (RCDataEntry.Property p : RCDataEntry.Property.values()) {
					System.out.println(p + ": " + de.valueOf(p));
				}
				
				System.out
							.println("***************************************************************************************");
				
				System.out.println("RESOURCE OFFSET: "
							+ parser.convertRVAToOffset(de.valueOf(RCDataEntry.Property.OFFSET_TO_DATA)
										.intValue(), ".rsrc"));
				
				System.out
							.println("***************************************************************************************");
				
			}
			
			private boolean traverseTree(RCTree tree, ByteBuffer buffer) throws IOException {
				for (RCDirEntry e : tree.values()) {
					if (e.dir == null && e.entry == null) {
						buffer.position(e.valueOfBM(RCDirEntry.Property.OFFSET_TO_DATA, 0x7fffffff));
						
						e.dir = new RCTree(buffer, new RCDH(buffer));
						return true;
					} else if (e.entry == null) {
						traverseTree(e.dir, buffer);
					}
				}
				return false;
			}
		};
		
		// pe2.parse(s2);
		
		Strategy s3 = new Strategy() {
			@Override
			public void parse(PEParse.PEHandle parser) {
				
				OHD od = parser.getDirTable();
						
				int size = od.valueOf(OHD.Directory.IMAGE_DIRECTORY_ENTRY_RESOURCE,
							OHDD.Property.SIZE);
				byte[] array = new byte[size];
				ByteBuffer bytes = ByteBuffer.wrap(array);
				bytes.order(ByteOrder.LITTLE_ENDIAN);
				
				long pos = parser.getDirectoryOffset(OHD.Directory.IMAGE_DIRECTORY_ENTRY_RESOURCE
							.ordinal());
				try {
					parser.getChannel().read(bytes, pos);
				} catch (IOException ex) {}
				
				bytes.position(0);
						
				RCRoot root = new RCRoot(bytes, new RCDH(bytes));
				
				try {
					while (traverseTree(root, bytes)) {}
				} catch (IOException ex) {}
				
				RCDataEntry de = root.findResource(RCRoot.RT.RCDATA, "1020", "1033");
				
				for (RCDataEntry.Property p : RCDataEntry.Property.values()) {
					System.out.println(p + ": " + de.valueOf(p));
				}
				
				System.out
							.println("***************************************************************************************");
				
				System.out.println("RESOURCE OFFSET: "
							+ parser.convertRVAToOffset(de.valueOf(RCDataEntry.Property.OFFSET_TO_DATA)
										.intValue(), ".rsrc"));
				
				System.out
							.println("***************************************************************************************");
				
			}
			
			private boolean traverseTree(RCTree tree, ByteBuffer buffer) throws IOException {
				for (RCDirEntry e : tree.values()) {
					if (e.dir == null && e.entry == null) {
						buffer.position(e.valueOfBM(RCDirEntry.Property.OFFSET_TO_DATA, 0x7fffffff));
						
						e.dir = new RCTree(buffer, new RCDH(buffer));
						return true;
					} else if (e.entry == null) {
						traverseTree(e.dir, buffer);
					}
				}
				return false;
			}
		};
		
		pe2.parse(s3);
	}
}
