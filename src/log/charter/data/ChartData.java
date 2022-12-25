package log.charter.data;

import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JScrollBar;

import log.charter.data.config.Config;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.rs.xml.vocals.ArrangementVocal;
import log.charter.song.ArrangementChart;
import log.charter.song.Level;
import log.charter.song.SongChart;
import log.charter.song.Tempo;
import log.charter.song.notes.Note;
import log.charter.sound.MusicData;

public class ChartData {
	public String path = Config.lastPath;
	public String projectFileName = "project.rscp";
	public boolean isEmpty = true;
	public SongChart songChart = null;
	public MusicData music = new MusicData(new byte[0], 44100);
	public int currentArrangement = 0;
	public int currentLevel = 0;

	public int time = 0;
	public int nextTime = 0;

	private AudioHandler audioHandler;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private JScrollBar scrollBar;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public ChartData() {
	}

	public void init(final AudioHandler audioHandler, final CharterMenuBar charterMenuBar,
			final ModeManager modeManager, final JScrollBar scrollBar, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.audioHandler = audioHandler;
		this.charterMenuBar = charterMenuBar;
		this.modeManager = modeManager;
		this.scrollBar = scrollBar;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public void addVocalNote(final int pos, final String text, final boolean wordPart, final boolean phraseEnd) {
		undoSystem.addUndo();
		selectionManager.clear();

		songChart.vocals.insertNote(pos, text, wordPart, phraseEnd);
	}

	public void changeDifficulty(final int newDiff) {
		currentLevel = newDiff;
	}

//	private void changeEventList(final List<Event> events, final double start, final double end) {
//		int id = 0;
//		while (id < events.size()) {
//			final Event e = events.get(id);
//			if ((e.pos + e.getLength()) < start) {
//				id++;
//				continue;
//			}
//			if (e.pos > end) {
//				break;
//			}
//			if ((e.pos == start) && ((e.pos + e.getLength()) == end)) {
//				events.remove(id);
//				return;
//			}
//			events.remove(id);
//		}
//
//		events.add(new Event(start, end - start));
//		events.sort(null);
//	}

	public void changeLyricLength(final int grids) {// TODO
//		undoSystem.addUndo();
//		for (final int id : selectedNotes) {
//			final Lyric l = s.v.lyrics.get(id);
//			if (useGrid) {
//				if (grids < 0) {
//					l.setLength(s.tempoMap.findNextGridPositionForTime(l.pos + l.getLength(), gridSize) - l.pos);
//				} else {
//					l.setLength(s.tempoMap.findPreviousGridPositionForTime(l.pos + l.getLength(), gridSize) - l.pos);
//				}
//			} else {
//				l.setLength(l.getLength() - (100 * grids));
//			}
//			if ((id + 1) < s.v.lyrics.size()) {
//				fixLyricLength(l, id, s.v.lyrics.get(id + 1));
//			}
//		}
	}

	public void changeNoteLength(final int grids) {// TODO
//		undoSystem.addUndo();
//		for (final int id : selectedNotes) {
//			final Note note = currentNotes.get(id);
//			if (useGrid) {
//				if (grids < 0) {
//					note.setLength(
//							s.tempoMap.findNextGridPositionForTime(note.pos + note.getLength(), gridSize) - note.pos);
//				} else {
//					note.setLength(s.tempoMap.findPreviousGridPositionForTime(note.pos + note.getLength(), gridSize)
//							- note.pos);
//				}
//			} else {
//				note.setLength(note.getLength() - (100 * grids));
//			}
//			fixNextNotesLength(note, id);
//		}
	}

	private void changeSections(final List<Object> events) {// TODO
//		if (selectedNotes.isEmpty()) {
//			return;
//		}
//		undoSystem.addUndo();
//
//		final Note first = currentNotes.get(selectedNotes.get(0));
//		final Note last = currentNotes.get(selectedNotes.get(selectedNotes.size() - 1));
//
//		changeEventList(events, first.pos, last.pos + last.getLength());
	}

	public void changeTempoBeatsInMeasure(final Tempo tmp, final boolean isNew, final int beats) {
		undoSystem.addUndo();
		tmp.beats = beats;
	}

	public void copy() {// TODO
		if (isEmpty) {
			return;
		}
//
//		final List<byte[]> list = new ArrayList<>(selectedNotes.size() + 1);
//
//		if (currentInstrument.type.isVocalsType()) {
//			final double firstLyricPos = s.v.lyrics.get(selectedNotes.get(0)).pos;
//			list.add("lyrics".getBytes());
//
//			for (final int id : selectedNotes) {
//				list.add(s.v.lyrics.get(id).toBytes(firstLyricPos));
//			}
//		} else {
//			final double firstNotePos = currentNotes.get(selectedNotes.get(0)).pos;
//			list.add("notes".getBytes());
//
//			for (final int id : selectedNotes) {
//				list.add(currentNotes.get(id).toBytes(firstNotePos));
//			}
//		}
//
//		ClipboardHandler.setClipboardBytes(joinList(list));
	}

	public void copyFrom(final Level levelChart) {// TODO
//		if ((!isEmpty && (instrumentType != currentInstrument.type)) || (diff != currentDiff)) {
//			final List<Note> from = s.getInstrument(instrumentType).notes.get(diff);
//
//			undoSystem.addUndo();
//
//			currentNotes.clear();
//			for (int i = 0; i < from.size(); i++) {
//				currentNotes.add(new Note(from.get(i)));
//			}
//		}
	}

	public void deleteSelected() {// TODO
		undoSystem.addUndo();

//		for (int i = selectedNotes.size() - 1; i >= 0; i--) {
//			final int id = selectedNotes.get(i);
//			if (currentInstrument.type.isVocalsType()) {
//				s.v.lyrics.remove(id);
//			} else {
//				currentNotes.remove(id);
//			}
//		}
		// deselect();
	}

	private void endNoteDragLyrics() {// TODO
		// undoSystem.addUndo();

//		final List<Lyric> events = new ArrayList<>(selectedNotes.size());
//		final List<Lyric> editedEvents = s.v.lyrics;
//		for (int i = selectedNotes.size() - 1; i >= 0; i--) {
//			final int id = selectedNotes.get(i);
//			final Lyric l = editedEvents.remove(id);
//			events.add(l);
//		}
//
//		final double dt = xToTime(mx) - events.get(events.size() - 1).pos;
//
//		deselect();
//		for (int i = events.size() - 1; i >= 0; i--) {
//			final Lyric l = events.get(i);
//			final IdOrPos noteMovedTo = findClosestVocalIdOrPosForTime(l.pos + dt, handler.isCtrl());
//			if (noteMovedTo.isPos()) {
//				l.pos = noteMovedTo.pos;
//				int firstAfter = findFirstLyricAfterTime(l.pos);
//				if (firstAfter == -1) {
//					firstAfter = editedEvents.size();
//				}
//				editedEvents.add(firstAfter, l);
//				selectedNotes.add(firstAfter);
//				if ((firstAfter - 1) > 0) {
//					fixLyricLength(editedEvents.get(firstAfter - 1), firstAfter - 1, l);
//				}
//				if ((firstAfter + 1) < events.size()) {
//					fixLyricLength(l, firstAfter, editedEvents.get(firstAfter + 1));
//				}
//			}
//		}
	}

	private void endNoteDragNotes() {
		undoSystem.addUndo();

//		final List<Note> events = new ArrayList<>(selectedNotes.size());
//		final List<Note> editedEvents = null;
//		for (int i = selectedNotes.size() - 1; i >= 0; i--) {
//			final int id = selectedNotes.get(i);
//			final Note l = editedEvents.remove(id);
//			events.add(l);
//		}
//
//		final double dt = xToTime(mx, time) - events.get(events.size() - 1).pos;
//
//		deselect();
//		for (int i = events.size() - 1; i >= 0; i--) {
//			final Note n = events.get(i);
//			final IdOrPos noteMovedTo = findClosestIdOrPosForTime(n.pos + dt, handler.isCtrl());
//			if (noteMovedTo.isPos()) {
//				final Note newNote = new Note(n);
//				newNote.pos = noteMovedTo.pos;
//				int firstAfter = findFirstNoteAfterTime(newNote.pos);
//				if (firstAfter == -1) {
//					firstAfter = editedEvents.size();
//				}
//				editedEvents.add(firstAfter, newNote);
//				selectedNotes.add(firstAfter);
//				fixNotesLength(newNote, firstAfter);
//			} else {
//				final int id = noteMovedTo.id;
//				final Note existing = editedEvents.get(id);
//				existing.notes |= n.notes;
//			}
//		}
	}

	private void fixLyricLength(final ArrangementVocal vocal, final int id, final ArrangementVocal next) {// TODO
//		if (next.pos < (Config.minLongNoteDistance + l.pos + l.getLength())) {
//			l.setLength(next.pos - Config.minLongNoteDistance - l.pos);
//		}
	}

	private void fixNextNotesLength(final Note n, final int id) {// TODO
//		if (n.getLength() < Config.minTailLength) {
//			n.setLength(1);
//		}
//		for (int i = id + 1; (i < currentNotes.size()) && (i < (id + 100)); i++) {
//			final Note nextNote = currentNotes.get(i);
//			if (fixNoteLength(n, id, nextNote)) {
//				return;
//			}
//		}
	}

	private boolean fixNoteLength(final Note n, final int nId, final Note next) {// TODO
//		if (n.getLength() < Config.minTailLength) {
//			n.setLength(1);
//			return true;
//		}
//
//		if (next == null) {
//			return false;
//		}
//
//		if ((n.crazy ? notesColorsOverlap(n, next) : true) && notesOverlap(n, next)) {
//			n.setLength(next.pos - Config.minLongNoteDistance - n.pos);
//			return true;
//		}

		return false;
	}

	private void fixNotesLength(final Note n, final int id) {
		fixNextNotesLength(n, id);
		fixPreviousNotesLength(n, id);
	}

	private void fixPreviousNotesLength(final Note n, final int id) {
		for (int i = id - 1; (i >= 0) && (i > (id - 100)); i--) {
			final Note prevNote = null;// TODO currentNotes.get(i);
			fixNoteLength(prevNote, i, n);
		}
	}

	public void moveSelectedOneStringUp() {// TODO
//		final List<Note> notes = new ArrayList<>(selectedNotes.size());
//		for (final int id : selectedNotes) {
//			final Note n = currentNotes.get(id);
//			if ((n.notes & 16) > 0) {
//				return;
//			}
//			notes.add(n);
//		}
//		for (final Note n : notes) {
//			n.notes *= 2;
//			if (n.notes == 0) {
//				n.notes = 1;
//			}
//		}
	}

	public void moveSelectedOneStringDown() {// TODO
//		final List<Note> notes = new ArrayList<>(selectedNotes.size());
//		for (final int id : selectedNotes) {
//			final Note n = currentNotes.get(id);
//			if (n.notes == 0) {
//				return;
//			}
//			notes.add(n);
//		}
//		for (final Note n : notes) {
//			n.notes = (n.notes & 1) > 0 ? 0 : n.notes / 2;
//		}
	}

	public void paste() throws HeadlessException, IOException, UnsupportedFlavorException {// TODO
//		final byte[] notesData = ClipboardHandler.readClipboardBytes();
//
//		try {
//			final List<byte[]> list = splitToList(notesData);
//			final String name = new String(list.get(0));
//			final boolean notesPaste = "notes".equals(name);
//			final boolean lyricsPaste = "lyrics".equals(name);
//			if ((lyricsPaste && !currentInstrument.type.isVocalsType())
//					|| (notesPaste && currentInstrument.type.isVocalsType())) {
//				return;
//			}
//
//			deselect();
//			undoSystem.addUndo();
//			final int n = list.size();
//			final double markerPos = nextT;
//
//			if (notesPaste) {
//				int noteId = findFirstNoteAfterTime(markerPos);
//				if (noteId < 0) {
//					noteId = currentNotes.size();
//				}
//
//				for (int i = 1; i < n; i++) {
//					final Note note = Note.fromBytes(list.get(i), markerPos);
//
//					while ((noteId < currentNotes.size()) && (currentNotes.get(noteId).pos < note.pos)) {
//						noteId++;
//					}
//
//					if (noteId < currentNotes.size()) {// is inside
//						if (currentNotes.get(noteId).pos != note.pos) {
//							currentNotes.add(noteId, note);
//							fixNotesLength(note, noteId);
//						}
//					} else {// is last
//						currentNotes.add(note);
//						fixNotesLength(note, noteId);
//					}
//					addToSelection(noteId);
//					noteId++;
//				}
//			} else if (lyricsPaste) {
//				int lyricId = findFirstLyricAfterTime(markerPos);
//				if (lyricId < 0) {
//					lyricId = s.v.lyrics.size();
//				}
//
//				for (int i = 1; i < n; i++) {
//					final Lyric l = Lyric.fromBytes(list.get(i), markerPos);
//
//					while ((lyricId < s.v.lyrics.size()) && (s.v.lyrics.get(lyricId).pos < l.pos)) {
//						lyricId++;
//					}
//
//					if (lyricId < s.v.lyrics.size()) {// is inside
//						if (s.v.lyrics.get(lyricId).pos != l.pos) {
//							s.v.lyrics.add(lyricId, l);
//							fixLyricLength(l, lyricId, s.v.lyrics.get(lyricId + 1));
//							if (lyricId > 0) {
//								fixLyricLength(s.v.lyrics.get(lyricId), lyricId - 1, l);
//							}
//						}
//					} else {// is last
//						s.v.lyrics.add(l);
//						if (lyricId > 0) {
//							fixLyricLength(s.v.lyrics.get(lyricId - 1), lyricId - 1, l);
//						}
//					}
//					selectedNotes.add(lyricId);
//					lyricId++;
//				}
//			}
//		} catch (final Exception e) {
//			Logger.error("Couldn't paste", e);
//		}
	}

	public void selectAll() {// TODO
//		deselect();
//		final List<? extends Event> events = currentInstrument.type.isVocalsType() ? s.v.lyrics : currentNotes;
//		for (int i = 0; i < events.size(); i++) {
//			selectedNotes.add(i);
//		}
	}

	public void setSong(final String dir, final SongChart song, final MusicData musicData,
			final String projectFileName) {// TODO
		currentArrangement = 0;
		currentLevel = 0;
		time = 0;
		nextTime = 0;
		isEmpty = false;

		songChart = song;
		audioHandler.stopMusic();
		selectionManager.clear();
		changeDifficulty(0);
		modeManager.editMode = EditMode.GUITAR;

		charterMenuBar.refreshMenus();
		scrollBar.setValue(0);
		scrollBar.setMaximum(musicData.msLength());

		path = dir;
		this.projectFileName = projectFileName;
		Config.lastPath = path;
		music = musicData;

		selectionManager.clear();
		undoSystem.clear();
	}

	public void snapSelectedNotes() {// TODO
		undoSystem.addUndo();
		// snapNotes(selectedNotes);
	}

	private void snapNotes2(final List<Integer> notes, final Function<Double, Double> positionCalculator) {// TODO
//		for (int i = 0; i < notes.size(); i++) {
//			final int id = notes.get(i);
//			final Note n = currentNotes.get(id);
//			final double newPos = positionCalculator.apply(n.pos);
//
//			final boolean isBeforePrevious = (id > 0) && (newPos <= currentNotes.get(id - 1).pos);
//			final boolean isAfterNext = ((id + 1) < currentNotes.size()) && (newPos >= currentNotes.get(id + 1).pos);
//			if (isBeforePrevious || isAfterNext) {
//				if (currentInstrument.type.isDrumsType()) {
//					if (isBeforePrevious) {
//						currentNotes.get(id - 1).drumMerge(n);
//					} else {
//						currentNotes.get(id + 1).drumMerge(n);
//					}
//				}
//				notes.remove(i);
//				for (int j = i; j < notes.size(); j++) {
//					notes.add(j, notes.remove(j) - 1);
//				}
//				currentNotes.remove(id);
//				i--;
//			} else {
//				final double newLength = positionCalculator.apply(n.pos + n.getLength()) - newPos;
//				n.pos = newPos;
//				n.setLength(newLength);
//			}
//		}
//
//			for (int i = 0; i < notes.size(); i++) {
//				final int id = notes.get(i);
//				fixNotesLength(currentNotes.get(id), id);
//			}
//
	}

	public void snapNotes(final List<Integer> notes) {// TODO
//		snapNotes2(notes, pos -> s.tempoMap.findClosestGridPositionForTime(pos, useGrid, gridSize));
	}

	public void snapNotes(final List<Integer> notes, final int gridSizeToUse) {// TODO
//		snapNotes2(notes, pos -> s.tempoMap.findClosestGridPositionForTime(pos, true, gridSizeToUse));
	}

	public void snapSelectedVocals() {// TODO
//		undoSystem.addUndo();
//
//		for (int i = 0; i < selectedNotes.size(); i++) {
//			final int id = selectedNotes.get(i);
//			final Lyric l = s.v.lyrics.get(id);
//			final double newPos = s.tempoMap.findClosestGridPositionForTime(l.pos, useGrid, gridSize);
//			if (((id > 0) && (newPos <= s.v.lyrics.get(id - 1).pos))//
//					|| (((id + 1) < s.v.lyrics.size()) && (newPos >= s.v.lyrics.get(id + 1).pos))) {
//				removeFromSelectionByPos(i);
//				s.v.lyrics.remove(id);
//			} else {
//				final double newLength = s.tempoMap.findClosestGridPositionForTime(l.pos + l.getLength(), useGrid,
//						gridSize) - newPos;
//				l.pos = newPos;
//				l.setLength(newLength);
//
//				if (id > 0) {
//					fixLyricLength(s.v.lyrics.get(id - 1), id - 1, l);
//				}
//				if ((id + 1) < s.v.lyrics.size()) {
//					fixLyricLength(l, id, s.v.lyrics.get(id + 1));
//				}
//			}
//		}
	}

	public void startTempoDrag(final Tempo prevTmp, final Tempo tmp, final Tempo nextTmp, final boolean isNew) {// TODO
//		draggedTempoPrev = prevTmp;
//		draggedTempo = tmp;
//		draggedTempoNext = nextTmp;
//		undoSystem.addUndo();
	}

	public void stopTempoDrag() {// TODO
//		draggedTempoPrev = null;
//		draggedTempo = null;
//		draggedTempoNext = null;
	}

	public void addNote(final double pos, final int colorBit) {// TODO
//		int color = 0;
//		if (currentInstrument.type.isGuitarType()) {
//			color = colorBit == 0 ? 0 : (1 << (colorBit - 1));
//		} else {
//			color = 1 << colorBit;
//		}
//
//		final Note n = new Note(pos, color);
//		int insertPos = findFirstNoteAfterTime(pos);
//		if (insertPos == -1) {
//			insertPos = currentNotes.size();
//			currentNotes.add(n);
//		} else {
//			currentNotes.add(insertPos, n);
//		}
//		n.tap = isInSection(n, currentInstrument.tap);
//
//		if (!currentInstrument.type.isDrumsType()) {
//			fixNotesLength(n, insertPos);
//		}
//		addToSelection(insertPos);
	}

	private boolean toggleNote(final int id, final int colorBit) {// TODO
		return false;
//		final Note n = currentNotes.get(id);
//		int color = 0;
//		if (currentInstrument.type.isGuitarType()) {
//			color = colorBit == 0 ? 0 : (1 << (colorBit - 1));
//		} else {
//			color = 1 << colorBit;
//		}
//
//		if (n.notes == color) {
//			currentNotes.remove(id);
//			removeFromSelection(id);
//			return true;
//		}
//		if (color == 0) {
//			n.notes = 0;
//			fixNotesLength(n, id);
//			if (!selectedNotes.contains(id)) {
//				addToSelection(id);
//			}
//			return false;
//		}
//
//		n.notes ^= color;
//		fixNotesLength(n, id);
//		if (!selectedNotes.contains(id)) {
//			addToSelection(id);
//		}
//		return false;
	}

	private void changeSelectedNotes(final Consumer<Note> action) {
//		undoSystem.addUndo();
//
//		selectedNotes.stream()//
//				.map(id -> currentNotes.get(id))//
//				.forEach(action);
	}

	private void changeSelectedNotesWithId(final BiConsumer<Note, Integer> action) {
//		undoSystem.addUndo();
//
//		selectedNotes.stream()//
//				.forEach(id -> action.accept(currentNotes.get(id), id));
	}

	public void toggleSelectedHammerOn(final boolean ctrl, final double maxDistance) {
//		changeSelectedNotesWithId((n, id) -> {
//			if (ctrl) {
//				if (id == 0) {
//					n.hopo = n.tap;
//				} else {
//					final Note prev = currentNotes.get(id - 1);
//					final double distance = n.pos - prev.pos;
//					n.hopo = n.tap || ((distance <= maxDistance) && (n.notes != prev.notes));
//				}
//			} else {
//				n.hopo = n.tap || (n.hopo ^ true);
//			}
//		});
	}

	public void toggleSelectedNotes(final int colorBit) {
//		undoSystem.addUndo();
//
//		for (int i = selectedNotes.size() - 1; i >= 0; i--) {
//			final int id = selectedNotes.get(i);
//			toggleNote(id, colorBit);
//			if (selectedNotes.get(i) != id) {
//				for (int j = i; j < selectedNotes.size(); j++) {
//					selectedNotes.set(j, selectedNotes.get(j) - 1);
//				}
//			}
//		}
	}

	public void toggleSelectedVocalsWordPart() {// TODO
//		undoSystem.addUndo();
//
//		for (final int id : selectedNotes) {
//			final Lyric l = s.v.lyrics.get(id);
//			l.wordPart = !l.wordPart;
//		}
	}

	public void toggleSelectedVocalsPhraseEnd() {// TODO change last note to be phrase end
//		undoSystem.addUndo();
//
//		for (final int id : selectedNotes) {
//			final Lyric l = s.v.lyrics.get(id);
//			l.wordPart = !l.wordPart;
//		}
	}

	public void undo() {
		undoSystem.undo();
	}

	public ArrangementChart getCurrentArrangement() {
		return songChart.arrangements.get(currentArrangement);
	}

	public Level getCurrentArrangementLevel() {
		return songChart.arrangements.get(currentArrangement).levels.get(currentLevel);
	}

	public void setNextTime(final int t) {
		nextTime = t;
		if (nextTime < 0) {
			nextTime = 0;
		}
		if (nextTime > music.msLength()) {
			nextTime = music.msLength();
		}
	}
}
