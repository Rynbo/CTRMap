/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.formats.mapmatrix;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import ctrmap.Workspace;
import ctrmap.formats.containers.GR;
import ctrmap.formats.containers.MM;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapMatrix {

	public MM file;
	public short hasLOD;
	public short unknown;
	public short width;
	public short height;
	public List<MatrixCameraBoundaries> cambounds = new ArrayList<>();
	public ResizeableMatrix<GR> regions;
	public ResizeableMatrix<Short> ids;
	public ResizeableMatrix<Short> zones;
	public ResizeableMatrix<Short> LOD;

	private int boundaryEntries;

	public MapMatrix(MM f) {
		file = f;
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(f.getFile(0)));
			hasLOD = dis.readShort();
			unknown = dis.readShort();
			width = dis.readShort();
			height = dis.readShort();
			ids = new ResizeableMatrix<>(width, height, (short) -1);
			regions = new ResizeableMatrix<>(width, height, null);
			zones = new ResizeableMatrix<>(width * 4, height * 4, (short) -1); //every region has 4x4 segments where zones can be switched freely (almost)
			LOD = new ResizeableMatrix<>(width, height, (short) -1); //LOD is their indexes in AD, need to figure out how they are determined
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					ids.set(j, i, dis.readShort());
					if (Workspace.valid && ids.get(j, i) != -1) {
						regions.set(j, i, new GR(Workspace.getWorkspaceFile(Workspace.ArchiveType.FIELD_DATA, ids.get(j, i))));
					}
				}
			}
			if (hasLOD == 1) { // for some reason, the LOD flag triggers both the zone switch and LOD
				for (int i = 0; i < height * 4; i++) {
					for (int j = 0; j < width * 4; j++) {
						zones.set(j, i, dis.readShort());
					}
				}
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						LOD.set(j, i, dis.readShort());
					}
				}
			}
			//should be at EOSection now
			dis.close();
			dis = new LittleEndianDataInputStream(new ByteArrayInputStream(f.getFile(1)));
			boundaryEntries = dis.readInt();
			for (int i = 0; i < boundaryEntries; i++) {
				cambounds.add(new MatrixCameraBoundaries(dis));
			}
			dis.close();
		} catch (Exception ex) {
			Logger.getLogger(MapMatrix.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public MapMatrix(MM f, int width, int height, int hasLOD) {
		file = f;
		this.width = (short) width;
		this.height = (short) height;
		this.hasLOD = (short) hasLOD;
		unknown = 0;
		ids = new ResizeableMatrix<>(width, height, (short) -1);
		regions = new ResizeableMatrix<>(width, height, null);
		zones = new ResizeableMatrix<>(width * 4, height * 4, (short) -1); //every region has 4x4 segments where zones can be switched freely (almost)
		LOD = new ResizeableMatrix<>(width, height, (short) -1);
		MatrixCameraBoundaries mcb = new MatrixCameraBoundaries();
		mcb.isRepeal = 0;
		mcb.north = -20;
		mcb.south = 740;
		mcb.west = -20;
		mcb.east = 740;
	}

	public byte[] assembleData() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(out);
			dos.writeShort(hasLOD);
			dos.writeShort(unknown);
			dos.writeShort(width);
			dos.writeShort(height);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					dos.writeShort(ids.get(j, i));
				}
			}
			if (hasLOD == 1) {
				for (int i = 0; i < height * 4; i++) {
					for (int j = 0; j < width * 4; j++) {
						dos.writeShort(zones.get(j, i));
					}
				}
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						dos.writeShort(LOD.get(j, i));
					}
				}
			}
			//pad to 4
			while (dos.size() % 4 != 0) {
				dos.write(0);
			}
			dos.close();
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(MapMatrix.class.getName()).log(Level.SEVERE, null, ex);
			return file.getFile(0);
		}
	}

	public byte[] assembleCamData() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(out);
			boundaryEntries = cambounds.size();
			dos.writeInt(boundaryEntries);
			for (int i = 0; i < boundaryEntries; i++) {
				cambounds.get(i).write(dos);
			}
			dos.close();
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(MapMatrix.class.getName()).log(Level.SEVERE, null, ex);
			return file.getFile(1);
		}
	}

	public void write() {
		file.storeFile(0, assembleData());
		file.storeFile(1, assembleCamData());
	}

	public static class ResizeableMatrix<T> {

		public ArrayList<ArrayList<T>> list = new ArrayList<>();
		private T defaultValue;

		public ResizeableMatrix(int width, int height, T defaultValue) {
			this.defaultValue = defaultValue;
			ensureSize(list, width, false);
			for (int i = 0; i < width; i++) {
				ensureSize(list.get(i), height, true);
			}
		}

		public int getWidth() {
			return list.size();
		}

		public int getHeight() {
			if (list.size() > 0) {
				return list.get(0).size();
			} else {
				return 0;
			}
		}

		private void ensureSize(ArrayList l, int size, boolean isDefault) {
			for (int i = l.size(); i < size; i++) {
				l.add((isDefault) ? defaultValue : new ArrayList<T>());
			}
		}

		public void set(int x, int y, T value) {
			list.get(x).set(y, value);
		}

		public T get(int x, int y) {
			return list.get(x).get(y);
		}

		public void resizeMatrix(int newWidth, int newHeight) {
			for (int i = list.size() - 1; i >= 0; i--) {
				if (i >= newWidth) {
					list.remove(i);
				}
			}
			for (int i = 0; i < list.size(); i++) {
				ArrayList<T> l = list.get(i);
				for (int j = l.size(); j >= 0; j--) {
					if (j >= newHeight) {
						l.remove(j);
					}
				}
			}
			//removal done, now ensure size if width or height was upped
			ensureSize(list, newWidth, false);
			for (int i = 0; i < newWidth; i++) {
				ensureSize(list.get(i), newHeight, true);
			}
		}

		public void addColumn() {
			addColumns(1);
		}

		public void addColumns(int count) {
			int origHeight = (list.size() > 0) ? list.get(0).size() : 0;
			for (int i = 0; i < count; i++) {
				list.add(new ArrayList<>());
				ensureSize(list.get(list.size() - 1), origHeight, true);
			}
		}

		public void addRow() {
			addRows(1);
		}

		public void addRows(int count) {
			int origHeight = (list.size() > 0) ? list.get(0).size() : 0;
			for (int i = 0; i < list.size(); i++) {
				ensureSize(list.get(i), origHeight + count, true);
			}
		}

		public void removeColumn() {
			removeColumns(1);
		}

		public void removeColumns(int count) {
			for (int i = 0; i < count; i++) {
				list.remove(list.size() - 1);
			}
		}

		public void removeRow() {
			removeRows(1);
		}

		public void removeRows(int count) {
			for (int i = 0; i < list.size(); i++) {
				for (int j = 0; j < count; j++) {
					list.get(i).remove(list.get(i).size() - 1);
				}
			}
		}
	}
}
