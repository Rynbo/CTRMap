package ctrmap.formats.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PawnDisassembler {

	public static List<PawnSubroutine> disassembleScript(GFLPawnScript scr) {
		scr.decompressThis();
		List<PawnSubroutine> ret = new ArrayList<>();
		PawnSubroutine entryPoint = new PawnSubroutine(0, scr.instructions, scr); //usually just HALT_P
		entryPoint.parent = scr;
		ret.add(entryPoint);
		//finished the main stuff
		for (int ins = entryPoint.getInstructionCount(); ins < scr.instructions.size();) {
			PawnSubroutine newSub = new PawnSubroutine(ins, scr.instructions, scr);
			newSub.parent = scr;
			ret.add(newSub);
			ins += newSub.getInstructionCount();
		}
		return ret;
	}

	public static List<PawnSubroutine> assembleScript(String code, boolean doOutput) {
		if (doOutput) {
			System.out.println("[INFO] CTRMap Pawn assembler running");
			System.out.println("[INFO] Going to parse " + code.length() + " characters of input.");
		}
		long begin = System.currentTimeMillis();
		List<PawnSubroutine> ret = new ArrayList<>();
		Scanner scanner = new Scanner(code);
		scanner.useDelimiter("\n");
		int ptr = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().replaceAll("\t", "");
			if (line.length() > 0) {
				if (line.startsWith("sub_")) {
					if (doOutput) {
						System.out.println("[INFO] Found subroutine " + line + " at pointer 0x" + Integer.toHexString(ptr).toUpperCase());
					}
					PawnSubroutine newSub = PawnSubroutine.fromCode(ptr, scanner, doOutput);
					if (newSub != null) {
						newSub.updateDisassembly();
						ret.add(newSub);
						ptr += newSub.getFinalRelativePointer();
					}
				}
			}
		}
		if (doOutput) {
			System.out.println("[INFO] All work done.");
			System.out.println("[INFO] The assembly has finished in " + (System.currentTimeMillis() - begin) / 1000f + " seconds.");
		}
		scanner.close();
		return ret;
	}

	public static int[] getRawInstructions(List<PawnInstruction> ins) {
		List<int[]> raw = new ArrayList<>();
		int targetLength = 0;
		for (PawnInstruction i : ins) {
			int[] srcRaw = i.getRaw();
			targetLength += srcRaw.length;
			raw.add(srcRaw);
		}
		int[] target = new int[targetLength];
		int off = 0;
		for (int[] a : raw) {
			System.arraycopy(a, 0, target, off, a.length);
			off += a.length;
		}
		return target;
	}

	public static int isSubCorrupted(String code, int start) {
		System.out.println(code.substring(Math.max(0, start - 20), start + 50));
		char chara;
		int idx = start;
		int lines = 0;
		if (idx >= code.length()) {
			return lines;
		}
		while ((chara = code.charAt(idx)) != '{') {
			if (chara == '\n') {
				lines++;
			}
			if (code.length() > idx + 3) {
				if (code.substring(idx, idx + 3).equals("sub")) { //new sub without ending this one
					return lines;
				}
			}
			idx++;
			if (code.length() <= idx) { //end of source
				return lines;
			}
		}
		if (idx >= code.length()) {
			return lines;
		}
		while ((chara = code.charAt(idx)) != '}') {
			if (chara == '\n') {
				lines++;
			}
			if (code.length() > idx + 2) {
				if (code.substring(idx, idx + 2).equals("sub")) { //new sub without ending this one
					return lines;
				}
			}
			idx++;
			if (code.length() <= idx) { //end of source
				return lines;
			}
		}
		return -1;
	}
}
