package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;

public class VocalConverter implements Converter {
	public static class TemporaryVocal extends Vocal {
		private final int position;
		private final int endPosition;

		public TemporaryVocal(final int position, final int endPosition) {
			this.position = position;
			this.endPosition = endPosition;
		}

		public Vocal transform(final ImmutableBeatsMap beats) {
			this.position(FractionalPosition.fromTimeRounded(beats, position));
			this.endPosition(FractionalPosition.fromTimeRounded(beats, endPosition));

			return new Vocal(this);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return Vocal.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Vocal vocal = (Vocal) source;

		writer.addAttribute("p", vocal.position().asString());
		writer.addAttribute("ep", vocal.endPosition().asString());
		writer.addAttribute("text", vocal.text());
		if (vocal.flag() != VocalFlag.NONE) {
			writer.addAttribute("flag", vocal.flag().name());
		}
	}

	private Vocal generateVocalFromPosition(final HierarchicalStreamReader reader) {
		final String positionString = reader.getAttribute("position");
		if (positionString != null && !positionString.isBlank()) {
			final int position = Integer.valueOf(positionString);
			final String lengthString = reader.getAttribute("length");
			final int length = lengthString == null || lengthString.isBlank() ? 0 : Integer.valueOf(lengthString);
			return new TemporaryVocal(position, position + length);
		}

		return new Vocal(FractionalPosition.fromString(reader.getAttribute("p")),
				FractionalPosition.fromString(reader.getAttribute("ep")));
	}

	private VocalFlag readFlag(final String s) {
		return s == null || s.isBlank() ? VocalFlag.NONE : VocalFlag.valueOf(s);
	}

	@Override
	public Vocal unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final Vocal vocal = generateVocalFromPosition(reader);
		vocal.flag(readFlag(reader.getAttribute("flag")));
		vocal.text(reader.getAttribute("text"));

		return vocal;
	}
}
