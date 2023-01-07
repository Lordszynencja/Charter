package log.charter.data;

import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JScrollBar;

import log.charter.data.config.Config;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.song.ArrangementChart;
import log.charter.song.Level;
import log.charter.song.SongChart;
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

	public void changeDifficulty(final int newDiff) {
		currentLevel = newDiff;
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
		undoSystem.addUndo();
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

	public void setNewSong(final String dir, final SongChart song, final MusicData musicData,
			final String projectFileName) {
		setSong(dir, song, musicData, projectFileName, EditMode.TEMPO_MAP, 0, 0, 0);
	}

	public void setSong(final String dir, final SongChart song, final MusicData musicData, final String projectFileName,
			final EditMode editMode, final int arrangement, final int level, final int time) {
		currentArrangement = arrangement;
		this.time = time;
		nextTime = time;
		isEmpty = false;

		songChart = song;
		music = musicData;

		audioHandler.stopMusic();
		selectionManager.clear();
		changeDifficulty(level);
		modeManager.editMode = editMode;

		charterMenuBar.refreshMenus();
		scrollBar.setMaximum(musicData.msLength());
		scrollBar.setValue(time);

		path = dir;
		this.projectFileName = projectFileName;
		Config.lastPath = path;
		Config.markChanged();

		selectionManager.clear();
		undoSystem.clear();
	}

	public void undo() {
		undoSystem.undo();
	}

	public int currentStrings() {
		if (modeManager.editMode != EditMode.GUITAR) {
			return -1;
		}
		return getCurrentArrangement().tuning.strings;
	}

	public ArrangementChart getCurrentArrangement() {
		if (modeManager.editMode != EditMode.GUITAR) {
			return null;
		}
		return songChart.arrangements.get(currentArrangement);
	}

	public Level getCurrentArrangementLevel() {
		if (modeManager.editMode != EditMode.GUITAR) {
			return null;
		}
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
