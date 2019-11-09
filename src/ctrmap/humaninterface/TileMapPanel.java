package ctrmap.humaninterface;

import com.jogamp.opengl.DefaultGLCapabilitiesChooser;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import ctrmap.CtrmapMainframe;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.vectors.Vec3f;
import ctrmap.formats.tilemap.Tilemap;
import ctrmap.formats.containers.GR;
import ctrmap.formats.gfcollision.GRCollisionFile;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.texturing.H3DTexture;
import ctrmap.formats.mapmatrix.MapMatrix;
import ctrmap.formats.propdata.ADPropRegistry;
import ctrmap.formats.propdata.GRPropData;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Originally for editing tilemaps, this has evolved far beyond its purpose.
 * Currently nests everything tied to the map matrix.
 */
public class TileMapPanel extends JPanel implements CM3DRenderable {

	private static final long serialVersionUID = 7357107275764622829L;
	public static final String PROP_REPAINT = "imageUpdated";
	private static final String ESC = "keyEscape";

	public ViewportMode mode = ViewportMode.SINGLE;

	public Tilemap[][] tilemaps;
	public BCHFile[][] models;
	public BCHFile[][] tallgrass;
	public GRCollisionFile[][] colls;
	public MapMatrix mm;
	public GR mainGR;
	public BufferedImage tilemapImage;// = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	public BufferedImage tilemapScaledImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	public BufferedImage cm2dOverlayImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	public double tilemapScale = 1.0d;
	public boolean loaded = false;
	private final JLabel placeholder = new JLabel("No map loaded");
	private Graphics g;
	public int width;
	public int height;

	private final GLProfile glp;
	private final GLCapabilities caps;
	private GLAutoDrawable CM2DDrawable;
	private BufferedImage CM2DTempImage;
	public boolean update = true;

	@Override
	public void doSelectionLoop(MouseEvent e, Component parent, float[] mvMatrix, float[] projMatrix, int[] view, Vec3f cameraVec) {}

	public enum ViewportMode {
		SINGLE,
		MULTI
	}

	public TileMapPanel() {
		super();
		setLayout(new GridBagLayout());
		add(placeholder);
		g = tilemapScaledImage.getGraphics();
		glp = GLProfile.get(GLProfile.GL2);
		caps = new GLCapabilities(glp);
		caps.setHardwareAccelerated(true);
		caps.setDoubleBuffered(false);
		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setBlueBits(8);
		caps.setGreenBits(8);
		caps.setOnscreen(false);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (CM2DDrawable != null) {
					CM3DComponents.forEach((r) -> {
						r.deleteGLInstanceBuffers(CM2DDrawable.getGL().getGL2());
					});
					update = true;
				}
				GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
				CM2DDrawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps, new DefaultGLCapabilitiesChooser(), mTilemapScrollPane.getViewport().getWidth(), mTilemapScrollPane.getViewport().getHeight());
				CM2DDrawable.display();
				CM2DDrawable.getContext().makeCurrent();

				GL2 gl = CM2DDrawable.getGL().getGL2();

