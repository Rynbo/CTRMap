
package ctrmap.formats.containers;

import java.io.File;

public class DefaultGamefreakContainer extends AbstractGamefreakContainer{
	private short magic;
	
	public DefaultGamefreakContainer(File f, short magic){
		super(f);
		this.magic = magic;
	}
	
	@Override
	public short getHeader() {
		return magic;
	}

	@Override
	public ContentType getDefaultContentType(int index) {
		return ContentType.UNKNOWN;
	}

	@Override
	public boolean getIsPadded() {
		return true;
	}
}
