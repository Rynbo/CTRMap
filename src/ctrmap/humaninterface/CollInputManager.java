package ctrmap.humaninterface;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Moves the camera in Collision Editor with user input.
 */
public class CollInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener, KeyListener{
	private int originMouseX;
	private int originMouseY;
	private float originScaleX;
	private float originScaleY;
	private float originRotateX;
	private float originRotateY;
	private float originRotateZ;
	
	private GLPanel parent;
	
	public CollInputManager(GLPanel parent){
		this.parent = parent;
		parent.addMouseWheelListener(this);
		parent.addMouseMotionListener(this);
		parent.addMouseListener(this);
		parent.addKeyListener(this);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		setOrigins(e);
	}
	public void setOrigins(MouseEvent e) {
		originMouseX = e.getX();
		originMouseY = e.getY();
		originScaleX = parent.scaleX;
		originScaleY = parent.scaleY;
		originRotateX = parent.rotateX;
		originRotateY = parent.rotateY;
		originRotateZ = parent.rotateZ;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			parent.scaleX = originScaleX + (e.getX() - originMouseX);
			parent.scaleY = originScaleY - (e.getY() - originMouseY);
		}
		else if (SwingUtilities.isLeftMouseButton(e)) {
			parent.rotateY = originRotateY + (e.getX() - originMouseX)/2f;
			float rotateX = parent.rotateY;
			float rotateX180multi = (float)Math.floor(rotateX/180f);
			float rotateXmed = (rotateX - rotateX180multi*180f)/180f;
			float backwardsMultiplier1 = ((Math.round(rotateX/180f) & 1) == 0) ? 1 : -1;
			float backwardsMultiplier2 = ((Math.round((rotateX + 90f)/180f) & 1) == 0) ? -1 : 1;
			if (rotateXmed > 0.5f) rotateXmed = 1f - rotateXmed;
			parent.rotateZ = originRotateZ + backwardsMultiplier2*rotateXmed*(e.getY() - originMouseY)/2f;
			parent.rotateX = originRotateX + backwardsMultiplier1*(0.5f - rotateXmed)*(e.getY() - originMouseY)/2f;
			setOrigins(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		parent.scale -= e.getWheelRotation()*20f;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
