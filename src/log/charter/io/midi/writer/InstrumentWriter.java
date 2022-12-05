package log.charter.io.midi.writer;

import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;
import static log.charter.io.midi.NoteIds.EVENT_DRUM_ROLL;
import static log.charter.io.midi.NoteIds.EVENT_SOLO;
import static log.charter.io.midi.NoteIds.EVENT_SP;
import static log.charter.io.midi.NoteIds.EVENT_SPECIAL_DRUM_ROLL;
import static log.charter.io.midi.NoteIds.getNoteId;
import static log.charter.util.ByteUtils.getBit;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import log.charter.io.midi.MidTrack.TrackType;
import log.charter.song.Event;
import log.charter.song.Instrument;
import log.charter.song.Instrument.InstrumentType;
import log.charter.song.Note;

public class InstrumentWriter {
	private static void addNoteMessage(final Track track, final byte note, final boolean start, final double pos)
			throws InvalidMidiDataException {
		final ShortMessage msgStart = new ShortMessage(-112, note, start ? 100 : 0);
		track.add(new MidiEvent(msgStart, Math.round(pos)));
	}

	private static void addNote(final int note, final double pos, final double end, final Track track)
			throws InvalidMidiDataException {
		if (note == -1) {
			error("INVALID NOTE");
		}
		addNoteMessage(track, (byte) note, true, pos);
		addNoteMessage(track, (byte) note, false, end);
	}

	private enum SysexMessageType {
		OPEN_NOTE((byte) 1), //
		TAP((byte) 4), //
		YELLOW_BOTH((byte) 17), //
		BLUE_BOTH((byte) 18), //
		GREEN_BOTH((byte) 19);

		private final byte value;

		private SysexMessageType(final byte value) {
			this.value = value;
		}

		public byte[] start(final byte diff, final byte mod) {
			return new byte[] { -16, 80, 83, 0, 0, diff, (byte) (value + mod), 1, -9 };
		}

		public byte[] end(final byte diff, final byte mod) {
			return new byte[] { -16, 80, 83, 0, 0, diff, (byte) (value + mod), 0, -9 };
		}
	}

	private static void addSysexMessage(final Track track, final byte[] data, final double pos)
			throws InvalidMidiDataException {
		final SysexMessage msg = new SysexMessage(data, data.length);
		track.add(new MidiEvent(msg, Math.round(pos)));
	}

	private static void addSysexNote(final SysexMessageType message, final int diff, final double pos, final double end,
			final Track track, final byte mod) throws InvalidMidiDataException {
		addSysexMessage(track, message.start((byte) diff, mod), pos);
		addSysexMessage(track, message.end((byte) diff, mod), end);
	}

	private static void writeGuitar(final Instrument instrument, final Track track) throws InvalidMidiDataException {
		final byte[] bytes = TrackType.fromInstrumentType(instrument.type).partName.getBytes();
		final MetaMessage msg = new MetaMessage();
		msg.setMessage(3, bytes, bytes.length);
		track.add(new MidiEvent(msg, 0));

		writeSP(instrument.sp, track);
		writeTap(instrument.tap, track);
		writeSolo(instrument.solo, track);
		writeGuitarNotes(instrument.notes, track);
	}

	private static void writeDrums(final Instrument instrument, final Track track) throws InvalidMidiDataException {
		final byte[] bytes = TrackType.fromInstrumentType(instrument.type).partName.getBytes();
		final MetaMessage msg = new MetaMessage();
		msg.setMessage(3, bytes, bytes.length);
		track.add(new MidiEvent(msg, 0));

		writeSP(instrument.sp, track);
		writeSolo(instrument.solo, track);
		writeDrumRoll(instrument.drumRoll, track);
		writeSpecialDrumRoll(instrument.specialDrumRoll, track);
		writeDrumsNotes(instrument.notes, track);
	}

	private static void writeKeys(final Instrument instrument, final Track track) throws InvalidMidiDataException {
		final byte[] bytes = TrackType.fromInstrumentType(instrument.type).partName.getBytes();
		final MetaMessage msg = new MetaMessage();
		msg.setMessage(3, bytes, bytes.length);
		track.add(new MidiEvent(msg, 0));

		writeSP(instrument.sp, track);
		writeSolo(instrument.solo, track);
		writeKeysNotes(instrument.notes, track);
	}

	public static void write(final Instrument instrument, final Track track) throws InvalidMidiDataException {
		debug("Writing " + instrument.type);

		if (instrument.type.isGuitarType()) {
			writeGuitar(instrument, track);
		} else if (instrument.type.isDrumsType()) {
			writeDrums(instrument, track);
		} else if (instrument.type.isKeysType()) {
			writeKeys(instrument, track);
		}

		debug("Writing " + instrument.type + " finished");
	}

