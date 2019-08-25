package ctrmap.formats;

import ctrmap.Utils;
import ctrmap.formats.gfcollision.GRCollisionMesh;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WavefrontOBJ {
	private ArrayList<OBJMesh> meshes = new ArrayList<>();
	public WavefrontOBJ(File f){
		BufferedReader src = null;
		try {
			src = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line;
			ArrayList<float[]> vertices = new ArrayList<>();
			int meshIndex = 0;
			while ((line = src.readLine()) != null){
				String[] read = line.split("\\s+");
				String command = read[0];
				switch(command){
					case "o":
					case "g":
						meshes.add(new OBJMesh(read[1]));
						meshIndex = meshes.size() - 1;
						break;
					case "v":
						vertices.add(new float[]{Float.parseFloat(read[1]), Float.parseFloat(read[2]), Float.parseFloat(read[3])});
						break;
					case "f":
						//pay respects
						if (read.length == 4){
							float[] v0 = vertices.get(Integer.valueOf(read[1].split("/")[0]) - 1);
							float[] v1 = vertices.get(Integer.valueOf(read[2].split("/")[0]) - 1);
							float[] v2 = vertices.get(Integer.valueOf(read[3].split("/")[0]) - 1);
							//calculate 2D area of triangle of grounded vertices, if 0, it's a wall, so we remove it
							if (!Utils.impreciseFloatEquals(v0[0] * (v1[2] - v2[2]) +  v1[0] * (v2[2] - v0[2]) +  v2[0] * (v0[2] - v1[2]), 0f)){
								meshes.get(meshIndex).addTri(v0, v1, v2);
							}
						}
						else if (read.length == 5){
							float[] v0 = vertices.get(Integer.valueOf(read[1].split("/")[0]) - 1);
							float[] v1 = vertices.get(Integer.valueOf(read[2].split("/")[0]) - 1);
							float[] v2 = vertices.get(Integer.valueOf(read[3].split("/")[0]) - 1);
							float[] v3 = vertices.get(Integer.valueOf(read[4].split("/")[0]) - 1);
							if (!Utils.impreciseFloatEquals(v0[0] * (v1[2] - v2[2]) +  v1[0] * (v2[2] - v0[2]) +  v2[0] * (v0[2] - v1[2]), 0f)){
								meshes.get(meshIndex).addTri(v0, v1, v1);
							}
							if (!Utils.impreciseFloatEquals(v0[0] * (v3[2] - v2[2]) +  v3[0] * (v2[2] - v0[2]) +  v2[0] * (v0[2] - v3[2]), 0f)){
								meshes.get(meshIndex).addTri(v0, v3, v2);
							}
						}
						break;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	public GRCollisionMesh[] getGfCollision(){
		GRCollisionMesh[] ret = new GRCollisionMesh[16];
		for (int i = 0; i < 16; i++){
			ret[i] = new GRCollisionMesh();
		}
		int count = 0;
		for (int i = 0, j = 0; i < meshes.size(); i++, j++){
			ret[j].tris.addAll(meshes.get(i).tris);
			if (j == 15) j = -1;
		}
		return ret;
	}
}
class OBJMesh{
	public ArrayList<Triangle> tris = new ArrayList<>();
	public String name;
	public OBJMesh(String name){
		this.name = name;
	}
	public void addTri(float[] v0, float[] v1, float[] v2){
		tris.add(new Triangle(new float[]{v0[0], v1[0], v2[0]}, new float[]{v0[1], v1[1], v2[1]}, new float[]{v0[2], v1[2], v2[2]}));
	}
}
