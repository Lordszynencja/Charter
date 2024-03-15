package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.SectionType;
import log.charter.data.song.position.FractionalPosition;
import log.charter.io.rsc.xml.converters.SimpleCollectionToStringConverter.EventTypesListConverter;

public class EventPointConverter implements Converter {
	public static class TemporaryEventPoint extends EventPoint {
		private final int position;

		public TemporaryEventPoint(final int position) {
			this.position = position;
			this.fractionalPosition(new FractionalPosition(0));
		}

		public EventPoint transform(final ImmutableBeatsMap beats) {
			this.fractionalPosition(FractionalPosition.fromTime(beats, position, true));
			return new EventPoint(this);
		}
	}

	private final EventTypesListConverter eventTypesListConverter = new EventTypesListConverter();

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return EventPoint.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final EventPoint eventPoint = (EventPoint) source;

		writer.addAttribute("p", eventPoint.fractionalPosition().asString());
		if (eventPoint.section != null) {
			writer.addAttribute("section", eventPoint.section.name());
		}
		if (eventPoint.phrase != null) {
			writer.addAttribute("phrase", eventPoint.phrase);
		}
		if (!eventPoint.events.isEmpty()) {
			writer.addAttribute("events", eventTypesListConverter.toString(eventPoint.events));
		}
	}

	private EventPoint generateEventPointFromPosition(final HierarchicalStreamReader reader) {
		final String position = reader.getAttribute("position");
		if (position != null) {
			return new TemporaryEventPoint(Integer.valueOf(position));
		}

		return new EventPoint(FractionalPosition.fromString(reader.getAttribute("p")));
	}

	private SectionType readSection(final String s) {
		return s == null ? null : SectionType.valueOf(s);
	}

	@Override
	public EventPoint unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final EventPoint eventPoint = generateEventPointFromPosition(reader);
		eventPoint.section = readSection(reader.getAttribute("section"));
		eventPoint.phrase = reader.getAttribute("phrase");
		eventPoint.events = eventTypesListConverter.fromString(reader.getAttribute("events"));

		return eventPoint;
	}
}
