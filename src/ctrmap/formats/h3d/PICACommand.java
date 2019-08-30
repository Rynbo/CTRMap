package ctrmap.formats.h3d;

public class PICACommand {

	public static final short culling = 0x40;
	public static final short polygonOffsetEnable = 0x4c;
	public static final short polygonOffsetZScale = 0x4d;
	public static final short polygonOffsetZBias = 0x4e;
	public static final short texUnitsConfig = 0x80;
	public static final short texUnit0BorderColor = 0x81;
	public static final short texUnit0Size = 0x82;
	public static final short texUnit0Param = 0x83;
	public static final short texUnit0LevelOfDetail = 0x84;
	public static final short texUnit0Address = 0x85;
	public static final short texUnit0Type = 0x8e;
	public static final short texUnit1BorderColor = 0x91;
	public static final short texUnit1Size = 0x92;
	public static final short texUnit1Param = 0x93;
	public static final short texUnit1LevelOfDetail = 0x94;
	public static final short texUnit1Address = 0x95;
	public static final short texUnit1Type = 0x96;
	public static final short texUnit2BorderColor = 0x99;
	public static final short texUnit2Size = 0x9a;
	public static final short texUnit2Param = 0x9b;
	public static final short texUnit2LevelOfDetail = 0x9c;
	public static final short texUnit2Address = 0x9d;
	public static final short texUnit2Type = 0x9e;
	public static final short tevStage0Source = 0xc0;
	public static final short tevStage0Operand = 0xc1;
	public static final short tevStage0Combine = 0xc2;
	public static final short tevStage0Constant = 0xc3;
	public static final short tevStage0Scale = 0xc4;
	public static final short tevStage1Source = 0xc8;
	public static final short tevStage1Operand = 0xc9;
	public static final short tevStage1Combine = 0xca;
	public static final short tevStage1Constant = 0xcb;
	public static final short tevStage1Scale = 0xcc;
	public static final short tevStage2Source = 0xd0;
	public static final short tevStage2Operand = 0xd1;
	public static final short tevStage2Combine = 0xd2;
	public static final short tevStage2Constant = 0xd3;
	public static final short tevStage2Scale = 0xd4;
	public static final short tevStage3Source = 0xd8;
	public static final short tevStage3Operand = 0xd9;
	public static final short tevStage3Combine = 0xda;
	public static final short tevStage3Constant = 0xdb;
	public static final short tevStage3Scale = 0xdc;
	public static final short fragmentBufferInput = 0xe0;
	public static final short tevStage4Source = 0xf0;
	public static final short tevStage4Operand = 0xf1;
	public static final short tevStage4Combine = 0xf2;
	public static final short tevStage4Constant = 0xf3;
	public static final short tevStage4Scale = 0xf4;
	public static final short tevStage5Source = 0xf8;
	public static final short tevStage5Operand = 0xf9;
	public static final short tevStage5Combine = 0xfa;
	public static final short tevStage5Constant = 0xfb;
	public static final short tevStage5Scale = 0xfc;
	public static final short fragmentBufferColor = 0xfd;
	public static final short colorOutputConfig = 0x100;
	public static final short blendConfig = 0x101;
	public static final short colorLogicOperationConfig = 0x102;
	public static final short blendColor = 0x103;
	public static final short alphaTestConfig = 0x104;
	public static final short stencilTestConfig = 0x105;
	public static final short stencilOperationConfig = 0x106;
	public static final short depthTestConfig = 0x107;
	public static final short cullModeConfig = 0x108;
	public static final short frameBufferInvalidate = 0x110;
	public static final short frameBufferFlush = 0x111;
	public static final short colorBufferRead = 0x112;
	public static final short colorBufferWrite = 0x113;
	public static final short depthBufferRead = 0x114;
	public static final short depthBufferWrite = 0x115;
	public static final short depthTestConfig2 = 0x126;
	public static final short fragmentShaderLookUpTableConfig = 0x1c5;
	public static final short fragmentShaderLookUpTableData = 0x1c8;
	public static final short lutSamplerAbsolute = 0x1d0;
	public static final short lutSamplerInput = 0x1d1;
	public static final short lutSamplerScale = 0x1d2;
	public static final short vertexShaderAttributesBufferAddress = 0x200;
	public static final short vertexShaderAttributesBufferFormatLow = 0x201;
	public static final short vertexShaderAttributesBufferFormatHigh = 0x202;
	public static final short vertexShaderAttributesBuffer0Address = 0x203;
	public static final short vertexShaderAttributesBuffer0Permutation = 0x204;
	public static final short vertexShaderAttributesBuffer0Stride = 0x205;
	public static final short vertexShaderAttributesBuffer1Address = 0x206;
	public static final short vertexShaderAttributesBuffer1Permutation = 0x207;
	public static final short vertexShaderAttributesBuffer1Stride = 0x208;
	public static final short vertexShaderAttributesBuffer2Address = 0x209;
	public static final short vertexShaderAttributesBuffer2Permutation = 0x20a;
	public static final short vertexShaderAttributesBuffer2Stride = 0x20b;
	public static final short vertexShaderAttributesBuffer3Address = 0x20c;
	public static final short vertexShaderAttributesBuffer3Permutation = 0x20d;
	public static final short vertexShaderAttributesBuffer3Stride = 0x20e;
	public static final short vertexShaderAttributesBuffer4Address = 0x20f;
	public static final short vertexShaderAttributesBuffer4Permutation = 0x210;
	public static final short vertexShaderAttributesBuffer4Stride = 0x211;
	public static final short vertexShaderAttributesBuffer5Address = 0x212;
	public static final short vertexShaderAttributesBuffer5Permutation = 0x213;
	public static final short vertexShaderAttributesBuffer5Stride = 0x214;
	public static final short vertexShaderAttributesBuffer6Address = 0x215;
	public static final short vertexShaderAttributesBuffer6Permutation = 0x216;
	public static final short vertexShaderAttributesBuffer6Stride = 0x217;
	public static final short vertexShaderAttributesBuffer7Address = 0x218;
	public static final short vertexShaderAttributesBuffer7Permutation = 0x219;
	public static final short vertexShaderAttributesBuffer7Stride = 0x21a;
	public static final short vertexShaderAttributesBuffer8Address = 0x21b;
	public static final short vertexShaderAttributesBuffer8Permutation = 0x21c;
	public static final short vertexShaderAttributesBuffer8Stride = 0x21d;
	public static final short vertexShaderAttributesBuffer9Address = 0x21e;
	public static final short vertexShaderAttributesBuffer9Permutation = 0x21f;
	public static final short vertexShaderAttributesBuffer9Stride = 0x220;
	public static final short vertexShaderAttributesBuffer10Address = 0x221;
	public static final short vertexShaderAttributesBuffer10Permutation = 0x222;
	public static final short vertexShaderAttributesBuffer10Stride = 0x223;
	public static final short vertexShaderAttributesBuffer11Address = 0x224;
	public static final short vertexShaderAttributesBuffer11Permutation = 0x225;
	public static final short vertexShaderAttributesBuffer11Stride = 0x226;
	public static final short indexBufferConfig = 0x227;
	public static final short indexBufferTotalVertices = 0x228;
	public static final short blockEnd = 0x23d;
	public static final short vertexShaderTotalAttributes = 0x242;
	public static final short vertexShaderBooleanUniforms = 0x2b0;
	public static final short vertexShaderIntegerUniforms0 = 0x2b1;
	public static final short vertexShaderIntegerUniforms1 = 0x2b2;
	public static final short vertexShaderIntegerUniforms2 = 0x2b3;
	public static final short vertexShaderIntegerUniforms3 = 0x2b4;
	public static final short vertexShaderInputBufferConfig = 0x2b9;
	public static final short vertexShaderEntryPoint = 0x2ba;
	public static final short vertexShaderAttributesPermutationLow = 0x2bb;
	public static final short vertexShaderAttributesPermutationHigh = 0x2bc;
	public static final short vertexShaderOutmapMask = 0x2bd;
	public static final short vertexShaderCodeTransferEnd = 0x2bf;
	public static final short vertexShaderFloatUniformConfig = 0x2c0;
	public static final short vertexShaderFloatUniformData = 0x2c1;

	public static enum vshAttribute {
		position,
		normal,
		tangent,
		color,
		textureCoordinate0,
		textureCoordinate1,
		textureCoordinate2,
		boneIndex,
		boneWeight,
		userAttribute0,
		userAttribute1,
		userAttribute2,
		userAttribute3,
		userAttribute4,
		userAttribute5,
		userAttribute6,
		userAttribute7,
		userAttribute8,
		userAttribute9,
		userAttribute10,
		userAttribute11,
		interleave,
		quantity
	}

	public static enum attributeFormatType {
		signedByte,
		unsignedByte,
		signedShort,
		single
	}

	public static class attributeFormat {

		public attributeFormatType type;
		public int attributeLength;
	}

	public static enum indexBufferFormat {
		unsignedByte,
		unsignedShort
	}
}