				gl.glShadeModel(GL2.GL_SMOOTH);
				gl.glClearColor(0f, 0f, 0f, 0f);
				gl.glClearDepth(1.0f);
				gl.glEnable(GL2.GL_TEXTURE_2D);
				gl.glEnable(GL2.GL_DEPTH_TEST);
				gl.glCullFace(GL2.GL_BACK);
				gl.glEnable(GL2.GL_CULL_FACE);
				gl.glDepthFunc(GL2.GL_LEQUAL);
				gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
			}
		});
	}

	public void loadTileMap(GR file) {
		if (!saveTileMap(true)) {
			return;
		}
		mZonePnl.zone = null;
		mm = null;
		mode = ViewportMode.SINGLE;
		width = 40;
		height = 40;
		remove(placeholder);
		tilemaps = new Tilemap[1][1];
		mainGR = file;
		models = new BCHFile[1][1];
		models[0][0] = new BCHFile(file.getFile(1));
		tallgrass = new BCHFile[1][1];
		byte[] tg = file.getFile(5);
		if (tg[0] == 'B' && tg[1] == 'C' && tg[2] == 'H') {
			BCHFile tgbch = new BCHFile(tg);
			if (!tgbch.models.isEmpty()) {
				H3DModel tgmdl = tgbch.models.get(0);
				tallgrass[0][0] = tgbch;
			}
		}
		colls = new GRCollisionFile[1][1];
		colls[0][0] = new GRCollisionFile(file);
		tilemaps[0][0] = new Tilemap(file);
		tilemapImage = tilemaps[0][0].getImage();
		tilemapScaledImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(400, 400);
		scaleImage(1);
		revalidate();
		loaded = true;
		loadProps(null, null);
		mNPCEditForm.loadFromEntities(null, null);
		m3DDebugPanel.translateX = 0f; //720/2 to center the camera
		m3DDebugPanel.translateY = -360f;
		m3DDebugPanel.translateZ = -720f; //at the end of the map vertically
		m3DDebugPanel.rotateX = 45f;
	}

	public void loadProps(List<H3DTexture> propTextures, ADPropRegistry reg) {
		if (mode == ViewportMode.MULTI) {
			GRPropData comb = new GRPropData();
			for (int i = 0; i < mm.height; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (mm.regions.get(j, i) != null) {
						comb.props.addAll(new GRPropData(mm.regions.get(j, i)).props);
					}
				}
			}
			mPropEditForm.loadDataFile(comb, reg, propTextures);
		} else if (mode == ViewportMode.SINGLE) {
			mPropEditForm.loadDataFile(mainGR, null);
		}
	}

	public void unload() {
		mode = ViewportMode.SINGLE;
		loaded = false;
		add(placeholder);
		setPreferredSize(placeholder.getPreferredSize());
		invalidate();
		revalidate();
		mm = null;
		tilemaps = null;
		models = null;
		tallgrass = null;
		colls = null;
	}

	public boolean saveMatrix(boolean dialog) {
		if (mm != null) {
			boolean broken = false;
			for (int i = 0; i < mm.height & !broken; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (j >= tilemaps.length || i >= tilemaps[j].length || tilemaps[j][i] == null) {
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
												if (mm.regions.get(j, i) == null) {
													continue;
												}
												tilemaps[j][i].modified = false; //prevent save dialog popping up until changed again
												mm.regions.get(j, i).storeFile(0, tilemaps[j][i].assembleTilemap());
												progress.setBarPercent((int) (((i * mm.width + j) / (float) (mm.width * mm.height)) * 100));
											}
										}
										return null;
									}
								};

								worker.execute();
								progress.showDialog();

								try {
									worker.get();
								} catch (InterruptedException | ExecutionException ex) {
									Logger.getLogger(TileMapPanel.class.getName()).log(Level.SEVERE, null, ex);
								}
								return true; //save as normal
							case JOptionPane.NO_OPTION:
								for (int k = 0; k < mm.height; k++) {
									for (int l = 0; l < mm.width; l++) {
										if (mm.regions.get(l, k) == null) {
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

	public void loadMatrix(MapMatrix matrix, ADPropRegistry reg, List<H3DTexture> worldTextures, List<H3DTexture> propTextures) {
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
				models = new BCHFile[mm.width][mm.height];
				tallgrass = new BCHFile[mm.width][mm.height];
				colls = new GRCollisionFile[mm.width][mm.height];
				mCollEditPanel.unload();
				for (int i = 0; i < mm.height; i++) {
					for (int j = 0; j < mm.width; j++) {
						if (mm.ids.get(j, i) != -1) {
							tilemaps[j][i] = new Tilemap(mm.regions.get(j, i));
							byte[] tg = mm.regions.get(j, i).getFile(5);
							if (tg.length > 0 && tg[0] == 'B' && tg[1] == 'C' && tg[2] == 'H') {
								BCHFile tgbch = new BCHFile(tg);
								if (!tgbch.models.isEmpty()) {
									H3DModel tgmdl = tgbch.models.get(0);
									tgmdl.setMaterialTextures(worldTextures);
									//GR overworld map BCH files have just 1 model, the tall grass is entirely separate BCH
									tgmdl.worldLocX = j * 720f + 360f;
									tgmdl.worldLocZ = i * 720f + 360f;
									tgmdl.makeAllBOs();
									tallgrass[j][i] = tgbch;
								}
							}
							BCHFile bch = new BCHFile(mm.regions.get(j, i).getFile(1));
							if (!bch.models.isEmpty()) {
								H3DModel model = bch.models.get(0);
								if (worldTextures != null) {
									worldTextures.addAll(bch.textures);
									model.setMaterialTextures(worldTextures);
								}
								if (propTextures != null) {
									model.setMaterialTextures(propTextures);
								}
								//GR overworld map BCH files have just 1 model, the tall grass is entirely separate BCH
								model.worldLocX = j * 720f + 360f;
								model.worldLocZ = i * 720f + 360f;
								model.makeAllBOs();
								models[j][i] = bch;
							}
							colls[j][i] = new GRCollisionFile(mm.regions.get(j, i));
							mCollEditPanel.loadCollision(colls[j][i], bch.models.get(0).name);
						}
						progress.setBarPercent((int) (((i * mm.width + j) / (float) (mm.width * mm.height)) * 100));
					}
				}
				m3DDebugPanel.translateX = -mm.width * 360f; //720/2 to center the camera
				m3DDebugPanel.translateY = -mm.height * 360f;
				m3DDebugPanel.translateZ = -mm.height * 720f; //at the end of the map vertically
				m3DDebugPanel.rotateX = 45f;
				m3DDebugPanel.rotateY = 0f;
				remove(placeholder);
				invalidate();
				revalidate();
				progress.setDescription("Checking compatibility");
				progress.setDescription("Preparing viewport");
				loaded = true;
				loadProps(propTextures, reg);
				scaleImage(1);
				return null;
			}
		};
		worker.execute();
		progress.showDialog();
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
		GraphicsConfiguration gConfig = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		tilemapScaledImage = gConfig.createCompatibleImage((int) (width * 10 * tilemapScale), (int) (height * 10 * tilemapScale));
		cm2dOverlayImage = gConfig.createCompatibleImage((int) (width * 10 * tilemapScale), (int) (height * 10 * tilemapScale), Transparency.TRANSLUCENT);
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
						//we stretch the regions a few pixels to cover gaps when zooming due to float imprecision
						g.drawImage(tilemaps[j][i].getImage(), (int) (Math.round(400d * tilemapScale * j)), (int) (Math.round(400d * tilemapScale * i)), regionSize + 1, regionSize, null);
					}
				}
			}
		}
	}

	public BufferedImage renderGL(GL2 gl) {
		if (update) {
			CM3DComponents.forEach((r) -> {
				r.uploadBuffers(gl);
			});
			update = false;
		}

		CM3DComponents.forEach((r) -> {
			r.renderCM3D(gl);
		});

		gl.glFlush();
		return new AWTGLReadBufferUtil(glp, true).readPixelsToBufferedImage(gl, true);
	}

	@Override
	public void renderCM3D(GL2 gl) {
		if (mode == ViewportMode.MULTI) {
			for (int i = 0; i < mm.height; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (j < models.length && i < models[j].length && models[j][i] != null) {
						models[j][i].render(gl);
					}
					if (j < tallgrass.length && i < tallgrass[j].length && tallgrass[j][i] != null) {
						tallgrass[j][i].render(gl);
					}
					//useless probably? Can't really edit it in CM3D so it's better to just link it with CollEd per region.
					/*if (colls[j][i] != null) {
						gl.glPushMatrix();
						gl.glTranslatef(j * 720 + 360f, 1, i * 720 + 360f); //we translate it 1 unit up so it does not overlap with the world models
						gl.glBegin(GL2.GL_TRIANGLES);
						colls[j][i].render(gl);
						gl.glEnd();
						gl.glPopMatrix();
					}*/
				}
			}
		} else {
			if (models[0][0] != null) {
				models[0][0].render(gl);
			}
			if (tallgrass[0][0] != null) {
				tallgrass[0][0].render(gl);
			}
		}
	}

	public float getHeightAtWorldLoc(float x, float z) {
		//decide coll mesh to be used
		if (x / 720f >= colls.length || z / 720f >= colls.length) {
			return Float.NaN;
			//methods using this should handle Float.NaN
		}
		GRCollisionFile f = colls[(int) (x / 720f)][(int) (z / 720f)];
		if (f == null) {
			return 0f;
		}
		return f.getHeightAtPoint((x % 720f) - 360f, (z % 720f) - 360f);
	}

	@Override
	public void renderOverlayCM3D(GL2 gl) {
	}

	@Override
	public void uploadBuffers(GL2 gl) {
		if (loaded && mode == ViewportMode.MULTI && mm != null) {
			for (int i = 0; i < mm.height; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (j < models.length && i < models[j].length && models[j][i] != null) {
						models[j][i].models.get(0).uploadAllBOs(gl);
					}
					if (j < tallgrass.length && i < tallgrass[j].length && tallgrass[j][i] != null) {
						tallgrass[j][i].models.get(0).uploadAllBOs(gl);
					}
				}
			}
		}
	}

	@Override
	public void deleteGLInstanceBuffers(GL2 gl) {
		if (loaded && mode == ViewportMode.MULTI && mm != null) {
			for (int i = 0; i < mm.height; i++) {
				for (int j = 0; j < mm.width; j++) {
					if (models[j][i] != null) {
						models[j][i].models.get(0).destroyAllBOs(gl);
					}
					if (tallgrass[j][i] != null) {
						tallgrass[j][i].models.get(0).destroyAllBOs(gl);
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
		int WPStartX = Math.round(mTilemapScrollPane.getViewport().getViewPosition().x - imgstartx);
		int WPStartY = Math.round(mTilemapScrollPane.getViewport().getViewPosition().y - imgstarty);
		int xFromWPStart = Math.round(mTilemapScrollPane.getViewport().getWidth() / 2);
		int yFromWPStart = Math.round(mTilemapScrollPane.getViewport().getHeight() / 2);
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

			BufferedImage crop2;
			double globimgdim = tilemapScaledImage.getHeight() / (double) height;
			int gidround = (int) Math.round(globimgdim);
			if (CM2DTempImage != null && (CtrmapMainframe.tool.CM2DNoUpdate || Selector.getSelectorCM2DRenderOptimizationFlag())) {
				crop2 = CM2DTempImage;
			} else {
				CM2DDrawable.display();
				CM2DDrawable.getContext().makeCurrent();
				GL2 gl = CM2DDrawable.getGL().getGL2();
				gl.glViewport(0, 0, scrollpanew, scrollpaneh);
				gl.glMatrixMode(GL2.GL_PROJECTION);
				gl.glLoadIdentity();

				gl.glOrtho(scrollpanex / globimgdim * 18d, (scrollpanex + scrollpanew) / globimgdim * 18d, (scrollpaney + scrollpaneh) / globimgdim * 18d, (scrollpaney) / globimgdim * 18d, 2000d, -2000d);

				gl.glMatrixMode(GL2.GL_MODELVIEW);
				gl.glLoadIdentity();

				gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
				gl.glLoadIdentity();
				gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
				crop2 = renderGL(gl);
			}
			Graphics2D g2d = (Graphics2D) g;
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g2d.drawImage(crop2, imgstartx + scrollpanex, scrollpaney - imgstarty, null);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

			CM2DTempImage = crop2;

			CtrmapMainframe.tool.drawOverlay(g, imgstartx, imgstarty, globimgdim);
			g.setColor(Color.RED);
			if (CtrmapMainframe.tool.getSelectorEnabled()) {
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
						progress.setBarPercent((int) (((i * tilemaps[i].length + j) / (float) (tilemaps.length * tilemaps[i].length)) * 100));
					}
				}
				mTileMapPanel.scaleImage(mTileMapPanel.tilemapScale);
				return null;
			}
		};
		worker.execute();
		progress.showDialog();
		try {
			worker.get();
		} catch (InterruptedException | ExecutionException ex) {
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
