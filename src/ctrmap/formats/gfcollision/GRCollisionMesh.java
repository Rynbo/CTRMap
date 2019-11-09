package ctrmap.formats.gfcollision;

import java.io.IOException;

import com.jogamp.opengl.GL2;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.formats.Triangle;
import java.util.ArrayList;

public class GRCollisionMesh {

	public ArrayList<Triangle> tris = new ArrayList<>();

	public GRCollisionMesh(LittleEndianDataInputStream dis, int numVertexes) {
		try {
			//could also be a multidim array idk
			for (int i = 0; i < numVertexes / 3; i++) {
				float[] x = new float[3];
				float[] y = new float[3];
				float[] z = new float[3];
				for (int j = 0; j < 3; j++) {
					x[j] = dis.readFloat();
					y[j] = dis.readFloat();
					z[j] = dis.readFloat();
					dis.skip(4);
				}
				tris.add(new Triangle(x, y, z));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public GRCollisionMesh() {
	}

	public void write(LittleEndianDataOutputStream dos) {
		for (int i = 0; i < tris.size(); i++) {
			tris.get(i).write(dos);
		}
	}

	public void render(GL2 gl) {
		for (int i = 0; i < tris.size(); i++) {
			tris.get(i).render(gl);
		}
	}
}
