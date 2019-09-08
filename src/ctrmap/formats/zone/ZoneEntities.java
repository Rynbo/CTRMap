package ctrmap.formats.zone;

import ctrmap.CtrmapMainframe;
import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.humaninterface.MapObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ZoneEntities implementation for Furniture and NPCs, Based on OWSE source (from PK3DS)
 */
public class ZoneEntities {

	public int totalLength;
	public int furnitureCount;
	public int NPCCount;
	public int warpCount;
	public int trigger1Count;
	public int trigger2Count;

	public ArrayList<Prop> furniture = new ArrayList<>();
	public ArrayList<NPC> npcs = new ArrayList<>();

	public byte[] rest_unprogrammed;

	public boolean modified = false;

	public ZoneEntities(byte[] data) {
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
			totalLength = dis.readInt();
			furnitureCount = dis.read();
			NPCCount = dis.read();
			warpCount = dis.read();
			trigger1Count = dis.read();
			trigger2Count = dis.read();
			dis.skip(3);
			for (int i = 0; i < furnitureCount; i++) {
				furniture.add(new Prop(dis));
			}
			for (int i = 0; i < NPCCount; i++) {
				npcs.add(new NPC(dis));
			}
			rest_unprogrammed = new byte[dis.available()];
			dis.read(rest_unprogrammed);
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(ZoneEntities.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public byte[] assembleData() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(baos);
			totalLength = 8 /*header without length*/ + furnitureCount * 0x14 + NPCCount * 0x30 + warpCount * 0x18 + trigger1Count * 0x18 + trigger2Count * 0x18;
			dos.writeInt(totalLength);
			dos.write(furnitureCount);
			dos.write(NPCCount);
			dos.write(warpCount);
			dos.write(trigger1Count);
			dos.write(trigger2Count);
			dos.writeShort((short) 0);
			dos.write(0);
			for (int i = 0; i < furniture.size(); i++) {
				furniture.get(i).write(dos);
			}
			for (int i = 0; i < npcs.size(); i++) {
				npcs.get(i).write(dos);
			}
			dos.write(rest_unprogrammed);
			dos.close();
			return baos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(ZoneEntities.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public static class Prop {

		public int script;
		public int unknown2;
		public int unknown4;
		public int unknown6;
		public int x;
		public int y;
		public int w;
		public int h;
		public int unknown10;

		public Prop(LittleEndianDataInputStream dis) {
			try {
				script = dis.readUnsignedShort();
				unknown2 = dis.readUnsignedShort();
				unknown4 = dis.readUnsignedShort();
				unknown6 = dis.readUnsignedShort();
				x = dis.readUnsignedShort();
				y = dis.readUnsignedShort();
				w = dis.readUnsignedShort();
				h = dis.readUnsignedShort();
				unknown10 = dis.readInt();
			} catch (IOException ex) {
				Logger.getLogger(ZoneEntities.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		public void write(LittleEndianDataOutputStream dos) {
			try {
				dos.writeShort((short) script);
				dos.writeShort((short) unknown2);
				dos.writeShort((short) unknown4);
				dos.writeShort((short) unknown6);
				dos.writeShort((short) x);
				dos.writeShort((short) y);
				dos.writeShort((short) w);
				dos.writeShort((short) h);
				dos.writeInt(unknown10);
			} catch (IOException ex) {
				Logger.getLogger(ZoneEntities.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static class NPC implements MapObject {

		public int uid = 0; //all unsigned shorts
		public int model = 0;
		public int movePerm1 = 0;
		public int movePerm2 = 0; //4 on items and 10 on double battle paired NPCs
		public int spawnFlag = 0;
		public int script = 0;
		public int faceDirection = 0;
		public int sightRange = 0;
		public int u10 = 0;
		public int u12 = 0;
		public int areaStartX = 0;
		public int areaStartY = 0;
		public int areaWidth = 0;
		public int areaHeight = 0;

		public int multiZoneLinkOriginZone = -1;
		public int multiZoneLinkTargetZone = -1;
		public int multiZoneLinkHostZone = -1;

		public int multiZoneLink1Type = 0;

		public int leashWidth = 0;
		public int leashHeight = 0;

		public int xTile = 0;
		public int xFlags;
		public int yTile = 0;
		public int yFlags;

		public float z3DCoordinate;

		//NONSTANDARD fields
		public int floatMotionOrigin = -1; //useless creating it for every axis, imprecisions are minimal
		public float floatMotionRemainder = 0;

		public NPC(LittleEndianDataInputStream dis) throws IOException {
			uid = dis.readUnsignedShort();
			model = dis.readUnsignedShort();
			movePerm1 = dis.readUnsignedShort();
			movePerm2 = dis.readUnsignedShort();
			spawnFlag = dis.readUnsignedShort();
			script = dis.readUnsignedShort();
			faceDirection = dis.readUnsignedShort();
			sightRange = dis.readUnsignedShort();

			u10 = dis.readUnsignedShort();
			u12 = dis.readUnsignedShort();
			areaStartX = dis.readShort();
			areaStartY = dis.readShort();
			areaWidth = dis.readShort();
			areaHeight = dis.readShort();

			multiZoneLinkOriginZone = dis.readShort();
			multiZoneLinkTargetZone = dis.readShort();
			multiZoneLinkHostZone = dis.readShort();

			multiZoneLink1Type = dis.readUnsignedShort();
			leashWidth = dis.readUnsignedShort();
			leashHeight = dis.readUnsignedShort();

			xTile = dis.readUnsignedShort();
			/*xFlags = xTile >> 11;
			xTile = xTile & 0x1FF;*/
			yTile = dis.readUnsignedShort();
			/*yFlags = yTile >> 11;
			yTile = yTile & 0x1FF;*/
			z3DCoordinate = dis.readFloat();
		}

		public NPC() {
		}

		public void write(LittleEndianDataOutputStream dos) throws IOException {
			dos.writeShort((short) uid);
			dos.writeShort((short) model);
			dos.writeShort((short) movePerm1);
			dos.writeShort((short) movePerm2);
			dos.writeShort((short) spawnFlag);
			dos.writeShort((short) script);
			dos.writeShort((short) faceDirection);
			dos.writeShort((short) sightRange);

			dos.writeShort((short) u10);
			dos.writeShort((short) u12);
			dos.writeShort((short) areaStartX);
			dos.writeShort((short) areaStartY);
			dos.writeShort((short) areaWidth);
			dos.writeShort((short) areaHeight);

			dos.writeShort((short) multiZoneLinkOriginZone);
			dos.writeShort((short) multiZoneLinkTargetZone);
			dos.writeShort((short) multiZoneLinkHostZone);

			dos.writeShort((short) multiZoneLink1Type);
			dos.writeShort((short) leashWidth);
			dos.writeShort((short) leashHeight);

			dos.writeShort((short) xTile);
			dos.writeShort((short) yTile);
			dos.writeFloat(z3DCoordinate);
		}

		@Override
		public boolean equals(Object o2) {
			if (o2 != null && o2 instanceof NPC) {
				NPC npc = (NPC) o2;
				return npc.uid == uid && npc.model == model && npc.movePerm1 == movePerm1
						&& npc.movePerm2 == movePerm2 && npc.spawnFlag == spawnFlag && npc.script == script
						&& npc.faceDirection == faceDirection && npc.sightRange == sightRange
						&& npc.u10 == u10 && npc.u12 == u12 && npc.areaStartX == areaStartX
						&& npc.areaStartY == areaStartY && npc.areaWidth == areaWidth
						&& npc.areaHeight == areaHeight && npc.multiZoneLinkOriginZone == multiZoneLinkOriginZone
						&& npc.multiZoneLinkTargetZone == multiZoneLinkTargetZone
						&& npc.multiZoneLinkHostZone == multiZoneLinkHostZone
						&& npc.multiZoneLink1Type == multiZoneLink1Type && npc.leashWidth == leashWidth
						&& npc.leashHeight == leashHeight && npc.xTile == xTile
						&& npc.yTile == yTile && npc.z3DCoordinate == z3DCoordinate;
			}
			return false;
		}

		@Override
		public float getX() {
			return xTile * 18f;
		}

		@Override
		public float getY() {
			return z3DCoordinate;
		}

		@Override
		public float getZ() {
			return yTile * 18f;
		}

		@Override
		public void setX(float val) {
			xTile = Math.round(val / 18f);
			if (val < xTile * 18f) {
				if (xTile != floatMotionOrigin) {
					floatMotionOrigin = xTile;
					floatMotionRemainder = 18f - (val % ((xTile - 1) * 18f));
				} else {
					floatMotionRemainder += 18f - (val % ((xTile - 1) * 18f));
					if (floatMotionRemainder >= 18f) {
						xTile--;
						floatMotionRemainder %= 18f;
						floatMotionOrigin = xTile;
					}
				}
			} else {
				if (xTile != floatMotionOrigin) {
					floatMotionOrigin = xTile;
					floatMotionRemainder = val % (xTile * 18f);
				} else {
					floatMotionRemainder += val % (xTile * 18f);
					if (floatMotionRemainder >= 18f) {
						xTile++;
						floatMotionRemainder %= 18f;
						floatMotionOrigin = xTile;
					}
				}
			}
			setYFromColl(xTile * 18f, yTile * 18f);
		}

		@Override
		public void setY(float val) {
			z3DCoordinate = val;
		}

		public void setYFromColl(float x, float y) {
			z3DCoordinate = CtrmapMainframe.mTileMapPanel.getHeightAtWorldLoc(x, y);
		}

		@Override
		public void setZ(float val) {
			yTile = Math.round(val / 18f);
			if (val < yTile * 18f) {
				if (yTile != floatMotionOrigin) {
					floatMotionOrigin = yTile;
					floatMotionRemainder = 18f - (val % ((yTile - 1) * 18f));
				} else {
					floatMotionRemainder += 18f - (val % ((yTile - 1) * 18f));
					if (floatMotionRemainder >= 18f) {
						yTile--;
						floatMotionRemainder %= 18f;
						floatMotionOrigin = yTile;
					}
				}
			} else {
				if (yTile != floatMotionOrigin) {
					floatMotionOrigin = yTile;
					floatMotionRemainder = val % (yTile * 18f);
				} else {
					floatMotionRemainder += val % (yTile * 18f);
					if (floatMotionRemainder >= 18f) {
						yTile++;
						floatMotionRemainder %= 18f;
						floatMotionOrigin = yTile;
					}
				}
			}
			setYFromColl(xTile * 18f + 9f, yTile * 18f + 9f);
		}
	}
}
