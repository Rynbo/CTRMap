package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import ctrmap.formats.vectors.Vec3f;
import java.awt.Component;
import java.awt.event.MouseEvent;

/**
 * Interface for rendering objects from components in CM3D
 */
public interface CM3DRenderable {
	public void renderCM3D(GL2 gl);
	public void renderOverlayCM3D(GL2 gl);
	public void uploadBuffers(GL2 gl);
	public void deleteGLInstanceBuffers(GL2 gl);
	public void doSelectionLoop(MouseEvent e, Component parent, float[] mvMatrix, float[] projMatrix, int[] view, Vec3f cameraVec);
}
