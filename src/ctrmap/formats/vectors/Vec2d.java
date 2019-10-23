
package ctrmap.formats.vectors;

public class Vec2d {
	public double x;
	public double y;
	
	public Vec2d(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public Vec2d(Vec2d src){
		this.x = src.x;
		this.y = src.y;
	}
	
	public Vec2d(){
		x = 0d;
		y = 0d;
	}
}
