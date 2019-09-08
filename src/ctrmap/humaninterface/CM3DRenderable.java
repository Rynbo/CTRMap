package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;

/**
 * Interface for rendering objects from components in CM3D
 */
public interface CM3DRenderable {
	public void renderCM3D(GL2 gl);
	public void renderOverlayCM3D(GL2 gl);
}
