package ctrmap.humaninterface.tools;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

public abstract class AbstractTool {
	public AbstractTool() {
		this.onToolInit();
	}
	public abstract void onToolInit();
	public abstract void onToolShutdown();
	public abstract void fireCancel();
	public abstract void drawOverlay(Graphics g, int imgstartx, int imgstarty, double globimgdim);
	public abstract boolean getSelectorEnabled();
	public abstract void onTileClick(MouseEvent e);
	public abstract void onTileMouseDown(MouseEvent e);
	public abstract void onTileMouseUp(MouseEvent e);
	public abstract void onTileMouseDragged(MouseEvent e);
}
