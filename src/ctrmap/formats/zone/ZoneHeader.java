package ctrmap.formats.zone;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.Utils;
import ctrmap.Workspace;
import ctrmap.formats.containers.AD;
import ctrmap.formats.containers.MM;
import ctrmap.formats.garc.LZ11;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.texturing.H3DTexture;
import ctrmap.formats.npcreg.NPCRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * From pk3DS source
 */
public class ZoneHeader {

	public int mapType;
	public int mapMove;
	public int areadataID;
	public AD areadata;
	public List<H3DTexture> propTextures = new ArrayList<>();
	public List<H3DTexture> worldTextures = new ArrayList<>();
	public NPCRegistry npcreg;
	public int mapmatrixID;
	public MM mapmatrix;
	public int textID; //normal text is also stored in script, game text is only UI stuff
	public int script; //handles script texts so it's read like a gametext file except that it's not from GAMETEXT. Will be implemented alongside scripting, if ever.
	public int townMapGroup;

	public int BGMSpring; //needs additional BCSAR research to get the numbers, the best way is probably to hack BrawlBox?
	public int BGMSummer;
	public int BGMAutumn;
	public int BGMWinter;

	public int parentMap; //useful for getting location names
	public int OLvalue;

	public int weather;
	public int battleBG;
	public int mapChange;

	public boolean enableSkybox;
	public boolean enableRollerSkates;
	public boolean enableCycling;
	public boolean enableRunning;
	public boolean enableEscapeRope;
	public boolean enableFlyFrom;
	public boolean enableCyclingBGM;
	public boolean unknownFlag;

	public int camera1;
	public int camera2;
	public int cameraFlags;

	public int X;
	public int Z;
	public int Y;
	public int X2;
	public int Z2;
	public int Y2;

	public int PX;
	public int PX2;
	public int PY;
	public int PY2;

	public int unknownFlags;

	public Workspace.GameType game;

	public boolean enableDowsingMachine;
	public boolean enableBreathFX;
	public boolean enableSpecialWalking = false;
	public boolean enableFlashableDarkness = false;
	public boolean enableGhosting;
	public boolean enable3D;
	public int OAZoneNumber;

