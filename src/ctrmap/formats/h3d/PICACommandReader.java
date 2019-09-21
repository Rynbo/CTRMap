package ctrmap.formats.h3d;

import ctrmap.formats.h3d.texturing.H3DMaterial;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Port of the Ohana class
 */
public class PICACommandReader {

	ArrayList<ArrayList<Float>> floatUniform = new ArrayList<>(96);
	List<Float> uniform = new ArrayList<>();

	float[] lookUpTable = new float[256];
	int lutIndex;

	private long[] commands = new long[0x10000];
	long currentUniform;

	public PICACommandReader(RandomAccessBAIS input, int wordCount, boolean ignoreAlign) throws IOException {
		int readWords = 0;
		while (readWords < wordCount) {
			long parameter = Integer.toUnsignedLong(input.readInt());
			long header = Integer.toUnsignedLong(input.readInt());
			readWords += 2;

			int id = (int) (header & 0xffff);
			long mask = (header >> 16) & 0xf;
			long extraParameters = (header >> 20) & 0x7ff;
			boolean consecutiveWriting = (header & 0x80000000) > 0;

			commands[id] = (getParameter(id) & (~mask & 0xf)) | (parameter & (0xfffffff0 | mask));
			if (id == PICACommand.blockEnd) {
				break;
			} else if (id == PICACommand.vertexShaderFloatUniformConfig) {
				currentUniform = parameter & 0x7fffffff;
			} else if (id == PICACommand.vertexShaderFloatUniformData) {
				uniform.add(toFloat((int) commands[id]));
			} else if (id == PICACommand.fragmentShaderLookUpTableData) {
				lookUpTable[lutIndex++] = commands[id];
			}
			for (int i = 0; i < extraParameters; i++) {
				if (consecutiveWriting) {
					id++;
				}
				commands[id] = (getParameter(id) & (~mask & 0xf)) | (input.readInt() & (0xfffffff0 | mask));
				readWords++;

				if (id > PICACommand.vertexShaderFloatUniformConfig && id < PICACommand.vertexShaderFloatUniformData + 8) {
					uniform.add(toFloat((int) commands[id]));
				} else if (id == PICACommand.fragmentShaderLookUpTableData) {
					lookUpTable[lutIndex++] = commands[id];
				}
			}

			if (uniform.size() > 0) {
				while (floatUniform.size() <= currentUniform) {
					floatUniform.add(new ArrayList<>());
				}
				floatUniform.get((int) currentUniform).addAll(uniform);
				uniform.clear();
			}
			lutIndex = 0;

			if (!ignoreAlign) {
				while ((input.position & 7) != 0) {
					input.readInt(); //Ignore 0x0 padding Words
				}
			}
		}
	}

	/// <summary>
	///     Gets the lastest written parameter of a given Command in the buffer.
	/// </summary>
	/// <param name="commandId">ID code of the command</param>
	/// <returns></returns>
	public long getParameter(int commandId) {
		return commands[commandId];
	}

	/// <summary>
	///     Converts a IEEE 754 encoded float on int to float.
	/// </summary>
	/// <param name="value"></param>
	/// <returns></returns>
	private float toFloat(int value) {
		byte[] buffer = new byte[4];
		buffer[0] = (byte) (value & 0xff);
		buffer[1] = (byte) ((value >> 8) & 0xff);
		buffer[2] = (byte) ((value >> 16) & 0xff);
		buffer[3] = (byte) ((value >> 24) & 0xff);
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();
	}

	/// <summary>
	///     Gets the Total Attributes minus 1.
	/// </summary>
	/// <returns></returns>
	public long getVSHTotalAttributes() {
		return getParameter(PICACommand.vertexShaderTotalAttributes);
	}

