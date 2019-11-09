package ctrmap.formats.containers;

import java.io.File;

/**
 * GR (Game Region? as coined by Kaphotics somewhere) container class.
 * 
 * Contents:
 * 0 - Tilemap
 * 1 - Model
 * 2 - Collision mesh
 * 3 - Prop placement data.
 * 4 - Extended GR data.
 * 5 - Encounter model
 * [ORAS] - KAGE description, likely used for dynamic shadows or something (hence the name) 
 * but was scrapped early and is not-000000 only in the intro truck, where zeroing it changes nothing.
 */
public class GR extends AbstractGamefreakContainer{
	public ContentType[] contents = new ContentType[]{
		ContentType.TILEMAP,
		ContentType.H3D_MODEL,
		ContentType.COLLISION,
		ContentType.PROP_DATA,
		ContentType.UNKNOWN,
		ContentType.H3D_MODEL
	};
	
	public GR(File f) {
		super(f);
	}
	
	public GR(File f, int len){
		super(f, len);
	}

	@Override
	public short getHeader() {
		return 0x4752;
	}

	@Override
	public ContentType getDefaultContentType(int index) {
		if (index > 5){
			//multi layer GR
			//can be tilemap or coll
			return ContentType.UNKNOWN;
		}
		else {
			return contents[index];
		}
	}

	@Override
	public boolean getIsPadded() {
		return true;
	}
}
