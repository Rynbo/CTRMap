package ctrmap.formats.cameradata;

import java.io.IOException;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Camera data entry loader
 */
public class CameraData {

	public byte[] unknownBytes = new byte[16];
	public int acceptCoords2; //should 2nd coords be taken into consideration for transitions etc? Same as 07 in movDir but used a few times, changes the fov by a %;
	public int acceptCoords1; //same for 1
	public short transitionPeriod; //*35ms
	public int layer; //layer
	public int unknown00;
	public int isNeutral; //only used in the rock gym (gym03/cyllage) but has a clear purpose - when set to 02, it acts as a neutral zone where the camera angle remains
	//the same when approached from different sides. serves in corners to retain camera angle when walked on from multiple camera'd tiles. when set to 00 on a 02 cam,
	//the cam acts as normal with a 00 FOV and angles, causing the game to look (in case of Grant's gym) towards the waterfall.
	public int unknown01or03;

	public CameraCoordinates coords1;

	public short boundY1;
	public short boundY2;
	public short boundX1;
	public short boundX2;

	public int unknownInt1;

	public short movementDirection; //1 is horizontal, 2 is vertical, 7 is fixed

	public short isFirstEnabled; //0000 is yes, FFFF is no, comes first in file but second from top/left in order
	public short isSecondEnabled;

	public short unknownFFFF;

	public CameraCoordinates coords2;

	public CameraData(LittleEndianDataInputStream dis) {
		try {
			dis.read(unknownBytes);
			acceptCoords2 = dis.read();
			acceptCoords1 = dis.read();
			transitionPeriod = dis.readShort();
			layer = dis.readByte();
			unknown00 = dis.read();
			isNeutral = dis.read();
			unknown01or03 = dis.read();

			coords1 = new CameraCoordinates(dis);

			boundY1 = dis.readShort();
			boundY2 = dis.readShort();
			boundX1 = dis.readShort();
			boundX2 = dis.readShort();

			unknownInt1 = dis.readInt();

			movementDirection = dis.readShort(); //maybe it's short considering 00_3 is always 0, but why would it be?
			//always 1, 2, 7 except for weird cases - e4 rooms (0) another weird one is parfum palace with
			//an incomplete tilemap connected with weird stuff I'll probably never RE used for the grass gym etc.
			//the stuff with "track" "connect" "line@normal" "joint" and the like.

			isFirstEnabled = dis.readShort();
			isSecondEnabled = dis.readShort();

			unknownFFFF = dis.read2Bytes(); //also always FFFF except for e4 rooms and guess what, parfum palace

			coords2 = new CameraCoordinates(dis);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CameraData() {
		unknownBytes = new byte[16];
		acceptCoords2 = 0x1;
		acceptCoords1 = 0x1;
		transitionPeriod = 0x0f;
		layer = 0xFF;
		isNeutral = 0x00;
		unknown01or03 = 0x01;

		coords1 = new CameraCoordinates();

		boundY1 = 0;
		boundY2 = 0;
		boundX1 = 0;
		boundX2 = 0;

		unknownInt1 = 0;

		movementDirection = 0x7;

		isFirstEnabled = 0;
		isSecondEnabled = 0;

		unknownFFFF = -1;

		coords2 = new CameraCoordinates();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof CameraData) {
			CameraData c2 = (CameraData) o;
			return this.acceptCoords1 == c2.acceptCoords1 && this.acceptCoords2 == c2.acceptCoords2
					&& this.boundX1 == c2.boundX1 && this.boundX2 == c2.boundX2
					&& this.boundY1 == c2.boundY1 && this.boundY2 == c2.boundY2
					&& this.coords1.equals(c2.coords1) && this.coords2.equals(c2.coords2)
					&& this.isFirstEnabled == c2.isFirstEnabled && this.isSecondEnabled == c2.isSecondEnabled
					&& this.isNeutral == c2.isNeutral && this.layer == c2.layer
					&& this.transitionPeriod == c2.transitionPeriod && this.movementDirection == c2.movementDirection
					&& this.unknown00 == c2.unknown00 && this.unknown01or03 == c2.unknown01or03;
			
		}
		return false;
	}

	public void write(LittleEndianDataOutputStream dos) {
		try {
			dos.write(unknownBytes);
			dos.write(acceptCoords2);
			dos.write(acceptCoords1);
			dos.writeShort(transitionPeriod);
			dos.writeByte(layer);
			dos.write(unknown00);
			dos.write(isNeutral);
			dos.write(unknown01or03);

			coords1.write(dos);

			dos.writeShort(boundY1);
			dos.writeShort(boundY2);
			dos.writeShort(boundX1);
			dos.writeShort(boundX2);
			dos.writeInt(unknownInt1);
			dos.writeShort(movementDirection);
			dos.writeShort(isFirstEnabled);
			dos.writeShort(isSecondEnabled);
			dos.write2Bytes(unknownFFFF);

			coords2.write(dos);
		} catch (IOException ex) {
			Logger.getLogger(CameraData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
