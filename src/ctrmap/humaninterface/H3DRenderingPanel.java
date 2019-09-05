package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.FPSAnimator;
import com.sun.javafx.geom.Vec3f;
import com.sun.javafx.geom.Vec4f;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.model.H3DVertex;
import ctrmap.formats.propdata.GRProp;
import ctrmap.formats.zone.ZoneEntities;
import ctrmap.humaninterface.tools.NPCTool;
import ctrmap.humaninterface.tools.PropTool;
import java.awt.event.MouseEvent;
import java.util.Arrays;

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

	public void cycleSelection(MouseEvent e) { //this method would largely benefit from abstractization of the edit forms as it has two unnecessary copy-pasted ifs. On the other hand, I actually like this way more than constant type casting like ((form.getObjectClass()) form.objects.get(i)). Too imprecise for reading, in a SPICA kind of way.
		GLU glu = new GLUgl2();
		int closestDist = Integer.MAX_VALUE;
		int closestIdx = -1;
		if (mTileEditForm.tool instanceof PropTool) {
			for (int i = 0; i < mPropEditForm.models.size(); i++) {
				if (mPropEditForm.models.get(i) == null) continue;
				GRProp p = mPropEditForm.props.props.get(i);
				for (int j = 0; j < mPropEditForm.models.get(i).meshes.size(); j++) {
					for (int k = 0; k < mPropEditForm.models.get(i).meshes.get(j).vertices.size(); k++) {
						H3DVertex check = mPropEditForm.models.get(i).meshes.get(j).vertices.get(k);
						float[] winPos = new float[3];
						//due to the way CM3D works with repositioning prop models in real time, we need to get the worldLoc from props
						glu.gluProject(check.position.x + p.x, check.position.y + p.y, check.position.z + p.z, mvMatrix, 0, projMatrix, 0, view, 0, winPos, 0);
						int dist = Math.abs((int) (winPos[0]) - e.getX()) + Math.abs((int) (getHeight() - winPos[1]) - e.getY());
						if (dist < closestDist) {
							closestDist = dist;
							closestIdx = i;
						}
					}
				}
			}
			if (closestIdx != -1 && closestDist < 10) {
				mPropEditForm.setProp(closestIdx);
			}
		}
		else if (mTileEditForm.tool instanceof NPCTool){
			for (int i = 0; i < mNPCEditForm.models.size(); i++) {
				if (mNPCEditForm.models.get(i) == null) continue;
				ZoneEntities.NPC npc = mNPCEditForm.e.npcs.get(i);
				for (int j = 0; j < mNPCEditForm.models.get(i).meshes.size(); j++) {
					for (int k = 0; k < mNPCEditForm.models.get(i).meshes.get(j).vertices.size(); k++) {
						H3DVertex check = mNPCEditForm.models.get(i).meshes.get(j).vertices.get(k);
						float[] winPos = new float[3];
						glu.gluProject(check.position.x + npc.xTile * 18f, check.position.y + npc.z3DCoordinate, check.position.z + npc.yTile * 18f, mvMatrix, 0, projMatrix, 0, view, 0, winPos, 0);
						int dist = Math.abs((int) (winPos[0]) - e.getX()) + Math.abs((int) (getHeight() - winPos[1]) - e.getY());
						if (dist < closestDist) {
							closestDist = dist;
							closestIdx = i;
						}
					}
				}
			}
			if (closestIdx != -1 && closestDist < 10) {
				mNPCEditForm.setNPC(closestIdx);
			}
		}
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

			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvMatrix, 0);
			gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projMatrix, 0);
			gl.glGetIntegerv(GL2.GL_VIEWPORT, view, 0);

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
