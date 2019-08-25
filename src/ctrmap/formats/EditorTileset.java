package ctrmap.formats;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditorTileset {

	public TileTemplate[] tiles;

	public static final int METS_MAGIC = 0x4D455453;
	
	public EditorTileset(File f) {
		try {
			load(new FileInputStream(f));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(EditorTileset.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public EditorTileset(InputStream in){
		load(in);
	}
	
	public final void load(InputStream in){
		try {
			DataInputStream dis = new DataInputStream(in);
			int magic = dis.readInt();
			if (magic != METS_MAGIC){
				return;
			}
			int length = dis.readInt();
			tiles = new TileTemplate[length];
			for (int i = 0; i < length; i++) {
				int strlen = dis.read();
				byte[] strbuf = new byte[strlen];
				dis.read(strbuf);
				String name = new String(strbuf, "UTF-8");
				int binary = dis.readInt();
				int color = dis.readInt();
				int cat1 = dis.read();
				int cat2 = dis.read();
				tiles[i] = new TileTemplate(binary, color, name, cat1, cat2);
			}
		} catch (IOException ex) {
			Logger.getLogger(EditorTileset.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public TileTemplate getTemplate(int binary) {
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i].binary == binary) {
				return tiles[i];
			}
		}
		return new TileTemplate(binary, 0xFF0000, "Unknown", 0, 0);
	}

	public TileTemplate getTemplate(int cat1, int cat2, String name) {
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i].name.equals(name) && tiles[i].cat1 == cat1 && tiles[i].cat2 == cat2) {
				return tiles[i];
			}
		}
		return null;
	}
}
