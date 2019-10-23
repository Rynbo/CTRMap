package ctrmap.humaninterface.tools;

import ctrmap.CtrmapMainframe;
import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.humaninterface.Selector;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class FillTool extends AbstractTool {

	public byte[] actTileData = new byte[4];
	public int originX = -1;
	public int originY = -1;
	public int lastX = -1;
	public int lastY = -1;
	public boolean locked = false;
	public boolean dragging;

	@Override
	public void onToolInit() {
		Utils.switchToolUI(mTileEditForm);
		mTileEditForm.makeTile();
		mTileEditForm.lockTile(true);
	}

	@Override
	public void onToolShutdown() {
		originX = -1;
		originY = -1;
		lastX = -1;
		lastY = -1;
		Selector.unfocus();
		mTileEditForm.lockTile(false);
		locked = false;
	}

	@Override
	public void fireCancel() {
		mTileEditForm.makeTile();
		locked = false;
	}

	@Override
	public void drawOverlay(Graphics g, int imgstartx, int imgstarty, double globimgdim) {
		if ((dragging || locked) && originX != -1) {
			int dX;
			int dY;
			if (Selector.hilightTileX == -1 || locked) {
				dX = lastX;
				dY = lastY;
			} else {
				dX = Selector.hilightTileX;
				dY = Selector.hilightTileY;
				lastX = dX;
				lastY = dY;
			}
			int oX = originX;
			int oY = originY;
			int x = Math.min(oX, dX);
			int y = Math.min(oY, dY);
			int w = Math.abs(dX - oX);
			int h = Math.abs(dY - oY);
			mTileEditForm.setTileLabel("New tile " + x + "x" + y + " - " + (x + w) + "x" + (y + h));
			g.setColor(Color.RED);
			g.drawRect(imgstartx + (int) Math.round(x * globimgdim), imgstarty + (int) Math.round(y * globimgdim), (int) Math.round((w + 1) * globimgdim), (int) Math.round((h + 1) * globimgdim));
		} else {
			mTileEditForm.setTileLabel("New tile");
		}
	}
	
	@Override
	public void onTileClick(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			Selector.acqCurTile();
		}
	}

	@Override
	public void onTileMouseDown(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			locked = false;
			originX = Selector.hilightTileX;
			originY = Selector.hilightTileY;
			dragging = true;
			CM2DNoUpdate = true;
		}
	}

	@Override
	public void onTileMouseUp(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			locked = true;
			dragging = false;
			CM2DNoUpdate = false;
		}
	}

	@Override
	public void onTileMouseDragged(MouseEvent e) {

	}

	@Override
	public boolean getSelectorEnabled() {
		return true;
	}

	@Override
	public void updateComponents() {
	}
	
	@Override
	public boolean getNaviEnabled() {
		return false;
	}
}