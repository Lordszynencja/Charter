package log.charter.services.data.copy.data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.collections.ArrayList2;

@XStreamAlias("eventPointsCopyData")
public class EventPointsCopyData implements ICopyData {
	public final Map<String, Phrase> phrases;
	public final List<CopiedArrangementEventsPointPosition> arrangementEventsPoints;

	public EventPointsCopyData(final Map<String, Phrase> phrases,
			final List<CopiedArrangementEventsPointPosition> arrangementEventsPoints) {
		this.phrases = phrases;
		this.arrangementEventsPoints = arrangementEventsPoints;
	}

	@Override
	public boolean isEmpty() {
		return arrangementEventsPoints.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition position, final boolean convertFromBeats) {
		paste(chartData, selectionManager, position, true, true, true, convertFromBeats);
	}

	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition, final boolean sections, final boolean phrases, final boolean events,
			final boolean convertFromBeats) {
		final Arrangement arrangement = chartData.currentArrangement();
		if (phrases) {
			for (final CopiedArrangementEventsPointPosition arrangementEventsPoint : arrangementEventsPoints) {
				final String phraseName = arrangementEventsPoint.phrase;
				if (!arrangement.phrases.containsKey(phraseName)) {
					arrangement.phrases.put(phraseName, this.phrases.get(phraseName));
				}
			}
		}

		final ImmutableBeatsMap beats = chartData.beats();
		final Set<EventPoint> positionsToSelect = new HashSet<>(arrangementEventsPoints.size());

		for (final CopiedArrangementEventsPointPosition copiedPosition : arrangementEventsPoints) {
			try {
				final EventPoint value = copiedPosition.getValue(beats, basePosition, convertFromBeats);

				if (value != null) {
					if (!sections) {
						value.section = null;
					}
					if (!phrases) {
						value.phrase = null;
					}
					if (!events) {
						value.events = new ArrayList2<>();
					}

					final EventPoint eventPoint = arrangement
							.findOrCreateArrangementEventsPoint(value.fractionalPosition());
					eventPoint.merge(value);
					positionsToSelect.add(value);
				}
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		}

		selectionManager.addSelectionForPositions(PositionType.EVENT_POINT, positionsToSelect);
	}
}
