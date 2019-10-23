
package ctrmap.formats.vectors;

public class Vec3f {
	public float x;
	public float y;
	public float z;
	
	public Vec3f(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3f(Vec3f src){
		this.x = src.x;
		this.y = src.y;
		this.z = src.z;
	}
	
	public Vec3f(){
		x = 0f;
		y = 0f;
		z = 0f;
	}
}