	/// <summary>
	///     Gets an array containing all Vertex Atrributes permutation order.
	/// </summary>
	/// <returns></returns>
	public PICACommand.vshAttribute[] getVSHAttributesBufferPermutation() {
		long permutation = getParameter(PICACommand.vertexShaderAttributesPermutationLow);
		permutation |= getParameter(PICACommand.vertexShaderAttributesPermutationHigh) << 32;

		PICACommand.vshAttribute[] attributes = new PICACommand.vshAttribute[23];
		for (int attribute = 0; attribute < attributes.length; attribute++) {
			attributes[attribute] = PICACommand.vshAttribute.values()[(int) ((permutation >> (attribute * 4)) & 0xf)];
		}
		return attributes;
	}

	/// <summary>
	///     Gets the Address where the Attributes Buffer is located.
	///     Note that it may be a Relative address, and may need to be relocated.
	/// </summary>
	/// <returns></returns>
	public long getVSHAttributesBufferAddress() {
		return getParameter(PICACommand.vertexShaderAttributesBufferAddress);
	}

	/// <summary>
	///     Gets an array containing all formats of the main Attributes Buffer.
	///     It have the length of each attribute, and the format (byte, float, short...).
	/// </summary>
	/// <returns></returns>
	public PICACommand.attributeFormat[] getVSHAttributesBufferFormat() {
		long format = getParameter(PICACommand.vertexShaderAttributesBufferFormatLow);
		format |= getParameter(PICACommand.vertexShaderAttributesBufferFormatHigh) << 32;

		PICACommand.attributeFormat[] formats = new PICACommand.attributeFormat[23];
		for (int attribute = 0; attribute < formats.length; attribute++) {
			byte value = (byte) ((format >> (attribute * 4)) & 0xf);
			formats[attribute] = new PICACommand.attributeFormat();
			formats[attribute].type = PICACommand.attributeFormatType.values()[(value & 3)];
			formats[attribute].attributeLength = value >> 2;
		}
		return formats;
	}

	/// <summary>
	///     Gets the Address of a given Attributes Buffer.
	///     It is relative to the Main Buffer address.
	/// </summary>
	/// <param name="bufferIndex">Index number of the buffer (0-11)</param>
	/// <returns></returns>
	public long getVSHAttributesBufferAddress(int bufferIndex) {
		return getParameter((short) (PICACommand.vertexShaderAttributesBuffer0Address + (bufferIndex * 3)));
	}

	/// <summary>
	///     Gets the Total Attributes of a given Attributes Buffer.
	/// </summary>
	/// <param name="bufferIndex">Index number of the buffer (0-11)</param>
	/// <returns></returns>
	public long getVSHTotalAttributes(int bufferIndex) {
		long value = getParameter((short) (PICACommand.vertexShaderAttributesBuffer0Stride + (bufferIndex * 3)));
		return value >> 28;
	}

	/// <summary>
	///     Gets Uniform Booleans used on Vertex Shader.
	/// </summary>
	/// <returns></returns>
	public boolean[] getVSHBooleanUniforms() {
		boolean[] output = new boolean[16];

		long value = getParameter(PICACommand.vertexShaderBooleanUniforms);
		for (int i = 0; i < 16; i++) {
			output[i] = (value & (1 << i)) > 0;
		}
		return output;
	}

	/// <summary>
	///     Gets the Permutation of a given Attributes Buffer.
	///     Values corresponds to a value on the main Permutation.
	/// </summary>
	/// <param name="bufferIndex">Index number of the buffer (0-11)</param>
	/// <returns></returns>
	public int[] getVSHAttributesBufferPermutation(int bufferIndex) {
		long permutation = getParameter((short) (PICACommand.vertexShaderAttributesBuffer0Permutation + (bufferIndex * 3)));
		permutation |= (getParameter((short) (PICACommand.vertexShaderAttributesBuffer0Stride + (bufferIndex * 3))) & 0xffff) << 32;

		int[] attributes = new int[23];
		for (int attribute = 0; attribute < attributes.length; attribute++) {
			attributes[attribute] = (int) ((permutation >> (attribute * 4)) & 0xf);
		}
		return attributes;
	}

