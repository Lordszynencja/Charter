package log.charter.services.data.copy.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedSound;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("soundsCopyData")
@XStreamInclude(CopiedSound.class)
public class SoundsCopyData implements ICopyData {
	private final List<ChordTemplate> chordTemplates;
	private final List<CopiedSound> sounds;

	public SoundsCopyData(final List<ChordTemplate> chordTemplates, final List<CopiedSound> sounds) {
		this.chordTemplates = chordTemplates;
		this.sounds = sounds;
	}

	@Override
	public PositionType type() {
		return PositionType.GUITAR_NOTE;
	}

	@Override
	public boolean isEmpty() {
		return sounds.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition, final boolean convertFromBeats) {
		final Arrangement arrangement = chartData.currentArrangement();
		final ImmutableBeatsMap beats = chartData.beats();
		final List<ChordOrNote> sounds = chartData.currentSounds();
		final Set<ChordOrNote> positionsToSelect = new HashSet<>(this.sounds.size());

		final Map<Integer, Integer> chordIdsMap = new HashMap<>();

		for (final CopiedSound copiedPosition : this.sounds) {
			try {
				final ChordOrNote sound = copiedPosition.getValue(beats, basePosition, convertFromBeats);
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
				positionsToSelect.add(sound);
			} catch (final Exception e) {
				Logger.error("Couldn't paste sound", e);
			}
		}

		sounds.sort(IConstantFractionalPosition::compareTo);
		selectionManager.addSelectionForPositions(PositionType.GUITAR_NOTE, positionsToSelect);
	}
}
