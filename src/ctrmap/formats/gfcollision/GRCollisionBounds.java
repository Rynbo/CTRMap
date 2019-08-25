package ctrmap.formats.gfcollision;

import java.io.IOException;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;

public class GRCollisionBounds {

	public BoundStructure[] structs = new BoundStructure[10];
	public float[] originalExtremes;

	public GRCollisionBounds(LittleEndianDataInputStream dis) {
		for (int i = 0; i < 10; i++) {
			structs[i] = new BoundStructure(dis);
		}
	}

	public void updateBounds(GRCollisionFile f) {
		float[] extremes = f.getMeshExtremes();
		//check changes
		originalExtremes = new float[6];
		//the bounds are always two squares above one another, so if X changes, Z does as well
		//and vice versa
		float adjustMaxX = extremes[1];
		float adjustMinX = extremes[0];
		float adjustCenterX = (adjustMaxX + adjustMinX) / 2f;
		float adjustMaxZ = extremes[5];
		float adjustMinZ = extremes[4];
		float adjustCenterZ = (adjustMaxZ + adjustMinZ) / 2f;
		//make the main stuff
		//mc > cm exch. x
		//min cc max > min cc max exch. z
		/*structs[0].x[0] = adjustMin;
                structs[0].z[0] = adjustMin;
                structs[0].x[1] = adjustCenter;
                structs[0].z[1] = adjustCenter;
                structs[0].x[2] = adjustMin;
                structs[0].z[2] = adjustCenter;
                structs[0].x[3] = adjustCenter;
                structs[0].z[3] = adjustMax;
                structs[1].x[0] = adjustCenter;
                structs[1].z[0] = adjustMin;
                structs[1].x[1] = adjustMax;
                structs[1].z[1] = adjustCenter;
                structs[1].x[2] = adjustCenter;
                structs[1].z[2] = adjustCenter;
                structs[1].x[3] = adjustMax;
                structs[1].z[3] = adjustMax;*/
		//for doc purposes, it goes like this (two reverse harry potter scars). for better performance, we'll do some math.
		float[] mccmZ = new float[]{adjustMinZ, adjustCenterZ, adjustCenterZ, adjustMaxZ};
		float multiplier = 1.0f; //gets set to 0.25 after first cycle of the next for loop for the quarterbounds
		float baseX = 0f;
		float baseZ = 0f;
		for (int j = 0; j < 10; j += 2) {
			for (int i = 0; i < 4; i++) {
				structs[j].x[i] = baseX + multiplier * (((i & 1) == 0) ? adjustMinX : adjustCenterX);
				structs[j + 1].x[i] = baseX + multiplier * (((i & 1) == 0) ? adjustCenterX : adjustMaxX);
				structs[j].z[i] = baseZ + multiplier * mccmZ[i];
				structs[j + 1].z[i] = baseZ + multiplier * mccmZ[i];
				/*structs[j].x[i] = (float)Math.round(structs[j].x[i]);
                                structs[j+1].x[i] = (float)Math.round(structs[j+1].x[i]);
                                structs[j].z[i] = (float)Math.round(structs[j].z[i]);
                                structs[j+1].z[i] = (float)Math.round(structs[j+1].z[i]);*/
			}
			if (j == 0) {
				multiplier = 0.5f;
				baseX = 0.5f * adjustMinX;
				baseZ = 0.5f * adjustMinZ;
			} else if (j == 2) {
				baseZ = 0.5f * adjustMaxZ;
			} else if (j == 4) {
				baseX = 0.5f * adjustMaxX;
				baseZ = 0.5f * adjustMinZ;
			} else if (j == 6) {
				baseZ = 0.5f * adjustMaxZ;
			}
		}
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 4; j++) {
				if ((j & 1) == 0) { //num is even
					structs[i].y[j] = extremes[2];
				} else {
					structs[i].y[j] = extremes[3];
				}
			}
		}
	}

	public void write(LittleEndianDataOutputStream dos) {
		for (int i = 0; i < 10; i++) {
			structs[i].write(dos);
		}
	}

	public void debugSysout() {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.println("Struct " + i + ", vertex " + j + ": X=" + structs[i].x[j] + " Y=" + structs[i].y[j] + " Z=" + structs[i].z[j]);
			}
		}
	}
}

class BoundStructure {

	public float[] x = new float[4];
	public float[] y = new float[4];
	public float[] z = new float[4];

	public BoundStructure(LittleEndianDataInputStream dis) {
		try {
			for (int i = 0; i < 4; i++) {
				x[i] = dis.readFloat();
				y[i] = dis.readFloat();
				z[i] = dis.readFloat();
				dis.skip(4);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(LittleEndianDataOutputStream dos) {
		try {
			for (int i = 0; i < 4; i++) {
				dos.writeFloat(x[i]);
				dos.writeFloat(y[i]);
				dos.writeFloat(z[i]);
				dos.writeFloat(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
