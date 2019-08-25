package ctrmap;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ParserLoader {
	public static final int TILE_NORMAL_WALKABLE = 0x20000000;
	public static final int TILE_NORMAL_UNWALKABLE = 0x21000001;
	public static final int TILE_TRI_WALKABLE = 0x28000000;
	
	public static final int TILE_TALLGRASS_REGULAR = 0x04200020;
	public static final int TILE_TALLGRASS_YELLOW = 0x04200021;
	public static final int TILE_TALLGRASS_PURPLE = 0x04200022;
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
	
	public static final int TILE_SITTABLEINS_FACESOUTH = 0x210000e3;
	public static final int TILE_SITTABLEINS_FACENORTH = 0x210000e4;
	public static final int TILE_SITTABLEINS_FACEWEST = 0x210000e6;
	
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

	public static void main(String[] args) {
		int[][] data = new int[40][40];
		if (args.length == 0) {
			args = new String[1];
			args[0] = new File("r16_southeast.153").getAbsolutePath();
		}
		for (int i = 0; i < args.length; i++) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(args[i]));
				//skip first 132 bytes - nothing useful here
				dis.skip(0x84);
				//Gamefreak GR maps have 40x40 tiles
				for (int y = 0; y < 40; y++) {
					for (int x = 0; x < 40; x++) {
						int tiledata;
						int out;
						tiledata = dis.readInt();
						//most common tiles are 0 - 3, then 6 - 9 are used for oriented tiles (like chairs)
						//tiles 1* are tall grass type tiles
						//tiles 2* are special tiles
						//3* are chair tiles
						//4* are ledge tiles
						//5* are rail dropout points
						switch (tiledata) {
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
							case TILE_SITTABLEINS_FACENORTH:
							case TILE_SITTABLE_FACENORTH:
								out = 61;
								break;
							case TILE_SITTABLELONG_FACESOUTH:
							case TILE_SITTABLEINS_FACESOUTH:
							case TILE_SITTABLE_FACESOUTH:
								out = 62;
								break;
							case TILE_SITTABLELONG_FACEWEST:
							case TILE_SITTABLEINS_FACEWEST:
							case TILE_SITTABLE_FACEWEST:
								out = 63;
								break;
							case TILE_SITTABLELONG_FACEEAST:
							case TILE_SITTABLE_FACEEAST:
								out = 64;
								break;
							default:
								System.out.println("Unknown tile at " + x + "x" + y + " byte dump: " + Integer.toHexString(tiledata));
								out = 0;
								//special cases
						}
						data[x][y] = out;
						//byte1 = 0x20 - walkable 0x21 unwalkable 0x28 ??? maybe warp idk
						//data assembly - 0x0 = unknown, treat as unw, 0x1 unwalkable normal, 0x2 walkable normal 
					}
				}
				ImageMapCreator imc = new ImageMapCreator();
				imc.createImage(data, new File(args[i]).getName());
				dis.close();			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
