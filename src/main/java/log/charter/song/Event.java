package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toCollection;

import java.util.List;

import log.charter.io.rs.xml.song.ArrangementEvent;
import log.charter.util.CollectionUtils.ArrayList2;

public class Event extends OnBeat {
	public enum EventType {
		HIGH_PITCH_TICK("B0", "High pitch tick (B0)"), //
		LOW_PITCH_TICK("B1", "Low pitch tick (B1)"), //
		CROWD_WAVING_HANDS("E0", "Crowd waving hands (E0)"), //
		CROWD_HAPPY("E1", "Crowd happy (E1)"), //
		CROWD_VERY_HAPPY("E2", "Crowd very happy (E2)"), //
		CROWD_APPLAUSE("E3", "Crowd applause (E3)"), //
		CROWD_CRITIQUE_APPLAUSE("D3", "Crowd critique applause (D3)"), //
		END_CROWD_APPLAUSE("E13", "End crowd applause (E13)");

		public final String rsName;
		public final String label;

		private EventType(final String rsName, final String label) {
			this.rsName = rsName;
			this.label = label;
		}

		public static EventType findByRSName(final String rsName) {
			for (final EventType eventType : values()) {
				if (eventType.rsName.equals(rsName)) {
					return eventType;
				}
			}

			return HIGH_PITCH_TICK;
		}
	}

	public static ArrayList2<Event> fromArrangement(final ArrayList2<Beat> beats,
			final List<ArrangementEvent> arrangementEvents) {
		final ArrayList2<Event> events = arrangementEvents.stream()//
				.filter(arrangementEvent -> {
					if (!arrangementEvent.code.startsWith("TS:")) {
						return true;
					}

					final int time = arrangementEvent.time;
					final String[] timeSignatureParts = arrangementEvent.code.split(":")[1].split("/");
					final int beatsInMeasure = max(1, min(1024, Integer.valueOf(timeSignatureParts[0])));
					final int noteDenominator = max(1, min(1024, Integer.valueOf(timeSignatureParts[1])));
					beats.stream()//
							.filter(beat -> beat.position() >= time)//
							.forEach(beat -> beat.setTimeSignature(beatsInMeasure, noteDenominator));

					return false;
				})//
				.map(arrangementEvent -> new Event(beats, arrangementEvent))//
				.collect(toCollection(ArrayList2::new));

		return events;
	}

	public EventType type;

	public Event(final Beat beat, final EventType type) {
		super(beat);
		this.type = type;
	}

	private Event(final ArrayList2<Beat> beats, final ArrangementEvent arrangementEvent) {
		super(beats, arrangementEvent.time);
		type = EventType.findByRSName(arrangementEvent.code);
	}

	public Event(final ArrayList2<Beat> beats, final Event other) {
		super(beats, other);
		type = other.type;
	}
}
