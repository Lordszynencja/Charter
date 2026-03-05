package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.CollectionUtils.lastBefore;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import log.charter.data.ChartData;
import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.song.ToneChange;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.CollectionUtils;

public class GuitarDrawerUtils {
	public static class ItemWithDrawingPosition<T> {
		public final T item;
		public final int x0;
		public final int x1;

		public ItemWithDrawingPosition(final T item, final int x0, final int x1) {
			this.item = item;
			this.x0 = x0;
			this.x1 = x1;
		}
	}

	private ChartData chartData;
	private ChartPanel chartPanel;
	private ChartTimeHandler chartTimeHandler;

	private <T extends IConstantFractionalPosition> ItemWithDrawingPosition<T> findPrevious(final double time,
			final HighwayDrawer highwayDrawer, final List<T> items, final ToIntFunction<T> sizeGetter) {
		if (!highwayDrawer.supportsCurrentValues()) {
			return null;
		}

		final FractionalPosition leftScreenEdgeTime = FractionalPosition.fromTime(chartData.beats(),
				xToPosition(0, time));

		final Integer previousId = lastBefore(items, leftScreenEdgeTime).findId();
		if (previousId == null) {
			return null;
		}

		final T item = items.get(previousId);
		final int width = sizeGetter.applyAsInt(item);
		int x0, x1;
		if (items.size() > previousId + 1) {
			final T itemAfter = items.get(previousId + 1);
			x1 = min(width, positionToX(itemAfter.position(chartData.beats()), time));
			x0 = x1 - width;
		} else {
			x0 = 0;
			x1 = width;
		}

		return new ItemWithDrawingPosition<>(item, x0, x1);
	}

	private <T extends IConstantFractionalPosition> ItemWithDrawingPosition<T> findNext(final double time,
			final HighwayDrawer highwayDrawer, final List<T> items, final ToIntFunction<T> sizeGetter) {
		if (!highwayDrawer.supportsCurrentValues()) {
			return null;
		}

		final FractionalPosition rightScreenEdgeTime = FractionalPosition.fromTime(chartData.beats(),
				xToPosition(chartPanel.getWidth(), time));

		final Integer previousId = lastBefore(items, rightScreenEdgeTime).findId();
		if (previousId != null) {
			final T item = items.get(previousId);
			final int width = sizeGetter.applyAsInt(item);
			final int x = positionToX(item.position(chartData.beats()), time);
			if (x + width >= chartPanel.getWidth()) {
				final int x1 = chartPanel.getWidth();
				final int x0 = x1 - width;
				return new ItemWithDrawingPosition<>(item, x0, x1);
			}
		}

		final Integer nextId = CollectionUtils.firstAfterEqual(items, rightScreenEdgeTime).findId();
		if (nextId == null) {
			return null;
		}

		final T item = items.get(nextId);
		final int width = sizeGetter.applyAsInt(item);
		final int x1 = chartPanel.getWidth();
		final int x0 = x1 - width;

		return new ItemWithDrawingPosition<>(item, x0, x1);
	}

	private <T> T withTimeAndHighwayDrawer(final BiFunction<Double, HighwayDrawer, T> function) {
		final Graphics2D g = (Graphics2D) chartPanel.getGraphics();
		final double time = chartTimeHandler.time();
		final int strings = chartData.currentStrings();
		final HighwayDrawer highwayDrawer = HighwayDrawer.getHighwayDrawer(g, strings, time);

		return function.apply(time, highwayDrawer);
	}

	public ItemWithDrawingPosition<EventPoint> findPreviousSection(final double time,
			final HighwayDrawer highwayDrawer) {
		return findPrevious(time, highwayDrawer,
				filter(chartData.currentEventPoints(), eventPoint -> eventPoint.section != null),
				item -> highwayDrawer.getSizeOfSection(item.section).width);
	}

	public ItemWithDrawingPosition<EventPoint> findPreviousSection() {
		return withTimeAndHighwayDrawer(this::findPreviousSection);
	}

	public ItemWithDrawingPosition<EventPoint> findNextSection(final double time, final HighwayDrawer highwayDrawer) {
		return findNext(time, highwayDrawer,
				filter(chartData.currentEventPoints(), eventPoint -> eventPoint.section != null),
				item -> highwayDrawer.getSizeOfSection(item.section).width);
	}

	public ItemWithDrawingPosition<EventPoint> findNextSection() {
		return withTimeAndHighwayDrawer(this::findNextSection);
	}

	public ItemWithDrawingPosition<EventPoint> findPreviousPhrase(final double time,
			final HighwayDrawer highwayDrawer) {
		return findPrevious(time, highwayDrawer, filter(chartData.currentEventPoints(), EventPoint::hasPhrase),
				item -> {
					final String phraseName = item.phrase;
					final Phrase phraseInfo = chartData.currentArrangement().phrases.get(phraseName);
					return highwayDrawer.getSizeOfPhrase(phraseInfo, phraseName).width;
				});
	}

	public ItemWithDrawingPosition<EventPoint> findPreviousPhrase() {
		return withTimeAndHighwayDrawer(this::findPreviousPhrase);
	}

	public ItemWithDrawingPosition<EventPoint> findNextPhrase(final double time, final HighwayDrawer highwayDrawer) {
		return findNext(time, highwayDrawer, filter(chartData.currentEventPoints(), EventPoint::hasPhrase), item -> {
			final String phraseName = item.phrase;
			final Phrase phraseInfo = chartData.currentArrangement().phrases.get(phraseName);
			return highwayDrawer.getSizeOfPhrase(phraseInfo, phraseName).width;
		});
	}

	public ItemWithDrawingPosition<EventPoint> findNextPhrase() {
		return withTimeAndHighwayDrawer(this::findNextPhrase);
	}

	public ItemWithDrawingPosition<ToneChange> findPreviousToneChange(final double time,
			final HighwayDrawer highwayDrawer) {
		final List<ToneChange> toneChanges = new ArrayList<>(chartData.currentToneChanges().size());
		toneChanges.add(new ToneChange(new FractionalPosition(0), chartData.currentArrangement().startingTone));
		toneChanges.addAll(chartData.currentToneChanges());

		return findPrevious(time, highwayDrawer, toneChanges, item -> highwayDrawer.getSizeOfTone(item.toneName).width);
	}

	public ItemWithDrawingPosition<ToneChange> findPreviousToneChange() {
		return withTimeAndHighwayDrawer(this::findPreviousToneChange);
	}

	public ItemWithDrawingPosition<ToneChange> findNextToneChange(final double time,
			final HighwayDrawer highwayDrawer) {
		return findNext(time, highwayDrawer, chartData.currentToneChanges(),
				item -> highwayDrawer.getSizeOfTone(item.toneName).width);
	}

	public ItemWithDrawingPosition<ToneChange> findNextToneChange() {
		return withTimeAndHighwayDrawer(this::findNextToneChange);
	}
}
