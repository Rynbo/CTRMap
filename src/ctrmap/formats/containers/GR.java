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

	public GR(File f) {
		super(f);
	}

	@Override
	public short getHeader() {
		return 0x4752;
	}

}
