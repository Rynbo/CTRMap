package ctrmap.humaninterface.tools;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.zone.ZoneEntities;
import ctrmap.humaninterface.Selector;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

public class NPCTool extends AbstractTool {
	
	private boolean dragging = false;
	private boolean isDownOnNPC = false;
	
	@Override
	public void onToolInit() {
		Utils.switchToolUI(mNPCEditForm);
	}
	
	@Override
	public void onToolShutdown() {}
	
	@Override
	public void fireCancel() {
	}
	
	@Override
	public void drawOverlay(Graphics g, int imgstartx, int imgstarty, double globimgdim) {
		int gidround = (int) Math.round(globimgdim);
		if (mNPCEditForm.loaded) {
			for (int i = 0; i < mNPCEditForm.e.NPCCount; i++) {
				ZoneEntities.NPC npc = mNPCEditForm.e.npcs.get(i);
				g.setColor(Color.WHITE);
				g.fillRect(imgstartx + (int) (npc.xTile * globimgdim), imgstarty + (int) (npc.yTile * globimgdim), gidround, gidround);
				g.setColor((mNPCEditForm.npcIndex == npc.uid) ? Color.RED : Color.BLACK);
				g.drawRect(imgstartx + (int) (npc.xTile * globimgdim), imgstarty + (int) (npc.yTile * globimgdim), gidround, gidround);
				g.setColor(Color.BLACK);
				g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, gidround));
				g.drawString(String.valueOf(npc.uid), imgstartx + (int) (npc.xTile * globimgdim) + 1, imgstarty + (int) (npc.yTile * globimgdim) + gidround - 1);
			}
		}
	}
	
	@Override
	public boolean getSelectorEnabled() {
		return true;
	}
	
	@Override
	public void onTileClick(MouseEvent e) {
	}
	
	@Override
	public void onTileMouseDown(MouseEvent e) {
		if (mNPCEditForm.loaded) {
			for (int i = 0; i < mNPCEditForm.e.npcs.size(); i++) {
				ZoneEntities.NPC npc = mNPCEditForm.e.npcs.get(i);
				if (Selector.hilightTileX == npc.xTile && Selector.hilightTileY == npc.yTile) {
					isDownOnNPC = true;
					mNPCEditForm.setNPC(i);
					break;
				}
			}
		}
	}
	
	@Override
	public void onTileMouseUp(MouseEvent e) {
		dragging = false;
		isDownOnNPC = false;
		frame.repaint();
	}
	
	@Override
	public void onTileMouseDragged(MouseEvent e) {
		if (mNPCEditForm.npc == null || !mNPCEditForm.loaded || !isDownOnNPC || Selector.hilightTileX == -1) {
			return;
		}
		dragging = true;
		mNPCEditForm.npc.xTile = Selector.hilightTileX;
		mNPCEditForm.npc.yTile = Selector.hilightTileY;
		mNPCEditForm.e.modified = true;
		mNPCEditForm.refresh();
	}
}
