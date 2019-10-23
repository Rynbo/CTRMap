package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import ctrmap.formats.h3d.model.H3DModel;

/**
 * Used for showing single H3D models in a window
 */
public class CustomH3DPreview extends GLJPanel implements GLEventListener {

	private static final long serialVersionUID = -6824913720967100278L;

	private H3DModel model;

	public CustomH3DPreview() {
		super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
		super.addGLEventListener(this);
		new FPSAnimator(this, 60).start();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
	}

	public void loadModel(H3DModel model) {
		this.model = model;
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		if (model != null) {
			float maxTransl0 = Math.max(0.5f * model.maxVector.x - model.minVector.x, 0.5f * model.maxVector.y - model.minVector.y);
			gl.glTranslatef((model.minVector.x + model.maxVector.x)/-2f, -model.maxVector.y / 2f, -maxTransl0 * 3f - 5f);
//			gl.glRotatef(90f, 0.0f, 1.0f, 0.0f);

			for (int i = 0; i < model.meshes.size(); i++) { //direct rendering to bypass translation
				model.meshes.get(i).uploadVBO(gl);
				model.meshes.get(i).render(gl, (model.materials.size() > model.meshes.get(i).materialId) ? model.materials.get(model.meshes.get(i).materialId) : null);
			}
		}
		gl.glFlush();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		if (height <= 0) {
			height = 1;
		}
		float h = (float) width / (float) height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		new GLU().gluPerspective(45.0f, h, 1f, 15000.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

}
