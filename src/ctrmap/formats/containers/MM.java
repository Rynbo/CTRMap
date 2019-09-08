package ctrmap.formats.containers;

import java.io.File;

/**
 * MapMatrix or MoveModel container class.
 * 
 * Contents/MapMatrix:
 * 0 - Matrix + Zone switches + LOD matrix
 * 1 - Camera repulsors
 * 
 * Contents/MoveModel:
 * 0 - Model
 * 1 - Pack of GfMotion animations
 */
public class MM extends AbstractGamefreakContainer{
	public MM(File f) {
		super(f);
	}
	@Override
	public short getHeader() {
		return 0x4D4D;
	}	
}
