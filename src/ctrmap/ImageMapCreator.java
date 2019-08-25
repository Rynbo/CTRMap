package ctrmap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageMapCreator {
	private BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	public void createImage(int[][] grdata, String filename) throws IOException {
		Graphics g = img.getGraphics();
		for (int x = 0; x < 40; x++) {
			for (int y = 0; y < 40; y++) {
				g.setColor(new Color(getColor(grdata[x][y])));
				g.fillRect(x*10, y*10, 10, 10);
				byte ori = getOrientation(grdata[x][y]);
				if (ori != -1) {
					drawNSWE(g.getColor(), ori, g, x*10, y*10);
				}
			}
		}
		ImageIO.write(img, "png", new File(filename + ".png"));
	}
	private byte getOrientation(int tiledata) {
		int tens = (int)(Math.floor(tiledata/10d));
		if (tens >= 5) {
			return (byte)(tiledata - tens*10);
		}
		return -1;
	}
	public int getColor(int tiledata) {
		switch(tiledata) {
			case 0x0:
				return 0xff0000;
			case 0x1:
				return 0x00cc00;
			case 0x2:
				return 0x00ff00;
			case 0x3:
				return 0x0000cc;
			case 0x4:
				return 0x00ccff;
			case 0x5:
				return 0x33ccff;
			case 0x6:
				return 0x0099ff;
			case 0x7:
				return 0x00ffff;
			case 10:
				return 0x99ff33;
			case 11:
				return 0xffff00;
			case 12:
				return 0xff33cc;
			case 14:
				return 0x663300;
			case 20:
				return 0xcc6600;
			case 21:
				return 0xccffff;
			case 22:
				return 0xffffff;
			case 23:
				return 0x00ffcc;
			case 24:
				return 0x0000ff;
			case 25:
				return 0xffffcc;
			case 26:
				return 0x99ccff;
			case 27:
				return 0xff3300;
			case 28:
				return 0x3399ff;
			case 29:
				return 0x666699;
			case 30:
				return 0x1f2e2e;
			case 31:
				return 0xff0066;
			case 32:
				return 0x66ffff;
			case 33:
				return 0xffffff;
			default:
				switch ((int)Math.floor((double)tiledata/10d)) {
					case 5:
						return 0x663300;
					case 6:
						return 0xcc9900;
					case 7:
						return 0x66ccff;
					case 8:
						return 0x00ff00;
				}
				return 0x00cc00;
		}
	}
	private void drawNSWE(Color normalcol, byte raworientation, Graphics g, int x, int y) {
		g.setColor(normalcol.darker());
		switch(raworientation) {
			case 1:
				g.drawLine(x, y, x + 9, y);
				break;
			case 2:
				g.drawLine(x, y + 9, x + 9, y + 9);
				break;
			case 3:
				g.drawLine(x, y, x, y + 9);
				break;
			case 4:
				g.drawLine(x + 9, y, x + 9, y + 9);
				break;
		}
	}
}
