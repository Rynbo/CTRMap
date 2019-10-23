
package ctrmap.resources;

import ctrmap.CtrmapMainframe;
import ctrmap.Workspace;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceAccess {
	public static byte[] getByteArray(String name){
		try {
			InputStream in = getStream(name);
			byte[] b = new byte[in.available()];
			in.read(b);
			in.close();
			return b;
		} catch (IOException ex) {
			return null;
		}
	}
	public static InputStream getStream(String name){
		return ResourceAccess.class.getClassLoader().getResourceAsStream("ctrmap/resources/" + name);
	}
	public static File copyToTemp(String name){
		try {
			InputStream in = getStream(name);
			byte[] b = new byte[in.available()];
			in.read(b);
			in.close();
			File out = new File(Workspace.temp + "/" + name);
			OutputStream os = new FileOutputStream(out);
			os.write(b);
			os.close();
			return out;
		} catch (IOException ex) {
			return null;
		}
	}
}
