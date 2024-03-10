package log.charter.services.data.copy.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedHandShapePosition;
import log.charter.services.data.selection.SelectionManager;
import log.charter.song.Arrangement;
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
	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean convertFromBeats) {
		final Arrangement arrangement = chartData.getCurrentArrangement();
		final BeatsMap beatsMap = chartData.songChart.beatsMap;
		final ArrayList2<HandShape> handShapes = chartData.getCurrentArrangementLevel().handShapes;
		final Set<Integer> positionsToSelect = new HashSet<>(this.handShapes.size());

		final double basePositionInBeats = beatsMap.getPositionInBeats(time);
		final Map<Integer, Integer> chordIdsMap = new HashMap<>();

		for (final CopiedHandShapePosition copiedPosition : this.handShapes) {
			try {
				final HandShape handShape = copiedPosition.getValue(beatsMap, time, basePositionInBeats,
						convertFromBeats);
				if (handShape == null) {
					continue;
				}

				final int templateId = handShape.templateId;
				if (!chordIdsMap.containsKey(templateId)) {
					chordIdsMap.put(templateId, arrangement.getChordTemplateIdWithSave(chordTemplates.get(templateId)));
				}
				handShape.templateId = chordIdsMap.get(templateId);

				handShapes.add(handShape);
				positionsToSelect.add(handShape.position());
			} catch (final Exception e) {
				Logger.error("Couldn't paste hand shape", e);
			}
		}

		handShapes.sort(null);
		selectionManager.addSelectionForPositions(PositionType.HAND_SHAPE, positionsToSelect);
	}
}
