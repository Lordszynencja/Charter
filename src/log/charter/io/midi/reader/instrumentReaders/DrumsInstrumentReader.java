package log.charter.io.midi.reader.instrumentReaders;

import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;
import static log.charter.io.midi.NoteIds.EVENT_DRUM_ROLL;
import static log.charter.io.midi.NoteIds.EVENT_SOLO;
import static log.charter.io.midi.NoteIds.EVENT_SP;
import static log.charter.io.midi.NoteIds.EVENT_SPECIAL_DRUM_ROLL;
import static log.charter.io.midi.NoteIds.getDiff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.io.midi.MidTrack;
import log.charter.io.midi.MidTrack.MidEvent;
import log.charter.io.midi.NoteIds;
import log.charter.io.midi.reader.InstrumentReader;
import log.charter.song.Event;
import log.charter.song.Instrument;
import log.charter.song.Instrument.InstrumentType;
import log.charter.song.Note;

public class DrumsInstrumentReader extends InstrumentReader {
	private static enum EventType {
		NOTE, SP_SECTION, SOLO_SECTION, DRUM_ROLL_SECTION, SPECIAL_DRUM_ROLL_SECTION;

		public static EventType from(final MidEvent e) {
			switch (e.msg[1] & 0xFF) {
			case EVENT_SP:
				return SP_SECTION;
			case EVENT_SOLO:
				return SOLO_SECTION;
			case EVENT_DRUM_ROLL:
				return DRUM_ROLL_SECTION;
			case EVENT_SPECIAL_DRUM_ROLL:
				return SPECIAL_DRUM_ROLL_SECTION;
			default:
				break;
			}

			return NOTE;
		}
	}

	private Event sp = null;
	private Event solo = null;
	private Event drumRoll = null;
	private Event specialDrumRoll = null;
	private final List<List<List<Note>>> notes;

	public DrumsInstrumentReader(final InstrumentType type) {
		super(type);
		notes = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			notes.add(new ArrayList<>(9));
			for (int j = 0; j < 12; j++) {
				notes.get(i).add(new ArrayList<>());
			}
		}
	}

	private void joinNoteLanes() {
		debug("joining note lanes");

		for (int i = 0; i < 4; i++) {
			final Map<Long, Note> notesMap = new HashMap<>();
			final List<List<Note>> diffNotes = notes.get(i);

			for (int j = 0; j < 5; j++) {
				for (final Note note : diffNotes.get(j)) {
					final Note n = notesMap.get((long) note.pos);
					if (n == null) {
						note.notes = 1 << j;
						note.crazy = true;
						notesMap.put((long) note.pos, note);
					} else {
						if (n.notes != 0) {
							n.notes |= 1 << j;
						}
						if (n.getLength() < note.getLength()) {
							n.setLength(note.getLength());
						}
					}
				}
			}
			notes.get(i).get(5).forEach(note -> {
				Note n = notesMap.get((long) note.pos);
				if (n == null) {
					note.notes = 1;
					notesMap.put((long) note.pos, note);
					n = note;
				} else {
					n.notes |= 1;
				}
				n.expertPlus = true;
			});
			notes.get(i).get(6).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.yellowTom = true;
				}
			});
			notes.get(i).get(7).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.blueTom = true;
				}
			});
			notes.get(i).get(8).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.greenTom = true;
				}
			});
			notes.get(i).get(9).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.yellowTom = true;
					n.yellowCymbal = true;
				}
			});
			notes.get(i).get(10).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.blueTom = true;
					n.blueCymbal = true;
				}
			});
			notes.get(i).get(11).forEach(note -> {
				final Note n = notesMap.get((long) note.pos);
				if (n != null) {
					n.greenTom = true;
					n.greenCymbal = true;
				}
			});

			instr.notes.get(i).addAll(notesMap.values());
		}
	}

	private static boolean isSpecialEvent(final MidEvent e) {
		return e.msg[0] == -16;
	}

	private void addSpecial(final MidEvent e) {
		final int difficulty = e.msg[5];
		final int type = e.msg[6] - 17;
		if (difficulty >= 0 && difficulty < 4 && type >= 0 && type < 3) {
			notes.get(difficulty).get(9 + type).add(new Note(e.t));
		}
	}

	private void handleNote(final MidEvent e) {
		if (isSpecialEvent(e)) {
			addSpecial(e);
		} else {
			final Integer id = e.msg[1] & 0xFF;
			final int diff = getDiff(InstrumentType.DRUMS, id);
			final int lane = NoteIds.getLane(InstrumentType.DRUMS, id);
			if (lane == 6 || lane == 7 || lane == 8) {
				for (int i = 0; i < 4; i++) {
					notes.get(i).get(lane).add(new Note(e.t));
				}
			} else if (diff != -1 && lane != -1) {
				notes.get(diff).get(lane).add(new Note(e.t));
			} else {
				error("Unknown note: " + e.toString());
			}
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

	private void handleDrumRoll(final MidEvent e) {
		if (drumRoll == null) {
			drumRoll = new Event(e.t);
		} else {
			drumRoll.setLength(e.t - drumRoll.pos);
			instr.drumRoll.add(drumRoll);
			drumRoll = null;
		}
	}

	private void handleSpecialDrumRoll(final MidEvent e) {
		if (specialDrumRoll == null) {
			specialDrumRoll = new Event(e.t);
		} else {
			specialDrumRoll.setLength(e.t - specialDrumRoll.pos);
			instr.specialDrumRoll.add(specialDrumRoll);
			specialDrumRoll = null;
		}
	}

	public Instrument read(final MidTrack t) {
		debug("Reading notes for " + instr.type.name);
		for (final MidEvent e : t.events) {
			switch (EventType.from(e)) {
			case SP_SECTION:
				handleSP(e);
				break;
			case NOTE:
				handleNote(e);
				break;
			case SOLO_SECTION:
				handleSolo(e);
				break;
			case DRUM_ROLL_SECTION:
				handleDrumRoll(e);
				break;
			case SPECIAL_DRUM_ROLL_SECTION:
				handleSpecialDrumRoll(e);
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
