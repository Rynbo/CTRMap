package ctrmap.humaninterface.tools;

import java.util.Arrays;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.tilemap.Tilemap;
import ctrmap.humaninterface.Selector;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class SetTool extends AbstractTool {
	public byte[] actTileData = new byte[4];
	@Override
	public void onToolInit() {
		Utils.switchToolUI(mTileEditForm);
		mTileEditForm.makeTile();
		mTileEditForm.lockTile(true);
	}
	
	@Override
	public void onTileClick(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			Selector.acqCurTile();
		}
		else{
			updateTile();
		}
	}

	@Override
	public void onTileMouseDown(MouseEvent e) {
	}

	@Override
	public void onTileMouseUp(MouseEvent e) {
		CM2DNoUpdate = false;
	}

	@Override
	public void onTileMouseDragged(MouseEvent e) {
		if (!SwingUtilities.isRightMouseButton(e)){
			CM2DNoUpdate = true;
			updateTile();
		}
	}
	
	private void updateTile(){
		if (Selector.hilightTileX != -1) {
			Tilemap tm = mTileMapPanel.getRegionForTile(Selector.hilightTileX, Selector.hilightTileY);
			if (tm == null){
				return;
			}
			if (!Arrays.equals(tm.getTileData(Selector.hilightTileX % 40, Selector.hilightTileY % 40), actTileData)) {
				tm.setTileData(Selector.hilightTileX % 40, Selector.hilightTileY % 40, actTileData);
				tm.updateImage();
				mTileMapPanel.perfScale(mTileMapPanel.tilemapScale, Selector.hilightTileX / 40, Selector.hilightTileY / 40);
			}
		}
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
	
	@Override
	public boolean getNaviEnabled() {
		return false;
	}
}
