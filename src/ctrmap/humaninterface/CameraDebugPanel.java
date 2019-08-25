package ctrmap.humaninterface;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ctrmap.CtrmapMainframe;
import ctrmap.formats.cameradata.CameraData;
import ctrmap.formats.cameradata.CameraDataFile;

public class CameraDebugPanel extends JPanel{
	private static final long serialVersionUID = 6939274613528701294L;
	public JComboBox<Integer> camSelect = new JComboBox<>();
	public JLabel unknown0 = new JLabel();
	public JLabel unknown1 = new JLabel();
	public JLabel unknown2 = new JLabel();
	public JLabel unknown3 = new JLabel();
	public JLabel unknown4 = new JLabel();
	public JLabel unknown5 = new JLabel();
	public JLabel unknown6 = new JLabel();
	public JLabel unknown7 = new JLabel();
	public JLabel coordinateData1 = new JLabel();
	public JLabel coordinateData2 = new JLabel();
	public JLabel range = new JLabel();
	private CameraDataFile f;
	public CameraDebugPanel() {
		super();
		camSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        @SuppressWarnings("unchecked")
				JComboBox<Integer> cb = (JComboBox<Integer>)e.getSource();
		        Integer selItem = (Integer)cb.getSelectedItem();
		        if (selItem == null) {
		        	return;
		        }
		        showCamera(f.camData.get(selItem));
			}
		});
	}
	public void addCD(CameraDataFile file) {
		this.f = file;
		camSelect.removeAllItems();
		if (f.numEntries == 0) {
			JOptionPane.showMessageDialog(null, "No camera data in this AD");
			return;
		}
		for (int i = 0; i < f.numEntries; i++) {
			camSelect.addItem(i);
		}
		camSelect.setSelectedIndex(0);
		showCamera(f.camData.get(0));
		camSelect.setSelectedItem(0);
		camSelect.setMaximumSize(new Dimension(200, 20));
		Font defFont = new Font("Arial", Font.PLAIN, 24);
		unknown0.setFont(defFont);
		unknown1.setFont(defFont);
		unknown2.setFont(defFont);
		unknown3.setFont(defFont);
		unknown4.setFont(defFont);
		unknown5.setFont(defFont);
		unknown6.setFont(defFont);
		unknown7.setFont(defFont);
		coordinateData1.setFont(defFont);
		coordinateData2.setFont(defFont);
		range.setFont(defFont);
		this.add(camSelect);
		this.add(unknown0);
		this.add(unknown1);
		this.add(unknown2);
		this.add(unknown3);
		this.add(unknown4);
		this.add(unknown5);
		this.add(unknown6);
		this.add(unknown7);
		this.add(coordinateData1);
		this.add(coordinateData2);
		this.add(range);
		CtrmapMainframe.frame.revalidate();
	}
	public void showCamera(CameraData cd) {
		unknown0.setText("First 16 bytes = " + Arrays.toString(cd.unknownBytes));
		unknown1.setText("01 01 duration something = " + Integer.toHexString(cd.acceptCoords2) + " " + Integer.toHexString(cd.acceptCoords1) + " " + Integer.toHexString(cd.transitionPeriod) + " ");
		unknown2.setText("Unknown usually FF 00 00 03/1 = " + Integer.toHexString(cd.layer) + " " + Integer.toHexString(cd.isNeutral) + " " + Integer.toHexString(cd.unknown01or03));
		unknown3.setText("Unknown int after coordinates usually 00000000 = " + Integer.toHexString(cd.unknownInt1));
		unknown4.setText("Movement direction = " + Integer.toHexString(cd.movementDirection));
		unknown6.setText("Unknown FFFF after enabled data = " + Integer.toHexString(cd.unknownFFFF & 0xFFFF));
		String cdatas1 = new String("Coordinate Data1: \n" + "X shift: " + cd.coords1.pitchShift + "\n Z shift: " + cd.coords1.yawShift
				+ " \n Pitch: " + cd.coords1.pitch + "\n Yaw: " + cd.coords1.yaw + " \n FOV: " + cd.coords1.FOV + "\n Distance: "
				+ cd.coords1.distanceFromTarget + "\n YawNoControlsChange: " + cd.coords1.roll);
		String cdatas2 = new String("Coordinate Data2: \n" + "X shift: " + cd.coords2.pitchShift + "\n Z shift: " + cd.coords2.yawShift
				+ " \n Pitch: " + cd.coords2.pitch + "\n Yaw: " + cd.coords2.yaw + " \n FOV: " + cd.coords2.FOV + "\n Distance: "
				+ cd.coords2.distanceFromTarget + "\n YawNoControlsChange: " + cd.coords2.roll);
		coordinateData1.setText("<html>" + cdatas1.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
		coordinateData2.setText("<html>" + cdatas2.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
		range.setText("Range Y: " + cd.boundY1 + " - " + cd.boundY2 + "; Range X: " + cd.boundX1 + " - " + cd.boundX2);
	}
}