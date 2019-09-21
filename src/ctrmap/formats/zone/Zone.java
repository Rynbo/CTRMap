package ctrmap.formats.zone;

import ctrmap.Utils;
import ctrmap.formats.containers.ZO;
import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 * Zone data container class, incomplete.
 */
public class Zone {
	public ZO file;
	public ZoneHeader header;
	public ZoneEntities entities;
	public Zone(ZO data){
		file = data;
		header = new ZoneHeader(data.getFile(0));
		entities = new ZoneEntities(data.getFile(1));
	}
	public boolean store(boolean dialog){
		byte[] headerData = header.assembleData();
		if (!Arrays.equals(headerData, file.getFile(0))){
			/*System.out.println(Arrays.toString(headerData));
			System.out.println(Arrays.toString(file.getFile(0)));*/
			if (dialog){
				int result = Utils.showSaveConfirmationDialog("Zone header");
				switch (result){
					case JOptionPane.YES_OPTION:
						file.storeFile(0, headerData);
					case JOptionPane.NO_OPTION:
						break;
					case JOptionPane.CANCEL_OPTION:
						return false;
				}
			}
			else {
				file.storeFile(0, headerData);
			}
		}
		if (entities.modified){
			if (dialog){
				int result = Utils.showSaveConfirmationDialog("Entity data");
				switch (result){
					case JOptionPane.YES_OPTION:
						file.storeFile(1, entities.assembleData());;
					case JOptionPane.NO_OPTION:
						break;
					case JOptionPane.CANCEL_OPTION:
						return false;
				}
			}
			else {
				file.storeFile(1, entities.assembleData());
			}
			entities.modified = false;
		}
		return true;
	}
}
