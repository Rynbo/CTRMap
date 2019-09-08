package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import com.jogamp.opengl.util.FPSAnimator;
import com.sun.javafx.geom.Vec3f;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.model.H3DVertex;
import ctrmap.formats.propdata.GRProp;
import ctrmap.formats.zone.ZoneEntities;
import ctrmap.humaninterface.tools.NPCTool;
import ctrmap.humaninterface.tools.PropTool;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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

	public CM3DNavigator navi = new CM3DNavigator();

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
		BufferedImage out = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
		Graphics g = out.getGraphics();
		g.setColor(Color.BLUE);
		GLU glu = new GLUgl2();
		double closestDist = Float.MAX_VALUE;
		int closestIdx = -1;
		if (mTileEditForm.tool instanceof PropTool) {
			for (int i = 0; i < mPropEditForm.models.size(); i++) {
				if (mPropEditForm.models.get(i) == null) {
					continue;
				}
				GRProp p = mPropEditForm.props.props.get(i);
				float[][] box = mPropEditForm.models.get(i).boxVectors;
				if (isBoxSelected(box, e, new Vec3f(p.x, p.y, p.z), new Vec3f(p.scaleX, p.scaleY, p.scaleZ), new Vec3f(p.rotateX, p.rotateY, p.rotateZ))) {
					H3DModel m = mPropEditForm.models.get(i);
					//GLU is buggy and sometimes completely fucks up the maths in certain camera angles. We can work around this by checking if the actual object is seen by the camera.
					boolean allow = false;
					for (int mesh = 0; mesh < m.meshes.size(); mesh++) {
						for (int vertex = 0; vertex < m.meshes.get(mesh).vertices.size(); vertex++) {
							H3DVertex v = m.meshes.get(mesh).vertices.get(vertex);
							float[] test = new float[3];
							glu.gluProject(v.position.x + p.x, v.position.y + p.y, v.position.z + p.z, mvMatrix, 0, projMatrix, 0, view, 0, test, 0);
							if (test[0] > 0 && test[0] < getWidth() && test[1] > 0 && test[1] < getHeight()) {
								allow = true;
								break;
							}
						}
						if (allow) {
							break;
						}
					}
					if (!allow) {
						continue;
					}
					Vec3f dummyCenterVector = new Vec3f(p.x, p.y, p.z);
					double dist = getDistanceFromCamera(dummyCenterVector);
					if (Math.abs(dist) < closestDist && i != mPropEditForm.propIndex) {
						closestDist = Math.abs(dist);
						closestIdx = i;
					}
				}
			}
			if (closestIdx != -1) {
				mPropEditForm.setProp(closestIdx);
			}
		} else if (mTileEditForm.tool instanceof NPCTool) {
			for (int i = 0; i < mNPCEditForm.models.size(); i++) {
				if (mNPCEditForm.models.get(i) == null) {
					continue;
				}
				ZoneEntities.NPC npc = mNPCEditForm.e.npcs.get(i);
				float[][] box = mNPCEditForm.models.get(i).boxVectors;
				if (isBoxSelected(box, e, new Vec3f(npc.getX(), npc.getY(), npc.getZ()), new Vec3f(1f, 1f, 1f), new Vec3f(0f, mNPCEditForm.get3DOrientation(npc.faceDirection), 0f))) {
					H3DModel m = mNPCEditForm.models.get(i);
					//GLU is buggy and sometimes completely fucks up the maths in certain camera angles. We can work around this by checking if the actual object is seen by the camera.
					boolean allow = false;
					for (int mesh = 0; mesh < m.meshes.size(); mesh++) {
						for (int vertex = 0; vertex < m.meshes.get(mesh).vertices.size(); vertex++) {
							H3DVertex v = m.meshes.get(mesh).vertices.get(vertex);
							float[] test = new float[3];
							glu.gluProject(v.position.x + npc.getX(), v.position.y + npc.getY(), v.position.z + npc.getZ(), mvMatrix, 0, projMatrix, 0, view, 0, test, 0);
							if (test[0] > 0 && test[0] < getWidth() && test[1] > 0 && test[1] < getHeight()) {
								allow = true;
								break;
							}
						}
						if (allow) {
							break;
						}
					}
					if (!allow) {
						continue;
					}
					Vec3f dummyCenterVector = new Vec3f(npc.getX(), npc.getY(), npc.getZ());
					double dist = getDistanceFromCamera(dummyCenterVector);
					if (Math.abs(dist) < closestDist && i != mNPCEditForm.npcIndex) {
						closestDist = Math.abs(dist);
						closestIdx = i;
					}
				}
			}
			if (closestIdx != -1) {
				mNPCEditForm.setNPC(closestIdx);
			}
		}
	}

	public boolean isBoxSelected(float[][] box, MouseEvent e, Vec3f position, Vec3f scale, Vec3f rotate) {
		GLUgl2 glu = new GLUgl2();
		float[][] winPosArray = new float[box.length][3];
		for (int j = 0; j < box.length; j++) {
			Vec3f vec = new Vec3f(box[j][0] * scale.x, box[j][1] * scale.y, box[j][2] * scale.z);
			Vec3f rotatedVec = Utils.noGlRotatef(Utils.noGlRotatef(Utils.noGlRotatef(vec, 
					new Vec3f(0f, 1f, 0f), Math.toRadians(rotate.y)), 
					new Vec3f(1f, 0f, 0f), Math.toRadians(rotate.x)), 
					new Vec3f(0f, 0f, 1f), Math.toRadians(rotate.z));
			glu.gluProject(rotatedVec.x + position.x, rotatedVec.y + position.y, rotatedVec.z + position.z, mvMatrix, 0, projMatrix, 0, view, 0, winPosArray[j], 0);
			winPosArray[j][1] = getHeight() - winPosArray[j][1];
		}
		for (int j = 0; j < winPosArray.length; j += 4) {
			Polygon polygon = new Polygon();
			for (int k = 0; k < 4; k++) {
				polygon.addPoint((int) winPosArray[j + k][0], (int) winPosArray[j + k][1]);
			}
			if (polygon.contains(e.getPoint())) {
				//GLU is buggy and sometimes completely fucks up the maths in certain camera angles. We can work around this by checking if the actual object is seen by the camera.
				return true;
			}
		}
		return false;
	}

	public double getDistanceFromCamera(Vec3f loc) {
		double dist = Math.pow((Math.pow(loc.x + translateX, 2)
				+ Math.pow(loc.y + translateY, 2)
				+ Math.pow(loc.z + translateZ, 2)
				* 1.0), 0.5);
		return Math.abs(dist);
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

			CM3DComponents.forEach((r) -> {
				r.renderCM3D(gl);
			});
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
			CM3DComponents.forEach((r) -> {
				r.renderOverlayCM3D(gl);
			});

			if (mTileEditForm.tool instanceof PropTool || mTileEditForm.tool instanceof NPCTool) {
				navi.renderNavigator(gl);
			}

			gl.glFlush();
			gl.glFinish();
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
		if (isBoxSelected(Xbox, e, new Vec3f(navi.ax.worldLocX, navi.ax.worldLocY, navi.ax.worldLocZ), new Vec3f(navi.scale, navi.scale, navi.scale), new Vec3f(0f, 0f, 0f))) {
			navi.targetAxis = CM3DNavigator.TargetAxis.X;
			return true;
		}
		float[][] Ybox = navi.ay.boxVectors;
		if (isBoxSelected(Ybox, e, new Vec3f(navi.ay.worldLocX, navi.ay.worldLocY, navi.ay.worldLocZ), new Vec3f(navi.scale, navi.scale, navi.scale), new Vec3f(0f, 0f, 0f))) {
			navi.targetAxis = CM3DNavigator.TargetAxis.Y;
			return true;
		}
		float[][] Zbox = navi.az.boxVectors;
		if (isBoxSelected(Zbox, e, new Vec3f(navi.az.worldLocX, navi.az.worldLocY, navi.az.worldLocZ), new Vec3f(navi.scale, navi.scale, navi.scale), new Vec3f(0f, 0f, 0f))) {
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
				mTileEditForm.tool.updateComponents();
				navi.synchronizeNavi();
				break;
			case Z:
				xAddition = Math.cos(Math.toRadians(rotateY - 90f)) * (e.getX() - originMouseX);
				yAddition = Math.cos(Math.toRadians(rotateY)) * (e.getY() - originMouseY);
				navi.target.setZ(navi.target.getZ() + (float) (navi.distFromTarget / 1200f * (xAddition + yAddition)));
				mTileEditForm.tool.updateComponents();
				navi.synchronizeNavi();
				break;
			case Y:
				yAddition = Math.cos(Math.toRadians(rotateX)) * (e.getY() - originMouseY);
				navi.target.setY(navi.target.getY() + (float) (navi.distFromTarget / 1200f * (-yAddition)));
				mTileEditForm.tool.updateComponents();
				navi.synchronizeNavi();
				break;
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
