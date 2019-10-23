package ctrmap.formats.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PawnInstruction {

	public int pointer;
	public int cellValue;
	public int argumentCount = 0;
	public boolean hasCompressedArgument;
	public int[] argumentCells;

	public String stringValue;
	public int line;

	public List<JumpListener> jmpListeners = new ArrayList<>();

	public static List<Commands> cmdList = Arrays.asList(Commands.values());

	public GFLPawnScript parent;

	public PawnInstruction(int ptr, int[] allCommands, GFLPawnScript parent) {
		this.parent = parent;
		pointer = ptr;
		cellValue = allCommands[ptr / 4];
		hasCompressedArgument = getHasCompressedArg(cellValue & 0x7FFF);
		argumentCells = getArguments(this, allCommands, false);
		argumentCount = argumentCells.length;

		stringValue = getDisassembly(this);
	}

	public PawnInstruction(int ptr, int cmd, String blankInstructionDummy) {
		pointer = ptr;
		cellValue = cmd;
		hasCompressedArgument = getHasCompressedArg(cmd);
		stringValue = blankInstructionDummy;
		argumentCells = getArguments(this, null, true);
		argumentCount = argumentCells.length;
	}

	public static PawnInstruction fromString(int ptr, String instruction) {
		return fromString(ptr, instruction, true);
	}

	public static PawnInstruction fromString(int ptr, String instruction, boolean doOutput) {
		PawnInstruction ret = null;
		int lastJunk = instruction.lastIndexOf(":");
		int idx = lastJunk + 1;
		while (idx < instruction.length() && instruction.charAt(idx) == ' ') {
			idx++;
		}
		char character;
		StringBuilder output = new StringBuilder();
		while (idx < instruction.length() && (character = instruction.charAt(idx)) != '(') {
			idx++;
			if (character == ' ') {
				break;
			} else {
				output.append(character);
			}
		}
		String finalCmd = output.toString().toUpperCase().replaceAll("_", "");
		for (int i = 0; i < cmdList.size(); i++) {
			if (cmdList.get(i).toString().replaceAll("_", "").equals(finalCmd)) {
				ret = new PawnInstruction(ptr, i, instruction);
				break;
			}
		}

		if (ret != null && ret.getCommand() == 0x82) { //CASETBL
			return ret; //the subroutine disassembler will handle the rest
		}

		if (ret == null || !(instruction.contains("=>") || instruction.contains("("))) {
			//syntax error or invalid instruction
			return new PawnInstruction(ptr, -1, instruction);
		}

		while (idx < instruction.length() && instruction.charAt(idx) == ' ') {
			idx++;
		}
		if (instruction.substring(idx).startsWith("=>")) {
			idx += 2;
			while (idx < instruction.length() && instruction.charAt(idx) == ' ') {
				idx++;
			}
			StringBuilder jmpBldr = new StringBuilder();
			while (idx < instruction.length() && (character = instruction.charAt(idx)) != ' ') {
				jmpBldr.append(character);
				idx++;
			}
			if (PawnInstruction.checkJmp(ret) && ret.argumentCells.length == 1 && jmpBldr.toString().length() > 0) {
				try {
					ret.argumentCells[0] = Integer.parseInt(jmpBldr.toString().replaceAll("0x", ""), 16);
				} catch (NumberFormatException ex) {
					if (doOutput) {
						System.err.println("[ERR] 0x" + Integer.toHexString(ptr).toUpperCase() + " : " + "Argument is not a number.");
					}
				}
			} else if (doOutput) {
				System.err.println("[ERR] 0x" + Integer.toHexString(ptr).toUpperCase() + " : " + "Jump instruction syntax on incompatible instruction.");
			}
		} else if (instruction.substring(idx).startsWith("(")) {
			String allArgs = instruction.substring(idx + 1).replaceAll(" ", "").replace(")", "");
			String[] argsUnparsed = allArgs.split(",");
			if (argsUnparsed.length == ret.argumentCount) {
				for (int i = 0; i < argsUnparsed.length; i++) {
					try {
						if (PawnInstruction.checkFlt(ret)) {
							ret.argumentCells[i] = Float.floatToIntBits(Float.parseFloat(argsUnparsed[i].replaceAll("f", "")));
						} else {
							ret.argumentCells[i] = Integer.parseInt(argsUnparsed[i]);
						}
					} catch (NumberFormatException e) {
						if (doOutput) {
							System.err.println("[ERR] 0x" + Integer.toHexString(ptr).toUpperCase() + " : " + "Argument " + i + " is not a number.");
						}
					}
				}
			} else if (doOutput) {
				System.err.println("[ERR] 0x" + Integer.toHexString(ptr).toUpperCase() + " : " + ret.stringValue + " : " + "Source argument count doesn't match its command.");
			}
		} else if (doOutput) {
			System.err.println("[ERR] 0x" + Integer.toHexString(ptr).toUpperCase() + " : " + ret.stringValue + " : " + "Expected arguments but none found.");
		}

		return ret;
	}

	public void updateDisassembly() {
		stringValue = getDisassembly(this);
	}

	public int getCommand() {
		return cellValue & 0x7FFF;
	}

	public int[] getRaw() {
		int cmd = cellValue;
		if (hasCompressedArgument) {
			cmd &= 0x7FFF;
			cmd |= argumentCells[0] << 16;
			return new int[]{cmd};
		} else {
			int[] out = new int[argumentCount + 1];
			out[0] = cmd;
			System.arraycopy(argumentCells, 0, out, 1, argumentCount);
			return out;
		}
	}

	public void chkAddJumpListener() {
		jmpListeners.clear();
		if (checkJmp(this) && parent != null) {
			JumpListener jl = new JumpListener(this);
			jl.setParent(parent.lookupInstructionByPtr(argumentCells[0] + pointer));
			jmpListeners.add(jl);
		}
		if (getCommand() == 0x82 && parent != null) {
			CaseListener cl = new CaseListener(this, parent);
			jmpListeners.add(cl);
		}
	}

	public void setParent(GFLPawnScript parent) {
		this.parent = parent;
		jmpListeners.clear();
		chkAddJumpListener();
	}

	public void callJumpListeners() {
		for (int i = 0; i < jmpListeners.size(); i++) {
			jmpListeners.get(i).onAddressChange();
		}
	}

	public boolean setArgsFromString(String str) {
		if (!checkJmp(this)) {
			String allArgs = str.replace("(", "").replace(")", "").replaceAll(" ", "");
			String[] argsUnparsed = allArgs.split(",");
			if (argsUnparsed.length == argumentCount) {
				for (int i = 0; i < argsUnparsed.length; i++) {
					try {
						if (PawnInstruction.checkFlt(this)) {
							argumentCells[i] = Float.floatToIntBits(Float.parseFloat(argsUnparsed[i].replaceAll("f", "")));
						} else {
							argumentCells[i] = Integer.parseInt(argsUnparsed[i]);
						}
					} catch (NumberFormatException e) {
						return false;
					}
				}
			} else {
				return false;
			}
		} else {
			String jumpOnly = str.replaceAll("=>", "").replaceAll(" ", "").replace("0x", "").replace("\n", "");
			try {
				argumentCells[0] = Integer.parseInt(jumpOnly, 16) - pointer;
				jmpListeners.clear();
				chkAddJumpListener();
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	public static boolean getHasCompressedArg(int command) {
		switch (command) {
			case 0x09:
			case 0x17:
			case 0x19:
			case 0x1B:
			case 0x21:
			case 0x22:
			case 0x23:
			case 0x24:
			case 0x25:
			case 0x2A:
			case 0x2B:
			case 0x2E: // Begin
			case 0x2F:
			case 0x30: // Return
			case 0x41:
			case 0x42:
			case 0x43:
			case 0x48:
			case 0x49:
			case 0x4A:
			case 0x4B:
			case 0x4C:
			case 0x4D:
			case 0x4E: // Add?
			case 0x4F:
			case 0x50:
			case 0x51: // Cmp?
			case 0x52:
			case 0x53:
			case 0x54:
			case 0x55:
			case 0x56:
			case 0x59: // ClearAll
			case 0x5A:
			case 0x5D:
			case 0x5E:
			case 0x5F:
			case 0x60:
			case 0x61:
			case 0x62:
			case 0x63:
			case 0x64:
			case 0x65:
			case 0x66:
			case 0x67:
			case 0x68:
			case 0x6B:
			case 0x6C:
			case 0x6F:
			case 0x70:
			case 0x71:
			case 0x74:
			case 0x7A:
			case 0x83:
			case 0x84:
			case 0x86:
			case 0x89: // LineNo?
			case 0xAA:
			case 0xAB: // PushConst2
			case 0xAC: // CmpConst2
			case 0xAD:
			case 0xAE:
			case 0xB7:
			case 0xB8:
			case 0xB9:
			case 0xBA:
			case 0xBB:
			case 0xBC: // PushConst
			case 0xBD:
			case 0xBE:
			case 0xBF: // AdjustStack
			case 0xC0:
			case 0xC1:
			case 0xC2:
			case 0xC3:
			case 0xC4:
			case 0xC5:
			case 0xC6:
			case 0xC7:
			case 0xC8:
			case 0xC9:
			case 0xCA:
			case 0xCF:
			case 0xD0:
			case 0xD1:
			case 0xD2:
			case 0xD3:
			case 0xD4:
			case 0xA1:
			case 0xA2:
			case 0xA3:
			case 0xA6:
			case 0xA7:
			case 0xAF:
			case 0xB0:
			case 0xB3:
			case 0xB4:
			case 0xCB:
			case 0xCD:
			case 0xA4:
			case 0xA5:
			case 0xA8:
			case 0xA9:
			case 0xB1:
			case 0xB2:
			case 0xB5:
			case 0xB6:
			case 0xCC:
			case 0xCE:
				return true;
			default:
				return false;
		}
	}

	public void checkJmpConvertArgs() {
		if (checkJmp(this)) {
			argumentCells[0] = argumentCells[0] - pointer;
		}
	}

	public static boolean checkJmp(PawnInstruction chk) {
		switch (chk.getCommand()) {
			case 0x31: // CallFunc
			case 0x33:
			case 0x35: // Jump!=
			case 0x36: // Jump==
			case 0x37:
			case 0x38:
			case 0x39:
			case 0x3A:
			case 0x3B:
			case 0x3C:
			case 0x3D:
			case 0x3E:
			case 0x3F:
			case 0x40:
			case 0x81: // Jump
				return true;
		}
		return false;
	}

	public static boolean checkFlt(PawnInstruction chk) {
		switch (chk.getCommand()) {
			case 0x8A:
			case 0x8E:
			case 0x8F:
			case 0x90:
			case 0x91:
			case 0x96: // float
			case 0x97:
			case 0x98:
			case 0x99:
				return true;
		}
		return false;
	}

	public static int[] getArguments(PawnInstruction ins, int[] all, boolean getBlank) {
		int next;
		switch (ins.getCommand()) {
			case 0x01:
			case 0x02:
			case 0x05:
			case 0x06:
			case 0x0F:
			case 0x10:
			case 0x13:
			case 0x14:
			case 0x6D:
			case 0x72:
			case 0x03:
			case 0x04:
			case 0x07:
			case 0x08:
			case 0x11:
			case 0x12:
			case 0x15:
			case 0x16:
			case 0x6E:
			case 0x9E:
			case 0x73:
			case 0x0A:
			case 0x0B:
			case 0x0C:
			case 0x0D:
			case 0x0E:
			case 0x18:
			case 0x1A:
			case 0x1C:
			case 0x1D:
			case 0x1E:
			case 0x1F:
			case 0x20:
			case 0x26:
			case 0x27: // PushConst
			case 0x28:
			case 0x29:
			case 0x2C:
			case 0x2D:
			case 0x34:
			case 0x44:
			case 0x45:
			case 0x46:
			case 0x47:
			case 0x57:
			case 0x58:
			case 0x5B:
			case 0x5C:
			case 0x69:
			case 0x6A:
			case 0x75:
			case 0x76:
			case 0x77:
			case 0x78:
			case 0x79:
			case 0x85:
			case 0x31: // CallFunc
			case 0x33:
			case 0x35: // Jump!=
			case 0x36: // Jump==
			case 0x37:
			case 0x38:
			case 0x39:
			case 0x3A:
			case 0x3B:
			case 0x3C:
			case 0x3D:
			case 0x3E:
			case 0x3F:
			case 0x40:
			case 0x81:
			case 0x7B: {
				next = 1;
				break;
			}
			case 0x82: // JumpIfElse
			{
				if (!getBlank) {
					next = all[ins.pointer / 4 + 1];
					int[] argumentCells = new int[next * 2 + 2];
					argumentCells[0] = next;
					System.arraycopy(all, ins.pointer / 4 + 2, argumentCells, 1, next * 2 + 1);
					return argumentCells;
				}
			}
			case 0x87:
			case 0x8B:
			case 0x8C:
			case 0x8D:
			case 0x9A:
			case 0x9B:
			case 0x9C:
			case 0x9D:
			case 0x8A: {
				next = 2;
				break;
			}
			case 0x8E:
			case 0x8F:
			case 0x90:
			case 0x91: {
				next = 3;
				break;
			}

			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95: {
				next = 4;
				break;
			}

			case 0x96: // float
			case 0x97:
			case 0x98:
			case 0x99: {
				next = 5;
				break;
			}
			default:
				next = 0;
				break;
		}
		if (!getBlank) {
			if (ins.hasCompressedArgument) {
				if (ins.getCommand() != 0xA1) {
					return new int[]{ins.cellValue >> 16};
				} else {
					return new int[]{ins.pointer / 4 + (int) (1 + (2 * (ins.cellValue / 4)) + 1)};
				}
			}
			int[] argumentCells = new int[next];
			System.arraycopy(all, ins.pointer / 4 + 1, argumentCells, 0, next);
			return argumentCells;
		} else {
			if (ins.hasCompressedArgument) {
				return new int[1];
			} else {
				return new int[next];
			}
		}
	}

	public static String getDisassembly(PawnInstruction ins) {
		int c = ins.cellValue;
		String op;
		switch (c & 0x7FFF) {
			default:
				System.out.println("Invalid Command ID " + (c & 0x7FFF));
			case 0x01:
			case 0x02:
			case 0x05:
			case 0x06:
			case 0x0F:
			case 0x10:
			case 0x13:
			case 0x14:
			case 0x6D:
			case 0x72: {
				op = eA(ins);
				break;
			}
			case 0x03:
			case 0x04:
			case 0x07:
			case 0x08:
			case 0x11:
			case 0x12:
			case 0x15:
			case 0x16:
			case 0x6E:
			case 0x73: {
				op = eA(ins);
				break;
			}
			case 0x09:
			case 0x17:
			case 0x19:
			case 0x1B:
			case 0x21:
			case 0x22:
			case 0x23:
			case 0x24:
			case 0x25:
			case 0x2A:
			case 0x2B:
			case 0x2E: // Begin
			case 0x2F:
			case 0x30: // Return
			case 0x41:
			case 0x42:
			case 0x43:
			case 0x48:
			case 0x49:
			case 0x4A:
			case 0x4B:
			case 0x4C:
			case 0x4D:
			case 0x4E:
			case 0x4F:
			case 0x50:
			case 0x51:
			case 0x52:
			case 0x53:
			case 0x54:
			case 0x55:
			case 0x56:
			case 0x59:
			case 0x5A:
			case 0x5D:
			case 0x5E:
			case 0x5F:
			case 0x60:
			case 0x61:
			case 0x62:
			case 0x63:
			case 0x64:
			case 0x65:
			case 0x66:
			case 0x67:
			case 0x68:
			case 0x6B:
			case 0x6C:
			case 0x6F:
			case 0x70:
			case 0x71:
			case 0x74:
			case 0x7A:
			case 0x83:
			case 0x84:
			case 0x86:
			case 0x89:
			case 0xAA:
			case 0xAB:
			case 0xAC:
			case 0xAD:
			case 0xAE:
			case 0xB7:
			case 0xB8:
			case 0xB9:
			case 0xBA:
			case 0xBB:
			case 0xBC: // PushConst
			case 0xBD:
			case 0xBE:
			case 0xBF:
			case 0xC0:
			case 0xC1:
			case 0xC2:
			case 0xC3:
			case 0xC4:
			case 0xC5:
			case 0xC6:
			case 0xC7:
			case 0xC8:
			case 0xC9:
			case 0xCA:
			case 0xCF:
			case 0xD0:
			case 0xD1:
			case 0xD2:
			case 0xD3:
			case 0xD4: {
				op = eA(c & 0xFF, ins);
				break;
			}
			case 0x0A:
			case 0x0B:
			case 0x0C:
			case 0x0D:
			case 0x0E:
			case 0x18:
			case 0x1A:
			case 0x1C:
			case 0x1D:
			case 0x1E:
			case 0x1F:
			case 0x20:
			case 0x26:
			case 0x27: // PushConst
			case 0x28:
			case 0x29:
			case 0x2C:
			case 0x2D:
			case 0x34:
			case 0x44:
			case 0x45:
			case 0x46:
			case 0x47:
			case 0x57:
			case 0x58:
			case 0x5B:
			case 0x5C:
			case 0x69:
			case 0x6A:
			case 0x75:
			case 0x76:
			case 0x77:
			case 0x78:
			case 0x79:
			case 0x85: {
				op = eA(ins);
				break;
			}

			case 0x31: // CallFunc
			case 0x33:
			case 0x35: // Jump!=
			case 0x36: // Jump==
			case 0x37:
			case 0x38:
			case 0x39:
			case 0x3A:
			case 0x3B:
			case 0x3C:
			case 0x3D:
			case 0x3E:
			case 0x3F:
			case 0x40:
			case 0x81: // Jump
			{
				int newOfs = ins.pointer + ins.argumentCells[0];
				op = Commands.values()[c].toString() + " => 0x" + Integer.toHexString(newOfs);
				break;
			}
			case 0x7B: {
				op = eA(c, ins);
				break;
			}
			case 0x82: // JumpIfElse
			{
				List<String> tree = new ArrayList<>();

				{
					int jmp = (int) ins.argumentCells[1];
					int toOffset = ins.pointer + 4 + jmp;
					tree.add("\t" + "*" + " => 0x" + Integer.toHexString(toOffset));
				}
				for (int j = 2; j < ins.argumentCells.length; j += 2) {
					int jmp = (int) ins.argumentCells[j + 1];
					int toOffset = (ins.pointer + j * 4) + jmp + 4;
					int ifValue = (int) ins.argumentCells[j];
					tree.add("\t" + ifValue + " => 0x" + Integer.toHexString(toOffset));
				}

				op = Commands.values()[c].toString() + "\n{\n" + String.join("\n", tree) + "\n}";
				break;
			}
			case 0x87: {
				op = eA(ins);
				break;
			}

			case 0x8B:
			case 0x8C:
			case 0x8D:
			case 0x9C:
			case 0x9D: {
				op = eA(ins);
				break;
			}
			case 0x8A: {
				op = eF(ins);
				break;
			}
			case 0x8E:
			case 0x8F:
			case 0x90:
			case 0x91: {
				op = eF(ins);
				break;
			}

			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95: {
				op = eA(ins);
				break;
			}

			case 0x96: // float
			case 0x97:
			case 0x98:
			case 0x99: {
				op = eF(ins);
				break;
			}

			case 0x9A:
			case 0x9B: // Copy
			{
				op = eA(ins);
				break;
			}
			case 0x9E: {
				op = eA(ins);
				break;
			}
			case 0x9F: {
				op = eA(ins);
				break;
			}

			case 0xA1: // Goto
			{
				op = eA(ins);
				break;
			}

			case 0xA2:
			case 0xA3:
			case 0xA6:
			case 0xA7:
			case 0xAF:
			case 0xB0:
			case 0xB3:
			case 0xB4:
			case 0xCB:
			case 0xCD: {
				op = eA(c & 0xFF, ins);
				break;
			}
			case 0xA4:
			case 0xA5:
			case 0xA8:
			case 0xA9:
			case 0xB1:
			case 0xB2:
			case 0xB5:
			case 0xB6:
			case 0xCC:
			case 0xCE: {
				op = eA(c & 0xFF, ins);
				break;
			}
		}
		return op;
	}

	private static String eA(PawnInstruction ins) {
		return eA(ins.getCommand(), ins);
	}

	private static String eA(int customCommand, PawnInstruction ins) {
		String cmd = Commands.values()[customCommand].toString();
		StringBuilder params = new StringBuilder();
		params.append("(");
		for (int i = 0; i < ins.argumentCount; i++) {
			params.append(ins.argumentCells[i]).append((i != ins.argumentCount - 1) ? "," : "");
		}
		params.append(")");
		return cmd + params.toString();
	}

	private static String eF(PawnInstruction ins) {
		String cmd = Commands.values()[ins.getCommand()].toString();
		StringBuilder params = new StringBuilder();
		params.append("(");
		for (int i = 0; i < ins.argumentCount; i++) {
			params.append(Float.intBitsToFloat(ins.argumentCells[i])).append("f").append((i != ins.argumentCount - 1) ? "," : "");
		}
		params.append(")");
		return cmd + params.toString();
	}

	public static class JumpListener {

		private final PawnInstruction src;
		private PawnInstruction target;

		public JumpListener(PawnInstruction jumpSource) {
			src = jumpSource;
		}

		public void setParent(PawnInstruction target) {
			this.target = target;
		}

		public void onAddressChange() {
			try {
				src.argumentCells[0] = target.pointer - src.pointer;
			} catch (NullPointerException e) {
				src.argumentCells[0] = -src.pointer;
			}
		}
	}

	public static class CaseListener extends JumpListener {

		private final PawnInstruction src;
		private Map<Integer, PawnInstruction> targets = new HashMap<>();
		private PawnInstruction defaultTarget;

		private GFLPawnScript instlib;

		public CaseListener(PawnInstruction jumpSource, GFLPawnScript instlib) {
			super(jumpSource);
			this.instlib = instlib;
			src = jumpSource;
			for (int i = 2; i < src.argumentCells.length; i += 2) {
				int ptr = (src.pointer + i * 4) + src.argumentCells[i + 1] + 4;
				targets.put(src.argumentCells[i], instlib.lookupInstructionByPtr(ptr));
			}
			defaultTarget = instlib.lookupInstructionByPtr((src.pointer + 4) + src.argumentCells[1]);
		}

		@Override
		public void onAddressChange() {
			for (int i = 2; i < src.argumentCells.length; i += 2) {
				if (targets.get(src.argumentCells[i]) != null) {
					try {
						src.argumentCells[i + 1] = targets.get(src.argumentCells[i]).pointer - (src.pointer + i * 4) - 4;
					} catch (NullPointerException e) {
						//invalid ptr
					}
				}
			}
			src.argumentCells[1] = defaultTarget.pointer - (src.pointer + 4);
			src.updateDisassembly();
		}
	}

	public enum Commands {
		NONE,
		LOAD_PRI,
		LOAD_ALT,
		LOAD_S_PRI,
		LOAD_S_ALT,
		LREF_PRI,
		LREF_ALT,
		LREF_S_PRI,
		LREF_S_ALT,
		LOAD_I,
		LODB_I,
		CONST_PRI,
		CONST_ALT,
		ADDR_PRI,
		ADDR_ALT,
		STOR_PRI,
		STOR_ALT,
		STOR_S_PRI,
		STOR_S_ALT,
		SREF_PRI,
		SREF_ALT,
		SREF_S_PRI,
		SREF_S_ALT,
		STOR_I,
		STRB_I,
		LIDX,
		LIDX_B,
		IDXADDR,
		IDXADDR_B,
		ALIGN_PRI,
		ALIGN_ALT,
		LCTRL,
		SCTRL,
		MOVE_PRI,
		MOVE_ALT,
		XCHG,
		PUSH_PRI,
		PUSH_ALT,
		PICK,
		PUSH_C,
		PUSH,
		PUSH_S,
		PPRI,
		PALT,
		STACK,
		HEAP,
		PROC,
		RET,
		RETN,
		CALL,
		CALL_PRI,
		JUMP,
		JREL,
		JZER,
		JNZ,
		JEQ,
		JNEQ,
		JLESS,
		JLEQ,
		JGRTR,
		JGEQ,
		JSLESS,
		JSLEQ,
		JSGRTR,
		JSGEQ,
		SHL,
		SHR,
		SSHR,
		SHL_C_PRI,
		SHL_C_ALT,
		SHR_C_PRI,
		SHR_C_ALT,
		SMUL,
		SDIV,
		SDIV_ALT,
		UMUL,
		UDIV,
		UDIV_ALT,
		ADD,
		SUB,
		SUB_ALT,
		AND,
		OR,
		XOR,
		NOT,
		NEG,
		INVERT,
		ADD_C,
		SMUL_C,
		ZERO_PRI,
		ZERO_ALT,
		ZERO,
		ZERO_S,
		SIGN_PRI,
		SIGN_ALT,
		EQ,
		NEQ,
		LESS,
		LEQ,
		GRTR,
		GEQ,
		SLESS,
		SLEQ,
		SGRTR,
		SGEQ,
		EQ_C_PRI,
		EQ_C_ALT,
		INC_PRI,
		INC_ALT,
		INC,
		INC_S,
		INC_I,
		DEC_PRI,
		DEC_ALT,
		DEC,
		DEC_S,
		DEC_I,
		MOVS,
		CMPS,
		FILL,
		HALT,
		BOUNDS,
		SYSREQ_PRI,
		SYSREQ_C,
		FILE,
		LINE,
		SYMBOL,
		SRANGE,
		JUMP_PRI,
		SWITCH,
		CASETBL,
		SWAP_PRI,
		SWAP_ALT,
		PUSH_ADR,
		NOP,
		SYSREQ_N,
		SYMTAG,
		BREAK,
		PUSH2_C,
		PUSH2,
		PUSH2_S,
		PUSH2_ADR,
		PUSH3_C,
		PUSH3,
		PUSH3_S,
		PUSH3_ADR,
		PUSH4_C,
		PUSH4,
		PUSH4_S,
		PUSH4_ADR,
		PUSH5_C,
		PUSH5,
		PUSH5_S,
		PUSH5_ADR,
		LOAD_BOTH,
		LOAD_S_BOTH,
		CONST,
		CONST_S,
		/* overlay instructions */
		ICALL,
		IRETN,
		ISWITCH,
		ICASETBL,
		/* packed instructions */
		LOAD_P_PRI,
		LOAD_P_ALT,
		LOAD_P_S_PRI,
		LOAD_P_S_ALT,
		LREF_P_PRI,
		LREF_P_ALT,
		LREF_P_S_PRI,
		LREF_P_S_ALT,
		LODB_P_I,
		CONST_P_PRI,
		CONST_P_ALT,
		ADDR_P_PRI,
		ADDR_P_ALT,
		STOR_P_PRI,
		STOR_P_ALT,
		STOR_P_S_PRI,
		STOR_P_S_ALT,
		SREF_P_PRI,
		SREF_P_ALT,
		SREF_P_S_PRI,
		SREF_P_S_ALT,
		STRB_P_I,
		LIDX_P_B,
		IDXADDR_P_B,
		ALIGN_P_PRI,
		ALIGN_P_ALT,
		PUSH_P_C,
		PUSH_P,
		PUSH_P_S,
		STACK_P,
		HEAP_P,
		SHL_P_C_PRI,
		SHL_P_C_ALT,
		SHR_P_C_PRI,
		SHR_P_C_ALT,
		ADD_P_C,
		SMUL_P_C,
		ZERO_P,
		ZERO_P_S,
		EQ_P_C_PRI,
		EQ_P_C_ALT,
		INC_P,
		INC_P_S,
		DEC_P,
		DEC_P_S,
		MOVS_P,
		CMPS_P,
		FILL_P,
		HALT_P,
		BOUNDS_P,
		PUSH_P_ADR,
		SYSREQ_D,
		SYSREQ_ND,
		NUM_OPCODES,
	}
}
