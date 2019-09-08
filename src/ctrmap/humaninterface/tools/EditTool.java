package ctrmap.humaninterface.tools;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.humaninterface.Selector;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class EditTool extends AbstractTool {

	@Override
	public void onToolInit() {
		Utils.switchToolUI(mTileEditForm);
		mTileEditForm.lockTile(false);
	}

	@Override
	public void onTileClick(MouseEvent e) {
		if (!SwingUtilities.isRightMouseButton(e)) {
			Selector.acqCurTile();
		}
	}

	@Override
	public void onTileMouseDown(MouseEvent e) {
		//this tool has no power here
	}

	@Override
	public void onTileMouseUp(MouseEvent e) {
		//and neither does it here
	}

	@Override
	public void onTileMouseDragged(MouseEvent e) {
		
	}

	@Override
	public void onToolShutdown() {
		mTileEditForm.lockTile(false);
		Selector.unfocus();
	}

	@Override
	public void fireCancel() {
		mTileEditForm.lockTile(false);
		Selector.unfocus();
	}

	@Override
	public void drawOverlay(Graphics g, int imgstartx, int imgstarty, double globimgdim) {}

	@Override
	public boolean getSelectorEnabled() {
		return true;
	}

	@Override
	public void updateComponents() {
	}
}
