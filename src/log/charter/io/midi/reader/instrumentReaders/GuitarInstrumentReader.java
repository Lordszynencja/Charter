package log.charter.io.midi.reader.instrumentReaders;

import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.io.midi.MidTrack;
import log.charter.io.midi.MidTrack.MidEvent;
import log.charter.io.midi.reader.InstrumentReader;
import log.charter.song.Event;
import log.charter.song.Instrument;
import log.charter.song.Instrument.InstrumentType;
import log.charter.song.Note;

public class GuitarInstrumentReader extends InstrumentReader {
	private static enum EventType {
		NOTE, TAP_SECTION, SP_SECTION, SOLO_SECTION;

		public static EventType from(final MidEvent e) {
			if ((e.msg.length == 9) && ((e.msg[0] & 0xFF) == 0xF0) && ((e.msg[5] & 0xFF) == 0xFF)) {
				return TAP_SECTION;
			}
			if ((e.msg[1] & 0xFF) == 116) {
				return SP_SECTION;
			}

			if (e.msg[1] == 103) {
				return SOLO_SECTION;
			}

			return NOTE;
		}
	}

	private Event sp = null;
	private Event tap = null;
	private Event solo = null;
	private final List<List<List<Note>>> notes;

	public GuitarInstrumentReader(final InstrumentType type) {
		super(type);
		notes = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			notes.add(new ArrayList<>(8));
			for (int j = 0; j < 8; j++) {
				notes.get(i).add(new ArrayList<>());
			}
		}
	}

	private void joinNoteLanes() {
		debug("joining note lanes");

		for (int i = 0; i < 4; i++) {
			final Map<Long, Note> notesMap = new HashMap<>();
			final List<List<Note>> diffNotes = notes.get(i);

			diffNotes.get(0).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n == null) {
					notesMap.put((long) note.pos, note);
				}
			});
			for (int j = 1; j < 6; j++) {
				for (final Note note : diffNotes.get(j)) {
					final Note n = notesMap.get((long) note.pos);
					if (n == null) {
						note.notes = (1 << (j - 1));
						notesMap.put((long) note.pos, note);
					} else {
						if (n.notes != 0) {
							n.notes |= 1 << (j - 1);
						}
						if (n.getLength() < note.getLength()) {
							n.setLength(note.getLength());
						}
					}
				}
			}
			notes.get(i).get(6).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.hopo = true;
					n.forced = true;
				}
			});
			notes.get(i).get(7).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.hopo = false;
					n.forced = true;
				}
			});

			instr.notes.get(i).addAll(notesMap.values());
		}
	}

	private void handleNote(final MidEvent e) {
		final int type = e.msg[0] & 0xFF;
		final byte diff;
		final byte color;

		if ((type >= 128) && (type <= 159)) {
			final int note = e.msg[1];
			diff = (byte) ((note - 60) / 12);
			color = (byte) ((note % 12) + 1);
		} else if (type == 240) {
			diff = e.msg[5];
			color = 0;
		} else {
			diff = -1;
			color = -1;
		}
		if ((diff >= 0) && (diff < 4) && (color < 8)) {
			notes.get(diff).get(color).add(new Note(e.t));
		} else if (diff != -1) {
			error("Unknown note: " + e.toString());
		}
	}

	private void handleSolo(final MidEvent e) {
		if (solo == null) {
			solo = new Event(e.t);
		} else {
			solo.setLength(e.t - solo.pos);
			instr.solo.add(solo);
			solo = null;
		}
	}

	private void handleSP(final MidEvent e) {
		if (sp == null) {
			sp = new Event(e.t);
		} else {
			sp.setLength(e.t - sp.pos);
			instr.sp.add(sp);
			sp = null;
		}
	}

	private void handleTap(final MidEvent e) {
		if ((e.msg[7] & 0xFF) == 1) {
			if (tap != null) {
				error("Slider already started, new start");
				return;
			}
			tap = new Event(e.t);
		} else if ((e.msg[7] & 0xFF) == 0) {
			if (tap == null) {
				error("No slider started");
				return;
			}

			tap.setLength(e.t - tap.pos);
			instr.tap.add(tap);
			tap = null;
		}
	}

	public Instrument read(final MidTrack t) {
		debug("Reading notes for " + instr.type.name);
		for (final MidEvent e : t.events) {
			switch (EventType.from(e)) {
			case SP_SECTION:
				handleSP(e);
				break;
			case TAP_SECTION:
				handleTap(e);
				break;
			case NOTE:
				handleNote(e);
				break;
			case SOLO_SECTION:
				handleSolo(e);
				break;
			default:
				break;
			}
		}

		joinNoteBeginningWithEnd(notes);
		joinNoteLanes();
		instr.fixNotes();

		debug("Reading notes finished");
		return instr;
	}
}
