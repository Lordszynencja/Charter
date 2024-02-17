package log.charter.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.song.notes.ChordOrNote.findNextSoundOnString;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import log.charter.data.config.Config;
import log.charter.song.Anchor;
import log.charter.song.Arrangement;
import log.charter.song.ChordTemplate;
import log.charter.song.EventPoint;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.SectionType;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.CommonNote;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.IPositionWithLength;
import log.charter.util.CollectionUtils.ArrayList2;

public class ArrangementFixer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void removeWrongPositions(final Arrangement arrangement, final int start, final int end) {
		arrangement.eventPoints.removeIf(p -> p.position() < start || p.position() > end);
		arrangement.toneChanges.removeIf(p -> p.position() < start || p.position() > end);
		for (final Level level : arrangement.levels) {
			level.anchors.removeIf(p -> p.position() < start || p.position() > end);
			level.sounds.removeIf(p -> p.position() < start || p.position() > end);
			level.handShapes.removeIf(p -> p.position() < start || p.position() > end);
		}
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

	private void addMissingHandShapes(final Level level) {
		final LinkedList<Chord> chordsForHandShapes = level.sounds.stream()//
				.filter(chordOrNote -> chordOrNote.isChord() && !chordOrNote.chord.splitIntoNotes)//
				.map(chordOrNote -> chordOrNote.chord)//
				.collect(Collectors.toCollection(LinkedList::new));
		final ArrayList2<Chord> chordsWithoutHandShapes = new ArrayList2<>();
		for (final HandShape handShape : level.handShapes) {
			while (!chordsForHandShapes.isEmpty() && chordsForHandShapes.get(0).position() < handShape.position()) {
				chordsWithoutHandShapes.add(chordsForHandShapes.remove(0));
			}
			while (!chordsForHandShapes.isEmpty() && chordsForHandShapes.get(0).position() < handShape.endPosition()) {
				chordsForHandShapes.remove(0);
			}
		}
		chordsWithoutHandShapes.addAll(chordsForHandShapes);

		for (final Chord chord : chordsWithoutHandShapes) {
			final int endPosition = chord.endPosition();

			final int length = max(50, endPosition - chord.position());
			final HandShape handShape = new HandShape(chord, length);
			level.handShapes.add(handShape);
		}

		level.handShapes.sort(null);
	}

	private void addMissingAnchors(final Arrangement arrangement, final Level level) {
		final List<EventPoint> sections = arrangement.eventPoints.stream().filter(p -> p.section != null)
				.collect(Collectors.toList());

		for (final EventPoint eventPoint : arrangement.eventPoints) {
			if (eventPoint.phrase == null) {
				continue;
			}

			final int sectionId = findLastIdBeforeEqual(sections, eventPoint);
			if (sectionId != -1 && sections.get(sectionId).section == SectionType.NO_GUITAR) {
				continue;
			}

			final int lastAnchorId = findLastIdBeforeEqual(level.anchors, eventPoint);
			if (lastAnchorId == -1) {
				continue;
			}

			final Anchor lastAnchor = level.anchors.get(lastAnchorId);
			if (lastAnchor.position() != eventPoint.position()) {
				final Anchor newAnchor = new Anchor(lastAnchor);
				newAnchor.position(eventPoint.position());
				level.anchors.add(newAnchor);
				level.anchors.sort(null);
			}
		}
	}

	private static void fixLinkedNote(final CommonNote note, final int id, final ArrayList2<ChordOrNote> sounds) {
		if (!note.linkNext()) {
			return;
		}

		final ChordOrNote nextSound = findNextSoundOnString(note.string(), id + 1, sounds);
		if (nextSound == null) {
			return;
		}

		if (nextSound.isChord()) {
			nextSound.chord.splitIntoNotes = true;
		}

		note.length(nextSound.position() - note.position() - 1);
	}

	public static void fixNoteLength(final CommonNote note, final int id, final ArrayList2<ChordOrNote> sounds) {
		if (note.linkNext()) {
			fixLinkedNote(note, id, sounds);
			return;
		}

		int endPosition = note.endPosition();

		if (note.passOtherNotes()) {
			final ChordOrNote nextSoundOnString = ChordOrNote.findNextSoundOnString(note.string(), id + 1, sounds);
			if (nextSoundOnString != null) {
				endPosition = min(endPosition, nextSoundOnString.position() - minNoteDistance);
			}
		} else if (id + 1 < sounds.size()) {
			final IPosition next = sounds.get(id + 1);
			endPosition = min(endPosition, next.position() - minNoteDistance);
		}

		note.endPosition(max(note.position(), endPosition));
	}

	public static void fixSoundLength(final int id, final ArrayList2<ChordOrNote> sounds) {
		final ChordOrNote sound = sounds.get(id);
		if (sound.isNote()) {
			fixNoteLength(CommonNote.create(sound.note), id, sounds);
		} else {
			sound.chord.chordNotes.forEach((string, chordNote) -> {
				fixNoteLength(CommonNote.create(sound.chord, string, chordNote), id, sounds);
			});
		}
	}

