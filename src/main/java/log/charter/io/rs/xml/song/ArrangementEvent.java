package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.util.collections.ArrayList2;

@XStreamAlias("event")
public class ArrangementEvent {
	public static ArrayList2<ArrangementEvent> fromEventsAndBeatMap(final ArrayList2<EventPoint> events,
			final BeatsMap beatsMap) {
		final ArrayList2<ArrangementEvent> arrangementEvents = new ArrayList2<>();
		for (final EventPoint eventPoint : events) {
			for (final EventType eventType : eventPoint.events) {
				arrangementEvents.add(new ArrangementEvent(eventPoint.position(), eventType));
			}
		}

		Beat previous = null;
		for (final Beat beat : beatsMap.beats) {
			if (previous == null || previous.beatsInMeasure != beat.beatsInMeasure) {
				arrangementEvents.add(new ArrangementEvent(beat.position(), beat.beatsInMeasure, beat.noteDenominator));
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
