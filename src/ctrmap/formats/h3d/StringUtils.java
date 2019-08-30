
package ctrmap.formats.h3d;

import ctrmap.LittleEndianDataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class StringUtils {
	public static String readString(LittleEndianDataInputStream in) throws IOException{
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = in.read()) != 0){
			sb.append((char)read);
		}
		return sb.toString();
	}
	public static String readString(int address, byte[] b) throws IOException{
		byte[] cut = Arrays.copyOfRange(b, address, b.length);
		LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(cut));
		return readString(in);
	}
}
