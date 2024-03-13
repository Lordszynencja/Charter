package log.charter.services.data.copy.data.positions;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.data.song.SectionType;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedArrangementEventsPointPosition")
public class CopiedArrangementEventsPointPosition extends CopiedPosition<EventPoint> {
	@XStreamAsAttribute
	public final SectionType section;
	@XStreamAsAttribute
	public final String phrase;
	@XStreamAsAttribute
	public final List<EventType> events;

	public CopiedArrangementEventsPointPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final EventPoint eventPoint) {
		super(beats, basePosition, eventPoint);
		section = eventPoint.section;
		phrase = eventPoint.phrase;
		events = new ArrayList<>(eventPoint.events);
	}

	@Override
	protected EventPoint prepareValue() {
		final EventPoint arrangementEventsPoint = new EventPoint();
		arrangementEventsPoint.section = section;
		arrangementEventsPoint.phrase = phrase;
		arrangementEventsPoint.events = events;

		return arrangementEventsPoint;
	}
}
