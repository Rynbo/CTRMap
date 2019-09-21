package ctrmap.formats.garc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * LZ11 decompression ported from C# source of PK3DS, originally from dsdecmp
 * LZ11 compression ported from Ohana3DS's VB.NET using https://www.carlosag.net/tools/codetranslator/ for syntax and some intuition to make it work from Ohana3DS (non-Rebirth)
 * 
 */
public class LZ11 {

	public static byte[] decompress(byte[] data) {
		ByteArrayInputStream instream = new ByteArrayInputStream(data);
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();

		byte type = (byte) instream.read();
		if (type != 0x11) {
			System.err.println("Not a LZ11 compressed file");
		}
		byte[] sizeBytes = new byte[4];
		instream.read(sizeBytes, 0, 3);
		int decompressedSize = Integer.reverseBytes(ByteBuffer.wrap(sizeBytes).getInt());

		final int bufferLength = 0x1000;
		byte[] buffer = new byte[bufferLength];
		int bufferOffset = 0;

		int currentOutSize = 0;
		int flags = 0, mask = 1;
		while (currentOutSize < decompressedSize) {
			if (mask == 1) {
				flags = instream.read();
				mask = 0x80;
			} else {
				mask >>= 1;
			}
			if ((flags & mask) > 0) {
				int byte1 = instream.read();
				int length = byte1 >> 4;
				int disp;

				switch (length) {
					case 0: {
						int byte2 = instream.read();
						int byte3 = instream.read();
						length = (((byte1 & 0x0F) << 4) | (byte2 >> 4)) + 0x11;
						disp = (((byte2 & 0x0F) << 8) | byte3) + 0x1;
						break;
					}
					case 1: {
						int byte2 = instream.read();
						int byte3 = instream.read();
						int byte4 = instream.read();
						length = (((byte1 & 0x0F) << 12) | (byte2 << 4) | (byte3 >> 4)) + 0x111;
						disp = (((byte3 & 0x0F) << 8) | byte4) + 0x1;
						break;
					}
					default: {
						int byte2 = instream.read();
						length = ((byte1 & 0xF0) >> 4) + 0x1;
						disp = (((byte1 & 0x0F) << 8) | byte2) + 0x1;
						break;
					}
				}

				int bufIdx = bufferOffset + bufferLength - disp;
				for (int i = 0; i < length; i++) {
					byte next = buffer[bufIdx % bufferLength];
					bufIdx++;
					outstream.write(next);
					buffer[bufferOffset] = next;
					bufferOffset = (bufferOffset + 1) % bufferLength;
				}
				currentOutSize += length;
			} else {
				int next = instream.read();

				outstream.write((byte) next);
				currentOutSize++;
				buffer[bufferOffset] = (byte) next;
				bufferOffset = (bufferOffset + 1) % bufferLength;
			}
		}
		return outstream.toByteArray();
	}

	public static byte[] compress(byte[] data) {
		byte[] dictionary = new byte[4096];
		int dicOffset = 0;
		byte[] compressed = new byte[data.length + data.length / 8 + 3];
		int dataOffset = 0;
		int compressedOffset = 0;
		int bitCount = 0;
		compressed[0] = 0x11;
		compressed[1] = (byte) (data.length & 0xFF);
		compressed[2] = (byte) ((data.length & 0xFF00) >> 8);
		compressed[3] = (byte) ((data.length & 0xFF0000) >> 16);
		compressedOffset += 4;
		int bitsPtr = 0;
		while (dataOffset < data.length) {
			if (bitCount == 0) {
				bitsPtr = compressedOffset;
				compressedOffset++;
				bitCount = 8;
			}

			int dicPos = 0;
			int foundData = 0;
			boolean compressedData = false;
			int idx = indexOfIntArray(dictionary, data[dataOffset]);
			if (idx != -1) {
				while (true) {
					int dataSize = 0;
					for (int j = 0; j <= 15; j++) {
						if (dataOffset + j >= data.length) {
							break;
						}
						if (dictionary[(idx + j) & 0xFFF] == data[dataOffset + j]) {
							dataSize++;
						} 
						else {
							break;
						}
					}
					if (dataSize >= 3) {
						if ((idx + dataSize < dicOffset) || (idx > dicOffset + dataSize)) {
							if (dataSize > foundData) {
								compressedData = true;
								foundData = dataSize;
								dicPos = idx;
							}

						}

					}
					idx = indexOfIntArray(dictionary, data[dataOffset], idx + 1);
					if ((idx == -1)) {
						break;
					}

				}

			}

			if (compressedData && ((dicPos < dataOffset) || (dataOffset > 0xFFF))) {
				int back = dicOffset - dicPos - 1;
				compressed[bitsPtr] = (byte)(compressed[bitsPtr] | ((byte)Math.pow(2, bitCount - 1)));
				compressed[compressedOffset] = (byte)((((foundData - 1) & 0xF) * 0x10) + ((back & 0xF00) / 0x100));
				compressed[compressedOffset + 1] = (byte)(back & 0xFF);
				compressedOffset += 2;
				for (int j = 0; j <= foundData - 1; j++) {
					dictionary[dicOffset] = data[dataOffset];
					dicOffset = (dicOffset + 1) & 0xFFF;
					dataOffset++;
				}

			} else {
				compressed[compressedOffset] = data[dataOffset];
				dictionary[dicOffset] = compressed[compressedOffset];
				dicOffset = (dicOffset + 1) & 0xFFF;
				compressedOffset++;
				dataOffset++;
			}
			bitCount--;
		}
		byte[] ret = new byte[compressedOffset];
		System.arraycopy(compressed, 0, ret, 0, ret.length);
		return ret;
	}

	public static int indexOfIntArray(byte[] array, int key){
		return indexOfIntArray(array, key, 0);
	}
	public static int indexOfIntArray(byte[] array, int key, int startIndex) {
		int returnvalue = -1;
		for (int i = startIndex; i < array.length; ++i) {
			if (key == array[i]) {
				returnvalue = i;
				break;
			}
		}
		return returnvalue;
	}
}
