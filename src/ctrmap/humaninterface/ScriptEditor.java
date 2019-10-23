package ctrmap.humaninterface;

import ctrmap.CtrmapMainframe;
import ctrmap.formats.scripts.GFLPawnScript;
import ctrmap.formats.scripts.PawnDisassembler;
import ctrmap.formats.scripts.PawnInstruction;
import ctrmap.formats.scripts.PawnPrefixEntry;
import ctrmap.formats.scripts.PawnSubroutine;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;

public class ScriptEditor extends javax.swing.JPanel {

	/**
	 * Creates new form ScriptEditor
	 */
	public GFLPawnScript script;
	public ScriptDocumentListener sdl;
	public ScriptDocumentFilter sdf;
	private JTextArea ptrs = new JTextArea();
	private DefaultListModel prefixEntryList = new DefaultListModel();
	private List<PawnPrefixEntry> currentEntries;
	private PawnPrefixEntry currentEntry;
	private int currentIdx = -1;

	private String textSwapBuffer;
	public boolean editData = false;

	public boolean loaded = false;

	public ScriptEditor() {
		initComponents();
		ptrs.setColumns(6);
		ptrs.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
		ptrs.setRows(5);
		ptrs.setBackground(new Color(255, 255, 170));
		ptrs.setMinimumSize(new Dimension(30, 720));
		ptrs.setBorder(new MatteBorder(0, 0, 0, 5, Color.GRAY));
		ptrs.setEditable(false);
		disassemblyScrollPane.setViewportView(disassemblyArea);
		prefixItemList.setModel(prefixEntryList);
		prefixItemList.addListSelectionListener((ListSelectionEvent e) -> {
			if (loaded) {
				savePrefixEntry();
				showPrefixEntry(prefixItemList.getSelectedIndex());
			}
		});
		sdl = new ScriptDocumentListener(this);
		sdf = new ScriptDocumentFilter(this);
		assemblerOutput.setLineWrap(true);
		assemblerOutput.setWrapStyleWord(true);
		disassemblyArea.getDocument().addDocumentListener(sdl);
		disassemblyScrollPane.setRowHeaderView(ptrs);
		((AbstractDocument) disassemblyArea.getDocument()).setDocumentFilter(sdf);
		((DefaultCaret) disassemblyArea.getCaret()).setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
	}

	public void loadScript(GFLPawnScript scr) {
		loaded = false;
		script = scr;
		currentIdx = -1;
		sdl.setScript(scr);
		sdf.setScript(scr);
		script.setInstructionListeners();
		prefixCat.setSelectedIndex(1);
		textSwapBuffer = getDataText(scr.data);
		editData = false;
		btnIsDataEdit.setSelected(false);
		btnIsDataEdit.setText("Edit data");
		try {
			updateDocument(false, CaretMotion.NONE);
		} catch (BadLocationException ex) {
			Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
		}
		loaded = true;
	}

	public enum CaretMotion {
		NONE,
		FORWARD,
		BACKWARD
	}

	public static class ScriptDocumentFilter extends DocumentFilter {

		private ScriptEditor se;
		private GFLPawnScript scr;

		public ScriptDocumentFilter(ScriptEditor se) {
			this.se = se;
		}

		public void setScript(GFLPawnScript scr) {
			this.scr = scr;
		}

