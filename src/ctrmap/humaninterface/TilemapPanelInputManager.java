package ctrmap.humaninterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.humaninterface.tools.CameraTool;
import ctrmap.humaninterface.tools.EditTool;
import ctrmap.humaninterface.tools.FillTool;
import ctrmap.humaninterface.tools.NPCTool;
import ctrmap.humaninterface.tools.PropTool;
import ctrmap.humaninterface.tools.SetTool;

public class TilemapPanelInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener, ActionListener {

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mTileMapPanel.scaleImage(mTileMapPanel.tilemapScale - e.getWheelRotation() / 10f);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		moveSelector(e);
		mTileEditForm.tool.onTileMouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		moveSelector(e);
	}

	private void moveSelector(MouseEvent e) {
		int imagestartX =(mTileMapPanel.getWidth() - mTileMapPanel.tilemapScaledImage.getWidth()) / 2;
		int xbound = (int) (mTileMapPanel.getLocationOnScreen().getX() + (mTileMapPanel.getWidth() - mTileMapPanel.tilemapScaledImage.getWidth()) / 2);
		int ybound = (int) (mTileMapPanel.getLocationOnScreen().getY() + (mTileMapPanel.getHeight() - mTileMapPanel.tilemapScaledImage.getHeight()) / 2);
		if (e.getXOnScreen() >= xbound && e.getXOnScreen() < xbound + mTileMapPanel.tilemapScaledImage.getWidth()
				&& e.getYOnScreen() >= ybound && e.getYOnScreen() < ybound + mTileMapPanel.tilemapScaledImage.getHeight()) {
			Selector.select(e.getXOnScreen() - xbound, e.getYOnScreen() - ybound);
		} else {
			if (Selector.hilightTileX != -1) {
				Selector.deselect();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (Selector.hilightTileX == -1) {
			mTileEditForm.tool.fireCancel();
		}
		mTileEditForm.tool.onTileClick(e);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mTileEditForm.tool.onTileMouseDown(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mTileEditForm.tool.onTileMouseUp(e);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		mTileEditForm.tool.onToolShutdown();
		boolean switchCam = false;
		switch (e.getActionCommand()) {
			case ("edit"):
				mTileEditForm.tool = new EditTool();
				currentTool.setText("Current tool: Edit");
				break;
			case ("set"):
				mTileEditForm.tool = new SetTool();
				currentTool.setText("Current tool: Set");
				break;
			case ("fill"):
				mTileEditForm.tool = new FillTool();
				currentTool.setText("Current tool: Fill");
				break;
			case ("cam"):
				mTileEditForm.tool = new CameraTool();
				currentTool.setText("Current tool: Camera");
				break;
			case ("prop"):
				mTileEditForm.tool = new PropTool();
				currentTool.setText("Current tool: Prop");
				break;
			case ("npc"):
				mTileEditForm.tool = new NPCTool();
				currentTool.setText("Current tool: NPC");
				break;
		}
	}
}
