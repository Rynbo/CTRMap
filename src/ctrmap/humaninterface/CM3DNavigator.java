
package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import ctrmap.formats.CMVD;
import ctrmap.resources.ResourceAccess;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static ctrmap.CtrmapMainframe.*;

/**
 * Used to move models around the world visually.
 */
public class CM3DNavigator {
	public CMVD ax;
	public CMVD ay;
	public CMVD az;
	
	public float scale = 1f;
	
	public MapObject target;
	public double distFromTarget = 0;
	public TargetAxis targetAxis = TargetAxis.NULL;
	
	public CM3DNavigator(){
		try {
			ax = new CMVD(ResourceAccess.getStream("MoveArrowX.cmvd"));
			ay = new CMVD(ResourceAccess.getStream("MoveArrowY.cmvd"));
			az = new CMVD(ResourceAccess.getStream("MoveArrowZ.cmvd"));
		} catch (IOException ex) {
			Logger.getLogger(CM3DNavigator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void bindModel(MapObject m){
		target = m;
		synchronizeNavi();
	}
	
	public void synchronizeNavi(){
		ax.worldLocX = target.getX();
		ax.worldLocZ = target.getZ();
		ax.worldLocY = target.getY();
		ay.worldLocX = target.getX();
		ay.worldLocZ = target.getZ();
		ay.worldLocY = target.getY();
		az.worldLocX = target.getX();
		az.worldLocZ = target.getZ();
		az.worldLocY = target.getY();
	}
	
	public void synchronizeModel(){
		//we can just lock this to AX as the positions are the same
		target.setX(ax.worldLocX);
		target.setY(ax.worldLocY);
		target.setZ(ax.worldLocZ);
	}
	
	public void renderNavigator(GL2 gl){
		if (target == null) return;
		float[] cameraVec = new float[]{-m3DDebugPanel.translateX, -m3DDebugPanel.translateY, -m3DDebugPanel.translateZ};
		float[] mdlVec = new float[]{target.getX(), target.getY(), target.getZ()};
		distFromTarget = Math.pow((Math.pow(Math.abs(mdlVec[0]) - Math.abs(cameraVec[0]), 2)
								+ Math.pow(Math.abs(mdlVec[1]) - Math.abs(cameraVec[1]), 2)
								+ Math.pow(Math.abs(mdlVec[2]) - Math.abs(cameraVec[2]), 2)
								* 1.0), 0.5);
		scale = (float)(distFromTarget / 1000d);
		ax.scale = scale;
		ay.scale = scale;
		az.scale = scale;
		ax.render(gl);
		ay.render(gl);
		az.render(gl);
	}
	public enum TargetAxis{
		NULL,
		X,
		Y,
		Z
	}
}
