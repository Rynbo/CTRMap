package ctrmap.formats.h3d.texturing;

import ctrmap.formats.h3d.PICACommandReader;
import ctrmap.formats.h3d.RandomAccessBAIS;
import ctrmap.formats.h3d.StringUtils;
import java.awt.Dimension;
import java.io.IOException;

public class H3DTexture {
	
	public String textureName;
	public Dimension textureSize;
	public byte[] textureData;
	
	public H3DTexture(RandomAccessBAIS in, byte[] data) throws IOException{
		int dataOffset = in.readInt();
		in.seek(dataOffset);

		int texUnit0CommandsOffset = in.readInt();
		int texUnit0CommandsWordCount = in.readInt();
		int texUnit1CommandsOffset = in.readInt();
		int texUnit1CommandsWordCount = in.readInt();
		int texUnit2CommandsOffset = in.readInt();
		int texUnit2CommandsWordCount = in.readInt();
		in.readInt();
		int textureNameOffset = in.readInt();
		textureName = StringUtils.readString(textureNameOffset, data);

		in.seek(texUnit0CommandsOffset);
		PICACommandReader textureCommands = new PICACommandReader(in, texUnit0CommandsWordCount, false);

		in.seek(textureCommands.getTexUnit0Address());
		textureSize = textureCommands.getTexUnit0Size();
		byte[] buffer = new byte[textureSize.width * textureSize.height * 4];
		in.read(buffer);
		textureData = TextureCodec.decode(
				buffer,
				textureSize.width,
				textureSize.height,
				textureCommands.getTexUnit0Format()
		);
	}
}
