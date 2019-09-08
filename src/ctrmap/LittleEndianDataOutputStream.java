package ctrmap;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
* Simple DataOutputStream implementation with automatic reversion of Java BE to LE data.
 */
public class LittleEndianDataOutputStream {
	private DataOutputStream dos;
	public LittleEndianDataOutputStream(OutputStream out) {
		dos = new DataOutputStream(out);
	}
	
	public void writeInt(int v) throws IOException{
		dos.writeInt(Integer.reverseBytes(v));
	}
	
	public void write4Bytes(int v) throws IOException{
		dos.writeInt(v);
	}
	
	public void writeShort(short v) throws IOException{
		dos.writeShort(Short.reverseBytes(v));
	}
	
	public void write2Bytes(short v) throws IOException{
		dos.writeShort(v);
	}
	
	public void writeByte(int v) throws IOException{
		dos.writeByte(v);
	}
	
	public void write(int v) throws IOException{
		dos.write(v);
	}
	
	public void write(byte[] b) throws IOException{
		dos.write(b);
	}
	
	public void writeFloat(float v) throws IOException{
		dos.writeInt(Integer.reverseBytes(Float.floatToIntBits(v)));
	}
	
	public int size() throws IOException{
		return dos.size();
	}
	
	public void close() throws IOException{
		dos.close();
	}
	
	public void flush() throws IOException{
		dos.flush();
	}
}
