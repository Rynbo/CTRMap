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

/**
 * Listens to user input interfaces and forwards any interaction to CM3D.
 */
public class CM3DInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener, KeyListener {

	private int originMouseX;
	private int originMouseY;
	private float originScaleX;
	private float originScaleY;
	private float originRotateX;
	private float originRotateY;
	private float originRotateZ;
	private float speed = 10f;
	private ArrayList<Integer> keycodes = new ArrayList<>();
	private boolean navi = false;

	@Override
	public void mouseClicked(MouseEvent e) {
		m3DDebugPanel.cycleSelection(e);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!m3DDebugPanel.hasFocus()) {
			m3DDebugPanel.requestFocus();
		}
		navi = m3DDebugPanel.checkNavi(e);
		setOrigins(e);
	}

	public void setOrigins(MouseEvent e) {
		originMouseX = e.getX();
		originMouseY = e.getY();
		originScaleX = m3DDebugPanel.translateX;
		originScaleY = m3DDebugPanel.translateY;
		originRotateX = m3DDebugPanel.rotateX;
		originRotateY = m3DDebugPanel.rotateY;
		originRotateZ = m3DDebugPanel.rotateZ;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		navi = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			m3DDebugPanel.translateX = originScaleX + (e.getX() - originMouseX);
			m3DDebugPanel.translateY = originScaleY - (e.getY() - originMouseY);
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			if (!navi) {
				m3DDebugPanel.rotateY = (originRotateY + (e.getX() - originMouseX) / 2f) % 360f;
				m3DDebugPanel.rotateX = ((float) Math.max(-90f, Math.min(90f, originRotateX + (e.getY() - originMouseY) / 2f))) % 360f;
			}
			else {
				m3DDebugPanel.doNavi(e, originMouseX, originMouseY);
			}
			setOrigins(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		speed = Math.max(-e.getWheelRotation() + speed, 2f);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!keycodes.contains(e.getKeyCode())) {
			keycodes.add(e.getKeyCode());
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
					Thread continuousUpdateThreadW = new Thread() {
						public void run() {
							long start;
							while (keycodes.contains(KeyEvent.VK_W)) {
								try {
									start = System.currentTimeMillis();
									m3DDebugPanel.translateX -= Math.sin(Math.toRadians(m3DDebugPanel.rotateY)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(m3DDebugPanel.rotateX)))) * speed;
									m3DDebugPanel.translateZ += Math.cos(Math.toRadians(m3DDebugPanel.rotateY)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(m3DDebugPanel.rotateX)))) * speed;
									m3DDebugPanel.translateY += Math.sin(Math.toRadians(m3DDebugPanel.rotateX)) * speed;
									Thread.sleep(10); //better than being tied to the framerate eh?
								} catch (InterruptedException ex) {
								}
							}
						}
					};
					continuousUpdateThreadW.start();
					break;
				case KeyEvent.VK_S:
					Thread continuousUpdateThreadS = new Thread() {
						public void run() {
							long start;
							while (keycodes.contains(KeyEvent.VK_S)) {
								try {
									start = System.currentTimeMillis();
									m3DDebugPanel.translateX += Math.sin(Math.toRadians(m3DDebugPanel.rotateY)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(m3DDebugPanel.rotateX)))) * speed;
									m3DDebugPanel.translateZ -= Math.cos(Math.toRadians(m3DDebugPanel.rotateY)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(m3DDebugPanel.rotateX)))) * speed;
									m3DDebugPanel.translateY -= Math.sin(Math.toRadians(m3DDebugPanel.rotateX)) * speed;
									Thread.sleep(10);
								} catch (InterruptedException ex) {
								}
							}
						}
					};
					continuousUpdateThreadS.start();
					break;
				case KeyEvent.VK_A:
					Thread continuousUpdateThreadA = new Thread() {
						public void run() {
							long start;
							while (keycodes.contains(KeyEvent.VK_A)) {
								try {
									start = System.currentTimeMillis();
									m3DDebugPanel.translateX -= Math.sin(Math.toRadians(m3DDebugPanel.rotateY - 90f)) * speed;
									m3DDebugPanel.translateZ += Math.cos(Math.toRadians(m3DDebugPanel.rotateY - 90f)) * speed;
									Thread.sleep(10);
								} catch (InterruptedException ex) {
								}
							}
						}
					};
					continuousUpdateThreadA.start();
					break;
				case KeyEvent.VK_D:
					Thread continuousUpdateThreadD = new Thread() {
						public void run() {
							long start;
							while (keycodes.contains(KeyEvent.VK_D)) {
								try {
									start = System.currentTimeMillis();
									m3DDebugPanel.translateX += Math.sin(Math.toRadians(m3DDebugPanel.rotateY - 90f)) * speed;
									m3DDebugPanel.translateZ -= Math.cos(Math.toRadians(m3DDebugPanel.rotateY - 90f)) * speed;
									Thread.sleep(10);
								} catch (InterruptedException ex) {
								}
							}
						}
					};
					continuousUpdateThreadD.start();
					break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		while (keycodes.contains(e.getKeyCode())) {
			keycodes.remove((Integer) e.getKeyCode());
		}
	}

}
