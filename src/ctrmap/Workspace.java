package ctrmap;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.formats.tilemap.EditorTileset;
import ctrmap.formats.garc.GARC;
import ctrmap.formats.text.LocationNames;
import ctrmap.humaninterface.LoadingDialog;
import ctrmap.resources.ResourceAccess;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * The core of CTRMap filesystem access which seamlessly takes care of everything the editor wants from the game.
 */
public class Workspace {

	static Preferences prefs;
	public static String WORKSPACE_PATH;
	public static String GAMEDIR_PATH;
	public static String ESPICA_PATH;
	public static boolean TILESET_DEFAULT;
	public static String TILESET_PATH;

	public static File areadata;
	public static File fielddata;
	public static File mapmatrix;
	public static File gametext;
	public static File zonedata;
	public static File buildingmodels;
	public static File npcregistries;
	public static File movemodels;
	public static File temp;

	public static File persist_config;
	public static ArrayList<String> persist_paths = new ArrayList<>();

	public static GARC ad;
	public static GARC gr;
	public static GARC mm;
	public static GARC texts;
	public static GARC zo;
	public static GARC bm;
	public static GARC npcreg;
	public static GARC npcmm;

	public static String[] musicNames;
	
	public static GameType game;
	public static boolean valid = false;

	public static void loadWorkspace() {
		prefs = Preferences.userRoot().node(Workspace.class.getName());
		WORKSPACE_PATH = prefs.get("WORKSPACE_PATH", "");
		GAMEDIR_PATH = prefs.get("GAMEDIR_PATH", "");
		ESPICA_PATH = prefs.get("ESPICA_PATH", "");
		TILESET_DEFAULT = prefs.getBoolean("TILESET_DEFAULT", true);
		TILESET_PATH = prefs.get("TILESET_PATH", "");
	}
	
	public static void createWorkspace(String wspath, String gamepath, String espicapath, boolean tilesetDefault, String customTilesetPath){
		WORKSPACE_PATH = wspath;
		GAMEDIR_PATH = gamepath;
		ESPICA_PATH = espicapath;
		TILESET_DEFAULT = tilesetDefault;
		TILESET_PATH = customTilesetPath;
	}

	public static EditorTileset getTileset() {
		if (!TILESET_DEFAULT) {
			if (TILESET_PATH != null) {
				File f = new File(TILESET_PATH);
				if (f.exists()) {
					EditorTileset ts = new EditorTileset(f);
					if (ts.tiles != null) { //if yes, the loader failed, wrong magic most likely
						return ts;
					}
				}
			}
		} else {
			return new EditorTileset(ResourceAccess.getStream("DefaultTileset.mets"));
		}
		Utils.showErrorMessage("Invalid tileset", "The tileset is corrupt. Restoring defaults.");
		TILESET_DEFAULT = true;
		return new EditorTileset(ResourceAccess.getStream("DefaultTileset.mets"));
	}