	private static void writeGuitarNotes(final List<List<Note>> notes, final Track track)
			throws InvalidMidiDataException {
		for (int diffId = 0; diffId < 4; diffId++) {
			final List<Note> diff = notes.get(diffId);
			for (final Note n : diff) {
				if (n.notes == 0) {
					addNote(getNoteId(InstrumentType.GUITAR, diffId, 0), n.pos, n.pos + n.getLength(), track);
					addSysexNote(SysexMessageType.OPEN_NOTE, diffId, n.pos, n.pos + n.getLength(), track, (byte) 0);
				} else {
					for (int i = 0; i < 5; i++) {
						if (getBit(n.notes, i)) {
							addNote(getNoteId(InstrumentType.GUITAR, diffId, i), n.pos, n.pos + n.getLength(), track);
						}
					}
				}

				addNote(getNoteId(InstrumentType.GUITAR, diffId, n.hopo ? 5 : 6), n.pos, n.pos + n.getLength(), track);
			}
		}
	}

	private static void writeDrumNoteColorCymbalTom(final Track track, final Note n, final int diff, final int bit,
			final int lane, final boolean tom, final boolean cymbal, final SysexMessageType type)
			throws InvalidMidiDataException {
		if (getBit(n.notes, bit)) {
			if (tom) {
				if (cymbal) {
					addSysexNote(type, diff, n.pos, n.pos + n.getLength(), track, (byte) 0);
				} else {
					addNote(getNoteId(InstrumentType.DRUMS, diff, lane), n.pos, n.pos + n.getLength(), track);
				}
			}
		}
	}

	private static void writeDrumsNotes(final List<List<Note>> notes, final Track track)
			throws InvalidMidiDataException {
		for (int diffId = 0; diffId < 4; diffId++) {
			final List<Note> diff = notes.get(diffId);
			for (final Note n : diff) {
				for (int i = 1; i < 5; i++) {
					if (getBit(n.notes, i)) {
						addNote(getNoteId(InstrumentType.DRUMS, diffId, i), n.pos, n.pos + n.getLength(), track);
					}
				}
				if (getBit(n.notes, 0)) {
					addNote(getNoteId(InstrumentType.DRUMS, diffId, n.expertPlus ? 5 : 0), n.pos, n.pos + n.getLength(),
							track);
				}
				writeDrumNoteColorCymbalTom(track, n, diffId, 2, 6, n.yellowTom, n.yellowCymbal,
						SysexMessageType.YELLOW_BOTH);
				writeDrumNoteColorCymbalTom(track, n, diffId, 3, 7, n.blueTom, n.blueCymbal,
						SysexMessageType.BLUE_BOTH);
				writeDrumNoteColorCymbalTom(track, n, diffId, 4, 8, n.greenTom, n.greenCymbal,
						SysexMessageType.GREEN_BOTH);
			}
		}
	}

	private static void writeKeysNotes(final List<List<Note>> notes, final Track track)
			throws InvalidMidiDataException {
		for (int diffId = 0; diffId < 4; diffId++) {
			final List<Note> diff = notes.get(diffId);
			for (final Note n : diff) {
				for (int i = 0; i < 5; i++) {
					if ((n.notes & (1 << i)) > 0) {
						addNote(getNoteId(InstrumentType.KEYS, diffId, i), n.pos, n.pos + n.getLength(), track);
					}
				}
			}
		}
	}

	private static void writeEvents(final List<Event> events, final Track track, final int noteId)
			throws InvalidMidiDataException {
		for (final Event e : events) {
			addNote(noteId, e.pos, e.pos + e.getLength(), track);
		}
	}

	private static void writeSolo(final List<Event> solo, final Track track) throws InvalidMidiDataException {
		writeEvents(solo, track, EVENT_SOLO);
	}

	private static void writeSP(final List<Event> sp, final Track track) throws InvalidMidiDataException {
		writeEvents(sp, track, EVENT_SP);
	}

	private static void writeDrumRoll(final List<Event> drumRoll, final Track track) throws InvalidMidiDataException {
		writeEvents(drumRoll, track, EVENT_DRUM_ROLL);
	}

	private static void writeSpecialDrumRoll(final List<Event> specialDrumRoll, final Track track)
			throws InvalidMidiDataException {
		writeEvents(specialDrumRoll, track, EVENT_SPECIAL_DRUM_ROLL);
	}

	private static void writeTap(final List<Event> tap, final Track track) throws InvalidMidiDataException {
		for (final Event e : tap) {
			addSysexNote(SysexMessageType.TAP, -1, e.pos, e.pos + e.getLength(), track, (byte) 0);
		}
	}
}
