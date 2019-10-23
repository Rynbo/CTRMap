package ctrmap.formats.scripts;

import ctrmap.LittleEndianDataInputStream;
import ctrmap.LittleEndianDataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GFLPawnScript {

	public byte[] compCode;
	public int[] decInstructions;
	private boolean decompressed;

	public List<PawnPrefixEntry> publics = new ArrayList<>();
	public List<PawnPrefixEntry> natives = new ArrayList<>();
	public List<PawnPrefixEntry> libraries = new ArrayList<>();
	public List<PawnPrefixEntry> publicVars = new ArrayList<>();
	public List<PawnPrefixEntry> tags = new ArrayList<>();
	public List<PawnPrefixEntry> names = new ArrayList<>();
	public List<PawnPrefixEntry> unknowns = new ArrayList<>();

	public List<PawnInstruction> instructions = new ArrayList<>();
	public List<PawnInstruction> data = new ArrayList<>();

	public PawnInstruction mainEntryPointDummy;

	public int len;
	public int magic;
	public int ver;
	public int minCompatVer;
	public int flags;
	public int defsize;
	public int instructionStart;
	public int dataStart;
	public int heapStart;
	public int mainEntryPoint;

	public int publicsOffset;
	public int nativesOffset;
	public int librariesOffset;
	public int publicVarsOffset;
	public int tagsOffset;
	public int namesOffset;
	public int overlaysOffset;

	public int allocatedMem;

	public int compCodeLen;
	public int decCodeLen;

	public byte[] rest;

	public GFLPawnScript(byte[] b) {
		this(new LittleEndianDataInputStream(new ByteArrayInputStream(b)));
	}

	public GFLPawnScript(LittleEndianDataInputStream in) {
		try {
			len = in.readInt();
			magic = in.readShort(); //F1 E0 == 32-bit cell
			ver = in.read();
			minCompatVer = in.read();
			flags = in.readUnsignedShort();
			defsize = in.readUnsignedShort();
			instructionStart = in.readInt();
			dataStart = in.readInt();
			heapStart = in.readInt();
			allocatedMem = in.readInt();
			mainEntryPoint = in.readInt();

			publicsOffset = in.readInt();
			nativesOffset = in.readInt();
			librariesOffset = in.readInt();
			publicVarsOffset = in.readInt();
			tagsOffset = in.readInt();
			namesOffset = in.readInt();
			overlaysOffset = in.readInt();

			readEntries(publics, PawnPrefixEntry.Type.PUBLIC, defsize, in, (nativesOffset - publicsOffset) / defsize);
			readEntries(natives, PawnPrefixEntry.Type.NATIVE, defsize, in, (librariesOffset - nativesOffset) / defsize);
			readEntries(libraries, PawnPrefixEntry.Type.LIBRARY, defsize, in, (publicVarsOffset - librariesOffset) / defsize);
			readEntries(publicVars, PawnPrefixEntry.Type.PUBLIC_VAR, defsize, in, (tagsOffset - publicVarsOffset) / defsize);
			readEntries(tags, PawnPrefixEntry.Type.TAG, defsize, in, (namesOffset - tagsOffset) / defsize);
			readEntries(names, PawnPrefixEntry.Type.NAME, defsize, in, (overlaysOffset - namesOffset) / defsize);
			//readEntries(unknowns, PawnPrefixEntry.Type.UNKNOWN, defsize, in, (instructionStart - unknownOffset) / defsize);

			compCodeLen = len - instructionStart;
			rest = new byte[instructionStart - overlaysOffset];
			in.read(rest);
			decCodeLen = heapStart - instructionStart;
			compCode = new byte[compCodeLen];
			in.read(compCode);
		} catch (IOException ex) {
			Logger.getLogger(GFLPawnScript.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write(LittleEndianDataOutputStream dos) throws IOException {
		byte[] instructionsToWrite;
		if (decompressed) {
			nativesOffset = publicsOffset + publics.size() * defsize;
			librariesOffset = nativesOffset + natives.size() * defsize;
			publicVarsOffset = librariesOffset + libraries.size() * defsize;
			tagsOffset = publicVarsOffset + publicVars.size() * defsize;
			namesOffset = tagsOffset + tags.size() * defsize;
			overlaysOffset = namesOffset + names.size() * defsize;

			updateRaw();
			instructionStart = overlaysOffset + rest.length;
			heapStart = instructionStart + decInstructions.length * 4;
			dataStart = heapStart - data.size() * 4;
			mainEntryPoint = mainEntryPointDummy.argumentCells[0];
			instructionsToWrite = compressScript(decInstructions);
			len = instructionStart + instructionsToWrite.length;
		} else {
			instructionsToWrite = compCode;
		}
		dos.writeInt(len);
		dos.writeShort((short) magic);
		dos.write(ver);
		dos.write(minCompatVer);
		dos.writeShort((short) flags);
		dos.writeShort((short) defsize);
		dos.writeInt(instructionStart);
		dos.writeInt(dataStart);
		dos.writeInt(heapStart);
		dos.writeInt(allocatedMem);
		dos.writeInt(mainEntryPoint);
		dos.writeInt(publicsOffset);
		dos.writeInt(nativesOffset);
		dos.writeInt(librariesOffset);
		dos.writeInt(publicVarsOffset);
		dos.writeInt(tagsOffset);
		dos.writeInt(namesOffset);
		dos.writeInt(overlaysOffset);

		writeEntries(publics, dos);
		writeEntries(natives, dos);
		writeEntries(libraries, dos);
		writeEntries(publicVars, dos);
		writeEntries(tags, dos);
		writeEntries(names, dos);
		//writeEntries(unknowns, dos);

		dos.write(rest);
		dos.write(instructionsToWrite);
		while (dos.size() % 4 != 0) { //padding to 4 bytes
			dos.write(0);
		}
	}

	public byte[] getScriptBytes() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			write(new LittleEndianDataOutputStream(baos));
			return baos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(GFLPawnScript.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public void decompressThis() {
		if (!decompressed) {
			decInstructions = quickDecompress(compCode, decCodeLen / 4);
			decompressed = true;
			instructions.clear();
			data.clear();
			for (int i = 0; i < (dataStart - instructionStart) / 4; i++) {
				PawnInstruction ins = new PawnInstruction(i * 4, decInstructions, this);
				instructions.add(ins);
				if (!ins.hasCompressedArgument) {
					i += ins.argumentCount;
				}
			}
			for (int j = dataStart - instructionStart; j < decInstructions.length * 4; j += 4) {
				String parse = Integer.toHexString(decInstructions[j / 4]);
				PawnInstruction moveDummy = new PawnInstruction(j, decInstructions[j / 4], parse);
				moveDummy.hasCompressedArgument = false;
				moveDummy.argumentCount = 0;
				data.add(moveDummy);
			}
			mainEntryPointDummy = new PawnInstruction(0, 0x81, "MAIN ENTRYPOINT");
			mainEntryPointDummy.argumentCount = 1;
			mainEntryPointDummy.argumentCells = new int[]{mainEntryPoint};
			mainEntryPointDummy.setParent(this);
		}
	}

	private void readEntries(List<PawnPrefixEntry> target, PawnPrefixEntry.Type typeForAll, int defsize, LittleEndianDataInputStream in, int count) throws IOException {
		for (int i = 0; i < count; i++) {
			target.add(new PawnPrefixEntry(defsize, typeForAll, in));
		}
	}

	private void writeEntries(List<PawnPrefixEntry> list, LittleEndianDataOutputStream target) throws IOException {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).write(target);
		}
	}

	public void updateRaw() {
		int[] codeIns = PawnDisassembler.getRawInstructions(instructions);
		int[] dataIns = PawnDisassembler.getRawInstructions(data);
		int movementLength = dataIns.length * 4;
		dataStart = heapStart - movementLength;
		decInstructions = new int[codeIns.length + dataIns.length];
		System.arraycopy(codeIns, 0, decInstructions, 0, codeIns.length);
		System.arraycopy(dataIns, 0, decInstructions, codeIns.length, dataIns.length);
	}

	public void replaceInstructions(int[] newIns) {
		int[] compiledMovement = PawnDisassembler.getRawInstructions(data);
		int[] target = new int[newIns.length + compiledMovement.length];
		System.arraycopy(newIns, 0, target, 0, newIns.length);
		System.arraycopy(compiledMovement, 0, target, newIns.length, compiledMovement.length);
		decInstructions = target;
	}

	public PawnInstruction lookupInstructionByPtr(int ptr) {
		for (int i = 0; i < instructions.size(); i++) {
			if (instructions.get(i).pointer == ptr) {
				return instructions.get(i);
			}
		}
		return null;
	}

	public void setInstructionListeners() {
		for (PawnInstruction i : instructions) {
			i.setParent(this);
		}
		mainEntryPointDummy.setParent(this);
	}

	public void callInstructionListeners() {
		for (int i = 0; i < instructions.size(); i++) {
			instructions.get(i).callJumpListeners();
			instructions.get(i).updateDisassembly();
		}
		mainEntryPointDummy.callJumpListeners();
	}

	public static int[] quickDecompress(byte[] data, int count) {
		int[] code = new int[count];
		int i = 0, j = 0, x = 0, f = 0;
		while (i < code.length) {
			int b = data[f++],
					v = b & 0x7F;
			if (++j == 1) // sign extension possible
			{
				x = (int) ((((v >> 6 == 0 ? 1 : 0) - 1) << 6) | v); // only for bit6 being set
			} else {
				x = (x << 7) | (byte) v; // shift data into place
			}
			if ((b & 0x80) != 0) {
				continue; // more data to read
			}
			code[i++] = x;
			j = 0; // write finalized instruction
		}
		return code;
	}

	public static byte[] compressScript(int[] cmd) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			for (int pos = 0; pos < cmd.length; pos++) {
				out.write(compressInstruction(cmd[pos]));
				out.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(GFLPawnScript.class.getName()).log(Level.SEVERE, null, ex);
		}
		return out.toByteArray();
	}

	private static byte[] compressInstruction(int instruction) {
		List<Byte> bytes = new ArrayList<>();
		boolean sign = (instruction & 0x80000000) != 0;

		// Signed (negative) values are handled opposite of unsigned (positive) values.
		// Positive values are "done" when we've shifted the value down to zero, but
		// we don't need to store the highest 1s in a signed value. We handle this by
		// tracking the loop via a NOTed shadow copy of the instruction if it's signed.
		int shadow = sign ? ~instruction : instruction;

		do {
			int least7 = instruction & 0b01111111;
			byte byteVal = (byte) least7;

			if (!bytes.isEmpty()) // Continuation bit on all but the lowest byte
			{
				byteVal |= 0x80;
			}

			bytes.add(byteVal);

			instruction >>= 7;
			shadow >>= 7;
		} while (shadow != 0);

		if (bytes.size() < 5) {
			// Ensure "sign bit" (bit just to the right of highest continuation bit) is
			// correct. Add an extra empty continuation byte if we need to. Values can't
			// be longer than 5 bytes, though.

			int signBit = sign ? 0x40 : 0x00;
			if ((bytes.get(bytes.size() - 1) & 0x40) != signBit) {
				bytes.add((byte) (sign ? 0xFF : 0x80));
			}
		}

		Collections.reverse(bytes);

		byte[] out = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			out[i] = bytes.get(i);
		}

		return out;
	}
}
