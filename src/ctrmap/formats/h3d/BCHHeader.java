package ctrmap.formats.h3d;

public class BCHHeader {

	public String magic;
	public byte backwardCompatibility;
	public byte forwardCompatibility;
	public int version;

	public int mainHeaderOffset;
	public int stringTableOffset;
	public int gpuCommandsOffset;
	public int dataOffset;
	public int dataExtendedOffset;
	public int relocationTableOffset;

	public int mainHeaderLength;
	public int stringTableLength;
	public int gpuCommandsLength;
	public int dataLength;
	public int dataExtendedLength;
	public int relocationTableLength;
	public int uninitializedDataSectionLength;
	public int uninitializedDescriptionSectionLength;

	public short flags;
	public short addressCount;
}
