package log.charter.services.data.fixers;

import static log.charter.util.CollectionUtils.filter;
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
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.CollectionUtils;
import log.charter.util.collections.ArrayList2;
import log.charter.util.data.Fraction;

public class ArrangementFixer {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;

	private void removeWrongPositions(final Arrangement arrangement, final FractionalPosition end) {
		final Predicate<IConstantFractionalPosition> invalidPositionCheck = p -> p.position().beatId < 0
				|| p.compareTo(end) > 0;

		arrangement.eventPoints.removeIf(invalidPositionCheck);
		arrangement.toneChanges.removeIf(invalidPositionCheck);
		for (final Level level : arrangement.levels) {
			level.anchors.removeIf(invalidPositionCheck);
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

	private void addMissingHandShapes(final Level level) {
		final ImmutableBeatsMap beats = chartData.beats();

		final LinkedList<Chord> chordsForHandShapes = level.sounds.stream()//
				.filter(chordOrNote -> chordOrNote.isChord() && !chordOrNote.chord().splitIntoNotes)//
				.map(chordOrNote -> chordOrNote.chord())//
				.collect(Collectors.toCollection(LinkedList::new));
		final ArrayList2<Chord> chordsWithoutHandShapes = new ArrayList2<>();
		for (final HandShape handShape : level.handShapes) {
			while (!chordsForHandShapes.isEmpty() && chordsForHandShapes.get(0).compareTo(handShape) < 0) {
				chordsWithoutHandShapes.add(chordsForHandShapes.remove(0));
			}
			while (!chordsForHandShapes.isEmpty()
					&& chordsForHandShapes.get(0).compareTo(handShape.endPosition()) < 0) {
				chordsForHandShapes.remove(0);
			}
		}
		chordsWithoutHandShapes.addAll(chordsForHandShapes);

		for (final Chord chord : chordsWithoutHandShapes) {
			final FractionalPosition position = chord.toFraction(beats).position();
			FractionalPosition endPosition = chord.endPosition().toFraction(beats).position();
			if (chord.length().compareTo(new FractionalPosition(new Fraction(1, 8))) < 0) {
				endPosition = position.add(new Fraction(1, 8));
			}

			final HandShape handShape = new HandShape(position, endPosition, chord.templateId());
			level.handShapes.add(handShape);
		}

		level.handShapes.sort(IConstantFractionalPosition::compareTo);
	}

	private void addMissingAnchors(final Arrangement arrangement, final Level level) {
		final List<EventPoint> sections = filter(arrangement.eventPoints, p -> p.section != null);

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
				final Anchor newAnchor = new Anchor(eventPoint.position());
				level.anchors.add(newAnchor);
				level.anchors.sort(IConstantFractionalPosition::compareTo);

				continue;
			}

			final Anchor lastAnchor = level.anchors.get(lastAnchorId);
			if (lastAnchor.position().compareTo(eventPoint) != 0) {
				final Anchor newAnchor = new Anchor(lastAnchor);
				newAnchor.position(eventPoint.position());
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

	private void removeTailsUnderMinLength(final List<ChordOrNote> sounds) {
		final ImmutableBeatsMap beats = chartData.beats();

		sounds.stream()//
				.flatMap(s -> s.noteInterfaces())//
				.filter(n -> !n.linkNext())//
				.forEach(n -> {
					if (n.endPosition().compareTo(beats.getMinEndPositionAfter(n).toFraction(beats)) < 0) {
						n.endPosition(n.position());
					}
				});
	}

	private void fixLevel(final Arrangement arrangement, final Level level) {
		level.sounds.removeIf(sound -> sound.isChord() //
				&& (sound.chord().templateId() >= arrangement.chordTemplates.size()//
						|| sound.chord().chordNotes.isEmpty()));

		removeDuplicatesFractional(level.anchors);
		removeDuplicatesFractional(level.sounds);
		removeDuplicatesFractional(level.handShapes);

		addMissingAnchors(arrangement, level);
		addMissingHandShapes(level);

		fixNoteLengths(level.sounds);
		fixLengths(level.handShapes);
		removeTailsUnderMinLength(level.sounds);
	}

	public void fixArrangements() {
		final int end = chartTimeHandler.maxTime();
		final FractionalPosition endFractional = FractionalPosition.fromTime(chartData.beats(), end);

		chartData.songChart.beatsMap.makeBeatsUntilSongEnd(end);
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();

		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			removeWrongPositions(arrangement, endFractional);
			for (final Level level : arrangement.levels) {
				fixLevel(arrangement, level);
			}

			DuplicatedChordTemplatesRemover.remove(arrangement);
			UnusedChordTemplatesRemover.remove(arrangement);
			MissingFingersOnChordTemplatesFixer.fix(arrangement);
		}

		chartData.songChart.beatsMap.truncate(chartTimeHandler.maxNonBeatTime());
	}
}
