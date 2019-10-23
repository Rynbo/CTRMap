package ctrmap.humaninterface.tools;

import ctrmap.CtrmapMainframe;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

/**
 * Abstract class of CM2D editor plug-in tools.
 */
public abstract class AbstractTool {

	public AbstractTool() {
		//Always unbind navi on tool switching so that it's not persistent between e.g. an NPC editor and a prop editor with no data
		CtrmapMainframe.m3DDebugPanel.bindNavi(null);
		this.onToolInit();
	}

	public abstract void onToolInit();

	public abstract void onToolShutdown();

	public abstract void fireCancel();

	public abstract void drawOverlay(Graphics g, int imgstartx, int imgstarty, double globimgdim);

	public abstract boolean getSelectorEnabled();

	public abstract boolean getNaviEnabled();

	public abstract void onTileClick(MouseEvent e);

	public abstract void onTileMouseDown(MouseEvent e);

	public abstract void onTileMouseUp(MouseEvent e);

	public abstract void onTileMouseDragged(MouseEvent e);

	public abstract void updateComponents();
	public boolean CM2DNoUpdate = false;
}
