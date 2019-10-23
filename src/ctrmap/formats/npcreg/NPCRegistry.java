package ctrmap.formats.npcreg;

import ctrmap.CtrmapMainframe;
import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.Utils;
import ctrmap.Workspace;
import ctrmap.formats.containers.MM;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Provides access to and manipulation of MoveModel/NPC registry, specified in a
 * Zone header by the shared AD ushort.
 */
public class NPCRegistry {

	public Map<Integer, NPCRegistryEntry> entries = new HashMap<>();
	public Map<Integer, H3DModel> models = new HashMap<>();
	public boolean modified = false;
	private File f;

	public NPCRegistry(File f) {
		this.f = f;
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new FileInputStream(f));
			//this struct does not describe length in any way. idk why. it works when I add new entries, so it's not described anywhere, even externally.
			while (dis.available() >= 0x18) {
				NPCRegistryEntry e = new NPCRegistryEntry(dis);
				entries.put(e.uid, e);
				BCHFile bch = new BCHFile(new MM(Workspace.getWorkspaceFile(Workspace.ArchiveType.MOVE_MODELS, e.model)).getFile(0));
				if (!bch.models.isEmpty()) {
					bch.models.get(0).setMaterialTextures(bch.textures);
					bch.models.get(0).makeAllBOs();
					models.put(e.uid, bch.models.get(0));
				}
			}
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(NPCRegistry.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void mapModel(int uid, int mdlnum) {
		BCHFile bch = new BCHFile(new MM(Workspace.getWorkspaceFile(Workspace.ArchiveType.MOVE_MODELS, mdlnum)).getFile(0));
		if (!bch.models.isEmpty()) {
			bch.models.get(0).setMaterialTextures(bch.textures);
			models.put(uid, bch.models.get(0));
		}
	}

	public H3DModel getModel(int uid) {
		return models.get(uid);
	}

	public boolean store(boolean dialog) {
		if (!modified) {
			return true;
		}
		if (dialog) {
			int result = Utils.showSaveConfirmationDialog("NPC registry");
			switch (result) {
				case JOptionPane.YES_OPTION:
					break;
				case JOptionPane.NO_OPTION:
					modified = false;
					return true;
				case JOptionPane.CANCEL_OPTION:
					return false;
			}
		}
		Workspace.addPersist(f);
		try {
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new FileOutputStream(f));
			for (Map.Entry<Integer, NPCRegistryEntry> e : entries.entrySet()) {
				e.getValue().write(dos);
			}
			dos.close();
		} catch (IOException ex) {
			Logger.getLogger(NPCRegistry.class.getName()).log(Level.SEVERE, null, ex);
		}
		modified = false;
		return true;
	}

	public static class NPCRegistryEntry {

		public int uid;
		public int u1; //always 1 except for 0 UID dummies, purpose unknown
		public int renderEnabled;
		public int u4; //if set, 2 or 4, has something to do with NPCs being in cutscenes
		public int u5; //always 0
		public int shadowEnabled;
		public int isItOb; //usually 1 but 0 on it**** and ob****, those are referenced by non-model UIDs, but changing it to 1 doesnt break anything so idk.
		public int u8; //always 1 except for 0 UID dummies but does nothing afaik
		public int u9; //only used in XY NPCs AND XY NPCs present in OA like the Battle Maison receptionist
		public int collW;
		public int collH;
		public int uC; //only used for cut animation, could be something like hide on interact? Cut trees get replaced with animated ones from a181 (XY) so for that maybe, but why isn't it on rock smash then?
		public int uD; //always 0
		public int uE; //always 0
		public int isDummy; //1 on XY dummies (not 0 UID entries but actual dummy models with the dummy BCH name) and thats about it
		public int u10; //always 0
		public int u11; //1 or 2, always used but 0 does not hurt anything.
		public int u12; //always 0
		public int u13; //always 0
		public int model;
		public int u16; //always 0
		public int u17; //always 0

		public NPCRegistryEntry(LittleEndianDataInputStream dis) throws IOException {
			uid = dis.readUnsignedShort();
			u1 = dis.read();
			renderEnabled = dis.read();
			u4 = dis.read();
			u5 = dis.read();
			shadowEnabled = dis.read();
			isItOb = dis.read();
			u8 = dis.read();
			u9 = dis.read();
			collW = dis.read();
			collH = dis.read();
			uC = dis.read();
			uD = dis.read();
			uE = dis.read();
			isDummy = dis.read();
			u10 = dis.read();
			u11 = dis.read();
			u12 = dis.read();
			u13 = dis.read();
			model = dis.readUnsignedShort();
			u16 = dis.read();
			u17 = dis.read();
		}

		public NPCRegistryEntry() {
			uid = 0;
			u1 = 0;
			renderEnabled = 2;
			u4 = 0;
			u5 = 0;
			shadowEnabled = 1;
			isItOb = 1;
			u8 = 1;
			u9 = 0;
			collW = 1;
			collH = 1;
			uC = 0;
			uD = 0;
			uE = 0;
			isDummy = 0;
			u10 = 0;
			u11 = 0;
			u12 = 2;
			u13 = 0;
			model = 0;
			u16 = 0;
			u17 = 0;
		}

		public void write(LittleEndianDataOutputStream dos) throws IOException {
			dos.writeShort((short) uid);
			dos.write(u1);
			dos.write(renderEnabled);
			dos.write(u4);
			dos.write(u5);
			dos.write(shadowEnabled);
			dos.write(isItOb);
			dos.write(u8);
			dos.write(u9);
			dos.write(collW);
			dos.write(collH);
			dos.write(uC);
			dos.write(uD);
			dos.write(uE);
			dos.write(isDummy);
			dos.write(u10);
			dos.write(u11);
			dos.write(u12);
			dos.write(u13);
			dos.writeShort((short) model);
			dos.write(u16);
			dos.write(u17);
		}

		@Override
		public boolean equals(Object o2) {
			if (o2 != null && o2 instanceof NPCRegistryEntry) {
				NPCRegistryEntry e = (NPCRegistryEntry) o2;
				return e.uid == uid && e.u1 == u1 && e.renderEnabled == renderEnabled && e.u4 == u4
						&& e.shadowEnabled == shadowEnabled && e.isItOb == isItOb && e.u8 == u8
						&& e.u9 == u9 && e.collW == collW && e.collH == collH && e.uC == uC
						&& e.isDummy == isDummy && e.u11 == u11 && e.model == model;
			}
			return false;
		}
	}
}
