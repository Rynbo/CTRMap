package ctrmap.formats.scripts;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import java.io.IOException;

public class PawnPrefixEntry {
	public Type type;
	public int[] data;
	
	public PawnPrefixEntry(int defsize, Type type, LittleEndianDataInputStream source) throws IOException{
		this.type = type;
		data = new int[defsize / 4];
		for (int i = 0; i < data.length; i++){
			data[i] = source.readInt();
		}
	}
	
	public PawnPrefixEntry(int defsize, Type type, int[] data){
		this.type = type;
		if (data == null){
			this.data = new int[defsize];
		}
		else {
			this.data = data;
		}
	}
	
	public void write(LittleEndianDataOutputStream out) throws IOException{
		for (int i = 0; i < data.length; i++){
			out.writeInt(data[i]);
		}
	}
	
	public enum Type{
		PUBLIC,
		NATIVE,
		LIBRARY,
		PUBLIC_VAR,
		TAG,
		NAME,
		UNKNOWN
	}
}
