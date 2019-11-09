package ctrmap.humaninterface;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import ctrmap.CtrmapMainframe;
import ctrmap.Utils;
import ctrmap.formats.propdata.GRProp;
import ctrmap.formats.propdata.GRPropData;
import ctrmap.Workspace;
import ctrmap.formats.containers.BM;
import ctrmap.formats.containers.GR;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.model.H3DVertex;
import ctrmap.formats.h3d.texturing.H3DTexture;
import ctrmap.formats.propdata.ADPropRegistry;
import ctrmap.formats.vectors.Vec3f;
import ctrmap.humaninterface.tools.PropTool;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

/**
 * GUI form for modifying GR-3 prop data.
 */
public class PropEditForm extends javax.swing.JPanel implements CM3DRenderable {

	/**
	 * Creates new form PropEditForm
	 */
	public GR gr;
	public GRPropData props;
	public ArrayList<H3DModel> models = new ArrayList<>();
	public GRProp prop;
	public ADPropRegistry reg;
	public ADPropRegistry.ADPropRegistryEntry regentry;
	public int propIndex;
	public boolean loaded = false;

	public List<H3DTexture> propTextures = new ArrayList<>();

	public PropEditForm() {
		initComponents();
		setFloatValueClass(new JFormattedTextField[]{x, y, z, sx, sy, sz, rx, ry, rz});
		//Only need the DocListeners on the fields that we want to reflect on the GUI immediately
		x.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (loaded && prop != null && prop.x != Utils.getFloatFromDocument(x)) {
					prop.x = Utils.getFloatFromDocument(x);
					props.modified = true;
					updateH3D(props.props.indexOf(prop));
				}
				firePropertyChange(TileMapPanel.PROP_REPAINT, false, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (loaded && prop != null && prop.x != Utils.getFloatFromDocument(x)) {
					prop.x = Utils.getFloatFromDocument(x);
					props.modified = true;
					updateH3D(props.props.indexOf(prop));
				}
				firePropertyChange(TileMapPanel.PROP_REPAINT, false, true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		y.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (loaded && prop != null && prop.y != Utils.getFloatFromDocument(y)) {
					prop.y = Utils.getFloatFromDocument(y);
					props.modified = true;
					updateH3D(props.props.indexOf(prop));
				}
				firePropertyChange(TileMapPanel.PROP_REPAINT, false, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (loaded && prop != null && prop.y != Utils.getFloatFromDocument(y)) {
					prop.y = Utils.getFloatFromDocument(y);
					props.modified = true;
					updateH3D(props.props.indexOf(prop));
				}
				firePropertyChange(TileMapPanel.PROP_REPAINT, false, true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		z.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (loaded && prop != null && prop.z != Utils.getFloatFromDocument(z)) {
					prop.z = Utils.getFloatFromDocument(z);
					props.modified = true;
					updateH3D(props.props.indexOf(prop));
				}
				firePropertyChange(TileMapPanel.PROP_REPAINT, false, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (loaded && prop != null && prop.z != Utils.getFloatFromDocument(z)) {
					prop.z = Utils.getFloatFromDocument(z);
					props.modified = true;
					updateH3D(props.props.indexOf(prop));
				}
				firePropertyChange(TileMapPanel.PROP_REPAINT, false, true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	public void setFloatValueClass(JFormattedTextField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(Float.class);
		}
	}

	public void loadDataFile(GR f, List<H3DTexture> propTextures) {
		gr = f;
		props = new GRPropData(gr);
		loadDataFile(props, null, propTextures);
	}

	public void loadDataFile(GRPropData f, ADPropRegistry reg, List<H3DTexture> propTextures) {
		this.propTextures = propTextures;
		models.clear();
		this.reg = reg;
		prop = null;
		loaded = false;
		props = f;
		for (int i = 0; i < props.props.size(); i++) {
			props.props.get(i).updateName(reg); //even if reg is null, the method handles it and (inaccurately) assigns the name by UID
			if (reg != null) {
				H3DModel model = reg.getModel(props.props.get(i).uid);
				models.add(model);
			}
			updateH3D(i);
		}
		entryBox.removeAllItems();
		for (int i = 0; i < props.props.size(); i++) {
			entryBox.addItem(String.valueOf(i));
		}
		loaded = true;
		if (entryBox.getItemCount() > 0) {
			showProp(0);
		}
	}

	public void unload() {
		loaded = false;
		propIndex = -1;
		prop = null;
		props = null;
		models.clear();
		reg = null;
		regentry = null;
		entryBox.setSelectedIndex(-1);
		entryBox.removeAllItems();
	}

	public void updateH3D(int index) {
		if (index >= models.size()) {
			return;
		}
		H3DModel m = models.get(index);
		GRProp p = props.props.get(index);
		if (m == null || p == null) {
			return;
		}
		m.worldLocX = p.x;
		m.worldLocY = p.y;
		m.worldLocZ = p.z;
		m.scaleX = p.scaleX;
		m.scaleY = p.scaleY;
		m.scaleZ = p.scaleZ;
		m.rotationX = p.rotateX;
		m.rotationY = p.rotateY;
		m.rotationZ = p.rotateZ;
		CtrmapMainframe.m3DDebugPanel.navi.synchronizeNavi();
	}

	public void setProp(int index) {
		entryBox.setSelectedIndex(index);
	}

	public void saveProp() {
		if (prop == null) {
			return;
		}
		GRProp prop2 = new GRProp();
		prop2.uid = (Integer) mdlNum.getValue();
		prop2.updateName(reg);
		prop2.x = Utils.getFloatFromDocument(x);
		prop2.y = Utils.getFloatFromDocument(y);
		prop2.z = Utils.getFloatFromDocument(z);
		prop2.rotateX = Utils.getFloatFromDocument(rx);
		prop2.rotateY = Utils.getFloatFromDocument(ry);
		prop2.rotateZ = Utils.getFloatFromDocument(rz);
		prop2.scaleX = Utils.getFloatFromDocument(sx);
		prop2.scaleY = Utils.getFloatFromDocument(sy);
		prop2.scaleZ = Utils.getFloatFromDocument(sz);
		if (!equalsData(prop, prop2)) {
			int idx = props.props.indexOf(prop);
			prop = prop2;
			props.props.set(idx, prop);
			props.modified = true;
			updateH3D(idx);
			firePropertyChange(TileMapPanel.PROP_REPAINT, false, true);
		}
	}

	public boolean store(boolean dialog) {
		if (props != null) {
			saveProp();
			if (props.modified) {
				if (dialog) {
					int rsl = Utils.showSaveConfirmationDialog("Prop data");
					switch (rsl) {
						case JOptionPane.YES_OPTION:
							if (CtrmapMainframe.mTileMapPanel.mm != null) {
								props.write(CtrmapMainframe.mTileMapPanel.mm);
							} else if (gr != null) {
								props.write();
							}
							break; //continue to save
						case JOptionPane.NO_OPTION:
							break;
						case JOptionPane.CANCEL_OPTION:
							return false;
					}
				} else {
					if (CtrmapMainframe.mTileMapPanel.mm != null) {
						props.write(CtrmapMainframe.mTileMapPanel.mm);
					} else if (gr != null) {
						props.write();
					}
				}
				props.modified = false;
			}
			if (reg != null && reg.modified) {
				if (dialog) {
					int rsl = Utils.showSaveConfirmationDialog("Prop registry");
					switch (rsl) {
						case JOptionPane.YES_OPTION:
							break; //continue to save
						case JOptionPane.NO_OPTION:
							reg.modified = false;
							return true;
						case JOptionPane.CANCEL_OPTION:
							return false;
					}
				}
				reg.write();
			}
		}
		return true;
	}

	@Override
	public void renderCM3D(GL2 gl) {
		if (reg != null) {
			for (int i = 0; i < models.size(); i++) {
				if (models.size() > i && models.get(i) != null) {
					updateH3D(i);
					models.get(i).render(gl);
				}
			}
		}
	}

	@Override
	public void renderOverlayCM3D(GL2 gl) {
		if (reg != null) {
			for (int i = 0; i < models.size(); i++) {
				if (models.size() > i && models.get(i) != null) {
					if (i == propIndex && CtrmapMainframe.tool instanceof PropTool) {
						updateH3D(i);
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

	public boolean equalsData(GRProp p1, GRProp p2) {
		if (p1.uid != p2.uid) {
			return false;
		}
		if (p1.x != p2.x) {
			return false;
		}
		if (p1.y != p2.y) {
			return false;
		}
		if (p1.z != p2.z) {
			return false;
		}
		if (p1.rotateY != p2.rotateY) {
			return false;
		}
		if (p1.rotateX != p2.rotateX) {
			return false;
		}
		if (p1.rotateZ != p2.rotateZ) {
			return false;
		}
		if (p1.scaleX != p2.scaleX) {
			return false;
		}
		if (p1.scaleY != p2.scaleY) {
			return false;
		}
		if (p1.scaleZ != p2.scaleZ) {
			return false;
		}
		return true;
	}

	public void showProp(int index) {
		try {
			propIndex = index;
			if (index == -1 || index >= props.props.size()) {
				prop = null;
				return;
			}
			prop = props.props.get(index);
			if (prop == null) {
				return;
			}
			loaded = false;
			mdlNum.setValue(prop.uid);
			prop.updateName(reg);
			mdlName.setText(prop.name);
			x.setValue(prop.x);
			y.setValue(prop.y);
			z.setValue(prop.z);
			rx.setValue(prop.rotateX);
			ry.setValue(prop.rotateY);
			rz.setValue(prop.rotateZ);
			sx.setValue(prop.scaleX);
			sy.setValue(prop.scaleY);
			sz.setValue(prop.scaleZ);
			updateModel(index);
			CtrmapMainframe.m3DDebugPanel.bindNavi(props.props.get(entryBox.getSelectedIndex()));
			CtrmapMainframe.frame.repaint();
			loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateModel(int index) {
		if (index > models.size() - 1) {
			PropPreview.loadModel(null);
		}
		if (reg != null) {
			models.set(index, reg.getModel(props.props.get(index).uid));
		}
		if (models.get(index) == null) {
			//failsafe
			File f = Workspace.getWorkspaceFile(Workspace.ArchiveType.BUILDING_MODELS, props.props.get(index).uid);
			if (f.exists()) {
				BCHFile bch = new BCHFile(new BM(f).getFile(0));
				bch.models.get(0).setMaterialTextures(bch.textures);
				bch.models.get(0).setMaterialTextures(propTextures);
				bch.models.get(0).makeAllBOs();
				models.set(index, bch.models.get(0));
			}
		}
		PropPreview.loadModel(models.get(index));
	}

	public void saveAndRefresh() {
		if (loaded) {
			saveProp();
			showProp(entryBox.getSelectedIndex());
			if (reg != null && prop != null) {
				regentry = reg.entries.get(prop.uid);
				if (regentry == null) {
					int createEntry = JOptionPane.showConfirmDialog(this,
							"The model and animation data needed for this prop\n"
							+ "was not found in this area's registry under the model UID.\n\n"
							+ "CTRMap can create dummy registry data for you, but keep in mind that:\n\n"
							+ "1. If the prop model's textures aren't in the scene, the game will hardlock.\n"
							+ "2. If you are using a custom prop, you need to set the entry's animation data\n"
							+ "   in PRE if you want the game to use it.\n"
							+ "3. Similarly, even if you are using one of GF's props, you still need to set\n"
							+ "   the animation data as CTRMap can not detect the correct settings without\n"
							+ "   iterating through every area searching other maps for clues, which would take forever.\n\n"
							+ "Do you want to create the registry entry?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (createEntry == JOptionPane.NO_OPTION) {
						return;
					}
					ADPropRegistry.ADPropRegistryEntry failsafe = new ADPropRegistry.ADPropRegistryEntry();
					failsafe.reference = prop.uid; //by GF's standard ref and model are always the same. They don't have to be but if the user fucks that up, it's their fault.
					failsafe.model = prop.uid;
					reg.entries.put(failsafe.reference, failsafe);
					reg.modified = true;
					regentry = failsafe;
				}
			}
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

        entryBox = new javax.swing.JComboBox<>();
        entryLabel = new javax.swing.JLabel();
        header1Separator = new javax.swing.JSeparator();
        mdlNumLabel = new javax.swing.JLabel();
        mdlNum = new javax.swing.JSpinner();
        headerSeparator = new javax.swing.JSeparator();
        locLabel = new javax.swing.JLabel();
        locXLabel = new javax.swing.JLabel();
        x = new javax.swing.JFormattedTextField();
        locYLabel = new javax.swing.JLabel();
        y = new javax.swing.JFormattedTextField();
        lozZLabel = new javax.swing.JLabel();
        z = new javax.swing.JFormattedTextField();
        scaleLabel = new javax.swing.JLabel();
        sx = new javax.swing.JFormattedTextField();
        sy = new javax.swing.JFormattedTextField();
        sz = new javax.swing.JFormattedTextField();
        scaleZLabel = new javax.swing.JLabel();
        scaleYLabel = new javax.swing.JLabel();
        scaleXLabel = new javax.swing.JLabel();
        rotLabel = new javax.swing.JLabel();
        rotXLabel = new javax.swing.JLabel();
        rx = new javax.swing.JFormattedTextField();
        rotYLabel = new javax.swing.JLabel();
        ry = new javax.swing.JFormattedTextField();
        rotZLabel = new javax.swing.JLabel();
        rz = new javax.swing.JFormattedTextField();
        mdlName = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        btnRemEntry = new javax.swing.JButton();
        btnNewEntry = new javax.swing.JButton();
        btnRegEdit = new javax.swing.JButton();
        PropPreview = new ctrmap.humaninterface.CustomH3DPreview();

        entryBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entryBoxActionPerformed(evt);
            }
        });

        entryLabel.setText("Prop entry");

        header1Separator.setOrientation(javax.swing.SwingConstants.VERTICAL);

        mdlNumLabel.setText("UID");

        mdlNum.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));
        mdlNum.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mdlNumStateChanged(evt);
            }
        });

        locLabel.setText("World location (3D floats)");

        locXLabel.setText("X");

        x.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        locYLabel.setText("Y");

        y.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        lozZLabel.setText("Z");

        z.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        scaleLabel.setText("Scale (multiplication floats)");

        sx.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        sy.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        sz.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        scaleZLabel.setText("Z");

        scaleYLabel.setText("Y");

        scaleXLabel.setText("X");

        rotLabel.setText("Rotation (angle floats)");

        rotXLabel.setText("X");

        rx.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        rotYLabel.setText("Y");

        ry.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        rotZLabel.setText("Z");

        rz.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        mdlName.setText("Model name: -");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnRemEntry.setText("Remove entry");
        btnRemEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemEntryActionPerformed(evt);
            }
        });

        btnNewEntry.setText("New entry");
        btnNewEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewEntryActionPerformed(evt);
            }
        });

        btnRegEdit.setForeground(new java.awt.Color(255, 51, 51));
        btnRegEdit.setText("[DANGER] Edit registry data");
        btnRegEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PropPreviewLayout = new javax.swing.GroupLayout(PropPreview);
        PropPreview.setLayout(PropPreviewLayout);
        PropPreviewLayout.setHorizontalGroup(
            PropPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        PropPreviewLayout.setVerticalGroup(
            PropPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerSeparator)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mdlName)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(mdlNumLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mdlNum, javax.swing.GroupLayout.PREFERRED_SIZE, 50, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(header1Separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(entryLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(entryBox, 0, 50, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(scaleLabel)
                            .addComponent(rotLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rotZLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rz))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lozZLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(z))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(locYLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(locXLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(x))
                            .addComponent(locLabel)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(scaleZLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sz))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(scaleYLabel)
                                    .addComponent(scaleXLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sx)
                                    .addComponent(sy)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rotYLabel)
                                    .addComponent(rotXLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rx, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ry, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRemEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNewEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRegEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(PropPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(header1Separator, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mdlNumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(mdlNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(entryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(entryBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mdlName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locXLabel)
                    .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locYLabel)
                    .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lozZLabel)
                    .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scaleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scaleXLabel)
                    .addComponent(sx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scaleYLabel)
                    .addComponent(sy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scaleZLabel)
                    .addComponent(sz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rotLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotXLabel)
                    .addComponent(rx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotYLabel)
                    .addComponent(ry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotZLabel)
                    .addComponent(rz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PropPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnNewEntry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemEntry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRegEdit)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void entryBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryBoxActionPerformed
		if (loaded && entryBox.getSelectedIndex() != -1) {
			saveAndRefresh();
		}
    }//GEN-LAST:event_entryBoxActionPerformed

    private void btnRegEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegEditActionPerformed
		if (regentry == null) {
			return;
		}
		ADPropRegistryEditor pre = new ADPropRegistryEditor(this);
		pre.loadRegistry(reg, propTextures);
		pre.setEntry(regentry.reference);
    }//GEN-LAST:event_btnRegEditActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveAndRefresh();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRemEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemEntryActionPerformed
		models.remove(entryBox.getSelectedIndex());
		props.props.remove(entryBox.getSelectedIndex());
		entryBox.removeItemAt(entryBox.getSelectedIndex());
		if (entryBox.getSelectedIndex() >= entryBox.getItemCount()) {
			entryBox.setSelectedIndex(entryBox.getSelectedIndex() - 1);
		} else {
			entryBox.setSelectedIndex(entryBox.getSelectedIndex());
		}
		props.modified = true;
    }//GEN-LAST:event_btnRemEntryActionPerformed

    private void btnNewEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewEntryActionPerformed
		loaded = false;
		GRProp newProp = new GRProp();
		newProp.uid = (prop != null) ? prop.uid : 0;
		Point defaultPos = CtrmapMainframe.mTileMapPanel.getWorldLocAtViewportCentre();
		newProp.x = defaultPos.x;
		newProp.z = defaultPos.y;
		newProp.updateName(reg);
		props.props.add(newProp);
		models.add(reg.getModel(newProp.uid));
		entryBox.addItem(String.valueOf(props.props.size() - 1));
		loaded = true;
		setProp(entryBox.getItemCount() - 1);
		props.modified = true;
    }//GEN-LAST:event_btnNewEntryActionPerformed

