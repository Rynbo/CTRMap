package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import ctrmap.CtrmapMainframe;
import ctrmap.formats.npcreg.NPCRegistry;
import ctrmap.formats.zone.ZoneEntities;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;
import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.Workspace;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.model.H3DVertex;
import ctrmap.formats.vectors.Vec3f;
import ctrmap.humaninterface.tools.NPCTool;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 * GUI form for modifying NPC properties.
 */
public class NPCEditForm extends javax.swing.JPanel implements CM3DRenderable {

	/**
	 * Creates new form NPCEditForm
	 */
	public boolean loaded = false;
	public ZoneEntities e;
	public ZoneEntities.NPC npc;
	public NPCRegistry reg;
	public NPCRegistry.NPCRegistryEntry regentry;
	public int npcIndex;
	public List<H3DModel> models = new ArrayList<>();
	public DefaultComboBoxModel motionModel = new DefaultComboBoxModel();
	public DefaultComboBoxModel motion2Model = new DefaultComboBoxModel();

	public NPCEditForm() {
		initComponents();
		setIntegerValueClass(new JFormattedTextField[]{x, y, areaW, areaH, mot, mp2, u10, u12, areaSX, areaSY, zl2, zl3, hostZone, originZone, linkedZone, linkID});
		((NumberFormatter) altitude.getFormatter()).setValueClass(Float.class);
		motDropdown.setModel(motionModel);
		mot2Dropdown.setModel(motion2Model);
	}

	public void loadFromEntities(ZoneEntities e, NPCRegistry reg) {
		models.clear();
		motionModel.removeAllElements();
		motion2Model.removeAllElements();
		addBaseMotion();
		if (Workspace.isXY()) {
			addXYMotion();
		} else {
			addOAMotion();
		}
		this.reg = reg;
		this.e = e;
		loaded = false;
		npc = null;
		regentry = null;
		entryBox.removeAllItems();
		if (e == null) {
			return;
		}
		for (int i = 0; i < e.NPCCount; i++) {
			entryBox.addItem(String.valueOf(e.npcs.get(i).uid));
			if (reg != null) {
				models.add(reg.getModel(e.npcs.get(i).model));
			}
		}
		loaded = true;
		if (entryBox.getItemCount() > 0) {
			showEntry(0);
		}
	}

	private void addBaseMotion() {
		String[] baseMoveCodes = new String[]{
			"Dummy",
			"MoveCodeNone",
			"MoveCodeDirRnd",
			"MoveCodeDirRndUD",
			"MoveCodeDirRndLR",
			"MoveCodeDirRndUL",
			"MoveCodeDirRndUR",
			"MoveCodeDirRndDL",
			"MoveCodeDirRndDR",
			"MoveCodeDirRndUDL",
			"MoveCodeDirRndUDR",
			"MoveCodeDirRndULR",
			"MoveCodeDirRndDLR",
			"MoveCodeUp",
			"MoveCodeDown",
			"MoveCodeLeft",
			"MoveCodeRight",
			"MoveCodeRndHLim",
			"MoveCodeRndBLim",
			"Move constant in left vertical limit",
			"Move constant in left vertical & upper horizontal limit",
			"Move constant in left vertical & upper horizontal limit; H first",
			"Move constant in left vertical & upper horizontal limit; V first 1",
			"Move constant in left vertical & upper horizontal limit; V first 2",
			"???",
			"???",
			"???",
			"Move constant in right vertical & upper horizontal limit",
			"???",
			"Move constant in left vertical & lower horizontal limit",
			"???",
			"Surf along vertical limit",
			"???",
			"???",
			"???",
			"MoveCodeSit",
			"MoveCodeAlongWallLeftHandLimitChange 1",
			"MoveCodeAlongWallLeftHandLimitChange 2",
			"MoveCodeAlongWallLeftHandLimitChange; VH limit invert",
			"MoveCodeAlongWallLeftHandLimitChange; H limit invert",
			"???",
			"MoveCodeAlongWallRightHandLimitChange; V limit invert",
			"???",
			"MoveCodeAlongWallRightHandLimitChange",
			"???",
			"???",
			"Clockwise cruise along edges of area with unwalkables on edges",
			"MoveCodeRand",
			"???",
			"???",
			"MoveCodeAlongBitLeftHandRoller",
			"MoveCodeAlongBitLeftHandRoller",
			"MoveCodeKakuremino",
			"???",
			"???",
			"???",
			"MoveCodeTsutikemuriAlongBitLeftHand",
			"MoveCodeTsutikemuriAlongBitRightHand",
			"MoveCodeTsutikemuriRand"
		};
		String[] baseMove2Codes = new String[]{
			"None",
			"Approaching trainer (Standard)",
			"Approaching trainer - see 1 tile around extra",
			"Approaching trainer - tilt head in expectation",
			"Item",
			"AI Motion",
			"Approaching trainer - paired",
			"Mirror trainer (down)",
			"Fixed line of sight (left)",
			"Fixed line of sight (right)",
			"Mirror trainer pair (down)",
			"Jumpout trainer",};
		motModelStrArrMerge(baseMoveCodes);
		mot2ModelStrArrMerge(baseMove2Codes);
	}

