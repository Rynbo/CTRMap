package ctrmap.humaninterface.tools;

import java.awt.event.MouseEvent;
import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.propdata.GRProp;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class PropTool extends AbstractTool {
	
	private boolean dragging = false;
	private boolean isDownOnProp = false;
	private double xshift = 0;
	private double yshift = 0;
	
	@Override
	public void onToolInit() {
		Utils.switchToolUI(mPropEditForm);
		mPropEditForm.saveAndRefresh();
	}

	@Override
	public void onToolShutdown() {}

	@Override
	public void fireCancel() {
	}

	@Override
	public void drawOverlay(Graphics g, int imgstartx, int imgstarty, double globimgdim){
		if (mTileMapPanel.isVerified && mPropEditForm.loaded){
			for (int i = 0; i < mPropEditForm.props.props.size(); i++){
				GRProp prop = mPropEditForm.props.props.get(i);
				double transformFrom720Space = 400d/720d;
				g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int)globimgdim));
				int textWidth = g.getFontMetrics().stringWidth(prop.name);
				int x = (int)(imgstartx + prop.x * mTileMapPanel.tilemapScale * transformFrom720Space - textWidth/2d);
				int y = (int)(imgstarty + prop.z * mTileMapPanel.tilemapScale * transformFrom720Space);
				prop.nameWidth = textWidth;
				prop.nameHeight = (int)globimgdim;
				g.setColor(Color.WHITE);
				g.fillRect(x, y, textWidth, (int)globimgdim + 2);
				g.setColor((i == mPropEditForm.propIndex) ? Color.RED : Color.BLACK);
				g.drawRect(x, y, textWidth, (int)globimgdim + 2);
				g.setColor(Color.BLACK);
				g.drawString(prop.name, x, y + (int)globimgdim + 1);
				if (dragging && mPropEditForm.propIndex == i){
					g.setColor(Color.RED);
					g.drawLine(0, y, imgstartx + mTileMapPanel.tilemapScaledImage.getWidth(), y);
					g.drawLine((int)(x + textWidth/2d), 0, (int)(x + textWidth/2d), imgstarty + mTileMapPanel.tilemapScaledImage.getHeight());
				}
			}
		}
	}
	
	@Override
	public void onTileClick(MouseEvent e) {
	}

	@Override
	public void onTileMouseDown(MouseEvent e) {
		if (mPropEditForm.loaded){
			for (int i = 0; i < mPropEditForm.props.props.size(); i++){
				GRProp prop = mPropEditForm.props.props.get(i);
				int imgstartx = (mTileMapPanel.getWidth() - mTileMapPanel.tilemapScaledImage.getWidth()) / 2;
				int imgstarty = (mTileMapPanel.getHeight() - mTileMapPanel.tilemapScaledImage.getHeight()) / 2;
				double xBase = prop.x * 400d/720d * mTileMapPanel.tilemapScale + imgstartx;
				double yBase = prop.z * 400d/720d * mTileMapPanel.tilemapScale + imgstarty;
				if (e.getX() > xBase - prop.nameWidth/2 && e.getX() < xBase + prop.nameWidth/2 && e.getY() > yBase && e.getY() < yBase + prop.nameHeight){
					isDownOnProp = true;
					xshift = 2*(xBase - e.getX()) / mTileMapPanel.tilemapScale; //xBase is in the center
					yshift = (yBase - e.getY()) / mTileMapPanel.tilemapScale;
					mPropEditForm.setProp(i);
					break;
				}
			}
		}
	}

	@Override
	public void onTileMouseUp(MouseEvent e) {
		dragging = false;
		isDownOnProp = false;
		frame.repaint();
	}

	@Override
	public void onTileMouseDragged(MouseEvent e) {
		if (mPropEditForm.prop == null || !mPropEditForm.loaded || !isDownOnProp) return;
		dragging = true;
		int imgstartx = (mTileMapPanel.getWidth() - mTileMapPanel.tilemapScaledImage.getWidth()) / 2;
		int imgstarty = (mTileMapPanel.getHeight() - mTileMapPanel.tilemapScaledImage.getHeight()) / 2;
		mPropEditForm.prop.x = (float)(xshift + e.getX() * (720f/400f) / mTileMapPanel.tilemapScale - imgstartx);
		mPropEditForm.prop.z = (float)(yshift + e.getY() * (720f/400f) / mTileMapPanel.tilemapScale - imgstarty);
		mPropEditForm.props.modified = true;
		mPropEditForm.showProp(mPropEditForm.propIndex);
		mTileMapPanel.renderTileMap();
	}

	@Override
	public boolean getSelectorEnabled() {
		return false;
	}

	@Override
	public void updateComponents() {
		mPropEditForm.props.modified = true;
		mPropEditForm.showProp(mPropEditForm.propIndex);
	}
	
	@Override
	public boolean getNaviEnabled() {
		return true;
	}
}
