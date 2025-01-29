package log.charter.gui.components.preview3D.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.services.RepeatManager;

public class HandShapeDrawData implements IConstantPosition {
	public static List<HandShapeDrawData> getHandShapesForTimeSpan(final ChartData data, final double timeFrom,
			final double timeTo) {
		if (data.currentArrangementLevel() == null) {
			return new ArrayList<>();
		}

		final ImmutableBeatsMap beats = data.beats();
		final List<HandShapeDrawData> handShapesToDraw = new ArrayList<>();
		final List<HandShape> handShapes = data.currentArrangementLevel().handShapes;
		final List<ChordTemplate> chordTemplates = data.currentArrangement().chordTemplates;

		final FractionalPosition fromPosition = FractionalPosition.fromTime(beats, timeFrom);
		final FractionalPosition toPosition = FractionalPosition.fromTime(beats, timeTo);
		Integer handShapesFrom = lastBeforeEqual(handShapes, fromPosition).findId();
		Integer handShapesTo = lastBeforeEqual(handShapes, toPosition).findId();
		if (handShapesFrom == null && handShapesTo == null) {
			return handShapesToDraw;
		}
		if (handShapesFrom == null) {
			handShapesFrom = 0;
		}
		if (handShapesTo == null) {
			handShapesTo = handShapes.size() - 1;
		}

		for (int i = handShapesFrom; i <= handShapesTo; i++) {
			final HandShape handShape = handShapes.get(i);
			if (handShape.templateId == null || handShape.templateId == -1) {
				continue;
			}

			final double handShapeTimeFrom = max(handShape.position(beats), timeFrom);
			final double handShapeTimeTo = min(handShape.endPosition().position(beats), timeTo);

			handShapesToDraw.add(new HandShapeDrawData(handShapeTimeFrom, handShapeTimeTo,
					chordTemplates.get(handShape.templateId)));
		}

		return handShapesToDraw;
	}

	public static List<HandShapeDrawData> getHandShapesForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final double timeFrom, final double timeTo) {
		double maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.repeatEnd() - 1);
		}

		final List<HandShapeDrawData> handShapesToDraw = getHandShapesForTimeSpan(data, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return handShapesToDraw;
		}

		final List<HandShapeDrawData> repeatedHandShapes = getHandShapesForTimeSpan(data, repeatManager.repeatStart(),
				repeatManager.repeatEnd() - 1);
		double repeatStart = repeatManager.repeatEnd();
		while (repeatStart < timeFrom) {
			repeatStart += repeatManager.repeatEnd() - repeatManager.repeatStart();
		}

		while (repeatStart < timeTo) {
			for (final HandShapeDrawData handShapeDrawData : repeatedHandShapes) {
				final double start = handShapeDrawData.timeFrom - repeatManager.repeatStart() + repeatStart;
				double end = start + handShapeDrawData.timeTo - handShapeDrawData.timeFrom;
				if (start > timeTo) {
					break;
				}
				if (end > timeTo) {
					end = timeTo;
				}

				handShapesToDraw.add(new HandShapeDrawData(start, end, handShapeDrawData));
			}

			repeatStart += repeatManager.repeatEnd() - repeatManager.repeatStart();
		}

		return handShapesToDraw;
	}

	public final double originalPosition;
	public final double timeFrom;
	public final double timeTo;
	public final ChordTemplate template;

	public HandShapeDrawData(final double timeFrom, final double timeTo, final ChordTemplate template) {
		originalPosition = timeFrom;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.template = template;
	}

	public HandShapeDrawData(final double timeFrom, final double timeTo, final HandShapeDrawData other) {
		originalPosition = other.originalPosition;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		template = other.template;
	}

	@Override
	public double position() {
		return timeFrom;
	}
}