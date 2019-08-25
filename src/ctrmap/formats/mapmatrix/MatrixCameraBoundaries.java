/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.formats.mapmatrix;

import ctrmap.LittleEndianDataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatrixCameraBoundaries {
	public int isRepeal;
	public float north;
	public float south;
	public float west;
	public float east;
	public MatrixCameraBoundaries(LittleEndianDataInputStream dis){
		try {
			north = dis.readFloat();
			south = dis.readFloat();
			west = dis.readFloat();
			east = dis.readFloat();
			isRepeal = dis.readInt();
		} catch (IOException ex) {
			Logger.getLogger(MatrixCameraBoundaries.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
