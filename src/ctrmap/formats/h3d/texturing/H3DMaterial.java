package ctrmap.formats.h3d.texturing;

import com.jogamp.opengl.GL2;
import ctrmap.formats.h3d.BCHHeader;
import ctrmap.formats.h3d.PICACommandReader;
import ctrmap.formats.h3d.RandomAccessBAIS;
import ctrmap.formats.h3d.StringUtils;
import ctrmap.formats.h3d.model.OhanaMeshUtils;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class H3DMaterial {

	public String name;

	public String name0;
	public String name1;
	public String name2;

	public H3DTexture texture0;
	public H3DTexture texture1;
	public H3DTexture texture2;

	public boolean isFragmentLightEnabled;
	public boolean isVertexLightEnabled;
	public boolean isHemiSphereLightEnabled;
	public boolean isHemiSphereOcclusionEnabled;
	public boolean isFogEnabled;

	public int lightSetIndex;
	public int fogIndex;

	public Params params = new Params();
	public List<MatMetaData> userData;

	public TextureCoordinator[] coordinators = new TextureCoordinator[3];
	public TextureMapper[] mappers = new TextureMapper[3];
	public TextureCombiner[] combiners = new TextureCombiner[6];

	public H3DMaterial(RandomAccessBAIS in, byte[] data, BCHHeader properties) throws IOException {
		int materialParametersOffset = in.readInt();
		in.readInt(); //TODO
		in.readInt();
		in.readInt();
		int textureCommandsOffset = in.readInt();
		int textureCommandsWordCount = in.readInt();

		int materialMapperOffset;
		if (properties.backwardCompatibility < 0x21) {
			materialMapperOffset = in.position;
			in.seek(0x30 + materialMapperOffset);
		} else {
			materialMapperOffset = in.readInt();
		}

		name0 = StringUtils.readString(in.readInt(), data);
		name1 = StringUtils.readString(in.readInt(), data);
		name2 = StringUtils.readString(in.readInt(), data);
		name = StringUtils.readString(in.readInt(), data);

		if (materialParametersOffset != 0) {
			in.seek(materialParametersOffset);
			int hash = in.readInt();

			short materialFlags = in.readShort();
			isFragmentLightEnabled = (materialFlags & 1) > 0;
			isVertexLightEnabled = (materialFlags & 2) > 0;
			isHemiSphereLightEnabled = (materialFlags & 4) > 0;
			isHemiSphereOcclusionEnabled = (materialFlags & 8) > 0;
			isFogEnabled = (materialFlags & 0x10) > 0;
			params.isPolygonOffsetEnabled = (materialFlags & 0x20) > 0;

			short fragmentFlags = in.readShort();
			BlendOperation.BlendMode blendMode = BlendOperation.BlendMode.values()[(fragmentFlags >> 10) & 3];

			in.readInt();

			for (int i = 0; i < 3; i++) {
				TextureCoordinator coordinator = new TextureCoordinator();
				int projectionAndCamera = in.readInt();
				coordinator.projection = TextureCoordinator.TextureProjection.values()[(projectionAndCamera >> 16) & 0xff];
				coordinator.referenceCamera = projectionAndCamera >> 24;
				coordinator.scaleU = in.readFloat();
				coordinator.scaleV = in.readFloat();
				coordinator.rotate = in.readFloat();
				coordinator.translateU = in.readFloat();
				coordinator.translateV = in.readFloat();

				coordinators[i] = coordinator;
			}

			lightSetIndex = in.readUnsignedShort();
			fogIndex = in.readUnsignedShort();

			params.colEmission = OhanaMeshUtils.getColor(in);
			params.colAmbient = OhanaMeshUtils.getColor(in);
			params.colDiffuse = OhanaMeshUtils.getColor(in);
			params.colSpecular0 = OhanaMeshUtils.getColor(in);
			params.colSpecular1 = OhanaMeshUtils.getColor(in);
			params.colConstant0 = OhanaMeshUtils.getColor(in);
			params.colConstant1 = OhanaMeshUtils.getColor(in);
			params.colConstant2 = OhanaMeshUtils.getColor(in);
			params.colConstant3 = OhanaMeshUtils.getColor(in);
			params.colConstant4 = OhanaMeshUtils.getColor(in);
			params.colConstant5 = OhanaMeshUtils.getColor(in);
			Color blendColor = OhanaMeshUtils.getColor(in);
			params.colorScale = in.readFloat();

			in.readInt();
			in.readInt();
			in.readInt();
			in.readInt();
			in.readInt();
			in.readInt();

			in.readInt();
			int position = in.position;
			in.seek(position + 24);
			in.readInt();

			params.polygonOffsetUnit = in.readFloat();
			int fshCommandsOffset = in.readInt();
			int fshCommandsWordCount = in.readInt();

			in.skip(52); //fragment shader useless shit

			in.skip(8); //shader and model references, don't need those

			//User Data
			if (properties.backwardCompatibility > 6) {
				int metaDataPointerOffset = in.readInt();
				if (metaDataPointerOffset != 0) {
					in.seek(metaDataPointerOffset);
					userData = getMetaData(in, data);
				}
			}

			//Mapper
			in.seek(materialMapperOffset);
			for (int i = 0; i < 3; i++) {
				TextureMapper mapper = new TextureMapper();
				int wrapAndMagFilter = in.readInt();
				int levelOfDetailAndMinFilter = in.readInt();
				mapper.wrapU = TextureMapper.TextureWrap.values()[(wrapAndMagFilter >> 8) & 0xff];
				mapper.wrapV = TextureMapper.TextureWrap.values()[(wrapAndMagFilter >> 16) & 0xff];
				mapper.magFilter = TextureMapper.TextureMagFilter.values()[wrapAndMagFilter >> 24];
				mapper.minFilter = TextureMapper.TextureMinFilter.values()[levelOfDetailAndMinFilter & 0xff];
				mapper.minLOD = (levelOfDetailAndMinFilter >> 8) & 0xff;
				mapper.LODBias = in.readFloat();
				mapper.borderColor = OhanaMeshUtils.getColor(in);

				mappers[i] = mapper;
			}

			//Fragment Shader commands
			in.seek(fshCommandsOffset);
			PICACommandReader fshCommands = new PICACommandReader(in, fshCommandsWordCount, false);
			for (byte stage = 0; stage < 6; stage++) {
				combiners[stage] = fshCommands.getTevStage(stage);
			}
			params.fragBufferColor = fshCommands.getFragmentBufferColor();
			params.blendop = fshCommands.getBlendOperation();
			params.blendop.mode = blendMode;
			params.blendop.blendColor = blendColor;
			params.blendop.logicalOperation = fshCommands.getColorLogicOperation();
			params.alphaTest = fshCommands.getAlphaTest();
			params.stencilTest = fshCommands.getStencilTest();
			params.depthTest = fshCommands.getDepthTest();
			params.cullMode = fshCommands.getCullMode();
		}
	}

	public static List<MatMetaData> getMetaData(RandomAccessBAIS input, byte[] data) throws IOException {
		List<MatMetaData> output = new ArrayList<>();

		int metaDataOffset = input.readInt();
		int metaDataEntries = input.readInt();
		int metaDataNameOffset = input.readInt();

		for (int index = 0; index < metaDataEntries; index++) {
			input.seek(metaDataOffset + (index * 0xc));

			MatMetaData metaData = new MatMetaData();

			metaData.name = StringUtils.readString(input.readInt(), data);
			if (metaData.name.startsWith("$")) {
				metaData.name = metaData.name.substring(1);
			}
			metaData.type = MatMetaData.MetaDataValueType.values()[input.readShort()];
			short entries = input.readShort();
			int dataOffset = input.readInt();

			input.seek(dataOffset);
			for (int i = 0; i < entries; i++) {
				switch (metaData.type) {
					case integer:
						metaData.values.add(input.readInt());
						break;
					case single:
						metaData.values.add(input.readFloat());
						break;
					case utf16String:
					case utf8String:
						int offset = input.readInt();
						int oldPosition = input.position;
						input.seek(offset);

						ByteArrayOutputStream strStream = new ByteArrayOutputStream();
						byte strChar = input.readByte();
						byte oldChar = (byte) 0xff;
						while ((metaData.type == MatMetaData.MetaDataValueType.utf8String && strChar != 0) || !(oldChar == 0 && strChar == 0)) {
							oldChar = strChar;
							strStream.write(strChar);
							strChar = input.readByte();
						}

						if (metaData.type == MatMetaData.MetaDataValueType.utf16String) {
							metaData.values.add(new String(strStream.toByteArray(), Charset.forName("UTF-16")));
						} else {
							metaData.values.add(new String(strStream.toByteArray(), Charset.forName("UTF-8")));
						}

						strStream.close();
						input.seek(oldPosition);
						break;
				}
			}

			output.add(metaData);
		}

		return output;
	}

	public static class TextureCombiner {

		public short rgbScale, alphaScale;
		public ConstantColor constantColor;
		public CombineOperator combineRgb, combineAlpha;
		public CombineSource[] rgbSource = new CombineSource[3];
		public CombineOperandRgb[] rgbOperand = new CombineOperandRgb[3];
		public CombineSource[] alphaSource = new CombineSource[3];
		public CombineOperandAlpha[] alphaOperand = new CombineOperandAlpha[3];

		public enum ConstantColor {
			constant0,
			constant1,
			constant2,
			constant3,
			constant4,
			constant5,
			emission,
			ambient,
			diffuse,
			specular0,
			specular1
		}

		public enum CombineOperator {
			replace,
			modulate,
			add,
			addSigned,
			interpolate,
			subtract,
			dot3Rgb,
			dot3Rgba,
			multiplyAdd,
			addMultiply
		}

		public enum CombineSource {
			primaryColor,
			fragmentPrimaryColor,
			fragmentSecondaryColor,
			texture0,
			texture1,
			texture2,
			texture3,
			dummy7,
			dummy8,
			dummy9,
			dummy0xa,
			dummy0xb,
			dummy0xc,
			previousBuffer,
			constant,
			previous
		}

		public enum CombineOperandRgb {
			color,
			oneMinusColor,
			alpha,
			oneMinusAlpha,
			red,
			oneMinusRed,
			dummy6,
			dummy7,
			green,
			oneMinusGreen,
			dummy0xa,
			dummy0xb,
			blue,
			oneMinusBlue
		}

		public enum CombineOperandAlpha {
			alpha,
			oneMinusAlpha,
			red,
			oneMinusRed,
			green,
			oneMinusGreen,
			blue,
			oneMinusBlue
		}
	}

	public static class TextureCoordinator {

		public TextureProjection projection;
		public int referenceCamera;
		public float scaleU, scaleV;
		public float rotate;
		public float translateU, translateV;

		public enum TextureProjection {
			uvMap,
			cameraCubeMap,
			cameraSphereMap,
			projectionMap,
			shadowMap,
			shadowCubeMap
		}
	}

	public static class TextureMapper {

		public TextureMinFilter minFilter;
		public TextureMagFilter magFilter;
		public TextureWrap wrapU, wrapV;
		public int minLOD;
		public float LODBias;
		public Color borderColor;

		public static int getGlTextureWrap(TextureWrap wrap) {
			switch (wrap) {
				default:
				case repeat:
					return GL2.GL_REPEAT;
				case mirroredRepeat:
					return GL2.GL_MIRRORED_REPEAT;
				case clampToEdge:
					return GL2.GL_CLAMP_TO_EDGE;
				case clampToBorder:
					return GL2.GL_CLAMP_TO_BORDER;
			}
		}

		public enum TextureMinFilter {
			nearest,
			nearestMipmapNearest,
			nearestMipmapLinear,
			linear,
			linearMipmapNearest,
			linearMipmapLinear
		}

		public enum TextureMagFilter {
			nearest,
			linear
		}

		public enum TextureWrap {
			clampToEdge,
			clampToBorder,
			repeat,
			mirroredRepeat
		}
	}

	public static class MatMetaData {

		public String name;
		public MetaDataValueType type;
		public List<Object> values = new ArrayList<>();

		public enum MetaDataValueType {
			integer,
			single,
			utf8String,
			utf16String
		}
	}

	public static class Params {

		public boolean isPolygonOffsetEnabled;
		public float polygonOffsetUnit;

		public BlendOperation blendop = new BlendOperation();

		public float colorScale;

		public Color fragBufferColor;
		public Color colEmission;
		public Color colAmbient;
		public Color colDiffuse;
		public Color colSpecular0;
		public Color colSpecular1;
		public Color colConstant0;
		public Color colConstant1;
		public Color colConstant2;
		public Color colConstant3;
		public Color colConstant4;
		public Color colConstant5;

		public AlphaTest alphaTest;
		public StencilOperation stencilTest;
		public DepthOperation depthTest;
		public CullMode cullMode;
	}

	public static class AlphaTest {

		public boolean isTestEnabled;
		public TestFunction testFunction;
		public int testReference;

		public int getGlTestFunc() {
			switch (testFunction) {
				case never:
					return GL2.GL_NEVER;
				case always:
					return GL2.GL_ALWAYS;
				case equal:
					return GL2.GL_EQUAL;
				case notEqual:
					return GL2.GL_NOTEQUAL;
				case less:
					return GL2.GL_LESS;
				case lessOrEqual:
					return GL2.GL_LEQUAL;
				case greater:
					return GL2.GL_GREATER;
				case greaterOrEqual:
					return GL2.GL_GEQUAL;
				default:
					return GL2.GL_ALWAYS;
			}
		}
	}

	public enum TestFunction {
		never,
		always,
		equal,
		notEqual,
		less,
		lessOrEqual,
		greater,
		greaterOrEqual
	}

	public static class StencilOperation {

		public boolean isTestEnabled;
		public TestFunction testFunction;
		public int testReference;
		public int testMask;
		public StencilOp failOperation;
		public StencilOp zFailOperation;
		public StencilOp passOperation;

		public enum StencilOp {
			keep,
			zero,
			replace,
			increase,
			decrease,
			increaseWrap,
			decreaseWrap
		}
	}

	public static class DepthOperation {

		public boolean isTestEnabled;
		public TestFunction testFunction;
		public boolean isMaskEnabled;
	}

	public enum CullMode {
		never,
		frontFace,
		backFace
	}

	public enum LogicalOperation {
		clear,
		and,
		andReverse,
		copy,
		set,
		copyInverted,
		noOperation,
		invert,
		notAnd,
		or,
		notOr,
		exclusiveOr,
		equiv,
		andInverted,
		orReverse,
		orInverted
	}

	public static class BlendOperation {

		public BlendMode mode;
		public LogicalOperation logicalOperation;
		public BlendFunction rgbFunctionSource, rgbFunctionDestination;
		public BlendEquation rgbBlendEquation;
		public BlendFunction alphaFunctionSource, alphaFunctionDestination;
		public BlendEquation alphaBlendEquation;
		public Color blendColor;

		public static int getGlBlendFunc(BlendFunction func) {
			switch (func) {
				default:
				case zero:
					return GL2.GL_ZERO;
				case one:
					return GL2.GL_ONE;
				case sourceColor:
					return GL2.GL_SRC_COLOR;
				case oneMinusSourceColor:
					return GL2.GL_ONE_MINUS_SRC_COLOR;
				case destinationColor:
					return GL2.GL_DST_COLOR;
				case oneMinusDestinationColor:
					return GL2.GL_ONE_MINUS_DST_COLOR;
				case sourceAlpha:
					return GL2.GL_SRC_ALPHA;
				case oneMinusSourceAlpha:
					return GL2.GL_ONE_MINUS_SRC_ALPHA;
				case destinationAlpha:
					return GL2.GL_DST_ALPHA;
				case oneMinusDestinationAlpha:
					return GL2.GL_ONE_MINUS_DST_ALPHA;
				case constantColor:
					return GL2.GL_CONSTANT_COLOR;
				case oneMinusConstantColor:
					return GL2.GL_ONE_MINUS_CONSTANT_COLOR;
				case constantAlpha:
					return GL2.GL_CONSTANT_ALPHA;
				case oneMinusConstantAlpha:
					return GL2.GL_ONE_MINUS_CONSTANT_ALPHA;
				case sourceAlphaSaturate:
					return GL2.GL_SRC_ALPHA_SATURATE;
			}
		}

		public static int getGlBlendEqt(BlendEquation eqt) {
			switch (eqt) {
				default:
				case add:
					return GL2.GL_FUNC_ADD;
				case subtract:
					return GL2.GL_FUNC_SUBTRACT;
				case reverseSubtract:
					return GL2.GL_FUNC_REVERSE_SUBTRACT;
				case min:
					return GL2.GL_MIN;
				case max:
					return GL2.GL_MAX;
			}
		}

		public enum BlendMode {
			logical,
			dummy,
			notUsed,
			blend
		}

		public enum BlendFunction {
			zero,
			one,
			sourceColor,
			oneMinusSourceColor,
			destinationColor,
			oneMinusDestinationColor,
			sourceAlpha,
			oneMinusSourceAlpha,
			destinationAlpha,
			oneMinusDestinationAlpha,
			constantColor,
			oneMinusConstantColor,
			constantAlpha,
			oneMinusConstantAlpha,
			sourceAlphaSaturate
		}

		public enum BlendEquation {
			add,
			subtract,
			reverseSubtract,
			min,
			max
		}
	}
}
