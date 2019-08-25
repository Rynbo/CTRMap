package ctrmap.formats;

import java.io.File;

public class AD extends AbstractGamefreakContainer{

	public AD(File f) {
		super(f);
	}

	@Override
	public short getHeader() {
		return 0x4144;
	}

}
