package ctrmap.formats;

import com.jogamp.opengl.GL2;
import com.sun.javafx.geom.Vec3f;
import ctrmap.formats.h3d.model.OhanaMeshUtils;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * CTRMap Vertex Data, a lightweight format to store colored vertices. 
 * Supports only up to 65535 vertices as the
 * current scope of its usage does not need more than that.
 */
public class CMVD {

	public static final int CMVD_MAGIC = 0x434D5644;

	public List<CMVDVertex> vertices;
	public List<int[]> faces;

	public float worldLocX = 0f;
	public float worldLocY = 0f;
	public float worldLocZ = 0f;
	
	public float scale = 1f;

	public float[][] boxVectors = new float[32][3];
	
	public Vec3f minVector = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	public Vec3f maxVector = new Vec3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

	public CMVD() {
		vertices = new ArrayList<>();
		faces = new ArrayList<>();
	}

	public void write(OutputStream out) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);
		dos.writeInt(CMVD_MAGIC);
		dos.writeShort(vertices.size());
		dos.writeShort(faces.size());
		for (int vertex = 0; vertex < vertices.size(); vertex++) {
			vertices.get(vertex).write(dos);
		}
		for (int face = 0; face < faces.size(); face++) {
			int[] f = faces.get(face);
			dos.writeShort(f[0]);
			dos.writeShort(f[1]);
			dos.writeShort(f[2]);
		}
		dos.close();
	}

	public CMVD(InputStream in) throws IOException {
		vertices = new ArrayList<>();
		faces = new ArrayList<>();
		DataInputStream dis = new DataInputStream(in);
		int magic = dis.readInt();
		if (magic != CMVD_MAGIC) {
			System.err.println("CMVD magic invalid.");
			return;
		}
		int verticesCount = dis.readUnsignedShort();
		int facesCount = dis.readUnsignedShort();
		for (int vertex = 0; vertex < verticesCount; vertex++) {
			CMVDVertex v = new CMVDVertex(dis);
			if (v.x < minVector.x) {
				minVector.x = v.x;
			}
			if (v.x > maxVector.x) {
				maxVector.x = v.x;
			}
			if (v.y < minVector.y) {
				minVector.y = v.y;
			}
			if (v.y > maxVector.y) {
				maxVector.y = v.y;
			}
			if (v.z < minVector.z) {
				minVector.z = v.z;
			}
			if (v.z > maxVector.z) {
				maxVector.z = v.z;
			}
			vertices.add(v);
		}
		for (int face = 0; face < facesCount; face++) {
			faces.add(new int[]{dis.readUnsignedShort(), dis.readUnsignedShort(), dis.readUnsignedShort()});
		}
		dis.close();
		makeBox();
	}

	public void makeBox(){
		OhanaMeshUtils.makeBox(boxVectors, minVector, maxVector);
	}
	
	public void render(GL2 gl) {
		gl.glPushMatrix();
		gl.glTranslatef(worldLocX, worldLocY, worldLocZ);
		gl.glScalef(scale, scale, scale);
		gl.glBegin(GL2.GL_TRIANGLES);
		for (int i = 0; i < faces.size(); i++) {
			int[] face = faces.get(i);
			for (int j = 0; j < 3; j++) {
				CMVDVertex v = vertices.get(face[j]);
				gl.glColor3ub((byte) v.col.getRed(), (byte) v.col.getGreen(), (byte) v.col.getBlue());
				gl.glVertex3f(v.x, v.y, v.z);
			}
		}
		gl.glEnd();
		gl.glPopMatrix();
	}

	public static class CMVDVertex {

		public float x;
		public float y;
		public float z;

		public Color col;

		public CMVDVertex() {
		}

		public CMVDVertex(DataInputStream dis) throws IOException {
			x = dis.readFloat();
			y = dis.readFloat();
			z = dis.readFloat();
			col = new Color(dis.readInt());
		}

		public void write(DataOutputStream dos) throws IOException {
			dos.writeFloat(x);
			dos.writeFloat(y);
			dos.writeFloat(z);
			dos.writeInt(col.getRGB());
		}
	}
}
