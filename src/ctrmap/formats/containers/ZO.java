
package ctrmap.formats.containers;

import java.io.File;

public class ZO extends AbstractGamefreakContainer{

	public ZO(File f) {
		super(f);
	}

	@Override
	public short getHeader() {
		return 0x5a4f;
	}
}
