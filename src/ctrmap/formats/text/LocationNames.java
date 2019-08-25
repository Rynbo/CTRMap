package ctrmap.formats.text;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.Workspace;

public class LocationNames {
	public static TextFile textfile;
	public static void loadFromGarc(Workspace.GameType game){
		if (game == Workspace.GameType.XY){
			textfile = new TextFile(mWorkspace.getWorkspaceFile(Workspace.ArchiveType.GAMETEXT, 72));
		}
		else{
			textfile = new TextFile(mWorkspace.getWorkspaceFile(Workspace.ArchiveType.GAMETEXT, 90));
		}
	}
	public static String getLocName(int parentLoc){
		String out = textfile.getLine(parentLoc);
		if (out == null) return "NullPointerException"; else return out;
	}
}