    private void mdlNumStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mdlNumStateChanged
		saveAndRefresh();
    }//GEN-LAST:event_mdlNumStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ctrmap.humaninterface.CustomH3DPreview PropPreview;
    private javax.swing.JButton btnNewEntry;
    private javax.swing.JButton btnRegEdit;
    private javax.swing.JButton btnRemEntry;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> entryBox;
    private javax.swing.JLabel entryLabel;
    private javax.swing.JSeparator header1Separator;
    private javax.swing.JSeparator headerSeparator;
    private javax.swing.JLabel locLabel;
    private javax.swing.JLabel locXLabel;
    private javax.swing.JLabel locYLabel;
    private javax.swing.JLabel lozZLabel;
    private javax.swing.JLabel mdlName;
    private javax.swing.JSpinner mdlNum;
    private javax.swing.JLabel mdlNumLabel;
    private javax.swing.JLabel rotLabel;
    private javax.swing.JLabel rotXLabel;
    private javax.swing.JLabel rotYLabel;
    private javax.swing.JLabel rotZLabel;
    private javax.swing.JFormattedTextField rx;
    private javax.swing.JFormattedTextField ry;
    private javax.swing.JFormattedTextField rz;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.JLabel scaleXLabel;
    private javax.swing.JLabel scaleYLabel;
    private javax.swing.JLabel scaleZLabel;
    private javax.swing.JFormattedTextField sx;
    private javax.swing.JFormattedTextField sy;
    private javax.swing.JFormattedTextField sz;
    private javax.swing.JFormattedTextField x;
    private javax.swing.JFormattedTextField y;
    private javax.swing.JFormattedTextField z;
    // End of variables declaration//GEN-END:variables

	@Override
	public void doSelectionLoop(MouseEvent e, Component parent, float[] mvMatrix, float[] projMatrix, int[] view, Vec3f cameraVec) {
		if (!(CtrmapMainframe.tool instanceof PropTool)) {
			return;
		}
		GLUgl2 glu = new GLUgl2();
		double closestDist = Float.MAX_VALUE;
		int closestIdx = -1;
		for (int i = 0; i < models.size(); i++) {
			if (models.get(i) == null) {
				continue;
			}
			GRProp p = props.props.get(i);
			float[][] box = models.get(i).boxVectors;
			boolean sysout = (p.name.equals("t101_bm_trees"));
			if (Utils.isBoxSelected(box, e, parent, new Vec3f(p.x, p.y, p.z), new Vec3f(p.scaleX, p.scaleY, p.scaleZ), new Vec3f(p.rotateX, p.rotateY, p.rotateZ), mvMatrix, projMatrix, view)) {
				H3DModel m = models.get(i);
				//GLU is buggy and sometimes completely fucks up the maths in certain camera angles. We can work around this by checking if the actual object is seen by the camera.
				boolean allow = false;
				for (int mesh = 0; mesh < m.meshes.size(); mesh++) {
					for (int vertex = 0; vertex < m.meshes.get(mesh).vertices.size(); vertex++) {
						H3DVertex v = m.meshes.get(mesh).vertices.get(vertex);
						float[] test = new float[3];
						glu.gluProject(v.position.x + p.x, v.position.y + p.y, v.position.z + p.z, mvMatrix, 0, projMatrix, 0, view, 0, test, 0);
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
				double dist = Utils.getDistanceFromVector(new Vec3f(p.x, p.y, p.z), cameraVec);
				if (Math.abs(dist) < closestDist && i != propIndex) {
					closestDist = Math.abs(dist);
					closestIdx = i;
				}
			}
		}
		if (closestIdx != -1) {
			setProp(closestIdx);
		}
	}
}
