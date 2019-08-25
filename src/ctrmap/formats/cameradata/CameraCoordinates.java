package ctrmap.formats.cameradata;

import java.io.IOException;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraCoordinates {

	public float yawShift;
	public float pitchShift;
	public float pitch;
	public float yaw;
	public float FOV; //this one is soo fun
	public float distanceFromTarget;
	public float roll;

	public CameraCoordinates(LittleEndianDataInputStream dis) {
		try {
			this.yawShift = dis.readFloat();
			this.pitchShift = dis.readFloat();
			this.pitch = dis.readFloat();
			this.yaw = dis.readFloat();
			this.FOV = dis.readFloat();
			this.distanceFromTarget = dis.readFloat();
			this.roll = dis.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CameraCoordinates() {
		this.yawShift = 0;
		this.pitchShift = 0;
		this.pitch = -45;
		this.yaw = 0;
		this.FOV = 30;
		this.distanceFromTarget = 200;
		this.roll = 0;
	}

	public void write(LittleEndianDataOutputStream dos) {
		try {
			dos.writeFloat(yawShift);
			dos.writeFloat(pitchShift);
			dos.writeFloat(pitch);
			dos.writeFloat(yaw);
			dos.writeFloat(FOV);
			dos.writeFloat(distanceFromTarget);
			dos.writeFloat(roll);
		} catch (IOException ex) {
			Logger.getLogger(CameraCoordinates.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
