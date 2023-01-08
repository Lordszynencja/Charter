package log.charter.data.copySystem.data;

import static log.charter.data.copySystem.data.positions.CopiedPosition.findBeatPositionForPosition;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedSoundPosition;
import log.charter.io.Logger;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
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
	public void paste(final ChartData data) {
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().chordsAndNotes;

		final double basePositionInBeats = findBeatPositionForPosition(beats, data.time);
		final Map<Integer, Integer> chordIdsMap = new HashMap<>();

		for (final CopiedSoundPosition copiedPosition : this.sounds) {
			try {
				final ChordOrNote sound = copiedPosition.getValue(beats, basePositionInBeats);
				if (sound == null) {
					continue;
				}

				if (sound.isChord()) {
					final int chordId = sound.chord.chordId;
					if (!chordIdsMap.containsKey(chordId)) {
						chordIdsMap.put(chordId, arrangement.getChordTemplateIdWithSave(chordTemplates.get(chordId)));
					}
					sound.chord.chordId = chordIdsMap.get(chordId);
				}

				sounds.add(sound);
			} catch (final Exception e) {
				Logger.error("Couldn't paste sound", e);
			}
		}

		sounds.sort(null);
	}
}
