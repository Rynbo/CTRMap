
package ctrmap.formats;

import java.io.File;

public class BM extends AbstractGamefreakContainer{
	public BM(File f) {
		super(f);
	}
	@Override
	public short getHeader() {
		return 0x424D;
	}
}