	private void addXYMotion() {
		String[] XYMoveCodes = new String[]{
			"MoveCodeYagiQuick",
			"MoveCodeYagiSlow",
			"MoveCodeYagiStay",};
		String[] XYMove2Codes = new String[]{
			"Approaching Painter (XY Extension)",
			"Rolling skater (XY Extension)",
			"EvTypeTrFighting (GymFight Skater) (XY Extension)"
		};
		motModelStrArrMerge(XYMoveCodes);
		mot2ModelStrArrMerge(XYMove2Codes);
	}

	private void addOAMotion() {
		String[] OAMoveCodes = new String[]{
			"???",
			"MoveCodeSeiza",
			"MoveCodeSecretBaseTrainer",
			"MoveCodeFishing2",};
		String[] OAMove2Codes = new String[]{
			"Approaching diver (OA Extension)",
			"Secret Base Trainer (OA Extension)"
		};
		motModelStrArrMerge(OAMoveCodes);
		mot2ModelStrArrMerge(OAMove2Codes);
	}

	public int getMot2Index(int raw) {
		if (raw <= 4) {
			return raw;
		}
		switch (raw) {
			case 7:
				return 5;
			case 9: //XY
			case 10: //OA
				return 6;
			case 11:
				return 12; //XY extension
			case 12:
				return 7;
			case 14:
				return 8;
			case 15:
				return 9;
			case 16:
				return 10;
			case 20:
				return 11;
		}
		if (Workspace.isXY()) {
			switch (raw) {
				case 22:
					return 13;
				case 23:
					return 14;
			}
		} else {
			switch (raw) {
				case 22:
					return 12;
				case 23:
					return 13;
			}
		}
		return -1;
	}

	public int getMot2Raw(int index) {
		if (index <= 4) {
			return index;
		}
		switch (index) {
			case 5:
				return 7;
			case 12:
				return 11; //XY extension
			case 7:
				return 12;
			case 8:
				return 14;
			case 9:
				return 15;
			case 10:
				return 16;
			case 11:
				return 20;
		}
		if (Workspace.isXY()) {
			switch (index) {
				case 6:
					return 9;
				case 13:
					return 22;
				case 14:
					return 23;
			}
		} else {
			switch (index) {
				case 6:
					return 10;
				case 12:
					return 22;
				case 13:
					return 23;
			}
		}
		return -1;
	}

