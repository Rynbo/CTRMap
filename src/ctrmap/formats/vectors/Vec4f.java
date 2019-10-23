
package ctrmap.formats.vectors;

public class Vec4f {
	public float x;
	public float y;
	public float z;
	public float w;
	
	public Vec4f(float x, float y, float z, float w){
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4f(Vec4f src){
		this.x = src.x;
		this.y = src.y;
		this.z = src.z;
		this.w = src.w;
	}
	
	public Vec4f(){
		x = 0;
		y = 0;
		z = 0;
		w = 0;
	}
}
