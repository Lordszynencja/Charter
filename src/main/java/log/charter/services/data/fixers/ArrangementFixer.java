package log.charter.services.data.fixers;

import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.CollectionUtils.max;
import static log.charter.util.CollectionUtils.min;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.EventPoint;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.SectionType;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;
import log.charter.data.song.vocals.VocalPath;
import log.charter.data.types.PositionType;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.CollectionUtils;
import log.charter.util.data.Fraction;

public class ArrangementFixer {
	private static final FractionalPosition maxDistanceBeforeBreakingHandshape = new FractionalPosition(2);

	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private SelectionManager selectionManager;

	private void removeWrongEventPoints(final Arrangement arrangement) {
		arrangement.eventPoints.removeIf(ep -> ep.section == null && !ep.hasPhrase() && ep.events.isEmpty());
	}

	private void removeWrongPositions(final Arrangement arrangement, final FractionalPosition end) {
		final Predicate<IConstantFractionalPosition> invalidPositionCheck = p -> p.position().beatId < 0
				|| p.compareTo(end) > 0;

		arrangement.eventPoints.removeIf(invalidPositionCheck);
		arrangement.toneChanges.removeIf(invalidPositionCheck);
		for (final Level level : arrangement.levels) {
			level.fhps.removeIf(invalidPositionCheck);
			level.sounds.removeIf(invalidPositionCheck);
			level.handShapes.removeIf(invalidPositionCheck);
		}
	}

	private void removeDuplicatesFractional(final List<? extends IConstantFractionalPosition> positions) {
		final List<IConstantFractionalPosition> positionsToRemove = new ArrayList<>();
		for (int i = 1; i < positions.size(); i++) {
			if (positions.get(i).position().equals(positions.get(i - 1).position())) {
				positionsToRemove.add(positions.get(i));
			}
		}

		positions.removeAll(positionsToRemove);
	}

	private void addHandShapeForChord(final List<EventPoint> phrases, final Level level, final Chord chord,
			final int id, final HandShape nextHandShape, final int nextHandShapeId) {
		IConstantFractionalPosition maxEndPosition = level.sounds.get(level.sounds.size() - 1).endPosition();
		final EventPoint nextPhrase = CollectionUtils.firstAfter(phrases, chord).find();
		if (nextPhrase != null) {
			maxEndPosition = max(maxEndPosition, nextPhrase);
		}
		if (nextHandShape != null) {
			maxEndPosition = max(maxEndPosition, nextHandShape);
		}

		Chord lastChord = chord;
		for (int i = id + 1; i < level.sounds.size(); i++) {
			final ChordOrNote sound = level.sounds.get(i);
			if (!sound.isChord() || sound.position().compareTo(maxEndPosition) >= 0) {
				break;
			}

			final Chord nextChord = sound.chord();
			if (nextChord.templateId() != chord.templateId()) {
				break;
			}
			if (nextChord.distance(lastChord.endPosition()).compareTo(maxDistanceBeforeBreakingHandshape) > 0) {
				break;
			}

			lastChord = nextChord;
		}

		final FractionalPosition position = chord.position();
		FractionalPosition endPosition = lastChord.endPosition().position();
		if (endPosition.compareTo(lastChord) <= 0) {
			endPosition = endPosition.add(new Fraction(1, 4));
		}

		final HandShape handShape = new HandShape(position, endPosition, chord.templateId());
		level.handShapes.add(nextHandShapeId, handShape);
	}

	private void addMissingHandShapes(final Arrangement arrangement, final Level level) {
		final List<EventPoint> phrases = filter(arrangement.eventPoints, ep -> ep.hasPhrase());

		int handShapeId = 0;
		HandShape handShape = level.handShapes.isEmpty() ? null : level.handShapes.get(handShapeId);
		for (int i = 0; i < level.sounds.size(); i++) {
			final ChordOrNote sound = level.sounds.get(i);
			if (!sound.isChord()) {
				continue;
			}
			while (handShape != null && handShape.endPosition().compareTo(sound) < 0) {
				handShapeId++;
				handShape = handShapeId >= level.handShapes.size() ? null : level.handShapes.get(handShapeId);
			}
			if (handShape != null && handShape.position().compareTo(sound) <= 0) {
				continue;
			}

			final Chord chord = sound.chord();
			addHandShapeForChord(phrases, level, chord, i, handShape, handShapeId);
			handShape = level.handShapes.get(handShapeId);
		}
	}

