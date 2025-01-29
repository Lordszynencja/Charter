package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.Logger;
import log.charter.io.gp.gp7.data.GP7Note;
import log.charter.io.gp.gp7.data.GP7Note.GP7HarmonicType;

public class GP7NoteConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return GP7Note.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	private boolean readEnable(final HierarchicalStreamReader reader) {
		boolean enabled = false;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			enabled = reader.getNodeName().equals("Enable");
			reader.moveUp();
		}

		return enabled;
	}

	private String readString(final HierarchicalStreamReader reader, final String name, final String defaultValue) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (reader.getNodeName().equals(name)) {
				final String value = reader.getValue();
				reader.moveUp();
				return value;
			}
			reader.moveUp();
		}

		return defaultValue;
	}

	private int readInt(final HierarchicalStreamReader reader, final String name, final int defaultValue) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (reader.getNodeName().equals(name)) {
				final int value = Integer.valueOf(reader.getValue());
				reader.moveUp();
				return value;
			}
			reader.moveUp();
		}

		return defaultValue;
	}

	private double readDouble(final HierarchicalStreamReader reader, final String name, final int defaultValue) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (reader.getNodeName().equals(name)) {
				final double value = Double.valueOf(reader.getValue());
				reader.moveUp();
				return value;
			}
			reader.moveUp();
		}

		return defaultValue;
	}

	private String getNodeString(final HierarchicalStreamReader reader, final int level) {
		String node = "  ".repeat(level) + "<" + reader.getNodeName();
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			node += " " + reader.getAttributeName(i) + "=\"" + reader.getAttribute(i) + "\"";
		}
		final boolean multiline = reader.hasMoreChildren();
		node += ">";
		if (!reader.getValue().isBlank()) {
			node += (multiline ? "\n" + "  ".repeat(level) : "") + reader.getValue();
		}

		if (multiline) {
			node += "\n";
		}
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			node += getNodeString(reader, level + 1) + "\n";
			reader.moveUp();
		}
		node += (multiline ? "  ".repeat(level) : "") + "</" + reader.getNodeName() + ">";

		return node;
	}

	private void mapProperties(final HierarchicalStreamReader reader, final GP7Note gp7Note) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			switch (reader.getAttribute("name")) {
				case "String" -> { gp7Note.string = readInt(reader, "String", 0); }
				case "Fret" -> { gp7Note.fret = readInt(reader, "Fret", 0); }
				case "HopoOrigin" -> { gp7Note.hopoOrigin = readEnable(reader); }
				case "HopoDestination" -> { gp7Note.hopoDestination = readEnable(reader); }
				case "LeftHandTapped" -> { gp7Note.leftHandTapped = readEnable(reader); }
				case "Tapped" -> { gp7Note.tapped = readEnable(reader); }
				case "Muted" -> { gp7Note.mute = readEnable(reader); }
				case "PalmMuted" -> { gp7Note.palmMute = readEnable(reader); }
				case "Popped" -> { gp7Note.popped = readEnable(reader); }
				case "Slapped" -> { gp7Note.slapped = readEnable(reader); }
				case "Harmonic" -> { gp7Note.harmonic = readEnable(reader); }
				case "HarmonicFret" -> { gp7Note.harmonicFret = readDouble(reader, "HFret", 12); }
				case "HarmonicType" ->
					{ gp7Note.harmonicType = GP7HarmonicType.valueOfWithCheck(readString(reader, "HType", "")); }
				case "Slide" -> { gp7Note.slideFlag = readInt(reader, "Flags", -1); }
				case "Bended" -> { gp7Note.bend = readEnable(reader); }
				case "BendOriginOffset" -> { gp7Note.bendOriginOffset = readDouble(reader, "Float", 0); }
				case "BendOriginValue" -> { gp7Note.bendOriginValue = readDouble(reader, "Float", 0); }
				case "BendMiddleOffset1" -> { gp7Note.bendMiddleOffset1 = readDouble(reader, "Float", 0); }
				case "BendMiddleOffset2" -> { gp7Note.bendMiddleOffset2 = readDouble(reader, "Float", 0); }
				case "BendMiddleValue" -> { gp7Note.bendMiddleValue = readDouble(reader, "Float", 0); }
				case "BendDestinationOffset" -> { gp7Note.bendDestinationOffset = readDouble(reader, "Float", 0); }
				case "BendDestinationValue" -> { gp7Note.bendDestinationValue = readDouble(reader, "Float", 0); }
				case "ConcertPitch" -> {}
				case "Midi" -> {}
				case "TransposedPitch" -> {}
				default -> {
					Logger.info("Unknown GP7Note property " + reader.getAttribute("name") + ":\n"
							+ getNodeString(reader, 0));
				}
			}
			reader.moveUp();
		}
	}

	private void mapLeftFingering(final HierarchicalStreamReader reader, final GP7Note gp7Note) {
		gp7Note.finger = switch (reader.getValue()) {
			case "I" -> 1;
			case "M" -> 2;
			case "C" -> 3;
			case "A" -> 4;
			case "T" -> 0;
			default -> throw new IllegalArgumentException("Unexpected value: " + reader.getValue());
		};
	}

	private void mapTie(final HierarchicalStreamReader reader, final GP7Note gp7Note) {
		gp7Note.tieOrigin = reader.getAttribute("origin").equals("true");
		gp7Note.tieDestination = reader.getAttribute("destination").equals("true");
	}

	private void mappingUnknownField(final HierarchicalStreamReader reader) {
		Logger.error("Unknown GP7Note field " + reader.getNodeName() + ":\n" + getNodeString(reader, 0));
	}

	@Override
	public GP7Note unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final GP7Note gp7Note = new GP7Note();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			switch (reader.getNodeName()) {
				case "Properties" -> mapProperties(reader, gp7Note);
				case "Accent" -> { gp7Note.accent = "1".equals(reader.getValue()); }
				case "LeftFingering" -> mapLeftFingering(reader, gp7Note);
				case "Tie" -> mapTie(reader, gp7Note);
				case "Vibrato" -> { gp7Note.vibrato = true; }
				case "AntiAccent" -> {}
				case "InstrumentArticulation" -> {}
				case "LetRing" -> {}
				default -> mappingUnknownField(reader);
			}
			reader.moveUp();
		}

		return gp7Note;
	}

}
