package ctrmap.formats.gfcollision;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.jogamp.opengl.GL2;
import static ctrmap.CtrmapMainframe.*;
import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.Utils;
import ctrmap.formats.containers.GR;
import ctrmap.formats.Triangle;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GRCollisionFile {

	private GR mapFile;

	public static final int COLL_MAGIC = 0x636F6C6C;
	private int length;
	private int unknown_const_0x5D8_1;
	private int[] unknown_consts_1_3_2_1_2 = new int[5];

	public GRCollisionBounds bounds;
	private int[] offsets = new int[16];
	private int[] lengths = new int[16];
	public GRCollisionMesh[] meshes = new GRCollisionMesh[16]; //GR is limited to exactly 16 meshes, nothing more, nothing less

	public static final int TERM_MAGIC = 0x7465726D;
	private int unknown_const_0x0;
	private int unknown_const_0x5D8_2;
	private int unknown_const_0x1;

	public GRCollisionFile(GR mapFile) {
		this.mapFile = mapFile;
		LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(this.mapFile.getFile(2)));
		try {
			if (dis.read4Bytes() != COLL_MAGIC) {
				System.err.println("Coll magic mismatch");
			}
			length = dis.readInt();
			if (length > dis.available() - 24) {
				System.err.println("Coll length exceeds EOF, will be adjusted on write.");
			}
			unknown_const_0x5D8_1 = dis.readInt();
			for (int i = 0; i < 5; i++) {
				unknown_consts_1_3_2_1_2[i] = dis.readInt();
			}
			bounds = new GRCollisionBounds(dis);
			for (int i = 0; i < 16; i++) {
				offsets[i] = dis.readInt();
				//these bytes indicate the offsets of meshes from the start of the mesh sections, but the bytes
				//after tell us their lengths and as we get to the end after finishing this sections, these are
				//all we need to parse the format. GF are kinda weird. We are storing the offsets for ease of writing tho.

				lengths[i] = dis.readInt();
			}
			for (int i = 0; i < 16; i++) {
				meshes[i] = new GRCollisionMesh(dis, lengths[i]);
			}
			dedupe();
			if (dis.read4Bytes() != TERM_MAGIC) {
				System.err.println("Term missing/misplaced/magic error.");
			}
			unknown_const_0x0 = dis.readInt();
			unknown_const_0x5D8_2 = dis.readInt();
			unknown_const_0x1 = dis.readInt();
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		bounds.originalExtremes = getMeshExtremes();
	}

	public float getHeightAtPoint(float x, float y){
		//just check all the tris, if they are deduped, GF's hacks are useless
		for (int i = 0; i < meshes.length; i++){
			for (int j = 0; j < meshes[i].tris.size(); j++){
				Polygon triPoly = meshes[i].tris.get(j).getAWTPoly();
				if (triPoly.contains(x, y)){
					Triangle tri = meshes[i].tris.get(j);
					float top1 = (tri.getX(1) - tri.getX(0)) * (tri.getY(2) - tri.getY(0)) - (tri.getX(2) - tri.getX(0)) * (tri.getY(1) - tri.getY(0));
					float bot1 = (tri.getX(1) - tri.getX(0)) * (tri.getZ(2) - tri.getZ(0)) - (tri.getX(2) - tri.getX(0)) * (tri.getZ(1) - tri.getZ(0));
					float top2 = (tri.getZ(1) - tri.getZ(0)) * (tri.getY(2) - tri.getY(0)) - (tri.getZ(2) - tri.getZ(0)) * (tri.getY(1) - tri.getY(0));
					
					float result = tri.getY(0) + (top1 / bot1) * (y - tri.getZ(0))  - (top2 / bot1) * (x - tri.getX(0));
					return result;
				}
			}
		}
		return 0f;
	}
	
	public void dedupe(){
		ArrayList<Triangle> usedTris = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < meshes[i].tris.size(); j++){
				Triangle testTri = meshes[i].tris.get(j);
				if (usedTris.isEmpty()){
					usedTris.add(testTri);
					continue;
				}
				boolean broken = false;
				for (int k = 0; k < usedTris.size(); k++){
					Triangle testAgainst = usedTris.get(k);
					if (Arrays.equals(testTri.x, testAgainst.x) && Arrays.equals(testTri.y, testAgainst.y) && Arrays.equals(testTri.z, testAgainst.z)){
						meshes[i].tris.remove(j);
						j --;
						broken = true;
						break;
					}
				}
				if (!broken){
					usedTris.add(testTri);
				}
			}
		}
	}
	
	public void write() {
		mCollEditPanel.deselectTri();
		mCollEditPanel.deselectMesh();
		bounds.updateBounds(this);
		GRCollisionMesh[] newMeshes = computeMeshOrder();
		offsets[0] = 0;
		lengths[0] = newMeshes[0].tris.size() * 3;
		for (int i = 1; i < 16; i++) {
			lengths[i] = newMeshes[i].tris.size() * 3;
			offsets[i] = offsets[i - 1] + lengths[i - 1];
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(out);
		try {
			dos.write4Bytes(COLL_MAGIC);
			length = 640 /*bounds*/ + 128 /*desc*/ + (offsets[15] + lengths[15]) * 16 /*number of vertices times 16 bytes*/ + 16 /*term*/;
			dos.writeInt(length);
			dos.writeInt(unknown_const_0x5D8_1);
			for (int i = 0; i < 5; i++) {
				dos.writeInt(unknown_consts_1_3_2_1_2[i]);
			}
			bounds.write(dos);
			for (int i = 0; i < 16; i++) {
				dos.writeInt(offsets[i]);
				dos.writeInt(lengths[i]);
			}
			for (int i = 0; i < 16; i++) {
				newMeshes[i].write(dos);
			}
			dos.write4Bytes(TERM_MAGIC);
			dos.writeInt(unknown_const_0x0);
			dos.writeInt(unknown_const_0x5D8_2);
			dos.writeInt(unknown_const_0x1);
			//ayy padding
			dos.write(Utils.getPadding(mapFile.getOffset(2), dos.size()));
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mapFile.storeFile(2, out.toByteArray());
		mCollEditPanel.buildTree();
	}

	public float[] getMeshExtremes() {
		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxZ = -Float.MAX_VALUE;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < meshes[i].tris.size(); j++) {
				for (int k = 0; k < 3; k++) {
					Triangle tri = meshes[i].tris.get(j);
					if (tri.getX(k) < minX) {
						minX = tri.getX(k);
					}
					if (tri.getX(k) > maxX) {
						maxX = tri.getX(k);
					}
					if (tri.getY(k) < minY) {
						minY = tri.getY(k);
					}
					if (tri.getY(k) > maxY) {
						maxY = tri.getY(k);
					}
					if (tri.getZ(k) < minZ) {
						minZ = tri.getZ(k);
					}
					if (tri.getZ(k) > maxZ) {
						maxZ = tri.getZ(k);
					}
				}
			}
		}
		return new float[]{minX, maxX, minY, maxY, minZ, maxZ};
	}

	public GRCollisionMesh[] computeMeshOrder() {
		GRCollisionMesh[] meshes2 = new GRCollisionMesh[16];
		for (int i = 0; i < 16; i++) {
			meshes2[i] = new GRCollisionMesh();
		}
		//Deduping shouldn't be needed now that we dedupe before loading the mesh into the editor but it's more foolproof and doesn't impact performance much. Might delete later idk.
		List<Triangle> trisDuped = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			trisDuped.addAll(meshes[i].tris);
		}
		List<Triangle> tris = new ArrayList<>();
		for (int i = 0; i < trisDuped.size(); i++) {
			Triangle orTri = trisDuped.get(i);
			if (tris.isEmpty()) {
				tris.add(orTri);
			}
			boolean broken = false;
			for (int j = 0; j < tris.size(); j++) {
				Triangle testTri = tris.get(j);
				if (Arrays.equals(testTri.x, orTri.x) && Arrays.equals(testTri.y, orTri.y) && Arrays.equals(testTri.z, orTri.z)) {
					broken = true;
				}
			}
			if (!broken) {
				tris.add(orTri);
			}
		}
		//make triangles counter clockwise
		for (int i = 0; i < tris.size(); i++) {
			Triangle tri = tris.get(i);
			List<float[]> vertexList = new ArrayList<>();
			vertexList.add(new float[]{tri.getX(0), tri.getY(0), tri.getZ(0)});
			vertexList.add(new float[]{tri.getX(1), tri.getY(1), tri.getZ(1)});
			vertexList.add(new float[]{tri.getX(2), tri.getY(2), tri.getZ(2)});
			float centerX = (vertexList.get(0)[0] + vertexList.get(1)[0] + vertexList.get(2)[0]) / 3f;
			float centerZ = (vertexList.get(0)[2] + vertexList.get(1)[2] + vertexList.get(2)[2]) / 3f;
			Collections.sort(vertexList, (float[] o1, float[] o2) -> {
				double baseAngle1 = (Math.toDegrees(Math.atan2(o1[2] - centerZ, o1[0] - centerX)) + 360) % 360;
				double baseAngle2 = (Math.toDegrees(Math.atan2(o2[2] - centerZ, o2[0] - centerX)) + 360) % 360;
				return (int) (baseAngle2 - baseAngle1);
			});
			float[] v0 = vertexList.get(0);
			float[] v1 = vertexList.get(1);
			float[] v2 = vertexList.get(2);
			tris.set(i, new Triangle(new float[]{v0[0], v1[0], v2[0]}, new float[]{v0[1], v1[1], v2[1]}, new float[]{v0[2], v1[2], v2[2]}));
		}
		float[] extremes = getMeshExtremes();
		float width = (extremes[1] - extremes[0]) / 4f;
		float height = (extremes[5] - extremes[4]) / 4f;
		//split the boundary to 16 rectangles
		Rectangle2D[] rects = new Rectangle2D[16];
		for (int i = 0; i < 16; i++) {
			float x = (float) (Math.floor(i / 8f) * 2 * width) + ((i / 4f - Math.floor(i / 4f) >= 0.5f) ? width : 0f) + extremes[0];
			float y = (((i & 1) == 0) ? 0 : height) + ((i / 8f - Math.floor(i / 8f) >= 0.5f) ? 2 * height : 0f) + extremes[4];
			rects[i] = new Rectangle2D.Float(x, y, width, height);
		}
		for (int i = 0; i < tris.size(); i++) {
			for (int j = 0; j < 16; j++) {
				for (int k = 0; k < 3; k++) {
					Triangle tri = tris.get(i);
					if (tri.getLine(k).intersects(rects[j]) || rects[j].contains(tri.getX(k), tri.getZ(k))) {
						meshes2[j].tris.add(tri);
						break;
					}
				}
			}
		}
		return meshes2;
	}

	public void render(GL2 gl) {
		for (int i = 0; i < 16; i++) {
			meshes[i].render(gl);
		}
	}
}