	private void addMissingFHPs(final Arrangement arrangement, final Level level) {
		final List<EventPoint> sections = filter(arrangement.eventPoints, p -> p.section != null);

		for (final EventPoint eventPoint : arrangement.eventPoints) {
			if (eventPoint.phrase == null) {
				continue;
			}

			final Integer sectionId = lastBeforeEqual(sections, eventPoint).findId();
			if (sectionId != null && sections.get(sectionId).section == SectionType.NO_GUITAR) {
				continue;
			}

			final Integer lastFHPId = CollectionUtils
					.lastBeforeEqual(level.fhps, eventPoint.toFraction(chartData.beats())).findId();
			if (lastFHPId == null) {
				final FHP newFHP = new FHP(eventPoint.position());
				level.fhps.add(newFHP);
				level.fhps.sort(IConstantFractionalPosition::compareTo);

				continue;
			}

			final FHP lastFHP = level.fhps.get(lastFHPId);
			if (lastFHP.position().compareTo(eventPoint) != 0) {
				final FHP newFHP = new FHP(lastFHP);
				newFHP.position(eventPoint.position());
				level.fhps.add(newFHP);
				level.fhps.sort(IConstantFractionalPosition::compareTo);
			}
		}
	}

	private List<Integer> removeId(final List<Integer> ids, final int idToRemove) {
		return ids.stream()//
				.filter(id -> id != idToRemove)//
				.map(id -> id > idToRemove ? id - 1 : id)//
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private void joinSimilarLinkedNotes(final Level level) {
		final boolean guitarSelected = selectionManager.selectedType() == PositionType.GUITAR_NOTE;
		List<Integer> selectedNoteIds = guitarSelected ? selectionManager.getSelectedIds(PositionType.GUITAR_NOTE)
				: null;

		if (guitarSelected) {
			selectionManager.clear();
		}

		for (int i = 0; i < level.sounds.size(); i++) {
			final ChordOrNote sound = level.sounds.get(i);
			if (!sound.isNote()) {
				continue;
			}

			final Note note = sound.note();
			if (!note.linkNext || note.slideTo != null) {
				continue;
			}

			int nextNoteId = i;
			while (nextNoteId + 1 < level.sounds.size()) {
				nextNoteId++;
				final ChordOrNote nextSound = level.sounds.get(nextNoteId);
				if (!nextSound.isNote()) {
					final Chord chord = nextSound.chord();
					if (chord.chordNotes.containsKey(note.string)) {
						break;
					}

					continue;
				}

				final Note nextNote = nextSound.note();
				if (nextNote.string != note.string) {
					continue;
				}

				if (nextNote.fret != note.fret //
						|| nextNote.slideTo != null//
						|| nextNote.vibrato != note.vibrato//
						|| nextNote.tremolo != note.tremolo) {
					break;
				}

				note.endPosition(nextNote.endPosition());
				if (!nextNote.bendValues.isEmpty()) {
					if (nextNote.bendValues.get(0).position().compareTo(nextNote) != 0) {
						note.bendValues.add(new BendValue(nextNote.position()));
					}
					note.bendValues.addAll(nextNote.bendValues);
				}
				note.linkNext = nextNote.linkNext;
				level.sounds.remove(nextNoteId);
				if (guitarSelected) {
					selectedNoteIds = removeId(selectedNoteIds, nextNoteId);
				}

				nextNoteId--;
			}

		}

		if (guitarSelected) {
			selectionManager.addSoundSelection(selectedNoteIds);
		}
	}

	private void removeNoteTailIfNeeded(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		if (note.linkNext() || note.tremolo() || note.vibrato() || !note.bendValues().isEmpty()
				|| note.slideTo() != null) {
			return;
		}

		final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(note.string(), id - 1, sounds);
		if (previousSound != null && previousSound.getString(note.string()).get().linkNext()) {
			return;
		}

		final ImmutableBeatsMap beats = chartData.beats();
		final IConstantFractionalPosition minimalEndPosition = beats.getMinEndPositionAfter(note).toFraction(beats);
		if (note.endPosition().compareTo(minimalEndPosition) < 0) {
			note.endPosition(note.position());
		}
	}

	private void fixLinkedNoteLength(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		LinkedNotesFixer.fixLinkedNote(note, id, sounds);
		WrongBendPositionRemover.fixBends(note);
	}

	private void fixNotLinkedNoteLength(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		IConstantFractionalPosition endPosition = note.endPosition();
		final ImmutableBeatsMap beats = chartData.beats();

		if (note.passOtherNotes()) {
			final ChordOrNote nextSoundOnString = ChordOrNote.findNextSoundOnString(note.string(), id + 1, sounds);
			if (nextSoundOnString != null) {
				endPosition = min(endPosition, beats.getMaxPositionBefore(nextSoundOnString).toFraction(beats));
			}
		} else if (id + 1 < sounds.size()) {
			final IConstantFractionalPosition next = sounds.get(id + 1);
			endPosition = min(endPosition, beats.getMaxPositionBefore(next).toFraction(beats));
		}

		endPosition = min(endPosition, chartTimeHandler.maxTimeFractional());

		note.endPosition(max(note.position(), endPosition).position());
	}

	public void fixNoteLengthWithoutCutting(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		if (note.linkNext()) {
			fixLinkedNoteLength(note, id, sounds);
			return;
		}

		fixNotLinkedNoteLength(note, id, sounds);
		WrongBendPositionRemover.fixBends(note);
	}

	private void fixNoteLength(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		if (note.linkNext()) {
			fixLinkedNoteLength(note, id, sounds);
			return;
		}

		fixNotLinkedNoteLength(note, id, sounds);
		removeNoteTailIfNeeded(note, id, sounds);
		WrongBendPositionRemover.fixBends(note);
	}

	public void fixSoundLength(final int id, final List<ChordOrNote> sounds) {
		sounds.get(id).notes().forEach(note -> fixNoteLength(note, id, sounds));
	}

	public void fixNoteLengths(final List<ChordOrNote> sounds, final int from, final int to) {
		for (int i = from; i <= to; i++) {
			fixSoundLength(i, sounds);
		}
	}

	public void fixNoteLengths(final List<ChordOrNote> sounds) {
		fixNoteLengths(sounds, 0, sounds.size() - 1);
	}

	public <T extends IVirtualPositionWithEnd> void fixLengths(final List<T> positions) {
		if (positions.isEmpty()) {
			return;
		}

		final ImmutableBeatsMap beats = chartData.beats();
		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(beats);

		for (int i = 0; i < positions.size() - 1; i++) {
			final IVirtualPositionWithEnd position = positions.get(i);
			final IVirtualConstantPosition maxPositionBeforeNext = beats.getMaxPositionBefore(positions.get(i + 1));
			final IVirtualConstantPosition minEndPosition = min(comparator, position.endPosition(),
					maxPositionBeforeNext);

			position.endPosition(beats, max(comparator, position, minEndPosition));
		}

		final IVirtualPositionWithEnd lastPosition = positions.get(positions.size() - 1);
		final IVirtualConstantPosition maxPosition = new ConstantPosition(chartTimeHandler.maxTime());
		final IVirtualConstantPosition minEndPosition = min(comparator, lastPosition.endPosition(), maxPosition);
		lastPosition.endPosition(beats, max(comparator, lastPosition, minEndPosition));
	}

	private void fixLevel(final Arrangement arrangement, final Level level) {
		level.sounds.removeIf(sound -> sound.isChord() //
				&& (sound.chord().templateId() >= arrangement.chordTemplates.size()//
						|| sound.chord().chordNotes.isEmpty()));

		removeDuplicatesFractional(level.fhps);
		removeDuplicatesFractional(level.sounds);
		removeDuplicatesFractional(level.handShapes);

		addMissingFHPs(arrangement, level);
		addMissingHandShapes(arrangement, level);

		joinSimilarLinkedNotes(level);
		fixNoteLengths(level.sounds);
		fixLengths(level.handShapes);
	}

	public void fixArrangements() {
		final double end = chartTimeHandler.maxTime();
		final FractionalPosition endFractional = FractionalPosition.fromTime(chartData.beats(), end);

		chartData.songChart.beatsMap.makeBeatsUntilSongEnd(end);
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();

		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			removeWrongEventPoints(arrangement);
			removeWrongPositions(arrangement, endFractional);
			for (final Level level : arrangement.levels) {
				fixLevel(arrangement, level);
			}

			DuplicatedChordTemplatesRemover.remove(arrangement);
			UnusedChordTemplatesRemover.remove(arrangement);
			MissingFingersOnChordTemplatesFixer.fix(arrangement);
		}

		chartData.songChart.beatsMap.truncate(chartTimeHandler.maxNonBeatTime());
		chordTemplatesEditorTab.refreshTemplates();

		for (final VocalPath vocals : chartData.songChart.vocalPaths) {
			fixLengths(vocals.vocals);
		}
	}
}
