package log.charter.song;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.round;
import static log.charter.io.Logger.error;

import java.util.ArrayList;
import java.util.List;

import log.charter.io.midi.MidiReader;

public class TempoMap {
	public static double btt(final double t, final int kbpm) {
		return (t * 60000000.0) / kbpm;
	}

	public static void calcBPM(final Tempo t0, final Tempo t1) {
		final int idDiff = t1.id - t0.id;
		final double timeDiff = t1.pos - t0.pos;
		t0.kbpm = (int) ((idDiff * 60000000.0) / timeDiff);
	}

	public static double ttb(final double t, final int kbpm) {
		return (t * kbpm) / 60000000.0;
	}

	public final List<Tempo> tempos = new ArrayList<>();
	public boolean isMs = false;

	public TempoMap() {
		tempos.add(new Tempo(0, 0, 120000, 4));
	}

	public TempoMap(final List<Tempo> tempos) {
		for (final Tempo tmp : tempos) {
			this.tempos.add(new Tempo(tmp));
		}
		if (this.tempos.isEmpty()) {
			this.tempos.add(new Tempo(0, 0, 120000, 4));
		}
		sort();
	}

	public TempoMap(final TempoMap tempoMap) {
		for (final Tempo tmp : tempoMap.tempos) {
			tempos.add(new Tempo(tmp));
		}
		isMs = tempoMap.isMs;
	}

	public void addTempo(final Tempo tmp, final boolean withSorting) {
		tempos.add(tmp);
		if (withSorting) {
			sort();
		}
	}

	public void addTempo(final Tempo tmp, final int id) {
		tempos.add(id, tmp);
	}

	private int closestGridPoint(final double time, final Tempo tmp, final int gridSize) {
		return (int) round(ttb(time - tmp.pos, tmp.kbpm) * gridSize) + (tmp.id * gridSize);
	}

	public double closestGridTime(final double time, final int tmpId, final int gridSize) {
		final Tempo tmp = tempos.get(tmpId);
		final long gridPoint = closestGridPoint(time, tmp, gridSize) - (tmp.id * gridSize);

		if (tmpId < (tempos.size() - 1)) {
			final Tempo nextTmp = tempos.get(tmpId + 1);
			if (gridPoint == ((nextTmp.id - tmp.id) * gridSize)) {
				return nextTmp.pos;
			}
		}
		return tmp.pos + (btt(gridPoint, tmp.kbpm) / gridSize);
	}

	public void convertToMs() {
		if (isMs) {
			return;
		}
		if (tempos.get(0).pos != 0) {
			error("first beat is not on zero: " + tempos.get(0));
		}

		Tempo prev = tempos.get(0);
		for (int i = 1; i < tempos.size(); i++) {
			final Tempo t0 = tempos.get(i);
			t0.pos = prev.pos + (((t0.id - prev.id) * (60000000.0 / prev.kbpm)));

			prev = t0;
		}
		isMs = true;
	}

	public void convertToTick() {
		if (!isMs) {
			return;
		}
		if (tempos.get(0).pos != 0) {
			error("first beat is not on zero: " + tempos.get(0));
		}

		for (int i = 1; i < tempos.size(); i++) {
			final Tempo tmp = tempos.get(i);
			tmp.pos = (tmp.id * MidiReader.ticksPerBeat);
		}
		isMs = false;
	}

	public int findBeatId(final double time) {// TODO binary search
		if (time <= 0) {
			return 0;
		}
		for (int i = 0; i < (tempos.size() - 1); i++) {
			final Tempo tmp = tempos.get(i);

			if ((long) tempos.get(i + 1).pos > time) {
				return (int) (tmp.id + ttb(time - tmp.pos, tmp.kbpm));
			}
		}
		final Tempo tmp = tempos.get(tempos.size() - 1);
		return (int) (tmp.id + ttb(time - tmp.pos, tmp.kbpm));
	}

	public double findBeatTime(final double time) {// TODO binary search
		if (time <= 0) {
			return 0;
		}
		for (int i = 0; i < (tempos.size() - 1); i++) {
			final Tempo tmp = tempos.get(i);

			if ((long) tempos.get(i + 1).pos > time) {
				return tmp.pos + btt(Math.floor(ttb(time - tmp.pos, tmp.kbpm)), tmp.kbpm);
			}
		}
		final Tempo tmp = tempos.get(tempos.size() - 1);
		return tmp.pos + btt(Math.floor(ttb(time - tmp.pos, tmp.kbpm)), tmp.kbpm);
	}