	public void motModelStrArrMerge(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			motionModel.addElement(strings[i]);
		}
	}

	public void mot2ModelStrArrMerge(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			motion2Model.addElement(strings[i]);
		}
	}

	public void unload() {
		loaded = false;
		npcIndex = -1;
		reg = null;
		regentry = null;
		e = null;
		npc = null;
		models.clear();
		entryBox.setSelectedIndex(-1);
		entryBox.removeAllItems();
	}

	public void refresh() {
		entryBox.setSelectedIndex(entryBox.getSelectedIndex());
	}

	public void showEntry(int index) {
		npcIndex = -1;
		if (index == -1) {
			return;
		}
		loaded = false;
		npc = e.npcs.get(index);
		npcIndex = index;
		regentry = null;
		mdl.setValue(npc.model);
		evtFlag.setValue(npc.spawnFlag);
		scr.setValue(npc.script);
		x.setValue(npc.xTile);
		y.setValue(npc.yTile);
		altitude.setValue(npc.z3DCoordinate);
		facedir.setValue(npc.faceDirection);
		range.setValue(npc.sightRange);
		areaW.setValue(npc.areaWidth);
		areaH.setValue(npc.areaHeight);
		mot.setValue(npc.movePerm1);
		if (npc.movePerm1 < motionModel.getSize()) {
			motDropdown.setSelectedIndex(npc.movePerm1);
		} else {
			motDropdown.setSelectedIndex(-1);
		}
		mp2.setValue(npc.movePerm2);
		if (getMot2Index(npc.movePerm2) < motion2Model.getSize()) {
			mot2Dropdown.setSelectedIndex(getMot2Index(npc.movePerm2));
		} else {
			mot2Dropdown.setSelectedIndex(-1);
		}
		hostZone.setValue(npc.multiZoneLinkHostZone);
		originZone.setValue(npc.multiZoneLinkOriginZone);
		linkedZone.setValue(npc.multiZoneLinkTargetZone);
		linkID.setValue(npc.multiZoneLink1Type);
		u10.setValue(npc.u10);
		u12.setValue(npc.u12);
		areaSX.setValue(npc.areaStartX);
		areaSY.setValue(npc.areaStartY);
		zl2.setValue(npc.leashWidth);
		zl3.setValue(npc.leashHeight);
		if (reg != null) {
			regentry = reg.entries.get(npc.model);
			if (regentry == null) {
				int createEntry = JOptionPane.showConfirmDialog(frame,
						"This NPC's MoveModel properties could not be found\n"
						+ "in this area's MoveModel registry under the model UID.\n\n"
						+ "CTRMap can create dummy registry data for you using the\n"
						+ "most compatible settings for the majority of NPCs.\n\n"
						+ "You may need to change collision properties in NRE.\n\n"
						+ "Do you want to create the registry entry?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (createEntry == JOptionPane.NO_OPTION) {
					loaded = true;
					return;
				}
				NPCRegistry.NPCRegistryEntry failsafe = new NPCRegistry.NPCRegistryEntry();
				failsafe.uid = npc.model; //in this case, GF sometimes uses different UIDs than models, for it****s and ob****s. It's not required though.
				failsafe.model = npc.model;
				if (reg.entries.size() < 31) {
					reg.entries.put(failsafe.uid, failsafe);
					reg.mapModel(failsafe.uid, failsafe.uid);
					reg.modified = true;
					regentry = failsafe;
				} else {
					JOptionPane.showMessageDialog(this, "The registry's maximum capacity of 31 unique NPCs has been reached.\n"
							+ "Please free up space in the registry editor and try again.", "Could not create registry entry", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		updateModel(index);
		updateH3D(index);
		m3DDebugPanel.bindNavi(e.npcs.get(index));
		loaded = true;
	}

	public void updateModel(int index) {
		if (reg != null) {
			models.set(index, reg.getModel(e.npcs.get(index).model));
		}
	}

	public void saveEntry() {
		if (npc == null) {
			return;
		}
		ZoneEntities.NPC npc2 = new ZoneEntities.NPC();
		npc2.uid = npc.uid;
		npc2.model = (Integer) mdl.getValue();
		npc2.spawnFlag = (Integer) evtFlag.getValue();
		npc2.script = (Integer) scr.getValue();
		npc2.xTile = (Integer) x.getValue();
		npc2.yTile = (Integer) y.getValue();
		npc2.z3DCoordinate = (Float) altitude.getValue();
		npc2.faceDirection = (Integer) facedir.getValue();
		npc2.sightRange = (Integer) range.getValue();
		npc2.areaStartX = (Integer) areaSX.getValue();
		npc2.areaStartY = (Integer) areaSY.getValue();
		npc2.areaWidth = (Integer) areaW.getValue();
		npc2.areaHeight = (Integer) areaH.getValue();
		npc2.movePerm1 = (Integer) mot.getValue();
		npc2.movePerm2 = (Integer) mp2.getValue();
		npc2.u10 = (Integer) u10.getValue();
		npc2.u12 = (Integer) u12.getValue();
		npc2.leashWidth = (Integer) zl2.getValue();
		npc2.leashHeight = (Integer) zl3.getValue();
		npc2.multiZoneLinkHostZone = (Integer) hostZone.getValue();
		npc2.multiZoneLinkOriginZone = (Integer) originZone.getValue();
		npc2.multiZoneLinkTargetZone = (Integer) linkedZone.getValue();
		npc2.multiZoneLink1Type = (Integer) linkID.getValue();

		if (!npc2.equals(npc)) {
			int idx = e.npcs.indexOf(npc);
			npc = npc2;
			e.npcs.set(idx, npc);
			e.modified = true;
		}
	}

	public boolean saveRegistry(boolean dialog) {
		if (reg == null) {
			return true;
		}
		return reg.store(dialog);
	}

	public void setNPC(int num) {
		if (loaded) {
			saveEntry();
			entryBox.setSelectedIndex(num);
		}
	}

	public void commitAndSwitch(int switchNum) {
		btnSave.requestFocus(); //first focus something else so that the listener is called
		FocusAdapter adapter = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				entryBox.removeFocusListener(this);
				setNPC(switchNum);
			}
		};
		entryBox.addFocusListener(adapter);
		entryBox.requestFocus();
	}

	public void setIntegerValueClass(JFormattedTextField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(Integer.class);
		}
	}

	public void updateH3D(int index) {
		H3DModel m = models.get(index);
		if (m == null) {
			return;
		}
		ZoneEntities.NPC n = e.npcs.get(index);
		m.worldLocX = n.xTile * 18f + 9f; //720 (chunk size) / 40 (tile width per chunk)
		m.worldLocZ = n.yTile * 18f + 9f;
		m.worldLocY = n.z3DCoordinate;
		m.rotationY = get3DOrientation(n.faceDirection);
	}

	public float get3DOrientation(int faceDirection) {
		switch (faceDirection) {
			case 0:
				return 180f;
			case 1:
				return 0f;
			case 2:
				return 270f;
			case 3:
				return 90f;
			default:
				return 0f;
		}
	}

	@Override
	public void renderCM3D(GL2 gl) {
		if (reg != null) {
			for (int i = 0; i < e.NPCCount; i++) {
				if (models.size() > i && models.get(i) != null) {
					updateH3D(i);
					models.get(i).render(gl);
					if (i == npcIndex && CtrmapMainframe.tool instanceof NPCTool) {
						models.get(i).renderBox(gl);
					}
				}
			}
		}
	}

	@Override
	public void uploadBuffers(GL2 gl) {
		for (int i = 0; i < models.size(); i++) {
			if (models.get(i) != null) {
				models.get(i).uploadAllBOs(gl);
			}
		}
	}

	@Override
	public void deleteGLInstanceBuffers(GL2 gl) {
		for (int i = 0; i < models.size(); i++) {
			if (models.get(i) != null) {
				models.get(i).destroyAllBOs(gl);
			}
		}
	}

	@Override
	public void renderOverlayCM3D(GL2 gl) {
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mdlLabel = new javax.swing.JLabel();
        evtFlagLabel = new javax.swing.JLabel();
        mdl = new javax.swing.JSpinner();
        evtFlag = new javax.swing.JSpinner();
        scrLabel = new javax.swing.JLabel();
        scr = new javax.swing.JSpinner();
        headerSep = new javax.swing.JSeparator();
        worldLabel = new javax.swing.JLabel();
        xLabel = new javax.swing.JLabel();
        altitude = new javax.swing.JFormattedTextField();
        yLabel = new javax.swing.JLabel();
        y = new javax.swing.JFormattedTextField();
        altitudeLabel = new javax.swing.JLabel();
        x = new javax.swing.JFormattedTextField();
        worldLocNote = new javax.swing.JLabel();
        areaWLabel = new javax.swing.JLabel();
        areaW = new javax.swing.JFormattedTextField();
        areaHLabel = new javax.swing.JLabel();
        areaH = new javax.swing.JFormattedTextField();
        facedirLabel = new javax.swing.JLabel();
        facedir = new javax.swing.JSpinner();
        rangeLabel = new javax.swing.JLabel();
        range = new javax.swing.JSpinner();
        aiLabel = new javax.swing.JLabel();
        motLabel = new javax.swing.JLabel();
        mot = new javax.swing.JFormattedTextField();
        aiSep = new javax.swing.JSeparator();
        horizonLabel = new javax.swing.JLabel();
        worldSep = new javax.swing.JSeparator();
        mp2Label = new javax.swing.JLabel();
        mp2 = new javax.swing.JFormattedTextField();
        u10Label = new javax.swing.JLabel();
        u10 = new javax.swing.JFormattedTextField();
        areaSX = new javax.swing.JFormattedTextField();
        u12Label = new javax.swing.JLabel();
        u12 = new javax.swing.JFormattedTextField();
        u14Label = new javax.swing.JLabel();
        zl2 = new javax.swing.JFormattedTextField();
        u16Label = new javax.swing.JLabel();
        areaSY = new javax.swing.JFormattedTextField();
        zl2Label = new javax.swing.JLabel();
        zl3Label = new javax.swing.JLabel();
        zl3 = new javax.swing.JFormattedTextField();
        entryBox = new javax.swing.JComboBox<>();
        btnNewEntry = new javax.swing.JButton();
        btnRemoveEntry = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnRegEdit = new javax.swing.JButton();
        zlLabel = new javax.swing.JLabel();
        hostZoneLabel = new javax.swing.JLabel();
        originZoneLabel = new javax.swing.JLabel();
        hostZone = new javax.swing.JFormattedTextField();
        linkIDLabel = new javax.swing.JLabel();
        linkID = new javax.swing.JFormattedTextField();
        originZone = new javax.swing.JFormattedTextField();
        linkedZoneLabel = new javax.swing.JLabel();
        linkedZone = new javax.swing.JFormattedTextField();
        zlSep = new javax.swing.JSeparator();
        motDropdown = new javax.swing.JComboBox<>();
        mot2Dropdown = new javax.swing.JComboBox<>();

        mdlLabel.setText("Model");

        evtFlagLabel.setText("Spawn condition event flag");

        mdl.setModel(new javax.swing.SpinnerNumberModel(0, null, 65535, 1));
        mdl.setMinimumSize(new java.awt.Dimension(50, 20));
        mdl.setPreferredSize(new java.awt.Dimension(50, 20));
        mdl.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mdlStateChanged(evt);
            }
        });

        evtFlag.setModel(new javax.swing.SpinnerNumberModel(0, null, 65535, 1));
        evtFlag.setMinimumSize(new java.awt.Dimension(50, 20));
        evtFlag.setPreferredSize(new java.awt.Dimension(50, 20));

        scrLabel.setText("Script");

        scr.setMinimumSize(new java.awt.Dimension(50, 20));
        scr.setPreferredSize(new java.awt.Dimension(50, 20));

        worldLabel.setText("World properties:");

        xLabel.setText("X");

        altitude.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));

        yLabel.setText("Y");

        y.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        altitudeLabel.setText("Altitude");

        x.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        worldLocNote.setForeground(new java.awt.Color(102, 102, 102));
        worldLocNote.setText("<html>\nNote: X and Y coordinates are specified in grid tiles while the altitude is<br/> a float that you can either take from the collision mesh at the NPC's postition<br/>or go crazy. Altitude represents the 3D world Y position.\n</html>");

        areaWLabel.setText("Move area width");

        areaW.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        areaHLabel.setText("Move area height");

        areaH.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        facedirLabel.setText("Default face direction");

        facedir.setMinimumSize(new java.awt.Dimension(50, 20));
        facedir.setPreferredSize(new java.awt.Dimension(50, 20));

        rangeLabel.setText("Sight range");

        range.setMinimumSize(new java.awt.Dimension(50, 20));
        range.setPreferredSize(new java.awt.Dimension(50, 20));

        aiLabel.setText("AI properties:");

        motLabel.setText("Motion AI");

        mot.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        mot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                motActionPerformed(evt);
            }
        });

        horizonLabel.setText("There's much to do and many unknowns on the horizon:");

        mp2Label.setText("Move events");

        mp2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        mp2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mp2ActionPerformed(evt);
            }
        });

        u10Label.setText("U10");

        u10.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        areaSX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        areaSX.setMinimumSize(new java.awt.Dimension(100, 20));

        u12Label.setText("U12");

        u12.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        u14Label.setText("Move area rel. X");

        zl2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        u16Label.setText("Move area rel. Y");
        u16Label.setMaximumSize(new java.awt.Dimension(84, 14));
        u16Label.setMinimumSize(new java.awt.Dimension(84, 14));
        u16Label.setPreferredSize(new java.awt.Dimension(84, 14));

        areaSY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        areaSY.setMinimumSize(new java.awt.Dimension(100, 20));

        zl2Label.setText("ZL2");

        zl3Label.setText("ZL3");

        zl3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        entryBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entryBoxActionPerformed(evt);
            }
        });

        btnNewEntry.setText("New entry");
        btnNewEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewEntryActionPerformed(evt);
            }
        });

        btnRemoveEntry.setText("Remove entry");
        btnRemoveEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveEntryActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnRegEdit.setForeground(new java.awt.Color(255, 51, 51));
        btnRegEdit.setText("[DANGER] Edit registry data");
        btnRegEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegEditActionPerformed(evt);
            }
        });

        zlLabel.setText("Zone linking:");

        hostZoneLabel.setText("Host zone");

        originZoneLabel.setText("Origin zone");

        hostZone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        hostZone.setMinimumSize(new java.awt.Dimension(100, 20));

        linkIDLabel.setText("Link ID");

        linkID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        linkID.setMinimumSize(new java.awt.Dimension(100, 20));
        linkID.setPreferredSize(new java.awt.Dimension(100, 20));

        originZone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        originZone.setMinimumSize(new java.awt.Dimension(100, 20));

        linkedZoneLabel.setText("Linked zone");

        linkedZone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        linkedZone.setMinimumSize(new java.awt.Dimension(100, 20));

        motDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                motDropdownActionPerformed(evt);
            }
        });

        mot2Dropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mot2DropdownActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(zl2Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zl2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zl3Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zl3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(u10Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(u10, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(u12Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(u12, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headerSep)
                            .addComponent(btnRegEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRemoveEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNewEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(worldSep)
                            .addComponent(entryBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(xLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(x)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(altitudeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(altitude))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(mdlLabel)
                                .addGap(3, 3, 3)
                                .addComponent(mdl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(evtFlagLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(evtFlag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(facedirLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(facedir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rangeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(range, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(worldLocNote, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                            .addComponent(zlSep)
                            .addComponent(aiSep)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(zlLabel)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(originZoneLabel)
                                            .addComponent(hostZoneLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(linkID, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(hostZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(originZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(linkedZoneLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(linkedZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(aiLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(worldLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(linkIDLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(horizonLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(areaWLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(u14Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(motLabel)
                                    .addComponent(mp2Label))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(areaW)
                                    .addComponent(areaSX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mot)
                                    .addComponent(mp2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(areaHLabel)
                                            .addComponent(u16Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(areaH)
                                            .addComponent(areaSY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addComponent(motDropdown, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mot2Dropdown, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(entryBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mdlLabel)
                    .addComponent(evtFlagLabel)
                    .addComponent(mdl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(evtFlag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrLabel)
                    .addComponent(scr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(worldLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xLabel)
                    .addComponent(altitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yLabel)
                    .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(altitudeLabel)
                    .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(worldLocNote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(facedirLabel)
                    .addComponent(facedir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rangeLabel)
                    .addComponent(range, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(worldSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aiLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(u14Label)
                    .addComponent(areaSX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(u16Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(areaSY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(areaWLabel)
                    .addComponent(areaHLabel)
                    .addComponent(areaH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(areaW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(motLabel)
                    .addComponent(mot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(motDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mp2Label)
                    .addComponent(mp2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mot2Dropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aiSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(zlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostZoneLabel)
                    .addComponent(hostZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(originZoneLabel)
                    .addComponent(originZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(linkedZoneLabel)
                    .addComponent(linkedZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(linkIDLabel)
                    .addComponent(linkID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(zlSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizonLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(u10Label)
                    .addComponent(u10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(u12Label)
                    .addComponent(u12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zl2Label)
                    .addComponent(zl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zl3Label)
                    .addComponent(zl3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNewEntry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoveEntry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRegEdit)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void entryBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryBoxActionPerformed
		if (loaded && entryBox.getSelectedIndex() != -1) {
			showEntry(entryBox.getSelectedIndex());
			frame.repaint();
		}
    }//GEN-LAST:event_entryBoxActionPerformed

    private void btnRegEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegEditActionPerformed
		if (npc == null || regentry == null) {
			return;
		}
		NPCRegistryEditor regedit = new NPCRegistryEditor();
		regedit.loadRegistry(reg);
		regedit.setEntry(npc.model);
    }//GEN-LAST:event_btnRegEditActionPerformed

    private void btnNewEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewEntryActionPerformed
		ZoneEntities.NPC newNPC = new ZoneEntities.NPC();
		int newuid = 0;
		for (int i = 0; i < e.npcs.size(); i++) {
			newuid = Math.max(e.npcs.get(i).uid + 1, newuid); //get first free UID but don't pollute free spaces if any
		}
		newNPC.uid = newuid;
		newNPC.model = (npc != null) ? npc.model : 0;
		Point defaultPos = mTileMapPanel.getTileAtViewportCentre();
		newNPC.xTile = defaultPos.x;
		newNPC.yTile = defaultPos.y;
		e.npcs.add(newNPC);
		if (reg != null) {
			models.add(reg.getModel(newNPC.model));
		}
		e.NPCCount++;
		entryBox.addItem(String.valueOf(newNPC.uid));
		setNPC(entryBox.getItemCount() - 1);
		frame.repaint();
		e.modified = true;
    }//GEN-LAST:event_btnNewEntryActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveEntry();
		frame.repaint();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRemoveEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveEntryActionPerformed
		if (reg != null) {
			models.remove(reg.getModel(npc.model));
		}
		e.npcs.remove(npc);
		e.NPCCount--;
		entryBox.removeItemAt(entryBox.getSelectedIndex());
		if (entryBox.getSelectedIndex() >= entryBox.getItemCount()) {
			entryBox.setSelectedIndex(entryBox.getSelectedIndex() - 1);
		} else {
			entryBox.setSelectedIndex(entryBox.getSelectedIndex());
		}
		frame.repaint();
		e.modified = true;
    }//GEN-LAST:event_btnRemoveEntryActionPerformed

    private void mdlStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mdlStateChanged
		setNPC(entryBox.getSelectedIndex());
    }//GEN-LAST:event_mdlStateChanged

    private void motActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_motActionPerformed
		if (loaded && (Integer) mot.getValue() < motDropdown.getItemCount()) {
			motDropdown.setSelectedIndex((Integer) mot.getValue());
		}
    }//GEN-LAST:event_motActionPerformed

    private void motDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_motDropdownActionPerformed
		if (loaded && motDropdown.getSelectedIndex() != -1) {
			mot.setValue(motDropdown.getSelectedIndex());
		}
    }//GEN-LAST:event_motDropdownActionPerformed

    private void mot2DropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mot2DropdownActionPerformed
		if (loaded && mot2Dropdown.getSelectedIndex() != -1) {
			mp2.setValue(getMot2Raw(mot2Dropdown.getSelectedIndex()));
		}
    }//GEN-LAST:event_mot2DropdownActionPerformed

    private void mp2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mp2ActionPerformed
		if (loaded && (Integer) mp2.getValue() < mot2Dropdown.getItemCount()) {
			mot2Dropdown.setSelectedIndex(getMot2Index((Integer) mp2.getValue()));
		}
    }//GEN-LAST:event_mp2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aiLabel;
    private javax.swing.JSeparator aiSep;
    private javax.swing.JFormattedTextField altitude;
    private javax.swing.JLabel altitudeLabel;
    private javax.swing.JFormattedTextField areaH;
    private javax.swing.JLabel areaHLabel;
    private javax.swing.JFormattedTextField areaSX;
    private javax.swing.JFormattedTextField areaSY;
    private javax.swing.JFormattedTextField areaW;
    private javax.swing.JLabel areaWLabel;
    private javax.swing.JButton btnNewEntry;
    private javax.swing.JButton btnRegEdit;
    private javax.swing.JButton btnRemoveEntry;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> entryBox;
    private javax.swing.JSpinner evtFlag;
    private javax.swing.JLabel evtFlagLabel;
    private javax.swing.JSpinner facedir;
    private javax.swing.JLabel facedirLabel;
    private javax.swing.JSeparator headerSep;
    private javax.swing.JLabel horizonLabel;
    private javax.swing.JFormattedTextField hostZone;
    private javax.swing.JLabel hostZoneLabel;
    private javax.swing.JFormattedTextField linkID;
    private javax.swing.JLabel linkIDLabel;
    private javax.swing.JFormattedTextField linkedZone;
    private javax.swing.JLabel linkedZoneLabel;
    private javax.swing.JSpinner mdl;
    private javax.swing.JLabel mdlLabel;
    private javax.swing.JFormattedTextField mot;
    private javax.swing.JComboBox<String> mot2Dropdown;
    private javax.swing.JComboBox<String> motDropdown;
    private javax.swing.JLabel motLabel;
    private javax.swing.JFormattedTextField mp2;
    private javax.swing.JLabel mp2Label;
    private javax.swing.JFormattedTextField originZone;
    private javax.swing.JLabel originZoneLabel;
    private javax.swing.JSpinner range;
    private javax.swing.JLabel rangeLabel;
    private javax.swing.JSpinner scr;
    private javax.swing.JLabel scrLabel;
    private javax.swing.JFormattedTextField u10;
    private javax.swing.JLabel u10Label;
    private javax.swing.JFormattedTextField u12;
    private javax.swing.JLabel u12Label;
    private javax.swing.JLabel u14Label;
    private javax.swing.JLabel u16Label;
    private javax.swing.JLabel worldLabel;
    private javax.swing.JLabel worldLocNote;
    private javax.swing.JSeparator worldSep;
    private javax.swing.JFormattedTextField x;
    private javax.swing.JLabel xLabel;
    private javax.swing.JFormattedTextField y;
    private javax.swing.JLabel yLabel;
    private javax.swing.JFormattedTextField zl2;
    private javax.swing.JLabel zl2Label;
    private javax.swing.JFormattedTextField zl3;
    private javax.swing.JLabel zl3Label;
    private javax.swing.JLabel zlLabel;
    private javax.swing.JSeparator zlSep;
    // End of variables declaration//GEN-END:variables

	@Override
	public void doSelectionLoop(MouseEvent evt, Component parent, float[] mvMatrix, float[] projMatrix, int[] view, Vec3f cameraVec) {
		if (!(CtrmapMainframe.tool instanceof NPCTool)){
			return;
		}
		double closestDist = Float.MAX_VALUE;
		int closestIdx = -1;
		GLUgl2 glu = new GLUgl2();
		for (int i = 0; i < models.size(); i++) {
			if (models.get(i) == null) {
				continue;
			}
			ZoneEntities.NPC testNpc = e.npcs.get(i);
			float[][] box = models.get(i).boxVectors;
			if (Utils.isBoxSelected(box, evt, parent, new Vec3f(testNpc.getX(), testNpc.getY(), testNpc.getZ()), new Vec3f(1f, 1f, 1f), new Vec3f(0f, get3DOrientation(testNpc.faceDirection), 0f), mvMatrix, projMatrix, view)) {
				H3DModel m = models.get(i);
				boolean allow = false;
				for (int mesh = 0; mesh < m.meshes.size(); mesh++) {
					for (int vertex = 0; vertex < m.meshes.get(mesh).vertices.size(); vertex++) {
						H3DVertex v = m.meshes.get(mesh).vertices.get(vertex);
						float[] test = new float[3];
						glu.gluProject(v.position.x + testNpc.getX(), v.position.y + testNpc.getY(), v.position.z + testNpc.getZ(), mvMatrix, 0, projMatrix, 0, view, 0, test, 0);
						if (test[0] > 0 && test[0] < parent.getWidth() && test[1] > 0 && test[1] < parent.getHeight()) {
							allow = true;
							break;
						}
					}
					if (allow) {
						break;
					}
				}
				if (!allow) {
					continue;
				}
				Vec3f dummyCenterVector = new Vec3f(testNpc.getX(), testNpc.getY(), testNpc.getZ());
				double dist = Utils.getDistanceFromVector(dummyCenterVector, cameraVec);
				if (Math.abs(dist) < closestDist && i != npcIndex) {
					closestDist = Math.abs(dist);
					closestIdx = i;
				}
			}
		}
		if (closestIdx != -1) {
			setNPC(closestIdx);
		}
	}
}
