package log.charter.services.data.fixers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.CollectionUtils.max;
import static log.charter.util.CollectionUtils.min;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.SectionType;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.position.ConstantPosition;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.IPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.CollectionUtils;
import log.charter.util.collections.ArrayList2;
import log.charter.util.data.Fraction;

public class ArrangementFixer {
	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;

	private void removeWrongPositions(final Arrangement arrangement, final int start, final int end) {
		final ImmutableBeatsMap beats = chartData.beats();
		final Predicate<IConstantPosition> invalidPositionCheck = p -> p.position() < start || p.position() > end;
		final Predicate<IConstantFractionalPosition> invalidFractionalPositionCheck = p -> p
				.fractionalPosition().beatId < 0 || p.position(beats) > end;

		arrangement.eventPoints.removeIf(invalidFractionalPositionCheck);
		arrangement.toneChanges.removeIf(invalidFractionalPositionCheck);
		for (final Level level : arrangement.levels) {
			level.anchors.removeIf(invalidFractionalPositionCheck);
			level.sounds.removeIf(invalidPositionCheck);
			level.handShapes.removeIf(invalidFractionalPositionCheck);
		}
	}

	private <T extends IConstantPosition> void removeDuplicates(final List<T> positions) {
		final List<IConstantPosition> positionsToRemove = new ArrayList<>();
		for (int i = 1; i < positions.size(); i++) {
			if (positions.get(i).compareTo(positions.get(i - 1)) == 0) {
				positionsToRemove.add(positions.get(i));
			}
		}

		positions.removeAll(positionsToRemove);
	}

	private void removeDuplicatesFractional(final List<? extends IConstantFractionalPosition> positions) {
		final List<IConstantFractionalPosition> positionsToRemove = new ArrayList<>();
		for (int i = 1; i < positions.size(); i++) {
			if (positions.get(i).fractionalPosition().equals(positions.get(i - 1).fractionalPosition())) {
				positionsToRemove.add(positions.get(i));
			}
		}

		positions.removeAll(positionsToRemove);
	}

	private void addMissingHandShapes(final Level level) {
		final ImmutableBeatsMap beats = chartData.beats();

		final LinkedList<Chord> chordsForHandShapes = level.sounds.stream()//
				.filter(chordOrNote -> chordOrNote.isChord() && !chordOrNote.chord().splitIntoNotes)//
				.map(chordOrNote -> chordOrNote.chord())//
				.collect(Collectors.toCollection(LinkedList::new));
		final ArrayList2<Chord> chordsWithoutHandShapes = new ArrayList2<>();
		for (final HandShape handShape : level.handShapes) {
			while (!chordsForHandShapes.isEmpty()
					&& chordsForHandShapes.get(0).position() < handShape.position(beats)) {
				chordsWithoutHandShapes.add(chordsForHandShapes.remove(0));
			}
			while (!chordsForHandShapes.isEmpty()
					&& chordsForHandShapes.get(0).position() < handShape.endPosition().position(beats)) {
				chordsForHandShapes.remove(0);
			}
		}
		chordsWithoutHandShapes.addAll(chordsForHandShapes);

		for (final Chord chord : chordsWithoutHandShapes) {
			final FractionalPosition position = chord.toFraction(beats).fractionalPosition();
			FractionalPosition endPosition = chord.endPosition().toFraction(beats).fractionalPosition();
			if (endPosition.add(position.negate()).compareTo(new FractionalPosition(0, new Fraction(1, 8))) < 0) {
				endPosition = position.add(new Fraction(1, 8));
			}

			final HandShape handShape = new HandShape(position, endPosition, chord.templateId());
			level.handShapes.add(handShape);
		}

		level.handShapes.sort(IConstantFractionalPosition::compareTo);
	}

