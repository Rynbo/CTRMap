package ctrmap.formats.text;

import ctrmap.LittleEndianDataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * X/Y text file reading and decryption, documented in Kaphotics' xytext
 */
public class TextFile {

	private int sectionCount;
	private int lineCount;
	private int dataLength;
	private int initialKey;
	private int sectionOffset;
	private int sectionLength;

	private int key;

	public String[] lines;

	public TextFile(File f) {
		try {
			LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new FileInputStream(f));
			sectionCount = dis.readShort(); //always 1 from what I know so we don't need to loop through this
			lineCount = dis.readShort();
			if (lineCount == 0) {
				return;
			}
			dataLength = dis.readInt();
			initialKey = dis.readInt();
			sectionOffset = dis.readInt();
			sectionLength = dis.readInt();
			lines = new String[lineCount];
			int[] offsets = new int[lineCount]; //relative to section start
			int[] lengths = new int[lineCount]; //not byte lengths - need to multiply by 2 to get actual lengths in file (this represents the length in shorts/u16s)
			for (int i = 0; i < lineCount; i++) {
				offsets[i] = dis.readInt();
				lengths[i] = dis.readShort();
				dis.skip(2);
			}
			//we don't need offsets for reading, similar to collisions. just loop through the lines according to the lengths
			short basekey = 0x7C89;
			int value;
			for (int i = 0; i < lineCount; i++) {
				key = Short.toUnsignedInt(basekey);
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < lengths[i]; j++) {
					value = getNextValue(dis);
					if (value == 0) {
						break;
					} else if (value == '\n') {
						sb.append("\\n"); //need to escape the endline to show it
					} else if (value == 0x10) { //actions and variables
						int len = getNextValue(dis);
						int type = getNextValue(dis);
						switch (type) {
							case 0xBE00:
								sb.append("\\r"); //wait for button to scroll
								break;
							case 0xBE01:
								sb.append("\\c"); //wait for button then advance clear the textbox
								break;
							case 0xBE02:
								sb.append("[WAIT ").append(getNextValue(dis)).append("]");
								break;
							case 0xBDFF: //blank line
								sb.append("[NO DATA - ").append(getNextValue(dis)).append("]");
								break;
							default:
								sb.append("[VAR ");
								sb.append(getVarName(type));
								if (len > 1){ //variable has arguments
									sb.append("(");
									for (int k = 1; k < len; k++){
										sb.append(getNextValue(dis));
										if (k + 1 != len) sb.append(",");
									}
									sb.append(")");
								}
								sb.append("]");
								break;
						}
					}
					else {
						switch (value) {
							case 0xE07F:
								sb.append((char)0x202F); // nbsp
								break;
							case 0xE08D:
								sb.append((char)0x2026); // …
								break;
							case 0xE08E:
								sb.append((char)0x2642); // ♂
								break;
							case 0xE08F:
								sb.append((0x2640)); // ♀
								break;
							default:
								sb.append((char)value);
								break;
						}
                    }
				}
				lines[i] = sb.toString();
				basekey += 0x2983;
				if (i != lineCount - 1){
					dis.skip(offsets[i + 1] - (offsets[i] + lengths[i] * 2)); //skip padding if present. cannot be done on last entry because we get the length of the padding with the next offset
				}
			}
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(TextFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public String getLine(int num){
		if (num >= lines.length) return null;
		return lines[num];
	}
	
	public static void main(String[] args){
		TextFile f = new TextFile(new File("072.bin"));
		for (int i = 0; i < f.lineCount; i++){
			System.out.println(f.getLine(i));
		}
	}
	
	public final int getNextValue(LittleEndianDataInputStream dis) throws IOException {
		int input = Short.toUnsignedInt(dis.readShort());
		int ret = input ^ key;
		key = (((key << 3) | (key >> 13)) & 0xffff);
		return ret;
	}

	public final String getVarName(int varCode) { //from XYTEXT by Kaphotics
		String ret;
		switch (varCode) // get variable's info name
		{
			case 0xFF00:
				ret = "SETCOLOR";
				break; // Change text line color (0 = white, 1 = red, 2 = blue...)
			case 0x0100:
				ret = "TRNAME";
				break; // 
			case 0x0101:
				ret = "PKSPECIES";
				break;
			case 0x0102:
				ret = "PKNICK";
				break;
			case 0x0103:
				ret = "TYPE";
				break;
			case 0x0105:
				ret = "LOCATION";
				break;
			case 0x0106:
				ret = "ABILITY";
				break;
			case 0x0107:
				ret = "MOVE";
				break;
			case 0x0108:
				ret = "ITEM1";
				break;
			case 0x0109:
				ret = "ITEM2";
				break;
			case 0x010A:
				ret = "sTRBAG";
				break;
			case 0x010B:
				ret = "BOX";
				break;
			case 0x010D:
				ret = "EVSTAT";
				break;
			case 0x0110:
				ret = "OPOWER";
				break;
			case 0x0127:
				ret = "RIBBON";
				break;
			case 0x0134:
				ret = "MIINAME";
				break;
			case 0x013E:
				ret = "WEATHER";
				break;
			case 0x0189:
				ret = "TRNICK";
				break;
			case 0x018A:
				ret = "TRNICK-INITIAL";
				break;
			case 0x018B:
				ret = "SHOUTOUT";
				break;
			case 0x018E:
				ret = "BERRY";
				break;
			case 0x018F:
				ret = "REMFEEL";
				break;
			case 0x0190:
				ret = "REMQUAL";
				break;
			case 0x0191:
				ret = "WEBSITE";
				break;
			case 0x019C:
				ret = "CHOICECOS";
				break;
			case 0x01A1:
				ret = "GSYNCID";
				break;
			case 0x0192:
				ret = "PRVIDSAY";
				break;
			case 0x0193:
				ret = "BTLTEST";
				break;
			case 0x0195:
				ret = "GENLOC";
				break;
			case 0x0199:
				ret = "CHOICEFOOD";
				break;
			case 0x019A:
				ret = "HOTELITEM";
				break;
			case 0x019B:
				ret = "TAXISTOP";
				break;
			case 0x019F:
				ret = "MAISONTITLE";
				break;
			case 0x1000:
				ret = "ITEMPLUR0";
				break;
			case 0x1001:
				ret = "ITEMPLUR1";
				break;
			case 0x1100:
				ret = "GENDBR";
				break;
			case 0x1101:
				ret = "NUMBRNCH";
				break;
			case 0x1302:
				ret = "iCOLOR2";
				break;
			case 0x1303:
				ret = "iCOLOR3";
				break;
			case 0x0200:
				ret = "NUM1";
				break;
			case 0x0201:
				ret = "NUM2";
				break;
			case 0x0202:
				ret = "NUM3";
				break;
			case 0x0203:
				ret = "NUM4";
				break;
			case 0x0204:
				ret = "NUM5";
				break;
			case 0x0205:
				ret = "NUM6";
				break;
			case 0x0206:
				ret = "NUM7";
				break;
			case 0x0207:
				ret = "NUM8";
				break;
			case 0x0208:
				ret = "NUM9";
				break;
			default:
				ret = Integer.toHexString(varCode);
				break;
		}
		return ret;
	}
}
