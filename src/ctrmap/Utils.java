package ctrmap;

import com.jogamp.opengl.glu.gl2.GLUgl2;
import static ctrmap.CtrmapMainframe.*;
import ctrmap.formats.vectors.Vec3f;
import ctrmap.resources.ResourceAccess;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.text.BadLocationException;

/**
 * Class to store methods used between various classes that do not extend the
 * same abstract base.
 */
public class Utils {

	public static byte[] getPadding(int offsetInPack, int length) {
		int endingOffset = (int) Math.ceil((offsetInPack + length) / 128f) * 128;
		return new byte[endingOffset - offsetInPack - length];
	}

	public static int ba2int(byte[] b) {
		int x = b[0];
		x = (x << 8) | (b[1] & 0xFF);
		x = (x << 8) | (b[2] & 0xFF);
		x = (x << 8) | (b[3] & 0xFF);
		return x;
	}

	public static boolean impreciseFloatEquals(float f0, float f1) {
		return Math.abs(f0 - f1) < 0.1f;
	}

	public static void showErrorMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}

	public static int showSaveConfirmationDialog(String changeSubject) {
		return JOptionPane.showConfirmDialog(null, changeSubject + " has been modified. Do you want to keep the changes?", "Save changes", JOptionPane.YES_NO_CANCEL_OPTION);
	}

	public static float getFloatFromDocument(JFormattedTextField docOwner) {
		try {
			String val = docOwner.getDocument().getText(0, docOwner.getDocument().getLength()).replace(',', '.');
			if (val.length() > 0 && !val.equals("-")) {
				return Float.valueOf(val);
			} else {
				return 0f;
			}
		} catch (BadLocationException | NumberFormatException ex) {
			return 0f;
		}
	}

	public static void mkDirsIfNotContains(File container, String[] requiredContents) {
		List<String> contents = Arrays.asList(container.list());
		for (int i = 0; i < requiredContents.length; i++) {
			if (!contents.contains(requiredContents[i])) {
				new File(container.getAbsolutePath() + "/" + requiredContents[i]).mkdir();
			}
		}
	}

	public static ImageIcon getImageIconFromResource(String respath) {
		return new ImageIcon(ResourceAccess.getByteArray(respath));
	}

	public static JRadioButton createGraphicalButton(String prefix) {
		JRadioButton ret = new JRadioButton(getImageIconFromResource(prefix + "_stale.png"));
		ret.setRolloverIcon(getImageIconFromResource(prefix + "_rollover.png"));
		ret.setPressedIcon(getImageIconFromResource(prefix + "_active.png"));
		ret.setSelectedIcon(getImageIconFromResource(prefix + "_active.png"));
		ret.setRolloverEnabled(true);
		return ret;
	}

	public static void switchToolUI(JComponent rightComponent) {
		jsp.setRightComponent(rightComponent);
		adjustSplitPanes();
		frame.revalidate();
	}

	public static void setGraphicUI(JComponent comp) {
		jsp.setLeftComponent(comp);
		adjustSplitPanes();
		frame.revalidate();
	}

	public static boolean checkBCHMagic(byte[] data) {
		if (data.length < 3) {
			return false;
		}
		if (data[0] == 'B' && data[1] == 'C' && data[2] == 'H') {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkMagic(byte[] data, String magic) {
		if (magic.length() > data.length) {
			return false;
		}
		byte[] test = Arrays.copyOfRange(data, 0, magic.length());
		return new String(test).equals(magic);
	}

	public static boolean isUTF8Capital(byte check) {
		return (check & 0xFF) >= 0x41 && (check & 0xFF) <= 0x5a;
	}

	public static boolean isBoxSelected(float[][] box, MouseEvent e, Component parent, Vec3f position, Vec3f scale, Vec3f rotate, float[] mvMatrix, float[] projMatrix, int[] view) {
		GLUgl2 glu = new GLUgl2();
		float[][] winPosArray = new float[box.length][3];
		for (int j = 0; j < box.length; j++) {
			Vec3f vec = new Vec3f(box[j][0] * scale.x, box[j][1] * scale.y, box[j][2] * scale.z);
			Vec3f rotatedVec = Utils.noGlRotatef(Utils.noGlRotatef(Utils.noGlRotatef(vec,
					new Vec3f(0f, 1f, 0f), Math.toRadians(rotate.y)),
					new Vec3f(1f, 0f, 0f), Math.toRadians(rotate.x)),
					new Vec3f(0f, 0f, 1f), Math.toRadians(rotate.z));
			glu.gluProject(rotatedVec.x + position.x, rotatedVec.y + position.y, rotatedVec.z + position.z, mvMatrix, 0, projMatrix, 0, view, 0, winPosArray[j], 0);
			winPosArray[j][1] = parent.getHeight() - winPosArray[j][1];
		}
		List<Polygon> polys = new ArrayList<>();
		for (int j = 0; j < winPosArray.length; j += 4) {
			Polygon polygon = new Polygon();
			List<Point> pts = new ArrayList<>();
			for (int k = 0; k < 4; k++) {
				pts.add(new Point((int) winPosArray[j + k][0], (int) winPosArray[j + k][1]));
			}
			/*float centerX = (pts.get(0).x + pts.get(1).x + pts.get(2).x + pts.get(3).x) / 4f;
			float centerZ = (pts.get(0).y + pts.get(1).y + pts.get(2).y + pts.get(3).y) / 4f;
			Collections.sort(pts, (Point o1, Point o2) -> {
				double baseAngle1 = (Math.toDegrees(Math.atan2(o1.y - centerZ, o1.x - centerX)) + 360) % 360;
				double baseAngle2 = (Math.toDegrees(Math.atan2(o2.y - centerZ, o2.x - centerX)) + 360) % 360;
				return (int) (baseAngle2 - baseAngle1);
			});*/
			for (int k = 0; k < 4; k++) {
				polygon.addPoint(pts.get(k).x, pts.get(k).y);
			}
			if (polygon.contains(e.getPoint())) {
				//GLU is buggy and sometimes completely fucks up the maths in certain camera angles. We can work around this by checking if the actual object is seen by the camera.
				return true;
			}
		}
		return false;
	}

	public static double getDistanceFromVector(Vec3f loc, Vec3f comp) {
		double dist = Math.pow((Math.pow(loc.x + comp.x, 2)
				+ Math.pow(loc.y + comp.y, 2)
				+ Math.pow(loc.z + comp.z, 2)
				* 1.0), 0.5);
		return Math.abs(dist);
	}

	/*
	From: https://stackoverflow.com/questions/31225062/rotating-a-vector-by-angle-and-axis-in-java
	 */
	public static Vec3f noGlRotatef(Vec3f vec, Vec3f axis, double theta) {
		float x, y, z;
		float u, v, w;
		x = vec.x;
		y = vec.y;
		z = vec.z;
		u = axis.x;
		v = axis.y;
		w = axis.z;
		float xPrime = (float) (u * (u * x + v * y + w * z) * (1d - Math.cos(theta))
				+ x * Math.cos(theta)
				+ (-w * y + v * z) * Math.sin(theta));
		float yPrime = (float) (v * (u * x + v * y + w * z) * (1d - Math.cos(theta))
				+ y * Math.cos(theta)
				+ (w * x - u * z) * Math.sin(theta));
		float zPrime = (float) (w * (u * x + v * y + w * z) * (1d - Math.cos(theta))
				+ z * Math.cos(theta)
				+ (-v * x + u * y) * Math.sin(theta));
		return new Vec3f(xPrime, yPrime, zPrime);
	}
}