	private void addMissingAnchors(final Arrangement arrangement, final Level level) {
		final List<EventPoint> sections = arrangement.eventPoints.stream().filter(p -> p.section != null)
				.collect(Collectors.toList());

		for (final EventPoint eventPoint : arrangement.eventPoints) {
			if (eventPoint.phrase == null) {
				continue;
			}

			final Integer sectionId = lastBeforeEqual(sections, eventPoint).findId();
			if (sectionId != null && sections.get(sectionId).section == SectionType.NO_GUITAR) {
				continue;
			}

			final Integer lastAnchorId = CollectionUtils
					.lastBeforeEqual(level.anchors, eventPoint.toFraction(chartData.beats())).findId();
			if (lastAnchorId == null) {
				continue;
			}

			final Anchor lastAnchor = level.anchors.get(lastAnchorId);
			if (lastAnchor.fractionalPosition().compareTo(eventPoint) != 0) {
				final Anchor newAnchor = new Anchor(lastAnchor);
				newAnchor.fractionalPosition(eventPoint.fractionalPosition());
				level.anchors.add(newAnchor);
				level.anchors.sort(IConstantFractionalPosition::compareTo);
			}
		}
	}

	public void fixNoteLength(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		if (note.linkNext()) {
			LinkedNotesFixer.fixLinkedNote(note, id, sounds);
			return;
		}

		int endPosition = note.endPosition();
		final ImmutableBeatsMap beats = chartData.beats();

		if (note.passOtherNotes()) {
			final ChordOrNote nextSoundOnString = ChordOrNote.findNextSoundOnString(note.string(), id + 1, sounds);
			if (nextSoundOnString != null) {
				endPosition = min(endPosition,
						beats.getMaxPositionBefore(nextSoundOnString).toPosition(beats).position());
			}
		} else if (id + 1 < sounds.size()) {
			final IPosition next = sounds.get(id + 1);
			endPosition = min(endPosition, beats.getMaxPositionBefore(next).toPosition(beats).position());
		}

		endPosition = min(endPosition, chartTimeHandler.maxTime());

		note.endPosition(max(note.position(), endPosition));
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
			final IVirtualConstantPosition maxPosition = max(comparator, position,
					beats.getMaxPositionBefore(positions.get(i + 1)));
			position.endPosition(beats, min(comparator, position.endPosition(), maxPosition));
		}

		final IVirtualPositionWithEnd lastPosition = positions.get(positions.size() - 1);
		final IVirtualConstantPosition maxPosition = new ConstantPosition(chartTimeHandler.maxTime());
		lastPosition.endPosition(beats, min(comparator, maxPosition, lastPosition.endPosition()));
	}

	private void fixLevel(final Arrangement arrangement, final Level level) {
		level.sounds//
				.stream().filter(chordOrNote -> chordOrNote.isNote())//
				.map(chordOrNote -> chordOrNote.note())//
				.forEach(note -> note.length(note.length() >= Config.minTailLengthFactor ? note.length() : 0));

		level.sounds
				.removeIf(sound -> sound.isChord() && sound.chord().templateId() >= arrangement.chordTemplates.size());

		removeDuplicatesFractional(level.anchors);
		removeDuplicates(level.sounds);
		removeDuplicatesFractional(level.handShapes);

		addMissingAnchors(arrangement, level);
		addMissingHandShapes(level);

		fixNoteLengths(level.sounds);

		fixLengths(level.handShapes);
	}

	public void fixArrangements() {
		final int start = chartData.songChart.beatsMap.beats.get(0).position();
		final int end = chartTimeHandler.maxTime();

		chartData.songChart.beatsMap.makeBeatsUntilSongEnd(end);
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();

		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			removeWrongPositions(arrangement, start, end);
			for (final Level level : arrangement.levels) {
				fixLevel(arrangement, level);
			}

			DuplicatedChordTemplatesRemover.remove(arrangement);
			UnusedChordTemplatesRemover.remove(arrangement);
			MissingFingersOnChordTemplatesFixer.fix(arrangement);
		}

	}
}
