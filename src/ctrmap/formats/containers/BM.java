
package ctrmap.formats.containers;

import java.io.File;

/**
 * BuildModel container class.
 * 
 * Contents:
 * 0 - Model
 * 1 to 65535 - BCH animations or CGFX particles
 */
public class BM extends AbstractGamefreakContainer{
	public BM(File f) {
		super(f);
	}
	@Override
	public short getHeader() {
		return 0x424D;
	}
}
