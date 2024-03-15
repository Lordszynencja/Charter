package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ToneChange;
import log.charter.data.song.position.FractionalPosition;

public class ToneChangeConverter implements Converter {
	public static class TemporaryToneChange extends ToneChange {
		private final int position;

		public TemporaryToneChange(final int position) {
			this.position = position;
		}

		public ToneChange transform(final ImmutableBeatsMap beats) {
			this.fractionalPosition(FractionalPosition.fromTime(beats, position, true));

			return new ToneChange(this);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return ToneChange.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final ToneChange toneChange = (ToneChange) source;

		writer.addAttribute("p", toneChange.fractionalPosition().asString());
		if (toneChange.toneName != null) {
			writer.addAttribute("tone", toneChange.toneName);
		}
	}

	private ToneChange generateAnchorFromPosition(final HierarchicalStreamReader reader) {
		final String position = reader.getAttribute("position");
		if (position != null) {
			return new TemporaryToneChange(Integer.valueOf(position));
		}

		return new ToneChange(FractionalPosition.fromString(reader.getAttribute("p")));
	}

	@Override
	public ToneChange unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final ToneChange toneChange = generateAnchorFromPosition(reader);
		toneChange.toneName = reader.getAttribute("tone");

		return toneChange;
	}
}
