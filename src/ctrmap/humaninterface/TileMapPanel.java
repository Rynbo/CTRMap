package ctrmap.humaninterface;

import ctrmap.CtrmapMainframe;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.Tilemap;
import ctrmap.formats.GR;
import ctrmap.formats.cameradata.CameraData;
import ctrmap.formats.mapmatrix.MapMatrix;
import ctrmap.formats.propdata.GRPropData;
import ctrmap.formats.zone.ZoneEntities;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class TileMapPanel extends JPanel {

	private static final long serialVersionUID = 7357107275764622829L;

	public static final String PROP_IMGSTATE = "imageUpdated";
	private static final String ESC = "keyEscape";

	public ViewportMode mode = ViewportMode.SINGLE;

	public Tilemap[][] tilemaps;
	public MapMatrix mm;
	public BufferedImage tilemapImage;// = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	public BufferedImage tilemapScaledImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	public double tilemapScale = 1.0d;
	public boolean loaded = false;
	private final JLabel placeholder = new JLabel("No map loaded");
	public boolean isVerified = false;
	private Graphics g;
	public int width;
	public int height;

	public enum ViewportMode {
		SINGLE,
		MULTI
	}

	public TileMapPanel() {
		super();
		setLayout(new GridBagLayout());
		add(placeholder);
		g = tilemapScaledImage.getGraphics();
		this.addMouseWheelListener(mTilemapInputManager);
		this.addMouseMotionListener(mTilemapInputManager);
		this.addMouseListener(mTilemapInputManager);
	}

	public void loadTileMap(GR file) {
		if (!saveTileMap(true)) {
			return;
		}
		mm = null;
		mode = ViewportMode.SINGLE;
		width = 40;
		height = 40;
		remove(placeholder);
		tilemaps = new Tilemap[1][1];
		tilemaps[0][0] = new Tilemap(file);
		tilemapImage = tilemaps[0][0].getImage();
		tilemapScaledImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(400, 400);
		scaleImage(1);
		frame.revalidate();
		loaded = true;
		isVerified = false;
		loadProps();
	}

	public void loadProps() {
		if (mode == ViewportMode.MULTI) {
			GRPropData comb = new GRPropData();
			for (int i = 0; i < mm.height; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (mm.regions[j][i] != null) {
						comb.props.addAll(new GRPropData(mm.regions[j][i]).props);
					}
				}
			}
			mPropEditForm.loadDataFile(comb);
		} else if (mode == ViewportMode.SINGLE) {
			mPropEditForm.loadDataFile(new GRPropData(mainGR));
		}
	}

	public void unload() {
		mode = ViewportMode.SINGLE;
		loaded = false;
		isVerified = false;
		add(placeholder);
		setPreferredSize(placeholder.getPreferredSize());
		invalidate();
		frame.revalidate();
		mm = null;
		mainGR = null;
		tilemaps = null;
	}

	public void verifyCompat() {
		for (int i = 0; i < mCamEditForm.f.camData.size(); i++) {
			CameraData cam = mCamEditForm.f.camData.get(i);
			if (cam.boundX1 > width * 40 || cam.boundY1 > height * 40) {
				JOptionPane.showMessageDialog(this, "The loaded AreaData refers to coordinates that exceeded the current matrix.", "Rendering error", JOptionPane.ERROR_MESSAGE);
				isVerified = false;
				return;
			}
		}
		isVerified = true;
	}

	public boolean saveMatrix(boolean dialog) {
		if (mm != null) {
			boolean broken = false;
			for (int i = 0; i < mm.height & !broken; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (tilemaps[j][i] == null) {
						continue;
					}
					if (tilemaps[j][i].modified) {
						int result = (dialog) ? Utils.showSaveConfirmationDialog("Region data") : JOptionPane.YES_OPTION;
						switch (result) {
							case JOptionPane.YES_OPTION:
								LoadingDialog progress = LoadingDialog.makeDialog("Saving matrix");
								SwingWorker worker = new SwingWorker() {
									@Override
									protected void done() {
										progress.close();
									}

									@Override
									protected Object doInBackground() {
										for (int i = 0; i < mm.height; i++) {
											for (int j = 0; j < mm.width; j++) {
												if (mm.regions[j][i] == null) {
													continue;
												}
												tilemaps[j][i].modified = false; //prevent save dialog popping up until changed again
												mm.regions[j][i].storeFile(0, tilemaps[j][i].assembleTilemap());
												progress.setBarPercent((int) ((i * j + j) / (float) (mm.width * mm.height) * 100));
											}
										}
										return null;
									}
								};

								worker.execute();
								progress.show();

								try {
									worker.get();
								} catch (InterruptedException | ExecutionException ex) {
									Logger.getLogger(TileMapPanel.class.getName()).log(Level.SEVERE, null, ex);
								}
								return true; //save as normal
							case JOptionPane.NO_OPTION:
								for (int k = 0; k < mm.height; k++) {
									for (int l = 0; l < mm.width; l++) {
										if (mm.regions[l][k] == null) {
											continue;
										}
										tilemaps[l][k].modified = false; //prevent save dialog popping up until changed again
									}
								}
								return true; //don't save, but don't interrupt the editor
							default:
								return false;//stop anything going on in the editor, cancel
						}
					}
				}
			}
		}
		return true; //no matrix, no fuss
	}

	public void loadMatrix(MapMatrix matrix) {
		LoadingDialog progress = LoadingDialog.makeDialog("Loading matrix");
		SwingWorker worker = new SwingWorker() {
			@Override
			protected void done() {
				progress.close();
			}

			@Override
			protected Object doInBackground() {
				mode = ViewportMode.MULTI;
				mm = matrix;
				width = mm.width * 40;
				height = mm.height * 40;
				tilemaps = new Tilemap[mm.width][mm.height];
				for (int i = 0; i < mm.height; i++) {
					for (int j = 0; j < mm.width; j++) {
						if (mm.ids[j][i] != -1) {
							tilemaps[j][i] = new Tilemap(mm.regions[j][i]);
						}
						progress.setBarPercent((int) ((i * j + j) / (float) (mm.width * mm.height) * 100));
					}
				}
				remove(placeholder);
				invalidate();
				frame.revalidate();
				progress.setDescription("Checking compatibility");
				if (mCamEditForm.f != null) {
					verifyCompat();
				}
				progress.setDescription("Preparing viewport");
				loaded = true;
				loadProps();
				scaleImage(1);
				return null;
			}
		};
		worker.execute();
		progress.show();
		try {
			worker.get();
		} catch (InterruptedException | ExecutionException ex) {
			Logger.getLogger(TileMapPanel.class
					.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean saveTileMap(boolean dialog) {
		if (loaded) {
			if (mode == ViewportMode.MULTI) {
				return saveMatrix(dialog);
			} else {
				if (tilemaps[0][0].modified && dialog) {
					int ret = Utils.showSaveConfirmationDialog("Tilemap");
					switch (ret) {
						case JOptionPane.YES_OPTION:
							mainGR.storeFile(0, tilemaps[0][0].assembleTilemap());
						case JOptionPane.NO_OPTION:
							tilemaps[0][0].modified = false;
							return true;
						default:
							return false;
					}
				}
			}
		}
		return true;
	}

	public void renderTileMap() {
		tilemapScaledImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage((int) (width * 10 * tilemapScale), (int) (height * 10 * tilemapScale));
		g = tilemapScaledImage.getGraphics();
		g.setColor(new Color(0xffffff));
		g.fillRect(0, 0, width * 10, height * 10);
		int regionSize = (int) (Math.round(400 * tilemapScale));
		if (mode == ViewportMode.SINGLE) {
			g.drawImage(tilemaps[0][0].getImage(), 0, 0, (int) (400 * tilemapScale), (int) (400 * tilemapScale), null);
		} else if (mode == ViewportMode.MULTI) {
			for (int i = 0; i < mm.height; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (tilemaps[j][i] != null) {
						//we stretch the regions a few pixels to cover gaps when zooming
						g.drawImage(tilemaps[j][i].getImage(), (int) (Math.round(400d * tilemapScale * j)), (int) (Math.round(400d * tilemapScale * i)), regionSize + 1, regionSize, null);
					}
				}
			}
		}
	}

	public Tilemap getRegionForTile(int x, int y) {
		if (tilemaps == null) {
			return null;
		}
		return tilemaps[x / 40][y / 40];
	}

	public Point getRawAtViewportCentre() {
		int imgstartx = (this.getWidth() - tilemapScaledImage.getWidth()) / 2;
		int imgstarty = (this.getHeight() - tilemapScaledImage.getHeight()) / 2;
		int WPStartX = (int) Math.round(mTilemapScrollPane.getViewport().getViewPosition().x - imgstartx);
		int WPStartY = (int) Math.round(mTilemapScrollPane.getViewport().getViewPosition().y - imgstarty);
		int xFromWPStart = (int) Math.round(mTilemapScrollPane.getViewport().getWidth() / 2);
		int yFromWPStart = (int) Math.round(mTilemapScrollPane.getViewport().getHeight() / 2);
		return new Point(WPStartX + xFromWPStart, WPStartY + yFromWPStart);
	}

	public Point getWorldLocAtViewportCentre() {
		Point raw = getRawAtViewportCentre();
		return new Point((int) Math.round(raw.x / 400d * 720d / tilemapScale), (int) Math.round(raw.y / 400d * 720d / tilemapScale));
	}

	public Point getTileAtViewportCentre() {
		double globimgdim = tilemapScaledImage.getHeight() / (double) height;
		Point raw = getRawAtViewportCentre();
		return new Point((int) Math.round(raw.x / globimgdim), (int) Math.round(raw.y / globimgdim));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (loaded) {
			int imgstartx = (this.getWidth() - tilemapScaledImage.getWidth()) / 2;
			int imgstarty = (this.getHeight() - tilemapScaledImage.getHeight()) / 2;
			int scrollpanex = mTilemapScrollPane.getViewport().getViewPosition().x;
			int scrollpaney = mTilemapScrollPane.getViewport().getViewPosition().y;
			int scrollpanew = mTilemapScrollPane.getViewport().getSize().width;
			int scrollpaneh = mTilemapScrollPane.getViewport().getSize().height;
			if (scrollpanex + scrollpanew > tilemapScaledImage.getWidth()) {
				scrollpanew = tilemapScaledImage.getWidth() - scrollpanex;
			}
			if (scrollpaney + scrollpaneh > tilemapScaledImage.getHeight()) {
				scrollpaneh = tilemapScaledImage.getHeight() - scrollpaney;
			}
			BufferedImage crop = tilemapScaledImage.getSubimage(scrollpanex, scrollpaney, scrollpanew, scrollpaneh);
			/**
			 * Originally, I've tried using the full drawImage method (with
			 * source and dest. coordinates) to only draw a part of the full
			 * image. That resulted in occasional slowdowns (cause unknown) in
			 * paintComponent about 1/10 times when using SetTool, resulting in
			 * major lag. Cropping the image into memory takes less than a
			 * millisecond (depending on the hardware) and results in an overall
			 * pleasant mapping experience.
			 */
			g.drawImage(crop, imgstartx + scrollpanex, imgstarty + scrollpaney, null);
			double globimgdim = tilemapScaledImage.getHeight() / (double) height;
			int gidround = (int) Math.round(globimgdim);
			mTileEditForm.tool.drawOverlay(g, imgstartx, imgstarty, globimgdim);
			g.setColor(Color.RED);
			if (mTileEditForm.tool.getSelectorEnabled()) {
				if (Selector.hilightTileX != -1) {
					g.drawRect(imgstartx + (int) Math.round(Selector.hilightTileX * globimgdim), imgstarty + (int) Math.round(Selector.hilightTileY * globimgdim), gidround, gidround);
				}
				if (Selector.selTileX != -1) {
					g.drawRect(imgstartx + (int) Math.round(Selector.selTileX * globimgdim), imgstarty + (int) Math.round(Selector.selTileY * globimgdim), gidround, gidround);
				}
			}
		}
	}

	public void updateAll() {
		LoadingDialog progress = LoadingDialog.makeDialog("Updating tilemap(s)");
		SwingWorker worker = new SwingWorker() {
			@Override
			protected void done() {
				progress.close();
			}

			@Override
			protected Object doInBackground() {
				if (tilemaps == null) {
					return null;
				}
				for (int i = 0; i < tilemaps.length; i++) { //can't use matrix when not loaded
					for (int j = 0; j < tilemaps[i].length; j++) {
						if (tilemaps[i][j] != null) {
							tilemaps[i][j].updateImage();
						}
						progress.setBarPercent((int) ((i * j + j) / (float) (tilemaps.length * tilemaps[i].length) * 100));
					}
				}
				mTileMapPanel.scaleImage(mTileMapPanel.tilemapScale);
				return null;
			}
		};
		worker.execute();
		progress.show();
		try {
			worker.get();
		} catch (InterruptedException ex) {
			Logger.getLogger(TileMapPanel.class
					.getName()).log(Level.SEVERE, null, ex);
		} catch (ExecutionException ex) {
			Logger.getLogger(TileMapPanel.class
					.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void scaleImage(double scale) {
		if (loaded && scale > 0.05f && scale <= 1f) {
			tilemapScale = scale;
			renderTileMap();
			this.setPreferredSize(new Dimension(tilemapScaledImage.getWidth(), tilemapScaledImage.getHeight()));
			this.invalidate();
			mTilemapScrollPane.revalidate();
			mTilemapScrollPane.repaint();
		}
	}

	public void perfScale(double scale, int changedRegionX, int changedRegionY) {
		if (loaded && scale > 0.05f && scale <= 1f) {
			tilemapScale = scale;
			this.setPreferredSize(new Dimension(tilemapScaledImage.getWidth(), tilemapScaledImage.getHeight()));
			g = tilemapScaledImage.getGraphics();
			int regionSize = (int) (Math.round(400 * tilemapScale));
			g.drawImage(tilemaps[changedRegionX][changedRegionY].getImage(), (int) (Math.round(400d * tilemapScale * changedRegionX)), (int) (Math.round(400d * tilemapScale * changedRegionY)), regionSize + 1, regionSize, null);
			mTilemapScrollPane.repaint();
		}
	}
}
