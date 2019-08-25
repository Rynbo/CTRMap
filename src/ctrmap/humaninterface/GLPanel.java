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
import ctrmap.formats.GR;
import ctrmap.formats.gfcollision.GRCollisionFile;

public class GLPanel extends GLJPanel implements GLEventListener{
	private static final long serialVersionUID = -6824913620967100278L;
	public GRCollisionFile coll;
	public float scale = -960f;
	public float scaleX = 0f;
	public float scaleY = 0f;
	
	public float rotateY = 0f;
	public float rotateX = 0f;
	public float rotateZ = 0f;
	
	public GLPanel() {
            super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
            super.addGLEventListener(this);
	    FPSAnimator animator = new FPSAnimator(this, 300, true);
	    animator.start();
            this.addMouseWheelListener(mCollInputManager);
            this.addMouseMotionListener(mCollInputManager);
            this.addMouseListener(mCollInputManager);
	}
	
	public void loadCollision(GR file) {
            coll = new GRCollisionFile(file);
            mCollEditPanel.loadCollision(coll);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();
            gl.glShadeModel(GL2.GL_SMOOTH);
	    gl.glClearColor(0f, 0f, 0f, 0f);
	    gl.glClearDepth(1.0f);
	    gl.glEnable(GL2.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL2.GL_LEQUAL);
	    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
	}
	@Override
	public void dispose(GLAutoDrawable drawable) {}
	
	@Override
	public void display(GLAutoDrawable drawable) {
	    if (coll != null) {
	    	GL2 gl = drawable.getGL().getGL2();
                    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT); 
                    gl.glLoadIdentity(); // Reset The View
                    gl.glTranslatef(scaleX, scaleY, scale); // Move the triangle
                    gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f);
                    gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f);
                    gl.glRotatef(rotateZ, 0.0f, 0.0f, 1.0f);
		    gl.glBegin(GL2.GL_TRIANGLES); 
		      
		    coll.render(gl);
		    
		    gl.glEnd();
		    gl.glFlush();
	    }
	}
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            GL2 gl = drawable.getGL().getGL2();
	    if( height <= 0 ) height = 1;	
	    float h = ( float ) width / ( float ) height;
	    gl.glViewport( 0, 0, width, height );
	    gl.glMatrixMode( GL2.GL_PROJECTION );
	    gl.glLoadIdentity();
			
	    new GLU().gluPerspective( 45.0f, h, 50f, 15000.0 );
	    gl.glMatrixMode( GL2.GL_MODELVIEW );
	    gl.glLoadIdentity();
	}
}
