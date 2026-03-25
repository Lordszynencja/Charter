package log.charter.io.midi;

import static log.charter.io.Logger.error;
import static log.charter.io.midi.MidiTempoEvent.defaultKiloBeatsPerMinute;
import static log.charter.io.midi.MidiTempoEvent.defaultTimeSignature;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Sequence;

import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;

public class MidiBeatsMapReader {
	private static class BeatsAdder {
		private final int resolution;
		private final List<Beat> beats = new ArrayList<>();
		private double position = 0;
		private long beatTime = 0;
		private long time = 0;
		private int beatsCounter = 0;
		private MidiTempoEvent lastEvent = new MidiTempoEvent(0, defaultKiloBeatsPerMinute, defaultTimeSignature);

		public BeatsAdder(final int resolution) {
			this.resolution = resolution;
		}

		private boolean countBeat() {
			beatsCounter++;
			if (beatsCounter > lastEvent.timeSignature.numerator) {
				beatsCounter = 0;
				return true;
			}

			return false;
		}

		private void addBeat(final boolean anchor) {
			final int position = (int) (this.position
					+ (beatTime - time) * 60_000_000.0 / lastEvent.kiloQuarterNotesPerMinute / resolution);
			final boolean firstInMeasure = countBeat();
			final Beat beat = new Beat(position, lastEvent.timeSignature, firstInMeasure, anchor);

			beats.add(beat);
			beatTime += resolution * 4 / lastEvent.timeSignature.denominator;
		}

		public void addBeats(final MidiTempoEvent tempo) {
			while (beatTime < tempo.time) {
				addBeat(beatTime + resolution > tempo.time);
			}

			position = (position
					+ (tempo.time - time) * 60_000_000.0 / lastEvent.kiloQuarterNotesPerMinute / resolution);
			time = tempo.time;
			lastEvent = tempo;
			if (beatTime == tempo.time) {
				addBeat(true);
			}
		}

		public BeatsMap generateBeatsMap() {
			if (beats.isEmpty()) {
				addBeat(true);
			}
			addBeat(false);

			return new BeatsMap(beats);
		}
	}

	public static BeatsMap readBeatsMap(final Sequence seq) {
		final List<MidiTempoEvent> track = MidiTempoEvent.readTempoTrack(seq);
		final int resolution = seq.getResolution();
		if (track.get(0).time != 0) {
			error("first beat is not on zero: " + track.get(0).time);
			return null;
		}

		final BeatsAdder beatsAdder = new BeatsAdder(resolution);

		for (final MidiTempoEvent tempo : track) {
			beatsAdder.addBeats(tempo);
		}

		return beatsAdder.generateBeatsMap();
	}
}
