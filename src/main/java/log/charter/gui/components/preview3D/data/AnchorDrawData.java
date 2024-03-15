package log.charter.gui.components.preview3D.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.IConstantPositionWithLength;
import log.charter.data.song.position.Position;
import log.charter.services.RepeatManager;

public class AnchorDrawData implements IConstantPositionWithLength, Comparable<IConstantPosition> {
	public static List<AnchorDrawData> getAnchorsForTimeSpan(final ChartData chartData, final int audioLength,
			final int timeFrom, final int timeTo) {
		if (chartData.currentArrangementLevel() == null) {
			return asList(new AnchorDrawData(timeFrom, timeTo, 0, 4));
		}

		final ImmutableBeatsMap beats = chartData.beats();

		final List<AnchorDrawData> anchorsToDraw = new ArrayList<>();
		final List<Anchor> anchors = chartData.currentAnchors();

		final int anchorsFrom = lastBeforeEqual(anchors, new Position(timeFrom).toFraction(beats)).findId(0);
		final Integer anchorsTo = lastBeforeEqual(anchors, new Position(timeTo).toFraction(beats)).findId();
		if (anchorsTo == null) {
			return anchorsToDraw;
		}

		for (int i = anchorsFrom; i <= anchorsTo; i++) {
			final Anchor anchor = anchors.get(i);

			final int anchorTimeFrom = max(anchor.position(beats), timeFrom);
			int anchorTimeTo;
			if (i < anchors.size() - 1) {
				anchorTimeTo = anchors.get(i + 1).position(beats) - 1;
			} else {
				anchorTimeTo = audioLength;
			}

			final List<EventPoint> phrases = filter(chartData.currentEventPoints(), e -> e.hasPhrase());
			final EventPoint nextPhraseIteration = firstAfter(phrases, anchor).find();
			if (nextPhraseIteration != null) {
				anchorTimeTo = min(anchorTimeTo, nextPhraseIteration.position(beats));
			}
			anchorTimeTo = min(anchorTimeTo, timeTo);

			anchorsToDraw.add(new AnchorDrawData(anchorTimeFrom, anchorTimeTo, anchor.fret - 1, anchor.topFret()));
		}

		return anchorsToDraw;
	}

	public static List<AnchorDrawData> getAnchorsForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final int audioLength, final int timeFrom, final int timeTo) {
		int maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.repeatEnd() - 1);
		}

		final List<AnchorDrawData> anchorsToDraw = getAnchorsForTimeSpan(data, audioLength, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return anchorsToDraw;
		}

		final List<AnchorDrawData> repeatedAnchors = getAnchorsForTimeSpan(data, audioLength,
				repeatManager.repeatStart(), repeatManager.repeatEnd() - 1);
		int repeatStart = repeatManager.repeatEnd();
		while (repeatStart < timeFrom) {
			repeatStart += repeatManager.repeatEnd() - repeatManager.repeatStart();
		}

		while (repeatStart < timeTo) {
			for (final AnchorDrawData anchorDrawData : repeatedAnchors) {
				final int start = anchorDrawData.timeFrom - repeatManager.repeatStart() + repeatStart;
				int end = start + anchorDrawData.timeTo - anchorDrawData.timeFrom;
				if (start > timeTo) {
					break;
				}
				if (end > timeTo) {
					end = timeTo;
				}
				anchorsToDraw.add(new AnchorDrawData(start, end, anchorDrawData.fretFrom, anchorDrawData.fretTo));
			}

			repeatStart += repeatManager.repeatEnd() - repeatManager.repeatStart();
		}

		return anchorsToDraw;
	}

	public final int timeFrom;
	public final int timeTo;
	public final int fretFrom;
	public final int fretTo;

	public AnchorDrawData(final int timeFrom, final int timeTo, final int fretFrom, final int fretTo) {
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.fretFrom = fretFrom;
		this.fretTo = fretTo;
	}

	@Override
	public int position() {
		return timeFrom;
	}

	@Override
	public int length() {
		return timeTo - timeFrom;
	}

	@Override
	public int compareTo(final IConstantPosition o) {
		return Integer.compare(timeFrom, o.position());
	}
}