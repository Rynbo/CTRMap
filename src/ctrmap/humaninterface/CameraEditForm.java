package ctrmap.humaninterface;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Utils;
import ctrmap.formats.cameradata.CameraData;
import ctrmap.formats.cameradata.CameraDataFile;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.text.NumberFormatter;

/**
 * GUI editor for CameraDataFile structs.
 */
public class CameraEditForm extends javax.swing.JPanel {

	/**
	 * Creates new form CameraEditForm
	 */
	public CameraDataFile f;
	public CameraData cam;
	public int camIndex = -1;
	public boolean loaded = false;

	public CameraEditForm() {
		initComponents();
		setFloatValueClass(new JFormattedTextField[]{pitchShift1, pitchShift2, yawShift1, yawShift2, plrDist1, plrDist2, pitch1, pitch2, yaw1, yaw2, roll1, roll2});
		((NumberFormatter) transTime.getFormatter()).setValueClass(Integer.class);
	}

	public void setFloatValueClass(JFormattedTextField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(Float.class);
		}
	}

	public void unload() {
		entryBox.setSelectedIndex(-1);
		showCamera(-1);
		cam = null;
		f = null;
		loaded = false;
	}

	public void loadDataFile(CameraDataFile cdf) {
		loaded = false;
		cam = null;
		f = cdf;
		entryBox.removeAllItems();
		for (int i = 0; i < cdf.camData.size(); i++) {
			entryBox.addItem(String.valueOf(i));
		}
		loaded = true;
		if (cdf.numEntries > 0) {
			entryBox.setSelectedIndex(0);
		}
		if (mTileMapPanel.loaded) {
			mTileMapPanel.verifyCompat();
		}
	}

	public void showCamera(int entryNum){
		showCamera(entryNum, true);
	}
	
	public void showCamera(int entryNum, boolean save) {
		if (!loaded) return;
		if (entryNum == -1) {
			setComponentsEnabled(new Component[]{d1active, d2active, d1default, d2default, fov1Slider, fov2Slider, x, y, w, h, motion, roll1, roll2, pitch1,
				pitch2, yaw1, yaw2, plrDist1, plrDist2, transTime, pitchShift1, pitchShift2, yawShift1, yawShift2, entryBox, btnRemove, btnSave}, false);
			return;
		} else {
			setComponentsEnabled(new Component[]{d1active, d2active, d1default, d2default, fov1Slider, fov2Slider, x, y, w, h, motion, roll1, roll2, pitch1,
				pitch2, yaw1, yaw2, plrDist1, plrDist2, transTime, pitchShift1, pitchShift2, yawShift1, yawShift2, entryBox, btnRemove, btnSave}, true);
		}
		camIndex = entryNum;
		if (save) saveCamera(false);
		cam = f.camData.get(entryNum);
		neutralCheckbox.setSelected(cam.isNeutral != 0);
		transTime.setValue(Short.toUnsignedInt(cam.transitionPeriod) * 35);
		motion.setSelectedIndex(getMotionIndex(cam.movementDirection));
		if (cam.unknown01or03 == 03){
			motion.setSelectedIndex(2); //different static
		}
		x.setValue(Short.toUnsignedInt(cam.boundX1));
		y.setValue(Short.toUnsignedInt(cam.boundY1));
		w.setValue(Short.toUnsignedInt(cam.boundX2) - Short.toUnsignedInt(cam.boundX1));
		h.setValue(Short.toUnsignedInt(cam.boundY2) - Short.toUnsignedInt(cam.boundY1));
		d1active.setSelected(cam.acceptCoords1 == 1);
		d2active.setSelected(cam.acceptCoords2 == 1);
		if (!(cam.isFirstEnabled != -1 && cam.isSecondEnabled != -1)) {
			d1default.setSelected(cam.isFirstEnabled != -1);
			d2default.setSelected(cam.isSecondEnabled != -1);
		} else {
			d1default.setSelected(false);
			d2default.setSelected(false);
		}
		setActiveEnabled();
		pitchShift1.setValue(cam.coords1.pitchShift);
		yawShift1.setValue(cam.coords1.yawShift);
		pitchShift2.setValue(cam.coords2.pitchShift);
		yawShift2.setValue(cam.coords2.yawShift);
		pitch1.setValue(cam.coords1.pitch);
		pitch2.setValue(cam.coords2.pitch);
		yaw1.setValue(cam.coords1.yaw);
		yaw2.setValue(cam.coords2.yaw);
		roll1.setValue(cam.coords1.roll);
		roll2.setValue(cam.coords2.roll);
		fov1Slider.setValue(Math.round(cam.coords1.FOV));
		fov2Slider.setValue(Math.round(cam.coords2.FOV));
		plrDist1.setValue(cam.coords1.distanceFromTarget);
		plrDist2.setValue(cam.coords2.distanceFromTarget);
		layer.setValue(cam.layer);
		setNeutralEnabled();
		mTileMapPanel.firePropertyChange(TileMapPanel.PROP_IMGSTATE, false, true);
	}

	public void commitAndSwitch(int switchNum) {
		entryBox.requestFocus(); //first focus something else so that the listener is called
		FocusAdapter adapter = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				btnSave.removeFocusListener(this);
				entryBox.setSelectedIndex(switchNum);
			}
		};
		btnSave.addFocusListener(adapter);
		btnSave.requestFocus();
	}

	public void setSelectedIndex(int idx) {
		entryBox.setSelectedIndex(idx);
	}

	public void saveCamera(){
		saveCamera(true);
	}
	
	public void saveCamera(boolean update) {
		if (cam == null) {
			return;
		}
		CameraData cam2 = new CameraData();
		cam2.acceptCoords1 = (d1active.isSelected()) ? 0x01 : 0x00;
		cam2.acceptCoords2 = (d2active.isSelected()) ? 0x01 : 0x00;
		cam2.boundX1 = ((Integer) x.getValue()).shortValue();
		cam2.boundY1 = ((Integer) y.getValue()).shortValue();
		cam2.boundX2 = (short) ((Integer) x.getValue() + (Integer) w.getValue()); //add it from the ground up to prevent Java signed shorts
		cam2.boundY2 = (short) ((Integer) y.getValue() + (Integer) h.getValue());
		if (d1default.isSelected() || d2default.isSelected()) {
			cam2.isFirstEnabled = (!d1default.isSelected()) ? (short) -1 : 0;
			cam2.isSecondEnabled = (!d2default.isSelected()) ? (short) -1 : 0;
		} else {
			if (cam.isFirstEnabled == -1 && cam.isSecondEnabled == -1){
				cam2.isFirstEnabled = -1;
				cam2.isSecondEnabled = -1;
			}
			else {
				cam2.isFirstEnabled = 0;
				cam2.isSecondEnabled = 0;
			}
		}
		if ((Integer) transTime.getValue() > 65535) {
			transTime.setValue(65535);
		}
		cam2.transitionPeriod = (short) Math.round((Integer) transTime.getValue() / 35f);
		if (neutralCheckbox.isSelected()){
			if (cam.isNeutral != 0) cam2.isNeutral = cam.isNeutral; else cam2.isNeutral = 0x2;
			//OA uses 0x1 for this while XY uses 0x2. If there is a difference, I'll be sure to link it to the workspace but for now, 2 (XY) is the default.
		}
		else{
			cam2.isNeutral = 0x0;
		}
		cam2.coords1.pitchShift = (Float) pitchShift1.getValue();
		cam2.coords1.yawShift = (Float) yawShift1.getValue();
		cam2.coords1.pitch = (Float) pitch1.getValue();
		cam2.coords1.yaw = (Float) yaw1.getValue();
		cam2.coords1.roll = (Float) roll1.getValue();
		cam2.coords1.FOV = (float) fov1Slider.getValue();
		cam2.coords1.distanceFromTarget = (Float) plrDist1.getValue();

		cam2.coords2.pitchShift = (Float) pitchShift2.getValue();
		cam2.coords2.yawShift = (Float) yawShift2.getValue();
		cam2.coords2.pitch = (Float) pitch2.getValue();
		cam2.coords2.yaw = (Float) yaw2.getValue();
		cam2.coords2.roll = (Float) roll2.getValue();
		cam2.coords2.FOV = (float) fov2Slider.getValue();
		cam2.coords2.distanceFromTarget = (Float) plrDist2.getValue();
		cam2.layer = ((Integer) layer.getValue()).byteValue();
		cam2.unknown01or03 = cam.unknown01or03;
		if (cam.unknown01or03 == 0x03 && getMotionRaw(motion.getSelectedIndex()) == 0x07){
			cam2.movementDirection = cam.movementDirection; //might not be set to static, so we'll keep it the same as 03 overrides it
		}
		else{
			cam2.movementDirection = getMotionRaw(motion.getSelectedIndex());
		}
		if (!equalsData(cam, cam2)){
			//set this after verifying as it is a nonstandard op specific to this editor and we don't wanna invoke the save dialog on autocorrect
			cam2.unknown01or03 = (cam2.movementDirection == 0x07) ? 0x03 : 0x01; //no idea what the difference between 00 and 01 is but 03 is static and so is MD7, so doing this is a good measure to allow users to change static cameras to dynamic without messing with this unknown
			f.camData.set(f.camData.indexOf(cam), cam2);
			cam = cam2;
			f.modified = true;
		}
		else {
		}
		if (update) showCamera(entryBox.getSelectedIndex(), false);
	}

	public boolean equalsData(CameraData c1, CameraData c2) {
		if (c1.acceptCoords1 != c2.acceptCoords1) {
			return false;
		}
		if (c1.acceptCoords2 != c2.acceptCoords2) {
			return false;
		}
		if (c1.boundX1 != c2.boundX1) {
			return false;
		}
		if (c1.boundX2 != c2.boundX2) {
			return false;
		}
		if (c1.boundY1 != c2.boundY1) {
			return false;
		}
		if (c1.boundY2 != c2.boundY2) {
			return false;
		}
		if (c1.isFirstEnabled != c2.isFirstEnabled) {
			return false;
		}
		if (c1.isSecondEnabled != c2.isSecondEnabled) {
			return false;
		}
		if (c1.isNeutral != c2.isNeutral) {
			return false;
		}
		if (c1.layer != c2.layer) {
			return false;
		}
		if (c1.movementDirection != c2.movementDirection) {
			return false;
		}
		if (c1.transitionPeriod != c2.transitionPeriod) {
			return false;
		}
		if (c1.unknown00 != c2.unknown00) {
			return false;
		}
		if (c1.unknown01or03 != c2.unknown01or03) {
			return false;
		}
		//yep we even need to check the coordinates manually
		if (!Utils.impreciseFloatEquals(Math.round(c1.coords1.FOV), Math.round(c2.coords1.FOV))) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords1.pitchShift, c2.coords1.pitchShift)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords1.yawShift, c2.coords1.yawShift)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords1.distanceFromTarget, c2.coords1.distanceFromTarget)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords1.pitch, c2.coords1.pitch)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords1.yaw, c2.coords1.yaw)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords1.roll, c2.coords1.roll)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(Math.round(c1.coords2.FOV), Math.round(c2.coords2.FOV))) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords2.pitchShift, c2.coords2.pitchShift)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords2.yawShift, c2.coords2.yawShift)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords2.distanceFromTarget, c2.coords2.distanceFromTarget)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords2.pitch, c2.coords2.pitch)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords2.yaw, c2.coords2.yaw)) {
			return false;
		}
		if (!Utils.impreciseFloatEquals(c1.coords2.roll, c2.coords2.roll)) {
			return false;
		}
		return true;
	}

	public float getFloatFromField(JFormattedTextField field) {
		try {
			float ret = Float.parseFloat(field.getText());
			return ret;
		} catch (NumberFormatException | NullPointerException ex) {
			JOptionPane.showMessageDialog(null, "Warning: Couldn't retrieve floating point value of " + field.getText());
			return 0f;
		}
	}

	public boolean store(boolean dialog) {
		if (f != null) {
			saveCamera();
			if (f.modified) {
				if (dialog) {
					int rsl = Utils.showSaveConfirmationDialog("Camera data");
					switch (rsl) {
						case JOptionPane.YES_OPTION:
							break; //continue to save
						case JOptionPane.NO_OPTION:
							f.modified = false;
							return true;
						case JOptionPane.CANCEL_OPTION:
							return false;
					}
				}
				f.write();
				f.modified = false;
			}
		}
		return true;
	}

	public int getMotionIndex(short raw) {
		switch (raw) {
			case 1:
				return 0;
			case 2:
				return 1;
			case 7:
				return 2;
		}
		System.out.println("Unknown motion type: " + raw);
		return 2;
	}

	public short getMotionRaw(int index) {
		switch (index) {
			case 0:
				return 1;
			case 1:
				return 2;
			case 2:
				return 7;
		}
		return 7;
	}

	public void setNeutralEnabled() {
		setComponentsEnabled(new Component[]{d1active, d2active, d1default, d2default, fov1Slider, fov2Slider, x, y, w, h, motion, roll1, roll2, pitch1,
			pitch2, yaw1, yaw2, plrDist1, plrDist2, transTime, pitchShift1, pitchShift2, yawShift1, yawShift2}, !(neutralCheckbox.isSelected()));
	}

	public void setActiveEnabled() {
		setComponentsEnabled(new Component[]{fov1Slider, roll1, pitch1, yaw1, plrDist1, pitchShift1, yawShift1}, d1active.isSelected() && !d1default.isSelected());
		setComponentsEnabled(new Component[]{fov2Slider, roll2, pitch2, yaw2, plrDist2, pitchShift2, yawShift2}, d2active.isSelected() && !d2default.isSelected());
	}

	public void setComponentsEnabled(Component[] ca, boolean b) {
		for (Component c : ca) {
			c.setEnabled(b);
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

        entryLabel = new javax.swing.JLabel();
        entryBox = new javax.swing.JComboBox<>();
        neutralCheckbox = new javax.swing.JCheckBox();
        transTimeLabel = new javax.swing.JLabel();
        motionLabel = new javax.swing.JLabel();
        motion = new javax.swing.JComboBox<>();
        headerSeparator = new javax.swing.JSeparator();
        motSeparator = new javax.swing.JSeparator();
        paramsSeparator = new javax.swing.JSeparator();
        worldLocLabel = new javax.swing.JLabel();
        worldLocSeparator = new javax.swing.JSeparator();
        xLabel = new javax.swing.JLabel();
        x = new javax.swing.JSpinner();
        yLabel = new javax.swing.JLabel();
        y = new javax.swing.JSpinner();
        wLabel = new javax.swing.JLabel();
        w = new javax.swing.JSpinner();
        hLabel = new javax.swing.JLabel();
        h = new javax.swing.JSpinner();
        data1Separator = new javax.swing.JSeparator();
        data1Label = new javax.swing.JLabel();
        pitchShift1Label = new javax.swing.JLabel();
        pitchShift1 = new javax.swing.JFormattedTextField();
        yawShift1Label = new javax.swing.JLabel();
        yawShift1 = new javax.swing.JFormattedTextField();
        pitch1Label = new javax.swing.JLabel();
        pitch1 = new javax.swing.JFormattedTextField();
        yaw1Label = new javax.swing.JLabel();
        yaw1 = new javax.swing.JFormattedTextField();
        fov1Slider = new javax.swing.JSlider();
        fov1Label = new javax.swing.JLabel();
        plrDist1Label = new javax.swing.JLabel();
        plrDist1 = new javax.swing.JFormattedTextField();
        d1active = new javax.swing.JCheckBox();
        data2Label = new javax.swing.JLabel();
        d2active = new javax.swing.JCheckBox();
        pitchShift2Label = new javax.swing.JLabel();
        pitch2Label = new javax.swing.JLabel();
        pitchShift2 = new javax.swing.JFormattedTextField();
        pitch2 = new javax.swing.JFormattedTextField();
        yaw2 = new javax.swing.JFormattedTextField();
        yawShift2 = new javax.swing.JFormattedTextField();
        data2Separator = new javax.swing.JSeparator();
        yawShift2Label = new javax.swing.JLabel();
        yaw2Label = new javax.swing.JLabel();
        fov2Label = new javax.swing.JLabel();
        fov2Slider = new javax.swing.JSlider();
        plrDist2Label = new javax.swing.JLabel();
        plrDist2 = new javax.swing.JFormattedTextField();
        controlsSeparator = new javax.swing.JSeparator();
        btnSave = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        transTime = new javax.swing.JFormattedTextField();
        d1default = new javax.swing.JCheckBox();
        d2default = new javax.swing.JCheckBox();
        pitch2deg = new javax.swing.JLabel();
        yaw2deg = new javax.swing.JLabel();
        pitch1deg = new javax.swing.JLabel();
        yaw1deg = new javax.swing.JLabel();
        msLabel = new javax.swing.JLabel();
        layer = new javax.swing.JSpinner();
        layerLabel = new javax.swing.JLabel();
        motSeparator1 = new javax.swing.JSeparator();
        roll1Label = new javax.swing.JLabel();
        roll1 = new javax.swing.JFormattedTextField();
        roll2Label = new javax.swing.JLabel();
        roll2 = new javax.swing.JFormattedTextField();
        roll1deg = new javax.swing.JLabel();
        roll2deg = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(370, 620));
        setPreferredSize(new java.awt.Dimension(370, 620));

        entryLabel.setText("Camera entry");

        entryBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entryBoxActionPerformed(evt);
            }
        });

        neutralCheckbox.setText("Neutral camera zone");
        neutralCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        neutralCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neutralCheckboxActionPerformed(evt);
            }
        });

        transTimeLabel.setText("Transition time");

        motionLabel.setText("Motion");

        motion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Horizontal (R > L : 1 > 2)", "Vertical (Down > Up : 1 > 2)", "Static" }));

        motSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        motSeparator.setPreferredSize(new java.awt.Dimension(60, 10));

        worldLocLabel.setText("World location");

        worldLocSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);

        xLabel.setText("X:");

        x.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));

        yLabel.setText("Y:");

        y.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));

        wLabel.setText("W:");

        w.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));

        hLabel.setText("H:");

        h.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));

        data1Label.setText("Data 1:");

        pitchShift1Label.setText("Pitch shift");

        pitchShift1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        yawShift1Label.setText("Yaw shift");

        yawShift1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        pitch1Label.setText("Pitch");

        pitch1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        yaw1Label.setText("Yaw");

        yaw1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        fov1Slider.setMajorTickSpacing(45);
        fov1Slider.setMaximum(180);
        fov1Slider.setMinimum(-180);
        fov1Slider.setPaintLabels(true);
        fov1Slider.setPaintTicks(true);
        fov1Slider.setValue(30);

        fov1Label.setText("FOV");

        plrDist1Label.setText("Distance from player");

        plrDist1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        d1active.setText("Active");
        d1active.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                d1activeActionPerformed(evt);
            }
        });

        data2Label.setText("Data 2:");

        d2active.setText("Active");
        d2active.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                d2activeActionPerformed(evt);
            }
        });

        pitchShift2Label.setText("Pitch shift");

        pitch2Label.setText("Pitch");

        pitchShift2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        pitch2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        yaw2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        yawShift2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        yawShift2Label.setText("Yaw shift");

        yaw2Label.setText("Yaw");

        fov2Label.setText("FOV");

        fov2Slider.setMajorTickSpacing(45);
        fov2Slider.setMaximum(180);
        fov2Slider.setMinimum(-180);
        fov2Slider.setPaintLabels(true);
        fov2Slider.setPaintTicks(true);
        fov2Slider.setValue(30);

        plrDist2Label.setText("Distance from player");

        plrDist2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        btnSave.setText("Apply");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove entry");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnAdd.setText("New entry");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        transTime.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        transTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transTimeActionPerformed(evt);
            }
        });

        d1default.setText("Default");
        d1default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                d1defaultActionPerformed(evt);
            }
        });

        d2default.setText("Default");
        d2default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                d2defaultActionPerformed(evt);
            }
        });

        pitch2deg.setText("°");

        yaw2deg.setText("°");
        yaw2deg.setToolTipText("");

        pitch1deg.setText("°");

        yaw1deg.setText("°");

        msLabel.setText("ms");

        layer.setModel(new javax.swing.SpinnerNumberModel(0, -127, 127, 1));

        layerLabel.setText("Layer");

        motSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        motSeparator1.setPreferredSize(new java.awt.Dimension(1, 10));

        roll1Label.setText("Roll");

        roll1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        roll2Label.setText("Roll");

        roll2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        roll1deg.setText("°");

        roll2deg.setText("°");
        roll2deg.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerSeparator)
            .addComponent(paramsSeparator)
            .addComponent(data1Separator)
            .addComponent(data2Separator, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(controlsSeparator, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(entryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(entryBox, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(layerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(layer, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(neutralCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(worldLocLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(worldLocSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(xLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(w, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(h, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fov1Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fov1Slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(data1Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(d1default, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(d1active))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(data2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(d2default)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(d2active))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fov2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fov2Slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(plrDist2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plrDist2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(plrDist1Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plrDist1))
                    .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(transTimeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(transTime, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(msLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motion, 0, 164, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(roll1Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(roll1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(yaw1Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pitch1Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pitch1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                    .addComponent(yaw1))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(pitch1deg)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(pitchShift1Label))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(yaw1deg)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(yawShift1Label)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pitchShift1)
                                    .addComponent(yawShift1)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(roll1deg)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(roll2Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(roll2, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(yaw2Label)
                                    .addComponent(pitch2Label))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(yaw2, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                                    .addComponent(pitch2))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(pitch2deg)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(pitchShift2Label))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(yaw2deg)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(yawShift2Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pitchShift2)
                                    .addComponent(yawShift2)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(roll2deg)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(motSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(entryLabel)
                        .addComponent(entryBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(neutralCheckbox)
                        .addComponent(layerLabel)
                        .addComponent(layer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(transTimeLabel)
                        .addComponent(motion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(motionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(motSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(transTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(msLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(paramsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(worldLocSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(xLabel)
                        .addComponent(yLabel)
                        .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(wLabel)
                        .addComponent(w, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(hLabel)
                        .addComponent(h, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(worldLocLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(data1Separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(data1Label)
                    .addComponent(d1active)
                    .addComponent(d1default))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pitch1Label)
                    .addComponent(pitch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pitch1deg)
                    .addComponent(pitchShift1Label)
                    .addComponent(pitchShift1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yaw1Label)
                    .addComponent(yaw1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yaw1deg)
                    .addComponent(yawShift1Label)
                    .addComponent(yawShift1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(roll1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(roll1Label)
                    .addComponent(roll1deg))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fov1Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fov1Slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(plrDist1Label)
                    .addComponent(plrDist1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(data2Separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(data2Label)
                    .addComponent(d2active)
                    .addComponent(d2default))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pitch2Label)
                    .addComponent(pitch2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pitch2deg)
                    .addComponent(pitchShift2Label)
                    .addComponent(pitchShift2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yaw2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yaw2deg)
                    .addComponent(yawShift2Label)
                    .addComponent(yawShift2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yaw2Label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(roll2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(roll2Label)
                    .addComponent(roll2deg))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fov2Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fov2Slider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(plrDist2Label)
                    .addComponent(plrDist2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(controlsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void d2activeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_d2activeActionPerformed
		setActiveEnabled();
    }//GEN-LAST:event_d2activeActionPerformed

    private void neutralCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neutralCheckboxActionPerformed
		cam.isNeutral = (neutralCheckbox.isSelected()) ? 2 : 0;
		setNeutralEnabled();
    }//GEN-LAST:event_neutralCheckboxActionPerformed

    private void d1defaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_d1defaultActionPerformed
		setActiveEnabled();
    }//GEN-LAST:event_d1defaultActionPerformed

    private void d1activeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_d1activeActionPerformed
		setActiveEnabled();
    }//GEN-LAST:event_d1activeActionPerformed

    private void d2defaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_d2defaultActionPerformed
		setActiveEnabled();
    }//GEN-LAST:event_d2defaultActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveCamera();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void entryBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryBoxActionPerformed
		if (entryBox.getSelectedIndex() != -1 && loaded) {
			saveCamera();
			showCamera(entryBox.getSelectedIndex(), true);
		}
    }//GEN-LAST:event_entryBoxActionPerformed

    private void transTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transTimeActionPerformed
		// TODO add your handling code here:
    }//GEN-LAST:event_transTimeActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
		if (cam != null) {
			int index = entryBox.getSelectedIndex();
			f.camData.remove(index);
			cam = null;
			entryBox.removeItemAt(index);
			if (f.camData.size() == index) {
				entryBox.setSelectedIndex(index - 1);
			}
			showCamera(entryBox.getSelectedIndex());
			f.modified = true;
		}
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
		f.camData.add(new CameraData());
		int index = entryBox.getItemCount();
		entryBox.addItem(String.valueOf(index));
		entryBox.setSelectedIndex(index);
		showCamera(index);
		f.modified = true;
    }//GEN-LAST:event_btnAddActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSave;
    private javax.swing.JSeparator controlsSeparator;
    private javax.swing.JCheckBox d1active;
    private javax.swing.JCheckBox d1default;
    private javax.swing.JCheckBox d2active;
    private javax.swing.JCheckBox d2default;
    private javax.swing.JLabel data1Label;
    private javax.swing.JSeparator data1Separator;
    private javax.swing.JLabel data2Label;
    private javax.swing.JSeparator data2Separator;
    private javax.swing.JComboBox<String> entryBox;
    private javax.swing.JLabel entryLabel;
    private javax.swing.JLabel fov1Label;
    private javax.swing.JSlider fov1Slider;
    private javax.swing.JLabel fov2Label;
    private javax.swing.JSlider fov2Slider;
    private javax.swing.JSpinner h;
    private javax.swing.JLabel hLabel;
    private javax.swing.JSeparator headerSeparator;
    private javax.swing.JSpinner layer;
    private javax.swing.JLabel layerLabel;
    private javax.swing.JSeparator motSeparator;
    private javax.swing.JSeparator motSeparator1;
    private javax.swing.JComboBox<String> motion;
    private javax.swing.JLabel motionLabel;
    private javax.swing.JLabel msLabel;
    private javax.swing.JCheckBox neutralCheckbox;
    private javax.swing.JSeparator paramsSeparator;
    private javax.swing.JFormattedTextField pitch1;
    private javax.swing.JLabel pitch1Label;
    private javax.swing.JLabel pitch1deg;
    private javax.swing.JFormattedTextField pitch2;
    private javax.swing.JLabel pitch2Label;
    private javax.swing.JLabel pitch2deg;
    private javax.swing.JFormattedTextField pitchShift1;
    private javax.swing.JLabel pitchShift1Label;
    private javax.swing.JFormattedTextField pitchShift2;
    private javax.swing.JLabel pitchShift2Label;
    private javax.swing.JFormattedTextField plrDist1;
    private javax.swing.JLabel plrDist1Label;
    private javax.swing.JFormattedTextField plrDist2;
    private javax.swing.JLabel plrDist2Label;
    private javax.swing.JFormattedTextField roll1;
    private javax.swing.JLabel roll1Label;
    private javax.swing.JLabel roll1deg;
    private javax.swing.JFormattedTextField roll2;
    private javax.swing.JLabel roll2Label;
    private javax.swing.JLabel roll2deg;
    private javax.swing.JFormattedTextField transTime;
    private javax.swing.JLabel transTimeLabel;
    private javax.swing.JSpinner w;
    private javax.swing.JLabel wLabel;
    private javax.swing.JLabel worldLocLabel;
    private javax.swing.JSeparator worldLocSeparator;
    private javax.swing.JSpinner x;
    private javax.swing.JLabel xLabel;
    private javax.swing.JSpinner y;
    private javax.swing.JLabel yLabel;
    private javax.swing.JFormattedTextField yaw1;
    private javax.swing.JLabel yaw1Label;
    private javax.swing.JLabel yaw1deg;
    private javax.swing.JFormattedTextField yaw2;
    private javax.swing.JLabel yaw2Label;
    private javax.swing.JLabel yaw2deg;
    private javax.swing.JFormattedTextField yawShift1;
    private javax.swing.JLabel yawShift1Label;
    private javax.swing.JFormattedTextField yawShift2;
    private javax.swing.JLabel yawShift2Label;
    // End of variables declaration//GEN-END:variables
}
