package log.charter.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.config.Config;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;

public class ArrangementFixer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void removeDuplicates(final List<? extends IPosition> positions) {
		final List<IPosition> positionsToRemove = new ArrayList<>();
		for (int i = 1; i < positions.size(); i++) {
			if (positions.get(i).position() == positions.get(i - 1).position()) {
				positionsToRemove.add(positions.get(i));
			}
		}

		positions.removeAll(positionsToRemove);
	}

	private void fixLevel(final Level level) {
		level.chordsAndNotes//
				.stream().filter(chordOrNote -> chordOrNote.note != null)//
				.map(chordOrNote -> chordOrNote.note)//
				.forEach(note -> note.length(note.length() >= Config.minTailLength ? note.length() : 0));

		removeDuplicates(level.chordsAndNotes);
	}

	private void removeChordTemplate(final ArrangementChart arrangementChart, final int removedId,
			final int replacementId) {
		arrangementChart.chordTemplates.remove(removedId);
		for (final Level level : arrangementChart.levels.values()) {
			for (final ChordOrNote chordOrNote : level.chordsAndNotes) {
				if (!chordOrNote.isChord() || chordOrNote.chord.chordId != removedId) {
					continue;
				}

				chordOrNote.chord.chordId = replacementId;
			}
			for (final HandShape handShape : level.handShapes) {
				if (handShape.chordId != removedId) {
					continue;
				}

				handShape.chordId = replacementId;
			}
		}
	}

	private void fixDuplicatedChordTemplates(final ArrangementChart arrangementChart) {
		final List<ChordTemplate> templates = arrangementChart.chordTemplates;
		for (int i = 0; i < templates.size(); i++) {
			final ChordTemplate template = templates.get(i);
			for (int j = i + 1; j < templates.size(); j++) {
				ChordTemplate otherTemplate = templates.get(j);
				while (template.equals(otherTemplate) && j < templates.size()) {
					removeChordTemplate(arrangementChart, j, i);
					otherTemplate = templates.get(j);
				}
			}
		}
	}

	private boolean isTemplateUsed(final ArrangementChart arrangementChart, final int templateId) {
		for (final Level level : arrangementChart.levels.values()) {
			for (final ChordOrNote sound : level.chordsAndNotes) {
				if (sound.isChord() && sound.chord.chordId == templateId) {
					return true;
				}
			}

			for (final HandShape handShape : level.handShapes) {
				if (handShape.chordId == templateId) {
					return true;
				}
			}
		}

		return false;
	}

	private void removeUnusedChordTemplates(final ArrangementChart arrangementChart) {
		final Map<Integer, Integer> templateIdsMap = new HashMap<>();

		final int templatesAmount = arrangementChart.chordTemplates.size();
		int usedTemplatesCounter = 0;
		for (int i = 0; i < templatesAmount; i++) {
			if (isTemplateUsed(arrangementChart, i)) {
				templateIdsMap.put(i, usedTemplatesCounter++);
			} else {
				arrangementChart.chordTemplates.remove(usedTemplatesCounter);
			}
		}

		for (final Level level : arrangementChart.levels.values()) {
			for (final ChordOrNote sound : level.chordsAndNotes) {
				if (sound.isChord()) {
					sound.chord.chordId = templateIdsMap.get(sound.chord.chordId);
				}
			}

			for (final HandShape handShape : level.handShapes) {
				handShape.chordId = templateIdsMap.get(handShape.chordId);
			}
		}
	}

	public void fixArrangement() {
		for (final ArrangementChart arrangementChart : data.songChart.arrangements) {
			for (final Level level : arrangementChart.levels.values()) {
				fixLevel(level);
			}

			fixDuplicatedChordTemplates(arrangementChart);
			removeUnusedChordTemplates(arrangementChart);
		}
	}
}
