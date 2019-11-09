package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;

/**
 * Matrix panel cursor.
 */
public class MatrixSelector {

	public static int hilightRegionX = -1;
	public static int hilightRegionY = -1;
	public static int selRegionX = -1;
	public static int selRegionY = -1;

	public static boolean selecting = false;
	public static boolean selectSubChunks = false;

	public static void select(int xOnImage, int yOnImage) {
		hilightRegionX = (int) (Math.floor(((float) xOnImage / mMtxPanel.getFullImageWidth()) * (double) (mMtxPanel.mm.width * (selectSubChunks ? 4 : 1))));
		hilightRegionY = (int) (Math.floor(((float) yOnImage / mMtxPanel.getFullImageHeight()) * (double) (mMtxPanel.mm.height * (selectSubChunks ? 4 : 1))));
	}

	public static void acqCurTile() {
		selRegionX = hilightRegionX;
		selRegionY = hilightRegionY;
		mMtxEditForm.showRegion(selRegionX, selRegionY);
		mMtxPanel.repaint();
	}

	public static void deselect() {
		hilightRegionX = -1;
		hilightRegionY = -1;
	}

	public static void unfocus() {
		selRegionX = -1;
		selRegionY = -1;
	}
}