	public static void validate(Component parent) {
		ArrayList<String> errors = new ArrayList<>();
		if (WORKSPACE_PATH == null) {
			errors.add("Workspace path not set");
		} else {
			File ws = new File(WORKSPACE_PATH);
			if (!ws.exists()) {
				errors.add("Workspace path not found");
			} else {
				Utils.mkDirsIfNotContains(ws, new String[]{
					"areadata",
					"fielddata",
					"mapmatrix",
					"gametext",
					"zonedata",
					"buildingmodels",
					"npcregistries",
					"movemodels",
					"temp"
				});
				temp = new File(ws + "/temp");
				persist_config = new File(WORKSPACE_PATH + "/ctrmap_persist.txt");
				persist_paths.clear();
				if (persist_config.exists()) {
					try {
						Scanner scanner = new Scanner(persist_config);
						scanner.useDelimiter("\n"); //for better crossplatformness, force the Linux endline everywhere
						while (scanner.hasNextLine()) {
							persist_paths.add(WORKSPACE_PATH + scanner.nextLine());
						}
						scanner.close();
					} catch (IOException ex) {
						Logger.getLogger(Workspace.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}
		if (GAMEDIR_PATH == null) {
			errors.add("Game directory path not set");
		} else {
			File basepath = new File(GAMEDIR_PATH);
			if (!basepath.exists()) {
				errors.add("Game directory path not found");
			} else {
				//check for game type by finding the last arc in the romfs - a/2/9/8 for ORAS and a/2/7/0 for XY
				File oras = new File(basepath + "/a/2/9/8");
				File xy = new File(basepath + "/a/2/7/0");
				if (oras.exists()) {
					game = GameType.ORAS;
				} else if (xy.exists()) {
					game = GameType.XY;
				} else {
					errors.add("Could not detect game version");
				}
				if (game != null) {
					//check needed archives
					areadata = new File(basepath + getArchivePath(ArchiveType.AREA_DATA, game));
					fielddata = new File(basepath + getArchivePath(ArchiveType.FIELD_DATA, game));
					mapmatrix = new File(basepath + getArchivePath(ArchiveType.MAP_MATRIX, game));
					gametext = new File(basepath + getArchivePath(ArchiveType.GAMETEXT, game));
					zonedata = new File(basepath + getArchivePath(ArchiveType.ZONE_DATA, game));
					buildingmodels = new File(basepath + getArchivePath(ArchiveType.BUILDING_MODELS, game));
					npcregistries = new File(basepath + getArchivePath(ArchiveType.NPC_REGISTRIES, game));
					movemodels = new File(basepath + getArchivePath(ArchiveType.MOVE_MODELS, game));
//					musicNames = BCSArStringLoader.getStrings(new File(basepath + getArchivePath(ArchiveType.SOUND_BCSAR, game)));
					if (!areadata.exists()) {
						errors.add("AreaData GARC not found");
					}
					if (!fielddata.exists()) {
						errors.add("FieldData GARC not found");
					}
					if (!mapmatrix.exists()) {
						errors.add("MapMatrix GARC not found");
					}
					if (!gametext.exists()) {
						errors.add("GameText GARC not found");
					}
					if (!zonedata.exists()) {
						errors.add("ZoneData GARC not found");
					}
					if (!buildingmodels.exists()) {
						errors.add("ZoneData GARC not found");
					}
					if (!npcregistries.exists()) {
						errors.add("NPCRegistries GARC not found");
					}
					if (!movemodels.exists()) {
						errors.add("MoveModels GARC not found");
					}
				}
			}
		}
		if (errors.isEmpty()) {
			valid = true;
			loadArchives();
			LocationNames.loadFromGarc();
			CtrmapMainframe.mBuilder.loadGARCs();
			CtrmapMainframe.mZonePnl.loadEverything();
		} else {
			valid = false;
			StringBuilder sb = new StringBuilder();
			for (String s : errors) {
				sb.append(s);
				sb.append("\n");
			}
			JOptionPane.showMessageDialog(parent, sb.toString(), "Setup Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void addPersist(File f) {
		if (!persist_paths.contains(f.getAbsolutePath())) {
			persist_paths.add(f.getAbsolutePath());
		}
	}

	public static String getArchivePath(ArchiveType archiveType, GameType gameType) {
		if (gameType == GameType.XY) {
			switch (archiveType) {
				case AREA_DATA:
					return "/a/0/1/3";
				case FIELD_DATA:
					return "/a/0/4/1";
				case MAP_MATRIX:
					return "/a/0/4/2";
				case GAMETEXT:
					return "/a/0/7/4";
				case ZONE_DATA:
					return "/a/0/1/2";
				case BUILDING_MODELS:
					return "/a/0/2/4";
				case NPC_REGISTRIES:
					return "/a/1/4/9";
				case MOVE_MODELS:
					return "/a/0/2/1";
				case SOUND_BCSAR:
					return "/sound/xy_sound.bcsar";
			}
		} else {
			switch (archiveType) {
				case AREA_DATA:
					return "/a/0/1/4";
				case FIELD_DATA:
					return "/a/0/3/9";
				case MAP_MATRIX:
					return "/a/0/4/0";
				case GAMETEXT:
					return "/a/0/7/3";
				case ZONE_DATA:
					return "/a/0/1/3";
				case BUILDING_MODELS:
					return "/a/0/2/3";
				case NPC_REGISTRIES:
					return "/a/1/3/7";
				case MOVE_MODELS:
					return "/a/0/2/1";
				case SOUND_BCSAR:
					return "/sound/sango_sound.bcsar";
			}
		}
		return null;
	}

	public static void cleanAll() {
		multiClean(true);
		persist_paths.clear();
		saveWorkspace();
	}

	public static void cleanAndReload() {
		mTileMapPanel.unload();
		mCollEditPanel.unload();
		mCamEditForm.unload();
		mNPCEditForm.unload();
		mPropEditForm.unload();
		cleanAll();
	}

	public static void cleanUnchanged() {
		multiClean(false);
	}

	private static void multiClean(boolean deletePersistent) {
		cleanDirectory(WORKSPACE_PATH + "/areadata", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/fielddata", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/gametext", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/mapmatrix", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/zonedata", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/buildingmodels", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/npcregistries", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/movemodels", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/temp", true);
	}

	public static void cleanDirectory(String dir, boolean deletePersistent) {
		File[] files = new File(dir).listFiles();
		if (files == null || files.length == 0) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			if (!deletePersistent) {
				if (persist_paths.contains(files[i].getAbsolutePath())) {
					continue;
				}
			}
			files[i].delete();
		}
	}

	public static GARC getArchive(ArchiveType type) {
		switch (type) {
			case AREA_DATA:
				return ad;
			case FIELD_DATA:
				return gr;
			case MAP_MATRIX:
				return mm;
			case GAMETEXT:
				return texts;
			case ZONE_DATA:
				return zo;
			case BUILDING_MODELS:
				return bm;
			case NPC_REGISTRIES:
				return npcreg;
			case MOVE_MODELS:
				return npcmm;
		}
		return null;
	}

	public static File getExtractionDirectory(ArchiveType type) {
		StringBuilder sb = new StringBuilder(WORKSPACE_PATH + "/");
		switch (type) {
			case AREA_DATA:
				sb.append("areadata");
				break;
			case FIELD_DATA:
				sb.append("fielddata");
				break;
			case MAP_MATRIX:
				sb.append("mapmatrix");
				break;
			case GAMETEXT:
				sb.append("gametext");
				break;
			case ZONE_DATA:
				sb.append("zonedata");
				break;
			case BUILDING_MODELS:
				sb.append("buildingmodels");
				break;
			case NPC_REGISTRIES:
				sb.append("npcregistries");
				break;
			case MOVE_MODELS:
				sb.append("movemodels");
				break;
			default:
				return null;
		}
		sb.append("/");
		return new File(sb.toString());
	}

	public static void loadArchives() {
		if (valid) {
			ad = new GARC(areadata);
			gr = new GARC(fielddata);
			mm = new GARC(mapmatrix);
			texts = new GARC(gametext);
			zo = new GARC(zonedata);
			bm = new GARC(buildingmodels);
			npcreg = new GARC(npcregistries);
			npcmm = new GARC(movemodels);
		}
	}
	
	public static void reloadGARC(ArchiveType arc){
		switch (arc){
			case AREA_DATA:
				ad = new GARC(ad.file);
			case BUILDING_MODELS:
				bm = new GARC(bm.file);
			case FIELD_DATA:
				gr = new GARC(gr.file);
			case MAP_MATRIX:
				mm = new GARC(mm.file);
			case MOVE_MODELS:
				npcmm = new GARC(npcmm.file);
			case NPC_REGISTRIES:
				npcreg = new GARC(npcreg.file);
			case ZONE_DATA:
				zo = new GARC(zo.file);
		}
	}

	public static File getWorkspaceFile(ArchiveType arc, int fileNum) {
		File wsFile;
		wsFile = new File(getExtractionDirectory(arc).getAbsolutePath() + "/" + fileNum);
		if (!wsFile.exists() && getArchive(arc).length > fileNum) {
			try {
				OutputStream os = new FileOutputStream(wsFile);
				byte[] b = getArchive(arc).getDecompressedEntry(fileNum);
				if (b == null) {
					os.close();
					return null;
				}
				os.write(b);
				os.flush();
				os.close();
			} catch (IOException ex) {
				Logger.getLogger(Workspace.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return wsFile;
	}

	public static void packWorkspace() {
		if (valid) {
			LoadingDialog progress = LoadingDialog.makeDialog("Packing");
			SwingWorker worker = new SwingWorker() {
				@Override
				protected void done() {
					progress.close();
				}

				@Override
				protected Object doInBackground() {
					progress.setDescription("Packing - fielddata");
					gr.packDirectory(getExtractionDirectory(ArchiveType.FIELD_DATA));
					progress.setBarPercent(30);
					progress.setDescription("Packing - areadata");
					ad.packDirectory(getExtractionDirectory(ArchiveType.AREA_DATA));
					progress.setBarPercent(60);
					progress.setDescription("Packing - zonedata");
					zo.packDirectory(getExtractionDirectory(ArchiveType.ZONE_DATA));
					progress.setBarPercent(65);
					progress.setDescription("Packing - mapmatrix");
					mm.packDirectory(getExtractionDirectory(ArchiveType.MAP_MATRIX));
					progress.setBarPercent(70);
					progress.setDescription("Packing - buildingmodels");
					bm.packDirectory(getExtractionDirectory(ArchiveType.BUILDING_MODELS));
					progress.setBarPercent(90);
					progress.setDescription("Packing - npcregistries");
					npcreg.packDirectory(getExtractionDirectory(ArchiveType.NPC_REGISTRIES));
					progress.setBarPercent(100);
					progress.setDescription("Done, updating GARCs");
					//the GARC indices may have changed and as such we need to reload them
					reloadGARC(ArchiveType.AREA_DATA);
					reloadGARC(ArchiveType.FIELD_DATA);
					reloadGARC(ArchiveType.ZONE_DATA);
					reloadGARC(ArchiveType.MAP_MATRIX);
					reloadGARC(ArchiveType.BUILDING_MODELS);
					reloadGARC(ArchiveType.NPC_REGISTRIES);
					return null;
				}
			};
			worker.execute();
			progress.showDialog();
		}
	}
	
	public static String getMusicName(int id){
		return musicNames[id - 65536];
	}

	public static enum GameType {
		XY,
		ORAS
	}
	
	public static boolean isOA(){
		return game == GameType.ORAS;
	}
	
	public static boolean isOADemo(){
		return new File(GAMEDIR_PATH + "/a/3/0/0").exists();
	}
	
	public static boolean isXY(){
		return game == GameType.XY;
	}

	public static enum ArchiveType {
		AREA_DATA,
		FIELD_DATA,
		MAP_MATRIX,
		GAMETEXT,
		ZONE_DATA,
		BUILDING_MODELS,
		NPC_REGISTRIES,
		MOVE_MODELS,
		SOUND_BCSAR
	}

	public static void prefsPutNonNull(String key, String value){
		if (value != null && key != null){
			prefs.put(key, value);
		}
	}
	
	public static void saveWorkspace() {
		prefsPutNonNull("WORKSPACE_PATH", WORKSPACE_PATH);
		prefsPutNonNull("GAMEDIR_PATH", GAMEDIR_PATH);
		prefsPutNonNull("ESPICA_PATH", ESPICA_PATH);
		prefs.putBoolean("TILESET_DEFAULT", TILESET_DEFAULT);
		prefsPutNonNull("TILESET_PATH", TILESET_PATH);
		try {
			persist_config.delete();
			persist_config.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(persist_config));
			for (String line : persist_paths) {
				writer.write(line.replace(WORKSPACE_PATH, "") + "\n");  //write the paths relative to wsdir to allow moving workspace to other machines
			}
			writer.close();
		} catch (IOException ex) {
			Logger.getLogger(Workspace.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
