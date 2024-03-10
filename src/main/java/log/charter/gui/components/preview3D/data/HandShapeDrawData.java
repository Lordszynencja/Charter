package log.charter.gui.components.preview3D.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.IConstantPosition;
import log.charter.data.song.notes.IConstantPositionWithLength;
import log.charter.services.RepeatManager;
import log.charter.util.CollectionUtils.ArrayList2;

public class HandShapeDrawData implements IConstantPositionWithLength {
	public static List<HandShapeDrawData> getHandShapesForTimeSpan(final ChartData data, final int timeFrom,
			final int timeTo) {
		if (data.getCurrentArrangementLevel() == null) {
			return new ArrayList<>();
		}

		final List<HandShapeDrawData> handShapesToDraw = new ArrayList<>();
		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;

		int handShapesFrom = IConstantPosition.findLastIdBeforeEqual(handShapes, timeFrom);
		if (handShapesFrom == -1) {
			handShapesFrom = 0;
		}
		final int handShapesTo = IConstantPosition.findLastIdBeforeEqual(handShapes, timeTo);

		for (int i = handShapesFrom; i <= handShapesTo; i++) {
			final HandShape handShape = handShapes.get(i);
			if (handShape.templateId == -1) {
				continue;
			}

			final int handShapeTimeFrom = max(handShape.position(), timeFrom);
			final int handShapeTimeTo = min(handShape.endPosition(), timeTo);

			handShapesToDraw.add(new HandShapeDrawData(handShapeTimeFrom, handShapeTimeTo,
					chordTemplates.get(handShape.templateId)));
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

	public final int originalPosition;
	public final int timeFrom;
	public final int timeTo;
	public final ChordTemplate template;

	public HandShapeDrawData(final int timeFrom, final int timeTo, final ChordTemplate template) {
		originalPosition = timeFrom;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.template = template;
	}

	public HandShapeDrawData(final int timeFrom, final int timeTo, final HandShapeDrawData other) {
		originalPosition = other.originalPosition;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
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