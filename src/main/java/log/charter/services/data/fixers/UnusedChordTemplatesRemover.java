package log.charter.services.data.fixers;

import java.util.HashMap;
import java.util.Map;

import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;

public class UnusedChordTemplatesRemover {
	private static boolean isTemplateUsed(final Arrangement arrangement, final int templateId) {
		for (final Level level : arrangement.levels) {
			for (final ChordOrNote sound : level.sounds) {
				if (sound.isChord() && sound.chord().templateId() == templateId) {
					return true;
				}
			}

			for (final HandShape handShape : level.handShapes) {
				if (handShape.templateId == templateId) {
					return true;
				}
			}
		}

		return false;
	}

	public static void remove(final Arrangement arrangement) {
		final Map<Integer, Integer> templateIdsMap = new HashMap<>();

		final int templatesAmount = arrangement.chordTemplates.size();
		int usedTemplatesCounter = 0;
		for (int i = 0; i < templatesAmount; i++) {
			if (isTemplateUsed(arrangement, i)) {
				templateIdsMap.put(i, usedTemplatesCounter++);
			} else {
				arrangement.chordTemplates.remove(usedTemplatesCounter);
			}
		}

		for (final Level level : arrangement.levels) {
			for (final ChordOrNote sound : level.sounds) {
				if (sound.isChord()) {
					final int newTemplateId = templateIdsMap.get(sound.chord().templateId());
					final ChordTemplate template = arrangement.chordTemplates.get(newTemplateId);
					sound.chord().updateTemplate(newTemplateId, template);
				}
			}

			for (final HandShape handShape : level.handShapes) {
				handShape.templateId = templateIdsMap.get(handShape.templateId);
			}
		}
	}
}
