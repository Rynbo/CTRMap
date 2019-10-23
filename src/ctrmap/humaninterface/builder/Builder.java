package ctrmap.humaninterface.builder;

import ctrmap.Utils;
import ctrmap.Workspace;
import ctrmap.formats.containers.AbstractGamefreakContainer;
import ctrmap.formats.containers.ContainerIdentifier;
import ctrmap.formats.containers.ContentType;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.humaninterface.ESPICAControl;
import ctrmap.resources.ResourceAccess;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;

public class Builder extends javax.swing.JPanel {

	/**
	 * Creates new form Builder
	 */
	public DefaultListModel<String> GARCmodel = new DefaultListModel<>();
	public DefaultListModel<String> contModel = new DefaultListModel<>();
	public ArrayList<BuilderFile> currentFiles = new ArrayList<>();
	public AbstractGamefreakContainer currentAGFC;
	public Workspace.ArchiveType currentGARC;

	public Builder() {
		initComponents();
		garcFileList.setModel(GARCmodel);
		contFileList.setModel(contModel);
		garcFileList.addListSelectionListener((ListSelectionEvent e) -> {
			contModel.removeAllElements();
			if (currentGARC != null && garcFileList.getSelectedIndex() != -1) {
				int index = garcFileList.getSelectedIndex();
				if (index < Workspace.getArchive(currentGARC).length) {
					try {
						File decFile = Workspace.getWorkspaceFile(currentGARC, index);
						byte[] magicarr = new byte[2];
						InputStream in = new FileInputStream(decFile);
						if (in.available() > 2) {
							in.read(magicarr);
						}
						if (Utils.isUTF8Capital(magicarr[0]) && Utils.isUTF8Capital(magicarr[1])) {
							loadContainer(ContainerIdentifier.makeAGFC(decFile, magicarr));
						}
						in.close();
					} catch (IOException ex) {
						Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		});
	}

	public void loadGARCs() {
		garc.removeAllItems();
		Workspace.ArchiveType[] arcTypeEnumValues = Workspace.ArchiveType.values();
		for (int i = 0; i < arcTypeEnumValues.length; i++) {
			garc.addItem(arcTypeEnumValues[i].name());
		}
	}

	public void loadContainer(AbstractGamefreakContainer cont) {
		container.setName(cont.getOriginFile().getAbsolutePath());
		contModel.removeAllElements();
		currentAGFC = cont;
		currentFiles.clear();
		for (int i = 0; i < cont.len; i++) {
			BuilderFile bf = new BuilderFile();
			byte[] file = cont.getFile(i);
			bf.raw = file;
			StringBuilder name = new StringBuilder();
			name.append(i);
			String type = "";
			if (Utils.checkBCHMagic(file)) {
				BCHFile bch = new BCHFile(file);
				bf.bchData = bch;
				Iterator mdlIt = bch.models.iterator();
				if (bch.models.isEmpty()) {
					if (!bch.textures.isEmpty()) {
						bf.type = ContentType.H3D_TEXTURE_PACK;
					} else {
						if (bch.contentHeader.skeletalAnimationsPointerTableEntries > 0) {
							bf.type = ContentType.H3D_ANIM_S;
						} else if (bch.contentHeader.materialAnimationsPointerTableEntries > 0) {
							bf.type = ContentType.H3D_ANIM_M;
						} else if (bch.contentHeader.visibilityAnimationsPointerTableEntries > 0) {
							bf.type = ContentType.H3D_ANIM_V;
						}
					}
				} else {
					bf.type = ContentType.H3D_MODEL;
					while (mdlIt.hasNext()) {
						type += ((H3DModel) mdlIt.next()).name;
						if (mdlIt.hasNext()) {
							type += ", ";
						}
					}
				}
			} else if (Utils.checkMagic(file, "coll")) {
				bf.type = ContentType.COLLISION;
			} else if (Utils.checkMagic(file, "CGFX")) {
				bf.type = ContentType.CGFX;
			} else if (file.length >= 6400 && file[0] == 0x28 && file[1] == 0x0 && file[2] == 0x28 && file[3] == 0x0) {
				bf.type = ContentType.TILEMAP;
			} else {
				bf.type = cont.getDefaultContentType(i);
			}
			if (!"".equals(type)) {
				name.append(" - ");
				name.append(type);
			} else if (!"".equals(bf.getTypeString())) {
				name.append(" - ");
				name.append(bf.getTypeString());
			}
			currentFiles.add(bf);
			contModel.addElement(name.toString());
		}
	}

	public static class BuilderFile {

		public byte[] raw;
		public BCHFile bchData;
		public ContentType type;

		public String getTypeString() {
			switch (type) {
				default:
					return "";
				case CGFX:
					return "CGFX";
				case COLLISION:
					return "Standard GFCollision";
				case H3D_ANIM_M:
					return "Material animation";
				case H3D_ANIM_S:
					return "Skeletal animation";
				case H3D_ANIM_V:
					return "Visibility animation";
				case H3D_MODEL:
					return "";
				case H3D_TEXTURE_PACK:
					return "BCH Texture pack";
				case TILEMAP:
					return "Standard tilemap";
			}
		}
	}

	public File openFileDialog(String title) {
		Preferences prefs = Preferences.userRoot().node(getClass().getName());
		JFileChooser jfc = new JFileChooser(prefs.get("LAST_DIR",
				new File(".").getAbsolutePath()));
		jfc.setDialogTitle(title);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setMultiSelectionEnabled(false);
		jfc.showOpenDialog(this);
		if (jfc.getSelectedFile() != null) {
			prefs.put("LAST_DIR", jfc.getSelectedFile().getParent());
		}
		return jfc.getSelectedFile();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contLabel = new javax.swing.JLabel();
        contScrollPane = new javax.swing.JScrollPane();
        contFileList = new javax.swing.JList<>();
        fileActionsLabel = new javax.swing.JLabel();
        btnFAImport = new javax.swing.JButton();
        btnFAExport = new javax.swing.JButton();
        btnFADummy = new javax.swing.JButton();
        container = new javax.swing.JLabel();
        contActionsLabel = new javax.swing.JLabel();
        btnImportContainer = new javax.swing.JButton();
        btnContSaveExternal = new javax.swing.JButton();
        cgSep = new javax.swing.JSeparator();
        garcLabel = new javax.swing.JLabel();
        garc = new javax.swing.JComboBox<>();
        garcScrollPane = new javax.swing.JScrollPane();
        garcFileList = new javax.swing.JList<>();
        garcActionsLabel = new javax.swing.JLabel();
        btnNewContainer = new javax.swing.JButton();
        btnClearContainer = new javax.swing.JButton();

        contLabel.setText("Container:");

        contScrollPane.setViewportView(contFileList);

        fileActionsLabel.setText("File actions:");

        btnFAImport.setText("Import");
        btnFAImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFAImportActionPerformed(evt);
            }
        });

        btnFAExport.setText("Export");

        btnFADummy.setText("Dummy");

        container.setText("<none>");

        contActionsLabel.setText("Container actions:");

        btnImportContainer.setText("Import from file");
        btnImportContainer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportContainerActionPerformed(evt);
            }
        });

        btnContSaveExternal.setText("Save externally");
        btnContSaveExternal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContSaveExternalActionPerformed(evt);
            }
        });

        cgSep.setOrientation(javax.swing.SwingConstants.VERTICAL);

        garcLabel.setText("GARC:");

        garc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                garcActionPerformed(evt);
            }
        });

        garcScrollPane.setViewportView(garcFileList);

        garcActionsLabel.setText("File actions:");

        btnNewContainer.setText("Create new");

        btnClearContainer.setText("Clear");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contActionsLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(contScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fileActionsLabel)
                            .addComponent(btnFADummy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnFAExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnFAImport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnClearContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(contLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(container))
                    .addComponent(btnNewContainer))
                .addGap(18, 18, 18)
                .addComponent(cgSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(garcLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(garc, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(garcActionsLabel)
                    .addComponent(garcScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnContSaveExternal)
                    .addComponent(btnImportContainer))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(contLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(garcLabel)
                            .addComponent(garc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(container))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(fileActionsLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnFAImport)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnFAExport)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnFADummy)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnClearContainer)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(contScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(contActionsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnNewContainer))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(garcScrollPane)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(garcActionsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnContSaveExternal)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImportContainer))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(cgSep)))
                .addGap(11, 11, 11))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void garcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_garcActionPerformed
		loadGARC(garc.getSelectedIndex());
    }//GEN-LAST:event_garcActionPerformed

    private void btnFAImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFAImportActionPerformed
		int index = contFileList.getSelectedIndex();
		final AbstractGamefreakContainer persistentContainerReference = currentAGFC;
		if (index != -1) {
			if (currentFiles.get(index).type == ContentType.H3D_MODEL && Workspace.isOA()) {
				int rsl = JOptionPane.showConfirmDialog(this, "The file selected is a model file. Do you want to import it as OBJ?", "Builder alert", JOptionPane.YES_NO_OPTION);
				if (rsl == JOptionPane.YES_OPTION) {
					File f = openFileDialog("Open a model file");
					if (f != null) {
						File donor = ResourceAccess.copyToTemp("DummyBCH3DModel.bch");
						File output = new File(Workspace.temp + "/espica_model_" + UUID.randomUUID().toString() + ".bch");
						int textures = JOptionPane.showConfirmDialog(this, "Do you want to embed the model's textures into the output BCH?", "Converter alert", JOptionPane.YES_NO_OPTION);
						String[] extra = (textures == JOptionPane.YES_OPTION) ? new String[0] : new String[]{"-notextures"};
						ESPICAControl.ESPICAProcess proc = new ESPICAControl.ESPICAProcess(ESPICAControl.ESPICAFunctionMode.MODEL_CONVERT, f, donor, output, extra);
						runESPICA(proc, () -> {
							persistentContainerReference.storeFile(index, output);
						});
					}
				} else {
					importGeneric(persistentContainerReference, index);
				}
			} else if (currentFiles.get(index).type == ContentType.H3D_TEXTURE_PACK) {
				int rsl = JOptionPane.showConfirmDialog(this, "The file selected is a texture pack. Do you want to merge a MTL file with it?", "Builder alert", JOptionPane.YES_NO_OPTION);
				if (rsl == JOptionPane.YES_OPTION) {
					File f = openFileDialog("Open a material description file");
					if (f != null) {
						File donor = persistentContainerReference.getIOFile(index);
						File output = new File(Workspace.temp + "/espica_texturepack_" + UUID.randomUUID().toString() + ".bch");
						ESPICAControl.ESPICAProcess proc = new ESPICAControl.ESPICAProcess(ESPICAControl.ESPICAFunctionMode.TEXTURE_MERGE, f, donor, output, new String[0]);
						runESPICA(proc, () -> {
							persistentContainerReference.storeFile(index, output);
							reloadContainer();
						});
					}
				} else {
					importGeneric(persistentContainerReference, index);
				}
			} else {
				importGeneric(persistentContainerReference, index);
			}
		}
		reloadContainer();
    }//GEN-LAST:event_btnFAImportActionPerformed

    private void btnImportContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportContainerActionPerformed
		File fToReplace = currentAGFC.getOriginFile();
		File fNew = openFileDialog("Select new container file");
		if (fNew != null && currentAGFC != null) {
			try {
				Files.copy(fNew.toPath(), fToReplace.toPath(), StandardCopyOption.REPLACE_EXISTING);
				currentAGFC = ContainerIdentifier.makeAGFC(fToReplace);
				reloadContainer();
			} catch (IOException ex) {
				Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
    }//GEN-LAST:event_btnImportContainerActionPerformed

    private void btnContSaveExternalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContSaveExternalActionPerformed
		File fNew = openFileDialog("Select external location");
		if (fNew != null && currentAGFC != null) {
			try {
				Files.copy(currentAGFC.getOriginFile().toPath(), fNew.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
    }//GEN-LAST:event_btnContSaveExternalActionPerformed

	private void reloadContainer() {
		loadContainer(currentAGFC);
	}

	private void importGeneric(AbstractGamefreakContainer persistentContainerReference, int index) {
		File in = openFileDialog("Select file to import");
		persistentContainerReference.storeFile(index, in);
	}

	private void runESPICA(ESPICAControl.ESPICAProcess proc, Runnable onSuccess) {
		if (Workspace.ESPICA_PATH == null) {
			JOptionPane.showMessageDialog(this, "ESPICA path not set or invalid. Please correct it in Workspace settings.", "ESPICA error", JOptionPane.ERROR_MESSAGE);
		} else {
			ESPICAControl esc = new ESPICAControl(onSuccess);
			esc.setVisible(true);
			esc.runProcess(Workspace.ESPICA_PATH, proc);
		}
	}

	public void loadGARC(int index) {
		GARCmodel.clear();
		if (index != -1) {
			currentGARC = Workspace.ArchiveType.values()[index];
			if (Workspace.getArchive(currentGARC) != null) {
				for (int i = 0; i < Workspace.getArchive(currentGARC).length; i++) {
					GARCmodel.addElement(String.valueOf(i));
				}
			}
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearContainer;
    private javax.swing.JButton btnContSaveExternal;
    private javax.swing.JButton btnFADummy;
    private javax.swing.JButton btnFAExport;
    private javax.swing.JButton btnFAImport;
    private javax.swing.JButton btnImportContainer;
    private javax.swing.JButton btnNewContainer;
    private javax.swing.JSeparator cgSep;
    private javax.swing.JLabel contActionsLabel;
    private javax.swing.JList<String> contFileList;
    private javax.swing.JLabel contLabel;
    private javax.swing.JScrollPane contScrollPane;
    private javax.swing.JLabel container;
    private javax.swing.JLabel fileActionsLabel;
    private javax.swing.JComboBox<String> garc;
    private javax.swing.JLabel garcActionsLabel;
    private javax.swing.JList<String> garcFileList;
    private javax.swing.JLabel garcLabel;
    private javax.swing.JScrollPane garcScrollPane;
    // End of variables declaration//GEN-END:variables
}
