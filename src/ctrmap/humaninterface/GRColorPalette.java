package ctrmap.humaninterface;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import ctrmap.ImageMapCreator;

public class GRColorPalette {
	
	public static final int TYPE_PURECOLOR = 0;
	public static final int TYPE_IMAGES = 1;
	
	public static final int TILE_NORMAL_WALKABLE = 0x20000000;
	public static final int TILE_NORMAL_UNWALKABLE = 0x21000001;
	public static final int TILE_TRI_WALKABLE = 0x28000000;
	
	public static final int TILE_TALLGRASS_REGULAR = 0x04200020;
	public static final int TILE_TALLGRASS_YELLOW = 0x04200021;
	public static final int TILE_TALLGRASS_PURPLE = 0x04200022;
	public static final int TILE_TALLGRASS_RED = 0x04200023;
	public static final int TILE_ENCOUNTER_CAVE = 0x24000024;
	
	public static final int TILE_NORMAL_TABLE = 0x010000d4;
	public static final int TILE_RAIL_WALKABLE = 0x2000008b;
	public static final int TILE_RAIL_RIDE = 0x2100008c;
	public static final int TILE_NORMAL_JUMPABLE = 0x20000093;
	public static final int TILE_ICE_SLIDABLE = 0x20000091;
	
	public static final int TILE_RAIL_DROPOUT_NORTH = 0x2100008f;
	public static final int TILE_RAIL_DROPOUT_SOUTH = 0x21000090;
	public static final int TILE_RAIL_DROPOUT_WEST = 0x21000091;
	public static final int TILE_RAIL_DROPOUT_EAST = 0x21000092;
	
	public static final int TILE_LEDGE_FACENORTH = 0x21000074;
	public static final int TILE_LEDGE_FACESOUTH = 0x21000075;
	public static final int TILE_LEDGE_FACEWEST = 0x21000073;
	public static final int TILE_LEDGE_FACEEAST = 0x21000072;
	
	public static final int TILE_SLIDE_FACESOUTH = 0x2000008a;
	public static final int TILE_SLIDE_FACEEAST = 0x20000082;
	public static final int TILE_SLIDE_FACEWEST = 0x20000088;
	public static final int TILE_SLIDE_FACEWESTCYLLAGE = 0x20000083;
	
	public static final int TILE_WALKBLOCK_NORTH = 0x20000053;
	public static final int TILE_WALKBLOCK_SOUTH = 0x20000054;
	public static final int TILE_WALKBLOCK_WEST = 0x20000052;
	public static final int TILE_WALKBLOCK_EAST = 0x20000051;
	
	public static final int TILE_SITTABLE_FACENORTH = 0x210000e8;
	public static final int TILE_SITTABLE_FACESOUTH = 0x210000e7;
	public static final int TILE_SITTABLE_FACEWEST = 0x210000ea;
	public static final int TILE_SITTABLE_FACEEAST = 0x210000e9;
	
	public static final int TILE_SITTABLELONG_FACENORTH = 0x210000ed;
	public static final int TILE_SITTABLELONG_FACESOUTH = 0x210000ec;
	public static final int TILE_SITTABLELONG_FACEWEST = 0x210000ef;
	public static final int TILE_SITTABLELONG_FACEEAST = 0x210000ee;
	
	public static final int TILE_SITTABLEONESIDE_FACESOUTH = 0x210000e3;
	public static final int TILE_SITTABLEONESIDE_FACENORTH = 0x210000e4;
	public static final int TILE_SITTABLEONESIDE_FACEWEST = 0x210000e6;
	
	public static final int TILE_WATER_EDGE = 0x23000e40;
	public static final int TILE_WATER_COLD_EDGEOUT = 0x23003a40;
	public static final int TILE_WATER_COLD_EDGE = 0x23003b40;
	public static final int TILE_WATER_EDGEWEIRD = 0x21003b01;
	public static final int TILE_WATER_RIVER = 0x06000e3d;
	public static final int TILE_WATER_SEA = 0x06000f3d;
	public static final int TILE_WATER_COLDOUT = 0x06003a3d;
	public static final int TILE_WATER_COLD = 0x06003b3d;
	public static final int TILE_WATER_FALL = 0x03000e3f;
	public static final int TILE_WATER_COLDFALL = 0x03003a3f;
	
	public static final int TILE_FOOTPRINT_SAND = 0x20400502;
	public static final int TILE_NOFOOTPRINT_SAND = 0x20000500;
	public static final int TILE_FOOTPRINT_FLOODCHMB = 0x20500007;
	public static final int TILE_FOOTPRINT_FLOODCHMB_TRI = 0x28500007;
	public static final int TILE_FOOTPRINT_WATER = 0x20500507;
	public static final int TILE_FOOTPRINT_SNOW = 0x20380004;
	
