package log.charter.io.rsc.xml.converters;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		final String t = showlight.types.stream().map(ShowlightType::name).collect(Collectors.joining(","));
		writer.addAttribute("t", t);
	}

	@Override
	public Showlight unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final FractionalPosition position = FractionalPosition.fromString(reader.getAttribute("p"));

		final List<ShowlightType> t = Stream.of(reader.getAttribute("t").split(",")).map(ShowlightType::valueOf)
				.collect(Collectors.toList());
		return new Showlight(position, t);
	}
}
