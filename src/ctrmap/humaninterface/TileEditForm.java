package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.tilemap.EditorTileset;
import ctrmap.formats.tilemap.TileTemplate;
import ctrmap.formats.tilemap.Tilemap;
import ctrmap.humaninterface.tools.AbstractTool;
import ctrmap.humaninterface.tools.EditTool;
import ctrmap.humaninterface.tools.FillTool;
import ctrmap.humaninterface.tools.SetTool;
import java.text.ParseException;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.DefaultFormatterFactory;

public class TileEditForm extends javax.swing.JPanel {

	private boolean isLocked = false;
	private DefaultListModel<String> currentListModel = new DefaultListModel<>();
	private DefaultListModel<String>[] models = new DefaultListModel[13];
	public EditorTileset tileset;
	public AbstractTool tool;

	/**
	 * Creates new form TileEditForm
	 */
	public TileEditForm() {
		initComponents();
		byte0.setName("0");
		byte1.setName("1");
		byte2.setName("2");
		byte3.setName("3");
		tileset = mWorkspace.getTileset();
		tileList.addListSelectionListener((ListSelectionEvent e) -> {
			if (tileList.getSelectedValue() == null) {
				return;
			}
			int binary = tileset.getTemplate(parseCat1(), parseCat2(), tileList.getSelectedValue()).binary;
			byte0.setValue((binary >>> 24) & 0xFF);
			byte1.setValue((binary >>> 16) & 0xFF);
			byte2.setValue((binary >>> 8) & 0xFF);
			byte3.setValue(binary & 0xFF);
		});
		for (int i = 0; i < 13; i++) {
			models[i] = new DefaultListModel<>();
		}
		for (int i = 0; i < tileset.tiles.length; i++) {
			TileTemplate tile = tileset.tiles[i];
			models[tile.cat1 * 4 + tile.cat2].addElement(tile.name);
		}

		JSpinner.DefaultEditor edt = (JSpinner.DefaultEditor) byte0.getEditor();
		edt.getTextField().setFormatterFactory(new HexFormatterFactory());
		edt = (JSpinner.DefaultEditor) byte1.getEditor();
		edt.getTextField().setFormatterFactory(new HexFormatterFactory());
		edt = (JSpinner.DefaultEditor) byte2.getEditor();
		edt.getTextField().setFormatterFactory(new HexFormatterFactory());
		edt = (JSpinner.DefaultEditor) byte3.getEditor();
		edt.getTextField().setFormatterFactory(new HexFormatterFactory());

		ChangeListener rawDataChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tool instanceof EditTool) {
					if (Selector.selTileX != -1) {
						Tilemap region = mTileMapPanel.getRegionForTile(Selector.selTileX, Selector.selTileY);
						if (region == null) {
							return;
						}
						JSpinner src = (JSpinner) e.getSource();
						region.rawTileData[Selector.selTileX % 40][Selector.selTileY % 40][src.getName().charAt(0) - '0'] = ((Integer) src.getValue()).byteValue();
						region.modified = true;
						region.updateImage();
						mTileMapPanel.scaleImage(mTileMapPanel.tilemapScale);
						showTile(Selector.selTileX, Selector.selTileY, true);
					}
				} else if (tool instanceof SetTool) {
					((SetTool) tool).actTileData = new byte[]{((Integer) byte0.getValue()).byteValue(), ((Integer) byte1.getValue()).byteValue(),
						((Integer) byte2.getValue()).byteValue(), ((Integer) byte3.getValue()).byteValue()};
				} else if (tool instanceof FillTool) {
					FillTool t = (FillTool) tool;
					t.actTileData = new byte[]{((Integer) byte0.getValue()).byteValue(), ((Integer) byte1.getValue()).byteValue(),
						((Integer) byte2.getValue()).byteValue(), ((Integer) byte3.getValue()).byteValue()};
					int startX = Math.min(t.originX, t.lastX);
					int startY = Math.min(t.originY, t.lastY);
					if (startX == -1) {
						return;
					}
					int width = Math.abs(t.lastX - t.originX);
					int height = Math.abs(t.lastY - t.originY);
					for (int x = 0; x < width + 1; x++) {
						for (int y = 0; y < height + 1; y++) {
							Tilemap reg = mTileMapPanel.getRegionForTile(startX + x, startY + y);
							if (reg != null){
								reg.setTileData((startX + x) % 40, (startY + y) % 40, t.actTileData);
							}
						}
					}
					//could have affected multiple regions, update them all
					mTileMapPanel.updateAll();
				}
				mTileMapPanel.firePropertyChange(TileMapPanel.PROP_IMGSTATE, false, true);
			}
		};

		byte0.addChangeListener(rawDataChangeListener);
		byte1.addChangeListener(rawDataChangeListener);
		byte2.addChangeListener(rawDataChangeListener);
		byte3.addChangeListener(rawDataChangeListener);
	}

	public void setTileLabel(String str) {
		tileId.setText(str);
	}

	private void setCat2Texts() {
		if (normal.isSelected()) {
			cat2b1.setText("Plain");
			cat2b2.setText("Encounters");
			cat2b3.setText("Puzzles");
			cat2b4.setText("Footprints");
			setCat2Enabled(true);
		} else if (water.isSelected()) {
			cat2b1.setText("Surf");
			cat2b2.setText("Waterfall");
			cat2b3.setText("Edge");
			cat2b4.setText("Edge2");
			setCat2Enabled(true);
		} else if (action.isSelected()) {
			cat2b1.setText("Trigger");
			cat2b2.setText("HM/Ride");
			cat2b3.setText("Sittables");
			cat2b4.setText("Rails");
			setCat2Enabled(true);
		} else {
			setCat2Enabled(false);
			cat2b1.setText("<N/A>");
			cat2b2.setText("<N/A>");
			cat2b3.setText("<N/A>");
			cat2b4.setText("<N/A>");
		}
	}

	private void setCat2Enabled(boolean b) {
		cat2b1.setEnabled(b);
		cat2b2.setEnabled(b);
		cat2b3.setEnabled(b);
		cat2b4.setEnabled(b);
	}

	private int parseCat1() {
		if (normal.isSelected()) {
			return 0;
		} else if (water.isSelected()) {
			return 1;
		} else if (action.isSelected()) {
			return 2;
		}
		return -1;
	}

	private int parseCat2() {
		if (cat2b1.isSelected()) {
			return 0;
		} else if (cat2b2.isSelected()) {
			return 1;
		} else if (cat2b3.isSelected()) {
			return 2;
		} else if (cat2b4.isSelected()) {
			return 3;
		}
		return -1;
	}

	private boolean showListModel() {
		int cat1 = parseCat1();
		int cat2 = parseCat2();
		int result = cat1 * 4 + cat2;
		if (cat1 == -1 || cat2 == -1) {
			result = 12;
		}
		tileList.setModel(models[result]);
		if (Selector.selTileX != -1 && result != 12) {
			tileList.setSelectedValue(tileset.getTemplate(Utils.ba2int(mTileMapPanel.getRegionForTile(Selector.selTileX, Selector.selTileY).getTileData(Selector.selTileX % 40, Selector.selTileY % 40))).name, true);
		}
		return result != 12;
	}

	public void showTile(int x, int y, boolean overrideLock) {
		if (!isLocked || overrideLock) {
			tileId.setText("Tile " + x + "x" + y);
			Tilemap region = mTileMapPanel.getRegionForTile(x, y);
			if (region != null) {
				byte[] data = region.getTileData(x % 40, y % 40);
				byte0.setValue((int) data[0] & 0xFF);
				byte1.setValue((int) data[1] & 0xFF);
				byte2.setValue((int) data[2] & 0xFF);
				byte3.setValue((int) data[3] & 0xFF);
				int dataInt = Utils.ba2int(data);
				TileTemplate tile = tileset.getTemplate(dataInt);
				if (tile.name.equals("Unknown")) {
					cat1BtnGroup.clearSelection();
					cat2BtnGroup.clearSelection();
				} else {
					switch (tile.cat1) {
						case 0:
							normal.setSelected(true);
							break;
						case 1:
							water.setSelected(true);
							break;
						case 2:
							action.setSelected(true);
							break;
					}
					switch (tile.cat2) {
						case 0:
							cat2b1.setSelected(true);
							break;
						case 1:
							cat2b2.setSelected(true);
							break;
						case 2:
							cat2b3.setSelected(true);
							break;
						case 3:
							cat2b4.setSelected(true);
							break;
					}
				}
				setCat2Texts();
				if (showListModel() != false) {
					tileList.setSelectedValue(tile.name, true);
				}
			} else {
				tileId.setText(tileId.getText() + " - Void");
			}
		}
	}

	public void makeTile() {
		Selector.unfocus();
		tileId.setText("New Tile");
		byte0.setValue(0);
		byte1.setValue(0);
		byte2.setValue(0);
		byte3.setValue(0);
		cat1BtnGroup.clearSelection();
		cat2BtnGroup.clearSelection();
		showListModel();
	}

	public void lockTile(boolean state) {
		isLocked = state;
	}

	private static class HexFormatterFactory extends DefaultFormatterFactory {

		private static final long serialVersionUID = 6795960205384589732L;

		@Override
		public JFormattedTextField.AbstractFormatter getDefaultFormatter() {
			return new JFormattedTextField.AbstractFormatter() {
				private static final long serialVersionUID = 6795960205384589733L;

				@Override
				public Object stringToValue(String text) throws ParseException {
					try {
						return Integer.valueOf(text, 16);
					} catch (NumberFormatException nfe) {
						throw new ParseException(text, 0);
					}
				}

				@Override
				public String valueToString(Object value) throws ParseException {
					return Integer.toHexString((Integer) value).toUpperCase();
				}
			};
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

        cat1BtnGroup = new javax.swing.ButtonGroup();
        cat2BtnGroup = new javax.swing.ButtonGroup();
        tileId = new javax.swing.JLabel();
        byte0 = new javax.swing.JSpinner();
        byte1 = new javax.swing.JSpinner();
        byte2 = new javax.swing.JSpinner();
        byte3 = new javax.swing.JSpinner();
        normal = new javax.swing.JRadioButton();
        water = new javax.swing.JRadioButton();
        action = new javax.swing.JRadioButton();
        listScrollPane = new javax.swing.JScrollPane();
        tileList = new javax.swing.JList<>();
        cat2Panel = new javax.swing.JPanel();
        cat2b4 = new javax.swing.JRadioButton();
        cat2b2 = new javax.swing.JRadioButton();
        cat2b1 = new javax.swing.JRadioButton();
        cat2b3 = new javax.swing.JRadioButton();
        btnUpdate = new javax.swing.JButton();

        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setMinimumSize(new java.awt.Dimension(300, 200));
        setPreferredSize(new java.awt.Dimension(300, 200));

        tileId.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tileId.setText("No tile selected");

        byte0.setAlignmentX(0.0F);
        byte0.setMinimumSize(new java.awt.Dimension(40, 20));
        byte0.setPreferredSize(new java.awt.Dimension(40, 20));

        byte1.setAlignmentX(0.0F);
        byte1.setMinimumSize(new java.awt.Dimension(40, 20));
        byte1.setPreferredSize(new java.awt.Dimension(40, 20));

        byte2.setAlignmentX(0.0F);
        byte2.setMinimumSize(new java.awt.Dimension(40, 20));
        byte2.setPreferredSize(new java.awt.Dimension(40, 20));

        byte3.setAlignmentX(0.0F);
        byte3.setMinimumSize(new java.awt.Dimension(40, 20));
        byte3.setPreferredSize(new java.awt.Dimension(40, 20));

        cat1BtnGroup.add(normal);
        normal.setText("Normal");
        normal.setActionCommand("normal");
        normal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        normal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                normalActionPerformed(evt);
            }
        });

        cat1BtnGroup.add(water);
        water.setText("Water");
        water.setActionCommand("water");
        water.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        water.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waterActionPerformed(evt);
            }
        });

        cat1BtnGroup.add(action);
        action.setText("Action");
        action.setActionCommand("action");
        action.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        action.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionActionPerformed(evt);
            }
        });

        tileList.setModel(currentListModel);
        tileList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tileList.setMaximumSize(new java.awt.Dimension(65535, 80));
        tileList.setMinimumSize(new java.awt.Dimension(32, 80));
        listScrollPane.setViewportView(tileList);

        cat2Panel.setAlignmentX(0.0F);
        cat2Panel.setPreferredSize(new java.awt.Dimension(270, 37));

        cat2BtnGroup.add(cat2b4);
        cat2b4.setText("<N/A>");
        cat2b4.setActionCommand("cat2b4");
        cat2b4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cat2b4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cat2b4ActionPerformed(evt);
            }
        });

        cat2BtnGroup.add(cat2b2);
        cat2b2.setText("<N/A>");
        cat2b2.setActionCommand("cat2b2");
        cat2b2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cat2b2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cat2b2ActionPerformed(evt);
            }
        });

        cat2BtnGroup.add(cat2b1);
        cat2b1.setText("<N/A>");
        cat2b1.setActionCommand("cat2b1");
        cat2b1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cat2b1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cat2b1ActionPerformed(evt);
            }
        });

        cat2BtnGroup.add(cat2b3);
        cat2b3.setText("<N/A>");
        cat2b3.setActionCommand("cat2b3");
        cat2b3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cat2b3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cat2b3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout cat2PanelLayout = new javax.swing.GroupLayout(cat2Panel);
        cat2Panel.setLayout(cat2PanelLayout);
        cat2PanelLayout.setHorizontalGroup(
            cat2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cat2PanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cat2b1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cat2b2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cat2b3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cat2b4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cat2PanelLayout.setVerticalGroup(
            cat2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cat2PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cat2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cat2b1)
                    .addComponent(cat2b2)
                    .addComponent(cat2b3)
                    .addComponent(cat2b4))
                .addContainerGap())
        );

        btnUpdate.setText("Force apply");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cat2Panel, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(byte0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(byte1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(byte2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(byte3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(normal)
                                .addGap(0, 0, 0)
                                .addComponent(water, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(action))
                            .addComponent(tileId, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 24, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 23, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tileId)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(byte0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(byte1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(byte2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(byte3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(normal)
                    .addComponent(water)
                    .addComponent(action))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cat2Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpdate))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionActionPerformed
		setCat2Texts();
		showListModel();
    }//GEN-LAST:event_actionActionPerformed

    private void waterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waterActionPerformed
		setCat2Texts();
		showListModel();
    }//GEN-LAST:event_waterActionPerformed

    private void normalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_normalActionPerformed
		setCat2Texts();
		showListModel();
    }//GEN-LAST:event_normalActionPerformed

    private void cat2b1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat2b1ActionPerformed
		showListModel();
    }//GEN-LAST:event_cat2b1ActionPerformed

    private void cat2b2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat2b2ActionPerformed
		showListModel();
    }//GEN-LAST:event_cat2b2ActionPerformed

    private void cat2b3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat2b3ActionPerformed
		showListModel();
    }//GEN-LAST:event_cat2b3ActionPerformed

    private void cat2b4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat2b4ActionPerformed
		showListModel();
    }//GEN-LAST:event_cat2b4ActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
		byte0.getChangeListeners()[0].stateChanged(new ChangeEvent(byte0));
    }//GEN-LAST:event_btnUpdateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton action;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JSpinner byte0;
    private javax.swing.JSpinner byte1;
    private javax.swing.JSpinner byte2;
    private javax.swing.JSpinner byte3;
    private javax.swing.ButtonGroup cat1BtnGroup;
    private javax.swing.ButtonGroup cat2BtnGroup;
    private javax.swing.JPanel cat2Panel;
    private javax.swing.JRadioButton cat2b1;
    private javax.swing.JRadioButton cat2b2;
    private javax.swing.JRadioButton cat2b3;
    private javax.swing.JRadioButton cat2b4;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JRadioButton normal;
    private javax.swing.JLabel tileId;
    private javax.swing.JList<String> tileList;
    private javax.swing.JRadioButton water;
    // End of variables declaration//GEN-END:variables
}
