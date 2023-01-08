package log.charter.data.copySystem.data;

import static log.charter.data.copySystem.data.positions.CopiedPosition.findBeatPositionForPosition;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedHandShapePosition;
import log.charter.io.Logger;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
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
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;

		final double basePositionInBeats = findBeatPositionForPosition(beats, data.time);
		final Map<Integer, Integer> chordIdsMap = new HashMap<>();

		for (final CopiedHandShapePosition copiedPosition : this.handShapes) {
			try {
				final HandShape handShape = copiedPosition.getValue(beats, basePositionInBeats);
				if (handShape == null) {
					continue;
				}

				final int chordId = handShape.chordId;
				if (!chordIdsMap.containsKey(chordId)) {
					chordIdsMap.put(chordId, arrangement.getChordTemplateIdWithSave(chordTemplates.get(chordId)));
				}
				handShape.chordId = chordIdsMap.get(chordId);

				handShapes.add(handShape);
			} catch (final Exception e) {
				Logger.error("Couldn't paste hand shape", e);
			}
		}

		handShapes.sort(null);
	}
}
