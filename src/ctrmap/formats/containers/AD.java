package ctrmap.formats.containers;

import java.io.File;

/**
 * Overworld AreaData container
 * 
 * Contents:
 * 0 - Prop registry
 * 1 - Prop textures
 * 2 - World animations
 * 3 - Camera data
 * 4 - ???
 * 5 - Environment assets (clouds, extra lighting)
 * 6 - Default camera data
 * 7 - Extended area collision
 * 8 - Map LOD assets
 * 9 - Global LOD assets
 * 10 - Unused in both XY and ORAS
 * 11 - World textures
 */
public class AD extends AbstractGamefreakContainer{

	public AD(File f) {
		super(f);
	}

	@Override
	public short getHeader() {
		return 0x4144;
	}

}
