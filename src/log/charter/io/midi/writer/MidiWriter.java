package log.charter.io.midi.writer;

import static log.charter.io.TickMsConverter.convertToTick;

import java.io.File;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import log.charter.io.Logger;
import log.charter.io.TickMsConverter;
import log.charter.song.Instrument;
import log.charter.song.Song;

public final class MidiWriter {
	public static void writeMidi(final String path, final Song s) {
		try {
			final Song conv = convertToTick(s);

			final Sequence seq = new Sequence(Sequence.PPQ, TickMsConverter.ticksPerBeat);

			TempoWriter.write(conv.tempoMap, seq.createTrack());
			SectionsWriter.write(conv.sections, seq.createTrack());
			for (final Instrument instr : conv.instruments()) {
				InstrumentWriter.write(instr, seq.createTrack());
			}
			VocalsWriter.write(conv.v, seq.createTrack());

			MidiSystem.write(seq, 1, new File(path));
		} catch (final Exception e) {
			Logger.error("Couldn't save midi file", e);
		}

	}
}