	/// <summary>
	///     Gets the Stride of a given Attributes Buffer.
	/// </summary>
	/// <param name="bufferIndex">Index number of the buffer (0-11)</param>
	/// <returns></returns>
	public byte getVSHAttributesBufferStride(int bufferIndex) {
		long value = getParameter((short) (PICACommand.vertexShaderAttributesBuffer0Stride + (bufferIndex * 3)));
		return (byte) ((value >> 16) & 0xff);
	}

	/// <summary>
	///     Gets the Float Uniform data array from the given register.
	/// </summary>
	/// <param name="register">Index number of the register (observed values: 6 and 7)</param>
	/// <returns></returns>
	public Stack<Float> getVSHFloatUniformData(int register) {
		Stack<Float> data = new Stack<>();
		floatUniform.get(register).forEach((value) -> {
			data.push(value);
		});
		return data;
	}

	/// <summary>
	///     Gets the Address where the Index Buffer is located.
	/// </summary>
	/// <returns></returns>
	public long getIndexBufferAddress() {
		return getParameter(PICACommand.indexBufferConfig) & 0x7fffffff;
	}

	/// <summary>
	///     Gets the Format of the Index Buffer (byte or short).
	/// </summary>
	/// <returns></returns>
	public PICACommand.indexBufferFormat getIndexBufferFormat() {
		return PICACommand.indexBufferFormat.values()[(int) (getParameter(PICACommand.indexBufferConfig) >> 31)];
	}

	/// <summary>
	///     Gets the total number of vertices indexed by the Index Buffer.
	/// </summary>
	/// <returns></returns>
	public long getIndexBufferTotalVertices() {
		return getParameter(PICACommand.indexBufferTotalVertices);
	}

	
	/// <summary>
	///     Gets TEV Stage parameters.
	/// </summary>
	/// <param name="stage">The stage (0-5)</param>
	/// <returns></returns>
	public H3DMaterial.TextureCombiner getTevStage(byte stage) {
		H3DMaterial.TextureCombiner output = new H3DMaterial.TextureCombiner();

		short baseCommand;
		switch (stage) {
			case 0:
				baseCommand = PICACommand.tevStage0Source;
				break;
			case 1:
				baseCommand = PICACommand.tevStage1Source;
				break;
			case 2:
				baseCommand = PICACommand.tevStage2Source;
				break;
			case 3:
				baseCommand = PICACommand.tevStage3Source;
				break;
			case 4:
				baseCommand = PICACommand.tevStage4Source;
				break;
			case 5:
				baseCommand = PICACommand.tevStage5Source;
				break;
			default:
				return null;
		}

		//Source
		int source = (int)getParameter(baseCommand);

		output.rgbSource[0] = H3DMaterial.TextureCombiner.CombineSource.values()[source & 0xf];
		output.rgbSource[1] = H3DMaterial.TextureCombiner.CombineSource.values()[(source >> 4) & 0xf];
		output.rgbSource[2] = H3DMaterial.TextureCombiner.CombineSource.values()[(source >> 8) & 0xf];

		output.alphaSource[0] = H3DMaterial.TextureCombiner.CombineSource.values()[(source >> 16) & 0xf];
		output.alphaSource[1] = H3DMaterial.TextureCombiner.CombineSource.values()[(source >> 20) & 0xf];
		output.alphaSource[2] = H3DMaterial.TextureCombiner.CombineSource.values()[(source >> 24) & 0xf];

		//Operand
		int operand = (int)getParameter((short) (baseCommand + 1));

		output.rgbOperand[0] = H3DMaterial.TextureCombiner.CombineOperandRgb.values()[operand & 0xf];
		output.rgbOperand[1] = H3DMaterial.TextureCombiner.CombineOperandRgb.values()[(operand >> 4) & 0xf];
		output.rgbOperand[2] = H3DMaterial.TextureCombiner.CombineOperandRgb.values()[(operand >> 8) & 0xf];

		output.alphaOperand[0] = H3DMaterial.TextureCombiner.CombineOperandAlpha.values()[(operand >> 12) & 0xf];
		output.alphaOperand[1] = H3DMaterial.TextureCombiner.CombineOperandAlpha.values()[(operand >> 16) & 0xf];
		output.alphaOperand[2] = H3DMaterial.TextureCombiner.CombineOperandAlpha.values()[(operand >> 20) & 0xf];

		//Operator
		int combine = (int)getParameter((short) (baseCommand + 2));

		output.combineRgb = H3DMaterial.TextureCombiner.CombineOperator.values()[combine & 0xffff];
		output.combineAlpha = H3DMaterial.TextureCombiner.CombineOperator.values()[combine >> 16];

		//Scale
		int scale = (int)getParameter((short) (baseCommand + 4));

		output.rgbScale = (short) ((scale & 0xffff) + 1);
		output.alphaScale = (short) ((scale >> 16) + 1);

		return output;
	}
	
