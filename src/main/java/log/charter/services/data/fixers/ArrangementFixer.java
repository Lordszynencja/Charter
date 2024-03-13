package log.charter.services.data.fixers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.SectionType;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.position.IConstantFractionalPosition;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.IPosition;
import log.charter.data.song.position.IPositionWithLength;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.CollectionUtils;
import log.charter.util.collections.ArrayList2;

public class ArrangementFixer {
	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;

	private void removeWrongPositions(final Arrangement arrangement, final int start, final int end) {
		final Predicate<IConstantPosition> invalidPositionCheck = p -> p.position() < start || p.position() > end;

		arrangement.eventPoints.removeIf(invalidPositionCheck);
		arrangement.toneChanges.removeIf(invalidPositionCheck);
		for (final Level level : arrangement.levels) {
			level.anchors.removeIf(anchor -> invalidPositionCheck.test(anchor.positionAsPosition(chartData.beats())));
			level.sounds.removeIf(invalidPositionCheck);
			level.handShapes.removeIf(invalidPositionCheck);
		}
	}

	private void removeDuplicates(final List<? extends IConstantPosition> positions) {
		final List<IConstantPosition> positionsToRemove = new ArrayList<>();
		for (int i = 1; i < positions.size(); i++) {
			if (positions.get(i).position() == positions.get(i - 1).position()) {
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
		final LinkedList<Chord> chordsForHandShapes = level.sounds.stream()//
				.filter(chordOrNote -> chordOrNote.isChord() && !chordOrNote.chord().splitIntoNotes)//
				.map(chordOrNote -> chordOrNote.chord())//
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

			final Integer sectionId = lastBeforeEqual(sections, eventPoint).findId(minNoteDistance);
			if (sectionId != null && sections.get(sectionId).section == SectionType.NO_GUITAR) {
				continue;
			}

			final Integer lastAnchorId = CollectionUtils
					.lastBeforeEqual(level.anchors, eventPoint.positionAsFraction(chartData.beats())).findId();
			if (lastAnchorId == null) {
				continue;
			}

			final Anchor lastAnchor = level.anchors.get(lastAnchorId);
			if (lastAnchor.position(chartData.beats()) != eventPoint.position()) {
				final Anchor newAnchor = new Anchor(lastAnchor);
				newAnchor.fractionalPosition(eventPoint.positionAsFraction(chartData.beats()));
				level.anchors.add(newAnchor);
				level.anchors.sort(null);
			}
		}
	}

	public static void fixNoteLength(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		if (note.linkNext()) {
			LinkedNotesFixer.fixLinkedNote(note, id, sounds);
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

	public static void fixSoundLength(final int id, final List<ChordOrNote> sounds) {
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

	public void fixLengths(final List<? extends IPositionWithLength> positions) {
		if (positions.isEmpty()) {
			return;
		}

		for (int i = 0; i < positions.size() - 1; i++) {
			final IPositionWithLength position = positions.get(i);
			final IPositionWithLength nextPosition = positions.get(i + 1);
			position.endPosition(min(nextPosition.position() - Config.minNoteDistance, position.endPosition()));
		}

		final IPositionWithLength lastPosition = positions.get(positions.size() - 1);
		lastPosition.endPosition(min(chartTimeHandler.maxTime(), lastPosition.endPosition()));
	}

	private void fixLevel(final Arrangement arrangement, final Level level) {
		level.sounds//
				.stream().filter(chordOrNote -> chordOrNote.isNote())//
				.map(chordOrNote -> chordOrNote.note())//
				.forEach(note -> note.length(note.length() >= Config.minTailLength ? note.length() : 0));

		level.sounds
				.removeIf(sound -> sound.isChord() && sound.chord().templateId() >= arrangement.chordTemplates.size());

		removeDuplicatesFractional(level.anchors);
		removeDuplicates(level.sounds);
		removeDuplicates(level.handShapes);

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
