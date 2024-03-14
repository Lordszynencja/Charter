package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.Anchor;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;

public class AnchorConverter implements Converter {
	public static class TemporaryAnchor extends Anchor {
		public final int position;

		public TemporaryAnchor(final int position) {
			this.position = position;
		}

		public Anchor transform(final ImmutableBeatsMap beats) {
			this.fractionalPosition(FractionalPosition.fromTime(beats, position, true));

			return new Anchor(this);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return Anchor.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Anchor anchor = (Anchor) source;

		writer.addAttribute("p", anchor.fractionalPosition().asString());
		writer.addAttribute("fret", anchor.fret + "");
		if (anchor.width != 4) {
			writer.addAttribute("width", anchor.width + "");
		}
	}

	private Anchor generateAnchorFromPosition(final HierarchicalStreamReader reader) {
		final String position = reader.getAttribute("position");
		if (position != null) {
			return new TemporaryAnchor(Integer.valueOf(position));
		}

		return new Anchor(FractionalPosition.fromString(reader.getAttribute("p")));
	}

	private int readWidth(final String s) {
		return s == null ? 4 : Integer.valueOf(s);
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final Anchor anchor = generateAnchorFromPosition(reader);
		anchor.fret = Integer.valueOf(reader.getAttribute("fret"));
		anchor.width = readWidth(reader.getAttribute("width"));

		return anchor;
	}
}
