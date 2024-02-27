package log.charter.io.midi;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import log.charter.io.Logger;
import log.charter.song.Beat;
import log.charter.song.BeatsMap;
import log.charter.util.CollectionUtils.ArrayList2;

public class MidiToBeatsMap {
	public static BeatsMap getBeatsMap(final String path, final int songLength) {
		try {
			final TempoMap tempoMap = MidiReader.readMidi(path);

			final ArrayList2<Beat> beats = new ArrayList2<>();
			int kbpm = -1;
			int id = -1;

			for (final Tempo tempo : tempoMap.tempos) {
				while (tempo.id > id + 1) {
					final Beat lastBeat = beats.getLast();
					final int nextPosition = lastBeat.position() + 60_000_000 / kbpm;
					final Beat intermediateBeat = new Beat(lastBeat);
					intermediateBeat.position(nextPosition);
					beats.add(intermediateBeat);
					id++;
				}
				final Beat beat = new Beat((int) tempo.pos, tempo.numerator, tempo.denominator, false,
						tempo.kbpm != kbpm);
				beats.add(beat);
				kbpm = tempo.kbpm;
				id = tempo.id;
			}

			final BeatsMap beatsMap = new BeatsMap(songLength, beats);
			beatsMap.makeBeatsUntilSongEnd();
			beatsMap.fixFirstBeatInMeasures();

			return beatsMap;
		} catch (InvalidMidiDataException | IOException e) {
			Logger.error("Couldn't load beats map for path " + path, e);
			return null;
		}
	}
}
