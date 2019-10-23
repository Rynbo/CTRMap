package ctrmap.formats.containers;

import ctrmap.Utils;
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
	public static final int MM_MAP_MATRIX = 0;
	public static final int MM_MOVE_MODEL = 1;
	
	public int type;
	
	public MM(File f) {
		super(f);
		if (Utils.checkBCHMagic(super.getFile(0))){
			type = MM_MOVE_MODEL;
		}
		else {
			type = MM_MAP_MATRIX;
		}
	}
	@Override
	public short getHeader() {
		return 0x4D4D;
	}	

	@Override
	public ContentType getDefaultContentType(int index) {
		if (type == MM_MAP_MATRIX){
			if (index == 0){
				return ContentType.H3D_TEXTURE_PACK;
			}
			else if (index == 1){
				return ContentType.CAMERA_DATA_MM_EXTRA;
			}
		}
		else {
			if (index == 0){
				return ContentType.H3D_MODEL;
			}
			else {
				return ContentType.GF_MOTION;
			}
		}
		return ContentType.UNKNOWN;
	}

	@Override
	public boolean getIsPadded() {
		return true;
	}
}
