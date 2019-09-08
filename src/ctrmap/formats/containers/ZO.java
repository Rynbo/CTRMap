
package ctrmap.formats.containers;

import java.io.File;

/**
 * Zone container class.
 * 
 * Contents:
 * 0 - Zone header
 * 1 - Zone entities
 * 2 - Map script
 * 3 - Encounter data
 * 4 - Reflections (NPC mirror)
 */
public class ZO extends AbstractGamefreakContainer{

	public ZO(File f) {
		super(f);
	}

	@Override
	public short getHeader() {
		return 0x5a4f;
	}
}
