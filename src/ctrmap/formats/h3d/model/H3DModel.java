package ctrmap.formats.h3d.model;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import ctrmap.formats.h3d.BCHHeader;
import ctrmap.formats.h3d.PICACommand;
import ctrmap.formats.h3d.PICACommandReader;
import ctrmap.formats.h3d.RandomAccessBAIS;
import ctrmap.formats.h3d.StringUtils;
import ctrmap.formats.h3d.texturing.H3DMaterial;
import ctrmap.formats.h3d.texturing.H3DTexture;
import ctrmap.formats.vectors.Vec2d;
import ctrmap.formats.vectors.Vec3f;
import ctrmap.formats.vectors.Vec4f;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class H3DModel {

	public float worldLocX = 0f;
	public float worldLocY = 0f;
	public float worldLocZ = 0f;

	public float scaleX = 1f;
	public float scaleY = 1f;
	public float scaleZ = 1f;

	public float rotationX = 0f;
	public float rotationY = 0f;
	public float rotationZ = 0f;

	public BCHModelHeader modelHeader;

	public String name;
	public int layerId;
	public List<H3DMesh> meshes = new ArrayList<>();
	public Matrix4 transform;

	public Vec3f minVector = new Vec3f();
	public Vec3f maxVector = new Vec3f();
	public float[][] boxVectors = new float[32][];

	public List<H3DMaterial> materials = new ArrayList<>();

	public H3DSkeleton skeleton;

	public int verticesCount() {
		int count = 0;
		for (H3DMesh obj : meshes) {
			count += obj.vertices.size();
		}
		return count;
	}

	public H3DModel(RandomAccessBAIS in, byte[] buf, BCHHeader properties) throws IOException {
		int objectsHeaderOffset = in.readInt();

		//Objects header
		in.seek(objectsHeaderOffset);
		modelHeader = new BCHModelHeader();
		modelHeader.flags = in.readByte();
		modelHeader.skeletonScalingType = in.readByte();
		modelHeader.silhouetteMaterialEntries = in.readShort();

		modelHeader.worldTransform = new Matrix4();
		modelHeader.worldTransform.getMatrix()[0] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[1] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[2] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[3] = in.readFloat();

		modelHeader.worldTransform.getMatrix()[4] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[5] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[6] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[7] = in.readFloat();

		modelHeader.worldTransform.getMatrix()[8] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[9] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[10] = in.readFloat();
		modelHeader.worldTransform.getMatrix()[11] = in.readFloat();

		modelHeader.materialsTableOffset = in.readInt();
		modelHeader.materialsTableEntries = in.readInt();
		modelHeader.materialsNameOffset = in.readInt();
		modelHeader.verticesTableOffset = in.readInt();
		modelHeader.verticesTableEntries = in.readInt();
		in.skip(properties.backwardCompatibility > 6 ? 0x28 : 0x20);
		modelHeader.skeletonOffset = in.readInt();
		modelHeader.skeletonEntries = in.readInt();
		modelHeader.skeletonNameOffset = in.readInt();
		modelHeader.objectsNodeVisibilityOffset = in.readInt();
		modelHeader.objectsNodeCount = in.readInt();
		int modelNameOffset = in.readInt();
		modelHeader.modelName = StringUtils.readString(modelNameOffset, buf);
		modelHeader.objectsNodeNameEntries = in.readInt();
		modelHeader.objectsNodeNameOffset = in.readInt();
		in.skip(4);
		modelHeader.metaDataPointerOffset = in.readInt();

		transform = modelHeader.worldTransform;
		name = modelHeader.modelName;

		modelHeader.objectNames = new String[modelHeader.objectsNodeNameEntries];
		in.seek(modelHeader.objectsNodeNameOffset);
		int rootReferenceBit = in.readInt();
		int rootLeftNode = in.readShort();
		int rootRightNode = in.readShort();
		int rootNameOffset = in.readInt();
		for (int i = 0; i < modelHeader.objectsNodeNameEntries; i++) {
			int referenceBit = in.readInt();
			int leftNode = in.readUnsignedShort();
			int rightNode = in.readUnsignedShort();
			modelHeader.objectNames[i] = StringUtils.readString(in.readInt(), buf);
		}

		//Skeleton
		in.seek(modelHeader.skeletonOffset);
		skeleton = new H3DSkeleton(in, buf, modelHeader.skeletonEntries, name);

		in.seek(modelHeader.objectsNodeVisibilityOffset);
		int nodeVisibility = in.readInt();

		//Vertices header
		in.seek(modelHeader.verticesTableOffset);
		for (int index = 0; index < modelHeader.verticesTableEntries; index++) {
			H3DMesh objectEntry = new H3DMesh();
			objectEntry.materialId = in.readUnsignedShort();
			short flags = in.readShort();

			if (properties.backwardCompatibility != 8) {
				objectEntry.isSilhouette = (flags & 1) > 0;
			}
			objectEntry.nodeId = in.readUnsignedShort();
			objectEntry.renderPriority = in.readUnsignedShort();
			objectEntry.vshAttributesBufferCommandsOffset = in.readInt(); //Buffer 0
			objectEntry.vshAttributesBufferCommandsWordCount = in.readInt();
			objectEntry.facesHeaderOffset = in.readInt();
			objectEntry.facesHeaderEntries = in.readInt();
			objectEntry.vshExtraAttributesBufferCommandsOffset = in.readInt(); //Buffers 1-11
			objectEntry.vshExtraAttributesBufferCommandsWordCount = in.readInt();
			objectEntry.centerVector = new Vec3f(in.readFloat(), in.readFloat(), in.readFloat());
			objectEntry.flagsOffset = in.readInt();
			in.skip(4);
			objectEntry.boundingBoxOffset = in.readInt();

			meshes.add(objectEntry);
		}
		for (int objIndex = 0; objIndex < meshes.size(); objIndex++) {
			H3DMesh obj = meshes.get(objIndex);
			if (obj.nodeId < modelHeader.objectNames.length) {
				obj.name = modelHeader.objectNames[obj.nodeId];
			} else {
				obj.name = "mesh" + objIndex;
			}
			obj.isVisible = (nodeVisibility & (1 << obj.nodeId)) > 0;
			if (obj.isSilhouette) {
				continue;
			}
			//Vertices
			in.seek(obj.vshAttributesBufferCommandsOffset);
			PICACommandReader vshCommands = new PICACommandReader(in, obj.vshAttributesBufferCommandsWordCount, false);

			Stack<Float> vshAttributesUniformReg6 = vshCommands.getVSHFloatUniformData(6);
			Stack<Float> vshAttributesUniformReg7 = vshCommands.getVSHFloatUniformData(7);
			Vec4f positionOffset = new Vec4f(
					vshAttributesUniformReg6.pop(),
					vshAttributesUniformReg6.pop(),
					vshAttributesUniformReg6.pop(),
					vshAttributesUniformReg6.pop()
			);
			float texture0Scale = vshAttributesUniformReg7.pop();
			float texture1Scale = vshAttributesUniformReg7.pop();
			float texture2Scale = vshAttributesUniformReg7.pop();
			float boneWeightScale = vshAttributesUniformReg7.pop();
			float positionScale = vshAttributesUniformReg7.pop();
			float normalScale = vshAttributesUniformReg7.pop();
			float tangentScale = vshAttributesUniformReg7.pop();
			float colorScale = vshAttributesUniformReg7.pop();

			//Faces
			int facesCount = obj.facesHeaderEntries;
			boolean hasFaces = facesCount > 0;
			int facesTableOffset = 0;
			if (!hasFaces) {
				in.seek(modelHeader.verticesTableOffset + modelHeader.verticesTableEntries * 0x38);
				in.seek(objIndex * 0x1c + 0x10 + in.position);

				facesTableOffset = in.readInt();
				facesCount = in.readInt();
			}
			try {

				for (int f = 0; f < facesCount; f++) {
					ArrayList<Integer> nodeList = new ArrayList<>();
					int idxBufferOffset;
					PICACommand.indexBufferFormat idxBufferFormat;
					int idxBufferTotalVertices;
					SkinningMode skinningMode = SkinningMode.none;

					if (hasFaces) {
						int baseOffset = obj.facesHeaderOffset + f * 0x34;
						in.seek(baseOffset);
						skinningMode = SkinningMode.values()[in.readUnsignedShort()];
						int nodeIdEntries = in.readUnsignedShort();
						for (int n = 0; n < nodeIdEntries; n++) {
							nodeList.add(in.readUnsignedShort());
						}

						in.seek(baseOffset + 0x2c);
						int faceHeaderOffset = in.readInt();
						int faceHeaderWordCount = in.readInt();
						in.seek(faceHeaderOffset);
						PICACommandReader idxCommands = new PICACommandReader(in, faceHeaderWordCount, false);
						idxBufferOffset = (int) idxCommands.getIndexBufferAddress();
						idxBufferFormat = idxCommands.getIndexBufferFormat();
						idxBufferTotalVertices = (int) idxCommands.getIndexBufferTotalVertices();
					} else {
						in.seek(facesTableOffset + f * 8);

						idxBufferOffset = in.readInt();
						idxBufferFormat = PICACommand.indexBufferFormat.unsignedShort;
						idxBufferTotalVertices = in.readInt();
					}

					int vshAttributesBufferOffset = (int) vshCommands.getVSHAttributesBufferAddress(0);
					int vshAttributesBufferStride = vshCommands.getVSHAttributesBufferStride(0);
					int vshTotalAttributes = (int) vshCommands.getVSHTotalAttributes(0);
					PICACommand.vshAttribute[] vshMainAttributesBufferPermutation = vshCommands.getVSHAttributesBufferPermutation();
					int[] vshAttributesBufferPermutation = vshCommands.getVSHAttributesBufferPermutation(0);
					PICACommand.attributeFormat[] vshAttributesBufferFormat = vshCommands.getVSHAttributesBufferFormat();
					for (int attribute = 0; attribute < vshTotalAttributes; attribute++) {
						switch (vshMainAttributesBufferPermutation[vshAttributesBufferPermutation[attribute]]) {
							case normal:
								obj.hasNormal = true;
								break;
							case tangent:
								obj.hasTangent = true;
								break;
							case color:
								obj.hasColor = true;
								break;
							case textureCoordinate0:
								obj.texUVCount = Math.max(obj.texUVCount, 1);
								break;
							case textureCoordinate1:
								obj.texUVCount = Math.max(obj.texUVCount, 2);
								break;
							case textureCoordinate2:
								obj.texUVCount = Math.max(obj.texUVCount, 3);
								break;
						}
					}

					if (nodeList.size() > 0) {
						obj.hasNode = true;
						obj.hasWeight = true;
					}

					in.seek(idxBufferOffset);
					ArrayList<String> list = new ArrayList<>();
					for (int faceIndex = 0; faceIndex < idxBufferTotalVertices; faceIndex++) {
						int index = 0;
						switch (idxBufferFormat) {
							case unsignedShort:
								index = in.readUnsignedShort();
								break;
							case unsignedByte:
								index = in.read();
								break;
						}

						int dataPosition = in.position;
						int vertexOffset = vshAttributesBufferOffset + (index * vshAttributesBufferStride);
						in.seek(vertexOffset);

						H3DVertex vertex = new H3DVertex();
						vertex.diffuseColor = 0xffffffff;
						for (int attribute = 0; attribute < vshTotalAttributes; attribute++) {
							PICACommand.vshAttribute att = vshMainAttributesBufferPermutation[vshAttributesBufferPermutation[attribute]];
							PICACommand.attributeFormat format = vshAttributesBufferFormat[vshAttributesBufferPermutation[attribute]];
							if (att == PICACommand.vshAttribute.boneWeight) {
								format.type = PICACommand.attributeFormatType.unsignedByte;
							}
							Vec4f vector = getVector(in, format);

							switch (att) {
								case position:
									float x = (vector.x * positionScale) + positionOffset.x;
									float y = (vector.y * positionScale) + positionOffset.y;
									float z = (vector.z * positionScale) + positionOffset.z;
									vertex.position = new Vec3f(x, y, z);
									break;
								case normal:
									vertex.normal = new Vec3f(vector.x * normalScale, vector.y * normalScale, vector.z * normalScale);
									break;
								case tangent:
									vertex.tangent = new Vec3f(vector.x * tangentScale, vector.y * tangentScale, vector.z * tangentScale);
									break;
								case color:
									int r = OhanaMeshUtils.saturate((vector.x * colorScale) * 0xff);
									int g = OhanaMeshUtils.saturate((vector.y * colorScale) * 0xff);
									int b = OhanaMeshUtils.saturate((vector.z * colorScale) * 0xff);
									int a = OhanaMeshUtils.saturate((vector.w * colorScale) * 0xff);
									//vertex.diffuseColor = new Color(r, g, b, a).getRGB();
									vertex.diffuseColor = b | (g << 8) | (r << 16) | (a << 24);
									break;
								case textureCoordinate0:
									vertex.texture0 = new Vec2d(vector.x * texture0Scale, vector.y * texture0Scale);
									break;
								case textureCoordinate1:
									vertex.texture1 = new Vec2d(vector.x * texture1Scale, vector.y * texture1Scale);
									break;
								case textureCoordinate2:
									vertex.texture2 = new Vec2d(vector.x * texture2Scale, vector.y * texture2Scale);
									break;
								case boneIndex:
									vertex.node.add(nodeList.get((int) vector.x));
									if (skinningMode == SkinningMode.smoothSkinning) {
										if (format.attributeLength > 0) {
											vertex.node.add(nodeList.get((int) vector.y));
										}
										if (format.attributeLength > 1) {
											vertex.node.add(nodeList.get((int) vector.z));
										}
										if (format.attributeLength > 2) {
											vertex.node.add(nodeList.get((int) vector.w));
										}
									}
									break;
								case boneWeight:
									vertex.weight.add(vector.x * boneWeightScale);
									if (skinningMode == SkinningMode.smoothSkinning) {
										if (format.attributeLength > 0) {
											vertex.weight.add(vector.y * boneWeightScale);
										}
										if (format.attributeLength > 1) {
											vertex.weight.add(vector.z * boneWeightScale);
										}
										if (format.attributeLength > 2) {
											vertex.weight.add(vector.w * boneWeightScale);
										}
									}
									break;
							}
						}

						//If the node list have 4 or less bones, then there is no need to store the indices per vertex
						//Instead, the entire list is used, since it supports up to 4 bones.
						if (vertex.node.isEmpty() && nodeList.size() <= 4) {
							for (int n = 0; n < nodeList.size(); n++) {
								vertex.node.add(nodeList.get(n));
							}
							if (vertex.weight.isEmpty()) {
								vertex.weight.add(1f);
							}
						}

						if (skinningMode != SkinningMode.smoothSkinning && vertex.node.size() > 0) {
							//Note: Rigid skinning can have only one bone per vertex
							//Note2: Vertex with Rigid skinning seems to be always have meshes centered, so is necessary to make them follow the skeleton
							if (vertex.weight.isEmpty()) {
								vertex.weight.add(1f);
							}
							vertex.position.x += skeleton.transform.get(vertex.node.get(0)).getMatrix()[12] * vertex.weight.get(0);
							vertex.position.y += skeleton.transform.get(vertex.node.get(0)).getMatrix()[13] * vertex.weight.get(0);
							vertex.position.z += skeleton.transform.get(vertex.node.get(0)).getMatrix()[14] * vertex.weight.get(0);
						}

						OhanaMeshUtils.calculateBounds(this, vertex);
						obj.vertices.add(vertex);

						in.seek(dataPosition);
					}
				}
			} catch (EOFException e) {
				e.printStackTrace();
			}
		}
		makeBox();
	}

	public void makeBox() {
		OhanaMeshUtils.makeBox(boxVectors, minVector, maxVector);
	}

	/**
	 * Deprecated. Replaced by fixed rigid skinning instead.
	 */
	public void adjustBoneVerticesToMatrix() {
		for (int i = 0; i < meshes.size(); i++) {
			for (int j = 0; j < meshes.get(i).vertices.size(); j++) {
				H3DVertex v = meshes.get(i).vertices.get(j);
				if (!v.node.isEmpty()) {
					Vec3f alter = new Vec3f();
					float weightSum = 0;
					int weightIndex = 0;
					for (int node = 0; node < v.node.size(); node++) {
						if (skeleton.transform.size() <= v.node.get(node)) {
							break; //weird model
						}
						float weight = 0;
						if (weightIndex < v.weight.size()) {
							weight = v.weight.get(weightIndex++);
						}
						weightSum += weight;
						alter.x += v.position.x * weight;
						alter.y += v.position.y * weight;
						alter.z += v.position.z * weight;
						alter.x += skeleton.transform.get(v.node.get(node)).getMatrix()[12] * weight;
						alter.y += skeleton.transform.get(v.node.get(node)).getMatrix()[13] * weight;
						alter.z += skeleton.transform.get(v.node.get(node)).getMatrix()[14] * weight;
					}
					if (weightSum < 1) {
						alter.x += v.position.x * (1 - weightSum);
						alter.y += v.position.y * (1 - weightSum);
						alter.z += v.position.z * (1 - weightSum);
					}
					v.position = alter;
				}
			}
		}
	}

	public void makeAllBOs() {
		for (int i = 0; i < meshes.size(); i++) {
			if (materials.size() > meshes.get(i).materialId) {
				meshes.get(i).makeBOs(materials.get(meshes.get(i).materialId));
			} else {
				meshes.get(i).makeBOs(null);
			}
			meshes.get(i).doVBOUpdate = true;
			meshes.get(i).useVBO = true;
		}
	}

	public void uploadAllBOs(GL2 gl) {
		for (int i = 0; i < meshes.size(); i++) {
			meshes.get(i).uploadVBO(gl);
		}
	}

	public void destroyAllBOs(GL2 gl) {
		for (int i = 0; i < meshes.size(); i++) {
			meshes.get(i).destroyGl(gl);
		}
	}

	public void setMaterialTextures(List<H3DTexture> textures) {
		for (int i = 0; i < materials.size(); i++) {
			H3DMaterial mat = materials.get(i);
			boolean[] complete = new boolean[3];
			for (int j = 0; j < textures.size(); j++) {
				if (textures.get(j).textureName.equals(mat.name0) && !"projection_dummy".equals(mat.name0)) {
					mat.texture0 = textures.get(j);
					complete[0] = true;
				}
				if (textures.get(j).textureName.equals(mat.name1) && !"projection_dummy".equals(mat.name1)) {
					mat.texture1 = textures.get(j);
					complete[1] = true;
				}
				if (textures.get(j).textureName.equals(mat.name2)) {
					mat.texture2 = textures.get(j);
					complete[2] = true;
				}
				if (complete[0] && complete[1] && complete[2]) {
					break;
				}
			}
		}
	}

	public static Vec3f transformVec3f(Vec3f input, Matrix4 matrix) {
		Vec3f output = new Vec3f();
		output.x = input.x * matrix.getMatrix()[0] + input.y * matrix.getMatrix()[1] + input.z * matrix.getMatrix()[2] + matrix.getMatrix()[3];
		output.y = input.x * matrix.getMatrix()[4] + input.y * matrix.getMatrix()[5] + input.z * matrix.getMatrix()[6] + matrix.getMatrix()[7];
		output.z = input.x * matrix.getMatrix()[8] + input.y * matrix.getMatrix()[9] + input.z * matrix.getMatrix()[10] + matrix.getMatrix()[11];
		return output;
	}

	public enum SkinningMode {
		none,
		smoothSkinning,
		rigidSkinning
	}

	private static Vec4f getVector(RandomAccessBAIS input, PICACommand.attributeFormat format) throws IOException {
		Vec4f output = new Vec4f();

		switch (format.type) {
			case signedByte:
				output.x = input.readByte();
				if (format.attributeLength > 0) {
					output.y = input.readByte();
				}
				if (format.attributeLength > 1) {
					output.z = input.readByte();
				}
				if (format.attributeLength > 2) {
					output.w = input.readByte();
				}
				break;
			case unsignedByte:
				output.x = input.read();
				if (format.attributeLength > 0) {
					output.y = input.read();
				}
				if (format.attributeLength > 1) {
					output.z = input.read();
				}
				if (format.attributeLength > 2) {
					output.w = input.read();
				}
				break;
			case signedShort:
				output.x = input.readShort();
				if (format.attributeLength > 0) {
					output.y = input.readShort();
				}
				if (format.attributeLength > 1) {
					output.z = input.readShort();
				}
				if (format.attributeLength > 2) {
					output.w = input.readShort();
				}
				break;
			case single:
				output.x = input.readFloat();
				if (format.attributeLength > 0) {
					output.y = input.readFloat();
				}
				if (format.attributeLength > 1) {
					output.z = input.readFloat();
				}
				if (format.attributeLength > 2) {
					output.w = input.readFloat();
				}
				break;
		}

		return output;
	}

	public void render(GL2 gl) {
		gl.glPushMatrix();
		gl.glTranslatef(worldLocX, worldLocY, worldLocZ);
		gl.glRotatef(rotationZ, 0f, 0f, 1f);
		gl.glRotatef(rotationX, 1f, 0f, 0f);
		gl.glRotatef(rotationY, 0f, 1f, 0f);
		gl.glScalef(scaleX, scaleY, scaleZ);
		for (int i = 0; i < meshes.size(); i++) {
			meshes.get(i).render(gl, (materials.size() > meshes.get(i).materialId) ? materials.get(meshes.get(i).materialId) : null);
		}
		gl.glPopMatrix();
	}

	public void renderBox(GL2 gl) { //calculated by calculateBounds
		gl.glPushMatrix();
		gl.glTranslatef(worldLocX, worldLocY, worldLocZ);
		gl.glRotatef(rotationZ, 0f, 0f, 1f);
		gl.glRotatef(rotationX, 1f, 0f, 0f);
		gl.glRotatef(rotationY, 0f, 1f, 0f);
		gl.glScalef(scaleX, scaleY, scaleZ);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glBegin(GL2.GL_LINES);

		gl.glColor3f(1f, 0f, 0f);

		for (int i = 0; i < boxVectors.length; i++) {
			gl.glVertex3fv(boxVectors[i], 0);
		}

		gl.glEnd();
		gl.glPopMatrix();
	}

	public static class H3DMesh {

		public String name;

		public int materialId;
		public boolean isSilhouette;
		public int nodeId;
		public int renderPriority;
		public int vshAttributesBufferCommandsOffset;
		public int vshAttributesBufferCommandsWordCount;
		public int facesHeaderOffset;
		public int facesHeaderEntries;
		public int vshExtraAttributesBufferCommandsOffset;
		public int vshExtraAttributesBufferCommandsWordCount;
		public int flagsOffset;
		public int boundingBoxOffset;
		public boolean hasNormal;
		public boolean hasTangent;
		public boolean hasColor;
		public boolean hasNode;
		public boolean hasWeight;
		public boolean isVisible;
		public int texUVCount;

		public ArrayList<H3DVertex> vertices = new ArrayList<>();
		public Vec3f centerVector;

		public FloatBuffer vbo;
		public ByteBuffer vcbo;
		public DoubleBuffer[] tcbo;

		public int texindex;
		public int textureWidth;
		public int textureHeight;
		public byte[] textureData;

		public boolean doVBOUpdate = true;
		public boolean useVBO = false;

		public Map<GL2, int[]> glBufPtrs = new HashMap<>();
		public Map<GL2, int[]> glTexturePtrs = new HashMap<>();

		public void makeBOs(H3DMaterial mat) { //maybe someday?
			vbo = FloatBuffer.allocate(vertices.size() * 3);
			vcbo = ByteBuffer.allocateDirect(vertices.size() * 4);
			tcbo = new DoubleBuffer[3];
			boolean mirrorx0 = false;
			boolean mirrorx1 = false;
			boolean mirrorx2 = false;

			if (mat != null) {
				tcbo[0] = DoubleBuffer.allocate(vertices.size() * 2);
				tcbo[1] = DoubleBuffer.allocate(vertices.size() * 2);
				tcbo[2] = DoubleBuffer.allocate(vertices.size() * 2);

				if (H3DMaterial.TextureMapper.getGlTextureWrap(mat.mappers[0].wrapU) == GL2.GL_MIRRORED_REPEAT) {
					mirrorx0 = true;
				}
				if (H3DMaterial.TextureMapper.getGlTextureWrap(mat.mappers[1].wrapU) == GL2.GL_MIRRORED_REPEAT) {
					mirrorx1 = true;
				}
				if (H3DMaterial.TextureMapper.getGlTextureWrap(mat.mappers[2].wrapU) == GL2.GL_MIRRORED_REPEAT) {
					mirrorx2 = true;
				}
				if (!(mat.texture0 == null && mat.texture1 == null && mat.texture2 == null)) {
					if (mat.texture0 != null) {
						texindex = 0;
						textureWidth = mat.texture0.textureSize.width;
						textureHeight = mat.texture0.textureSize.height;
						textureData = mat.texture0.textureData;
					} else if (mat.texture1 != null) {
						texindex = 1;
						textureWidth = mat.texture1.textureSize.width;
						textureHeight = mat.texture1.textureSize.height;
						textureData = mat.texture1.textureData;
					} else {
						texindex = 2;
						textureWidth = mat.texture2.textureSize.width;
						textureHeight = mat.texture2.textureSize.height;
						textureData = mat.texture2.textureData;
					}
				}
			}
			for (int i = 0; i < vertices.size(); i++) {
				H3DVertex v = vertices.get(i);
				vbo.put(v.position.x);
				vbo.put(v.position.y);
				vbo.put(v.position.z);

				if (mat == null || !(mat.name.contains("blc") || mat.name0.equals("enc_grass"))) {
					vcbo.put((byte) (v.diffuseColor >> 16 & 0xFF));
					vcbo.put((byte) (v.diffuseColor >> 8 & 0xFF));
					vcbo.put((byte) (v.diffuseColor & 0xFF));
					vcbo.put((byte) (v.diffuseColor >> 24 & 0xFF));
				} else {
					vcbo.putInt(0xffffffff);
				}
				if (mat != null) {
					tcbo[0].put(((mirrorx0) ? 1 : 0) - v.texture0.x);
					tcbo[0].put(v.texture0.y);
					tcbo[1].put(((mirrorx1) ? 1 : 0) - v.texture1.x);
					tcbo[1].put(v.texture1.y);
					tcbo[2].put(((mirrorx2) ? 1 : 0) - v.texture2.x);
					tcbo[2].put(v.texture2.y);
				}
			}
		}

		public void destroyGl(GL2 gl) {
			if (glBufPtrs.get(gl) != null) {
				gl.glDeleteBuffers(1, glBufPtrs.get(gl), 0);
				gl.glDeleteTextures(1, glTexturePtrs.get(gl), 0);
				glBufPtrs.remove(gl);
				glTexturePtrs.remove(gl);
			}
		}

		public void uploadVBO(GL2 gl) {
			if (vbo == null) {
				useVBO = false;
				return;
			}
			if (!glBufPtrs.containsKey(gl)) {
				int[] ptr = new int[1];
				gl.glGenBuffers(1, ptr, 0);
				glBufPtrs.put(gl, ptr);
			}
			if (!glTexturePtrs.containsKey(gl)) {
				int[] textureptr = new int[1];
				gl.glGenTextures(1, textureptr, 0);
				glTexturePtrs.put(gl, textureptr);
			}
			int[] vboPtr = glBufPtrs.get(gl);
			int[] texturePtr = glTexturePtrs.get(gl);
			gl.glDeleteTextures(1, texturePtr, 0);
			gl.glDeleteBuffers(1, vboPtr, 0);
			vbo.rewind();
			vcbo.rewind();
			tcbo[0].rewind();
			tcbo[1].rewind();
			tcbo[2].rewind();
			long cap = vbo.capacity() * Float.BYTES + vcbo.capacity();
			if (tcbo[0] != null) {
				cap += tcbo[0].capacity() * Double.BYTES;
			}
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboPtr[0]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, cap, null, GL2.GL_STATIC_DRAW);
			gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, 0, vbo.capacity() * Float.BYTES, vbo);
			gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, vbo.capacity() * Float.BYTES, vcbo.capacity(), vcbo);
			if (tcbo[0] != null) {
				gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, vbo.capacity() * Float.BYTES + vcbo.capacity(), tcbo[0].capacity() * Double.BYTES, tcbo[0]);
			}
			gl.glBindTexture(GL2.GL_TEXTURE_2D, glTexturePtrs.get(gl)[0]);
			if (textureData != null) {
				gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, textureWidth, textureHeight, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(textureData));
			}
		}

		public void render(GL2 gl, H3DMaterial mat) {
			/*
			Stuff involving checking for "blc" in material name is a workaround for tall grass rendering, which uses weird vertex coloring and alpha testing
			 */
			boolean mirrorx = false;
			if (glTexturePtrs.containsKey(gl)) {
				if (mat != null) {
					if (!(mat.texture0 == null && mat.texture1 == null && mat.texture2 == null)) {
						gl.glEnable(GL2.GL_TEXTURE_2D);
						gl.glBindTexture(GL2.GL_TEXTURE_2D, glTexturePtrs.get(gl)[0]);
						int twX = H3DMaterial.TextureMapper.getGlTextureWrap(mat.mappers[texindex].wrapU);
						if (twX == GL2.GL_MIRRORED_REPEAT) {
							mirrorx = true;
						}
						int twY = H3DMaterial.TextureMapper.getGlTextureWrap(mat.mappers[texindex].wrapV);
						gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, twX);
						gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, twY);
						gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
						gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
						if (mat.params.blendop.mode == H3DMaterial.BlendOperation.BlendMode.blend) {
							gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA); //forces to 1-a to prevent drunk framebuffer. renders fine.
							gl.glBlendEquation(H3DMaterial.BlendOperation.getGlBlendEqt(mat.params.blendop.alphaBlendEquation));
							gl.glDisable(GL2.GL_ALPHA_TEST);
							gl.glEnable(GL2.GL_BLEND);
						} else {
							gl.glDisable(GL2.GL_BLEND);
							if (mat.params.alphaTest.isTestEnabled && !mat.name.contains("blc")) {
								gl.glAlphaFunc(mat.params.alphaTest.getGlTestFunc(), 0); //forcing to 0 prevents some meshes not appearing, with references like 58 etc. no glitches afaik caused by this.
								gl.glEnable(GL2.GL_ALPHA_TEST);
							} else {
								gl.glDisable(GL2.GL_ALPHA_TEST);
							}
						}
						if (mat.name.contains("blc") || mat.name0.equals("enc_grass")) {
							gl.glAlphaFunc(GL.GL_NOTEQUAL, 0);
							gl.glEnable(GL2.GL_ALPHA_TEST);
						}
					}
				}
			}
			if (useVBO) {
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, glBufPtrs.get(gl)[0]);
				gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

				gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
				gl.glColorPointer(4, GL2.GL_UNSIGNED_BYTE, 0, vbo.capacity() * Float.BYTES);

				if (tcbo[0] != null) {
					gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
					gl.glTexCoordPointer(2, GL2.GL_DOUBLE, 0, vbo.capacity() * Float.BYTES + vcbo.capacity());
				}

				gl.glDrawArrays(GL2.GL_TRIANGLES, 0, vbo.capacity() / 3);
				gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);  // disable vertex arrays
				gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
				gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			} else {
				gl.glBegin(GL2.GL_TRIANGLES);
				for (int i = 0; i < vertices.size(); i++) {
					H3DVertex v = vertices.get(i);
					if (hasColor && !(mat.name.contains("blc") || mat.name0.equals("enc_grass"))) {
						gl.glColor4ub((byte) (v.diffuseColor >> 16 & 0xFF), (byte) (v.diffuseColor >> 8 & 0xFF), (byte) (v.diffuseColor & 0xFF), (byte) (v.diffuseColor >> 24 & 0xFF));
					} else {
						gl.glColor3f(1f, 1f, 1f);
					}
					if (mat != null) {
						gl.glTexCoord2d((mirrorx ? 1 : 0) - v.texture0.x, v.texture0.y);
					}
					gl.glVertex3f(v.position.x, v.position.y, v.position.z);
				}
				gl.glEnd();
			}
		}
	}
}