	public double findBeatTimeById(final int id) {// TODO binary search
		if (id < 0) {
			return 0;
		}
		for (int i = 0; i < (tempos.size() - 1); i++) {
			final Tempo tmp = tempos.get(i);

			if (tempos.get(i + 1).id > id) {
				return tmp.pos + btt(id - tmp.id, tmp.kbpm);
			}
		}
		final Tempo tmp = tempos.get(tempos.size() - 1);
		return tmp.pos + btt(id - tmp.id, tmp.kbpm);
	}

	public double findClosestGridPositionForTime(final double time, final boolean useGrid, final int gridSize) {
		if (useGrid) {
			final int tmpId = findTempoId(time);
			final double closestGrid = closestGridTime(time, tmpId, gridSize);
			return closestGrid;
		}
		return time;
	}

	public double findNextBeatTime(final double time) {// TODO binary search
		if (time < 0) {
			return 0;
		}
		for (int i = 0; i < (tempos.size() - 1); i++) {
			final Tempo tmp = tempos.get(i);

			if (tempos.get(i + 1).pos > time) {
				final int id = (int) (tmp.id + ttb((time - tmp.pos) + 1, tmp.kbpm) + 1);
				if (tempos.get(i + 1).id <= id) {
					return tempos.get(i + 1).pos;
				}
				return tmp.pos + btt(id - tmp.id, tmp.kbpm);
			}
		}
		final Tempo tmp = tempos.get(tempos.size() - 1);

		final int id = (int) (tmp.id + ttb((time - tmp.pos) + 1, tmp.kbpm) + 1);
		return tmp.pos + btt(id - tmp.id, tmp.kbpm);
	}

	public double findNextBeatTime(final int time) {// TODO binary search
		if (time < 0) {
			return 0;
		}
		int lastKbpm = 120000;
		for (int i = 0; i < (tempos.size() - 1); i++) {
			final Tempo tmp = tempos.get(i);
			lastKbpm = tmp.kbpm;

			if ((long) tempos.get(i + 1).pos > time) {
				final int id = (int) (tmp.id + ttb((time - tmp.pos) + 1, lastKbpm) + 1);
				if (tempos.get(i + 1).id <= id) {
					return tempos.get(i + 1).pos;
				}
				return tmp.pos + btt(id - tmp.id, lastKbpm);
			}
		}
		final Tempo tmp = tempos.get(tempos.size() - 1);
		lastKbpm = tmp.kbpm;
		final int id = (int) (tmp.id + ttb((time - tmp.pos) + 1, lastKbpm) + 1);
		return tmp.pos + btt(id - tmp.id, lastKbpm);
	}

	public double findNextGridPositionForTime(final double time, final int gridSize) {
		final int tmpId = findTempoId(time);

		final Tempo tmp = tempos.get(tmpId);
		final long gridPoint = (long) ceil(ttb((time + 0.001) - tmp.pos, tmp.kbpm) * gridSize);

		if (tmpId < (tempos.size() - 1)) {
			final Tempo nextTmp = tempos.get(tmpId + 1);
			if (gridPoint == ((nextTmp.id - tmp.id) * gridSize)) {
				return nextTmp.pos;
			}
		}
		return tmp.pos + (btt(gridPoint, tmp.kbpm) / gridSize);
	}

	/**
	 *
	 * @param time
	 *
	 * @return current tempo, new instance of current tempo, next tempo and if tempo
	 *         is new
	 */
	public Object[] findOrCreateClosestTempo(final double time) {// TODO
		final int tmpId = findTempoId(time);
		final Tempo tmp = tempos.get(tmpId);
		final long gridPoint = round(ttb(time - tmp.pos, tmp.kbpm));
		if (gridPoint == 0) {
			if (tmpId == 0) {
				return null;
			}
			return new Object[] { tempos.get(tmpId - 1), tmp,
					tempos.size() > (tmpId + 1) ? tempos.get(tmpId + 1) : null, false };
		}

		final Tempo nextTmp = tempos.size() > (tmpId + 1) ? tempos.get(tmpId + 1) : null;
		if ((nextTmp != null) && (gridPoint == (nextTmp.id - tmp.id))) {
			return new Object[] { tmp, nextTmp, tempos.size() > (tmpId + 2) ? tempos.get(tmpId + 2) : null, false };
		}

		final Tempo newTempo = new Tempo(tmp);
		newTempo.id = (int) (tmp.id + gridPoint);
		newTempo.pos = tmp.pos + btt(gridPoint, tmp.kbpm);

		this.addTempo(newTempo, true);
		return new Object[] { tmp, newTempo, nextTmp, true };
	}

