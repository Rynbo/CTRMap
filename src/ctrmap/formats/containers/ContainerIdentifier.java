package ctrmap.formats.containers;

import ctrmap.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContainerIdentifier {

	public static AbstractGamefreakContainer makeAGFC(File f, byte[] magic) {
		if (Utils.isUTF8Capital(magic[0]) && Utils.isUTF8Capital(magic[1])) {
			String magicStr = new String(new byte[]{magic[0], magic[1]});
			switch (magicStr) {
				case "AD":
					return new AD(f);
				case "BM":
					return new BM(f);
				case "GR":
					return new GR(f);
				case "MM":
					return new MM(f);
				case "ZO":
					return new ZO(f);
				default:
					return new DefaultGamefreakContainer(f, ByteBuffer.wrap(magic).getShort());
			}
		} else {
			return null;
		}
	}

	public static AbstractGamefreakContainer makeAGFC(File f) {
		try {
			InputStream in = new FileInputStream(f);
			byte[] magic = new byte[2];
			in.read(magic);
			in.close();
			return makeAGFC(f, magic);
		} catch (IOException ex) {
			Logger.getLogger(ContainerIdentifier.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

}
