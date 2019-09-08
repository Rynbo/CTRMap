package ctrmap.humaninterface;

/**
 * Used for getting shared attributes of map entities and props
 */
public interface MapObject {
	public float getX();
	public float getY();
	public float getZ();
	public void setX(float val);
	public void setY(float val);
	public void setZ(float val);
}
