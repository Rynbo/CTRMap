package ctrmap.formats.garc;

import ctrmap.CtrmapMainframe;
import ctrmap.LittleEndianDataInputStream;
import ctrmap.Workspace;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GARC implementation, ported mostly from the original Ohana3DS and pk3DS, but will be soon rewritten.
 */
public class GARC {

	public File file;

	public short padding;
	
	private ArrayList<GARCEntry> entries = new ArrayList<>();
	
	public int length;
	
	public GARC(File f) {
		try {
			this.file = f;
			
			RandomAccessFile in = new RandomAccessFile(f, "r");
			
			byte[] strbuf = new byte[4];
			in.read(strbuf);
			
			String garcMagic = new String(strbuf);
			int garcLength = Integer.reverseBytes(in.readInt());
			short endian = Short.reverseBytes(in.readShort());
			short version = Short.reverseBytes(in.readShort());
			int sectionCount = Integer.reverseBytes(in.readInt());
			int dataOffset = Integer.reverseBytes(in.readInt());
			int decompressedLength = Integer.reverseBytes(in.readInt());
			int compressedLength = Integer.reverseBytes(in.readInt());
			padding = 4; //version 4 has always nearest pad to 4, ver 6 can specify more
			
			in.seek(garcLength);
			
			long fatoPosition = in.getFilePointer();
			in.read(strbuf);
            String fatoMagic = new String(strbuf);
            int fatoLength = Integer.reverseBytes(in.readInt());
            short fatoEntries = Short.reverseBytes(in.readShort());
			length = fatoEntries;
			short pad = in.readShort(); //0xFFFF
			
			long fatbPosition = fatoPosition + fatoLength;
			for (int i = 0; i < fatoEntries; i++)
            {
                in.seek(fatoPosition + 0xc + i * 4);
                in.seek(Integer.reverseBytes(in.readInt()) + fatbPosition + 0xc);

                int flags = Integer.reverseBytes(in.readInt());

                String folder = "";

                if (flags != 1) folder = String.format("folder_{0:D5}/", i);

                for (int bit = 0; bit < 32; bit++)
                {
                    if ((flags & (1 << bit)) > 0)
                    {
                        int startOffset = Integer.reverseBytes(in.readInt());
                        int endOffset = Integer.reverseBytes(in.readInt());
                        int length = Integer.reverseBytes(in.readInt());

                        long position = in.getFilePointer();

                        in.seek(startOffset + dataOffset);

                        byte[] buffer = new byte[length];
                        in.read(buffer);

                        boolean isCompressed = buffer.length > 0 ? buffer[0] == 0x11 : false;
                        String name = String.valueOf(i);

                        GARCEntry entry = new GARCEntry();
                        entry.name = name;
                        entry.offset = startOffset + dataOffset;
                        entry.length = length;
                        entry.compressed = isCompressed;
						entries.add(entry);
						
                        in.seek(position);
                    }
                }
            }
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(GARC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	public void packDirectory(File dir){
		try {
			if (!dir.isDirectory()){
				return;
			}
			ArrayList<File> files = new ArrayList<>();
			files.addAll(Arrays.asList(dir.listFiles()));
			for (int i = 0; i < files.size(); i++){
				if (!Workspace.persist_paths.contains(files.get(i).getAbsolutePath())){
					files.remove(i);
					i--;
				}
			}
			Collections.sort(files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					int i1 = Integer.parseInt(o1.getName());
					int i2 = Integer.parseInt(o2.getName());
					return i1 - i2;
				}
			});
			int[] changedIndices = new int[files.size()];
			for (int i = 0; i < files.size(); i++){
				changedIndices[i] = Integer.valueOf(files.get(i).getName());
			}	
			File newGARC = new File(Workspace.WORKSPACE_PATH + "/" + file.getName() + "_new");
			//compress all custom files
			byte[][] compressedData = new byte[files.size()][];
			for (int i = 0; i < files.size(); i++){
				InputStream in = new FileInputStream(files.get(i));
				byte[] or = new byte[in.available()];
				in.read(or);
				in.close();
				if (entries.get(changedIndices[i]).compressed){
					compressedData[i] = LZ11.compress(or);
				}
				else{
					compressedData[i] = or;
				}
			}
			//get largest unpadded size
			int maxlength = 0;
			int[] filelengths = new int[compressedData.length];
			int[] padlengths = new int[compressedData.length];
			for (int i = 0; i < compressedData.length; i++){
				int len = compressedData[i].length;
				filelengths[i] = len;
				int remainder = len % padding;
				int padLength = (remainder == 0) ? 0 : padding - remainder;
				padlengths[i] = padLength;
				if (len + padLength > maxlength) maxlength = len + padLength;
			}	
			LittleEndianDataInputStream old = new LittleEndianDataInputStream(new FileInputStream(file));
			newGARC.delete();
			RandomAccessFile dos = new RandomAccessFile(newGARC, "rw");
			//first 14 bytes of header should be unchanged - let's copy paste them
			byte[] buf = new byte[16];
			old.read(buf);
			dos.write(buf);
			dos.writeInt(0); //TEMPORARY - will be replaced with data offset
			dos.writeInt(0); //TEMPORARY - will be replaced with file size
			old.skip(8);
			int lastMaxSize = old.readInt();
			dos.writeInt(Integer.reverseBytes(Math.max(lastMaxSize, maxlength))); //write either the max unpadded length of new or original files
			//FATO points to FATB - is unchanged
			//we need to read original FATO length
			int fatoMagic = old.readInt();
			int fatoLength = old.readInt();
			buf = new byte[fatoLength - 8]; //subtract 8 as we have already read the first 8 bytes of the section
			old.read(buf);
			dos.writeInt(Integer.reverseBytes(fatoMagic));
			dos.writeInt(Integer.reverseBytes(fatoLength));
			dos.write(buf);
			//we are at the beginning of FATB in both the original stream and the new file
			//we now need to shift the offsets of all files
			//first we just rewrite the magic, length and entry count
			dos.writeInt(Integer.reverseBytes(old.readInt())); //magic
			dos.writeInt(Integer.reverseBytes(old.readInt())); //FATB length
			int entryCount = old.readInt();
			dos.writeInt(Integer.reverseBytes(entryCount)); //FATB entry count
			FATBEntry[] fatbe = new FATBEntry[entryCount];
			for (int i = 0; i < fatbe.length; i++){
				fatbe[i] = new FATBEntry();
				fatbe[i].flags = old.readInt();
				fatbe[i].offset = old.readInt();
				fatbe[i].endOffset = old.readInt();
				fatbe[i].len = old.readInt();
			}
			//end of FATB for original
			int offsetShift = 0;
			int processedCustomFiles = 0;
			for (int i = 0; i < fatbe.length; i++){
				FATBEntry e = fatbe[i];
				if (indexOfIntArray(changedIndices, i) != -1){
					//write changed file info
					dos.writeInt(Integer.reverseBytes(1));
					dos.writeInt(Integer.reverseBytes(e.offset + offsetShift));
					int endOffset = e.offset + offsetShift + filelengths[processedCustomFiles] + padlengths[processedCustomFiles];
					dos.writeInt(Integer.reverseBytes(endOffset));
					dos.writeInt(Integer.reverseBytes(filelengths[processedCustomFiles]));
					offsetShift += endOffset - (e.endOffset + offsetShift);
					processedCustomFiles ++;
				}
				else {
					dos.writeInt(Integer.reverseBytes(e.flags));
					dos.writeInt(Integer.reverseBytes(e.offset + offsetShift));
					dos.writeInt(Integer.reverseBytes(e.endOffset + offsetShift));
					dos.writeInt(Integer.reverseBytes(e.len));
				}
			}
			buf = new byte[8];
			old.read(buf);
			dos.write(buf);
			//written static part of FIMB
			//the last one is data length - we need it as the last thing ever written, so we dummy it out and mark the position
			int dataLengthPos = (int)dos.getFilePointer();
			dos.writeInt(0);
			//we are at data now, let's write it in
			processedCustomFiles = 0;
			for (int i = 0; i < fatbe.length; i++){
				FATBEntry e = fatbe[i];
				if (indexOfIntArray(changedIndices, i) != -1){
					dos.write(compressedData[processedCustomFiles]);
					for (int j = 0; j < padlengths[processedCustomFiles]; j++){
						dos.write(0xFF);
					}
					processedCustomFiles++;
				}
				else {
					InputStream entryReader = new FileInputStream(file);
					byte[] b = new byte[entries.get(i).length];
					entryReader.skip(entries.get(i).offset);
					entryReader.read(b);
					dos.write(b);
					int remainder = b.length % padding;
					int padLength = (remainder == 0) ? 0 : padding - remainder;
					for (int j = 0; j < padLength; j++){
						dos.write(0xFF);
					}
					entryReader.close();
				}
			}
			int totalLength = (int)dos.length();
			int dataLength = totalLength - (dataLengthPos + 4); //dLP is the offset in FIMB after which the data follows
			dos.seek(0x10);
			dos.writeInt(Integer.reverseBytes(dataLengthPos + 4));
			dos.writeInt(Integer.reverseBytes(totalLength));
			dos.seek(dataLengthPos);
			dos.writeInt(Integer.reverseBytes(dataLength));
			dos.close();
			old.close();
			Files.move(newGARC.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			Logger.getLogger(GARC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	public byte[] getDecompressedEntry(int num){
		if (num >= entries.size()) return null;
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new FileInputStream(file));
			dis.skip(entries.get(num).offset);
			byte[] data = new byte[entries.get(num).length];
			dis.read(data);
			dis.close();
			if (entries.get(num).compressed){
				return LZ11.decompress(data);
			}
			else{
				return data;
			}
		} catch (IOException ex) {
			Logger.getLogger(GARC.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	public static int indexOfIntArray(int[] array, int key) {
		int returnvalue = -1;
		for (int i = 0; i < array.length; ++i) {
			if (key == array[i]) {
				returnvalue = i;
				break;
			}
		}
		return returnvalue;
	}
	public static void main(String[] args){
		GARC garc = new GARC(new File("1"));
		garc.packDirectory(new File("1_pack"));
	}
}
class GARCEntry{
	String name;
	int offset;
	int length;
	boolean compressed;
}
class FATBEntry{
	int flags;
	int offset;
	int endOffset;
	int len;
}
