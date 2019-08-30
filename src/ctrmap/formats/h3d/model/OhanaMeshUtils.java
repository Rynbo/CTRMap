package ctrmap.formats.h3d.model;

import ctrmap.formats.h3d.RandomAccessBAIS;
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
		public List<Integer> indices = new ArrayList<Integer>();

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
