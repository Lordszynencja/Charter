package log.charter.gui.components.preview3D.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.managers.RepeatManager;
import log.charter.song.Anchor;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.notes.IConstantPosition;
import log.charter.song.notes.IConstantPositionWithLength;
import log.charter.util.CollectionUtils.ArrayList2;

public class HandShapeDrawData implements IConstantPositionWithLength {
	public static List<HandShapeDrawData> getHandShapesForTimeSpan(final ChartData data, final int timeFrom,
			final int timeTo) {
		final List<HandShapeDrawData> handShapesToDraw = new ArrayList<>();
		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;

		int handShapesFrom = IConstantPosition.findLastIdBeforeEqual(handShapes, timeFrom);
		if (handShapesFrom == -1) {
			handShapesFrom = 0;
		}
		final int handShapesTo = IConstantPosition.findLastIdBeforeEqual(handShapes, timeTo);

		for (int i = handShapesFrom; i <= handShapesTo; i++) {
			final HandShape handShape = handShapes.get(i);

			final int handShapeTimeFrom = max(handShape.position(), timeFrom);
			final int handShapeTimeTo = min(handShape.endPosition(), timeTo);
			final Anchor anchor = IConstantPosition.findLastBeforeEqual(anchors, handShape.position());

			handShapesToDraw.add(new HandShapeDrawData(handShapeTimeFrom, handShapeTimeTo, anchor.fret - 1,
					anchor.topFret(), chordTemplates.get(handShape.templateId)));
		}

		return handShapesToDraw;
	}

	public static List<HandShapeDrawData> getHandShapesForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final int timeFrom, final int timeTo) {
		int maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.getRepeatEnd() - 1);
		}

		final List<HandShapeDrawData> handShapesToDraw = getHandShapesForTimeSpan(data, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return handShapesToDraw;
		}

		final List<HandShapeDrawData> repeatedHandShapes = getHandShapesForTimeSpan(data,
				repeatManager.getRepeatStart(), repeatManager.getRepeatEnd() - 1);
		int repeatStart = repeatManager.getRepeatEnd();
		while (repeatStart < timeFrom) {
			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		while (repeatStart < timeTo) {
			for (final HandShapeDrawData handShapeDrawData : repeatedHandShapes) {
				final int start = handShapeDrawData.timeFrom - repeatManager.getRepeatStart() + repeatStart;
				int end = start + handShapeDrawData.timeTo - handShapeDrawData.timeFrom;
				if (start > timeTo) {
					break;
				}
				if (end > timeTo) {
					end = timeTo;
				}

				handShapesToDraw.add(new HandShapeDrawData(start, end, handShapeDrawData));
			}

			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		return handShapesToDraw;
	}

	public final int timeFrom;
	public final int timeTo;
	public final int fretFrom;
	public final int fretTo;
	public final ChordTemplate template;

	public HandShapeDrawData(final int timeFrom, final int timeTo, final int fretFrom, final int fretTo,
			final ChordTemplate template) {
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.fretFrom = fretFrom;
		this.fretTo = fretTo;
		this.template = template;
	}

	public HandShapeDrawData(final int timeFrom, final int timeTo, final HandShapeDrawData other) {
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		fretFrom = other.fretFrom;
		fretTo = other.fretTo;
		template = other.template;
	}

	@Override
	public int position() {
		return timeFrom;
	}

	@Override
	public int length() {
		return timeTo - timeFrom;
	}
}