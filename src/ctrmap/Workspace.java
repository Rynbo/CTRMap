package ctrmap;

import static ctrmap.CtrmapMainframe.mCamEditForm;
import static ctrmap.CtrmapMainframe.mCollEditPanel;
import static ctrmap.CtrmapMainframe.mTileMapPanel;
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
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class Workspace {

	Preferences prefs;
	public String WORKSPACE_PATH;
	public String GAMEDIR_PATH;
	public boolean TILESET_DEFAULT;
	public String TILESET_PATH;

	public File areadata;
	public File fielddata;
	public File mapmatrix;
	public File gametext;
	public File zonedata;
	public File buildingmodels;
	public File npcregistries;

	public File persist_config;
	public ArrayList<String> persist_paths = new ArrayList<>();

	public GARC ad;
	public GARC gr;
	public GARC mm;
	public GARC texts;
	public GARC zo;
	public GARC bm;
	public GARC npcreg;

	public GameType game;
	public boolean valid = false;

	public Workspace() {
		prefs = Preferences.userRoot().node(getClass().getName());
		WORKSPACE_PATH = prefs.get("WORKSPACE_PATH", "");
		GAMEDIR_PATH = prefs.get("GAMEDIR_PATH", "");
		TILESET_DEFAULT = prefs.getBoolean("TILESET_DEFAULT", true);
		TILESET_PATH = prefs.get("TILESET_PATH", "");
	}

	public EditorTileset getTileset() {
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

	public void validate(Component parent) {
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
					"npcregistries"
				});
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
				}
			}
		}
		if (errors.isEmpty()) {
			valid = true;
			loadArchives();
			LocationNames.loadFromGarc(game);
			CtrmapMainframe.zoneDebugPnl.loadEverything();
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

	public void addPersist(File f) {
		if (!persist_paths.contains(f.getAbsolutePath())) {
			persist_paths.add(f.getAbsolutePath());
		}
	}

	public String getArchivePath(ArchiveType archiveType, GameType gameType) {
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
			}
		}
		return null;
	}

	public void cleanAll() {
		multiClean(true);
		persist_paths.clear();
		saveWorkspace();
	}

	public void cleanAndReload() {
		mTileMapPanel.unload();
		mCollEditPanel.unload();
		mCamEditForm.unload();
		cleanAll();
	}

	public void cleanUnchanged() {
		multiClean(false);
	}

	private void multiClean(boolean deletePersistent) {
		cleanDirectory(WORKSPACE_PATH + "/areadata", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/fielddata", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/gametext", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/mapmatrix", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/zonedata", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/buildingmodels", deletePersistent);
		cleanDirectory(WORKSPACE_PATH + "/npcregistries", deletePersistent);
	}

	public void cleanDirectory(String dir, boolean deletePersistent) {
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

	public GARC getArchive(ArchiveType type) {
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
		}
		return null;
	}

	public File getExtractionDirectory(ArchiveType type) {
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
			default:
				return null;
		}
		sb.append("/");
		return new File(sb.toString());
	}

	public void loadArchives() {
		if (valid) {
			ad = new GARC(areadata);
			gr = new GARC(fielddata);
			mm = new GARC(mapmatrix);
			texts = new GARC(gametext);
			zo = new GARC(zonedata);
			bm = new GARC(buildingmodels);
			npcreg = new GARC(npcregistries);
		}
	}

	public File getWorkspaceFile(ArchiveType arc, int fileNum) {
		File wsFile = null;
		wsFile = new File(getExtractionDirectory(arc).getAbsolutePath() + "/" + fileNum);
		if (wsFile != null && !wsFile.exists()) {
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

	public void packWorkspace() {
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
					progress.setBarPercent(70);
					progress.setDescription("Packing - buildingmodels");
					bm.packDirectory(getExtractionDirectory(ArchiveType.BUILDING_MODELS));
					progress.setBarPercent(90);
					progress.setDescription("Packing - npcregistries");
					npcreg.packDirectory(getExtractionDirectory(ArchiveType.NPC_REGISTRIES));
					progress.setBarPercent(100);
					progress.setDescription("Done, updating GARCs");
					//the GARC indices may have changed and as such we need to reload them
					gr = new GARC(gr.file);
					ad = new GARC(ad.file);
					zo = new GARC(zo.file);
					bm = new GARC(bm.file);
					npcreg = new GARC(npcreg.file);
					return null;
				}
			};
			worker.execute();
			progress.show();
		}
	}

	public enum GameType {
		XY,
		ORAS
	}

	public enum ArchiveType {
		AREA_DATA,
		FIELD_DATA,
		MAP_MATRIX,
		GAMETEXT,
		ZONE_DATA,
		BUILDING_MODELS,
		NPC_REGISTRIES
	}

	public void saveWorkspace() {
		prefs.put("WORKSPACE_PATH", WORKSPACE_PATH);
		prefs.put("GAMEDIR_PATH", GAMEDIR_PATH);
		prefs.putBoolean("TILESET_DEFAULT", TILESET_DEFAULT);
		prefs.put("TILESET_PATH", TILESET_PATH);
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
