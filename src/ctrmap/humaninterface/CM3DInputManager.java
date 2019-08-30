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

public class CM3DInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener, KeyListener{
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
		originScaleX = m3DDebugPanel.scaleX;
		originScaleY = m3DDebugPanel.scaleY;
		originRotateX = m3DDebugPanel.rotateX;
		originRotateY = m3DDebugPanel.rotateY;
		originRotateZ = m3DDebugPanel.rotateZ;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			m3DDebugPanel.scaleX = originScaleX + (e.getX() - originMouseX);
			m3DDebugPanel.scaleY = originScaleY - (e.getY() - originMouseY);
		}
		else if (SwingUtilities.isLeftMouseButton(e)) {
			m3DDebugPanel.rotateY = (originRotateY + (e.getX() - originMouseX)/2f) % 360f;
			float rotateX = m3DDebugPanel.rotateY;
			float rotateX180multi = (float)Math.floor(rotateX/180f);
			float rotateXmed = (rotateX - rotateX180multi*180f)/180f;
			float backwardsMultiplier1 = (((int)Math.round(rotateX/180f) & 1) == 0) ? 1 : -1;
			float backwardsMultiplier2 = (((int)Math.round((rotateX + 90f)/180f) & 1) == 0) ? -1 : 1;
			if (rotateXmed > 0.5f) rotateXmed = 1f - rotateXmed;
			//m3DDebugPanel.rotateZ = Math.min(180f, originRotateZ + backwardsMultiplier2*rotateXmed*(e.getY() - originMouseY)/2f);
			//m3DDebugPanel.rotateX = Math.min(180f, originRotateX + backwardsMultiplier1*(0.5f - rotateXmed)*(e.getY() - originMouseY)/2f);
			//m3DDebugPanel.rotateZ = (float)Math.min(180f, originRotateZ + Math.sin(Math.toRadians(rotateX)) * (e.getY() - originMouseY)/2f);
			m3DDebugPanel.rotateX = ((float)Math.min(180f, originRotateX + (e.getY() - originMouseY)/2f)) % 360f;
			setOrigins(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		m3DDebugPanel.scale -= e.getWheelRotation()*20f;
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
								m3DDebugPanel.scaleX -= Math.sin(Math.toRadians(m3DDebugPanel.rotateY))*10f;
								m3DDebugPanel.scale += Math.cos(Math.toRadians(m3DDebugPanel.rotateY))*10f;
								m3DDebugPanel.scaleY += Math.sin(Math.toRadians(m3DDebugPanel.rotateX))*10f;
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
								m3DDebugPanel.scaleX += Math.sin(Math.toRadians(m3DDebugPanel.rotateY))*10f;
								m3DDebugPanel.scale -= Math.cos(Math.toRadians(m3DDebugPanel.rotateY))*10f;
								m3DDebugPanel.scaleY -= Math.sin(Math.toRadians(m3DDebugPanel.rotateX))*10f;
								Thread.sleep(50 - Math.min(0, (System.currentTimeMillis() - start)));
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
