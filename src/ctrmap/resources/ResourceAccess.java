
package ctrmap.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class ResourceAccess {
	public static byte[] getByteArray(String name){
		try {
			InputStream in = getStream(name);
			byte[] b = new byte[in.available()];
			in.read(b);
			return b;
		} catch (IOException ex) {
			return null;
		}
	}
	public static InputStream getStream(String name){
		return ResourceAccess.class.getClassLoader().getResourceAsStream("ctrmap/resources/" + name);
	}
}
