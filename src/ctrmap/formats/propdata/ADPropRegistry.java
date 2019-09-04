package ctrmap.formats.propdata;

import ctrmap.CtrmapMainframe;
import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.Utils;
import ctrmap.Workspace;
import ctrmap.formats.containers.AD;
import ctrmap.formats.containers.BM;
import ctrmap.formats.h3d.BCHFile;
import ctrmap.formats.h3d.model.H3DModel;
import ctrmap.formats.h3d.texturing.H3DTexture;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic implementation of the first section of AreaData, the prop registry.
 */
public class ADPropRegistry {

	private AD f;
	public Map<Integer, ADPropRegistryEntry> entries = new HashMap<>();
	public Map<Integer, H3DModel> models = new HashMap<>();
	public boolean modified = false;

	public ADPropRegistry(AD ad, List<H3DTexture> textures) {
		try {
			f = ad;
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(ad.getFile(0)));
			int numEntries = dis.readInt();
			for (int i = 0; i < numEntries; i++) {
				ADPropRegistryEntry entry = new ADPropRegistryEntry(dis);
				entries.put(entry.reference, entry);
				BCHFile bch = new BCHFile(new BM(CtrmapMainframe.mWorkspace.getWorkspaceFile(Workspace.ArchiveType.BUILDING_MODELS, entry.model)).getFile(0));
				if (!bch.models.isEmpty()){
					bch.models.get(0).setMaterialTextures(bch.textures);
					//bch.models.get(0).adjustBoneVerticesToMatrix();
					if (textures != null){
						bch.models.get(0).setMaterialTextures(textures);
					}
					models.put(entry.reference, bch.models.get(0));
				}
			}
			dis.close();
		} catch (IOException ex) {
			System.err.println("IOException thrown when reading prop registry");
			ex.printStackTrace();
		}
	}
	
	public H3DModel getModel(int uid){
		return models.get(uid);
	}
	
	public void write(){
		if (modified == false) return;
		modified = false;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(baos);
			dos.writeInt(entries.size());
			for (Map.Entry<Integer, ADPropRegistryEntry> e : entries.entrySet()){
				e.getValue().write(dos);
			}
			dos.write(Utils.getPadding(f.getOffset(0), dos.size()));
			dos.close();
			f.storeFile(0, baos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ADPropRegistry.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static class ADPropRegistryEntry {

		public int reference;
		public int model;
		public int eventScr1; //used for automated prop animation (doors opening on contact etc, probably called from warp scripts, some may (?) return callbacks to the scripting engine when anim is finished)
		public int eventScr2; //used in XY for day/night reacting props such as lamps or some fountains
		public byte u0;
		public byte isOmnipresentAnimated;
		public int u1;
		public short[] omnipresentAnimations = new short[3]; //the engine has 3 animation slots everywhere for the 3 CTR anim types - Skel, mat and vis
		public short[] evtAnimations1 = new short[4]; //for some reason there are extra 2 bytes before the next section. Might just be padding but as I haven't seen them set to anything not FFFF, idk what they are for.
		public short[][] evtAnimations2 = new short[3][3];
		public byte[] u2 = new byte[0x24]; //mostly constant except for the XY aquacorde fountain (maybe more, haven't looked). Changing it had no visible effect.
		//said prop had some CGFX particle data in its files but I couldn't notice those in game. So it might just be related to that and could have been scrapped.

		public ADPropRegistryEntry(LittleEndianDataInputStream dis) throws IOException {
			reference = dis.readUnsignedShort(); //both ref and mdl are the same in GF-made regions afaik but they can be changed
			model = dis.readUnsignedShort(); //ref is the one the GR propdata finds in the registry, mdl is then found in the BM GARC
			eventScr1 = dis.read();
			eventScr2 = dis.read();
			u0 = dis.readByte();
			isOmnipresentAnimated = dis.readByte();
			u1 = dis.readInt();
			for (int i = 0; i < 3; i++) {
				omnipresentAnimations[i] = dis.readShort();
			}
			for (int i = 0; i < 4; i++) {
				evtAnimations1[i] = dis.readShort();
			}
			for (int i = 0; i < 9; i++) {
				evtAnimations2[i / 3][i % 3] = dis.readShort();
			}
			dis.read(u2);
			dis.close();
		}
		
		public ADPropRegistryEntry(){
			reference = 0;
			model = 0;
			eventScr1 = 0;
			eventScr2 = 0;
			isOmnipresentAnimated = 0;
			u1 = 0;
			for (int i = 0; i < 3; i++) {
				omnipresentAnimations[i] = -1;
			}
			for (int i = 0; i < 3; i++) {
				evtAnimations1[i] = -1;
			}
			for (int i = 0; i < 9; i++) {
				evtAnimations2[i / 3][i % 3] = -1;
			}
			u2 = new byte[]{-1, -1, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
		}
		
		public void write(LittleEndianDataOutputStream dos) throws IOException{
			dos.writeShort((short)reference);
			dos.writeShort((short)model);
			dos.write(eventScr1);
			dos.write(eventScr2);
			dos.writeByte(u0);
			dos.writeByte(isOmnipresentAnimated);
			dos.writeInt(u1);
			for (int i = 0; i < 3; i++) {
				dos.writeShort(omnipresentAnimations[i]);
			}
			for (int i = 0; i < 4; i++) {
				dos.writeShort(evtAnimations1[i]);
			}
			for (int i = 0; i < 9; i++) {
				dos.writeShort(evtAnimations2[i / 3][i % 3]);
			}
			dos.write(u2);
			dos.close();
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof ADPropRegistryEntry && obj != null){
				ADPropRegistryEntry e = (ADPropRegistryEntry) obj;
				return e.reference == reference && e.model == model && e.eventScr1 == eventScr1 && e.eventScr2 == eventScr2 &&
						e.u0 == u0 && e.isOmnipresentAnimated == isOmnipresentAnimated && e.u1 == u1 &&
						Arrays.equals(e.omnipresentAnimations, omnipresentAnimations) && Arrays.equals(e.evtAnimations1, evtAnimations1) &&
						Arrays.deepEquals(e.evtAnimations2, evtAnimations2);
			}
			return true;
		}
	}
}
