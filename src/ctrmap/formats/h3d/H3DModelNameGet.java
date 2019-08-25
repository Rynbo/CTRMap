
package ctrmap.formats.h3d;

import ctrmap.LittleEndianDataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class for obtaining model names from BCH containers, based on VisualBasic Ohana3DS source.
 * Ohana3DS-Rebirth was also used for docs but it relocates the offsets based on the reloc table which we do not
 * need in this case.
 * This class assumes that the file contains at least 1 model.
 */
public class H3DModelNameGet {
	public static String H3DModelNameGet(byte[] bchFile){
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(bchFile);
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
			dis.skip(8);
			int contentHeaderOffset = dis.readInt();
			int stringTableOffset = dis.readInt();
			dis.skip(contentHeaderOffset - 16);
			int modelsPointerTableOffset = dis.readInt() + contentHeaderOffset; //relocate
			is.reset();
			dis.skip(modelsPointerTableOffset);
			int model0offset = dis.readInt() + contentHeaderOffset; //relocate again
			is.reset();
			dis.skip(model0offset);
			dis.skip(0x84);
			int stroff = dis.readInt();
			is.reset();
			dis.skip(stroff + stringTableOffset);
			StringBuilder sb = new StringBuilder();
			int read;
			while ((read = dis.read()) != 0){
				sb.append((char)read);
			}
			dis.close();
			is.close();
			return sb.toString();
		} catch (IOException ex) {
			Logger.getLogger(H3DModelNameGet.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
}