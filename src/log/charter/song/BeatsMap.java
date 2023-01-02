package log.charter.song;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static log.charter.song.notes.IPosition.findFirstIdAfter;

import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.RocksmithChartProject;
import log.charter.util.CollectionUtils.ArrayList2;

public class BeatsMap {
	public int songLengthMs;
	public boolean useGrid = true;
	public int gridSize = 4;

	public ArrayList2<Beat> beats = new ArrayList2<>();

	/**
	 * creates beats map for new project
	 */
	public BeatsMap(final int songLength) {
		songLengthMs = songLength;

		beats.add(new Beat(0, 4, true));
		makeBeatsUntilSongEnd();
	}

	/**
	 * creates beats map for existing project
	 */
	public BeatsMap(final int songLengthMs, final RocksmithChartProject rocksmithChartProject) {
		this.songLengthMs = songLengthMs;

		beats = rocksmithChartProject.beats;
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
		Beat current = beats.getLast();
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

		final int beatsInMeasure = current.beatsInMeasure;
		int pos = current.position() + distance;
		int beatInMeasure = 0;
		for (int i = beats.size() - 1; i >= 0; i--) {
			if (beats.get(i).firstInMeasure) {
				break;
			}
			beatInMeasure++;
		}

		while (pos < songLengthMs) {
			beatInMeasure++;
			previous = current;
			current = new Beat(pos, beatsInMeasure, beatInMeasure == beatsInMeasure);
			beats.add(current);

			pos += distance;
			if (beatInMeasure == beatsInMeasure) {
				beatInMeasure = 0;
			}
		}
	}

	public void fixFirstBeatInMeasures() {
		int previousBIM = -1;
		int count = 0;
		for (int i = 0; i < beats.size(); i++) {
			count++;
			final Beat beat = beats.get(i);
			if (beat.beatsInMeasure != previousBIM) {
				beat.firstInMeasure = true;
				previousBIM = beat.beatsInMeasure;
				count = 0;
			} else if (count == previousBIM) {
				beat.firstInMeasure = true;
				count = 0;
			}
		}
	}

	public void changeBeatsInMeasure(final int id, final int newBeatsInMeasure) {
		final Beat beat = beats.get(id);
		final int oldBeatsInMeasure = beat.beatsInMeasure;
		int count = oldBeatsInMeasure;
		while (id < beats.size() && beat.beatsInMeasure == oldBeatsInMeasure) {
			beat.beatsInMeasure = newBeatsInMeasure;
			if (count == newBeatsInMeasure) {
				beat.firstInMeasure = true;
				count = 0;
			}
			count++;
		}
	}

	public int getPositionWithAddedGrid(final int position, int gridAdditions, final int gridSize) {
		if (!useGrid) {
			return position + 10 * gridAdditions;
		}

		int nextBeatId = findFirstIdAfter(beats, position);
		if (nextBeatId == -1) {
			return beats.getLast().position();
		}
		if (nextBeatId == 0) {
			gridAdditions--;
			nextBeatId = 1;
		}

		Beat next = beats.get(nextBeatId);
		Beat current = beats.get(nextBeatId - 1);

		next = beats.get(nextBeatId);
		current = beats.get(nextBeatId - 1);

		int beatLength = next.position() - current.position();
		final int distanceInBeat = position - current.position();
		int gridInBeat = (int) floor(1.0 * distanceInBeat * gridSize / beatLength + 0.1);
		while (gridAdditions > 0) {
			gridInBeat++;
			gridAdditions--;
		}

		if (gridInBeat >= gridSize) {
			nextBeatId += gridInBeat / gridSize;
			gridInBeat %= gridSize;
			if (nextBeatId >= beats.size()) {
				return beats.getLast().position();
			}

			next = beats.get(nextBeatId);
			current = beats.get(nextBeatId - 1);
		}

		beatLength = next.position() - current.position();
		return current.position() + beatLength * gridInBeat / gridSize;
	}

	public int getPositionWithAddedGrid(final int position, final int gridAdditions) {
		return this.getPositionWithAddedGrid(position, gridAdditions, gridSize);
	}

	public int getPositionWithRemovedGrid(final int position, int gridRemovals) {
		if (!useGrid) {
			return position - 10 * gridRemovals;
		}

		int nextBeatId = findFirstIdAfter(beats, position);
		if (nextBeatId == 0) {
			return beats.get(0).position();
		}
		if (nextBeatId == -1) {
			gridRemovals--;
			nextBeatId = beats.size() - 1;
		}

		Beat next = beats.get(nextBeatId);
		Beat current = beats.get(nextBeatId - 1);

		next = beats.get(nextBeatId);
		current = beats.get(nextBeatId - 1);

		int beatLength = next.position() - current.position();
		final int distanceInBeat = position - current.position();
		int gridInBeat = (int) ceil(1.0 * distanceInBeat * gridSize / beatLength - 0.1);
		while (gridRemovals > 0) {
			gridInBeat--;
			gridRemovals--;
		}

		while (gridInBeat < 0) {
			nextBeatId--;
			gridInBeat += gridSize;
			if (nextBeatId < 1) {
				return beats.get(0).position();
			}

			next = beats.get(nextBeatId);
			current = beats.get(nextBeatId - 1);
		}

		beatLength = next.position() - current.position();
		return current.position() + beatLength * gridInBeat / gridSize;
	}

	public int getNextPositionFromGridAfter(final int position) {
		if (!useGrid) {
			return position + 10;
		}

		final int nextBeatId = findFirstIdAfter(beats, position);
		if (nextBeatId == -1) {
			return beats.getLast().position();
		}
		if (nextBeatId == 0) {
			return beats.get(0).position();
		}

		final int previousBeatPosition = beats.get(nextBeatId - 1).position();
		final int beatSize = beats.get(nextBeatId).position() - previousBeatPosition;
		final int distanceInBeat = position - previousBeatPosition;
		int gridInBeat = (int) Math.round(1.0 * distanceInBeat * gridSize / beatSize);
		gridInBeat++;

		return previousBeatPosition + beatSize * gridInBeat / gridSize;
	}

	public int getPositionFromGridClosestTo(final int position) {
		if (!useGrid) {
			return position;
		}

		final int nextBeatId = findFirstIdAfter(beats, position);
		if (nextBeatId == -1) {
			return beats.getLast().position();
		}
		if (nextBeatId == 0) {
			return beats.get(0).position();
		}

		final int previousBeatPosition = beats.get(nextBeatId - 1).position();
		final int beatSize = beats.get(nextBeatId).position() - previousBeatPosition;
		final int distanceInBeat = position - previousBeatPosition;
		final int gridInBeat = (int) Math.round(1.0 * distanceInBeat * gridSize / beatSize);

		return previousBeatPosition + beatSize * gridInBeat / gridSize;
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
}