	/// <summary>
	///     Gets the Fragment Buffer Color.
	/// </summary>
	/// <returns></returns>
	public Color getFragmentBufferColor() {
		int rgba = (int) getParameter(PICACommand.fragmentBufferColor);
		return new Color(
				rgba & 0xff,
				(rgba >> 8) & 0xff,
				(rgba >> 16) & 0xff,
				rgba >> 24 & 0xff
		);
	}

	
	/// <summary>
	///     Gets Blending operation parameters.
	/// </summary>
	/// <returns></returns>
	public H3DMaterial.BlendOperation getBlendOperation() {
		H3DMaterial.BlendOperation output = new H3DMaterial.BlendOperation();

		int value = (int)getParameter(PICACommand.blendConfig);
		output.rgbFunctionSource = H3DMaterial.BlendOperation.BlendFunction.values()[(value >> 16) & 0xf];
		output.rgbFunctionDestination = H3DMaterial.BlendOperation.BlendFunction.values()[(value >> 20) & 0xf];
		output.alphaFunctionSource = H3DMaterial.BlendOperation.BlendFunction.values()[(value >> 24) & 0xf];
		output.alphaFunctionDestination = H3DMaterial.BlendOperation.BlendFunction.values()[(value >> 28) & 0xf];
		output.rgbBlendEquation = H3DMaterial.BlendOperation.BlendEquation.values()[value & 0xff];
		output.alphaBlendEquation = H3DMaterial.BlendOperation.BlendEquation.values()[(value >> 8) & 0xff];

		return output;
	}
	
 
	/// <summary>
	///     Gets the Logical operation applied to Fragment colors.
	/// </summary>
	/// <returns></returns>
	public H3DMaterial.LogicalOperation getColorLogicOperation() {
		return H3DMaterial.LogicalOperation.values()[(int)getParameter(PICACommand.colorLogicOperationConfig) & 0xf];
	}
	 
 
	/// <summary>
	///     Gets the parameters used for Alpha testing.
	/// </summary>
	/// <returns></returns>
	public H3DMaterial.AlphaTest getAlphaTest() {
		H3DMaterial.AlphaTest output = new H3DMaterial.AlphaTest();

		int value = (int)getParameter(PICACommand.alphaTestConfig);
		output.isTestEnabled = (value & 1) > 0;
		output.testFunction = H3DMaterial.TestFunction.values()[(value >> 4) & 0xf];
		output.testReference = ((value >> 8) & 0xff);

		return output;
	}
	 
 
	/// <summary>
	///     Gets the parameters used for Stencil testing.
	/// </summary>
	/// <returns></returns>
	public H3DMaterial.StencilOperation getStencilTest() {
		H3DMaterial.StencilOperation output = new H3DMaterial.StencilOperation();

		//Test
		int test = (int)getParameter(PICACommand.stencilTestConfig);

		output.isTestEnabled = (test & 1) > 0;
		output.testFunction = H3DMaterial.TestFunction.values()[(test >> 4) & 0xf];
		output.testReference = (test >> 16) & 0xff;
		output.testMask = (test >> 24);

		//Operation
		int operation = (int)getParameter(PICACommand.stencilOperationConfig);

		output.failOperation = H3DMaterial.StencilOperation.StencilOp.values()[operation & 0xf];
		output.zFailOperation = H3DMaterial.StencilOperation.StencilOp.values()[(operation >> 4) & 0xf];
		output.passOperation = H3DMaterial.StencilOperation.StencilOp.values()[(operation >> 8) & 0xf];

		return output;
	}
	 
 
	/// <summary>
	///     Gets the parameters used for Depth testing.
	/// </summary>
	/// <returns></returns>
	public H3DMaterial.DepthOperation getDepthTest() {
		H3DMaterial.DepthOperation output = new H3DMaterial.DepthOperation();

		int value = (int)getParameter(PICACommand.depthTestConfig);
		output.isTestEnabled = (value & 1) > 0;
		output.testFunction = H3DMaterial.TestFunction.values()[(value >> 4) & 0xf];
		output.isMaskEnabled = (value & 0x1000) > 0;

		return output;
	}
	 
 
	/// <summary>
	///     Gets the Culling mode.
	/// </summary>
	/// <returns></returns>
	public H3DMaterial.CullMode getCullMode() {
		int value = (int)getParameter(PICACommand.cullModeConfig);
		return H3DMaterial.CullMode.values()[value & 0xf];
	}
	 
