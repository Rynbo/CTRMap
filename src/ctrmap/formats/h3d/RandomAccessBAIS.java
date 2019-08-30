package ctrmap.formats.h3d;

import ctrmap.LittleEndianDataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A class to imitate RandomAccessFile for byte array streams
 */
public class RandomAccessBAIS extends LittleEndianDataInputStream {

	private InputStream input;
	public int position = 0;

	public RandomAccessBAIS(InputStream in) {
		super(in);
		input = in;
		if (!in.markSupported()) {
			throw new IllegalArgumentException("This class only supports resettable streams");
		}
	}

	@Override
	public int readInt() throws IOException {
		position += 4;
		return super.readInt();
	}

	@Override
	public int read4Bytes() throws IOException {
		position += 4;
		return super.read4Bytes();
	}

	@Override
	public short readShort() throws IOException {
		position += 2;
		return super.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return Short.toUnsignedInt(readShort());
	}

	public short read2Bytes() throws IOException {
		position += 2;
		return super.read2Bytes();
	}

	@Override
	public byte readByte() throws IOException {
		position++;
		return super.readByte();
	}

	@Override
	public int read() throws IOException {
		position++;
		return super.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		int read = super.read(b);
		position += read;
		return read;
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public long skip(long n) throws IOException {
		position += n;
		return super.skip(n);
	}

	public void seek(int offset) throws IOException {
		input.reset();
		position = offset;
		input.skip(offset);
	}
}
