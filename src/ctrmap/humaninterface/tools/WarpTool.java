package ctrmap.humaninterface.tools;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.zone.ZoneEntities;
import ctrmap.humaninterface.Selector;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

public class WarpTool extends AbstractTool {

	private boolean isDownOnWarp = false;

	@Override
	public void onToolInit() {
		Utils.switchToolUI(mWarpEditForm);
	}

	@Override
	public void onToolShutdown() {
	}

	@Override
	public void fireCancel() {
	}

	@Override
	public void drawOverlay(Graphics g, int imgstartx, int imgstarty, double globimgdim) {
		int gidround = (int) Math.round(globimgdim);
		for (int i = 0; i < mWarpEditForm.e.warpCount; i++) {
			ZoneEntities.Warp warp = mWarpEditForm.e.warps.get(i);
			int xdraw = imgstartx + (int) Math.round(globimgdim * ((warp.x - 9f) / 18f));
			int ydraw = imgstarty + (int) Math.round(globimgdim * ((warp.y - 9f) / 18f));
			int w = (int) Math.round(globimgdim * warp.w);
			int h = (int) Math.round(globimgdim * warp.h);
			g.setColor(Color.WHITE);
			g.fillRect(xdraw, ydraw, w, h);
			g.setColor((mWarpEditForm.warp == warp) ? Color.RED : Color.BLACK);
			g.drawRect(xdraw, ydraw, w, h);
			g.setColor(Color.BLACK);
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, gidround));
			g.drawString(String.valueOf(i), xdraw + 1, ydraw + gidround - 1);
		}
	}

	@Override
	public void onTileClick(MouseEvent e
	) {
		if (mWarpEditForm.loaded) {
			for (int i = 0; i < mWarpEditForm.e.warpCount; i++) {
				ZoneEntities.Warp warp = mWarpEditForm.e.warps.get(i);
				int imgstartx = (mTileMapPanel.getWidth() - mTileMapPanel.tilemapScaledImage.getWidth()) / 2;
				int imgstarty = (mTileMapPanel.getHeight() - mTileMapPanel.tilemapScaledImage.getHeight()) / 2;
				double xBase = (warp.x - 9f) * 400d / 720d * mTileMapPanel.tilemapScale + imgstartx;
				double yBase = (warp.y - 9f) * 400d / 720d * mTileMapPanel.tilemapScale + imgstarty;
				double width = (warp.w * 18f) * 400d / 720d * mTileMapPanel.tilemapScale;
				double height = (warp.h * 18f) * 400d / 720d * mTileMapPanel.tilemapScale;
				if (e.getX() > xBase && e.getX() < xBase + width && e.getY() > yBase && e.getY() < yBase + height) {
					mWarpEditForm.showEntry(i);
					frame.repaint();
					break;
				}
			}
		}
	}

	@Override
	public void onTileMouseDown(MouseEvent e
	) {
		if (mWarpEditForm.loaded) {
			for (int i = 0; i < mWarpEditForm.e.warpCount; i++) {
				ZoneEntities.Warp warp = mWarpEditForm.e.warps.get(i);
				int imgstartx = (mTileMapPanel.getWidth() - mTileMapPanel.tilemapScaledImage.getWidth()) / 2;
				int imgstarty = (mTileMapPanel.getHeight() - mTileMapPanel.tilemapScaledImage.getHeight()) / 2;
				double xBase = (warp.x - 9f) * 400d / 720d * mTileMapPanel.tilemapScale + imgstartx;
				double yBase = (warp.y - 9f) * 400d / 720d * mTileMapPanel.tilemapScale + imgstarty;
				double width = (warp.w * 18f) * 400d / 720d * mTileMapPanel.tilemapScale;
				double height = (warp.h * 18f) * 400d / 720d * mTileMapPanel.tilemapScale;
				if (e.getX() > xBase && e.getX() < xBase + width && e.getY() > yBase && e.getY() < yBase + height) {
					mWarpEditForm.setWarp(i);
					isDownOnWarp = true;
					frame.repaint();
					break;
				}
			}
		}
	}

	@Override
	public void onTileMouseUp(MouseEvent e
	) {
		isDownOnWarp = false;
		frame.repaint();
	}

	@Override
	public void onTileMouseDragged(MouseEvent e
	) {
		if (mWarpEditForm.warp == null || !mWarpEditForm.loaded || !isDownOnWarp || Selector.hilightTileX == -1) {
			return;
		}
		mWarpEditForm.warp.x = Selector.hilightTileX * 18 + 9;
		mWarpEditForm.warp.y = Selector.hilightTileY * 18 + 9;
		mWarpEditForm.e.modified = true;
		mWarpEditForm.refresh();
	}

	@Override
	public boolean getSelectorEnabled() {
		return false;
	}

	@Override
	public void updateComponents() {
		mCamEditForm.showCamera(mCamEditForm.camIndex, false);
	}

	@Override
	public boolean getNaviEnabled() {
		return false;
	}
}