	/// <summary>
	///     Gets the Address of the texture at Texture Unit 0.
	/// </summary>
	/// <returns></returns>
	public int getTexUnit0Address() {
		return (int) getParameter(PICACommand.texUnit0Address);
	}

	/*
	/// <summary>
	///     Gets the mapping parameters used on Texture Unit 0,
	///     such as the wrapping mode, filtering and so on.
	/// </summary>
	/// <returns></returns>
	public RenderBase.OTextureMapper getTexUnit0Mapper() {
		RenderBase.OTextureMapper output = new RenderBase.OTextureMapper();

		int value = getParameter(PICACommand.texUnit0Param);
		output.magFilter = (RenderBase.OTextureMagFilter) ((value >> 1) & 1);
		output.minFilter = (RenderBase.OTextureMinFilter) (((value >> 2) & 1) | ((value >> 23) & 2));
		output.wrapU = (RenderBase.OTextureWrap) ((value >> 12) & 0xf);
		output.wrapV = (RenderBase.OTextureWrap) ((value >> 8) & 0xf);

		return output;
	}
	 */
	/// <summary>
	///     Gets the border color used on Texture Unit 0,
	///     when the wrapping mode is set to Border.
	/// </summary>
	/// <returns></returns>
	public Color getTexUnit0BorderColor() {
		int rgba = (int) getParameter(PICACommand.texUnit0BorderColor);
		return new Color(
				(byte) (rgba & 0xff),
				(byte) ((rgba >> 8) & 0xff),
				(byte) ((rgba >> 16) & 0xff),
				(byte) (rgba >> 24)
		);
	}

	/// <summary>
	///     Gets the resolution of the texture at Texture Unit 0.
	/// </summary>
	/// <returns></returns>
	public Dimension getTexUnit0Size() {
		long value = getParameter(PICACommand.texUnit0Size);
		return new Dimension((int) (value >> 16), (int) (value & 0xffff));
	}

	/// <summary>
	///     Gets the encoded format of the texture at Texture Unit 0.
	/// </summary>
	public TextureFormat getTexUnit0Format() {
		return TextureFormat.values()[(int)getParameter(PICACommand.texUnit0Type)];
	}

	/// <summary>
	///     Gets the Address of the texture at Texture Unit 1.
	/// </summary>
	/// <returns></returns>
	public long getTexUnit1Address() {
		return getParameter(PICACommand.texUnit1Address);
	}