		@Override
		public void remove(DocumentFilter.FilterBypass fb, int offset, int length) {
			try {
				if (scr != null && se.loaded && !se.editData) {
					int off = offset;
					int len = length;
					String text = se.disassemblyArea.getText();
					String textToBeRemoved = text.substring(offset, offset + length);
					boolean isCtbl = false;
					int chara = off;
					while (chara > 0) {
						char test = text.charAt(chara);
						if (test != '\t') {
							chara--;
							if (test == ')') {
								break;
							}
						} else {
							if (text.charAt(chara - 1) == '\t') {
								isCtbl = true;
							}
							break;
						}
					}
					if (textToBeRemoved.contains("\n")) { //Only perform when an instruction has been actually removed, not just typo correcting and stuff
						int changedLineStart = getCountOfLineEndsInString(text, off);
						int changedLineEnd = getCountOfLineEndsInString(text, off + len);
						//int addedLines = changedLineEnd - changedLineStart;
						String newText = text.substring(0, offset) + text.substring(offset + length);
						List<PawnSubroutine> reassembled = PawnDisassembler.assembleScript(newText, false);
						int insCount = 0;
						for (PawnSubroutine s : reassembled) {
							insCount += s.instructions.size();
						}
						setLines(PawnDisassembler.disassembleScript(scr));
						List<PawnInstruction> removedInstructions = new ArrayList<>();
						int remCount = scr.instructions.size() - insCount;
						if (insCount < scr.instructions.size()) {
							for (PawnInstruction i : scr.instructions) {
								if ((remCount == 1 ? i.line > changedLineStart : i.line >= changedLineStart) && i.line <= changedLineEnd) {
									removedInstructions.add(i);
								}
							}
							scr.instructions.removeAll(removedInstructions);
							se.updateDocument(false, CaretMotion.BACKWARD);
						} else if (isCtbl) {
							setLines(reassembled);
							for (PawnSubroutine s : reassembled) {
								for (PawnInstruction i : s.instructions) {
									if (i.getCommand() == 0x82) {
										for (int linecheck = 0; linecheck < (i.argumentCount / 2 + 2); linecheck++) { //additional two lines for brackets
											if (i.line + linecheck >= changedLineStart && i.line + linecheck <= changedLineEnd) {
												//get entire casetbl for assembly
												int idx = 0;
												int endlinecnt = 0;
												while (idx < text.length() && endlinecnt < i.line) {
													if (text.charAt(idx) == '\n') {
														endlinecnt++;
													}
													idx++;
												}
												int closingBkt = text.indexOf("}", idx);
												String caseblk = text.substring(idx, closingBkt + 1);
												PawnInstruction newIns = PawnSubroutine.caseTblFromString(i.pointer, new Scanner(caseblk));
												PawnInstruction replace = scr.lookupInstructionByPtr(i.pointer);
												if (replace.argumentCount > newIns.argumentCount) {
													replace.argumentCells = newIns.argumentCells;
													replace.argumentCount = newIns.argumentCount;
													se.updateDocument(false, CaretMotion.NONE);
												}
												break;
											}
										}
									}
								}
							}
						}
					}

				}
				super.remove(fb, offset, length);
			} catch (BadLocationException ex) {
				Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static class ScriptDocumentListener implements DocumentListener {

		private GFLPawnScript scr;
		private ScriptEditor se;

		public ScriptDocumentListener(ScriptEditor se) {
			this.se = se;
		}

		public void setScript(GFLPawnScript scr) {
			this.scr = scr;
		}

		private int scanBackwardsFindUntil(String text, int pos, String toFind, String stopAt, boolean caseSensitive) {
			int idx = pos;
			if (caseSensitive) {
				while (idx > 0) {
					if (text.length() <= idx + toFind.length() || !text.substring(idx, idx + toFind.length()).equals(toFind)) {
						if (text.length() <= idx + toFind.length() && text.substring(idx, idx + stopAt.length()).equals(stopAt)) {
							return -1;
						}
						idx--;
					} else {
						return idx;
					}
				}
			} else {
				String upperTF = toFind.toUpperCase();
				String upperSA = stopAt.toUpperCase();
				while (idx > 0) {
					if (!text.substring(idx, idx + toFind.length()).toUpperCase().equals(upperTF)) {
						if (text.substring(idx, idx + stopAt.length()).toUpperCase().equals(upperSA)) {
							return -1;
						}
						idx--;
					} else {
						return idx;
					}
				}
			}
			return -1;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (scr != null && se.loaded && !se.editData) {
				try {
					final int off = e.getOffset();
					final int len = e.getLength();
					String text = e.getDocument().getText(0, e.getDocument().getLength());
					if (text.substring(off, off + len).equals("{")) {
						//scan if sub or casetbl
						if (text.charAt(off - 1) != '\n') {
							SwingUtilities.invokeLater(() -> {
								try {
									e.getDocument().insertString(off, "\n", new SimpleAttributeSet());
								} catch (BadLocationException ex) {
									Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
								}
							});
						}
						if (scanBackwardsFindUntil(text, off - 1, "sub_", "\t", true) != -1) {
							SwingUtilities.invokeLater(() -> {
								try {
									e.getDocument().insertString(off + 2, "\n\tRETN(0)\n}", new SimpleAttributeSet());
								} catch (BadLocationException ex) {
									Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
								}
							});
						} else {
							int idx = off - 1;
							while (text.charAt(idx) == '\n' || text.charAt(idx) == '\t') {
								idx--;
							}
							if (scanBackwardsFindUntil(text, idx, "CASETBL", "\t", false) != -1) {
								SwingUtilities.invokeLater(() -> {
									try {
										e.getDocument().insertString(off + 2, "\n\t\t* => 0x0\n\t}", new SimpleAttributeSet());
									} catch (BadLocationException ex) {
										Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
									}
								});
							}
						}
					}
					if (text.substring(off, off + len).equals("\n")) {
						int pos = off;
						StringBuilder tabBldr = new StringBuilder();
						if (off > 0 && (text.charAt(off - 1) == '{' || (off > 2 && text.substring(off - 2, off).equals("\t}")))) {
							tabBldr.append('\t'); //sub beginning
						} else {
							while (pos > 0) {
								if (text.charAt(pos) == '\t') {
									while (pos > 0 && text.charAt(pos) == '\t') {
										tabBldr.append('\t');
										pos--;
									}
									break;
								} else if (text.charAt((pos)) == '{' || text.charAt(pos) == '}') {
									//beginning/end of subroutine
									break;
								}
								pos--;
							}
						}
						SwingUtilities.invokeLater(() -> {
							try {
								e.getDocument().insertString(off + 1, tabBldr.toString(), new SimpleAttributeSet());
							} catch (BadLocationException ex) {
								Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
							}
						});
					}
					int changedLineStart = getCountOfLineEndsInString(text, off);
					int changedLineEnd = getCountOfLineEndsInString(text, off + len);
					StringBuilder changedLine = new StringBuilder();
					int chara = off;
					boolean isArgs = false;
					while (chara > 0) {
						char test = text.charAt(chara);
						if (test != '(' && !(test == '=' && text.charAt(chara + 1) == '>')) {
							chara--;
							if (test == ')' || test == '\t' || test == '\n') {
								break;
							}
						} else {
							isArgs = true;
							while (test != ')' && test != '\t' && test != '\n') {
								test = text.charAt(chara);
								changedLine.append(test);
								chara++;
							}
							break;
						}
					}

					List<PawnSubroutine> reassembled = PawnDisassembler.assembleScript(text, false);
					int insCount = 0;
					for (PawnSubroutine s : reassembled) {
						insCount += s.instructions.size();
					}
					if (insCount > scr.instructions.size()) {
						setLines(reassembled);
						List<PawnInstruction> addedInstructions = new ArrayList<>();
						for (PawnSubroutine s : reassembled) {
							for (PawnInstruction i : s.instructions) {
								if (i.line >= changedLineStart && i.line <= changedLineEnd) {
									addedInstructions.add(i);
								} else if (i.getCommand() == 0x82) {
									for (int linecheck = 0; linecheck < (i.argumentCount / 2 + 1); linecheck++) { //additional two lines for brackets
										if (i.line + linecheck >= changedLineStart && i.line + linecheck <= changedLineEnd) {
											addedInstructions.add(i);
											break;
										}
									}
								}
							}
						}
						boolean added = false;
						for (int i = 0; i < scr.instructions.size(); i++) {
							PawnInstruction ins = scr.instructions.get(i);
							if (ins.line >= changedLineStart) {
								int idx = 0;
								for (PawnInstruction a : addedInstructions) {
									if (a.getCommand() == 0x82) {
										scr.instructions.add(i - 1 + idx, a);
									} else {
										scr.instructions.add(i + idx, a);
									}
									idx++;
								}
								added = true;
								break;
							}
						}
						if (!added) {
							//no instruction found after new ones, means they are at the end of script, so let's just append them
							scr.instructions.addAll(addedInstructions);
						}
						se.updateDocument(true, CaretMotion.FORWARD);
					} else if (isArgs) {
						setLines(reassembled);
						Outer:
						for (PawnSubroutine s : reassembled) {
							for (PawnInstruction i : s.instructions) {
								if (i.getCommand() == 0x82) {
									for (int linecheck = 0; linecheck < (i.argumentCount / 2 + 2); linecheck++) { //additional two lines for brackets
										if (i.line + linecheck >= changedLineStart && i.line + linecheck <= changedLineEnd) {
											//get entire casetbl for assembly
											int idx = 0;
											int endlinecnt = 0;
											while (idx < text.length() && endlinecnt < i.line) {
												if (text.charAt(idx) == '\n') {
													endlinecnt++;
												}
												idx++;
											}
											int closingBkt = text.indexOf("}", idx);
											String caseblk = text.substring(idx, closingBkt + 1);
											PawnInstruction newIns = PawnSubroutine.caseTblFromString(i.pointer, new Scanner(caseblk));
											PawnInstruction replace = scr.lookupInstructionByPtr(i.pointer);
											if (replace.argumentCount < newIns.argumentCount) {
												replace.argumentCells = newIns.argumentCells;
												replace.argumentCount = newIns.argumentCount;
												se.updateDocument(true, CaretMotion.FORWARD);
											}
											break;
										}
									}
								} else if (i.line >= changedLineStart && i.line <= changedLineEnd) {
									scr.lookupInstructionByPtr(i.pointer).setArgsFromString(changedLine.toString());
									break Outer;
								}
							}
						}
					}
				} catch (BadLocationException ex) {
					Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			try {
				if (!se.editData) {
					int off = e.getOffset();
					int len = e.getLength();
					String text = e.getDocument().getText(0, e.getDocument().getLength());
					int chara = Math.min(off, text.length() - 1);
					boolean isArgs = false;
					StringBuilder changedLine = new StringBuilder();
					while (chara > 0) {
						char test = text.charAt(chara);
						if (test != '(' && !(test == '=' && text.charAt(chara + 1) == '>')) {
							chara--;
							if (test == ')' || test == '\t' || test == '\n') {
								break;
							}
						} else {
							isArgs = true;
							while (test != ')' && test != '\t' && test != '\n') {
								test = text.charAt(chara);
								changedLine.append(test);
								chara++;
							}
							break;
						}
					}
					if (isArgs) {
						int changedLineStart = getCountOfLineEndsInString(text, off);
						int changedLineEnd = getCountOfLineEndsInString(text, off + len);
						for (PawnInstruction i : scr.instructions) {
							if (i.line >= changedLineStart && i.line <= changedLineEnd) {
								boolean success = scr.lookupInstructionByPtr(i.pointer).setArgsFromString(changedLine.toString());
								if (success) {
									se.updateDocument(false, CaretMotion.BACKWARD);
								}
								break;
							}
						}
					}
				}
			} catch (BadLocationException ex) {
				Logger.getLogger(ScriptEditor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	}

	public static void setPtrsByIndex(List<PawnInstruction> instructions) {
		int currentPtr = 0;
		for (int i = 0; i < instructions.size(); i++) {
			PawnInstruction ins = instructions.get(i);
			ins.pointer = currentPtr;
			currentPtr += 4;
			if (!ins.hasCompressedArgument) {
				currentPtr += ins.argumentCount * 4;
			}
		}
	}

	public static int getPtrByLine(List<PawnSubroutine> subs, int line) {
		int ret = 0;
		for (int sub = 0; sub < subs.size(); sub++) {
			for (int i = 0; i < subs.get(sub).instructions.size(); i++) {
				PawnInstruction ins = subs.get(sub).instructions.get(i);
				if (ins.line <= line) {
					ret = ins.pointer;
				}
			}
		}
		return ret;
	}

	public static void setLines(List<PawnSubroutine> subs) {
		int currentLine = 0;
		for (int i = 0; i < subs.size(); i++) {
			currentLine += 2;
			for (int j = 0; j < subs.get(i).instructions.size(); j++) {
				subs.get(i).instructions.get(j).line = currentLine;
				currentLine++;
				currentLine += getCountOfLineEndsInString(PawnInstruction.getDisassembly(subs.get(i).instructions.get(j)));
			}
			currentLine += 2;
		}
	}

	public String getPtrText(GFLPawnScript scr) {
		int line = 2;
		StringBuilder sb = new StringBuilder("\n\n");
		for (PawnInstruction i : scr.instructions) {
			sb.append(getFormattedStringShort(i.pointer));
			int extraLines = getCountOfLineEndsInString(i.stringValue);
			for (int j = 0; j < extraLines; j++) {
				sb.append('\n');
			}
			if (i.getCommand() == 0x2F || i.getCommand() == 0x30) {
				sb.append("\n\n\n\n");
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public static String getDisassemblyTextForArea(GFLPawnScript scr) {
		StringBuilder daText = new StringBuilder();
		List<PawnSubroutine> subs = PawnDisassembler.disassembleScript(scr);
		int currentLine = 0;
		for (int i = 0; i < subs.size(); i++) {
			subs.get(i).updateDisassembly();
			daText.append(subs.get(i).name);
			daText.append("\n{\n");
			currentLine += 2;
			List<String> dis = subs.get(i).getAllInstructionStrings(1);
			for (int j = 0; j < dis.size(); j++) {
				subs.get(i).instructions.get(j).line = currentLine;
				daText.append(dis.get(j));
				daText.append("\n");
				currentLine++;
				currentLine += getCountOfLineEndsInString(dis.get(j));
			}
			daText.append("}\n\n");
			currentLine += 2;
		}
		return daText.toString();
	}

	public String getDataText(List<PawnInstruction> dataPseudoInstructions) {
		StringBuilder sb = new StringBuilder();
		for (PawnInstruction i : dataPseudoInstructions) {
			sb.append(getFormattedString(i.cellValue));
			sb.append("\n");
		}
		return sb.toString();
	}

	public String getDataPtr(List<PawnInstruction> dataPseudoInstructions) {
		StringBuilder sb = new StringBuilder();
		int ptr = 0;
		for (PawnInstruction i : dataPseudoInstructions) {
			sb.append(getFormattedStringShort(ptr));
			sb.append("\n");
			ptr += 4;
		}
		return sb.toString();
	}

	public List<PawnInstruction> getDataInstructions(String text) {
		Scanner scanner = new Scanner(text);
		List<PawnInstruction> ret = new ArrayList<>();
		int ptr = 0;
		while (scanner.hasNextLine()) {
			PawnInstruction i = new PawnInstruction(ptr, Integer.reverseBytes(Integer.parseUnsignedInt(scanner.nextLine().replaceAll(" ", ""), 16)), "DATA ELEMENT");
			i.hasCompressedArgument = false;
			i.argumentCount = 0;
			ret.add(i);
			ptr += 4;
		}
		return ret;
	}

	public void updateDocument(boolean putCaretAtEnd, CaretMotion advanceCaret) throws BadLocationException {
		setPtrsByIndex(script.instructions);
		script.callInstructionListeners();
		script.setInstructionListeners();
		String newText = getDisassemblyTextForArea(script);
		String ptrText = getPtrText(script);
		final int originalLocation = disassemblyArea.getCaretPosition();
		int idx = originalLocation;
		if (putCaretAtEnd) {
			while (idx < newText.length() && newText.charAt(idx) != '\n') {
				idx++;
			}
		}
		final int finalIdx = idx;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				disassemblyArea.setText(newText.trim());
				ptrs.setText(ptrText);
				setLines(PawnDisassembler.disassembleScript(script));
				int caretPlus = 0;
				if (advanceCaret == CaretMotion.FORWARD) {
					caretPlus = 1;
				} else if (advanceCaret == CaretMotion.BACKWARD) {
					caretPlus = -1;
				}
				disassemblyArea.setCaretPosition(Math.min(putCaretAtEnd ? finalIdx : originalLocation + caretPlus, newText.length() - 1));
				int disLine = getCountOfLineEndsInString(newText, disassemblyArea.getCaretPosition());
				int lines = 0;
				for (int i = 0; i < ptrText.length(); i++) {
					if (ptrText.charAt(i) == '\n') {
						lines++;
					}
					if (lines == disLine) {
						ptrs.setCaretPosition(i);
						break;
					}
				}
			}
		});
	}

	public void setPrefixCategory(PawnPrefixEntry.Type cat) {
		savePrefixEntry();
		loaded = false;
		prefixEntryList.clear();
		currentEntry = null;
		currentIdx = -1;
		address.setText("");
		name.setText("");
		if (script != null) {
			List<PawnPrefixEntry> src = null;
			switch (cat) {
				case PUBLIC:
					src = script.publics;
					break;
				case NATIVE:
					src = script.natives;
					break;
				case LIBRARY:
					src = script.libraries;
					break;
				case PUBLIC_VAR:
					src = script.publicVars;
					break;
				case TAG:
					src = script.tags;
					break;
				case NAME:
					src = script.names;
					break;
			}
			currentEntries = src;
			if (src != null) {
				if (cat == PawnPrefixEntry.Type.PUBLIC) {
					for (int i = 0; i < src.size(); i++) {
						PawnPrefixEntry e = src.get(i);
						prefixEntryList.addElement(i + " - " + getFormattedStringLE(e.data[0]) + " " + getFormattedStringLE(e.data[1]));
					}
					prefixEntryList.addElement("main - " + getFormattedStringLE(script.mainEntryPointDummy.argumentCells[0]));
				} else {
					for (int i = 0; i < src.size(); i++) {
						PawnPrefixEntry e = src.get(i);
						prefixEntryList.addElement(i + " - " + getFormattedString(e.data[0]) + " " + getFormattedString(e.data[1]));
					}
				}
			}
		}
		loaded = true;
	}

	private PawnPrefixEntry.Type getTypeForIdx(int idx) {
		PawnPrefixEntry.Type type = null;
		switch (idx) {
			case 0:
				type = PawnPrefixEntry.Type.PUBLIC;
				break;
			case 1:
				type = PawnPrefixEntry.Type.NATIVE;
				break;
			case 2:
				type = PawnPrefixEntry.Type.LIBRARY;
				break;
			case 3:
				type = PawnPrefixEntry.Type.PUBLIC_VAR;
				break;
			case 4:
				type = PawnPrefixEntry.Type.TAG;
				break;
			case 5:
				type = PawnPrefixEntry.Type.NAME;
				break;
		}
		return type;
	}

	public void showPrefixEntry(int idx) {
		currentIdx = idx;
		if (currentEntries != null && idx >= 0) {
			if (prefixCat.getSelectedIndex() == 0) {
				//publics
				if (idx == currentEntries.size()) {
					currentEntry = new PawnPrefixEntry(8, PawnPrefixEntry.Type.PUBLIC, new int[]{script.mainEntryPointDummy.argumentCells[0], 0});
				} else {
					currentEntry = currentEntries.get(idx);
				}
				address.setText(getFormattedStringLE(currentEntry.data[0]));
				name.setText(getFormattedStringLE(currentEntry.data[1]));
			} else if (idx < currentEntries.size()) {
				PawnPrefixEntry e = currentEntries.get(idx);
				currentEntry = e;
				address.setText(getFormattedString(e.data[0]));
				name.setText(getFormattedString(e.data[1]));
			}
		}
	}

	public void savePrefixEntry() {
		if (currentEntry != null && currentIdx != -1) {
			if (currentEntry.type == PawnPrefixEntry.Type.PUBLIC) {
				currentEntry.data[0] = Integer.parseUnsignedInt(address.getText().replaceAll(" ", ""), 16);
				currentEntry.data[1] = Integer.parseUnsignedInt(name.getText().replaceAll(" ", ""), 16);
				if (currentIdx == currentEntries.size()) {
					script.mainEntryPointDummy.argumentCells[0] = currentEntry.data[0];
					script.mainEntryPointDummy.chkAddJumpListener();
					prefixEntryList.setElementAt("main - " + getFormattedStringLE(script.mainEntryPointDummy.argumentCells[0]), currentIdx);
				} else {
					prefixEntryList.setElementAt(currentIdx + " - " + getFormattedStringLE(currentEntry.data[0]) + " " + getFormattedStringLE(currentEntry.data[1]), currentIdx);
				}
			} else {
				currentEntry.data[0] = Integer.reverseBytes(Integer.parseUnsignedInt(address.getText().replaceAll(" ", ""), 16));
				currentEntry.data[1] = Integer.reverseBytes(Integer.parseUnsignedInt(name.getText().replaceAll(" ", ""), 16));
				prefixEntryList.setElementAt(currentIdx + " - " + getFormattedString(currentEntry.data[0]) + " " + getFormattedString(currentEntry.data[1]), currentIdx);
			}
		}
	}

	public static int getCountOfLineEndsInString(String str) {
		return getCountOfLineEndsInString(str, str.length());
	}

	public static int getCountOfLineEndsInString(String str, int limit) {
		return getCountOfCharInString(str, '\n', limit);
	}

	public static int getCountOfCharInString(String str, char chara, int limit) {
		int cnt = 0;
		for (int i = 0; i < limit; i++) {
			if (str.charAt(i) == chara) {
				cnt++;
			}
		}
		return cnt;
	}

	public String getFormattedString(int instructionRaw) {
		String hexstring = Integer.toHexString(Integer.reverseBytes(instructionRaw));
		return ("00000000" + hexstring).substring(hexstring.length()).toUpperCase().replaceAll("..", "$0 ");
	}

	public String getFormattedStringLE(int instructionRaw) {
		String hexstring = Integer.toHexString(instructionRaw);
		return ("00000000" + hexstring).substring(hexstring.length()).toUpperCase().replaceAll("..", "$0 ");
	}

	public String getFormattedStringShort(int instructionRaw) {
		String hexstring = Integer.toHexString(instructionRaw);
		return ("0000" + hexstring).substring(hexstring.length()).toUpperCase().replaceAll("..", "$0 ");
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        assemblerOutputScrollPane = new javax.swing.JScrollPane();
        assemblerOutput = new javax.swing.JTextArea();
        disassemblyScrollPane = new javax.swing.JScrollPane();
        disassemblyArea = new javax.swing.JTextArea();
        btnTestAssembly = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        prefixCat = new javax.swing.JComboBox<>();
        prefixScrollPane = new javax.swing.JScrollPane();
        prefixItemList = new javax.swing.JList<>();
        btnIsDataEdit = new javax.swing.JToggleButton();
        btnNewPrefixEntry = new javax.swing.JButton();
        btnRemovePrefixEntry = new javax.swing.JButton();
        btnSavePrefixEntry = new javax.swing.JButton();
        addressLabel = new javax.swing.JLabel();
        address = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        name = new javax.swing.JTextField();

        assemblerOutput.setEditable(false);
        assemblerOutput.setColumns(20);
        assemblerOutput.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        assemblerOutput.setRows(5);
        assemblerOutputScrollPane.setViewportView(assemblerOutput);

        disassemblyArea.setColumns(20);
        disassemblyArea.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        disassemblyArea.setRows(5);
        disassemblyArea.setMargin(new java.awt.Insets(0, 10, 2, 2));
        disassemblyScrollPane.setViewportView(disassemblyArea);

        btnTestAssembly.setText("Run assembler");
        btnTestAssembly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestAssemblyActionPerformed(evt);
            }
        });

        btnSave.setText("Commit changes");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        prefixCat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Publics", "Natives", "Libraries", "Public variables", "Tags", "Names" }));
        prefixCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefixCatActionPerformed(evt);
            }
        });

        prefixScrollPane.setViewportView(prefixItemList);

        btnIsDataEdit.setText("Edit data");
        btnIsDataEdit.setName(""); // NOI18N
        btnIsDataEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIsDataEditActionPerformed(evt);
            }
        });

