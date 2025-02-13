package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.FHP;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;

public class FHPConverter implements Converter {
	public static class TemporaryFHP extends FHP {
		private final int position;

		public TemporaryFHP(final int position) {
			this.position = position;
		}

		public FHP transform(final ImmutableBeatsMap beats) {
			this.position(FractionalPosition.fromTimeRounded(beats, position));

			return new FHP(this);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return FHP.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final FHP fhp = (FHP) source;

		writer.addAttribute("p", fhp.position().asString());
		writer.addAttribute("fret", fhp.fret + "");
		if (fhp.width != 4) {
			writer.addAttribute("width", fhp.width + "");
		}
	}

	private FHP generateFHPFromPosition(final HierarchicalStreamReader reader) {
		final String position = reader.getAttribute("position");
		if (position != null) {
			return new TemporaryFHP(Integer.valueOf(position));
		}

		return new FHP(FractionalPosition.fromString(reader.getAttribute("p")));
	}

	private int readWidth(final String s) {
		return s == null ? 4 : Integer.valueOf(s);
	}

	@Override
	public FHP unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final FHP fhp = generateFHPFromPosition(reader);
		fhp.fret = Integer.valueOf(reader.getAttribute("fret"));
		fhp.width = readWidth(reader.getAttribute("width"));

		return fhp;
	}
}
