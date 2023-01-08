package log.charter.io.midi;

import static log.charter.io.Logger.debug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import log.charter.song.TempoMap;

public final class MidiReader {

	public static final int ticksPerBeat = 1;

	public static TempoMap readMidi(final String path) throws InvalidMidiDataException, IOException {
		return new MidiReader(path).read();
	}

	private final List<MidTrack> tracks;

	private MidiReader(final String path) throws InvalidMidiDataException, IOException {
		final Sequence seq = MidiSystem.getSequence(new File(path));
		final double scaler = ((double) ticksPerBeat) / seq.getResolution();

		tracks = new ArrayList<>(seq.getTracks().length);
		boolean isTempo = true;
		for (final Track t : seq.getTracks()) {
			tracks.add(new MidTrack(t, isTempo, scaler));
			isTempo = false;
		}
		debug("Sequence loaded from " + path);
	}

	private TempoMap read() {
		TempoMap tempoMap = new TempoMap();

		for (final MidTrack t : tracks) {
			switch (t.type) {
			case TEMPO:
				tempoMap = new TempoMap(TempoReader.read(t));
				tempoMap.join();
				break;
			default:
				break;
			}
		}

		tempoMap.convertToMs();

		return tempoMap;
	}
}