	/*
	/// <summary>
	///     Gets the mapping parameters used on Texture Unit 1,
	///     such as the wrapping mode, filtering and so on.
	/// </summary>
	/// <returns></returns>
	public RenderBase.OTextureMapper getTexUnit1Mapper() {
		RenderBase.OTextureMapper output = new RenderBase.OTextureMapper();

		int value = getParameter(PICACommand.texUnit1Param);
		output.magFilter = (RenderBase.OTextureMagFilter) ((value >> 1) & 1);
		output.minFilter = (RenderBase.OTextureMinFilter) (((value >> 2) & 1) | ((value >> 23) & 2));
		output.wrapU = (RenderBase.OTextureWrap) ((value >> 12) & 0xf);
		output.wrapV = (RenderBase.OTextureWrap) ((value >> 8) & 0xf);

		return output;
	}
	 */
	/// <summary>
	///     Gets the border color used on Texture Unit 1,
	///     when the wrapping mode is set to Border.
	/// </summary>
	/// <returns></returns>
	public Color getTexUnit1BorderColor() {
		int rgba = (int) getParameter(PICACommand.texUnit1BorderColor);
		return new Color(
				(byte) (rgba & 0xff),
				(byte) ((rgba >> 8) & 0xff),
				(byte) ((rgba >> 16) & 0xff),
				(byte) (rgba >> 24)
		);
	}

	/// <summary>
	///     Gets the resolution of the texture at Texture Unit 1.
	/// </summary>
	/// <returns></returns>
	public Dimension getTexUnit1Size() {
		long value = getParameter(PICACommand.texUnit1Size);
		return new Dimension((int) (value >> 16), (int) (value & 0xffff));
	}

	/*
	/// <summary>
	///     Gets the encoded format of the texture at Texture Unit 1.
	/// </summary>
	public RenderBase.OTextureFormat getTexUnit1Format() {
		return (RenderBase.OTextureFormat) getParameter(PICACommand.texUnit1Type);
	}
	 */
	/// <summary>
	///     Gets the Address of the texture at Texture Unit 2.
	/// </summary>
	/// <returns></returns>
	public long getTexUnit2Address() {
		return getParameter(PICACommand.texUnit2Address);
	}

	/*
	/// <summary>
	///     Gets the mapping parameters used on Texture Unit 2,
	///     such as the wrapping mode, filtering and so on.
	/// </summary>
	/// <returns></returns>
	public RenderBase.OTextureMapper getTexUnit2Mapper() {
		RenderBase.OTextureMapper output = new RenderBase.OTextureMapper();

		int value = getParameter(PICACommand.texUnit2Param);
		output.magFilter = (RenderBase.OTextureMagFilter) ((value >> 1) & 1);
		output.minFilter = (RenderBase.OTextureMinFilter) (((value >> 2) & 1) | ((value >> 23) & 2));
		output.wrapU = (RenderBase.OTextureWrap) ((value >> 12) & 0xf);
		output.wrapV = (RenderBase.OTextureWrap) ((value >> 8) & 0xf);

		return output;
	}
	 */
	/// <summary>
	///     Gets the border color used on Texture Unit 2,
	///     when the wrapping mode is set to Border.
	/// </summary>
	/// <returns></returns>
	public Color getTexUnit2BorderColor() {
		int rgba = (int) getParameter(PICACommand.texUnit2BorderColor);
		return new Color(
				(byte) (rgba & 0xff),
				(byte) ((rgba >> 8) & 0xff),
				(byte) ((rgba >> 16) & 0xff),
				(byte) (rgba >> 24)
		);
	}

	/// <summary>
	///     Gets the resolution of the texture at Texture Unit 2.
	/// </summary>
	/// <returns></returns>
	public Dimension getTexUnit2Size() {
		long value = getParameter(PICACommand.texUnit2Size);
		return new Dimension((int) (value >> 16), (int) (value & 0xffff));
	}

	/*
	/// <summary>
	///     Gets the encoded format of the texture at Texture Unit 2.
	/// </summary>
	public RenderBase.OTextureFormat getTexUnit2Format() {
		return (RenderBase.OTextureFormat) getParameter(PICACommand.texUnit2Type);
	}
	 */
	public enum TextureFormat {
		rgba8,
		rgb8,
		rgba5551,
		rgb565,
		rgba4,
		la8,
		hilo8,
		l8,
		a8,
		la4,
		l4,
		a4,
		etc1,
		etc1a4,
		dontCare
	}
}