	public static final int TILE_TRIGGER_PC = 0x10000d5;
	public static final int TILE_TRIGGER_CLIMB = 0x1000059;
	public static final int TILE_TRIGGER_TRASHCAN = 0x10000dd;
	public static final int TILE_TRIGGER_SORTIMENT = 0x10000db;
	
	public static final int TILE_HM_STRENGHT = 0x21000030;
	
	public int type;
	private byte[] colors;
	public static ArrayList<Integer> tileNums = new ArrayList<Integer>(Arrays.asList(
		TILE_NORMAL_WALKABLE,
		TILE_NORMAL_UNWALKABLE,
		TILE_TRI_WALKABLE,
		TILE_WATER_RIVER,
		TILE_WATER_SEA,
		TILE_WATER_COLD,
		TILE_WATER_COLDOUT,
		TILE_WATER_EDGE,
		TILE_WATER_EDGEWEIRD,
		TILE_WATER_COLD_EDGE,
		TILE_WATER_COLD_EDGEOUT,
		TILE_WATER_FALL,
		TILE_WATER_COLDFALL,
		TILE_TALLGRASS_REGULAR,
		TILE_TALLGRASS_YELLOW,
		TILE_TALLGRASS_PURPLE,
		TILE_TALLGRASS_RED,
		TILE_ENCOUNTER_CAVE,
		TILE_SITTABLE_FACENORTH,
		TILE_SITTABLE_FACESOUTH,
		TILE_SITTABLE_FACEWEST,
		TILE_SITTABLE_FACEEAST,
		TILE_SITTABLELONG_FACENORTH,
		TILE_SITTABLELONG_FACESOUTH,
		TILE_SITTABLELONG_FACEWEST,
		TILE_SITTABLELONG_FACEEAST,
		TILE_SITTABLEONESIDE_FACENORTH,
		TILE_SITTABLEONESIDE_FACESOUTH,
		TILE_SITTABLEONESIDE_FACEWEST,
		0x00000001,
		TILE_NORMAL_TABLE,
		TILE_WALKBLOCK_NORTH,
		TILE_WALKBLOCK_SOUTH,
		TILE_WALKBLOCK_WEST,
		TILE_WALKBLOCK_EAST,
		TILE_LEDGE_FACENORTH,
		TILE_LEDGE_FACESOUTH,
		TILE_LEDGE_FACEWEST,
		TILE_LEDGE_FACEEAST,
		0x00000002,
		TILE_SLIDE_FACESOUTH,
		TILE_SLIDE_FACEWEST,
		TILE_SLIDE_FACEWESTCYLLAGE,
		TILE_SLIDE_FACEEAST,
		TILE_RAIL_WALKABLE,
		TILE_RAIL_RIDE,
		TILE_RAIL_DROPOUT_NORTH,
		TILE_RAIL_DROPOUT_SOUTH,
		TILE_RAIL_DROPOUT_WEST,
		TILE_RAIL_DROPOUT_EAST,
		TILE_FOOTPRINT_SAND,
		TILE_NOFOOTPRINT_SAND,
		TILE_FOOTPRINT_SNOW,
		TILE_FOOTPRINT_WATER,
		TILE_FOOTPRINT_FLOODCHMB,
		TILE_FOOTPRINT_FLOODCHMB_TRI,
		TILE_ICE_SLIDABLE,
		TILE_NORMAL_JUMPABLE,
		TILE_TRIGGER_CLIMB,
		TILE_TRIGGER_PC,
		TILE_TRIGGER_SORTIMENT,
		TILE_TRIGGER_TRASHCAN,
		TILE_HM_STRENGHT
	));
	public GRColorPalette() {
		//default color palette
		this.type = TYPE_PURECOLOR;
		try {
			InputStream in = new FileInputStream("default.gmp");
			colors = new byte[in.available()];
			in.read(colors);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Color getColor(byte[] tiledata) {
		int index = tileNums.indexOf(ba2int(tiledata));
		if (index != -1) {
			int x = colors[index*4 + 1];
			x = (x << 8) | (colors[index*4 + 2] & 0xFF);
			x = (x << 8) | (colors[index*4 + 3] & 0xFF);
			return new Color(x);
		}
		else {
			return new Color(0xff0000);
		}
	}
	private int ba2int(byte[] b) {
		int x = b[0];
		x = (x << 8) | (b[1] & 0xFF);
		x = (x << 8) | (b[2] & 0xFF);
		x = (x << 8) | (b[3] & 0xFF);
		return x;
	}
	public static void main(String[] args) {
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream("default.gmp"));
			ImageMapCreator imc = new ImageMapCreator();
			for (int i = 0; i < tileNums.size(); i++) {
				int out;
				switch (tileNums.get(i)) {
				case TILE_NORMAL_WALKABLE:
					out = 0x2;
					break;
				case TILE_NORMAL_UNWALKABLE:
					out = 0x1;
					break;
				case TILE_TRI_WALKABLE:
					out = 0x3;
					break;
				case TILE_WATER_RIVER:
					out = 0x4;
					break;
				case TILE_WATER_SEA:
					out = 0x5;
					break;
				case TILE_WATER_COLD_EDGE:
				case TILE_WATER_COLD_EDGEOUT:
				case TILE_WATER_EDGEWEIRD:
				case TILE_WATER_EDGE:
					out = 0x6;
					break;
				case TILE_WATER_COLDOUT:
				case TILE_WATER_COLD:
					out = 0x7;
					break;
				case TILE_TALLGRASS_REGULAR:
					out = 10;
					break;
				case TILE_TALLGRASS_YELLOW:
					out = 11;
					break;
				case TILE_TALLGRASS_PURPLE:
					out = 12;
					break;
				case TILE_ENCOUNTER_CAVE:
					out = 14;
					break;
				case TILE_NORMAL_TABLE:
					out = 20;
					break;
				case TILE_RAIL_WALKABLE:
					out = 21;
					break;
				case TILE_RAIL_RIDE:
					out = 22;
					break;
				case TILE_NORMAL_JUMPABLE:
					out = 23;
					break;
				case TILE_WATER_COLDFALL:
				case TILE_WATER_FALL:
					out = 24;
					break;
				case TILE_NOFOOTPRINT_SAND:
				case TILE_FOOTPRINT_SAND:
					out = 25;
					break;
				case TILE_FOOTPRINT_FLOODCHMB:
				case TILE_FOOTPRINT_FLOODCHMB_TRI:
				case TILE_FOOTPRINT_WATER:
					out = 26;
					break;
				case TILE_TRIGGER_PC:
					out = 27;
					break;
				case TILE_TRIGGER_CLIMB:
					out = 28;
					break;
				case TILE_TRIGGER_TRASHCAN:
					out = 30;
					break;
				case TILE_TRIGGER_SORTIMENT:
					out = 31;
					break;
				case TILE_ICE_SLIDABLE:
					out = 32;
					break;
				case TILE_FOOTPRINT_SNOW:
					out = 33;
					break;
				case TILE_HM_STRENGHT:
					out = 29;
					break;
				case TILE_RAIL_DROPOUT_NORTH:
					out = 71;
					break;
				case TILE_RAIL_DROPOUT_SOUTH:
					out = 72;
					break;
				case TILE_RAIL_DROPOUT_WEST:
					out = 73;
					break;
				case TILE_RAIL_DROPOUT_EAST:
					out = 74;
					break;
				case TILE_LEDGE_FACENORTH:
					out = 51;
					break;
				case TILE_SLIDE_FACESOUTH:
				case TILE_LEDGE_FACESOUTH:
					out = 52;
					break;
				case TILE_SLIDE_FACEWEST:
				case TILE_SLIDE_FACEWESTCYLLAGE:
				case TILE_LEDGE_FACEWEST:
					out = 53;
					break;
				case TILE_SLIDE_FACEEAST:
				case TILE_LEDGE_FACEEAST:
					out = 54;
					break;
				case TILE_WALKBLOCK_NORTH:
					System.out.println("wb");
					out = 81;
					break;
				case TILE_WALKBLOCK_SOUTH:
					out = 82;
					break;
				case TILE_WALKBLOCK_WEST:
					out = 83;
					break;
				case TILE_WALKBLOCK_EAST:
					out = 84;
					break;
				case TILE_SITTABLELONG_FACENORTH:
				case TILE_SITTABLEONESIDE_FACENORTH:
				case TILE_SITTABLE_FACENORTH:
					out = 61;
					break;
				case TILE_SITTABLELONG_FACESOUTH:
				case TILE_SITTABLEONESIDE_FACESOUTH:
				case TILE_SITTABLE_FACESOUTH:
					out = 62;
					break;
				case TILE_SITTABLELONG_FACEWEST:
				case TILE_SITTABLEONESIDE_FACEWEST:
				case TILE_SITTABLE_FACEWEST:
					out = 63;
					break;
				case TILE_SITTABLELONG_FACEEAST:
				case TILE_SITTABLE_FACEEAST:
					out = 64;
					break;
				default:
					out = 0;
					//special cases
				}
				dos.writeInt(imc.getColor(out));
			}
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
