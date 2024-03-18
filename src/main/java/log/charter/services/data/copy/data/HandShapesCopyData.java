package log.charter.services.data.copy.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedHandShape;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("handShapesCopyData")
public class HandShapesCopyData implements ICopyData {
	private final List<ChordTemplate> chordTemplates;
	private final List<CopiedHandShape> handShapes;

	public HandShapesCopyData(final List<ChordTemplate> chordTemplates,
			final List<CopiedHandShape> handShapes) {
		this.chordTemplates = chordTemplates;
		this.handShapes = handShapes;
	}

	@Override
	public boolean isEmpty() {
		return handShapes.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition, final boolean convertFromBeats) {
		final Arrangement arrangement = chartData.currentArrangement();
		final ImmutableBeatsMap beats = chartData.beats();
		final List<HandShape> handShapes = chartData.currentHandShapes();
		final Set<HandShape> positionsToSelect = new HashSet<>(this.handShapes.size());
		final Map<Integer, Integer> chordIdsMap = new HashMap<>();

		for (final CopiedHandShape copiedPosition : this.handShapes) {
			try {
				final HandShape handShape = copiedPosition.getValue(beats, basePosition, convertFromBeats);
				if (handShape == null) {
					continue;
				}

				final int templateId = handShape.templateId;
				if (!chordIdsMap.containsKey(templateId)) {
					chordIdsMap.put(templateId, arrangement.getChordTemplateIdWithSave(chordTemplates.get(templateId)));
				}
				handShape.templateId = chordIdsMap.get(templateId);

				handShapes.add(handShape);
				positionsToSelect.add(handShape);
			} catch (final Exception e) {
				Logger.error("Couldn't paste hand shape", e);
			}
		}

		handShapes.sort(IConstantFractionalPosition::compareTo);
		selectionManager.addSelectionForPositions(PositionType.HAND_SHAPE, positionsToSelect);
	}
}
