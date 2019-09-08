package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.formats.tilemap.Tilemap;

/**
 * CM2D cursor.
 */
public class Selector {
	public static int hilightTileX = -1;
	public static int hilightTileY = -1;
	public static int selTileX = -1;
	public static int selTileY = -1;
	
	public static void select(int xOnImage, int yOnImage) {
		if (mTileMapPanel.loaded) {
			hilightTileX = (int)(Math.floor(((float)xOnImage/mTileMapPanel.tilemapScaledImage.getWidth())*(double)(mTileMapPanel.width)));
            hilightTileY = (int)(Math.floor(((float)yOnImage/mTileMapPanel.tilemapScaledImage.getHeight())*(double)(mTileMapPanel.height)));
			mTileEditForm.showTile(hilightTileX, hilightTileY, false);
			mTileMapPanel.firePropertyChange(TileMapPanel.PROP_IMGSTATE, false, true);
		}
	}
	
	public static void acqCurTile() {
		Tilemap reg = mTileMapPanel.getRegionForTile(hilightTileX, hilightTileY);
		if (reg == null){
			return;
		}
		if (hilightTileX != -1) {
			if (hilightTileX == selTileX && hilightTileY == selTileY) {
				mTileEditForm.lockTile(false);
				selTileX = -1;
				selTileY = -1;
				return;
			}
			selTileX = hilightTileX;
			selTileY = hilightTileY;
			mTileEditForm.lockTile(false);
			mTileEditForm.showTile(selTileX, selTileY, false);
			mTileEditForm.lockTile(true);
		}
		else {
			selTileX = -1;
			selTileY = -1;
			mTileEditForm.lockTile(false);
		}
		mTileMapPanel.firePropertyChange(TileMapPanel.PROP_IMGSTATE, false, true);
	}
	
	public static void deselect() {
		hilightTileX = -1;
		hilightTileY = -1;
		mTileMapPanel.firePropertyChange(TileMapPanel.PROP_IMGSTATE, false, true);
	}
	public static void unfocus() {
		selTileX = -1;
		selTileY = -1;
		mTileMapPanel.firePropertyChange(TileMapPanel.PROP_IMGSTATE, false, true);
	}
}
