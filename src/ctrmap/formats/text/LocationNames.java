package ctrmap.formats.text;

import ctrmap.Workspace;

/**
 * Accessor class for obtaining location names from GAMETEXT files.
 */
public class LocationNames {

	public static TextFile textfile;

	public static void loadFromGarc() {
		if (Workspace.isXY()) {
			textfile = new TextFile(Workspace.getWorkspaceFile(Workspace.ArchiveType.GAMETEXT, 72));
		} else {
			if (Workspace.isOADemo()) {
				textfile = new TextFile(Workspace.getWorkspaceFile(Workspace.ArchiveType.GAMETEXT, 91));
			} else {
				textfile = new TextFile(Workspace.getWorkspaceFile(Workspace.ArchiveType.GAMETEXT, 90));
			}
		}
	}

	public static String getLocName(int parentLoc) {
		String out = textfile.getLine(parentLoc);
		if (out == null) {
			return "NullPointerException";
		} else {
			return out;
		}
	}
}
