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
import java.util.EnumMap;

/**
 * Completely re written 
 * 
 * @author Daniel Kemp
 *
 */
public class RCRoot extends RCTree {
	
	private static final long serialVersionUID = 855142791164906461L;
	
	/**
	 * Predefined Resource Types
	 */
	public static enum RT {
		
		EMPTY_0(0, "s"),
		
		/**
		 * Hardware-dependent cursor resource.
		 */
		CURSOR(1, "Cursors"),
		
		/**
		 * Bitmap resource.
		 */
		BITMAP(2, "Bitmap"),
		
		/**
		 * Hardware-dependent icon resource.
		 */
		ICON(3, "Icon"),
		
		/**
		 * Menu resource.
		 */
		MENU(4, "Menu"),
		
		/**
		 * Dialog box.
		 */
		DIALOG(5, "Dialog"),
		
		/**
		 * String-table entry.
		 */
		STRING(6, "String"),
		
		/**
		 * Font directory resource.
		 */
		FONTDIR(7, "Font Dir"),
		
		/**
		 * Font resource.
		 */
		FONT(8, "Font"),
		
		/**
		 * Accelerator table.
		 */
		ACCELERATOR(9, "Accelerator Table"),
		
		/**
		 * Application-defined resource (raw data).
		 */
		RCDATA(10, "RCData"),
		
		/**
		 * Message-table entry.
		 */
		MESSAGETABLE(11, "Message Table"),
		
		/**
		 * Hardware-independent cursor resource.
		 */
		GROUP_CURSOR(12, "Cursor Groups"),
		
		EMPTY_13(13, "s"),

		/**
		 * Hardware-independent icon resource.
		 */
		GROUP_ICON(14, "Icon Groups"),
		
		EMPTY_15(15, "s"),

		/**
		 * Version resource.
		 */
		VERSION(16, "Version"),
		
		/**
		 * Allows a resource editing tool to associate a string with an .rc file.
		 * Typically, the string is the name of the header file that provides
		 * symbolic names. The resource compiler parses the string but otherwise
		 * ignores the value. For example, 1 DLGINCLUDE "MyFile.h"
		 */
		DLGINCLUDE(17, "Resource Include"),
		
		EMPTY_18(18, "s"),

		/**
		 * Plug and Play resource.
		 */
		PLUGPLAY(19, "Plug and Play"),
		
		/**
		 * VXD
		 */
		VXD(20, "VXD"),
		
		/**
		 * Animated cursor.
		 */
		ANICURSOR(21, "Animated Cursor"),
		
		/**
		 * Animated icon.
		 */
		ANIICON(22, "Animated Icon"),
		
		/**
		 * HTML resource.
		 */
		_HTML(23, "HTML"),
		
		/**
		 * Side-by-Side Assembly Manifest.
		 */
		MANIFEST(24, "Manifest");

		public int type;
		public String name;
		
		RT(int type, String name) {
			this.type = type;
			this.name = name;
		}
	}
	
	public EnumMap<RCRoot.RT, RCDirEntry> map;
	
	public RCRoot(ByteBuffer buffer, RCDH header)  {
		super(buffer, header);			
	}
	
	protected void doExtra(RCDirEntry e) {
		if ( map == null) {
			map = new EnumMap<RT, RCDirEntry>(RT.class);
		}
		Integer id = e.valueOf(RCDirEntry.Property.NAME);
		map.put(RCRoot.RT.values()[id.intValue()], e);		
	}
	
	public RCTree findResourceTree(RCRoot.RT type) {
		return map.get(type).dir;
	}
	
	public RCTree findResourceTree(RCRoot.RT type, String name) {
		return findResourceTree(type).findResourceTree(name);
	}

	public RCDataEntry findResource(RT type, String name, String language) {
		return map.get(type).dir.findResource(name, language);
	}
}