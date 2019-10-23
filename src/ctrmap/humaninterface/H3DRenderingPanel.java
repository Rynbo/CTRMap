package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import ctrmap.CtrmapMainframe;

import ctrmap.Utils;
import ctrmap.Workspace;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.vectors.Vec3f;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * CM3D OpenGL surface and glue.
 */
public class H3DRenderingPanel extends GLJPanel implements GLEventListener {

	private static final long serialVersionUID = -6824913620967100278L;
	public BCHFile bch;
	public float translateZ = 0f;
	public float translateX = 0f;
	public float translateY = 0f;

	public float rotateY = 0f;
	public float rotateX = 0f;
	public float rotateZ = 0f;

	public float[] mvMatrix = new float[16];
	public float[] projMatrix = new float[16];
	public int[] view = new int[4];

	public CM3DNavigator navi = new CM3DNavigator(this);
	public List<CM3DRenderable> CM3DComponents = new ArrayList<>();
	
	public boolean reload = false;

	public H3DRenderingPanel(List<CM3DRenderable> slaves) {
		super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
		CM3DComponents = slaves;
		super.addGLEventListener(this);
		new FPSAnimator(this, 60).start();
	}

	public void loadH3D(BCHFile file) {
		bch = file;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.setSwapInterval(1);
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		/*gl.glCullFace(GL2.GL_BACK);
		gl.glEnable(GL2.GL_CULL_FACE);*/
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		CM3DComponents.forEach((r) -> {
			r.uploadBuffers(gl);
		});
		reload = false;
	}

	public void cycleSelection(MouseEvent e) { //this method would largely benefit from abstractization of the edit forms as it has two unnecessary copy-pasted ifs. On the other hand, I actually like this way more than constant type casting like ((form.getObjectClass()) form.objects.get(i)). Too imprecise for reading, in a SPICA kind of way.
		BufferedImage out = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
		Graphics g = out.getGraphics();
		g.setColor(Color.BLUE);
		CM3DComponents.forEach((t) -> {
			t.doSelectionLoop(e, this, mvMatrix, projMatrix, view, new Vec3f(translateX, translateY, translateZ));
		});
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		if (Workspace.valid) {
			GL2 gl = drawable.getGL().getGL2();
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			gl.glLoadIdentity();
			gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(rotateZ, 0.0f, 0.0f, 1.0f);
			gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f);
			gl.glTranslatef(translateX, translateY, translateZ);

			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvMatrix, 0);
			gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projMatrix, 0);
			gl.glGetIntegerv(GL2.GL_VIEWPORT, view, 0);

			if (reload) {
				CM3DComponents.forEach((r) -> {
					r.uploadBuffers(gl);
				});
				reload = false;
			}
			CM3DComponents.forEach((r) -> {
				r.renderCM3D(gl);
			});
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
			CM3DComponents.forEach((r) -> {
				r.renderOverlayCM3D(gl);
			});

			if (CtrmapMainframe.tool.getNaviEnabled()) {
				navi.renderNavigator(gl);
			}

			gl.glFlush();
		}
	}

	public void bindNavi(MapObject o) {
		navi.bindModel(o);
	}

	public boolean checkNavi(MouseEvent e) {
		if (navi.target == null) {
			return false;
		}
		float[][] Xbox = navi.ax.boxVectors;
		if (Utils.isBoxSelected(Xbox, e, this, new Vec3f(navi.ax.worldLocX, navi.ax.worldLocY, navi.ax.worldLocZ), new Vec3f(navi.scale, navi.scale, navi.scale), new Vec3f(0f, 0f, 0f), mvMatrix, projMatrix, view)) {
			navi.targetAxis = CM3DNavigator.TargetAxis.X;
			return true;
		}
		float[][] Ybox = navi.ay.boxVectors;
		if (Utils.isBoxSelected(Ybox, e, this, new Vec3f(navi.ay.worldLocX, navi.ay.worldLocY, navi.ay.worldLocZ), new Vec3f(navi.scale, navi.scale, navi.scale), new Vec3f(0f, 0f, 0f), mvMatrix, projMatrix, view)) {
			navi.targetAxis = CM3DNavigator.TargetAxis.Y;
			return true;
		}
		float[][] Zbox = navi.az.boxVectors;
		if (Utils.isBoxSelected(Zbox, e, this, new Vec3f(navi.az.worldLocX, navi.az.worldLocY, navi.az.worldLocZ), new Vec3f(navi.scale, navi.scale, navi.scale), new Vec3f(0f, 0f, 0f), mvMatrix, projMatrix, view)) {
			navi.targetAxis = CM3DNavigator.TargetAxis.Z;
			return true;
		}

		navi.targetAxis = CM3DNavigator.TargetAxis.NULL;

		return false;
	}

	public void doNavi(MouseEvent e, int originMouseX, int originMouseY) {
		double xAddition;
		double yAddition;
		if (navi.target == null) {
			return;
		}
		switch (navi.targetAxis) {
			case X:
				xAddition = Math.cos(Math.toRadians(rotateY)) * (e.getX() - originMouseX);
				yAddition = Math.cos(Math.toRadians(rotateY + 90f)) * (e.getY() - originMouseY);
				navi.target.setX(navi.target.getX() + (float) (navi.distFromTarget / 1200f * (xAddition + yAddition)));
				break;
			case Z:
				xAddition = Math.cos(Math.toRadians(rotateY - 90f)) * (e.getX() - originMouseX);
				yAddition = Math.cos(Math.toRadians(rotateY)) * (e.getY() - originMouseY);
				navi.target.setZ(navi.target.getZ() + (float) (navi.distFromTarget / 1200f * (xAddition + yAddition)));
				break;
			case Y:
				yAddition = Math.cos(Math.toRadians(rotateX)) * (e.getY() - originMouseY);
				navi.target.setY(navi.target.getY() + (float) (navi.distFromTarget / 1200f * (-yAddition)));
				break;
		}
		navi.synchronizeNavi();
		CtrmapMainframe.tool.updateComponents();
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
