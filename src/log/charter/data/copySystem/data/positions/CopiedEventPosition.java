package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.Beat;
import log.charter.song.Event;
import log.charter.song.Event.EventType;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("copiedEvent")
public class CopiedEventPosition extends CopiedOnBeatPosition<Event> {
	public final EventType eventType;

	public CopiedEventPosition(final ArrayList2<Beat> beats, final int baseBeat, final Event onBeat) {
		super(beats, baseBeat, onBeat);
		eventType = onBeat.type;
	}

	@Override
	protected Event createValue() {
		return new Event(null, eventType);
	}
}
