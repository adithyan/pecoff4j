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

public class DataEntry
{
    public boolean isSection;
    public int index;
    public String name;
    public int pointer;
    public boolean isDebugRawData;
    public int baseAddress;

    public DataEntry() {
    }

    public DataEntry(int index, int pointer) {
        this.index = index;
        this.pointer = pointer;
    }
    
    public DataEntry(int index, int pointer, String name) {
      this.index = index;
      this.pointer = pointer;
      this.name = name;
  }
}
