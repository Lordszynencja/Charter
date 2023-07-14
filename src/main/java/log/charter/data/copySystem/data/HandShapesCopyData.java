package log.charter.data.copySystem.data;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedHandShapePosition;
import log.charter.io.Logger;
import log.charter.song.ArrangementChart;
import log.charter.song.BeatsMap;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("handShapesCopyData")
public class HandShapesCopyData implements ICopyData {
	private final ArrayList2<ChordTemplate> chordTemplates;
	private final ArrayList2<CopiedHandShapePosition> handShapes;

	public HandShapesCopyData(final ArrayList2<ChordTemplate> chordTemplates,
			final ArrayList2<CopiedHandShapePosition> handShapes) {
		this.chordTemplates = chordTemplates;
		this.handShapes = handShapes;
	}

	@Override
	public boolean isEmpty() {
		return handShapes.isEmpty();
	}

	@Override
	public void paste(final ChartData data) {
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final BeatsMap beatsMap = data.songChart.beatsMap;
		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;

		final double basePositionInBeats = beatsMap.getPositionInBeats(data.time);
		final Map<Integer, Integer> chordIdsMap = new HashMap<>();

		for (final CopiedHandShapePosition copiedPosition : this.handShapes) {
			try {
				final HandShape handShape = copiedPosition.getValue(beatsMap, basePositionInBeats);
				if (handShape == null) {
					continue;
				}

				final int templateId = handShape.templateId;
				if (!chordIdsMap.containsKey(templateId)) {
					chordIdsMap.put(templateId, arrangement.getChordTemplateIdWithSave(chordTemplates.get(templateId)));
				}
				handShape.templateId = chordIdsMap.get(templateId);

				handShapes.add(handShape);
			} catch (final Exception e) {
				Logger.error("Couldn't paste hand shape", e);
			}
		}

		handShapes.sort(null);
	}
}
