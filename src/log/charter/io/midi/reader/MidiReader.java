package log.charter.io.midi.reader;

import static log.charter.io.Logger.debug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import log.charter.io.TickMsConverter;
import log.charter.io.midi.MidTrack;
import log.charter.song.Instrument.InstrumentType;
import log.charter.song.Song;
import log.charter.song.TempoMap;

public final class MidiReader {

	public static Song readMidi(final String path) throws InvalidMidiDataException, IOException {
		return new MidiReader(path).read();
	}

	private final List<MidTrack> tracks;

	private MidiReader(final String path) throws InvalidMidiDataException, IOException {
		final Sequence seq = MidiSystem.getSequence(new File(path));
		final double scaler = ((double) TickMsConverter.ticksPerBeat) / seq.getResolution();

		tracks = new ArrayList<>(seq.getTracks().length);
		boolean isTempo = true;
		for (final Track t : seq.getTracks()) {
			tracks.add(new MidTrack(t, isTempo, scaler));
			isTempo = false;
		}
		debug("Sequence loaded from " + path);
	}

	private Song read() {
		final Song s = new Song();

		for (final MidTrack t : tracks) {
			switch (t.type) {
			case TEMPO:
				s.tempoMap = new TempoMap(TempoReader.read(t));
				s.tempoMap.join();
				break;
			case GUITAR:
				s.g = InstrumentReader.read(t, InstrumentType.GUITAR);
				break;
			case GUITAR_COOP:
				s.gc = InstrumentReader.read(t, InstrumentType.GUITAR_COOP);
				break;
			case GUITAR_RHYTHM:
				s.gr = InstrumentReader.read(t, InstrumentType.GUITAR_RHYTHM);
				break;
			case BASS:
				s.b = InstrumentReader.read(t, InstrumentType.BASS);
				break;
			case DRUMS:
			case REAL_DRUMS:
				s.d = InstrumentReader.read(t, InstrumentType.DRUMS);
				break;
			case KEYS:
				s.k = InstrumentReader.read(t, InstrumentType.KEYS);
				break;
			case VOCALS:
				s.v = VocalsReader.read(t);
				break;
			case EVENTS:
				s.sections = SectionsReader.read(t);
				break;
			default:
				break;
			}
		}

		return TickMsConverter.convertToMs(s);
	}
}
