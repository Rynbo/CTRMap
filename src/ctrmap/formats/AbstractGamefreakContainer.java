package ctrmap.formats;

import ctrmap.CtrmapMainframe;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import java.util.Arrays;

public abstract class AbstractGamefreakContainer {
	private File f;
	private int len;
	private int[] offsets;
	
	public abstract short getHeader();

	public AbstractGamefreakContainer(File f) {
		if (f != null) {
			this.f = f;
			verify();
		}
		else {
			System.err.println("Unable to open file. Expect a crash soon.");
		}
	}
	public boolean verify() {
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new FileInputStream(f));
			if (dis.read2Bytes() != getHeader()) {
				System.err.println("GfContainer header mismatch!");
				System.out.println("Error verifying file " + f.getAbsolutePath());
			}
			len = dis.readShort();
			offsets = new int[len + 1];
			for (int i = 0; i < len + 1; i++) {
				offsets[i] = dis.readInt();
			}
			dis.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("An IOException occured while reading " + f.getName());
			return false;
		}
	}
	public File getOriginFile() {
		return f;
	}
	public int getOffset(int fileNum) {
		return offsets[fileNum];
	}
	public byte[] getFile(int fileNum) {
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new FileInputStream(f));
			dis.skip(offsets[fileNum]);
			byte[] b = new byte[offsets[fileNum + 1] - offsets[fileNum]];
			dis.read(b);
			dis.close();
			return b;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public void storeFile(int num, byte[] data) {
		if (Arrays.equals(data, getFile(num))){
			return;
		}
		try {
			int pos = 0;
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new FileInputStream(f));
			byte[] b = new byte[dis.available()];
			dis.read(b);
			dis.close();
			//calculate new offsets
			int change = data.length - (offsets[num + 1] - offsets[num]);
			for (int i = num + 1; i < len + 1; i++) {
				offsets[i] += change;
			}
			LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(new FileOutputStream(f));
			out.write2Bytes(getHeader());
			out.writeShort((short)len);
			pos += 4;
			for (int i = 0; i < offsets.length; i++) {
				out.writeInt(offsets[i]);
				pos += 4;
			}
			while (pos < offsets[0]) {
				out.write(0);
				pos++;
			}
			int firstlen = offsets[num] - offsets[0];
			if (firstlen > 0) {
				byte[] bytesbefore = new byte[firstlen];
				System.arraycopy(b, offsets[0], bytesbefore, 0, firstlen);
				out.write(bytesbefore);
			}
			out.write(data);
			int secondlen = offsets[len] - offsets[num + 1];
			if (secondlen > 0) {
				byte[] bytesafter = new byte[secondlen];
				System.arraycopy(b, offsets[num + 1] - change, bytesafter, 0, secondlen);
				out.write(bytesafter);
			}
			out.flush();
			out.close();
			CtrmapMainframe.mWorkspace.addPersist(getOriginFile());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("An IOException occured while reading " + f.getName());
		}
	}
}
