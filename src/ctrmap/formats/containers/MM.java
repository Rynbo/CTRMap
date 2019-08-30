/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.formats.containers;

import java.io.File;

public class MM extends AbstractGamefreakContainer{
	public MM(File f) {
		super(f);
	}
	@Override
	public short getHeader() {
		return 0x4D4D;
	}	
}
