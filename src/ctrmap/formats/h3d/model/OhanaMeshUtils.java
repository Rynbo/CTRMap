package ctrmap.formats.h3d.model;

import ctrmap.formats.h3d.RandomAccessBAIS;
import ctrmap.formats.vectors.Vec3f;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OhanaMeshUtils {

	public static void calculateBounds(H3DModel mdl, H3DVertex vertex) {
		if (vertex.position.x < mdl.minVector.x) {
			mdl.minVector.x = vertex.position.x;
		}
		if (vertex.position.x > mdl.maxVector.x) {
			mdl.maxVector.x = vertex.position.x;
		}
		if (vertex.position.y < mdl.minVector.y) {
			mdl.minVector.y = vertex.position.y;
		}
		if (vertex.position.y > mdl.maxVector.y) {
			mdl.maxVector.y = vertex.position.y;
		}
		if (vertex.position.z < mdl.minVector.z) {
			mdl.minVector.z = vertex.position.z;
		}
		if (vertex.position.z > mdl.maxVector.z) {
			mdl.maxVector.z = vertex.position.z;
		}
	}
	
	public static void makeBox(float[][] boxVectors, Vec3f minVector, Vec3f maxVector){
		boxVectors[0] = new float[]{minVector.x, minVector.y, minVector.z};
		boxVectors[1] = new float[]{minVector.x, maxVector.y, minVector.z};
		boxVectors[2] = new float[]{minVector.x, maxVector.y, minVector.z};
		boxVectors[3] = new float[]{maxVector.x, maxVector.y, minVector.z};
		boxVectors[4] = new float[]{maxVector.x, maxVector.y, minVector.z};
		boxVectors[5] = new float[]{maxVector.x, minVector.y, minVector.z};
		boxVectors[6] = new float[]{maxVector.x, minVector.y, minVector.z};
		boxVectors[7] = new float[]{minVector.x, minVector.y, minVector.z};
		
		boxVectors[8] = new float[]{minVector.x, minVector.y, maxVector.z};
		boxVectors[9] = new float[]{minVector.x, maxVector.y, maxVector.z};
		boxVectors[10] = new float[]{minVector.x, maxVector.y, maxVector.z};
		boxVectors[11] = new float[]{maxVector.x, maxVector.y, maxVector.z};
		boxVectors[12] = new float[]{maxVector.x, maxVector.y, maxVector.z};
		boxVectors[13] = new float[]{maxVector.x, minVector.y, maxVector.z};
		boxVectors[14] = new float[]{maxVector.x, minVector.y, maxVector.z};
		boxVectors[15] = new float[]{minVector.x, minVector.y, maxVector.z};
		
		boxVectors[16] = new float[]{minVector.x, minVector.y, minVector.z};
		boxVectors[17] = new float[]{minVector.x, minVector.y, maxVector.z};
		boxVectors[18] = new float[]{minVector.x, maxVector.y, minVector.z};
		boxVectors[19] = new float[]{minVector.x, maxVector.y, maxVector.z};
		boxVectors[20] = new float[]{maxVector.x, maxVector.y, minVector.z};
		boxVectors[21] = new float[]{maxVector.x, maxVector.y, maxVector.z};
		boxVectors[23] = new float[]{maxVector.x, minVector.y, maxVector.z};
		boxVectors[22] = new float[]{maxVector.x, minVector.y, minVector.z};
		
		//we make extra unnecessary vectors for better programmatic checking for boxes when picking objects
		
		boxVectors[24] = new float[]{minVector.x, minVector.y, minVector.z};
		boxVectors[25] = new float[]{maxVector.x, minVector.y, minVector.z};
		boxVectors[26] = new float[]{maxVector.x, minVector.y, maxVector.z};
		boxVectors[27] = new float[]{minVector.x, minVector.y, maxVector.z};
		boxVectors[28] = new float[]{minVector.x, maxVector.y, minVector.z};
		boxVectors[29] = new float[]{maxVector.x, maxVector.y, minVector.z};
		boxVectors[30] = new float[]{maxVector.x, maxVector.y, maxVector.z};
		boxVectors[31] = new float[]{minVector.x, maxVector.y, maxVector.z};
	}

	public static Color getColor(RandomAccessBAIS input) throws IOException{
		int r = input.read();
		int g = input.read();
		int b = input.read();
		int a = input.read();

		return new Color(r, g, b, a);
	}

	public static Color getColorFloat(RandomAccessBAIS input) throws IOException{
		byte r = (byte) (input.readFloat() * 0xff);
		byte g = (byte) (input.readFloat() * 0xff);
		byte b = (byte) (input.readFloat() * 0xff);
		byte a = (byte) (input.readFloat() * 0xff);

		return new Color(r, g, b, a);
	}

	/// <summary>
	///     Clamps a Float value between 0 and 255 and return as Byte.
	/// </summary>
	/// <param name="value">The float value</param>
	/// <returns></returns>
	public static int saturate(float value) {
		if (value > 255f) {
			return 0xff;
		}
		if (value < 0f) {
			return 0;
		}
		return (int)value;
	}

	public static final int optimizerLookBack = 32;

	public class optimizedMesh {

		public List<H3DVertex> vertices = new ArrayList<>();
		public List<Integer> indices = new ArrayList<>();

		public boolean hasNormal;
		public boolean hasTangent;
		public boolean hasColor;
		public boolean hasNode;
		public boolean hasWeight;
		public int texUVCount;
	}
	/*
	public static optimizedMesh optimizeMesh(H3DMesh mesh) {
		optimizedMesh output = new optimizedMesh();

		output.hasNormal = mesh.hasNormal;
		output.hasTangent = mesh.hasTangent;
		output.hasColor = mesh.hasColor;
		output.hasNode = mesh.hasNode;
		output.hasWeight = mesh.hasWeight;
		output.texUVCount = mesh.texUVCount;

		for (int i = 0; i < mesh.vertices.Count; i++) {
			bool found = false;
			for (int j = 1; j <= optimizerLookBack; j++) {
				int p = output.vertices.Count - j;
				if (p < 0 || p >= output.vertices.Count) {
					break;
				}
				if (output.vertices[p].Equals(mesh.vertices[i])) {
					output.indices.Add((uint) p);
					found = true;
					break;
				}
			}

			if (!found) {
				output.vertices.Add(mesh.vertices[i]);
				output.indices.Add((uint) (output.vertices.Count - 1));
			}
		}

		return output;
	}

	public static uint getOptimizedVertCount(List<RenderBase.OMesh> om) {
		uint cnt = 0;
		foreach(var v in om
		
			) {
                cnt += (uint) optimizeMesh(v).vertices.Count;
		}
		return cnt;
	}*/
}
