package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Workspace;
import ctrmap.formats.text.LocationNames;
import ctrmap.formats.zone.ZoneEntities;
import java.awt.Point;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

public class WarpEditForm extends javax.swing.JPanel {

	public ZoneEntities e;
	public ZoneEntities.Warp warp;
	public boolean loaded = false;

	public DefaultComboBoxModel<String> transitionModel = new DefaultComboBoxModel<>();

	public void loadFromEntities(ZoneEntities e) {
		loaded = false;
		this.e = e;
		warp = null;
		entryBox.removeAllItems();
		tgtZone.removeAllItems();
		if (e == null) {
			return;
		}
		for (int i = 0; i < mZonePnl.zones.length; i++) {
			tgtZone.addItem(i + " - " + LocationNames.getLocName(mZonePnl.zones[i].header.parentMap));
		}
		for (int i = 0; i < e.warpCount; i++) {
			addNamedWarpEntry(e.warps.get(i), i);
		}
		transition.removeAllItems();
		addBaseTransitions();
		if (Workspace.isOA()) {
			addOATransitions();
		} else {
			addXYTransitions();
		}
		loaded = true;
		if (entryBox.getItemCount() > 0) {
			showEntry(0);
		}
	}

	public void addNamedWarpEntry(ZoneEntities.Warp warp, int warpNumber) {
		entryBox.addItem(warpNumber + " - " + LocationNames.getLocName(mZonePnl.zones[warp.targetZone].header.parentMap));
	}

	public WarpEditForm() {
		initComponents();
		transition.setModel(transitionModel);
		setIntegerValueClass(new JFormattedTextField[]{x, y, z, w, h, tgtWarp});
	}

