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
	
	public ContentType[] contents = {
		ContentType.PROP_REGISTRY,
		ContentType.H3D_TEXTURE_PACK,
		ContentType.H3D_OTHER,
		ContentType.CAMERA_DATA,
		ContentType.UNKNOWN,
		ContentType.H3D_OTHER,
		ContentType.CAMERA_DATA,
		ContentType.COLLISION,
		ContentType.SUB_CONTAINER,
		ContentType.SUB_CONTAINER,
		ContentType.UNKNOWN,
		ContentType.H3D_TEXTURE_PACK
	};
	
	public AD(File f) {
		super(f);
	}

	@Override
	public short getHeader() {
		return 0x4144;
	}

	@Override
	public ContentType getDefaultContentType(int index) {
		return contents[index];
	}

	@Override
	public boolean getIsPadded() {
		return true;
	}
}
