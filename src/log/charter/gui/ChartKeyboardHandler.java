package log.charter.gui;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.EditMode.GUITAR;
import static log.charter.data.EditMode.VOCALS;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.data.IdOrPos;
import log.charter.gui.chartPanelDrawers.common.DrawerUtils;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.modes.GuitarModeHandler;
import log.charter.gui.modes.ModeHandler;
import log.charter.gui.modes.vocal.VocalModeHandler;
import log.charter.gui.panes.LyricPane;
import log.charter.io.Logger;
import log.charter.song.Beat;
import log.charter.song.Chord;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.song.Position;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

public class ChartKeyboardHandler implements KeyListener {

	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private SelectionManager selectionManager;

	private final int playStartT = 0;
	private final int nextNoteId = -1;
	private final int nextBeatTime = -1;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;
	private boolean left = false;
	private boolean right = false;
	private boolean gPressed = false;

	private final Map<EditMode, ModeHandler> modeHandlers = new HashMap<>();

	public ChartKeyboardHandler() {
		modeHandlers.put(EditMode.GUITAR, new GuitarModeHandler());
		modeHandlers.put(EditMode.VOCALS, new VocalModeHandler());
	}

	public void init(final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.selectionManager = selectionManager;

		modeHandlers.values().forEach(modeHandler -> modeHandler.init(data, frame, this));
	}

	public void cancelAllActions() {
		data.softClearWithoutDeselect();
		audioHandler.stopMusic();
	}

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		left = false;
		right = false;
		gPressed = false;
	}

//	public void copyFrom(final InstrumentType instrumentType, final int diff) {
//		data.copyFrom(instrumentType, diff);
//		setChanged();
//	}

