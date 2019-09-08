package ctrmap;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple DataInputStream implementation with automatic reversion of LE to Java BE.
 */
public class LittleEndianDataInputStream {
	private DataInputStream dis;
	public LittleEndianDataInputStream(InputStream in) {
		dis = new DataInputStream(in);
	}
	
	public int readInt() throws IOException{
		return Integer.reverseBytes(dis.readInt());
	}
	
	public int read4Bytes() throws IOException{
		return dis.readInt();
	}
	
	public short readShort() throws IOException{
		return Short.reverseBytes(dis.readShort());
	}
	
	public int readUnsignedShort() throws IOException{
		return Short.toUnsignedInt(readShort());
	}
	
	public short read2Bytes() throws IOException{
		return dis.readShort();
	}
	
	public byte readByte() throws IOException{
		return dis.readByte();
	}
	
	public int read() throws IOException{
		return dis.read();
	}
	
	public int read(byte[] b) throws IOException{
		return dis.read(b);
	}
	
	public float readFloat() throws IOException{
		return Float.intBitsToFloat(readInt());
	}
	
	public int available() throws IOException{
		return dis.available();
	}
	
	public long skip(long n) throws IOException{
		return dis.skip(n);
	}
	
	public void close() throws IOException{
		dis.close();
	}
}
