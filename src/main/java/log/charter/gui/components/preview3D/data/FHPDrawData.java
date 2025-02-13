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
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.FHP;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.Position;
import log.charter.services.RepeatManager;

public class FHPDrawData implements IConstantPosition {

	private static void addFHP(final ChartData chartData, final double audioLength, final double timeFrom,
			final double timeTo, final ImmutableBeatsMap beats, final List<FHPDrawData> fhpsToDraw,
			final List<FHP> fhps, final int i, final FHP fhp) {
		final double fhpTimeFrom = max(fhp.position(beats), timeFrom);
		double fhpTimeTo;
		if (i < fhps.size() - 1) {
			fhpTimeTo = fhps.get(i + 1).position(beats) - 1;
		} else {
			fhpTimeTo = audioLength;
		}

		final List<EventPoint> phrases = filter(chartData.currentEventPoints(), e -> e.hasPhrase());
		final EventPoint nextPhraseIteration = firstAfter(phrases, fhp).find();
		if (nextPhraseIteration != null) {
			fhpTimeTo = min(fhpTimeTo, nextPhraseIteration.position(beats));
		}
		fhpTimeTo = min(fhpTimeTo, timeTo);

		fhpsToDraw.add(new FHPDrawData(fhpTimeFrom, fhpTimeTo, fhp.fret - 1, fhp.topFret()));
	}

	public static List<FHPDrawData> getFHPsForTimeSpan(final ChartData chartData, final double audioLength,
			final double timeFrom, final double timeTo) {
		if (chartData.currentArrangementLevel() == null) {
			return asList(new FHPDrawData(timeFrom, timeTo, 0, 4));
		}

		final ImmutableBeatsMap beats = chartData.beats();

		final List<FHPDrawData> fhpsToDraw = new ArrayList<>();
		final List<FHP> fhps = chartData.currentFHPs();

		final int fhpsFrom = lastBeforeEqual(fhps, new Position(timeFrom).toFraction(beats)).findId(0);
		final Integer fhpsTo = lastBeforeEqual(fhps, new Position(timeTo).toFraction(beats)).findId();
		if (fhpsTo == null) {
			return fhpsToDraw;
		}

		for (int i = fhpsFrom; i <= fhpsTo; i++) {
			addFHP(chartData, audioLength, timeFrom, timeTo, beats, fhpsToDraw, fhps, i, fhps.get(i));
		}

		return fhpsToDraw;
	}

	public static List<FHPDrawData> getFHPsForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final double audioLength, final double timeFrom, final double timeTo) {
		double maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.repeatEnd() - 1);
		}

		final List<FHPDrawData> fhpsToDraw = getFHPsForTimeSpan(data, audioLength, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return fhpsToDraw;
		}

		final List<FHPDrawData> repeatedFHPs = getFHPsForTimeSpan(data, audioLength, repeatManager.repeatStart(),
				repeatManager.repeatEnd() - 1);
		final double repeatLength = repeatManager.repeatEnd() - repeatManager.repeatStart();
		double repeatStart = repeatManager.repeatEnd();
		while (repeatStart + repeatLength < timeFrom) {
			repeatStart += repeatLength;
		}

		while (repeatStart < timeTo) {
			final double repeatOffset = repeatStart - repeatManager.repeatStart();
			for (final FHPDrawData fhpDrawData : repeatedFHPs) {
				final double start = fhpDrawData.timeFrom + repeatOffset;
				double end = fhpDrawData.timeTo + repeatOffset;
				if (start > timeTo) {
					break;
				}
				if (end > timeTo) {
					end = timeTo;
				}
				fhpsToDraw.add(new FHPDrawData(start, end, fhpDrawData.fretFrom, fhpDrawData.fretTo));
			}

			repeatStart += repeatLength;
		}

		return fhpsToDraw;
	}

	public final double timeFrom;
	public final double timeTo;
	public final int fretFrom;
	public final int fretTo;

	public FHPDrawData(final double timeFrom, final double timeTo, final int fretFrom, final int fretTo) {
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.fretFrom = fretFrom;
		this.fretTo = fretTo;
	}

	@Override
	public double position() {
		return timeFrom;
	}

}