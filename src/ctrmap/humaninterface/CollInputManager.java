package ctrmap.humaninterface;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import static ctrmap.CtrmapMainframe.*;

public class CollInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener{
	private int originMouseX;
	private int originMouseY;
	private float originScaleX;
	private float originScaleY;
	private float originRotateX;
	private float originRotateY;
	private float originRotateZ;
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
		originScaleX = mGLPanel.scaleX;
		originScaleY = mGLPanel.scaleY;
		originRotateX = mGLPanel.rotateX;
		originRotateY = mGLPanel.rotateY;
		originRotateZ = mGLPanel.rotateZ;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			mGLPanel.scaleX = originScaleX + (e.getX() - originMouseX);
			mGLPanel.scaleY = originScaleY - (e.getY() - originMouseY);
		}
		else if (SwingUtilities.isLeftMouseButton(e)) {
			mGLPanel.rotateY = originRotateY + (e.getX() - originMouseX)/2f;
			float rotateX = mGLPanel.rotateY;
			float rotateX180multi = (float)Math.floor(rotateX/180f);
			float rotateXmed = (rotateX - rotateX180multi*180f)/180f;
			float backwardsMultiplier1 = (((int)Math.round(rotateX/180f) & 1) == 0) ? 1 : -1;
			float backwardsMultiplier2 = (((int)Math.round((rotateX + 90f)/180f) & 1) == 0) ? -1 : 1;
			if (rotateXmed > 0.5f) rotateXmed = 1f - rotateXmed;
			mGLPanel.rotateZ = originRotateZ + backwardsMultiplier2*rotateXmed*(e.getY() - originMouseY)/2f;
			mGLPanel.rotateX = originRotateX + backwardsMultiplier1*(0.5f - rotateXmed)*(e.getY() - originMouseY)/2f;
			setOrigins(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mGLPanel.scale -= e.getWheelRotation()*20f;
	}

}
