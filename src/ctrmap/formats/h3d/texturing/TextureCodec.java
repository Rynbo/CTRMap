package ctrmap.formats.h3d.texturing;

import ctrmap.formats.h3d.PICACommandReader;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


/**
 * Full port of the Ohana class
 */
public class TextureCodec {

	private static int[] tileOrder = {0, 1, 8, 9, 2, 3, 10, 11, 16, 17, 24, 25, 18, 19, 26, 27, 4, 5, 12, 13, 6, 7, 14, 15, 20, 21, 28, 29, 22, 23, 30, 31, 32, 33, 40, 41, 34, 35, 42, 43, 48, 49, 56, 57, 50, 51, 58, 59, 36, 37, 44, 45, 38, 39, 46, 47, 52, 53, 60, 61, 54, 55, 62, 63};
	private static int[][] etc1LUT = {{2, 8, -2, -8}, {5, 17, -5, -17}, {9, 29, -9, -29}, {13, 42, -13, -42}, {18, 60, -18, -60}, {24, 80, -24, -80}, {33, 106, -33, -106}, {47, 183, -47, -183}};

	public static byte[] decode(byte[] data, int width, int height, PICACommandReader.TextureFormat format) {
		byte[] output = new byte[width * height * 4];
		int dataOffset = 0;
		boolean toggle = false;

		switch (format) {
			case rgba8:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + ((tY * 8 + y) * width)) * 4;

							/*output[outputOffset] = data[dataOffset + 3];
							output[outputOffset + 1] = data[dataOffset + 2];
							output[outputOffset + 2] = data[dataOffset + 1];
							output[outputOffset + 3] = data[dataOffset];*/
							output[outputOffset] = data[dataOffset + 1];
							output[outputOffset + 1] = data[dataOffset + 2];
							output[outputOffset + 2] = data[dataOffset + 3];
							output[outputOffset + 3] = data[dataOffset];
							dataOffset += 4;
						}
					}
				}
				break;

			case rgb8:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							System.arraycopy(data, dataOffset, output, outputOffset, 3);
							output[outputOffset + 3] = (byte) 0xff;

							dataOffset += 3;
						}
					}
				}
				break;

			case rgba5551:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							int pixelData = (data[dataOffset] | (data[dataOffset + 1] << 8));

							byte r = (byte) (((pixelData >> 1) & 0x1f) << 3);
							byte g = (byte) (((pixelData >> 6) & 0x1f) << 3);
							byte b = (byte) (((pixelData >> 11) & 0x1f) << 3);
							byte a = (byte) ((pixelData & 1) * 0xff);

							output[outputOffset] = (byte) (r | (r >> 5));
							output[outputOffset + 1] = (byte) (g | (g >> 5));
							output[outputOffset + 2] = (byte) (b | (b >> 5));
							output[outputOffset + 3] = a;

							dataOffset += 2;
						}
					}
				}
				break;

			case rgb565:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							int pixelData = (data[dataOffset] | (data[dataOffset + 1] << 8));

							byte r = (byte) ((pixelData & 0x1f) << 3);
							byte g = (byte) (((pixelData >> 5) & 0x3f) << 2);
							byte b = (byte) (((pixelData >> 11) & 0x1f) << 3);

							output[outputOffset] = (byte) (r | (r >> 5));
							output[outputOffset + 1] = (byte) (g | (g >> 6));
							output[outputOffset + 2] = (byte) (b | (b >> 5));
							output[outputOffset + 3] = (byte) 0xff;

							dataOffset += 2;
						}
					}
				}
				break;

			case rgba4:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							int pixelData = (data[dataOffset] | (data[dataOffset + 1] << 8));

							byte r = (byte) ((pixelData >> 4) & 0xf);
							byte g = (byte) ((pixelData >> 8) & 0xf);
							byte b = (byte) ((pixelData >> 12) & 0xf);
							byte a = (byte) (pixelData & 0xf);

							output[outputOffset] = (byte) (r | (r << 4));
							output[outputOffset + 1] = (byte) (g | (g << 4));
							output[outputOffset + 2] = (byte) (b | (b << 4));
							output[outputOffset + 3] = (byte) (a | (a << 4));

							dataOffset += 2;
						}
					}
				}
				break;

			case la8:
			case hilo8:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							output[outputOffset] = data[dataOffset];
							output[outputOffset + 1] = data[dataOffset];
							output[outputOffset + 2] = data[dataOffset];
							output[outputOffset + 3] = data[dataOffset + 1];

							dataOffset += 2;
						}
					}
				}
				break;

			case l8:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							output[outputOffset] = data[dataOffset];
							output[outputOffset + 1] = data[dataOffset];
							output[outputOffset + 2] = data[dataOffset];
							output[outputOffset + 3] = (byte) 0xff;

							dataOffset++;
						}
					}
				}
				break;

			case a8:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							output[outputOffset] = (byte) 0xff;
							output[outputOffset + 1] = (byte) 0xff;
							output[outputOffset + 2] = (byte) 0xff;
							output[outputOffset + 3] = data[dataOffset];

							dataOffset++;
						}
					}
				}
				break;

			case la4:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							output[outputOffset] = (byte) (data[dataOffset] >> 4);
							output[outputOffset + 1] = (byte) (data[dataOffset] >> 4);
							output[outputOffset + 2] = (byte) (data[dataOffset] >> 4);
							output[outputOffset + 3] = (byte) (data[dataOffset] & 0xf);

							dataOffset++;
						}
					}
				}
				break;

			case l4:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							byte c = toggle ? (byte) ((data[dataOffset++] & 0xf0) >> 4) : (byte) (data[dataOffset] & 0xf);
							toggle = !toggle;
							c = (byte) ((c << 4) | c);
							output[outputOffset] = c;
							output[outputOffset + 1] = c;
							output[outputOffset + 2] = c;
							output[outputOffset + 3] = (byte) 0xff;
						}
					}
				}
				break;

			case a4:
				for (int tY = 0; tY < height / 8; tY++) {
					for (int tX = 0; tX < width / 8; tX++) {
						for (int pixel = 0; pixel < 64; pixel++) {
							int x = tileOrder[pixel] % 8;
							int y = (tileOrder[pixel] - x) / 8;
							int outputOffset = ((tX * 8) + x + (((tY * 8 + y)) * width)) * 4;

							output[outputOffset] = (byte) 0xff;
							output[outputOffset + 1] = (byte) 0xff;
							output[outputOffset + 2] = (byte) 0xff;
							byte a = toggle ? (byte) ((data[dataOffset++] & 0xf0) >> 4) : (byte) (data[dataOffset] & 0xf);
							toggle = !toggle;
							output[outputOffset + 3] = (byte) ((a << 4) | a);
						}
					}
				}
				break;

			case etc1:
			case etc1a4:
				byte[] decodedData = etc1Decode(data, width, height, format == PICACommandReader.TextureFormat.etc1a4);
				int[] etc1Order = etc1Scramble(width, height);
				int i = 0;
				for (int tY = 0; tY < height / 4; tY++) {
					for (int tX = 0; tX < width / 4; tX++) {
						int TX = etc1Order[i] % (width / 4);
						int TY = (etc1Order[i] - TX) / (width / 4);
						for (int y = 0; y < 4; y++) {
							for (int x = 0; x < 4; x++) {
								dataOffset = ((TX * 4) + x + (((TY * 4) + y) * width)) * 4;
								long outputOffset = ((tX * 4) + x + (((tY * 4 + y)) * width)) * 4;

								System.arraycopy(decodedData, (int) dataOffset, output, (int) outputOffset, 4);
							}
						}
						i += 1;
					}
				}

				break;
		}
		byte[] flip = new byte[width*height*4]; //flip for OpenGL and convert to RGBA
		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				int offset = y * width * 4 + x * 4;
				byte red = output[output.length - offset - 4];
				byte green = output[output.length - offset - 4 + 1];
				byte blue = output[output.length - offset - 4 + 2];
				byte alpha = output[output.length - offset - 4 + 3];
				flip[offset] = blue;
				flip[offset + 1] = green;
				flip[offset + 2] = red;
				flip[offset + 3] = alpha;
			}
		}
		/*BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				int index = r * width * 4 + c * 4;
				int red = output[index] & 0xFF;
				int green = output[index + 1] & 0xFF;
				int blue = output[index + 2] & 0xFF;
				int rgb = (red << 16) | (green << 8) | blue;
				img.setRGB(c, r, rgb);
			}
		}
		try {
			ImageIO.write(img, "png", new File("out" + System.currentTimeMillis() + ".png"));
		} catch (IOException ex) {
			Logger.getLogger(TextureCodec.class.getName()).log(Level.SEVERE, null, ex);
		*/
		return flip;
	}

	private static byte[] etc1Decode(byte[] input, int width, int height, boolean alpha) {
		byte[] output = new byte[(width * height * 4)];
		int offset = 0;

		for (int y = 0; y < height / 4; y++) {
			for (int x = 0; x < width / 4; x++) {
				byte[] colorBlock = new byte[8];
				byte[] alphaBlock = new byte[8];
				if (alpha) {
					for (int i = 0; i < 8; i++) {
						colorBlock[7 - i] = input[offset + 8 + i];
						alphaBlock[i] = input[offset + i];
					}
					offset += 16;
				} else {
					for (int i = 0; i < 8; i++) {
						colorBlock[7 - i] = input[offset + i];
						alphaBlock[i] = (byte) 0xff;
					}
					offset += 8;
				}

				colorBlock = etc1DecodeBlock(colorBlock);

				boolean toggle = false;
				int alphaOffset = 0;
				for (int tX = 0; tX < 4; tX++) {
					for (int tY = 0; tY < 4; tY++) {
						int outputOffset = (x * 4 + tX + ((y * 4 + tY) * width)) * 4;
						int blockOffset = (tX + (tY * 4)) * 4;
						System.arraycopy(colorBlock, blockOffset, output, outputOffset, 3);

						byte a = (byte) (toggle ? (alphaBlock[alphaOffset++] & 0xf0) >> 4 : alphaBlock[alphaOffset] & 0xf);
						output[outputOffset + 3] = (byte) ((a << 4) | a);
						toggle = !toggle;
					}
				}
			}
		}

		return output;
	}

	private static byte[] etc1DecodeBlock(byte[] data) {
		long blockTop = Integer.toUnsignedLong(ByteBuffer.wrap(data, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
		long blockBottom = Integer.toUnsignedLong(ByteBuffer.wrap(data, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());

		boolean flip = (blockTop & 0x1000000) > 0;
		boolean difference = (blockTop & 0x2000000) > 0;

		long r1, g1, b1;
		long r2, g2, b2;

		if (difference) {
			r1 = blockTop & 0xf8;
			g1 = (blockTop & 0xf800) >> 8;
			b1 = (blockTop & 0xf80000) >> 16;

			r2 = (r1 >> 3) + ((blockTop & 7) << 5) >> 5;
			g2 = (g1 >> 3) + ((blockTop & 0x700) >> 3) >> 5;
			b2 = (b1 >> 3) + ((blockTop & 0x70000) >> 11) >> 5;

			r1 |= r1 >> 5;
			g1 |= g1 >> 5;
			b1 |= b1 >> 5;

			r2 = (r2 << 3) | (r2 >> 2);
			g2 = (g2 << 3) | (g2 >> 2);
			b2 = (b2 << 3) | (b2 >> 2);
		} else {
			r1 = blockTop & 0xf0;
			g1 = (blockTop & 0xf000) >> 8;
			b1 = (blockTop & 0xf00000) >> 16;

			r2 = (blockTop & 0xf) << 4;
			g2 = (blockTop & 0xf00) >> 4;
			b2 = (blockTop & 0xf0000) >> 12;

			r1 |= r1 >> 4;
			g1 |= g1 >> 4;
			b1 |= b1 >> 4;

			r2 |= r2 >> 4;
			g2 |= g2 >> 4;
			b2 |= b2 >> 4;
		}

		long table1 = (blockTop >> 29) & 7;
		long table2 = (blockTop >> 26) & 7;

		byte[] output = new byte[(4 * 4 * 4)];
		if (!flip) {
			for (int y = 0; y <= 3; y++) {
				for (int x = 0; x <= 1; x++) {
					Color color1 = etc1Pixel((int)r1, (int)g1, (int)b1, x, y, blockBottom, table1);
					Color color2 = etc1Pixel((int)r2, (int)g2, (int)b2, x + 2, y, blockBottom, table2);

					int offset1 = (y * 4 + x) * 4;
					output[offset1] = (byte) color1.getBlue();
					output[offset1 + 1] = (byte) color1.getGreen();
					output[offset1 + 2] = (byte) color1.getRed();

					int offset2 = (y * 4 + x + 2) * 4;
					output[offset2] = (byte) color1.getBlue();
					output[offset2 + 1] = (byte) color1.getGreen();
					output[offset2 + 2] = (byte) color1.getRed();
				}
			}
		} else {
			for (int y = 0; y <= 1; y++) {
				for (int x = 0; x <= 3; x++) {
					Color color1 = etc1Pixel((int)r1, (int)g1, (int)b1, x, y, blockBottom, table1);
					Color color2 = etc1Pixel((int)r2, (int)g2, (int)b2, x, y + 2, blockBottom, table2);

					int offset1 = (y * 4 + x) * 4;
					output[offset1] = (byte) color1.getBlue();
					output[offset1 + 1] = (byte) color1.getGreen();
					output[offset1 + 2] = (byte) color1.getRed();

					int offset2 = ((y + 2) * 4 + x) * 4;
					output[offset2] = (byte) color1.getBlue();
					output[offset2 + 1] = (byte) color1.getGreen();
					output[offset2 + 2] = (byte) color1.getRed();
				}
			}
		}
		return output;
	}

	private static Color etc1Pixel(int r, int g, int b, int x, int y, long block, long table) {
		int index = x * 4 + y;
		long MSB = block << 1;

		int pixel = (index < 8)
				? etc1LUT[(int)table][(int)(((block >> (index + 24)) & 1) + ((MSB >> (index + 8)) & 2))]
				: etc1LUT[(int)table][(int)(((block >> (index + 8)) & 1) + ((MSB >> (index - 8)) & 2))];
		
		r = saturate((int) (r + pixel));
		g = saturate((int) (g + pixel));
		b = saturate((int) (b + pixel));

		Color ret = new Color(r, g, b);
		return ret;
	}

	private static int saturate(int value) {
		if (value > 0xff) {
			return 0xff;
		}
		if (value < 0) {
			return 0;
		}
		return value & 0xff;
	}

	private static int[] etc1Scramble(int width, int height) {
		int[] tileScramble = new int[((width / 4) * (height / 4))];
		int baseAccumulator = 0;
		int rowAccumulator = 0;
		int baseNumber = 0;
		int rowNumber = 0;

		for (int tile = 0; tile < tileScramble.length; tile++) {
			if ((tile % (width / 4) == 0) && tile > 0) {
				if (rowAccumulator < 1) {
					rowAccumulator += 1;
					rowNumber += 2;
					baseNumber = rowNumber;
				} else {
					rowAccumulator = 0;
					baseNumber -= 2;
					rowNumber = baseNumber;
				}
			}

			tileScramble[tile] = baseNumber;

			if (baseAccumulator < 1) {
				baseAccumulator++;
				baseNumber++;
			} else {
				baseAccumulator = 0;
				baseNumber += 3;
			}
		}

		return tileScramble;
	}
}
