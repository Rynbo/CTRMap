package ctrmap.humaninterface;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import static ctrmap.CtrmapMainframe.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CollInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener, KeyListener{
	private int originMouseX;
	private int originMouseY;
	private float originScaleX;
	private float originScaleY;
	private float originRotateX;
	private float originRotateY;
	private float originRotateZ;
	private ArrayList<Integer> keycodes = new ArrayList<>();
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
			m3DDebugPanel.translateX = originScaleX + (e.getX() - originMouseX);
			m3DDebugPanel.translateY = originScaleY - (e.getY() - originMouseY);
		}
		else if (SwingUtilities.isLeftMouseButton(e)) {
			mGLPanel.rotateY = originRotateY + (e.getX() - originMouseX)/2f;
			m3DDebugPanel.rotateY = originRotateY + (e.getX() - originMouseX)/2f;
			float rotateX = mGLPanel.rotateY;
			float rotateX180multi = (float)Math.floor(rotateX/180f);
			float rotateXmed = (rotateX - rotateX180multi*180f)/180f;
			float backwardsMultiplier1 = (((int)Math.round(rotateX/180f) & 1) == 0) ? 1 : -1;
			float backwardsMultiplier2 = (((int)Math.round((rotateX + 90f)/180f) & 1) == 0) ? -1 : 1;
			if (rotateXmed > 0.5f) rotateXmed = 1f - rotateXmed;
			mGLPanel.rotateZ = originRotateZ + backwardsMultiplier2*rotateXmed*(e.getY() - originMouseY)/2f;
			mGLPanel.rotateX = originRotateX + backwardsMultiplier1*(0.5f - rotateXmed)*(e.getY() - originMouseY)/2f;
			m3DDebugPanel.rotateZ = originRotateZ + backwardsMultiplier2*rotateXmed*(e.getY() - originMouseY)/2f;
			m3DDebugPanel.rotateX = originRotateX + backwardsMultiplier1*(0.5f - rotateXmed)*(e.getY() - originMouseY)/2f;
			setOrigins(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mGLPanel.scale -= e.getWheelRotation()*20f;
		m3DDebugPanel.translateZ -= e.getWheelRotation()*20f;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!keycodes.contains(e.getKeyCode())){
			keycodes.add(e.getKeyCode());
		}
		switch (e.getKeyCode()){
			case KeyEvent.VK_W:
				Thread continuousUpdateThreadW = new Thread(){
					public void run(){
						long start;
						while (keycodes.contains(KeyEvent.VK_W)){
							try {
								start = System.currentTimeMillis();
								m3DDebugPanel.translateX -= Math.sin(Math.toRadians(m3DDebugPanel.rotateY))*10f;
								m3DDebugPanel.translateZ += Math.cos(Math.toRadians(m3DDebugPanel.rotateY))*10f;
								m3DDebugPanel.translateY += Math.cos(Math.toRadians(m3DDebugPanel.rotateZ))*10f;
								Thread.sleep(50 - Math.min(0, (System.currentTimeMillis() - start)));
							} catch (InterruptedException ex) {}
						}
					}
				};
				continuousUpdateThreadW.start();
				break;
			case KeyEvent.VK_S:
				Thread continuousUpdateThreadS = new Thread(){
					public void run(){
						long start;
						while (keycodes.contains(KeyEvent.VK_S)){
							try {
								start = System.currentTimeMillis();
								m3DDebugPanel.translateZ -= 10;
								Thread.sleep(50 - (System.currentTimeMillis() - start));
							} catch (InterruptedException ex) {}
						}
					}
				};
				continuousUpdateThreadS.start();
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		while (keycodes.contains(e.getKeyCode())){
			keycodes.remove((Integer)e.getKeyCode());
		}
	}

}
