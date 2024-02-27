package log.charter.io.midi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import log.charter.io.midi.MidTrack.TrackType;

public final class MidiReader {
	public static TempoMap readMidi(final String path) throws InvalidMidiDataException, IOException {
		return new MidiReader(path).readTempoMap();
	}

	private final List<MidTrack> tracks;

	private MidiReader(final String path) throws InvalidMidiDataException, IOException {
		final Sequence seq = MidiSystem.getSequence(new File(path));
		final double scaler = 1.0 / seq.getResolution();

		tracks = new ArrayList<>(seq.getTracks().length);
		boolean isTempo = true;
		for (final Track t : seq.getTracks()) {
			tracks.add(new MidTrack(t, isTempo, scaler));
			isTempo = false;
		}
	}

	private TempoMap readTempoMap() {
		for (final MidTrack t : tracks) {
			if (t.type != TrackType.TEMPO) {
				continue;
			}

			final TempoMap tempoMap = new TempoMap(MidiTempoReader.read(t));
			tempoMap.join();
			tempoMap.convertToMs();
			return tempoMap;
		}

		return new TempoMap();
	}
}
