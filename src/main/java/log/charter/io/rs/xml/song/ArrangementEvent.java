package log.charter.io.rs.xml.song;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("event")
public class ArrangementEvent {
	public static List<ArrangementEvent> fromEventsAndBeatMap(final ImmutableBeatsMap beats,
			final List<EventPoint> events, final BeatsMap beatsMap) {
		final List<ArrangementEvent> arrangementEvents = new ArrayList<>();
		for (final EventPoint eventPoint : events) {
			for (final EventType eventType : eventPoint.events) {
				arrangementEvents.add(new ArrangementEvent((int) eventPoint.position(beats), eventType));
			}
		}

		Beat previous = null;
		for (final Beat beat : beatsMap.beats) {
			if (previous == null || previous.beatsInMeasure != beat.beatsInMeasure) {
				arrangementEvents
						.add(new ArrangementEvent((int) beat.position(), beat.beatsInMeasure, beat.noteDenominator));
			}

			previous = beat;
		}

		arrangementEvents.sort((a, b) -> Integer.compare(a.time, b.time));

		return arrangementEvents;
	}

	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public String code;

	public ArrangementEvent() {
	}

	public ArrangementEvent(final int position, final EventType eventType) {
		time = position;
		code = eventType.rsName;
	}

	public ArrangementEvent(final int time, final int beatsInMeasure, final int noteDenominator) {
		this.time = time;
		code = "TS:" + beatsInMeasure + "/" + noteDenominator;
	}
}
