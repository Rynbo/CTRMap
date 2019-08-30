package ctrmap.formats.tilemap;
import ctrmap.formats.containers.GR;
import ctrmap.CtrmapMainframe;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ctrmap.LittleEndianDataOutputStream;
import ctrmap.Utils;

public class Tilemap {
	public GR mapFile;
	public byte[][][] rawTileData;
	public boolean modified;
	private BufferedImage tilemapImage;
	private Graphics g;
	private short width;
	private short height;
	
	public Tilemap(GR mapFile) {
		this.mapFile = mapFile;
		rawTileData = getTileData();
		tilemapImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
		g = tilemapImage.getGraphics();
		updateImage();
	}
	private byte[][][] getTileData(){
		try {
			InputStream in = new ByteArrayInputStream(mapFile.getFile(0));
			width = Short.reverseBytes((short)((in.read() << 8) | in.read()));
			height = Short.reverseBytes((short)((in.read() << 8) | in.read()));
			byte[][][] b = new byte[width][height][4];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					in.read(b[x][y]);
				}
			}
			in.close();
			return b;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public byte[] assembleTilemap() {
		//GF likes their files to start at offsets ending with either 00 or 80
		//even though we can disrespect that and the games run just fine, it's prettier and easier for debugging
		//so we fill the remaining data till that offset with zeros. the tile data starts at 0x80 but we'll rather do it programatically.
		int startingOffset = mapFile.getOffset(0);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(baos);
		try {
			dos.writeShort(width);
			dos.writeShort(height);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					dos.write(getTileData(x, y));
				}
			}
			int written = dos.size();
			dos.write(Utils.getPadding(startingOffset, written));
			dos.flush();
			dos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public void updateImage() {
		for (int x = 0; x < 40; x++) {
			for (int y = 0; y < 40; y++) {
				g.setColor(CtrmapMainframe.mTileEditForm.tileset.getTemplate(Utils.ba2int(rawTileData[x][y])).color);
				g.fillRect(x*10, y*10, 10, 10);
			}
		}
	}
	public BufferedImage getImage(){
		return tilemapImage;
	}
	public void setTileData(int x, int y, byte[] tiledata) {
		modified = true;
		rawTileData[x][y] = tiledata.clone();
	}
	public byte[] getTileData(int x, int y) {
		return rawTileData[x][y];
	}
}