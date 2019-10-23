package ctrmap.formats.h3d.model;

import ctrmap.formats.vectors.Vec2d;
import ctrmap.formats.vectors.Vec3f;
import java.util.ArrayList;

public class H3DVertex {

	public Vec3f position = new Vec3f();
	public Vec3f normal = new Vec3f();
	public Vec3f tangent = new Vec3f();
	public Vec2d texture0 = new Vec2d();
	public Vec2d texture1 = new Vec2d();
	public Vec2d texture2 = new Vec2d();
	public ArrayList<Integer> node = new ArrayList<>();
	public ArrayList<Float> weight = new ArrayList<>();
	public int diffuseColor = 0;
}