	public ZoneHeader(byte[] data, Workspace.GameType game) {
		try {
			this.game = game;
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
			mapType = dis.read();
			mapMove = dis.read();
			areadataID = dis.readShort();
			mapmatrixID = dis.readShort();
			textID = dis.readShort();

			BGMSpring = dis.readInt();
			BGMSummer = dis.readInt();
			BGMAutumn = dis.readInt();
			BGMWinter = dis.readInt();

			script = dis.readShort();
			townMapGroup = dis.readShort();

			int H0x1C = dis.readShort();
			parentMap = H0x1C & 0x3FF;
			OLvalue = H0x1C >> 10;

			int H0x1E = dis.readShort();
			weather = H0x1E & 0x1F;
			enableSkybox = ((H0x1E >> 5) & 1) == 1;
			enableRollerSkates = ((H0x1E >> 6) & 1) == 1;
			battleBG = (H0x1E >> 7) & 0x7F;

			int H0x20 = dis.readShort();
			mapChange = H0x20 & 0x1F;
			enableCycling = ((H0x20 >> 10) & 1) == 1;
			enableRunning = ((H0x20 >> 11) & 1) == 1;
			enableEscapeRope = ((H0x20 >> 12) & 1) == 1;
			enableFlyFrom = ((H0x20 >> 13) & 1) == 1;
			enableCyclingBGM = ((H0x20 >> 14) & 1) == 1;
			unknownFlag = ((H0x20 >> 15) & 1) == 1;

			camera1 = dis.readShort();
			camera2 = dis.readShort();
			cameraFlags = dis.readShort();

			unknownFlags = dis.readInt();
			// >> 13 in OA
			int relocator = (game == Workspace.GameType.XY) ? 0 : 1;
			enableDowsingMachine = (unknownFlags >> (12 + relocator) & 1) == 1;
			// >> 12 in OA
			enableBreathFX = (unknownFlags >> (11 + relocator) & 1) == 1;
			enable3D = (unknownFlags >> (15 + relocator) & 1) == 1;
			// means flashable darkness in OA
			if (game == Workspace.GameType.XY) {
				enableSpecialWalking = (unknownFlags >> 2 & 1) == 0;
			} else {
				enableFlashableDarkness = (unknownFlags >> 1 & 1) == 1;
			}
			enableGhosting = (unknownFlags & 1) == 1;
			//OA exclusive zone ID
			OAZoneNumber = (unknownFlags >> 21);

			X = dis.readShort();
			Z = dis.readShort();
			Y = dis.readShort();
			X2 = dis.readShort();
			Z2 = dis.readShort();
			Y2 = dis.readShort();
		} catch (IOException ex) {
			Logger.getLogger(ZoneHeader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public byte[] assembleData() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(baos);
			dos.write(mapType);
			dos.write(mapMove);
			dos.writeShort((short) areadataID);
			dos.writeShort((short) mapmatrixID);
			dos.writeShort((short) textID);
			dos.writeInt(BGMSpring);
			dos.writeInt(BGMSummer);
			dos.writeInt(BGMAutumn);
			dos.writeInt(BGMWinter);
			dos.writeShort((short) script);
			dos.writeShort((short) townMapGroup);

			short H0x1C = (short) parentMap;
			H0x1C = (short) (H0x1C | ((OLvalue) << 10));
			dos.writeShort(H0x1C);

			short H0x1E = (short) weather;
			H0x1E = (short) (H0x1E | ((enableSkybox) ? 1 : 0) << 5);
			H0x1E = (short) (H0x1E | ((enableRollerSkates) ? 1 : 0) << 6);
			H0x1E = (short) (H0x1E | (battleBG << 7));
			dos.writeShort(H0x1E);

			short H0x20 = (short) mapChange;
			H0x20 = (short) (H0x20 | ((enableCycling) ? 1 : 0) << 10);
			H0x20 = (short) (H0x20 | ((enableRunning) ? 1 : 0) << 11);
			H0x20 = (short) (H0x20 | ((enableEscapeRope) ? 1 : 0) << 12);
			H0x20 = (short) (H0x20 | ((enableFlyFrom) ? 1 : 0) << 13);
			H0x20 = (short) (H0x20 | ((enableCyclingBGM) ? 1 : 0) << 14);
			H0x20 = (short) (H0x20 | ((unknownFlag) ? 1 : 0) << 15);
			dos.writeShort(H0x20);

			dos.writeShort((short) camera1);
			dos.writeShort((short) camera2);
			dos.writeShort((short) cameraFlags);

			calculateFlags();

			dos.writeInt(unknownFlags);

			dos.writeShort((short) X);
			dos.writeShort((short) Z);
			dos.writeShort((short) Y);

			dos.writeShort((short) X2);
			dos.writeShort((short) Z2);
			dos.writeShort((short) Y2);

			return baos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(ZoneHeader.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public void calculateFlags() {
		if (game == Workspace.GameType.XY) {
			unknownFlags = unknownFlags & 0xFFFFFFFE | ((enableGhosting) ? 1 : 0);
			unknownFlags = unknownFlags & 0xFFFFF7FF | ((enableBreathFX) ? 1 : 0) << 11;
			unknownFlags = unknownFlags & 0xFFFFEFFF | ((enableDowsingMachine) ? 1 : 0) << 12;
			unknownFlags = unknownFlags & 0xFFFF7FFF | ((enable3D) ? 1 : 0) << 15;
			unknownFlags = unknownFlags & 0xFFFFFFFB | ((enableSpecialWalking) ? 0 : 1) << 2;
		} else {
			unknownFlags = unknownFlags & 0xFFFFFFFE | ((enableGhosting) ? 1 : 0);
			unknownFlags = unknownFlags & 0xFFFFEFFF | ((enableBreathFX) ? 1 : 0) << 12;
			unknownFlags = unknownFlags & 0xFFFFDFFF | ((enableDowsingMachine) ? 1 : 0) << 13;
			unknownFlags = unknownFlags & 0xFFFEFFFF | ((enable3D) ? 1 : 0) << 16;
			unknownFlags = unknownFlags & 0xFFFFFFFD| ((enableFlashableDarkness) ? 1 : 0) << 1;
		}
		//unknownFlags = unknownFlags & 0x7FFFFF | (OAZoneNumber << 21);
	}

	public void fetchArchives() {
		areadata = new AD(Workspace.getWorkspaceFile(Workspace.ArchiveType.AREA_DATA, areadataID));
		byte[] adbch1 = areadata.getFile(1);
		if (Utils.checkBCHMagic(adbch1)) {
			BCHFile adbch1bch = new BCHFile(adbch1);
			if (adbch1bch.errorlevel == 0) {
				propTextures.addAll(adbch1bch.textures);
			}
		}
		byte[] wTex = areadata.getFile(11);
		if (wTex[0] == 0x11) {
			//LZ11 COMPRESSED THOT ALERT
			byte[] dec = LZ11.decompress(wTex);
			worldTextures.addAll(new BCHFile(dec).textures);
		} else {
			worldTextures.addAll(new BCHFile(wTex).textures);
		}
		npcreg = new NPCRegistry(Workspace.getWorkspaceFile(Workspace.ArchiveType.NPC_REGISTRIES, areadataID));
		mapmatrix = new MM(Workspace.getWorkspaceFile(Workspace.ArchiveType.MAP_MATRIX, mapmatrixID));
	}

	public void freeArchives() {
		areadata = null;
		propTextures.clear();
		worldTextures.clear();
		npcreg = null;
		mapmatrix = null;
	}
}
