/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.formats.mapmatrix;

import static ctrmap.CtrmapMainframe.*;
import ctrmap.LittleEndianDataInputStream;
import ctrmap.Workspace;
import ctrmap.formats.GR;
import ctrmap.formats.MM;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapMatrix {
	private MM file;
	private short hasLOD;
	private short unknown;
	public short width;
	public short height;
	public MatrixCameraBoundaries[] cambounds;
	public GR[][] regions;
	public short[][] ids;
	public short[][] zones;
	public short[][] LOD;
	
	private int boundaryEntries;
	
	public MapMatrix(MM f){
		file = f;
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(f.getFile(0)));
			hasLOD = dis.readShort();
			unknown = dis.readShort();
			width = dis.readShort();
			height = dis.readShort();
			ids = new short[width][height];
			regions = new GR[width][height];
			for (int i = 0; i < height; i++){
				for (int j = 0; j < width; j++){
					ids[j][i] = dis.readShort();
					if (mWorkspace.valid && ids[j][i] != -1){
						regions[j][i] = new GR(mWorkspace.getWorkspaceFile(Workspace.ArchiveType.FIELD_DATA, ids[j][i]));
					}
				}
			}
			if (hasLOD == 1){ // for some reason, the LOD flag triggers both the zone switch and LOD
				zones = new short[width*4][height*4]; //every region has 4x4 segments where zones can be switched freely (almost)
				LOD = new short[width][height]; //LOD is their indexes in AD, need to figure out how they are determined
				for (int i = 0; i < height*4; i++){
					for (int j = 0; j < width*4; j++){
						zones[j][i] = dis.readShort();
					}
				}
				for (int i = 0; i < height; i++){
					for (int j = 0; j < width; j++){
						LOD[j][i] = dis.readShort();
					}
				}
			}
			//should be at EOSection now
			dis.close();
			dis = new LittleEndianDataInputStream(new ByteArrayInputStream(f.getFile(1)));
			boundaryEntries = dis.readInt();
			cambounds = new MatrixCameraBoundaries[boundaryEntries];
			for (int i = 0; i < boundaryEntries; i++){
				cambounds[i] = new MatrixCameraBoundaries(dis);
			}
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(MapMatrix.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
