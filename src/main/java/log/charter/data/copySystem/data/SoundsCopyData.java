package log.charter.data.copySystem.data;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedSoundPosition;
import log.charter.io.Logger;
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
	public void paste(final int time, final ChartData data, final boolean convertFromBeats) {
		final Arrangement arrangement = data.getCurrentArrangement();
		final BeatsMap beatsMap = data.songChart.beatsMap;
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;

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
			} catch (final Exception e) {
				Logger.error("Couldn't paste sound", e);
			}
		}

		sounds.sort(null);
	}
}
