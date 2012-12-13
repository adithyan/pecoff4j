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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class RCTree extends LinkedHashMap<String, RCDirEntry> {
	
	private static final long serialVersionUID = 855142791164906461L;
	
	public RCDH header;
	public int numIDEntries;
	public List<RCDataEntry> dataEntries = new LinkedList<>();
	
	public RCTree(ByteBuffer buffer, RCDH dirHeader) {
		super(dirHeader.numEntries);
		this.header = dirHeader;
		numIDEntries = header.valueOf(RCDH.Property.NUMBER_OF_ID_ENTRIES).intValue();
		
		RCDirEntry e = null;
		
		// Named entries
		for (int i = 0; i < header.numEntries - numIDEntries; i++) {
			e = new RCDirEntry(buffer, true, this);
			put(e.getName(), e);
		}
		// ID entries
		for (int i = 0; i < numIDEntries; i++) {
			e = new RCDirEntry(buffer, false, this);
			put(e.valueOf(RCDirEntry.Property.NAME).toString(), e);
			doExtra(e);
		}
	}
	
	protected void doExtra(RCDirEntry e) {};
	
	public List<RCDataEntry> getDataEntries() {
		
		for (RCDirEntry e : this.values()) {
			if (!e.dir.dataEntries.isEmpty()) {
				if (e.dir != null) {
				
					dataEntries.addAll(e.dir.dataEntries);
				} else if (e.entry != null) {
					e.parent.dataEntries.add(e.entry);
				}
			} else {
				e.dir.getDataEntries();
			}
		}
		return dataEntries;
	}
	
	public RCDataEntry findResource(String name, String language) {
		return get(name).dir.findResource(language);
	}
	
	public RCDataEntry findResource(String res) {
		return this.get(res).entry;		
	}
	
	public RCTree findResourceTree(String name) {
		return this.get(name).dir;
	}
}