	public void fixNoteLengths(final ArrayList2<ChordOrNote> sounds) {
		for (int i = 0; i < sounds.size(); i++) {
			fixSoundLength(i, sounds);
		}
	}

	public void fixLengths(final ArrayList2<? extends IPositionWithLength> positions) {
		if (positions.isEmpty()) {
			return;
		}

		for (int i = 0; i < positions.size() - 1; i++) {
			final IPositionWithLength position = positions.get(i);
			final IPositionWithLength nextPosition = positions.get(i + 1);
			position.endPosition(min(nextPosition.position() - Config.minNoteDistance, position.endPosition()));
		}

		final IPositionWithLength lastPosition = positions.getLast();
		lastPosition.endPosition(min(data.songChart.beatsMap.songLengthMs - 1, lastPosition.endPosition()));
	}

	private void fixLevel(final Arrangement arrangement, final Level level) {
		level.sounds//
				.stream().filter(chordOrNote -> chordOrNote.isNote())//
				.map(chordOrNote -> chordOrNote.note)//
				.forEach(note -> note.length(note.length() >= Config.minTailLength ? note.length() : 0));

		level.sounds
				.removeIf(sound -> sound.isChord() && sound.chord.templateId() >= arrangement.chordTemplates.size());

		removeDuplicates(level.anchors);
		removeDuplicates(level.sounds);
		removeDuplicates(level.handShapes);

		addMissingAnchors(arrangement, level);
		addMissingHandShapes(level);

		fixNoteLengths(level.sounds);

		fixLengths(level.handShapes);
	}

	private void removeChordTemplate(final Arrangement arrangementChart, final int removedId,
			final int replacementId) {
		arrangementChart.chordTemplates.remove(removedId);
		for (final Level level : arrangementChart.levels) {
			for (final ChordOrNote chordOrNote : level.sounds) {
				if (!chordOrNote.isChord() || chordOrNote.chord.templateId() != removedId) {
					continue;
				}

				chordOrNote.chord.updateTemplate(replacementId, arrangementChart.chordTemplates.get(replacementId));
			}
			for (final HandShape handShape : level.handShapes) {
				if (handShape.templateId != removedId) {
					continue;
				}

				handShape.templateId = replacementId;
			}
		}
	}

	public void fixDuplicatedChordTemplates(final Arrangement arrangementChart) {
		final List<ChordTemplate> templates = arrangementChart.chordTemplates;
		for (int i = 0; i < templates.size(); i++) {
			final ChordTemplate template = templates.get(i);
			for (int j = i + 1; j < templates.size(); j++) {
				ChordTemplate otherTemplate = templates.get(j);
				while (template.equals(otherTemplate) && j < templates.size()) {
					removeChordTemplate(arrangementChart, j, i);
					if (j < templates.size()) {
						otherTemplate = templates.get(j);
					}
				}
			}
		}
	}

	private boolean isTemplateUsed(final Arrangement arrangementChart, final int templateId) {
		for (final Level level : arrangementChart.levels) {
			for (final ChordOrNote sound : level.sounds) {
				if (sound.isChord() && sound.chord.templateId() == templateId) {
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

	public void removeUnusedChordTemplates(final Arrangement arrangementChart) {
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

		for (final Level level : arrangementChart.levels) {
			for (final ChordOrNote sound : level.sounds) {
				if (sound.isChord()) {
					final int newTemplateId = templateIdsMap.get(sound.chord.templateId());
					final ChordTemplate template = arrangementChart.chordTemplates.get(newTemplateId);
					sound.chord.updateTemplate(newTemplateId, template);
				}
			}

			for (final HandShape handShape : level.handShapes) {
				handShape.templateId = templateIdsMap.get(handShape.templateId);
			}
		}
	}

	private void fixMissingFingersOnChordTemplates(final Arrangement arrangementChart) {
		arrangementChart.chordTemplates.forEach(chordTemplate -> {
			for (final int string : new HashSet<>(chordTemplate.fingers.keySet())) {
				if (chordTemplate.fingers.get(string) == null) {
					chordTemplate.fingers.remove(string);
				}
			}
		});
	}

	public void fixArrangements() {
		final int start = data.songChart.beatsMap.beats.get(0).position();
		final int end = data.songChart.beatsMap.songLengthMs;

		for (final Arrangement arrangement : data.songChart.arrangements) {
			removeWrongPositions(arrangement, start, end);
			for (final Level level : arrangement.levels) {
				fixLevel(arrangement, level);
			}

			fixDuplicatedChordTemplates(arrangement);
			removeUnusedChordTemplates(arrangement);
			fixMissingFingersOnChordTemplates(arrangement);
		}

		data.songChart.beatsMap.makeBeatsUntilSongEnd();
		data.songChart.beatsMap.fixFirstBeatInMeasures();
	}
}
