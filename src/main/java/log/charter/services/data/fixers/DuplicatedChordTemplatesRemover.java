package log.charter.services.data.fixers;

import java.util.List;

import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;

public class DuplicatedChordTemplatesRemover {
	private static void removeChordTemplate(final Arrangement arrangement, final int removedId,
			final int replacementId) {
		arrangement.chordTemplates.remove(removedId);
		for (final Level level : arrangement.levels) {
			for (final ChordOrNote chordOrNote : level.sounds) {
				if (!chordOrNote.isChord() || chordOrNote.chord().templateId() < removedId) {
					continue;
				}

				final int templateId = chordOrNote.chord().templateId() == removedId ? replacementId
						: chordOrNote.chord().templateId() - 1;
				chordOrNote.chord().updateTemplate(templateId, arrangement.chordTemplates.get(templateId));
			}
			for (final HandShape handShape : level.handShapes) {
				if (handShape.templateId < removedId) {
					continue;
				}

				handShape.templateId = handShape.templateId == removedId ? replacementId : handShape.templateId - 1;
			}
		}
	}

	public static void remove(final Arrangement arrangement) {
		final List<ChordTemplate> templates = arrangement.chordTemplates;
		for (int i = 0; i < templates.size(); i++) {
			final ChordTemplate template = templates.get(i);
			for (int j = templates.size() - 1; j > i; j--) {
				if (template.equals(templates.get(j))) {
					removeChordTemplate(arrangement, j, i);
				}
			}
		}
	}
}