//	private void editSection(final int x) {
//		final int id = data.s.tempoMap.findBeatId(data.xToTime(x + 10));
//		final String newSectionName = JOptionPane.showInputDialog(frame, "Section name:", data.s.sections.get(id));
//		if (newSectionName == null) {
//			return;
//		}
//
//		if (newSectionName.trim().equals("")) {
//			data.s.sections.remove(id);
//		} else {
//			data.s.sections.put(id, newSectionName);
//		}
//	}

	public void exit() {
		audioHandler.stopMusic();
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit",
				JOptionPane.YES_NO_OPTION)) {
			if (!frame.checkChanged()) {
				return;
			}

			frame.dispose();
			System.exit(0);
		}
	}

	public void moveFromArrowKeys() {
		if (!left && !right) {
			return;
		}
		final int speed = (Framer.frameLength * (shift ? 10 : 2)) / (ctrl ? 10 : 1);
		frame.setNextTime(data.time - (left ? speed : 0) + (right ? speed : 0));
	}

	public boolean alt() {
		return alt;
	}

	public boolean ctrl() {
		return ctrl;
	}

	public boolean shift() {
		return shift;
	}

	private void handleSpace() {
		audioHandler.switchMusicPlayStatus();
	}

	private void handleHome() {
		if (!ctrl) {
			frame.setNextTime(0);
			return;
		}

		if (data.editMode == GUITAR) {
			final Level currentLevel = data.getCurrentArrangementLevel();
			final List<Chord> chords = currentLevel.chords;
			final List<Note> notes = currentLevel.notes;

			if (!chords.isEmpty()) {
				if (!notes.isEmpty()) {
					frame.setNextTime(min(chords.get(0).position, notes.get(0).position));
				} else {
					frame.setNextTime(chords.get(0).position);
				}
			} else if (!notes.isEmpty()) {
				frame.setNextTime(notes.get(0).position);
			} else {
				frame.setNextTime(0);
			}

		} else if (data.editMode == VOCALS) {
			final List<Vocal> vocals = data.songChart.vocals.vocals;
			if (!vocals.isEmpty()) {
				frame.setNextTime(vocals.get(0).position);
			} else {
				frame.setNextTime(0);
			}
		}
	}

	private void handleEnd() {
		if (!ctrl) {
			frame.setNextTime(data.music.msLength());
			return;
		}

		if (data.editMode == GUITAR) {
			final Level currentLevel = data.getCurrentArrangementLevel();
			final ArrayList2<Chord> chords = currentLevel.chords;
			final ArrayList2<Note> notes = currentLevel.notes;
			if (!chords.isEmpty()) {
				if (!notes.isEmpty()) {
					frame.setNextTime(max(chords.getLast().position, notes.getLast().position));
				} else {
					frame.setNextTime(chords.getLast().position);
				}
			} else if (!notes.isEmpty()) {
				frame.setNextTime(notes.getLast().position);
			} else {
				frame.setNextTime(data.music.msLength());
			}
		} else if (data.editMode == VOCALS) {
			final ArrayList2<Vocal> vocals = data.songChart.vocals.vocals;
			if (!vocals.isEmpty()) {
				frame.setNextTime(vocals.getLast().position);
			} else {
				frame.setNextTime(data.music.msLength());
			}
		}
	}

	private void handleUp() {
		if (data.editMode == GUITAR) {
			data.moveSelectedOneStringUp();
			data.setChanged();
		}
	}

	private void handleDown() {
		if (data.editMode == GUITAR) {
			data.moveSelectedOneStringDown();
			data.setChanged();
		}
	}

	private void handleLeft() {
		if (alt) {
			final Beat beat = Position.findLastBefore(data.songChart.beatsMap.beats, data.time);
			frame.setNextTime(beat.position);
		} else {
			left = true;
		}
	}

	private void handleRight() {
		if (alt) {
			final Beat beat = Position.findFirstAfter(data.songChart.beatsMap.beats, data.time);
			frame.setNextTime(beat.position);
		} else {
			right = true;
		}
	}

	private void handleG() {// TODO open popup to change grid size
		gPressed = true;
	}

	private void numberPressed(final int num) {
		if (gPressed) {// TODO remake to make ctrl + G open a popup to set grid size
//			if (num != 0) {
//				data.gridSize = num;
//				data.useGrid = true;
//			}
		} else if (DrawerUtils.isInTempos(data.my)) {// TODO
//			if (num != 0) {
//				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(data.mx));
//				if (tempoData != null) {
//					data.changeTempoBeatsInMeasure((Tempo) tempoData[1], (boolean) tempoData[3], num);
//					setChanged();
//				}
//			}
		} else if (shift && (num >= 0) && (num <= data.getCurrentArrangement().arrangementType.strings)) {
			data.toggleSelectedNotes(num);
		}
	}

	private final Map<Integer, Runnable> keyPressBehaviors = new HashMap<>();

	{
		keyPressBehaviors.put(KeyEvent.VK_SPACE, this::handleSpace);
		keyPressBehaviors.put(KeyEvent.VK_CONTROL, () -> ctrl = true);
		keyPressBehaviors.put(KeyEvent.VK_ALT, () -> alt = true);
		keyPressBehaviors.put(KeyEvent.VK_SHIFT, () -> shift = true);
		keyPressBehaviors.put(KeyEvent.VK_HOME, this::handleHome);
		keyPressBehaviors.put(KeyEvent.VK_END, this::handleEnd);
		keyPressBehaviors.put(KeyEvent.VK_UP, this::handleUp);
		keyPressBehaviors.put(KeyEvent.VK_DOWN, this::handleDown);
		keyPressBehaviors.put(KeyEvent.VK_LEFT, this::handleLeft);
		keyPressBehaviors.put(KeyEvent.VK_RIGHT, this::handleRight);
		keyPressBehaviors.put(KeyEvent.VK_G, this::handleG);
		keyPressBehaviors.put(KeyEvent.VK_0, () -> numberPressed(0));
		keyPressBehaviors.put(KeyEvent.VK_1, () -> numberPressed(1));
		keyPressBehaviors.put(KeyEvent.VK_2, () -> numberPressed(2));
		keyPressBehaviors.put(KeyEvent.VK_3, () -> numberPressed(3));
		keyPressBehaviors.put(KeyEvent.VK_4, () -> numberPressed(4));
		keyPressBehaviors.put(KeyEvent.VK_5, () -> numberPressed(5));
		keyPressBehaviors.put(KeyEvent.VK_6, () -> numberPressed(6));
		keyPressBehaviors.put(KeyEvent.VK_7, () -> numberPressed(7));
		keyPressBehaviors.put(KeyEvent.VK_8, () -> numberPressed(8));
		keyPressBehaviors.put(KeyEvent.VK_9, () -> numberPressed(9));
	}

	private static final List<Integer> keysNotStoppingMusicOnPress = asList(//
			KeyEvent.VK_CONTROL, //
			KeyEvent.VK_ALT, //
			KeyEvent.VK_SHIFT, //
			KeyEvent.VK_F5, //
			KeyEvent.VK_C, //
			KeyEvent.VK_M, //
			KeyEvent.VK_SPACE);

	@Override
	public void keyPressed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if (data.isEmpty) {
			switch (keyCode) {
			case KeyEvent.VK_CONTROL:
				ctrl = true;
				break;
			case KeyEvent.VK_ALT:
				alt = true;
				break;
			case KeyEvent.VK_SHIFT:
				shift = true;
				break;
			default:
				break;
			}
			return;
		}

		if (!keysNotStoppingMusicOnPress.contains(keyCode)) {
			audioHandler.stopMusic();
		}

		keyPressBehaviors.getOrDefault(keyCode, () -> {
		}).run();

		if (e.getKeyCode() != KeyEvent.VK_G) {
			gPressed = false;
		}
	}

	public void toggleDrawWaveform() {
		data.drawAudio = !data.drawAudio;
	}

	public void toggleDrawDebug() {
		data.drawDebug = !data.drawDebug;
	}

	public void delete() {
		data.deleteSelected();
		data.setChanged();
	}

	public void undo() {
		data.undo();
		data.setChanged();
	}

	public void redo() {
		data.redo();
		data.setChanged();
	}

	public void paste() {
		if (data.isEmpty) {
			return;
		}

		try {
			data.paste();
		} catch (final Exception exception) {
			Logger.error("Couldn't paste notes", exception);
		}
		data.setChanged();
	}

	public void toggleHammerOn() {
		data.toggleSelectedHammerOn(false, -1);
		data.setChanged();
	}

	public void togglePullOff() {
		data.toggleSelectedHammerOn(false, -1);
		data.setChanged();
	}

	public void toggleCrazy() {
		data.toggleSelectedCrazy();
		data.setChanged();
	}

	public void toggleGrid() {
		data.useGrid = !data.useGrid;
	}

	public void changeGridSize() {
		int newGridSize = -1;
		while ((newGridSize < 0) || (newGridSize > 100)) {
			try {
				final String value = JOptionPane.showInputDialog("Grid size", "" + data.gridSize);
				if (value == null) {
					return;
				}
				newGridSize = Integer.valueOf(value);
			} catch (final Exception exception) {
			}
		}
		data.gridSize = newGridSize;
		data.useGrid = true;
	}

	public void editLyric() {
		if (data.selectedNotes.size() == 1) {
			final int noteId = data.selectedNotes.get(0);
			final int pos = data.songChart.vocals.vocals.get(noteId).position;
			new LyricPane(frame, data, IdOrPos.fromId(noteId, pos));
		}
		data.setChanged();
	}

	public void toggleVocalsWordPart() {
		data.toggleSelectedVocalsWordPart();
		data.setChanged();
	}

	public void toggleVocalsPhraseEnd() {
		data.toggleSelectedVocalsPhraseEnd();
		data.setChanged();
	}

	public void snapNotes() {// TODO
//		if (data.currentInstrument.type.isVocalsType()) {
//			data.snapSelectedVocals();
//		} else {
//			data.snapSelectedNotes();
//		}
//		setChanged();
	}

	public void doubleGridSize() {
		data.gridSize *= 2;
	}

	public void halfGridSize() {
		if (data.gridSize % 2 == 0) {
			data.gridSize /= 2;
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left = false;
			break;
		case KeyEvent.VK_RIGHT:
			right = false;
			break;
		case KeyEvent.VK_CONTROL:
			ctrl = false;
			break;
		case KeyEvent.VK_ALT:
			alt = false;
			break;
		case KeyEvent.VK_SHIFT:
			shift = false;
			break;
		case KeyEvent.VK_G:
			gPressed = true;
			break;
		default:
			break;
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	private void selectNotes(final int x) {// TODO
//		final IdOrPos idOrPos = data.currentInstrument.type.isVocalsType() ? data.findClosestVocalIdOrPosForX(x)
//				: data.findClosestIdOrPosForX(x);
//
//		final int[] newSelectedNotes;
//		final Integer last;
//		if (shift) {
//			if (idOrPos.isId() && (data.lastSelectedNote != null)) {
//				last = idOrPos.id;
//				final int start;
//				final int n;
//				if (data.lastSelectedNote < idOrPos.id) {
//					start = data.lastSelectedNote;
//					n = (idOrPos.id - data.lastSelectedNote) + 1;
//				} else {
//					start = idOrPos.id;
//					n = (data.lastSelectedNote - idOrPos.id) + 1;
//				}
//				newSelectedNotes = new int[n];
//				for (int i = 0; i < n; i++) {
//					newSelectedNotes[i] = start + i;
//				}
//			} else {
//				last = null;
//				newSelectedNotes = new int[0];
//			}
//		} else {
//			if (idOrPos.isId()) {
//				last = idOrPos.id;
//				newSelectedNotes = new int[] { idOrPos.id };
//			} else {
//				last = null;
//				newSelectedNotes = new int[0];
//			}
//		}
//		if (!ctrl) {
//			data.deselect();
//		}
//
//		for (final Integer id : newSelectedNotes) {
//			if (!data.selectedNotes.remove(id)) {
//				data.selectedNotes.add(id);
//			}
//		}
//		data.selectedNotes.sort(null);
//		data.lastSelectedNote = last;
	}

	public void selectAll() {
		data.selectAll();
	}

	public void copy() {
		data.copy();
	}
}
