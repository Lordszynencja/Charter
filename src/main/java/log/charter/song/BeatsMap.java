package log.charter.song;

import static java.lang.Math.abs;
import static log.charter.song.notes.IConstantPosition.findClosestId;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.grid.GridPosition;

public class BeatsMap {
	public int songLengthMs;
	public ArrayList2<Beat> beats = new ArrayList2<>();

	/**
	 * creates base beats map
	 */
	public BeatsMap(final int songLength) {
		this(songLength, true);
	}

	public BeatsMap(final int songLength, final ArrayList2<Beat> beats) {
		songLengthMs = songLength;
		this.beats = beats;
	}

	public BeatsMap(final int songLength, final int startFrom) {
		songLengthMs = songLength;
		beats.add(new Beat(startFrom, 4, 4, true));
		makeBeatsUntilSongEnd();
	}

	public BeatsMap(final int songLength, final boolean fillBeatsForSong) {
		songLengthMs = songLength;
		if (fillBeatsForSong) {
			beats.add(new Beat(0, 4, 4, true));
			makeBeatsUntilSongEnd();
		}
	}

	/**
	 * creates beats map for existing project
	 */
	public BeatsMap(final int songLengthMs, final ChartProject chartProject) {
		this.songLengthMs = songLengthMs;

		beats = chartProject.beats;
	}

	/**
	 * creates beats map for rs xml import
	 */
	public BeatsMap(final int songLengthMs, final SongArrangement songArrangement) {
		this.songLengthMs = songLengthMs;

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

	public BeatsMap(final BeatsMap other) {
		songLengthMs = other.songLengthMs;
		beats = other.beats.map(Beat::new);
	}

	public void makeBeatsUntilSongEnd() {
		final Beat current = beats.getLast();
		if (current.position() > songLengthMs) {
			beats.removeIf(beat -> beat.position() > songLengthMs);
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
		while (pos < songLengthMs) {
			beatInMeasure++;
			beats.add(new Beat(pos, beatsInMeasure, current.noteDenominator, beatInMeasure == beatsInMeasure));

			pos += distance;
			if (beatInMeasure == beatsInMeasure) {
				beatInMeasure = 0;
			}
		}
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
		if (gridPosition.position() != position) {
			gridRemovals--;
		}
		for (int i = 0; i < gridRemovals; i++) {
			gridPosition.previous();
		}

		return gridPosition.position();
	}

	public int getNextPositionFromGridAfter(final int position) {
		return getPositionWithAddedGrid(position, 1);
	}

	public int getNextPositionFromGrid(final int position) {
		return getPositionWithAddedGrid(position, 1);
	}

	public int getPositionFromGridClosestTo(final int position) {
		final GridPosition<Beat> gridPosition = GridPosition.create(beats, position);
		final int leftPosition = gridPosition.position();
		final int rightPosition = gridPosition.next().position();
		if (abs(position - leftPosition) < abs(position - rightPosition)) {
			return leftPosition;

		}
		return rightPosition;
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
		final int beatId = findLastIdBeforeEqual(beats, position);
		if (beatId >= beats.size() - 1) {
			return beatId;
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

	public void setBPM(final int beatId, final double newBPM) {
		for (int i = beats.size() - 1; i > beatId; i--) {
			beats.remove(i);
		}

		final Beat startBeat = beats.getLast();
		final int startPosition = startBeat.position();
		int position = (int) (startPosition + 60_000 / newBPM);
		int createdBeatId = 1;
		while (position <= songLengthMs) {
			final Beat newBeat = new Beat(position, startBeat.beatsInMeasure, startBeat.noteDenominator, false);
			beats.add(newBeat);
			createdBeatId++;
			position = startPosition + (int) (createdBeatId * 60_000 / newBPM);
		}

		fixFirstBeatInMeasures();
	}

	public void setTempo(final int bpmBeatId, final double newBPM) {
		final int previousBeatLength = beats.size() > 1 && bpmBeatId < beats.size() - 1
				? beats.get(bpmBeatId + 1).position() - beats.get(bpmBeatId).position()
				: 500; // Magic number, calculated for 120 BPM

		if (bpmBeatId >= beats.size()) {
			while (beats.getLast().position() <= songLengthMs) {
				appendLastBeat();
			}
		} else {
			final Beat bpmBeat = beats.get(bpmBeatId);

			final int beatLength = beatLengthFromTempoAndDenominator((int) newBPM, bpmBeat.noteDenominator);
			final int deltaBeatLength = beatLength - previousBeatLength;
			final int startPosition = bpmBeat.position();
			int nextStartPosition = startPosition + beatLength;

			final int updatedBeatId = 1; // The next beat will get a new start position
			int i = bpmBeatId + updatedBeatId;
			int beatOffsetFromBpmChanges = deltaBeatLength;

			while (nextStartPosition <= songLengthMs) {
				if (i <= beats.size() - 1) {
					nextStartPosition += beatLength;
					final int originalBeatPosition = beats.get(i).position();
					beats.get(i).position(originalBeatPosition + beatOffsetFromBpmChanges);
					beatOffsetFromBpmChanges += deltaBeatLength;
				} else {
					appendLastBeat();
					nextStartPosition += beatLength;
				}
				i++;
			}
		}
		fixFirstBeatInMeasures();
	}

	public void movePositions(final int start, final int end, final Collection<? extends IPosition> positions) {
		final double startInBeats = getPositionInBeats(start);
		final double endInBeats = getPositionInBeats(end);
		final double add = endInBeats - startInBeats;
		for (final IPosition position : positions) {
			final double positionInBeats = getPositionInBeats(position.position());
			final int newPosition = getPositionForPositionInBeats(positionInBeats + add);
			position.position(newPosition);
		}
	}

	public double findBPM(final Beat beat) {
		return findBPM(beat, findClosestId(beats, beat.position()));
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

	static public int beatLengthFromTempoAndDenominator(final int tempo, final int denominator) {
		// Example: 60 quarter notes per minute in 4/4 -> 1 beat = 1 second beat length
		// if converting to 7/8 the beat length will be (1 s / 8/4 ) -> 0.5 s
		return 60_000 / tempo / (denominator / 4);
	}

	public void appendLastBeat() {
		final Beat lastBeat = beats.getLast();
		final int nextBeatPosition = lastBeat.position() + getLastBeatLength();
		final Beat newBeat = new Beat(nextBeatPosition, lastBeat.beatsInMeasure, lastBeat.noteDenominator, false);
		beats.add(newBeat);
	}

	public int getLastBeatLength() {
		if (beats.size() >= 2) {
			return beats.getLast().position() - beats.get(beats.size() - 2).position();
		} else {
			return 500; // Magic number, calculated for 120 BPM
		}

	}
}
