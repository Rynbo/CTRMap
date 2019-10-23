package ctrmap.formats.cameradata;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.Utils;
import ctrmap.formats.containers.AD;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser and accessor for AD-3 Camera data.
 */
public class CameraDataFile {
	private AD file;
	
	public int numEntries;
	public ArrayList<CameraData> camData;
	
	public boolean modified = false;
	
	public CameraDataFile(AD ad) {
		file = ad;
		byte[] input = ad.getFile(3);
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(input));
			numEntries = dis.readInt();
			camData = new ArrayList<>();
			for (int i = 0; i < numEntries; i++) {
				camData.add(new CameraData(dis));
			}
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void write(){
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(out);
			dos.writeInt(camData.size());
			for (CameraData d : camData){
				d.write(dos);
			}
			file.storeFile(3, out.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(CameraDataFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
