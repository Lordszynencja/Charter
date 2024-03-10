package log.charter.services.data.copy.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedSoundPosition;
import log.charter.services.data.selection.SelectionManager;
import log.charter.song.Arrangement;
import log.charter.song.BeatsMap;
import log.charter.song.ChordTemplate;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("soundsCopyData")
public class SoundsCopyData implements ICopyData {
	private final ArrayList2<ChordTemplate> chordTemplates;
	private final ArrayList2<CopiedSoundPosition> sounds;

	public SoundsCopyData(final ArrayList2<ChordTemplate> chordTemplates,
			final ArrayList2<CopiedSoundPosition> sounds) {
		this.chordTemplates = chordTemplates;
		this.sounds = sounds;
	}

	@Override
	public boolean isEmpty() {
		return sounds.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean convertFromBeats) {
		final Arrangement arrangement = chartData.getCurrentArrangement();
		final BeatsMap beatsMap = chartData.songChart.beatsMap;
		final ArrayList2<ChordOrNote> sounds = chartData.getCurrentArrangementLevel().sounds;
		final Set<Integer> positionsToSelect = new HashSet<>(this.sounds.size());

		final double basePositionInBeats = beatsMap.getPositionInBeats(time);
		final Map<Integer, Integer> chordIdsMap = new HashMap<>();

		for (final CopiedSoundPosition copiedPosition : this.sounds) {
			try {
				final ChordOrNote sound = copiedPosition.getValue(beatsMap, time, basePositionInBeats,
						convertFromBeats);
				if (sound == null) {
					continue;
				}

				if (sound.isChord()) {
					final int templateId = sound.chord().templateId();
					if (!chordIdsMap.containsKey(templateId)) {
						chordIdsMap.put(templateId,
								arrangement.getChordTemplateIdWithSave(chordTemplates.get(templateId)));
					}

					final int newTemplateId = chordIdsMap.get(templateId);
					final ChordTemplate newTemplate = arrangement.chordTemplates.get(newTemplateId);
					sound.chord().updateTemplate(newTemplateId, newTemplate);
				}

				sounds.add(sound);
				positionsToSelect.add(sound.position());
			} catch (final Exception e) {
				Logger.error("Couldn't paste sound", e);
			}
		}

		sounds.sort(null);
		selectionManager.addSelectionForPositions(PositionType.GUITAR_NOTE, positionsToSelect);
	}
}
