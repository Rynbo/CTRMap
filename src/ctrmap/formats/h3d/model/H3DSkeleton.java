package ctrmap.formats.h3d.model;

import com.jogamp.opengl.math.Matrix4;
import com.sun.javafx.geom.Vec3f;
import ctrmap.formats.h3d.RandomAccessBAIS;
import ctrmap.formats.h3d.StringUtils;
import ctrmap.formats.h3d.texturing.H3DMaterial;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class H3DSkeleton {

	public List<H3DBone> bones = new ArrayList<>();
	public List<Matrix4> transform = new ArrayList<>();

	public H3DSkeleton(RandomAccessBAIS in, byte[] buf, int numBones, String name) throws IOException {
		for (int index = 0; index < numBones; index++) {
			H3DBone bone = new H3DBone();

			int boneFlags = in.readInt();
			bone.billboardMode = H3DBone.BillboardMode.values()[(boneFlags >> 16) & 0xf];
			bone.isSegmentScaleCompensate = (boneFlags & 0x00400000) > 0;
			bone.parentId = in.readShort();
			int boneSpacer = in.readUnsignedShort();
			bone.scale = new Vec3f(in.readFloat(), in.readFloat(), in.readFloat());
			bone.rotation = new Vec3f(in.readFloat(), in.readFloat(), in.readFloat());
			bone.translation = new Vec3f(in.readFloat(), in.readFloat(), in.readFloat());
			bone.absoluteScale = new Vec3f(bone.scale);

			Matrix4 boneMatrix = new Matrix4();
			boneMatrix.getMatrix()[0] = in.readFloat();
			boneMatrix.getMatrix()[1] = in.readFloat();
			boneMatrix.getMatrix()[2] = in.readFloat();
			boneMatrix.getMatrix()[3] = in.readFloat();

			boneMatrix.getMatrix()[4] = in.readFloat();
			boneMatrix.getMatrix()[5] = in.readFloat();
			boneMatrix.getMatrix()[6] = in.readFloat();
			boneMatrix.getMatrix()[7] = in.readFloat();

			boneMatrix.getMatrix()[8] = in.readFloat();
			boneMatrix.getMatrix()[9] = in.readFloat();
			boneMatrix.getMatrix()[10] = in.readFloat();
			boneMatrix.getMatrix()[11] = in.readFloat();
			
			bone.invTransform = boneMatrix;

			bone.name = StringUtils.readString(in.readInt(), buf);

			int metaDataPointerOffset = in.readInt();
			if (metaDataPointerOffset != 0) {
				int position = in.position;
				in.seek(metaDataPointerOffset);
				bone.userData = H3DMaterial.getMetaData(in, buf);
				in.seek(position);
			}

			bones.add(bone);
		}

		for (int index = 0; index < numBones; index++) {
			scaleSkeleton(bones, index, index);
		}

		for (int index = 0; index < numBones; index++) {
			Matrix4 mtx = new Matrix4();
			transformSkeleton(bones, index, mtx);
			transform.add(mtx);
		}
	}

	private static void scaleSkeleton(List<H3DSkeleton.H3DBone> skeleton, int index, int parentIndex) {
		if (index != parentIndex) {
			skeleton.get(parentIndex).absoluteScale.x *= skeleton.get(index).scale.x;
			skeleton.get(parentIndex).absoluteScale.y *= skeleton.get(index).scale.y;
			skeleton.get(parentIndex).absoluteScale.z *= skeleton.get(index).scale.z;

			skeleton.get(parentIndex).translation.x *= skeleton.get(index).scale.x;
			skeleton.get(parentIndex).translation.y *= skeleton.get(index).scale.y;
			skeleton.get(parentIndex).translation.z *= skeleton.get(index).scale.z;
		}

		if (skeleton.get(index).parentId > -1) {
			scaleSkeleton(skeleton, skeleton.get(index).parentId, parentIndex);
		}
	}

	public static void transformSkeleton(List<H3DBone> bones, int index, Matrix4 target) {
		H3DBone bone = bones.get(index);
		target.scale(bone.absoluteScale.x, bone.absoluteScale.y, bone.absoluteScale.z);
		target.rotate((float) Math.toRadians(bone.rotation.x), 1f, 0f, 0f);
		target.rotate((float) Math.toRadians(bone.rotation.y), 0f, 1f, 0f);
		target.rotate((float) Math.toRadians(bone.rotation.z), 0f, 0f, 1f);
		target.translate(bone.translation.x, bone.translation.y, bone.translation.z);

		if (bone.parentId > -1) {
			transformSkeleton(bones, bone.parentId, target);
		}
	}

	public static class H3DBone {

		public Vec3f translation;
		public Vec3f rotation;
		public Vec3f scale;
		public Vec3f absoluteScale;
		public Matrix4 invTransform;
		public short parentId;
		public String name = null;

		public BillboardMode billboardMode;
		public boolean isSegmentScaleCompensate;

		public List<H3DMaterial.MatMetaData> userData;

		public enum BillboardMode {
			off,
			dummy,
			world,
			worldViewpoint,
			screen,
			screenViewpoint,
			yAxial,
			yAxialViewpoint
		}
	}
}
