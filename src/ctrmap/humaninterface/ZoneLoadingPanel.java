package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Workspace;
import ctrmap.formats.containers.ZO;
import ctrmap.formats.cameradata.CameraDataFile;
import ctrmap.formats.mapmatrix.MapMatrix;
import ctrmap.formats.propdata.ADPropRegistry;
import ctrmap.formats.text.LocationNames;
import ctrmap.formats.zone.Zone;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFormattedTextField;
import javax.swing.SwingWorker;
import javax.swing.text.NumberFormatter;

/**
 * Top-level loader for all in the Pokemon world, should really stop being
 * debug.
 */
public class ZoneLoadingPanel extends javax.swing.JPanel {

	/**
	 * Creates new form ZoneDebugPanel
	 */
	public Zone[] zones;
	public Zone zone;
	public int zoneIndex = -1;
	private boolean loaded = false;

	public ZoneLoadingPanel() {
		initComponents();
		setIntValueClass(new JFormattedTextField[]{cam1, cam2, camFlags, unknownFlags, battleBG, ad, bgmSpring,
			matrix, textFile, script, move, parentMap, x1, y1, z1, x2, y2, z2});
	}

	public void loadZone(Zone z) {
		try {
			loaded = false;
			if (zone != null) {
				zone.header.freeArchives();
				System.gc();
			}
			zone = z;

			isParentMap.setSelected(z.header.OLvalue == 1);
			cam1.setValue(z.header.camera1);
			cam2.setValue(z.header.camera2);
			camFlags.setValue(z.header.cameraFlags);
			x1.setValue(z.header.X);
			x2.setValue(z.header.X2);
			unknownFlags.setValue(z.header.unknownFlags);
			coldbreath.setSelected(z.header.enableBreathFX);
			ghosting.setSelected(z.header.enableGhosting);
			dowsing.setSelected(z.header.enableDowsingMachine);
			enable3d.setSelected(z.header.enable3D);
			unknownFlag.setSelected(z.header.unknownFlag);
			ad.setValue(z.header.areadataID);
			battleBG.setValue(z.header.battleBG);
			bgmSpring.setValue(z.header.BGMSpring);
			run.setSelected(z.header.enableRunning);
			skate.setSelected(z.header.enableRollerSkates);
			cycling.setSelected(z.header.enableCycling);
			escrope.setSelected(z.header.enableEscapeRope);
			fly.setSelected(z.header.enableFlyFrom);
			skybox.setSelected(z.header.enableSkybox);
			bgmCyclingEnable.setSelected(z.header.enableCyclingBGM);
			mapTransition.setSelectedIndex(z.header.mapChange);
			matrix.setValue(z.header.mapmatrixID);
			move.setValue(z.header.mapMove);
			parentMap.setValue(z.header.parentMap);
			script.setValue(z.header.script);
			textFile.setValue(z.header.textID);
			tmg.setSelectedIndex(z.header.townMapGroup);
			type.setSelectedIndex(getTypeIndex(z.header.mapType));
			weather.setSelectedIndex(getWeatherIndex(z.header.weather));
			y1.setValue(z.header.Y);
			z1.setValue(z.header.Z);
			y2.setValue(z.header.Y2);
			z2.setValue(z.header.Z2);

			specWalk.setSelected(z.header.enableSpecialWalking);
			flash.setSelected(z.header.enableFlashableDarkness);
			flash.setEnabled(Workspace.game == Workspace.GameType.ORAS);
			specWalk.setEnabled(Workspace.game == Workspace.GameType.XY);

			loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadEverything() {
		LoadingDialog progress = LoadingDialog.makeDialog("Loading Zone data");
		SwingWorker worker = new SwingWorker() {
			@Override
			protected void done() {
				progress.close();
			}

			@Override
			protected Object doInBackground() {
				loaded = false;
				if (zone != null) {
					zone.header.freeArchives();
				}
				System.gc();
				zone = null;
				zoneList.setSelectedIndex(-1);
				zoneList.removeAllItems();
				tmg.setSelectedIndex(-1);
				tmg.removeAllItems();
				int totalZones = Workspace.getArchive(Workspace.ArchiveType.ZONE_DATA).length;
				totalZones -= (Workspace.game == Workspace.GameType.XY) ? 1 : 2;
				zones = new Zone[totalZones]; //last file is not a ZO
				for (int i = 0; i < totalZones; i++) {
					ZO zo = new ZO(Workspace.getWorkspaceFile(Workspace.ArchiveType.ZONE_DATA, i));
					zones[i] = new Zone(zo, Workspace.game);
					//unknown flags 1024 == 8192 ???, 4096, 16384 always 0, >> 20 lumi warp zone?,
					String name = LocationNames.getLocName(zones[i].header.parentMap) + " - " + i;
					if (zones[i].s.publics.size() > 3) {
						System.out.println(i + "/" + name);
					}
					/*for (int j = 0; j < zones[i].entities.NPCCount; j++){
						ZoneEntities.NPC npc = zones[i].entities.npcs.get(j);
						if (npc.movePerm2 != 1 && npc.movePerm2 != 0 && npc.movePerm2 != 4 && npc.movePerm2 != 9){
							System.out.println(name + "/NPC " + j + " - " + npc.movePerm2);
						}
					}*/
					zoneList.addItem(name);
					tmg.addItem(name);
					progress.setBarPercent((int) ((float) i / totalZones * 100));
				}
				if (Workspace.game == Workspace.GameType.XY) {
					type.setModel(new DefaultComboBoxModel<>(new String[]{
						"Small generic",
						"Outside generic",
						"Grass gym",
						"Inside generic",
						"Anistar Sundial",
						"Lumiose Boulevard",
						"??? Route 6/19",
						"Lumiose Plazas"
					}));
					weather.setModel(new DefaultComboBoxModel<>(new String[]{
						"None",
						"Default 1",
						"Default 2",
						"Default 3",
						"Default 4",
						"Default 5",
						"Default 6",
						"Default 7",
						"Default 8",
						"Default 9",
						"Default 10",
						"Default 11",
						"Default 12",
						"Default 13",
						"Default 14",
						"Clear (perpetual)",
						"Clear (debris storms)",
						"Clear (battle sandstorms)",
						"Clear (snowstorms)",
						"Snow (perpetual)",
						"Snow/Clear/Overcast",
						"Snow/Overcast",
						"Starry/Clear/Snowstorm",
						"Starry/Cloudless",
						"Starry/Clear"
					}));
				} else {
					type.setModel(new DefaultComboBoxModel<>(new String[]{
						"Default map",
						"Single zone map"}));
					weather.setModel(new DefaultComboBoxModel<>(new String[]{
						"Clear",
						"Rain",
						"Thunderstorm",
						"Foggy",
						"Volcanic ash",
						"Sandstorm",
						"Dark",
						"Primordial rain",
						"Primordial drought",
						"Clear (No skybox)"
					}));
				}
				loaded = true;
				return null;
			}
		};
		worker.execute();
		progress.showDialog();
	}

	public boolean store(boolean dialog) {
		if (zone == null) {
			return true;
		}
		zone.header.mapType = getTypeRaw(type.getSelectedIndex());
		zone.header.mapMove = (Integer) move.getValue();
		zone.header.areadataID = (Integer) ad.getValue();
		zone.header.mapmatrixID = (Integer) matrix.getValue();
		zone.header.textID = (Integer) textFile.getValue();
		zone.header.script = (Integer) script.getValue();

		zone.header.townMapGroup = (Integer) tmg.getSelectedIndex();
		zone.header.mapChange = mapTransition.getSelectedIndex();
		zone.header.parentMap = (Integer) parentMap.getValue();
		zone.header.OLvalue = isParentMap.isSelected() ? 1 : 0;

		zone.header.weather = getWeatherRaw(weather.getSelectedIndex());
		zone.header.battleBG = (Integer) battleBG.getValue();
		zone.header.BGMSpring = (Integer) bgmSpring.getValue();
		zone.header.enableCyclingBGM = bgmCyclingEnable.isSelected();

		zone.header.enableRunning = run.isSelected();
		zone.header.enableRollerSkates = skate.isSelected();
		zone.header.enableCycling = cycling.isSelected();

		zone.header.enableEscapeRope = escrope.isSelected();
		zone.header.enableFlyFrom = fly.isSelected();
		zone.header.enableDowsingMachine = dowsing.isSelected();

		zone.header.enableBreathFX = coldbreath.isSelected();
		zone.header.enableGhosting = ghosting.isSelected();
		zone.header.enableSpecialWalking = specWalk.isSelected();
		zone.header.enable3D = enable3d.isSelected();

		zone.header.enableSkybox = skybox.isSelected();

		zone.header.camera1 = (Integer) cam1.getValue();
		zone.header.camera2 = (Integer) cam2.getValue();
		zone.header.cameraFlags = (Integer) camFlags.getValue();
		zone.header.unknownFlags = (Integer) unknownFlags.getValue();
		zone.header.unknownFlag = unknownFlag.isSelected();
		zone.header.unknownFlags = (Integer) unknownFlags.getValue();
		zone.header.calculateFlags();

		zone.header.X = (Integer) x1.getValue();
		zone.header.Y = (Integer) y1.getValue();
		zone.header.Z = (Integer) z1.getValue();

		zone.header.X2 = (Integer) x2.getValue();
		zone.header.Y2 = (Integer) y2.getValue();
		zone.header.Z2 = (Integer) z2.getValue();

		mNPCEditForm.saveEntry();
		if (zone.store(dialog)) {
			try {
				//save to master table
				File master = Workspace.getWorkspaceFile(Workspace.ArchiveType.ZONE_DATA, Workspace.getArchive(Workspace.ArchiveType.ZONE_DATA).length
						- ((Workspace.game == Workspace.GameType.XY) ? 1 : 2));
				RandomAccessFile dos = new RandomAccessFile(master, "rw");
				dos.skipBytes(zoneIndex * 0x38);
				byte[] test = new byte[0x38];
				dos.read(test);
				byte[] replace = zone.file.getFile(0);
				if (!Arrays.equals(replace, test)) {
					dos.seek(zoneIndex * 0x38);
					dos.write(replace);
					Workspace.addPersist(master);
				}
				dos.close();
			} catch (IOException ex) {
				Logger.getLogger(ZoneLoadingPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
			loadZone(zone);
			return true;
		} else {
			return false;
		}
	}

	public void setIntValueClass(JFormattedTextField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(Integer.class);
		}
	}

	public int getWeatherRaw(int index) {
		if (Workspace.game == Workspace.GameType.ORAS) {
			return index;
		} else {
			switch (index) {
				case 0:
					return 29;
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					return index - 1;
				case 6:
				case 7:
				case 8:
					return index;
				case 9:
				case 10:
					return index + 1;
				case 11:
				case 12:
					return index + 5;
				case 13:
				case 14:
					return index + 6;
				case 15:
					return 23;
				case 16:
					return 9;
				case 17:
					return 5;
				case 18:
					return 14;
				case 19:
					return 18;
				case 20:
					return 13;
				case 21:
					return 15;
				case 22:
					return 12;
				case 23:
					return 21;
				case 24:
					return 22;
			}
		}
		return 29;
	}

	public int getWeatherIndex(int raw) {
		if (Workspace.game == Workspace.GameType.ORAS) {
			return raw;
		} else {
			if (raw < 5) {
				return raw + 1;
			} else {
				switch (raw) {
					case 29:
						return 0;
					case 5:
						return 17;
					case 6:
					case 7:
					case 8:
						return raw;
					case 9:
						return 16;
					case 10:
						return 9;
					case 11:
						return 10;
					case 12:
						return 22;
					case 13:
						return 20;
					case 14:
						return 18;
					case 15:
						return 21;
					case 16:
						return 11;
					case 17:
						return 12;
					case 18:
						return 19;
					case 19:
						return 13;
					case 20:
						return 14;
					case 21:
						return 23;
					case 22:
						return 24;
					case 23:
						return 15;
				}
			}
		}
		return -1;
	}

	public int getTypeIndex(int raw) {
		switch (raw) {
			case 0:
				return 0;
			case 1:
				return 1;
			case 2:
				return 2;
			case 3:
				if (Workspace.game == Workspace.GameType.XY) {
					return 3;
				} else {
					return 1;
				}
			case 6:
				return 4;
			case 7:
				return 5;
			case 8:
				return 6;
			case 9:
				return 7;
			default:
				return -1;
		}
	}

	public int getTypeRaw(int index) {
		switch (index) {
			case 0:
				return 0;
			case 1:
				if (Workspace.game == Workspace.GameType.XY) {
					return 1;
				} else {
					return 3;
				}
			case 2:
				return 2;
			case 3:
				return 3;
			case 4:
				return 6;
			case 5:
				return 7;
			case 6:
				return 8;
			case 7:
				return 9;
			default:
				return -1;
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        camFlags = new javax.swing.JFormattedTextField();
        btnSave = new javax.swing.JButton();
        cam1label = new javax.swing.JLabel();
        cam2label = new javax.swing.JLabel();
        camFlagsLabel = new javax.swing.JLabel();
        x1Label = new javax.swing.JLabel();
        x2Label = new javax.swing.JLabel();
        x2 = new javax.swing.JFormattedTextField();
        cam1 = new javax.swing.JFormattedTextField();
        cam2 = new javax.swing.JFormattedTextField();
        x1 = new javax.swing.JFormattedTextField();
        uFlabel = new javax.swing.JLabel();
        unknownFlags = new javax.swing.JFormattedTextField();
        unknownFlag = new javax.swing.JCheckBox();
        zoneList = new javax.swing.JComboBox<>();
        loadZoneLabel = new javax.swing.JLabel();
        loaderSeparator = new javax.swing.JSeparator();
        typeLabel = new javax.swing.JLabel();
        type = new javax.swing.JComboBox<>();
        moveLabel = new javax.swing.JLabel();
        adLabel = new javax.swing.JLabel();
        move = new javax.swing.JFormattedTextField();
        matrix = new javax.swing.JFormattedTextField();
        matrixLabel = new javax.swing.JLabel();
        ad = new javax.swing.JFormattedTextField();
        textLabel = new javax.swing.JLabel();
        textFile = new javax.swing.JFormattedTextField();
        scriptLabel = new javax.swing.JLabel();
        script = new javax.swing.JFormattedTextField();
        tmgLabel = new javax.swing.JLabel();
        engDataSep = new javax.swing.JSeparator();
        BGMTitleLabel = new javax.swing.JLabel();
        bgmSpring = new javax.swing.JFormattedTextField();
        worldPropLabel = new javax.swing.JLabel();
        engDataLabel = new javax.swing.JLabel();
        parentMapLabel = new javax.swing.JLabel();
        parentMap = new javax.swing.JFormattedTextField();
        weatherLabel = new javax.swing.JLabel();
        battleBGLabel = new javax.swing.JLabel();
        battleBG = new javax.swing.JFormattedTextField();
        mapChangeLabel = new javax.swing.JLabel();
        plrFlagsTitleLabel = new javax.swing.JLabel();
        run = new javax.swing.JCheckBox();
        skate = new javax.swing.JCheckBox();
        cycling = new javax.swing.JCheckBox();
        escrope = new javax.swing.JCheckBox();
        fly = new javax.swing.JCheckBox();
        skybox = new javax.swing.JCheckBox();
        bgmCyclingEnable = new javax.swing.JCheckBox();
        unknownSep = new javax.swing.JSeparator();
        unknownTitleLabel = new javax.swing.JLabel();
        y1Label = new javax.swing.JLabel();
        y1 = new javax.swing.JFormattedTextField();
        z1Label = new javax.swing.JLabel();
        z1 = new javax.swing.JFormattedTextField();
        y2 = new javax.swing.JFormattedTextField();
        y2Label = new javax.swing.JLabel();
        z2 = new javax.swing.JFormattedTextField();
        z2Label = new javax.swing.JLabel();
        mainSep = new javax.swing.JSeparator();
        plrFlagsSep = new javax.swing.JSeparator();
        isParentMap = new javax.swing.JCheckBox();
        spawnPointLabel = new javax.swing.JLabel();
        weather = new javax.swing.JComboBox<>();
        coldbreath = new javax.swing.JCheckBox();
        ghosting = new javax.swing.JCheckBox();
        dowsing = new javax.swing.JCheckBox();
        specWalk = new javax.swing.JCheckBox();
        tmg = new javax.swing.JComboBox<>();
        flash = new javax.swing.JCheckBox();
        mapTransition = new javax.swing.JComboBox<>();
        enable3d = new javax.swing.JCheckBox();

        camFlags.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        cam1label.setText("Camera1");

        cam2label.setText("Camera2");

        camFlagsLabel.setText("CameraFlags");

        x1Label.setText("X1");

        x2Label.setText("X");

        x2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        cam1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        cam2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        x1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        uFlabel.setText("UnknownFlags");

        unknownFlags.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        unknownFlag.setText("UnknownFlag");

        zoneList.setMaximumRowCount(20);
        zoneList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoneListActionPerformed(evt);
            }
        });

        loadZoneLabel.setText("Load Zone");

        typeLabel.setText("Type:");

        moveLabel.setText("Move:");

        adLabel.setText("Area data:");

        move.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        matrix.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        matrixLabel.setText("Map matrix:");

        ad.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        textLabel.setText("Text file:");

        textFile.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        scriptLabel.setText("Script file:");

        script.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        tmgLabel.setText("Town map group:");

        engDataSep.setOrientation(javax.swing.SwingConstants.VERTICAL);

        BGMTitleLabel.setText("BGM:");

        bgmSpring.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        worldPropLabel.setText("World properties:");

        engDataLabel.setText("Engine data:");

        parentMapLabel.setText("Parent map:");

        parentMap.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        weatherLabel.setText("Default weather:");

        battleBGLabel.setText("Battle background pack:");

        battleBG.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        mapChangeLabel.setText("MapChange:");

        plrFlagsTitleLabel.setText("Player flags:");

        run.setText("Allow running");

        skate.setText("Allow Rollerblades");

        cycling.setText("Allow Cycling");

        escrope.setText("Allow use of Escape Rope");

        fly.setText("Allow flying from this zone");

        skybox.setText("Draw Skybox (OmegaAlpha only)");

        bgmCyclingEnable.setText("Enable Cycling BGM");
        bgmCyclingEnable.setMargin(new java.awt.Insets(2, 0, 2, 2));

        unknownTitleLabel.setText("Unknown properties (From PK3DS):");

        y1Label.setText("Y1");

        y1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        z1Label.setText("Z1");

        z1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        y2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        y2Label.setText("Y");

        z2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        z2Label.setText("Z");

        isParentMap.setText("Is parent map");
        isParentMap.setMargin(new java.awt.Insets(2, 0, 2, 2));

        spawnPointLabel.setText("OA Default spawn point (3D world float coordinate as short):             (XY role unknown)");

        weather.setMaximumRowCount(25);

        coldbreath.setText("Cold breath FX");

        ghosting.setText("Ghosting blur shader");

        dowsing.setText("Allow Dowsing machine");

        specWalk.setText("Special walking animation (XY)");

        flash.setText("Flash-able darkness (OA)");

        mapTransition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "In - Fade/Opening rhombus; Out - Fade/Pokemon logo", "Directional swipe", "In - Closing circle/Fade; Out - Fade/Opening circle", "In - Pokemon logo/Fade; Out - Fade/Pokemon logo", "In - Closing rhombus/Fade; Out - Fade/Opening rhombus", "In - Swipe/Fade; Out - Fade/Swipe", "In - Closing circle/Fade; Out - White fade", "All fade" }));

        enable3d.setText("Enable 3D");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loaderSeparator, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(plrFlagsSep)
                    .addComponent(unknownSep)
                    .addComponent(mainSep)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(run, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(skate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cycling, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(45, 45, 45)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(escrope, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fly, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dowsing))
                                .addGap(45, 45, 45)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(coldbreath)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(specWalk)
                                            .addComponent(ghosting))
                                        .addGap(45, 45, 45)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(enable3d)
                                            .addComponent(flash)))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(engDataLabel)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(moveLabel)
                                                .addComponent(typeLabel))
                                            .addGap(31, 31, 31)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(move, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(adLabel)
                                            .addGap(9, 9, 9)
                                            .addComponent(ad)
                                            .addGap(40, 40, 40)))
                                    .addComponent(scriptLabel)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(matrixLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(matrix, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(textLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(script, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                                                .addComponent(textFile, javax.swing.GroupLayout.Alignment.TRAILING)))))
                                .addGap(18, 18, 18)
                                .addComponent(engDataSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(worldPropLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(isParentMap)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(tmgLabel)
                                                    .addComponent(mapChangeLabel)
                                                    .addComponent(parentMapLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(parentMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(tmg, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(mapTransition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                        .addGap(26, 26, 26)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(battleBGLabel)
                                            .addComponent(weatherLabel)
                                            .addComponent(BGMTitleLabel)
                                            .addComponent(bgmCyclingEnable))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(weather, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(bgmSpring, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(battleBG, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addComponent(plrFlagsTitleLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(loadZoneLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zoneList, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(skybox)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(x2Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(x2, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y2Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(y2, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(z2Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(z2, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(spawnPointLabel)
                            .addComponent(unknownTitleLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(x1Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(x1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y1Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(y1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(z1Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(z1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(camFlagsLabel)
                                    .addComponent(cam2label)
                                    .addComponent(cam1label)
                                    .addComponent(uFlabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(camFlags, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(cam1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cam2, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(unknownFlags, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(unknownFlag))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zoneList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadZoneLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loaderSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(worldPropLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tmgLabel)
                            .addComponent(weatherLabel)
                            .addComponent(weather, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tmg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mapChangeLabel)
                            .addComponent(battleBGLabel)
                            .addComponent(battleBG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mapTransition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(parentMapLabel)
                            .addComponent(parentMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BGMTitleLabel)
                            .addComponent(bgmSpring, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bgmCyclingEnable)
                            .addComponent(isParentMap))
                        .addGap(62, 62, 62))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(engDataLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(typeLabel)
                                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(moveLabel)
                                    .addComponent(move, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(adLabel)
                                    .addComponent(ad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(matrix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(matrixLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(scriptLabel)
                                    .addComponent(script, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(engDataSep, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(plrFlagsSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(plrFlagsTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(run)
                    .addComponent(escrope)
                    .addComponent(coldbreath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(skate)
                    .addComponent(fly)
                    .addComponent(ghosting)
                    .addComponent(enable3d))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cycling)
                    .addComponent(dowsing)
                    .addComponent(specWalk)
                    .addComponent(flash))
                .addGap(18, 18, 18)
                .addComponent(skybox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spawnPointLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(x2Label)
                    .addComponent(x2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2Label)
                    .addComponent(y2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(z2Label)
                    .addComponent(z2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unknownSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(unknownTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cam1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cam1label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cam2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cam2label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(camFlags, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(camFlagsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unknownFlags, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(uFlabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unknownFlag)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(x1Label)
                    .addComponent(x1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y1Label)
                    .addComponent(y1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(z1Label)
                    .addComponent(z1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		store(false);
    }//GEN-LAST:event_btnSaveActionPerformed

    private void zoneListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoneListActionPerformed
		if (zoneList.getSelectedIndex() != -1 && loaded) {
			if (mCamEditForm.store(true) && mTileMapPanel.saveTileMap(true) && mPropEditForm.store(true) && mNPCEditForm.saveRegistry(true) && store(true)) {
				LoadingDialog progress = LoadingDialog.makeDialog("Loading zone");
				SwingWorker worker = new SwingWorker() {
					@Override
					protected void done() {
						progress.close();
					}

					@Override
					protected Object doInBackground() {
						mCamEditForm.store(true);
						progress.setBarPercent(20);
						Zone z = zones[zoneList.getSelectedIndex()];
						loadZone(z);
						zoneIndex = zoneList.getSelectedIndex();
						progress.setBarPercent(50);
						z.header.fetchArchives();
						z.s.decompressThis();
						progress.setBarPercent(100);
						mTileMapPanel.loadMatrix(new MapMatrix(z.header.mapmatrix), new ADPropRegistry(z.header.areadata, z.header.propTextures), z.header.worldTextures, z.header.propTextures);
						mCamEditForm.loadDataFile(new CameraDataFile(z.header.areadata));
						mNPCEditForm.loadFromEntities(z.entities, z.header.npcreg);
						mWarpEditForm.loadFromEntities(z.entities);
						mScriptPnl.loadScript(z.s);
						m3DDebugPanel.bindNavi(null);
						System.gc();
						m3DDebugPanel.reload = true;
						mTileMapPanel.update = true;
						return null;
					}
				};
				worker.execute();
				progress.showDialog();
			}
		}
    }//GEN-LAST:event_zoneListActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BGMTitleLabel;
    private javax.swing.JFormattedTextField ad;
    private javax.swing.JLabel adLabel;
    private javax.swing.JFormattedTextField battleBG;
    private javax.swing.JLabel battleBGLabel;
    private javax.swing.JCheckBox bgmCyclingEnable;
    private javax.swing.JFormattedTextField bgmSpring;
    private javax.swing.JButton btnSave;
    private javax.swing.JFormattedTextField cam1;
    private javax.swing.JLabel cam1label;
    private javax.swing.JFormattedTextField cam2;
    private javax.swing.JLabel cam2label;
    private javax.swing.JFormattedTextField camFlags;
    private javax.swing.JLabel camFlagsLabel;
    private javax.swing.JCheckBox coldbreath;
    private javax.swing.JCheckBox cycling;
    private javax.swing.JCheckBox dowsing;
    private javax.swing.JCheckBox enable3d;
    private javax.swing.JLabel engDataLabel;
    private javax.swing.JSeparator engDataSep;
    private javax.swing.JCheckBox escrope;
    private javax.swing.JCheckBox flash;
    private javax.swing.JCheckBox fly;
    private javax.swing.JCheckBox ghosting;
    private javax.swing.JCheckBox isParentMap;
    private javax.swing.JLabel loadZoneLabel;
    private javax.swing.JSeparator loaderSeparator;
    private javax.swing.JSeparator mainSep;
    private javax.swing.JLabel mapChangeLabel;
    private javax.swing.JComboBox<String> mapTransition;
    private javax.swing.JFormattedTextField matrix;
    private javax.swing.JLabel matrixLabel;
    private javax.swing.JFormattedTextField move;
    private javax.swing.JLabel moveLabel;
    private javax.swing.JFormattedTextField parentMap;
    private javax.swing.JLabel parentMapLabel;
    private javax.swing.JSeparator plrFlagsSep;
    private javax.swing.JLabel plrFlagsTitleLabel;
    private javax.swing.JCheckBox run;
    private javax.swing.JFormattedTextField script;
    private javax.swing.JLabel scriptLabel;
    private javax.swing.JCheckBox skate;
    private javax.swing.JCheckBox skybox;
    private javax.swing.JLabel spawnPointLabel;
    private javax.swing.JCheckBox specWalk;
    private javax.swing.JFormattedTextField textFile;
    private javax.swing.JLabel textLabel;
    private javax.swing.JComboBox<String> tmg;
    private javax.swing.JLabel tmgLabel;
    private javax.swing.JComboBox<String> type;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel uFlabel;
    private javax.swing.JCheckBox unknownFlag;
    private javax.swing.JFormattedTextField unknownFlags;
    private javax.swing.JSeparator unknownSep;
    private javax.swing.JLabel unknownTitleLabel;
    private javax.swing.JComboBox<String> weather;
    private javax.swing.JLabel weatherLabel;
    private javax.swing.JLabel worldPropLabel;
    private javax.swing.JFormattedTextField x1;
    private javax.swing.JLabel x1Label;
    private javax.swing.JFormattedTextField x2;
    private javax.swing.JLabel x2Label;
    private javax.swing.JFormattedTextField y1;
    private javax.swing.JLabel y1Label;
    private javax.swing.JFormattedTextField y2;
    private javax.swing.JLabel y2Label;
    private javax.swing.JFormattedTextField z1;
    private javax.swing.JLabel z1Label;
    private javax.swing.JFormattedTextField z2;
    private javax.swing.JLabel z2Label;
    private javax.swing.JComboBox<String> zoneList;
    // End of variables declaration//GEN-END:variables
}
