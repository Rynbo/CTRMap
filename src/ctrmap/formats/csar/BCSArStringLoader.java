package ctrmap.formats.csar;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BCSArStringLoader {

	public static String[] getStrings(File bcsar) {
		try {
			RandomAccessFile dis = new RandomAccessFile(bcsar, "r");
			dis.skipBytes(0x18);
			int strgOffset = Integer.reverseBytes(dis.readInt());
			dis.skipBytes(8);
			int infoOffset = Integer.reverseBytes(dis.readInt());
			dis.seek(strgOffset);
			dis.skipBytes(0x18);
			int fileCount = Integer.reverseBytes(dis.readInt());
			String[] names = new String[fileCount];
			int[] offsets = new int[fileCount];
			int[] lengths = new int[fileCount];
			for (int file = 0; file < fileCount; file++) {
				dis.skipBytes(4); //node type
				offsets[file] = Integer.reverseBytes(dis.readInt()) + strgOffset + 0x18;
				lengths[file] = Integer.reverseBytes(dis.readInt());
			}
			for (int file = 0; file < fileCount; file++) {
				dis.seek(offsets[file]);
				StringBuilder sb = new StringBuilder();
				int read;
				while ((read = dis.read()) != 0) {
					sb.append((char) read);
				}
				names[file] = sb.toString();
			}
			return names;
		} catch (IOException ex) {
			Logger.getLogger(BCSArStringLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
