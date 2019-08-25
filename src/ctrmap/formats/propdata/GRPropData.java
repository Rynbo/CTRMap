
package ctrmap.formats.propdata;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.formats.GR;
import ctrmap.formats.mapmatrix.MapMatrix;
import ctrmap.Utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Info about the placement of world props, stored in the fourth section (after collision) in GR files.
 * Note that the prop models have to be registered in the corresponding AreaData's first section as well
 * as have their required textures available in the location's AD BCH file (TODO - injecting textures).
 */
public class GRPropData {
	public ArrayList<GRProp> props = new ArrayList<>();
	public boolean modified = false;
	public GR f;
	public GRPropData(GR gr){
		try {
			f = gr;
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(f.getFile(3)));
			int length = dis.readInt();
			for (int i = 0; i < length; i++){
				props.add(new GRProp(dis));
			}
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(GRPropData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	public GRPropData(){}
	public void write(){
		if (f == null) return;
		f.storeFile(3, assemblePropData());
	}
	public void write(MapMatrix mm){
		GRPropData[][] newData = new GRPropData[mm.width][mm.height];
		for (int i = 0; i < mm.height; i++){
			for (int j = 0; j < mm.width; j++){
				if (mm.regions[j][i] != null){
					newData[j][i] = new GRPropData();
				}
			}
		}
		for (int i = 0; i < props.size(); i++){
			GRProp prop = props.get(i);
			int horizontalRegion = (int)prop.x/720;
			int verticalRegion = (int)prop.z/720;
			if (mm.regions[horizontalRegion][verticalRegion] != null){
				newData[horizontalRegion][verticalRegion].props.add(prop);
			}
			else {
				int limit = Math.max(Math.abs(mm.width/2 + horizontalRegion), Math.abs(mm.height/2 + verticalRegion));
				scanLoop:
				for (int expand = 0; expand < limit; expand++){
					for (int y = verticalRegion - 1 - expand; y < verticalRegion + 1 + expand; y++){
						if (y < 0 || y >= mm.height)
						for (int x = horizontalRegion - 1 - expand; x < horizontalRegion + 1 + expand; x++){
							if (x < 0 || x >= mm.width) continue;
							if (mm.regions[x][y] != null){
								newData[x][y].props.add(prop);
								break scanLoop;
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < mm.height; i++){
			for (int j = 0; j < mm.width; j++){
				if (mm.regions[j][i] != null){
					byte[] data = newData[j][i].assemblePropData();
					byte[] padding = Utils.getPadding(mm.regions[j][i].getOffset(3), data.length);
					byte[] output = new byte[data.length + padding.length];
					System.arraycopy(data, 0, output, 0, data.length);
					System.arraycopy(padding, 0, output, data.length, padding.length);
					mm.regions[j][i].storeFile(3, output);
				}
			}
		}
	}
	public byte[] assemblePropData(){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(baos);
			dos.writeInt(props.size());
			for (int i = 0; i < props.size(); i++){
				props.get(i).write(dos);
			}
			dos.close();
			return baos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(GRPropData.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
}
