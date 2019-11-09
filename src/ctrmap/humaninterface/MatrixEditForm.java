package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.mapmatrix.MapMatrix;
import ctrmap.formats.mapmatrix.MatrixCameraBoundaries;
import ctrmap.formats.text.LocationNames;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.text.NumberFormatter;

public class MatrixEditForm extends javax.swing.JPanel {

	public MapMatrix mm;

	private int curRegX = -1;
	private int curRegY = -1;

	public boolean loaded = false;

	private int currentCam = -1;
	private MatrixCameraBoundaries cam;

	/**
	 * Creates new form MatrixEditForm
	 */
	public MatrixEditForm() {
		initComponents();
		setFloatValueClass(new JFormattedTextField[]{northBound, southBound, westBound, eastBound});
	}

	public void setFloatValueClass(JFormattedTextField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(Float.class);
		}
	}

	public void loadMatrix(MapMatrix mm) {
		try {
			loaded = false;
			this.mm = mm;
			cam = null;
			currentCam = -1;
			zoneRefDropdown.removeAllItems();
			boundEntryBox.removeAllItems();
			if (mm != null) {
				allowExtended.setSelected(mm.hasLOD == 1);
				if (mZonePnl.zones != null) {
					for (int i = 0; i < mZonePnl.zones.length; i++) {
						zoneRefDropdown.addItem(i + " - " + LocationNames.getLocName(mZonePnl.zones[i].header.parentMap));
					}
				}
				for (int i = 0; i < mm.cambounds.size(); i++) {
					boundEntryBox.addItem(String.valueOf(i));
				}
				btnChunkTool.setSelected(true);
				switchTools();
				showRegion(0, 0);
				loaded = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveAll() {
		if (mm != null && loaded) {
			mm.hasLOD = (short) (allowExtended.isSelected() ? 1 : 0);
			if (curRegX != -1) {
				if (btnChunkTool.isSelected()) {
					mm.ids.set(curRegX, curRegY, (Short) chunkId.getValue());
					if (mm.hasLOD == 1) {
						mm.LOD.set(curRegX, curRegY, (Short) chunkLod.getValue());
					}
				} else if (btnMzTool.isSelected()) {
					setZoneByNumber();
				}
			}
			saveCam();
		}
	}

	public void saveCam() {
		if (cam != null) {
			cam.isRepeal = btnBoundTypeRepeal.isSelected() ? 1 : 0;
			cam.north = (Float) northBound.getValue();
			cam.south = (Float) southBound.getValue();
			cam.west = (Float) westBound.getValue();
			cam.east = (Float) eastBound.getValue();
		}
	}

	public boolean store(boolean dialog) {
		if (mm != null) {
			saveAll();
			byte[] newCamData = mm.assembleCamData();
			if (!Arrays.equals(mm.file.getFile(0), mm.assembleData()) || !Arrays.equals(Arrays.copyOf(mm.file.getFile(1), newCamData.length), newCamData)) {
				if (dialog) {
					int rsl = Utils.showSaveConfirmationDialog("Map matrix");
					switch (rsl) {
						case JOptionPane.YES_OPTION:
							break;
						case JOptionPane.NO_OPTION:
							return true;
						case JOptionPane.CANCEL_OPTION:
							return false;
					}
				}
				mm.write();
			}
		}
		return true;
	}

	public void showRegion(int x, int y) {
		saveAll();
		if (mm != null) {
			if (btnChunkTool.isSelected()) {
				curRegX = x;
				curRegY = y;
				if (curRegX != -1) {
					chunkId.setValue(mm.ids.get(x, y));
					if (mm.hasLOD == 1) {
						chunkLod.setValue(mm.LOD.get(x, y));
					}
				}
			} else if (btnMzTool.isSelected() && mm.hasLOD == 1) {
				curRegX = x;
				curRegY = y;
				if (curRegX != -1) {
					zoneRefNumber.setValue(mm.zones.get(x, y));
					zoneRefDropdown.setSelectedIndex(mm.zones.get(x, y));
				}
			}
		} else {
			curRegX = -1;
			curRegY = -1;
		}
	}

	private void showCam(int idx) {
		if (mm != null) {
			currentCam = idx;
			cam = mm.cambounds.get(idx);
			if (cam.isRepeal == 1) {
				btnBoundTypeRepeal.setSelected(true);
			} else {
				btnBoundTypeRestrict.setSelected(true);
			}
			northBound.setValue(cam.north);
			southBound.setValue(cam.south);
			westBound.setValue(cam.west);
			eastBound.setValue(cam.east);
		} else {
			currentCam = -1;
		}
		mMtxPanel.repaint();
	}

	public void drawToolGraphics(Graphics g, int imgstartx, int imgstarty) {
		if (mm != null) {
			if (btnChunkTool.isSelected()) {
				for (int x = 0; x < mm.width; x++) {
					for (int y = 0; y < mm.height; y++) {
						g.setColor(Color.BLACK);
						int regionX = x * 100 + imgstartx;
						int regionY = y * 100 + imgstarty;
						g.setFont(new Font(Font.SERIF, Font.PLAIN, 11));
						g.drawString("Region " + x + "x" + y, regionX + 5, regionY + 11);
						g.drawString("Refers to:", regionX + 5, regionY + 22);
						g.setColor(mm.ids.get(x, y) == -1 ? Color.GRAY : new Color(0, 150, 0));
						g.drawString((mm.ids.get(x, y) == -1 ? "No file" : "FIELD_DATA/" + mm.ids.get(x, y)), regionX + 5, regionY + 33);
						if (mm.hasLOD == 1 && mm.LOD.get(x, y) != -1) {
							g.setColor(Color.BLUE);
							g.drawString("LOD: " + mm.LOD.get(x, y), regionX + 5, regionY + 44);
						}
					}
				}
				g.setColor(Color.RED);
				if (MatrixSelector.hilightRegionX != -1) {
					g.drawRect(MatrixSelector.hilightRegionX * 100 + imgstartx, MatrixSelector.hilightRegionY * 100 + imgstarty, 100, 100);
				}
				if (MatrixSelector.selRegionX != -1) {
					g.drawRect(MatrixSelector.selRegionX * 100 + imgstartx, MatrixSelector.selRegionY * 100 + imgstarty, 100, 100);
				}
			} else if (btnMzTool.isSelected()) {
				for (int x = 0; x < mm.zones.getWidth(); x++) {
					for (int y = 0; y < mm.zones.getHeight(); y++) {
						g.setColor(Color.BLACK);
						int regionX = x * 25 + imgstartx;
						int regionY = y * 25 + imgstarty;
						g.setFont(new Font(Font.SERIF, Font.PLAIN, 11));
						g.drawString(String.valueOf(mm.zones.get(x, y)), regionX + 5, regionY + 11);
					}
				}
				g.setColor(Color.RED);
				if (MatrixSelector.hilightRegionX != -1) {
					g.drawRect(MatrixSelector.hilightRegionX * 25 + imgstartx, MatrixSelector.hilightRegionY * 25 + imgstarty, 25, 25);
				}
				if (MatrixSelector.selRegionX != -1) {
					g.drawRect(MatrixSelector.selRegionX * 25 + imgstartx, MatrixSelector.selRegionY * 25 + imgstarty, 25, 25);
				}
			} else if (btnCamTool.isSelected()) {
				float transform = 100f / 720f;
				for (int i = 0; i < mm.cambounds.size(); i++) {
					MatrixCameraBoundaries b = mm.cambounds.get(i);
					g.setColor(Color.WHITE);
					if (b.isRepeal == 1) {
						g.fillRect((int) (b.west * transform + imgstartx), (int) (b.north * transform + imgstarty), (int) ((b.east - b.west) * transform), (int) ((b.south - b.north) * transform));
						g.setColor(Color.BLACK);
					} else {
						g.setColor(Color.BLUE);
					}
					if (i == currentCam) {
						g.setColor(Color.RED);
					}
					g.drawRect((int) (b.west * transform + imgstartx), (int) (b.north * transform + imgstarty), (int) ((b.east - b.west) * transform), (int) ((b.south - b.north) * transform));
					g.setFont(new Font(Font.SERIF, Font.PLAIN, 15));
					g.drawString("C" + i, (int) (b.west * transform + imgstartx) + 2, (int) (b.north * transform + imgstarty) + 16);
				}
			}
		}
	}

	public void switchTools() {
		if (btnChunkTool.isSelected()) {
			MatrixSelector.selectSubChunks = false;
			enableChunkUI(true);
			enableCamUI(false);
			enableMZUI(false);
		} else if (btnMzTool.isSelected()) {
			MatrixSelector.selectSubChunks = true;
			enableChunkUI(false);
			enableCamUI(false);
			enableMZUI(true);
		} else if (btnCamTool.isSelected()) {
			enableChunkUI(false);
			enableCamUI(true);
			enableMZUI(false);
		}
		mMtxPanel.repaint();
	}

	private void enableChunkUI(boolean yesno) {
		chunkId.setEnabled(yesno);
		if (mm.hasLOD == 1) {
			chunkLod.setEnabled(yesno);
		} else {
			chunkLod.setEnabled(false);
		}
	}

	private void enableMZUI(boolean yesno) {
		if (mm.hasLOD == 1) {
			btnMzTool.setEnabled(true);
			zoneRefDropdown.setEnabled(yesno);
			btnFillChunk.setEnabled(yesno);
		} else {
			btnMzTool.setEnabled(false);
			zoneRefDropdown.setEnabled(false);
			btnFillChunk.setEnabled(false);
		}
	}

	private void enableCamUI(boolean yesno) {
		boundEntryBox.setEnabled(yesno);
		btnBoundTypeRepeal.setEnabled(yesno);
		btnBoundTypeRestrict.setEnabled(yesno);
		northBound.setEnabled(yesno);
		southBound.setEnabled(yesno);
		westBound.setEnabled(yesno);
		eastBound.setEnabled(yesno);
		btnNewCam.setEnabled(yesno);
		btnRemoveCam.setEnabled(yesno);
	}

	public void checkCamTool(MouseEvent e) {
		if (mm != null && btnCamTool.isSelected()) {
			for (int i = 0; i < mm.cambounds.size(); i++) {
				MatrixCameraBoundaries b = mm.cambounds.get(i);
				int imgstartx = (mMtxPanel.getWidth() - mMtxPanel.getFullImageWidth()) / 2;
				int imgstarty = (mMtxPanel.getHeight() - mMtxPanel.getFullImageHeight()) / 2;
				double xBase = b.west * 100d / 720d + imgstartx;
				double yBase = b.north * 100d / 720d + imgstarty;
				double x2Base = b.east * 100d / 720d + imgstartx;
				double y2Base = b.south * 100d / 720d + imgstarty;
				if (e.getX() > xBase && e.getX() < x2Base && e.getY() > yBase && e.getY() < y2Base) {
					setCam(i);
					break;
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

        toolBtnGroup = new javax.swing.ButtonGroup();
        boundTypeBtnGroup = new javax.swing.ButtonGroup();
        matrixControlLbl = new javax.swing.JLabel();
        btnAddRow = new javax.swing.JButton();
        btnRemoveRow = new javax.swing.JButton();
        btnAddCol = new javax.swing.JButton();
        btnRemoveCol = new javax.swing.JButton();
        chunkId = new javax.swing.JFormattedTextField();
        chunkIdLabel = new javax.swing.JLabel();
        chunkLodLabel = new javax.swing.JLabel();
        chunkLod = new javax.swing.JFormattedTextField();
        mtxControlSep = new javax.swing.JSeparator();
        mtxPropsLabel = new javax.swing.JLabel();
        allowExtended = new javax.swing.JCheckBox();
        mtxPropsSep = new javax.swing.JSeparator();
        chunkToolSep = new javax.swing.JSeparator();
        btnChunkTool = new javax.swing.JRadioButton();
        btnMzTool = new javax.swing.JRadioButton();
        mzReferenceLabel = new javax.swing.JLabel();
        zoneRefDropdown = new javax.swing.JComboBox<>();
        mzToolSep = new javax.swing.JSeparator();
        btnCamTool = new javax.swing.JRadioButton();
        boundTypeLabel = new javax.swing.JLabel();
        btnBoundTypeRestrict = new javax.swing.JRadioButton();
        btnBoundTypeRepeal = new javax.swing.JRadioButton();
        limitsLabel = new javax.swing.JLabel();
        northLabel = new javax.swing.JLabel();
        southLabel = new javax.swing.JLabel();
        westLabel = new javax.swing.JLabel();
        eastLabel = new javax.swing.JLabel();
        boundEntryLabel = new javax.swing.JLabel();
        boundEntryBox = new javax.swing.JComboBox<>();
        eastBound = new javax.swing.JFormattedTextField();
        westBound = new javax.swing.JFormattedTextField();
        southBound = new javax.swing.JFormattedTextField();
        northBound = new javax.swing.JFormattedTextField();
        btnFillChunk = new javax.swing.JButton();
        zoneRefNumber = new javax.swing.JFormattedTextField();
        btnSave = new javax.swing.JButton();
        btnNewCam = new javax.swing.JButton();
        btnRemoveCam = new javax.swing.JButton();

        matrixControlLbl.setText("Matrix control:");

        btnAddRow.setText("Add row");
        btnAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRowActionPerformed(evt);
            }
        });

        btnRemoveRow.setText("Remove row");
        btnRemoveRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveRowActionPerformed(evt);
            }
        });

        btnAddCol.setText("Add column");
        btnAddCol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddColActionPerformed(evt);
            }
        });

        btnRemoveCol.setText("Remove column");
        btnRemoveCol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveColActionPerformed(evt);
            }
        });

        chunkId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chunkIdActionPerformed(evt);
            }
        });

        chunkIdLabel.setText("Chunk reference");

        chunkLodLabel.setText("Chunk LOD reference:");

        chunkLod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chunkLodActionPerformed(evt);
            }
        });

        mtxPropsLabel.setText("Matrix properties:");

        allowExtended.setText("Allow LOD and Multizone");
        allowExtended.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allowExtendedActionPerformed(evt);
            }
        });

        toolBtnGroup.add(btnChunkTool);
        btnChunkTool.setText("Chunk tool");
        btnChunkTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChunkToolActionPerformed(evt);
            }
        });

        toolBtnGroup.add(btnMzTool);
        btnMzTool.setText("Multizone tool");
        btnMzTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMzToolActionPerformed(evt);
            }
        });

        mzReferenceLabel.setText("Zone reference:");

        zoneRefDropdown.setMaximumRowCount(20);
        zoneRefDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoneRefDropdownActionPerformed(evt);
            }
        });

        toolBtnGroup.add(btnCamTool);
        btnCamTool.setText("Camera boundary tool");
        btnCamTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCamToolActionPerformed(evt);
            }
        });

        boundTypeLabel.setText("Boundary type:");

        boundTypeBtnGroup.add(btnBoundTypeRestrict);
        btnBoundTypeRestrict.setText("Restrict");
        btnBoundTypeRestrict.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camUIActionPerformed(evt);
            }
        });

        boundTypeBtnGroup.add(btnBoundTypeRepeal);
        btnBoundTypeRepeal.setText("Repeal");
        btnBoundTypeRepeal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camUIActionPerformed(evt);
            }
        });

        limitsLabel.setText("Limits (World floats):");

        northLabel.setText("North");

        southLabel.setText("South");

        westLabel.setText("West");

        eastLabel.setText("East");

        boundEntryLabel.setText("Boundary entry:");

        boundEntryBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boundEntryBoxActionPerformed(evt);
            }
        });

        eastBound.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        eastBound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camUIActionPerformed(evt);
            }
        });

        westBound.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        westBound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camUIActionPerformed(evt);
            }
        });

        southBound.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        southBound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camUIActionPerformed(evt);
            }
        });

        northBound.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        northBound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camUIActionPerformed(evt);
            }
        });

        btnFillChunk.setText("Set for entire chunk");
        btnFillChunk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFillChunkActionPerformed(evt);
            }
        });

        zoneRefNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoneRefNumberActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnNewCam.setText("New");
        btnNewCam.setMaximumSize(new java.awt.Dimension(71, 23));
        btnNewCam.setMinimumSize(new java.awt.Dimension(71, 23));
        btnNewCam.setPreferredSize(new java.awt.Dimension(71, 23));
        btnNewCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewCamActionPerformed(evt);
            }
        });

        btnRemoveCam.setText("Remove");
        btnRemoveCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveCamActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mtxControlSep)
            .addComponent(mtxPropsSep)
            .addComponent(chunkToolSep)
            .addComponent(mzToolSep)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCamTool)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(boundEntryLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(boundEntryBox, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnNewCam, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemoveCam))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(limitsLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(boundTypeLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnBoundTypeRestrict))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(southLabel)
                                            .addComponent(northLabel)
                                            .addComponent(eastLabel)
                                            .addComponent(westLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(westBound)
                                            .addComponent(eastBound)
                                            .addComponent(southBound)
                                            .addComponent(northBound))))
                                .addGap(18, 18, 18)
                                .addComponent(btnBoundTypeRepeal)))))
                .addGap(0, 58, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(allowExtended)
                            .addComponent(matrixControlLbl)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnAddCol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnAddRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnRemoveRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRemoveCol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(mtxPropsLabel)
                            .addComponent(btnChunkTool)
                            .addComponent(btnMzTool)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mzReferenceLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chunkLodLabel)
                                            .addComponent(chunkIdLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(chunkId)
                                            .addComponent(chunkLod, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(btnFillChunk)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(zoneRefNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(zoneRefDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(matrixControlLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRow)
                    .addComponent(btnRemoveRow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCol)
                    .addComponent(btnRemoveCol))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mtxControlSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mtxPropsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allowExtended)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mtxPropsSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnChunkTool)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chunkId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chunkIdLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chunkLodLabel)
                    .addComponent(chunkLod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(chunkToolSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMzTool)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mzReferenceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zoneRefDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zoneRefNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFillChunk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mzToolSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCamTool)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(boundEntryLabel)
                    .addComponent(boundEntryBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewCam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveCam))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBoundTypeRepeal)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(boundTypeLabel)
                            .addComponent(btnBoundTypeRestrict))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(limitsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(northLabel)
                            .addComponent(northBound, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(southLabel)
                            .addComponent(southBound, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(westLabel)
                            .addComponent(westBound, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(eastLabel)
                            .addComponent(eastBound, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnChunkToolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChunkToolActionPerformed
		switchTools();
    }//GEN-LAST:event_btnChunkToolActionPerformed

    private void btnMzToolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMzToolActionPerformed
		switchTools();
    }//GEN-LAST:event_btnMzToolActionPerformed

    private void btnCamToolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCamToolActionPerformed
		switchTools();
    }//GEN-LAST:event_btnCamToolActionPerformed

    private void btnFillChunkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFillChunkActionPerformed
		zoneRefNumberActionPerformed(evt);
		if (mm != null && btnMzTool.isSelected() && curRegX != -1) {
			int toFill = (Integer) zoneRefNumber.getValue();
			int begX = curRegX - (curRegX % 4);
			int begY = curRegY - (curRegY % 4);
			for (int x = begX; x < begX + 4; x++) {
				for (int y = begY; y < begY + 4; y++) {
					mm.zones.set(x, y, (short) toFill);
				}
			}
		}
		mMtxPanel.repaint();
    }//GEN-LAST:event_btnFillChunkActionPerformed

    private void zoneRefNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoneRefNumberActionPerformed
		if ((Integer) zoneRefNumber.getValue() < zoneRefDropdown.getItemCount()) {
			zoneRefDropdown.setSelectedIndex((Integer) zoneRefNumber.getValue());
			setZoneByNumber();
		}
    }//GEN-LAST:event_zoneRefNumberActionPerformed

    private void zoneRefDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoneRefDropdownActionPerformed
		if (loaded && zoneRefDropdown.getSelectedIndex() != -1) {
			zoneRefNumber.setValue(zoneRefDropdown.getSelectedIndex());
			setZoneByNumber();
		}
    }//GEN-LAST:event_zoneRefDropdownActionPerformed

    private void btnAddColActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddColActionPerformed
		if (mm != null) {
			mm.ids.addColumn();
			if (mm.hasLOD == 1) {
				mm.LOD.addColumn();
				mm.zones.addColumns(4);
			}
			mm.width++;
			mMtxPanel.repaint();
		}
    }//GEN-LAST:event_btnAddColActionPerformed

    private void btnAddRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRowActionPerformed
		if (mm != null) {
			mm.ids.addRow();
			if (mm.hasLOD == 1) {
				mm.LOD.addRow();
				mm.zones.addRows(4);
			}
			mm.height++;
			mMtxPanel.repaint();
		}
    }//GEN-LAST:event_btnAddRowActionPerformed

    private void btnRemoveRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveRowActionPerformed
		if (mm != null) {
			mm.ids.removeRow();
			if (mm.hasLOD == 1) {
				mm.LOD.removeRow();
				mm.zones.removeRows(4);
			}
			mm.height--;
			mMtxPanel.repaint();
		}
    }//GEN-LAST:event_btnRemoveRowActionPerformed

    private void btnRemoveColActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveColActionPerformed
		if (mm != null) {
			mm.ids.removeColumn();
			if (mm.hasLOD == 1) {
				mm.LOD.removeColumn();
				mm.zones.removeColumns(4);
			}
			mm.width--;
			mMtxPanel.repaint();
		}
    }//GEN-LAST:event_btnRemoveColActionPerformed

    private void camUIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camUIActionPerformed
		saveCamAndUpdate();
    }//GEN-LAST:event_camUIActionPerformed

	public void setCam(int idx) {
		boundEntryBox.setSelectedIndex(idx);
	}

    private void boundEntryBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boundEntryBoxActionPerformed
		if (loaded) {
			saveAll();
			showCam(boundEntryBox.getSelectedIndex());
		}
    }//GEN-LAST:event_boundEntryBoxActionPerformed

    private void allowExtendedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allowExtendedActionPerformed
		if (mm != null) {
			mm.hasLOD = allowExtended.isSelected() ? (short) 1 : 0;
		}
		switchTools();
    }//GEN-LAST:event_allowExtendedActionPerformed

    private void chunkIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chunkIdActionPerformed
		saveAll();
		mMtxPanel.repaint();
    }//GEN-LAST:event_chunkIdActionPerformed

    private void chunkLodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chunkLodActionPerformed
		saveAll();
		mMtxPanel.repaint();
    }//GEN-LAST:event_chunkLodActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveAll();
		mMtxPanel.repaint();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnNewCamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewCamActionPerformed
		if (mm != null) {
			MatrixCameraBoundaries mcb = new MatrixCameraBoundaries();
			mm.cambounds.add(mcb);
			boundEntryBox.addItem(String.valueOf(mm.cambounds.size() - 1));
			setCam(mm.cambounds.size() - 1);
			mMtxPanel.repaint();
		}
    }//GEN-LAST:event_btnNewCamActionPerformed

    private void btnRemoveCamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveCamActionPerformed
		if (mm != null) {
			mm.cambounds.remove(cam);
			boundEntryBox.removeItemAt(boundEntryBox.getSelectedIndex());
			if (boundEntryBox.getSelectedIndex() >= boundEntryBox.getItemCount()) {
				boundEntryBox.setSelectedIndex(boundEntryBox.getSelectedIndex() - 1);
			} else {
				boundEntryBox.setSelectedIndex(boundEntryBox.getSelectedIndex());
			}
			mMtxPanel.repaint();
		}
    }//GEN-LAST:event_btnRemoveCamActionPerformed

	private void saveCamAndUpdate() {
		saveCam();
		mMtxPanel.repaint();
	}

	private void setZoneByNumber() {
		if (mm != null && btnMzTool.isSelected() && mm.hasLOD == 1 && curRegX != -1) {
			mm.zones.set(curRegX, curRegY, (Short) zoneRefNumber.getValue());
			mMtxPanel.repaint();
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allowExtended;
    private javax.swing.JComboBox<String> boundEntryBox;
    private javax.swing.JLabel boundEntryLabel;
    private javax.swing.ButtonGroup boundTypeBtnGroup;
    private javax.swing.JLabel boundTypeLabel;
    private javax.swing.JButton btnAddCol;
    private javax.swing.JButton btnAddRow;
    private javax.swing.JRadioButton btnBoundTypeRepeal;
    private javax.swing.JRadioButton btnBoundTypeRestrict;
    private javax.swing.JRadioButton btnCamTool;
    private javax.swing.JRadioButton btnChunkTool;
    private javax.swing.JButton btnFillChunk;
    private javax.swing.JRadioButton btnMzTool;
    private javax.swing.JButton btnNewCam;
    private javax.swing.JButton btnRemoveCam;
    private javax.swing.JButton btnRemoveCol;
    private javax.swing.JButton btnRemoveRow;
    private javax.swing.JButton btnSave;
    private javax.swing.JFormattedTextField chunkId;
    private javax.swing.JLabel chunkIdLabel;
    private javax.swing.JFormattedTextField chunkLod;
    private javax.swing.JLabel chunkLodLabel;
    private javax.swing.JSeparator chunkToolSep;
    private javax.swing.JFormattedTextField eastBound;
    private javax.swing.JLabel eastLabel;
    private javax.swing.JLabel limitsLabel;
    private javax.swing.JLabel matrixControlLbl;
    private javax.swing.JSeparator mtxControlSep;
    private javax.swing.JLabel mtxPropsLabel;
    private javax.swing.JSeparator mtxPropsSep;
    private javax.swing.JLabel mzReferenceLabel;
    private javax.swing.JSeparator mzToolSep;
    private javax.swing.JFormattedTextField northBound;
    private javax.swing.JLabel northLabel;
    private javax.swing.JFormattedTextField southBound;
    private javax.swing.JLabel southLabel;
    private javax.swing.ButtonGroup toolBtnGroup;
    private javax.swing.JFormattedTextField westBound;
    private javax.swing.JLabel westLabel;
    private javax.swing.JComboBox<String> zoneRefDropdown;
    private javax.swing.JFormattedTextField zoneRefNumber;
    // End of variables declaration//GEN-END:variables
}
