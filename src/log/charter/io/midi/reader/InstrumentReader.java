package log.charter.io.midi.reader;

import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;

import java.util.ArrayList;
import java.util.List;

import log.charter.io.midi.MidTrack;
import log.charter.io.midi.reader.instrumentReaders.DrumsInstrumentReader;
import log.charter.io.midi.reader.instrumentReaders.GuitarInstrumentReader;
import log.charter.song.Instrument;
import log.charter.song.Instrument.InstrumentType;
import log.charter.song.Note;

public class InstrumentReader {

	protected static void joinNoteBeginningWithEnd(final List<List<List<Note>>> notes) {
		debug("joining notes start and end");
		for (final List<List<Note>> diffNotes : notes) {
			for (final List<Note> laneNotes : diffNotes) {
				final int n = laneNotes.size();
				final List<Note> newNotes = new ArrayList<>(n / 2);
				for (int j = 0; j < n;) {
					try {
						final Note nStart = laneNotes.get(j++);
						final Note nEnd = laneNotes.get(j++);
						nStart.setLength(nEnd.pos - nStart.pos);
						newNotes.add(nStart);
					} catch (final IndexOutOfBoundsException e) {
						error("Unfinished note");
					}
				}
				laneNotes.clear();
				laneNotes.addAll(newNotes);
			}
		}
	}

	public static Instrument read(final MidTrack t, final InstrumentType type) {
		switch (type) {
		case GUITAR:
		case GUITAR_COOP:
		case GUITAR_RHYTHM:
		case BASS:
		case KEYS:
			return new GuitarInstrumentReader(type).read(t);
		case DRUMS:
			return new DrumsInstrumentReader(type).read(t);
		default:
			error("Unknown instrument type " + type.name());
			return null;
		}
	}

	protected final Instrument instr;

	protected InstrumentReader(final InstrumentType type) {
		instr = new Instrument(type);
	}

}
