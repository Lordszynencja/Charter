package log.charter.data.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.song.position.virtual.IVirtualConstantPosition.distance;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPosition;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.util.data.Fraction;
import log.charter.util.grid.GridPosition;

public class BeatsMap {
	public enum DistanceType {
		MILISECONDS(Label.DISTANCE_TYPE_MILISECONDS), //
		BEATS(Label.DISTANCE_TYPE_BEATS), //
		NOTES(Label.DISTANCE_TYPE_NOTES);

		public final Label label;

		DistanceType(final Label label) {
			this.label = label;
		}
	}

	public class ImmutableBeatsMap implements List<Beat> {
		@Override
		public int size() {
			return beats.size();
		}

		@Override
		public Beat get(final int beatId) {
			return getBeatSafe(beatId);
		}

		@Override
		public boolean isEmpty() {
			return beats.isEmpty();
		}

		@Override
		public boolean contains(final Object o) {
			return beats.contains(o);
		}

		@Override
		public Iterator<Beat> iterator() {
			return beats.iterator();
		}

		@Override
		public Object[] toArray() {
			return beats.toArray();
		}

		@Override
		public <T> T[] toArray(final T[] a) {
			return beats.toArray(a);
		}

		@Override
		public boolean add(final Beat e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(final Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			return beats.containsAll(c);
		}

		@Override
		public boolean addAll(final Collection<? extends Beat> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(final int index, final Collection<? extends Beat> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Beat set(final int index, final Beat element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(final int index, final Beat element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Beat remove(final int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int indexOf(final Object o) {
			return beats.indexOf(o);
		}

		@Override
		public int lastIndexOf(final Object o) {
			return beats.lastIndexOf(o);
		}

		@Override
		public ListIterator<Beat> listIterator() {
			return beats.listIterator();
		}

		@Override
		public ListIterator<Beat> listIterator(final int index) {
			return beats.listIterator(index);
		}

		@Override
		public List<Beat> subList(final int fromIndex, final int toIndex) {
			return beats.subList(fromIndex, toIndex);
		}

		public double findBPM(final Beat beat) {
			return BeatsMap.this.findBPM(beat);
		}

		public double findBPM(final Beat beat, final int beatId) {
			return BeatsMap.this.findBPM(beat, beatId);
		}

		public void snap(final IVirtualPosition position) {
			position.position(immutable, getPositionFromGridClosestTo(position));
		}

		public IVirtualConstantPosition getPositionWithAddedGrid(final IVirtualConstantPosition position,
				int gridAdditions) {
			final GridPosition<Beat> gridPosition = GridPosition.create(beats, position);
			if (gridPosition.compareTo(position) > 0) {
				gridAdditions--;
			}
			for (int i = 0; i < gridAdditions; i++) {
				gridPosition.next();
			}

			return gridPosition;
		}

		public IVirtualConstantPosition getPositionWithRemovedGrid(final IVirtualConstantPosition position,
				int gridRemovals) {
			final GridPosition<Beat> gridPosition = GridPosition.create(beats, position);
			if (gridPosition.compareTo(position) < 0) {
				gridRemovals--;
			}
			for (int i = 0; i < gridRemovals; i++) {
				gridPosition.previous();
			}

			return gridPosition;
		}

		public IVirtualConstantPosition addGrid(final IVirtualConstantPosition position, final int steps) {
			return steps < 0 ? getPositionWithRemovedGrid(position, -steps)//
					: getPositionWithAddedGrid(position, steps);
		}

		public int findPreviousAnchoredBeat(final int beatId) {
			for (int i = beatId - 1; i > 0; i--) {
				if (beats.get(i).anchor) {
					return i;
				}
			}

			return 0;
		}

		public Integer findNextAnchoredBeat(final int beatId) {
			for (int i = beatId + 1; i < beats.size(); i++) {
				if (beats.get(i).anchor) {
					return i;
				}
			}

			return null;
		}

		public FractionalPosition getPositionFromGridClosestTo(final IVirtualConstantPosition position) {
			final GridPosition<Beat> gridPosition = GridPosition.create(beats, position);
			final FractionalPosition leftPosition = gridPosition.fractionalPosition();
			gridPosition.next();
			final FractionalPosition rightPosition = gridPosition.fractionalPosition();

			final IVirtualConstantPosition distanceToLeft = distance(immutable, position, leftPosition);
			final IVirtualConstantPosition distanceToRight = distance(immutable, position, rightPosition);

			if (IVirtualConstantPosition.compare(immutable, distanceToLeft, distanceToRight) <= 0) {
				return leftPosition;
			}

			return rightPosition;
		}

		public void moveSounds(final Collection<ChordOrNote> sounds, final FractionalPosition toAdd) {
			for (final ChordOrNote sound : sounds) {
				sound.position(sound.position().add(toAdd));

				if (sound.isChord()) {
					for (final ChordNote chordNote : sound.chord().chordNotes.values()) {
						chordNote.endPosition(chordNote.endPosition().add(toAdd));
						chordNote.bendValues.forEach(b -> b.position(b.position().add(toAdd)));
					}
				} else {
					final Note note = sound.note();
					note.endPosition(note.endPosition().add(toAdd));
					note.bendValues.forEach(b -> b.position(b.position().add(toAdd)));
				}
			}
		}

		public void movePositions(final Collection<? extends IVirtualPosition> positions,
				final FractionalPosition toAdd) {
			for (final IVirtualPosition position : positions) {
				final FractionalPosition newPosition = position.toFraction(immutable).position().add(toAdd);
				position.position(immutable, newPosition);

				if (IVirtualPositionWithEnd.class.isAssignableFrom(position.getClass())) {
					final IVirtualPositionWithEnd positionWithEnd = (IVirtualPositionWithEnd) position;
					final FractionalPosition newEndPosition = positionWithEnd.endPosition().toFraction(immutable)
							.position().add(toAdd);
					positionWithEnd.endPosition(immutable, newEndPosition);
				}
			}
		}

		private IVirtualConstantPosition addMiliseconds(final IVirtualConstantPosition position, final int distance) {
			return IVirtualConstantPosition.add(this, position, new Position(distance));
		}

		private IVirtualConstantPosition addBeats(final IVirtualConstantPosition position,
				final FractionalPosition distance) {
			return IVirtualConstantPosition.add(this, position, distance);
		}

		private IVirtualConstantPosition removeNote(final IVirtualConstantPosition position, Fraction distance) {
			FractionalPosition fractionalPosition = position.toFraction(this).position();
			if (fractionalPosition.fraction.numerator > 0) {
				final int noteDenominator = get(fractionalPosition.beatId).noteDenominator;
				final Fraction distanceLeftInBeats = distance.multiply(noteDenominator);
				if (distanceLeftInBeats.compareTo(fractionalPosition.fraction) <= 0) {
					return fractionalPosition.add(distanceLeftInBeats.negate());
				}

				distance = distance.add(fractionalPosition.fraction.divide(noteDenominator));
				fractionalPosition = new FractionalPosition(fractionalPosition.beatId);
			}

			int beatId = fractionalPosition.beatId - 1;
			int noteDenominator = get(beatId).noteDenominator;
			while (distance.compareTo(new Fraction(1, noteDenominator)) > 0) {
				distance = distance.add(new Fraction(-1, noteDenominator));
				beatId--;
				noteDenominator = get(beatId).noteDenominator;
			}

			return new FractionalPosition(beatId, new Fraction(1).add(distance.multiply(noteDenominator).negate()));
		}

		private IVirtualConstantPosition addNote(final IVirtualConstantPosition position, Fraction distance) {
			if (distance.negative()) {
				return removeNote(position, distance.negate());
			}

			FractionalPosition fractionalPosition = position.toFraction(this).position();
			if (fractionalPosition.fraction.numerator > 0) {
				final int noteDenominator = get(fractionalPosition.beatId).noteDenominator;
				final Fraction distanceLeftInBeats = distance.multiply(noteDenominator);
				final Fraction distanceToNextBeat = new Fraction(1).add(fractionalPosition.fraction.negate());
				if (distanceLeftInBeats.compareTo(distanceToNextBeat) <= 0) {
					return fractionalPosition.add(distanceLeftInBeats);
				}

				distance = distance.add(distanceToNextBeat.divide(noteDenominator));
				fractionalPosition = new FractionalPosition(fractionalPosition.beatId + 1);
			}

			int beatId = fractionalPosition.beatId;
			int noteDenominator = get(beatId).noteDenominator;
			while (distance.compareTo(new Fraction(1, noteDenominator)) > 0) {
				distance = distance.add(new Fraction(-1, noteDenominator));
				beatId++;
				noteDenominator = get(beatId).noteDenominator;
			}

			return new FractionalPosition(beatId, distance.multiply(noteDenominator));
		}

		public IVirtualConstantPosition getMaxPositionBefore(final IVirtualConstantPosition position) {
			if (Config.minNoteDistanceType == DistanceType.MILISECONDS) {
				return addMiliseconds(position, -Config.minNoteDistanceFactor);
			}
			if (Config.minNoteDistanceType == DistanceType.BEATS) {
				return addBeats(position, new FractionalPosition(new Fraction(-1, Config.minNoteDistanceFactor)));
			}
			if (Config.minNoteDistanceType == DistanceType.NOTES) {
				return removeNote(position, new Fraction(1, Config.minNoteDistanceFactor));
			}

			return position;
		};

		public IVirtualConstantPosition getMinEndPositionAfter(final IVirtualConstantPosition position) {
			if (Config.minTailLengthType == DistanceType.MILISECONDS) {
				return addMiliseconds(position, Config.minTailLengthFactor);
			}
			if (Config.minTailLengthType == DistanceType.BEATS) {
				return addBeats(position, new FractionalPosition(new Fraction(1, Config.minTailLengthFactor)));
			}
			if (Config.minTailLengthType == DistanceType.NOTES) {
				return addNote(position, new Fraction(1, Config.minTailLengthFactor));
			}

			return position;
		};
	}

	public final ImmutableBeatsMap immutable = new ImmutableBeatsMap();

	public List<Beat> beats = new ArrayList<>();

	/**
	 * creates base beats map
	 */
	public BeatsMap(final double audioLength) {
		beats.add(new Beat(0, 4, 4, true));
		makeBeatsUntilSongEnd(audioLength);
	}

	public BeatsMap(final List<Beat> beats) {
		this.beats = beats;

		for (int i = beats.size() - 2; i >= 0; i--) {
			if (beats.get(i).position() == beats.get(i + 1).position()) {
				beats.remove(i + 1);
			}
		}
	}

	/**
	 * creates beats map for existing project
	 */
	public BeatsMap(final ChartProject chartProject) {
		this(chartProject.beats);
	}

	/**
	 * creates beats map for rs xml import
	 */
	public BeatsMap(final SongArrangement songArrangement) {
		beats = Beat.fromEbeats(songArrangement.ebeats.list);

		int beatsInMeasure = -1;
		int beatCount = 0;
		for (final Beat beat : beats) {
			if (beat.beatsInMeasure != beatsInMeasure) {
				beat.firstInMeasure = true;
				beatsInMeasure = beat.beatsInMeasure;
				beatCount = 0;
			}
			if (beatCount == beatsInMeasure) {
				beat.firstInMeasure = true;
				beatCount = 0;
			}

			beatCount++;
		}
	}

	public void makeBeatsUntilSongEnd(final double audioLength) {
		final Beat current = beats.get(beats.size() - 1);
		if (current.position() > audioLength) {
			return;
		}

		Beat previous;
		double distance;
		if (beats.size() == 1) {
			previous = current;
			distance = 500;
		} else {
			previous = beats.get(beats.size() - 2);
			distance = current.position() - previous.position();
			if (distance < 50) {
				distance = 50;
			}
		}

		double pos = current.position() + distance;
		int beatInMeasure = 0;
		for (int i = beats.size() - 1; i >= 0; i--) {
			if (beats.get(i).firstInMeasure) {
				break;
			}
			beatInMeasure++;
		}

		final int beatsInMeasure = current.beatsInMeasure;
		while (pos < audioLength) {
			beatInMeasure++;
			beats.add(new Beat(pos, beatsInMeasure, current.noteDenominator, beatInMeasure == beatsInMeasure));

			pos += distance;
			if (beatInMeasure == beatsInMeasure) {
				beatInMeasure = 0;
			}
		}
	}

	public void truncate(final double maxTime) {
		beats.removeIf(beat -> beat.position() > maxTime);
	}

	public void fixFirstBeatInMeasures() {
		final List<Beat> beatsFromPreviousMeasure = new ArrayList<>();
		int previousBIM = -1;
		for (int i = 0; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			if (beat.beatsInMeasure != previousBIM) {
				for (final Beat beatFromPreviousMeasure : beatsFromPreviousMeasure) {
					beatFromPreviousMeasure.beatsInMeasure = beatsFromPreviousMeasure.size();
				}
				beat.firstInMeasure = true;
				previousBIM = beat.beatsInMeasure;
				beatsFromPreviousMeasure.clear();
			} else if (beatsFromPreviousMeasure.size() >= previousBIM) {
				beatsFromPreviousMeasure.clear();
				beat.firstInMeasure = true;
			} else {
				beat.firstInMeasure = false;
			}

			beatsFromPreviousMeasure.add(beat);
		}
	}

	public double getPositionInBeats(final int position) {
		final Integer beatId = lastBeforeEqual(beats, new Position(position), IConstantPosition::compareTo).findId();
		if (beatId == null || beatId >= beats.size() - 1) {
			return beats.size() - 1;
		}

		final Beat beat = beats.get(beatId);
		final Beat nextBeat = beats.get(beatId + 1);

		return beatId + 1.0 * (position - beat.position()) / (nextBeat.position() - beat.position());
	}

	public double getPositionForPositionInBeats(final double beatPosition) {
		final int beatId = (int) beatPosition;
		final Beat beat = beats.get(beatId);
		if (beatId >= beats.size() - 1) {
			return beat.position();
		}

		final Beat nextBeat = beats.get(beatId + 1);

		return beat.position() + (nextBeat.position() - beat.position()) * (beatPosition % 1.0);
	}

	public void setBPM(final int beatId, final double newBPM, final double maxBeatPositionCreated) {
		for (int i = beats.size() - 1; i > beatId; i--) {
			beats.remove(i);
		}

		final Beat startBeat = beats.get(beats.size() - 1);
		final double startPosition = startBeat.position();
		double position = startPosition + 60_000 / newBPM;
		int createdBeatId = 1;
		while (position <= maxBeatPositionCreated) {
			final Beat newBeat = new Beat(position, startBeat.beatsInMeasure, startBeat.noteDenominator, false);
			beats.add(newBeat);
			createdBeatId++;
			position = startPosition + createdBeatId * 60_000 / newBPM;
		}

		fixFirstBeatInMeasures();
	}

	public void setBPMWithMaxBeatId(final int beatId, final double newBPM, final int maxBeatId) {
		for (int i = beats.size() - 1; i > beatId; i--) {
			beats.remove(i);
		}

		final Beat startBeat = beats.get(beats.size() - 1);
		final double startPosition = startBeat.position();
		double position = (int) (startPosition + 60_000 / newBPM);
		int createdBeatId = 1;
		while (beatId + createdBeatId <= maxBeatId) {
			final Beat newBeat = new Beat(position, startBeat.beatsInMeasure, startBeat.noteDenominator, false);
			beats.add(newBeat);
			createdBeatId++;
			position = startPosition + createdBeatId * 60_000 / newBPM;
		}

		fixFirstBeatInMeasures();
	}

	public double findBPM(final Beat beat) {
		return findBPM(beat, lastBeforeEqual(beats, beat, IConstantPosition::compareTo).findId(0));
	}

	public double findBPM(final Beat beat, final int beatId) {
		int nextAnchorId = beats.size() - 1;

		for (int i = beatId + 1; i < beats.size(); i++) {
			if (beats.get(i).anchor) {
				nextAnchorId = i;
				break;
			}
		}

		final Beat nextAnchor = beats.get(nextAnchorId);
		final double distancePassed = nextAnchor.position() - beat.position();
		final int beatsPassed = nextAnchorId - beatId;
		return 60_000.0 / distancePassed * beatsPassed;
	}

	public Beat getBeatSafe(final int beatId) {
		if (beats.isEmpty()) {
			return new Beat(0);
		}

		if (beatId < 0) {
			return beats.get(0);
		}
		if (beatId >= beats.size()) {
			return beats.get(beats.size() - 1);
		}

		return beats.get(beatId);
	}

	public void moveBeats(final double chartLength, final double positionDifference) {
		for (final Beat beat : beats) {
			beat.position(max(0, min(chartLength, beat.position() + positionDifference)));
		}

		makeBeatsUntilSongEnd(chartLength);
	}

}
