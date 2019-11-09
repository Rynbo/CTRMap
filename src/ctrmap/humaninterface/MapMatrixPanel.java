package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.formats.mapmatrix.MapMatrix;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

public class MapMatrixPanel extends JPanel {

	public MapMatrix mm;

	public MapMatrixPanel() {
		super();
	}

	public void loadMatrix(MapMatrix mm) {
		this.mm = mm;
		mMtxEditForm.loadMatrix(mm);
	}

	public int getFullImageWidth(){
		if (mm == null){
			return 0;
		}
		else {
			return mm.width * 100;
		}
	}
	
	public int getFullImageHeight(){
		if (mm == null){
			return 0;
		}
		else {
			return mm.height * 100;
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (mm != null) {
			int imgstartx = (this.getWidth() - getFullImageWidth()) / 2;
			int imgstarty = (this.getHeight() - getFullImageHeight()) / 2;
			for (int x = 0; x < mm.width; x++) {
				for (int y = 0; y < mm.height; y++) {
					g.setColor(Color.BLACK);
					int regionX = x * 100 + imgstartx;
					int regionY = y * 100 + imgstarty;
					g.drawRect(regionX, regionY, 100, 100);
				}
			}
			mMtxEditForm.drawToolGraphics(g, imgstartx, imgstarty);
		}
	}
}