        btnNewPrefixEntry.setText("Add new");
        btnNewPrefixEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewPrefixEntryActionPerformed(evt);
            }
        });

        btnRemovePrefixEntry.setText("Remove");
        btnRemovePrefixEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemovePrefixEntryActionPerformed(evt);
            }
        });

        btnSavePrefixEntry.setText("Save");
        btnSavePrefixEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSavePrefixEntryActionPerformed(evt);
            }
        });

        addressLabel.setText("Address:");

        nameLabel.setText("Name:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(assemblerOutputScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(prefixCat, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(prefixScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addressLabel)
                            .addComponent(nameLabel)
                            .addComponent(btnSavePrefixEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemovePrefixEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNewPrefixEntry)
                            .addComponent(address)
                            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(disassemblyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnTestAssembly, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnIsDataEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(disassemblyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnTestAssembly)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSave))
                            .addComponent(assemblerOutputScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnIsDataEdit))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(prefixCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(prefixScrollPane)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(addressLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nameLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnNewPrefixEntry)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnRemovePrefixEntry)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSavePrefixEntry)))))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		if (!editData) {
			btnTestAssemblyActionPerformed(evt);
		} else {
			script.data = getDataInstructions(disassemblyArea.getText());
			script.updateRaw();
		}
		CtrmapMainframe.mZonePnl.store(false);
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnTestAssemblyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestAssemblyActionPerformed
		if (!editData) {
			assemblerOutput.setText("Assembler running:\n");
			PrintStream originalOut = System.out;
			PrintStream originalErr = System.err;
			PrintStream newOut = new PrintStream(new JTextAreaPrintStream(assemblerOutput));
			System.setOut(newOut);
			System.setErr(newOut);
			List<PawnSubroutine> subs = PawnDisassembler.assembleScript(disassemblyArea.getText(), true);
			script.instructions.clear();
			for (PawnSubroutine sub : subs) {
				script.instructions.addAll(sub.instructions);
			}
			script.updateRaw();
			loadScript(script);
			System.setOut(originalOut);
			System.setErr(originalErr);
		}
    }//GEN-LAST:event_btnTestAssemblyActionPerformed

    private void prefixCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefixCatActionPerformed
		if (script != null) {
			PawnPrefixEntry.Type type = getTypeForIdx(prefixCat.getSelectedIndex());
			if (type != null) {
				setPrefixCategory(type);
			}
		}
    }//GEN-LAST:event_prefixCatActionPerformed


    private void btnIsDataEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIsDataEditActionPerformed
		if (editData != btnIsDataEdit.isSelected()) {
			editData = btnIsDataEdit.isSelected();
			String tempSwapBuffer = textSwapBuffer;
			textSwapBuffer = disassemblyArea.getText();
			disassemblyArea.setText(tempSwapBuffer);
			if (btnIsDataEdit.isSelected()) {
				btnIsDataEdit.setText("Edit code");
				ptrs.setText(getDataPtr(script.data));
			} else {
				btnIsDataEdit.setText("Edit data");
			}
		}
    }//GEN-LAST:event_btnIsDataEditActionPerformed

    private void btnSavePrefixEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSavePrefixEntryActionPerformed
		savePrefixEntry();
		showPrefixEntry(prefixItemList.getSelectedIndex());
    }//GEN-LAST:event_btnSavePrefixEntryActionPerformed

    private void btnNewPrefixEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewPrefixEntryActionPerformed
		PawnPrefixEntry.Type type = getTypeForIdx(prefixCat.getSelectedIndex());
		currentEntries.add(new PawnPrefixEntry(8, type, new int[2]));
		setPrefixCategory(type);
		prefixItemList.setSelectedIndex(currentEntries.size() - 1);
    }//GEN-LAST:event_btnNewPrefixEntryActionPerformed

    private void btnRemovePrefixEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemovePrefixEntryActionPerformed
		if (currentIdx != -1) {
			prefixItemList.setSelectedIndex(-1);
			currentEntries.remove(currentIdx);
			setPrefixCategory(getTypeForIdx(prefixCat.getSelectedIndex()));
		}
    }//GEN-LAST:event_btnRemovePrefixEntryActionPerformed

	public static class JTextAreaPrintStream extends OutputStream {

		private JTextArea area;

		public JTextAreaPrintStream(JTextArea area) {
			this.area = area;
		}

		@Override
		public void write(byte[] buffer, int offset, int length) throws IOException {
			final String text = new String(buffer, offset, length);
			area.append(text);
		}

		@Override
		public void write(int b) throws IOException {
			area.append(String.valueOf((char) b));
		}

	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextArea assemblerOutput;
    private javax.swing.JScrollPane assemblerOutputScrollPane;
    private javax.swing.JToggleButton btnIsDataEdit;
    private javax.swing.JButton btnNewPrefixEntry;
    private javax.swing.JButton btnRemovePrefixEntry;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSavePrefixEntry;
    private javax.swing.JButton btnTestAssembly;
    private javax.swing.JTextArea disassemblyArea;
    private javax.swing.JScrollPane disassemblyScrollPane;
    private javax.swing.JTextField name;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox<String> prefixCat;
    private javax.swing.JList<String> prefixItemList;
    private javax.swing.JScrollPane prefixScrollPane;
    // End of variables declaration//GEN-END:variables
}
