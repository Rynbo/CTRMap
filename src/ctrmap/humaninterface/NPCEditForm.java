package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import ctrmap.formats.npcreg.NPCRegistry;
import ctrmap.formats.zone.ZoneEntities;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;
import static ctrmap.CtrmapMainframe.*;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.humaninterface.tools.NPCTool;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * GUI form for modifying NPC properties.
 */
public class NPCEditForm extends javax.swing.JPanel implements CM3DRenderable{

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

	public NPCEditForm() {
		initComponents();
		setIntegerValueClass(new JFormattedTextField[]{x, y, areaW, areaH, mot, mp2, u10, u12, areaSX, areaSY, zl2, zl3, hostZone, originZone, linkedZone, linkID});
		((NumberFormatter) altitude.getFormatter()).setValueClass(Float.class);
	}

	public void loadFromEntities(ZoneEntities e, NPCRegistry reg) {
		models.clear();
		this.reg = reg;
		this.e = e;
		loaded = false;
		npc = null;
		regentry = null;
		entryBox.removeAllItems();
		if (e == null) return;
		for (int i = 0; i < e.NPCCount; i++) {
			entryBox.addItem(String.valueOf(e.npcs.get(i).uid));
			if (reg != null){
				models.add(reg.getModel(e.npcs.get(i).model));
			}
		}
		loaded = true;
		if (entryBox.getItemCount() > 0) {
			showEntry(0);
		}
	}

	public void refresh(){
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
		hostZone.setValue(npc.multiZoneLinkHostZone);
		originZone.setValue(npc.multiZoneLinkOriginZone);
		linkedZone.setValue(npc.multiZoneLinkTargetZone);
		linkID.setValue(npc.multiZoneLink1Type);
		mp2.setValue(npc.movePerm2);
		u10.setValue(npc.u10);
		u12.setValue(npc.u12);
		areaSX.setValue(npc.areaStartX);
		areaSY.setValue(npc.areaStartY);
		zl2.setValue(npc.leashWidth);
		zl3.setValue(npc.leashHeight);
		if (reg != null){
			regentry = reg.entries.get(npc.model);
			if (regentry == null){
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
				reg.entries.put(failsafe.uid, failsafe);
				reg.modified = true;
				regentry = failsafe;
			}
		}
		updateModel(index);
		updateH3D(index);
		loaded = true;
	}

	public void updateModel(int index){
		if (reg != null){
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

	public boolean saveRegistry(boolean dialog){
		if (reg == null) return true;
		return reg.store(dialog);
	}
	
	public void setNPC(int num) {
		if (loaded) {
			saveEntry();
			entryBox.setSelectedIndex(num);
			m3DDebugPanel.navi.bindModel(e.npcs.get(num));
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
	
	public void updateH3D(int index){
		H3DModel m = models.get(index);
		if (m == null) return;
		ZoneEntities.NPC n = e.npcs.get(index);
		m.worldLocX = n.xTile * 18f + 9f; //720 (chunk size) / 40 (tile width per chunk)
		m.worldLocZ = n.yTile * 18f + 9f;
		m.worldLocY = n.z3DCoordinate;
		m.rotationY = get3DOrientation(n.faceDirection);
	}
	
	public float get3DOrientation(int faceDirection){
		switch (faceDirection){
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
	public void renderCM3D(GL2 gl){
		if (reg != null){
			for (int i = 0; i < e.NPCCount; i++){
				if (models.size() > i && models.get(i) != null){
					updateH3D(i);
					models.get(i).render(gl);
					if (i == npcIndex && mTileEditForm.tool instanceof NPCTool){
						models.get(i).renderBox(gl);
					}
				}
			}
		}
	}
	
	@Override
	public void renderOverlayCM3D(GL2 gl){}

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

        horizonLabel.setText("There's much to do and many unknowns on the horizon:");

        mp2Label.setText("MP2");

        mp2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(aiSep)
            .addComponent(zlSep)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerSep)
                    .addComponent(worldSep)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnRegEdit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRemoveEntry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNewEntry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(entryBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
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
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
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
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(facedirLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(facedir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rangeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(range, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(worldLocNote, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(areaWLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(u14Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(motLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(areaW)
                                    .addComponent(areaSX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mot))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(areaHLabel)
                                    .addComponent(u16Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(areaH)
                                    .addComponent(areaSY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
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
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(aiLabel)
                                    .addComponent(worldLabel)
                                    .addComponent(linkIDLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(zl2Label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(zl2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(zl3Label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(zl3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(horizonLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(mp2Label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mp2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(u10Label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(u10, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(u12Label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(u12, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
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
                .addComponent(headerSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(worldSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(mot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aiSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(zlSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizonLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mp2Label)
                    .addComponent(mp2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
		for (int i = 0; i < e.npcs.size(); i++){
			newuid = Math.max(e.npcs.get(i).uid + 1, newuid); //get first free UID but don't pollute free spaces if any
		}
		newNPC.uid = newuid;
		newNPC.model = (npc != null) ? npc.model : 0;
		Point defaultPos = mTileMapPanel.getTileAtViewportCentre();
		newNPC.xTile = defaultPos.x;
		newNPC.yTile = defaultPos.y;
		e.npcs.add(newNPC);
		if (reg != null){
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
		if (reg != null){
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
}
