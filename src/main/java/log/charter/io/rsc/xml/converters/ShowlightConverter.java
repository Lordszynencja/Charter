package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.song.position.FractionalPosition;

public class ShowlightConverter implements Converter {
	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return Showlight.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Showlight showlight = (Showlight) source;

		writer.addAttribute("p", showlight.position().asString());
		writer.addAttribute("t", showlight.type.name());
	}

	@Override
	public Showlight unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final FractionalPosition position = FractionalPosition.fromString(reader.getAttribute("p"));
		final ShowlightType type = ShowlightType.valueOf(reader.getAttribute("t"));
		return new Showlight(position, type);
	}
}
