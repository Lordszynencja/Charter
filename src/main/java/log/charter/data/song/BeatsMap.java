package log.charter.data.song;

import static log.charter.data.song.position.IVirtualConstantPosition.distance;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.IPosition;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.song.position.IVirtualPosition;
import log.charter.data.song.position.Position;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.util.collections.ArrayList2;
import log.charter.util.grid.GridPosition;

public class BeatsMap {
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

			if (position.isPosition()) {
				final IPosition p = position.asPosition().get();
				p.position(this, getPositionFromGridClosestTo(p));
			}
		}

		public int getPositionWithAddedGrid(final int position, final int gridAdditions) {
			return BeatsMap.this.getPositionWithAddedGrid(position, gridAdditions);
		}

		public int getPositionWithRemovedGrid(final int position, final int gridRemovals) {
			return BeatsMap.this.getPositionWithRemovedGrid(position, gridRemovals);
		}

		public int getNextPositionFromGrid(final int position) {
			return getPositionWithAddedGrid(position, 1);
		}

		public IVirtualConstantPosition getPositionFromGridClosestTo(final IVirtualConstantPosition position) {
			final GridPosition<Beat> gridPosition = GridPosition.create(beats, position);
			final IVirtualConstantPosition leftPosition = gridPosition;
			final IVirtualConstantPosition rightPosition = gridPosition.next();

			final IVirtualConstantPosition distanceToLeft = distance(immutable, position, leftPosition);
			final IVirtualConstantPosition distanceToRight = distance(immutable, position, rightPosition);

			if (IVirtualConstantPosition.compare(immutable, distanceToLeft, distanceToRight) <= 0) {
				return leftPosition;
			}

			return rightPosition;
		}

		public void movePositions(final Collection<? extends IVirtualPosition> positions,
				final FractionalPosition toAdd) {
			for (final IVirtualPosition position : positions) {
				final FractionalPosition newPosition = position.positionAsFraction(immutable).fractionalPosition()
						.add(toAdd);

				position.position(immutable, newPosition);
			}
		}
	}

	public final ImmutableBeatsMap immutable = new ImmutableBeatsMap();

	public ArrayList2<Beat> beats = new ArrayList2<>();

	/**
	 * creates base beats map
	 */
	public BeatsMap(final int audioLength) {
		beats.add(new Beat(0, 4, 4, true));
		makeBeatsUntilSongEnd(audioLength);
	}

	public BeatsMap(final ArrayList2<Beat> beats) {
		this.beats = beats;
	}

	/**
	 * creates beats map for existing project
	 */
	public BeatsMap(final ChartProject chartProject) {
		beats = chartProject.beats;
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

	public void makeBeatsUntilSongEnd(final int audioLength) {
		final Beat current = beats.getLast();
		if (current.position() > audioLength) {
			return;
		}

		Beat previous;
		int distance;
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

		int pos = current.position() + distance;
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

	public void truncate(final int audioLength) {
		beats.removeIf(beat -> beat.position() > audioLength);
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

	public int getPositionWithAddedGrid(final int position, final int gridAdditions) {
		final GridPosition<Beat> gridPosition = GridPosition.create(beats, position);
		for (int i = 0; i < gridAdditions; i++) {
			gridPosition.next();
		}

		return gridPosition.position();
	}

	public int getPositionWithRemovedGrid(final int position, int gridRemovals) {
		final GridPosition<Beat> gridPosition = GridPosition.create(beats, position);
		if (gridPosition.position() < position) {
			gridRemovals--;
		}
		for (int i = 0; i < gridRemovals; i++) {
			gridPosition.previous();
		}

		return gridPosition.position();
	}

	public int getNextPositionFromGrid(final int position) {
		return getPositionWithAddedGrid(position, 1);
	}

	public IVirtualConstantPosition getPositionFromGridClosestTo(final IVirtualConstantPosition position) {
		return immutable.getPositionFromGridClosestTo(position);
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

	public double getPositionInBeats(final int position) {
		final Integer beatId = lastBeforeEqual(beats, new Position(position), IConstantPosition::compareTo).findId();
		if (beatId == null || beatId >= beats.size() - 1) {
			return beats.size() - 1;
		}

		final Beat beat = beats.get(beatId);
		final Beat nextBeat = beats.get(beatId + 1);

		return beatId + 1.0 * (position - beat.position()) / (nextBeat.position() - beat.position());
	}

	public int getPositionForPositionInBeats(final double beatPosition) {
		final int beatId = (int) beatPosition;
		final Beat beat = beats.get(beatId);
		if (beatId >= beats.size() - 1) {
			return beat.position();
		}

		final Beat nextBeat = beats.get(beatId + 1);

		return (int) (beat.position() + (nextBeat.position() - beat.position()) * (beatPosition % 1.0));
	}

	public void setBPM(final int beatId, final double newBPM, final int audioLength) {
		for (int i = beats.size() - 1; i > beatId; i--) {
			beats.remove(i);
		}

		final Beat startBeat = beats.getLast();
		final int startPosition = startBeat.position();
		int position = (int) (startPosition + 60_000 / newBPM);
		int createdBeatId = 1;
		while (position <= audioLength) {
			final Beat newBeat = new Beat(position, startBeat.beatsInMeasure, startBeat.noteDenominator, false);
			beats.add(newBeat);
			createdBeatId++;
			position = startPosition + (int) (createdBeatId * 60_000 / newBPM);
		}

		fixFirstBeatInMeasures();
	}

	public void movePositions(final Collection<? extends IVirtualPosition> positions,
			final IVirtualConstantPosition start, final IVirtualConstantPosition end) {
		final FractionalPosition toAdd = start.positionAsFraction(immutable).fractionalPosition()//
				.add(end.positionAsFraction(immutable).fractionalPosition());

		immutable.movePositions(positions, toAdd);
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
		final int distancePassed = nextAnchor.position() - beat.position();
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
			return beats.getLast();
		}

		return beats.get(beatId);
	}
}
