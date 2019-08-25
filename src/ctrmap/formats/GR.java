package ctrmap.formats;

import java.io.File;

public class GR extends AbstractGamefreakContainer{

	public GR(File f) {
		super(f);
	}

	@Override
	public short getHeader() {
		return 0x4752;
	}

}
