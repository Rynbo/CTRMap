package ctrmap;

import com.sun.javafx.geom.Vec3f;
import static ctrmap.CtrmapMainframe.*;
import ctrmap.resources.ResourceAccess;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.text.BadLocationException;

/**
 * Class to store methods used between various classes that do not extend the same abstract base.
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
		List<String> contents = (List<String>) Arrays.asList(container.list());
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
		float xPrime = (float)(u * (u * x + v * y + w * z) * (1d - Math.cos(theta))
				+ x * Math.cos(theta)
				+ (-w * y + v * z) * Math.sin(theta));
		float yPrime = (float)(v * (u * x + v * y + w * z) * (1d - Math.cos(theta))
				+ y * Math.cos(theta)
				+ (w * x - u * z) * Math.sin(theta));
		float zPrime = (float)(w * (u * x + v * y + w * z) * (1d - Math.cos(theta))
				+ z * Math.cos(theta)
				+ (-v * x + u * y) * Math.sin(theta));
		return new Vec3f(xPrime, yPrime, zPrime);
	}
}
