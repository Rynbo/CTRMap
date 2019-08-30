package ctrmap.formats.h3d;

import com.jogamp.opengl.GL2;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.texturing.H3DMaterial;
import ctrmap.formats.h3d.texturing.H3DTexture;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Based on gdkchan's Ohana3DS and Rebirth, mostly a copy-paste
 */
public class BCHFile {

	private byte[] buf;

	public BCHHeader header;
	public BCHContentHeader contentHeader;
	public ArrayList<H3DModel> models = new ArrayList<>();
	public ArrayList<H3DTexture> textures = new ArrayList<>();

	public BCHFile(byte[] src) {
		try {
			buf = src.clone();
			RandomAccessBAIS in = new RandomAccessBAIS(new ByteArrayInputStream(buf));
			header = new BCHHeader();
			header.magic = StringUtils.readString(in);
			if (!header.magic.equals("BCH")) {
				System.err.println("BCH magic mismatch! - " + src[0] + src[1] + src[2]);
			}
			header.backwardCompatibility = in.readByte();
			header.forwardCompatibility = in.readByte();
			header.version = in.readShort();

			header.mainHeaderOffset = in.readInt();
			header.stringTableOffset = in.readInt();
			header.gpuCommandsOffset = in.readInt();
			header.dataOffset = in.readInt();
			if (header.backwardCompatibility > 0x20) {
				header.dataExtendedOffset = in.readInt();
			}
			header.relocationTableOffset = in.readInt();

			header.mainHeaderLength = in.readInt();
			header.stringTableLength = in.readInt();
			header.gpuCommandsLength = in.readInt();
			header.dataLength = in.readInt();
			if (header.backwardCompatibility > 0x20) {
				header.dataExtendedLength = in.readInt();
			}
			header.relocationTableLength = in.readInt();

			header.uninitializedDataSectionLength = in.readInt();
			header.uninitializedDescriptionSectionLength = in.readInt();

			if (header.backwardCompatibility > 7) {
				header.flags = in.readShort();
				header.addressCount = in.readShort();
			}

			//Normally we would parse the content header now. But first, we need to absolutize the offsets for it not to be a living hell.
			for (int o = header.relocationTableOffset; o < header.relocationTableOffset + header.relocationTableLength; o += 4) {
				in.seek(o);
				int value = in.readInt();
				int offset = value & 0x1ffffff;
				byte flags = (byte) (value >> 25);
				int seek = 0;

				switch (flags) {
					case 0:
						seek = (offset * 4) + header.mainHeaderOffset;
						relocateOffset(seek, header.mainHeaderOffset, buf);
						break;

					case 1:
						seek = offset + header.mainHeaderOffset;
						relocateOffset(seek, header.stringTableOffset, buf);
						break;

					case 2:
						seek = (offset * 4) + header.mainHeaderOffset;
						relocateOffset(seek, header.gpuCommandsOffset, buf);
						break;

					case 7:
					case 0xc:
						seek = (offset * 4) + header.mainHeaderOffset;
						relocateOffset(seek, header.dataOffset, buf);
						break;
				}
				seek = (offset * 4) + header.gpuCommandsOffset;
				if (header.backwardCompatibility < 6) {
					switch (flags) {
						case 0x23:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Texture
						case 0x25:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Vertex
						case 0x26:
							changeValueTo(seek, ((peek(seek, buf) + header.dataOffset) & 0x7fffffff) | 0x80000000, buf);
							break; //Index 16 bits mode
						case 0x27:
							changeValueTo(seek, (peek(seek, buf) + header.dataOffset) & 0x7fffffff, buf);
							break; //Index 8 bits mode
					}
				} else if (header.backwardCompatibility < 8) {
					switch (flags) {
						case 0x24:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Texture
						case 0x26:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Vertex
						case 0x27:
							changeValueTo(seek, ((peek(seek, buf) + header.dataOffset) & 0x7fffffff) | 0x80000000, buf);
							break; //Index 16 bits mode
						case 0x28:
							changeValueTo(seek, (peek(seek, buf) + header.dataOffset) & 0x7fffffff, buf);
							break; //Index 8 bits mode
					}
				} else if (header.backwardCompatibility < 0x21) {
					switch (flags) {
						case 0x25:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Texture
						case 0x27:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Vertex
						case 0x28:
							changeValueTo(seek, ((peek(seek, buf) + header.dataOffset) & 0x7fffffff) | 0x80000000, buf);
							break; //Index 16 bits mode
						case 0x29:
							changeValueTo(seek, (peek(seek, buf) + header.dataOffset) & 0x7fffffff, buf);
							break; //Index 8 bits mode
					}
				} else {
					switch (flags) {
						case 0x25:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Texture
						case 0x26:
							relocateOffset(seek, header.dataOffset, buf);
							break; //Vertex relative to Data Offset
						case 0x27:
							changeValueTo(seek, ((peek(seek, buf) + header.dataOffset) & 0x7fffffff) | 0x80000000, buf);
							break; //Index 16 bits mode relative to Data Offset
						case 0x28:
							changeValueTo(seek, (peek(seek, buf) + header.dataOffset) & 0x7fffffff, buf);
							break; //Index 8 bits mode relative to Data Offset
						case 0x2b:
							relocateOffset(seek, header.dataExtendedOffset, buf);
							break; //Vertex relative to Data Extended Offset
						case 0x2c:
							changeValueTo(seek, ((peek(seek, buf) + header.dataExtendedOffset) & 0x7fffffff) | 0x80000000, buf);
							break; //Index 16 bits mode relative to Data Extended Offset
						case 0x2d:
							changeValueTo(seek, (peek(seek, buf) + header.dataExtendedOffset) & 0x7fffffff, buf);
							break; //Index 8 bits mode relative to Data Extended Offset
					}
				}
			}
			in.seek(header.mainHeaderOffset);
			contentHeader = new BCHContentHeader();
			contentHeader.modelsPointerTableOffset = in.readInt();
			contentHeader.modelsPointerTableEntries = in.readInt();
			contentHeader.modelsNameOffset = in.readInt();
			contentHeader.materialsPointerTableOffset = in.readInt();
			contentHeader.materialsPointerTableEntries = in.readInt();
			contentHeader.materialsNameOffset = in.readInt();
			contentHeader.shadersPointerTableOffset = in.readInt();
			contentHeader.shadersPointerTableEntries = in.readInt();
			contentHeader.shadersNameOffset = in.readInt();
			contentHeader.texturesPointerTableOffset = in.readInt();
			contentHeader.texturesPointerTableEntries = in.readInt();
			contentHeader.texturesNameOffset = in.readInt();
			contentHeader.materialsLUTPointerTableOffset = in.readInt();
			contentHeader.materialsLUTPointerTableEntries = in.readInt();
			contentHeader.materialsLUTNameOffset = in.readInt();
			contentHeader.lightsPointerTableOffset = in.readInt();
			contentHeader.lightsPointerTableEntries = in.readInt();
			contentHeader.lightsNameOffset = in.readInt();
			contentHeader.camerasPointerTableOffset = in.readInt();
			contentHeader.camerasPointerTableEntries = in.readInt();
			contentHeader.camerasNameOffset = in.readInt();
			contentHeader.fogsPointerTableOffset = in.readInt();
			contentHeader.fogsPointerTableEntries = in.readInt();
			contentHeader.fogsNameOffset = in.readInt();
			contentHeader.skeletalAnimationsPointerTableOffset = in.readInt();
			contentHeader.skeletalAnimationsPointerTableEntries = in.readInt();
			contentHeader.skeletalAnimationsNameOffset = in.readInt();
			contentHeader.materialAnimationsPointerTableOffset = in.readInt();
			contentHeader.materialAnimationsPointerTableEntries = in.readInt();
			contentHeader.materialAnimationsNameOffset = in.readInt();
			contentHeader.visibilityAnimationsPointerTableOffset = in.readInt();
			contentHeader.visibilityAnimationsPointerTableEntries = in.readInt();
			contentHeader.visibilityAnimationsNameOffset = in.readInt();
			contentHeader.lightAnimationsPointerTableOffset = in.readInt();
			contentHeader.lightAnimationsPointerTableEntries = in.readInt();
			contentHeader.lightAnimationsNameOffset = in.readInt();
			contentHeader.cameraAnimationsPointerTableOffset = in.readInt();
			contentHeader.cameraAnimationsPointerTableEntries = in.readInt();
			contentHeader.cameraAnimationsNameOffset = in.readInt();
			contentHeader.fogAnimationsPointerTableOffset = in.readInt();
			contentHeader.fogAnimationsPointerTableEntries = in.readInt();
			contentHeader.fogAnimationsNameOffset = in.readInt();
			contentHeader.scenePointerTableOffset = in.readInt();
			contentHeader.scenePointerTableEntries = in.readInt();
			contentHeader.sceneNameOffset = in.readInt();

			//Textures
			for (int textureIndex = 0; textureIndex < contentHeader.texturesPointerTableEntries; textureIndex++) {
				in.seek(contentHeader.texturesPointerTableOffset + (textureIndex * 4));
				textures.add(new H3DTexture(in, buf));
			}

			//Models
			for (int modelIndex = 0; modelIndex < contentHeader.modelsPointerTableEntries; modelIndex++) {
				in.seek(contentHeader.modelsPointerTableOffset + (modelIndex * 4));
				H3DModel model = new H3DModel(in, buf, header);
				//Materials
				for (int index = 0; index < model.modelHeader.materialsTableEntries; index++) {
					if (header.backwardCompatibility < 0x21) {
						in.seek(model.modelHeader.materialsTableOffset + (index * 0x58));
					} else {
						in.seek(model.modelHeader.materialsTableOffset + (index * 0x2c));
					}
					model.materials.add(new H3DMaterial(in, buf, header));
				}
				models.add(model);
			}
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(BCHFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void render(GL2 gl) {
		for (int i = 0; i < models.size(); i++) {
			models.get(i).render(gl);
		}
	}

	public static int peek(int offset, byte[] b) {
		return ByteBuffer.wrap(b, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	public static void relocateOffset(int offsetOfTheOffset, int relocationMod, byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b, offsetOfTheOffset, 4);
		int originalOffset = Integer.reverseBytes(buf.getInt());
		buf.putInt(0, Integer.reverseBytes(originalOffset + relocationMod));
		buf.rewind();
		byte[] ret = new byte[4];
		buf.get(ret);
		System.arraycopy(ret, 0, b, offsetOfTheOffset, 4);
	}

	public static void changeValueTo(int offset, int newValue, byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b, offset, 4);
		buf.putInt(0, Integer.reverseBytes(newValue));
		buf.rewind();
		byte[] ret = new byte[4];
		buf.get(ret);
		System.arraycopy(ret, 0, b, offset, 4);
	}
}