	public void setIntegerValueClass(JFormattedTextField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(Integer.class);
		}
	}

	public void showEntry(int index) {
		if (index == -1 || index >= e.warpCount) {
			return;
		}
		warp = e.warps.get(index);
		tgtZone.setSelectedIndex(warp.targetZone);
		tgtWarp.setValue(warp.targetWarpId);
		warpType.setSelectedIndex(warp.directionality);
		posType.setSelectedIndex(warp.coordinateType);
		transition.setSelectedIndex(getTransitionIndex(warp.transitionType));
		x.setValue(warp.x);
		y.setValue(warp.y);
		z.setValue(warp.z);
		w.setValue(warp.w);
		h.setValue(warp.h);
		facedir.setSelectedIndex(warp.faceDirection);
	}

	private void addBaseTransitions() {
		String[] baseTr = new String[]{
			"0 - Warp of No Return",
			"2 - Warp on touch; Arrive at warp.",
			"3 - Warp on touch, push; Arrive at neighbor.",
			"4 - Arrival autotrigger (Contest/Salon)",
			"5 - Warp on walk; Arrive at neighbor",
			"6 - Warp pad",};
		trModelStrArrMerge(baseTr);
	}

	private void addXYTransitions() {
		String[] XYtr = new String[]{
			"Lumiose City camera rotate",
			"=2/No arrival FX",
			"=3/No arrival FX",
			"Camera move down (obtuse angle)",
			"Camera move up low",
			"Camera move up high",
			"Camera move up high 2",
			"Camera move up highest",
			"Camera rotate up low",
			"Camera rotate up high",
			"Camera rotate in warp direction (FlareHQ)",};
		trModelStrArrMerge(XYtr);
	}

	private void addOATransitions() {
		String[] OATr = new String[]{
			"Ladder up",
			"Ladder down",
			"Camera move up low (slow)",
			"Camera move up low (fast)",
			"Camera move up low (lowest angle)",
			"Camera move up low (lower angle)",
			"Camera move up low (low angle)",
			"Camera move up high",
			"Camera move up higher",
			"Camera move up highest",
			"Camera move up high (slow)",
			"Camera move up high (lower angle)",
			"Camera move up high (low angle)",
			"Camera move up and left",
			"Camera move up and right",
			"Camera move forward",
			"Camera move above player (90deg)",
			"Camera zoom out"
		};
		trModelStrArrMerge(OATr);
	}

	public int getTransitionIndex(int raw) {
		switch (raw) {
			case 0:
				return 0;
			case 2:
				return 1;
			case 3:
				return 2;
			case 4:
				return 3;
			case 5:
				return 4;
			case 6:
				return 5;
			case 7:
				return 6; //XY
			case 8:
				return 7; //XY
			case 9:
				return 8; //XY
			case 10:
				return 6; //OA
			case 11:
				return 7; //OA
		}
		if (Workspace.isXY()) {
			switch (raw) {
				case 15:
					return 9;
				case 55:
					return 10;
				case 16:
					return 11;
				case 17:
					return 12;
				case 56:
					return 13;
				case 22:
					return 14;
				case 57:
					return 15;
				case 54:
					return 16;
			}
		} else {
			switch (raw) {
				case 55:
					return 8;
				case 56:
					return 9;
				case 54:
					return 10;
				case 17:
					return 11;
				case 18:
					return 12;
				case 24:
					return 13;
				case 23:
					return 14;
				case 22:
					return 15;
				case 26:
					return 16;
				case 20:
					return 17;
				case 16:
					return 18;
				case 19:
					return 19;
				case 15:
					return 20;
				case 21:
					return 21;
				case 57:
					return 22;
				case 25:
					return 23;
			}
		}
		return -1;
	}

	public int getTransitionRaw(int index) {
		switch (index) {
			case 0:
				return 0;
			case 1:
				return 2;
			case 2:
				return 3;
			case 3:
				return 4;
			case 4:
				return 5;
			case 5:
				return 6;
		}
		if (Workspace.isXY()) {
			switch (index) {
				case 6:
					return 7;
				case 7:
					return 8;
				case 8:
					return 9;
				case 9:
					return 15;
				case 10:
					return 22;
				case 11:
					return 16;
				case 12:
					return 17;
				case 13:
					return 56;
				case 14:
					return 22;
				case 15:
					return 57;
				case 16:
					return 54;
			}
		} else {
			switch (index) {
				case 6:
					return 10;
				case 7:
					return 11;
				case 8:
					return 55;
				case 9:
					return 56;
				case 10:
					return 54;
				case 11:
					return 17;
				case 12:
					return 18;
				case 13:
					return 24;
				case 14:
					return 23;
				case 15:
					return 22;
				case 16:
					return 26;
				case 17:
					return 20;
				case 18:
					return 16;
				case 19:
					return 19;
				case 20:
					return 15;
				case 21:
					return 21;
				case 22:
					return 57;
				case 23:
					return 25;
			}
		}
		return -1;
	}

	public void trModelStrArrMerge(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			transitionModel.addElement(strings[i]);
		}
	}

	public void saveEntry() {
		if (warp == null) {
			return;
		}
		ZoneEntities.Warp warp2 = new ZoneEntities.Warp();
		warp2.targetZone = tgtZone.getSelectedIndex();
		warp2.targetWarpId = (Integer) tgtWarp.getValue();
		warp2.directionality = warpType.getSelectedIndex();
		warp2.transitionType = getTransitionRaw(transition.getSelectedIndex());
		warp2.coordinateType = posType.getSelectedIndex();
		warp2.x = (Integer) x.getValue();
		warp2.y = (Integer) y.getValue();
		warp2.z = (Integer) z.getValue();
		warp2.w = (Integer) w.getValue();
		warp2.h = (Integer) h.getValue();
		warp2.faceDirection = facedir.getSelectedIndex();
		if (!warp.equals(warp2)) {
			int idx = e.warps.indexOf(warp);
			warp = warp2;
			e.warps.set(idx, warp);
			e.modified = true;
		}
	}

	public void setWarp(int index) {
		entryBox.setSelectedIndex(index);
	}

	public void refresh() {
		entryBox.setSelectedIndex(entryBox.getSelectedIndex());
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        zLabel = new javax.swing.JLabel();
        y = new javax.swing.JFormattedTextField();
        yLabel = new javax.swing.JLabel();
        facedirLabel = new javax.swing.JLabel();
        wLabel = new javax.swing.JLabel();
        facedir = new javax.swing.JComboBox<>();
        w = new javax.swing.JFormattedTextField();
        entryBox = new javax.swing.JComboBox<>();
        h = new javax.swing.JFormattedTextField();
        tgtConfLabel = new javax.swing.JLabel();
        hLabel = new javax.swing.JLabel();
        tgtZoneLabel = new javax.swing.JLabel();
        transitionLabel = new javax.swing.JLabel();
        tgtZone = new javax.swing.JComboBox<>();
        tgtWarpIdLabel = new javax.swing.JLabel();
        tgtWarp = new javax.swing.JFormattedTextField();
        warpTypeLabel = new javax.swing.JLabel();
        warpType = new javax.swing.JComboBox<>();
        posSep = new javax.swing.JSeparator();
        posLabel = new javax.swing.JLabel();
        posTypeLabel = new javax.swing.JLabel();
        posType = new javax.swing.JComboBox<>();
        xLabel = new javax.swing.JLabel();
        x = new javax.swing.JFormattedTextField();
        z = new javax.swing.JFormattedTextField();
        btnSave = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        transition = new javax.swing.JComboBox<>();

        zLabel.setText("Z");

        y.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        yLabel.setText("Y");

        facedirLabel.setText("Contact direction:");

        wLabel.setText("W");

        facedir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Up", "Down", "Left", "Right" }));

        w.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        entryBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entryBoxActionPerformed(evt);
            }
        });

        h.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        tgtConfLabel.setText("Target configuration:");

        hLabel.setText("H");

        tgtZoneLabel.setText("Target zone:");

        transitionLabel.setText("Transition type:");

        tgtWarpIdLabel.setText("Target warp ID:");

        tgtWarp.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        warpTypeLabel.setText("Warp type:");

        warpType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bidirectional", "Send only", "Receive only" }));

        posLabel.setText("Positioning:");

        posTypeLabel.setText("Type:");

        posType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "X/Z/Y", "X/Y/-" }));

        xLabel.setText("X");

        x.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        z.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnAdd.setText("New entry");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove entry");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(posSep)
                    .addComponent(entryBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tgtConfLabel)
                            .addComponent(posLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(posTypeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(posType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(transitionLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(xLabel)
                                    .addComponent(zLabel)
                                    .addComponent(yLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(hLabel)
                                                    .addComponent(wLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(facedirLabel)
                                                .addGap(32, 32, 32)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(facedir, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(h)
                                            .addComponent(w, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)))
                                    .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tgtWarpIdLabel)
                            .addComponent(tgtZoneLabel)
                            .addComponent(warpTypeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(transition, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tgtZone, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tgtWarp)
                                    .addComponent(warpType, 0, 109, Short.MAX_VALUE))
                                .addGap(0, 190, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(entryBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgtConfLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tgtZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tgtZoneLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tgtWarp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tgtWarpIdLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(warpTypeLabel)
                    .addComponent(warpType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transitionLabel)
                    .addComponent(transition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(posSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(posLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posTypeLabel)
                    .addComponent(posType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wLabel)
                    .addComponent(w, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(h, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hLabel)
                    .addComponent(zLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(facedir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(facedirLabel))
                .addGap(18, 18, 18)
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void entryBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryBoxActionPerformed
		if (loaded && entryBox.getSelectedIndex() != -1) {
			showEntry(entryBox.getSelectedIndex());
		}
    }//GEN-LAST:event_entryBoxActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveEntry();
		frame.repaint();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
		if (e != null) {
			ZoneEntities.Warp newWarp = new ZoneEntities.Warp();
			Point defaultPos = mTileMapPanel.getTileAtViewportCentre();
			newWarp.x = defaultPos.x * 18 + 9;
			newWarp.y = defaultPos.y * 18 + 9;
			e.warps.add(newWarp);
			e.warpCount++;
			addNamedWarpEntry(newWarp, e.warpCount - 1);
			setWarp(entryBox.getItemCount() - 1);
			frame.repaint();
			e.modified = true;
		}
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
		if (e != null) {
			e.warps.remove(warp);
			e.warpCount--;
			entryBox.removeItemAt(entryBox.getSelectedIndex());
			if (entryBox.getSelectedIndex() >= entryBox.getItemCount()) {
				entryBox.setSelectedIndex(entryBox.getSelectedIndex() - 1);
			} else {
				entryBox.setSelectedIndex(entryBox.getSelectedIndex());
			}
			frame.repaint();
			e.modified = true;
		}
    }//GEN-LAST:event_btnRemoveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> entryBox;
    private javax.swing.JComboBox<String> facedir;
    private javax.swing.JLabel facedirLabel;
    private javax.swing.JFormattedTextField h;
    private javax.swing.JLabel hLabel;
    private javax.swing.JLabel posLabel;
    private javax.swing.JSeparator posSep;
    private javax.swing.JComboBox<String> posType;
    private javax.swing.JLabel posTypeLabel;
    private javax.swing.JLabel tgtConfLabel;
    private javax.swing.JFormattedTextField tgtWarp;
    private javax.swing.JLabel tgtWarpIdLabel;
    private javax.swing.JComboBox<String> tgtZone;
    private javax.swing.JLabel tgtZoneLabel;
    private javax.swing.JComboBox<String> transition;
    private javax.swing.JLabel transitionLabel;
    private javax.swing.JFormattedTextField w;
    private javax.swing.JLabel wLabel;
    private javax.swing.JComboBox<String> warpType;
    private javax.swing.JLabel warpTypeLabel;
    private javax.swing.JFormattedTextField x;
    private javax.swing.JLabel xLabel;
    private javax.swing.JFormattedTextField y;
    private javax.swing.JLabel yLabel;
    private javax.swing.JFormattedTextField z;
    private javax.swing.JLabel zLabel;
    // End of variables declaration//GEN-END:variables
}