	public double findPreviousGridPositionForTime(final double time, final int gridSize) {
		final int tmpId = findTempoId(time - 0.001);

		final Tempo tmp = tempos.get(tmpId);
		final long gridPoint = (long) floor(ttb((time - 0.001) - tmp.pos, tmp.kbpm) * gridSize);

		if (tmpId < (tempos.size() - 1)) {
			final Tempo nextTmp = tempos.get(tmpId + 1);
			if (gridPoint == ((nextTmp.id - tmp.id) * gridSize)) {
				return nextTmp.pos;
			}
		}
		return tmp.pos + (btt(gridPoint, tmp.kbpm) / gridSize);
	}

	public Tempo findTempo(final double time) {
		return tempos.get(findTempoId(time));
	}

	public int findTempoId(final double time) {// TODO binary search
		if (time <= 0) {
			return 0;
		}

		int result = 0;
		for (int i = 0; i < tempos.size(); i++) {
			final Tempo tmp = tempos.get(i);

			if (tmp.pos > time) {
				return result;
			}
			result = i;
		}

		return result;
	}

	public List<Double> getGridPositionsFromTo(final int gridSize, final double start, final double end) {// TODO
		final List<Double> grids = new ArrayList<>();

		int tmpId = findTempoId(start);
		final int endTmpId = findTempoId(end);
		Tempo tmp = tempos.get(tmpId);
		final Tempo endTmp = tempos.get(endTmpId);
		int gridPoint = closestGridPoint(start, tmp, gridSize);
		final int lastGridPoint = closestGridPoint(end, endTmp, gridSize);
		int nextTmpGrid = tmpId < (tempos.size() - 1) ? tempos.get(tmpId + 1).id * gridSize : -1;
		double gridDist = btt(1.0 / gridSize, tmp.kbpm);
		double pos = tmp.pos + (gridDist * (gridPoint - (tmp.id * gridSize)));

		while (gridPoint <= lastGridPoint) {
			if (gridPoint == nextTmpGrid) {
				tmpId++;
				tmp = tempos.get(tmpId);
				nextTmpGrid = tmpId < (tempos.size() - 1) ? tempos.get(tmpId + 1).id * gridSize : -1;
				pos = tmp.pos;
				gridDist = btt(1.0 / gridSize, tmp.kbpm);
			}
			grids.add(pos);
			pos += gridDist;
			gridPoint++;
		}

		return grids;
	}

	public Tempo getTempoById(final int id) {
		return tempos.get(id);
	}

	public void join() {
		final List<Tempo> newTempos = new ArrayList<>(tempos.size());
		int i;
		for (i = 0; i < (tempos.size() - 1); i++) {
			final Tempo tmp = tempos.get(i);
			final Tempo nextTmp = tempos.get(i + 1);
			if (tmp.pos == nextTmp.pos) {
				tmp.kbpm = tmp.kbpm > 0 ? tmp.kbpm : nextTmp.kbpm;
				tmp.beats = tmp.beats > 0 ? tmp.beats : nextTmp.beats;
				newTempos.add(tmp);
				i++;
			} else {
				newTempos.add(tmp);
			}
		}
		if (i == (tempos.size() - 1)) {
			newTempos.add(tempos.get(tempos.size() - 1));
		}

		int lastKbpm = 120000;
		int lastBeats = 4;
		for (i = 0; i < newTempos.size(); i++) {
			final Tempo tmp = newTempos.get(i);
			if (tmp.kbpm <= 0) {
				tmp.kbpm = lastKbpm;
			}
			if (tmp.beats <= 0) {
				tmp.beats = lastBeats;
			}

			lastKbpm = tmp.kbpm;
			lastBeats = tmp.beats;
		}

		tempos.clear();
		tempos.addAll(newTempos);
	}

	public void sort() {
		tempos.sort(null);
	}
}
