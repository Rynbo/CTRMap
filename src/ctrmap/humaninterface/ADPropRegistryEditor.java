package ctrmap.humaninterface;

import ctrmap.Utils;
import ctrmap.Workspace;
import ctrmap.formats.containers.BM;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.texturing.H3DTexture;
import ctrmap.formats.propdata.ADPropRegistry;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.NumberFormatter;

/**
 * Regedit form for altering AD-1 Prop registry data.
 */
public class ADPropRegistryEditor extends javax.swing.JFrame {

	/**
	 * Creates new form ADPropRegistryEditor
	 */
	private ADPropRegistry reg;
	private ADPropRegistry.ADPropRegistryEntry e;
	private DefaultListModel<String> model = new DefaultListModel<>();
	private List<H3DModel> models = new ArrayList<>();
	private List<H3DTexture> propTextures = new ArrayList<>();
	private ArrayList<Integer> dict = new ArrayList<>();

	private PropEditForm parent;
	
	public ADPropRegistryEditor(PropEditForm parentPropForm) {
		initComponents();
		parent = parentPropForm;
		setShortValueClass(new JFormattedTextField[]{oa1, oa2, oa3, ea11, ea12, ea13});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (saveEntry(true)) {
					dispose();
					if (parent != null){
						parent.setProp(parent.propIndex);
					}
				}
			}
		});
		setVisible(true);
	}

	public void setShortValueClass(JFormattedTextField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(Short.class);
		}
	}

	public void loadRegistry(ADPropRegistry reg, List<H3DTexture> propTextures) {
		this.propTextures.addAll(propTextures);
		this.reg = reg;
		entryList.removeAll();
		model.removeAllElements();
		entryList.setModel(model);
		if (reg != null) {
			for (Map.Entry<Integer, ADPropRegistry.ADPropRegistryEntry> entry : reg.entries.entrySet()) {
				dict.add(entry.getValue().reference);
				models.add(getH3DModel(entry.getValue().model));
				String mdlName = models.get(dict.size() - 1) != null ? models.get(dict.size() - 1).name : "null";
				model.addElement("UID: " + entry.getValue().reference + " | Model: " + entry.getValue().model + " | " + mdlName);
			}
		}
		entryCountLabel.setText("Entry count: " + model.getSize());
		entryList.addListSelectionListener((ListSelectionEvent e) -> {
			if (entryList.getSelectedValue() == null || entryList.getSelectedIndex() == -1) {
				return;
			}
			saveEntry(false);
			showEntry(dict.get(entryList.getSelectedIndex()));
		});
	}

	public H3DModel getH3DModel(int model) {
		File bchFile = Workspace.getWorkspaceFile(Workspace.ArchiveType.BUILDING_MODELS, model);
		if (bchFile.exists()) {
			BCHFile mdlBch = new BCHFile(new BM(bchFile).getFile(0));
			mdlBch.models.get(0).setMaterialTextures(mdlBch.textures);
			mdlBch.models.get(0).setMaterialTextures(propTextures);
			return mdlBch.models.get(0);
		}
		return null;
	}

	public void showEntry(int id) {
		if (reg == null || !reg.entries.containsKey(id)) {
			return;
		}
		ADPropRegistry.ADPropRegistryEntry entry = reg.entries.get(id);
		uid.setValue(entry.reference);
		bm.setValue(entry.model);
		evtScr1.setValue(entry.eventScr1);
		evtScr2.setValue(entry.eventScr2);
		oa1.setValue(entry.omnipresentAnimations[0]);
		oa2.setValue(entry.omnipresentAnimations[1]);
		oa3.setValue(entry.omnipresentAnimations[2]);
		ea11.setValue(entry.evtAnimations1[0]);
		ea12.setValue(entry.evtAnimations1[1]);
		ea13.setValue(entry.evtAnimations1[2]);
		ea14.setValue(entry.evtAnimations1[3]);
		for (int i = 0; i < 9; i++) {
			evtA2Table.setValueAt(entry.evtAnimations2[i / 3][i % 3], i % 3, i / 3);
		}
		oaEnabled.setSelected(entry.isOmnipresentAnimated == 1);

		customH3DPreview1.loadModel(models.get(dict.indexOf(id)));

		e = entry;
	}

	public boolean saveEntry(boolean dialog) {
		if (e != null) {
			if (reg.entries.containsKey((Integer) uid.getValue()) && !reg.entries.get((Integer) uid.getValue()).equals(e)) {
				JOptionPane.showMessageDialog(this, "The specified UID is already registered. Please use another one.", "UID not unique", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			ADPropRegistry.ADPropRegistryEntry e2 = new ADPropRegistry.ADPropRegistryEntry();
			e2.reference = (Integer) uid.getValue();
			e2.model = (Integer) bm.getValue();
			e2.eventScr1 = ((Integer) evtScr1.getValue()).byteValue();
			e2.eventScr2 = ((Integer) evtScr2.getValue()).byteValue();
			e2.u0 = e.u0;
			e2.isOmnipresentAnimated = oaEnabled.isSelected() ? (byte) 1 : 0;
			e2.u1 = e.u1;
			e2.omnipresentAnimations[0] = (Short) oa1.getValue();
			e2.omnipresentAnimations[1] = (Short) oa2.getValue();
			e2.omnipresentAnimations[2] = (Short) oa3.getValue();
			e2.evtAnimations1[0] = (Short) ea11.getValue();
			e2.evtAnimations1[1] = (Short) ea12.getValue();
			e2.evtAnimations1[2] = (Short) ea13.getValue();
			e2.evtAnimations1[3] = (Short) ea14.getValue();
			for (int i = 0; i < 9; i++) {
				e2.evtAnimations2[i / 3][i % 3] = (Short) evtA2Table.getValueAt(i % 3, i / 3);
			}
			if (!e2.equals(e)) {
				if (dialog) {
					switch (Utils.showSaveConfirmationDialog("Prop registry")) {
						case JOptionPane.YES_OPTION:
							break;
						case JOptionPane.NO_OPTION:
							return true;
						case JOptionPane.CANCEL_OPTION:
							return false;
					}
				}
				if (e.model != e2.model) {
					models.set(dict.indexOf(e.reference), getH3DModel(e2.model));
					model.setElementAt("UID: " + e2.reference + " | Model: " + e2.model + " | " + models.get(dict.indexOf(e.reference)).name, dict.indexOf(e.reference));
				}
				if (e.reference != e2.reference) {
					model.setElementAt("UID: " + e2.reference + " | Model: " + e2.model + " | " + models.get(dict.indexOf(e.reference)).name, dict.indexOf(e.reference));
				}
				dict.set(dict.indexOf(e.reference), e2.reference);
				reg.entries.remove(e.reference);
				e = e2;
				reg.entries.put(e.reference, e);
				reg.modified = true;
				showEntry(e.reference);
			}
		}
		return true;
	}

	public void setEntry(int id) {
		entryList.setSelectedIndex(dict.indexOf(id));
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        uidLabel = new javax.swing.JLabel();
        uid = new javax.swing.JSpinner();
        bmLabel = new javax.swing.JLabel();
        bm = new javax.swing.JSpinner();
        evtScr1Label = new javax.swing.JLabel();
        evtScr1 = new javax.swing.JSpinner();
        evtScr2Label = new javax.swing.JLabel();
        evtScr2 = new javax.swing.JSpinner();
        sep1 = new javax.swing.JSeparator();
        omniLabel = new javax.swing.JLabel();
        oa1 = new javax.swing.JFormattedTextField();
        oa2 = new javax.swing.JFormattedTextField();
        oa3 = new javax.swing.JFormattedTextField();
        evtA1Label = new javax.swing.JLabel();
        ea11 = new javax.swing.JFormattedTextField();
        ea12 = new javax.swing.JFormattedTextField();
        ea13 = new javax.swing.JFormattedTextField();
        ea14 = new javax.swing.JFormattedTextField();
        evtA2Label = new javax.swing.JLabel();
        evtA2SP = new javax.swing.JScrollPane();
        evtA2Table = new javax.swing.JTable();
        btnSave = new javax.swing.JButton();
        oaEnabled = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        entryList = new javax.swing.JList<>();
        customH3DPreview1 = new ctrmap.humaninterface.CustomH3DPreview();
        entryCountLabel = new javax.swing.JLabel();
        btnRemEntry = new javax.swing.JButton();
        btnNewEntry = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("PRE");
        setLocationByPlatform(true);
        setResizable(false);

        uidLabel.setText("UID");

        uid.setModel(new javax.swing.SpinnerNumberModel(0, null, 65535, 1));
        uid.setMinimumSize(new java.awt.Dimension(50, 20));
        uid.setPreferredSize(new java.awt.Dimension(50, 20));

        bmLabel.setText("Source file");

        bm.setModel(new javax.swing.SpinnerNumberModel(0, null, 65535, 1));
        bm.setInheritsPopupMenu(true);
        bm.setMinimumSize(new java.awt.Dimension(50, 20));
        bm.setPreferredSize(new java.awt.Dimension(50, 20));

        evtScr1Label.setText("Event script 1");

        evtScr1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        evtScr2Label.setText("Event script 2");

        evtScr2.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        evtScr2.setPreferredSize(new java.awt.Dimension(50, 20));

        omniLabel.setText("Omnipresent animations:");

        oa1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        oa2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        oa3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        evtA1Label.setText("Event script 1 animations:");

        ea11.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        ea12.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        ea13.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        ea14.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        evtA2Label.setText("Event script 2 animations:");

        evtA2Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Data 1", "Data 2", "Data 3"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Short.class, java.lang.Short.class, java.lang.Short.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        evtA2SP.setViewportView(evtA2Table);

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        oaEnabled.setText("Enabled");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jScrollPane1.setViewportView(entryList);

        javax.swing.GroupLayout customH3DPreview1Layout = new javax.swing.GroupLayout(customH3DPreview1);
        customH3DPreview1.setLayout(customH3DPreview1Layout);
        customH3DPreview1Layout.setHorizontalGroup(
            customH3DPreview1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 242, Short.MAX_VALUE)
        );
        customH3DPreview1Layout.setVerticalGroup(
            customH3DPreview1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        entryCountLabel.setText("Entry count:");

        btnRemEntry.setText("Remove");
        btnRemEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemEntryActionPerformed(evt);
            }
        });

        btnNewEntry.setText("Add new");
        btnNewEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewEntryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(uidLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(uid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bmLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(evtScr1Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(evtScr1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(evtScr2Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(evtScr2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(btnSave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(ea11)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(ea12)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(ea13)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(ea14))
                                .addComponent(evtA1Label, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(evtA2Label, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(evtA2SP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(oa1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(oa2)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(oa3))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(omniLabel)
                                    .addGap(241, 241, 241)
                                    .addComponent(oaEnabled))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sep1, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(entryCountLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(btnRemEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNewEntry, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(customH3DPreview1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uidLabel)
                            .addComponent(uid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bmLabel)
                            .addComponent(bm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(evtScr1Label)
                            .addComponent(evtScr1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(evtScr2Label)
                            .addComponent(evtScr2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sep1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(omniLabel)
                            .addComponent(oaEnabled))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(oa1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(oa2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(oa3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(evtA1Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ea11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ea12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ea13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ea14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(evtA2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(evtA2SP, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(entryCountLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(customH3DPreview1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnNewEntry)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemEntry)))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveEntry(false);
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnNewEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewEntryActionPerformed
		ADPropRegistry.ADPropRegistryEntry e2 = new ADPropRegistry.ADPropRegistryEntry();
		reg.entries.put(-1, e2);
		model.addElement("UID: " + e2.reference + " | Model: " + e2.model);
		dict.add(e2.reference);
		models.add(getH3DModel(e2.model));
		setEntry(e2.reference);
		reg.modified = true;
    }//GEN-LAST:event_btnNewEntryActionPerformed

    private void btnRemEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemEntryActionPerformed
		int index = entryList.getSelectedIndex();
		reg.entries.remove(dict.get(index));
		model.removeElementAt(index);
		models.remove(index);
		dict.remove(index);
		if (index >= model.size()) {
			entryList.setSelectedIndex(index - 1);
		} else {
			entryList.setSelectedIndex(index);
		}
		reg.modified = true;
    }//GEN-LAST:event_btnRemEntryActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(ADPropRegistryEditor.class
					.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(() -> {
			new ADPropRegistryEditor(null).setVisible(true);
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner bm;
    private javax.swing.JLabel bmLabel;
    private javax.swing.JButton btnNewEntry;
    private javax.swing.JButton btnRemEntry;
    private javax.swing.JButton btnSave;
    private ctrmap.humaninterface.CustomH3DPreview customH3DPreview1;
    private javax.swing.JFormattedTextField ea11;
    private javax.swing.JFormattedTextField ea12;
    private javax.swing.JFormattedTextField ea13;
    private javax.swing.JFormattedTextField ea14;
    private javax.swing.JLabel entryCountLabel;
    private javax.swing.JList<String> entryList;
    private javax.swing.JLabel evtA1Label;
    private javax.swing.JLabel evtA2Label;
    private javax.swing.JScrollPane evtA2SP;
    private javax.swing.JTable evtA2Table;
    private javax.swing.JSpinner evtScr1;
    private javax.swing.JLabel evtScr1Label;
    private javax.swing.JSpinner evtScr2;
    private javax.swing.JLabel evtScr2Label;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JFormattedTextField oa1;
    private javax.swing.JFormattedTextField oa2;
    private javax.swing.JFormattedTextField oa3;
    private javax.swing.JCheckBox oaEnabled;
    private javax.swing.JLabel omniLabel;
    private javax.swing.JSeparator sep1;
    private javax.swing.JSpinner uid;
    private javax.swing.JLabel uidLabel;
    // End of variables declaration//GEN-END:variables
}
