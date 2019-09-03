package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.formats.gfcollision.GRCollisionFile;
import ctrmap.formats.h3d.BCHFile;

public class H3DRenderingPanel extends GLJPanel implements GLEventListener {

	private static final long serialVersionUID = -6824913620967100278L;
	public BCHFile bch;
	public float translateZ = 0f;
	public float translateX = 0f;
	public float translateY = 0f;

	public float rotateY = 0f;
	public float rotateX = 0f;
	public float rotateZ = 0f;

	public H3DRenderingPanel() {
		super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
		super.addGLEventListener(this);
		FPSAnimator animator = new FPSAnimator(this, 75, true);
		animator.start();
		this.addMouseWheelListener(mCM3DInputManager);
		this.addMouseMotionListener(mCM3DInputManager);
		this.addMouseListener(mCM3DInputManager);
		this.addKeyListener(mCM3DInputManager);
	}

	public void loadH3D(BCHFile file) {
		bch = file;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		if (mTileMapPanel.loaded) {
			GL2 gl = drawable.getGL().getGL2();
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			gl.glLoadIdentity();
			gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(rotateZ, 0.0f, 0.0f, 1.0f);
			gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f);
			gl.glTranslatef(translateX, translateY, translateZ);
			
			mTileMapPanel.renderH3D(gl);
			mPropEditForm.renderH3D(gl);
			mNPCEditForm.renderH3D(gl);
			
			gl.glFlush();
			gl.glFinish();
		}
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

		new GLU().gluPerspective(45.0f, h, 50f, 15000.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
}
