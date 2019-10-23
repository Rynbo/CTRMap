package ctrmap.formats.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PawnSubroutine {

	public String name;
	public int originalPtr;
	public List<PawnInstruction> instructions = new ArrayList<>();

	public GFLPawnScript parent;

	public PawnSubroutine(int startingInstruction, List<PawnInstruction> source, GFLPawnScript parent) {
		this.parent = parent;
		originalPtr = startingInstruction;
		name = "sub_" + Integer.toHexString(source.get(startingInstruction).pointer).toUpperCase();
		int ins = startingInstruction;
		MainLoop:
		for (; ins < source.size();) {
			PawnInstruction instruction = source.get(ins);
			switch (instruction.getCommand()) {
				case 0x2F:
				case 0x30:
					//return, finalize subroutine
					instructions.add(instruction);
					break MainLoop;
				default:
					instructions.add(instruction);
					ins++;
					break;
			}
		}
	}

	public PawnSubroutine(int ptr) {
		this.name = "sub_" + Integer.toHexString(ptr);
		this.originalPtr = ptr;
	}

	public static PawnSubroutine fromCode(int pointer, Scanner code) {
		return fromCode(pointer, code, true);
	}

	public static PawnSubroutine fromCode(int pointer, Scanner code, boolean doOutput) {
		PawnSubroutine ret = new PawnSubroutine(pointer);
		ret.originalPtr = pointer;
		String line;
		int ptr = ret.originalPtr;
		while (code.hasNextLine() && !code.nextLine().replaceAll("\t", "").equals("{")) {
			//await subroutine beginning
		}
		if (!code.hasNextLine()){
			return null;
		}
		if (doOutput) {
			System.out.println("[INFO] Found code, parsing instructions.");
		}
		while (code.hasNextLine() && !(line = code.nextLine().replaceAll("\t", "")).equals("}")) {
			if (line.length() == 0) {
				continue;
			}
			PawnInstruction newIns = PawnInstruction.fromString(ptr, line, doOutput);
			newIns.checkJmpConvertArgs();
			if (newIns.getCommand() == 0x82) {
				newIns = caseTblFromString(newIns.pointer, code);
			}
			if (newIns.cellValue != -1) {
				ret.instructions.add(newIns);
				if (!newIns.hasCompressedArgument) {
					ptr += newIns.argumentCount * 4;
				}
				ptr += 4;
			}
		}
		if (doOutput) {
			System.out.println("[INFO] Done parsing instructions for " + ret.name + ", found " + ret.instructions.size() + " valid instructions.");
		}
		return ret;
	}

	public static PawnInstruction caseTblFromString(int ptr, Scanner code) {
		PawnInstruction newIns = new PawnInstruction(ptr, 0x82, "");
		while (!code.nextLine().replaceAll("\t", "").equals("{")) {
			//await casetbl beginning
		}
		Map<Integer, Integer> cases = new HashMap<>();
		int defaultCaseJmp = 0;
		String line;
		while (!(line = code.nextLine().replaceAll("\t", "")).equals("}")) {
			if (line.startsWith("*")) {
				int chara = line.lastIndexOf("=>") + 2;
				defaultCaseJmp = Integer.parseInt(line.substring(chara).trim().replaceAll("x", ""), 16);
			} else {
				if (line.contains("=>")) {
					try {
						int firstGap = line.trim().indexOf(' ');
						int id = Integer.parseInt(line.trim().substring(0, firstGap));
						int chara = line.lastIndexOf("=>") + 2;
						if (line.length() > chara) {
							int caseJmp = Integer.parseInt(line.substring(chara).trim().replaceAll("0x", ""), 16);
							cases.put(id, caseJmp);
						}
					} catch (NumberFormatException e) {

					}
				}
			}
			//await casetbl end
		}
		newIns.argumentCount = cases.size() * 2 + 2;
		newIns.argumentCells = new int[newIns.argumentCount];
		newIns.argumentCells[0] = cases.size();
		int idx = 2;
		for (Map.Entry e : cases.entrySet()) {
			newIns.argumentCells[idx + 1] = (Integer) e.getValue() - (ptr + idx * 4) - 4;
			newIns.argumentCells[idx] = (Integer) e.getKey();
			idx += 2;
		}
		newIns.argumentCells[1] = defaultCaseJmp - (ptr + 4);
		return newIns;
	}

	public int getInstructionCount() {
		return instructions.size();
	}

	public int getFinalRelativePointer() {
		int ptr = 0;
		for (int i = 0; i < instructions.size(); i++) {
			ptr += 4;
			PawnInstruction ins = instructions.get(i);
			if (!ins.hasCompressedArgument) {
				ptr += ins.argumentCount * 4;
			}
		}
		return ptr;
	}

	public void updateDisassembly() {
		for (int i = 0; i < instructions.size(); i++) {
			instructions.get(i).updateDisassembly();
		}
	}

	public List<String> getAllInstructionStrings(int indentLevel) {
		List<String> ret = new ArrayList<>();
		for (int i = 0; i < instructions.size(); i++) {
			StringBuilder sb = new StringBuilder();
			StringBuilder indentator = new StringBuilder();
			if (indentLevel != -1) {
				for (int j = 0; j < indentLevel; j++) {
					indentator.append("\t");
				}
			}
			sb.append(indentator);
			sb.append(instructions.get(i).stringValue.replaceAll("\n", "\n" + indentator.toString()));
			ret.add(sb.toString());
		}
		return ret;
	}
}
