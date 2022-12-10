package log.charter.gui;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.EditMode.GUITAR;
import static log.charter.data.EditMode.VOCALS;
import static log.charter.gui.ChartPanel.isInLanes;
import static log.charter.gui.ChartPanel.isInTempos;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import log.charter.data.ChartData;
import log.charter.data.Config;
import log.charter.data.EditMode;
import log.charter.data.IdOrPos;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.modes.GuitarModeHandler;
import log.charter.gui.modes.ModeHandler;
import log.charter.gui.modes.VocalModeHandler;
import log.charter.gui.panes.LyricPane;
import log.charter.io.Logger;
import log.charter.io.rs.xml.song.Chord;
import log.charter.io.rs.xml.vocals.Vocal;
import log.charter.main.LogCharterRSMain;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.sound.MusicData;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.SoundPlayer;
import log.charter.sound.SoundPlayer.Player;
import log.charter.util.CollectionUtils.ArrayList2;

public class ChartEventsHandler implements KeyListener, MouseListener {
	public static final int FrameLength = 10;

	private final RepeatingPlayer tickPlayer = new RepeatingPlayer(MusicData.generateSound(4000, 0.01, 1));
	private final RepeatingPlayer notePlayer = new RepeatingPlayer(MusicData.generateSound(1000, 0.02, 0.8));

	public final ChartData data;
	public final CharterFrame frame;

	private int currentFrame = 0;
	private int framesDone = 0;
	private Player player = null;
	private int playStartT = 0;
	private final int nextNoteId = -1;
	private boolean claps = false;
	private int nextBeatTime = -1;
	private boolean metronome = false;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;
	private boolean left = false;
	private boolean right = false;
	private boolean gPressed = false;
	private boolean clickCancelsRelease = false;
	private boolean releaseCancelled = false;

	public final SongFileHandler songFileHandler;

	private final Map<EditMode, ModeHandler> modeHandlers = new HashMap<>();

	public ChartEventsHandler(final CharterFrame frame) {
		this.frame = frame;
		data = new ChartData();
		data.handler = this;
		songFileHandler = new SongFileHandler(this);

		new Thread(() -> {
			try {
				while (true) {
					currentFrame++;
					Thread.sleep(FrameLength);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		new Thread(() -> {
			try {
				while (true) {
					while (currentFrame > framesDone) {
						frame();
						frame.repaint();
						framesDone++;
					}
					Thread.sleep(1);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		modeHandlers.put(EditMode.GUITAR, new GuitarModeHandler(this));
		modeHandlers.put(EditMode.VOCALS, new VocalModeHandler(this));
	}

	public void cancelAllActions() {
		data.softClearWithoutDeselect();
		stopMusic();
	}

	public boolean checkChanged() {
		if (data.changed) {
			final int result = JOptionPane.showConfirmDialog(frame, "You have unsaved changes. Do you want to save?",
					"Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				songFileHandler.save();
				return true;
			} else if (result == JOptionPane.NO_OPTION) {
				return true;
			}
			return false;
		}
		return true;
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
		stopMusic();
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit",
				JOptionPane.YES_NO_OPTION)) {
			if (!checkChanged()) {
				return;
			}
			frame.dispose();
			System.exit(0);
		}
	}

	private void frame() {
		if ((player != null) && (player.startTime > 0)) {
			setNextTime(
					(playStartT + (((System.nanoTime() - player.startTime) * data.music.slowMultiplier()) / 1000000))
							- Config.delay);
			final double soundTime = data.nextT + Config.delay;

			// TODO clap notes
//			final List<? extends Event> notes = data.currentInstrument.type.isVocalsType() ? data.s.v.lyrics
//					: data.currentNotes;
//
//			while ((nextNoteId != -1) && (notes.get(nextNoteId).pos < soundTime)) {
//				nextNoteId++;
//				if (nextNoteId >= notes.size()) {
//					nextNoteId = -1;
//				}
//				if (claps) {
//					notePlayer.queuePlaying();
//				}
//			}
//
			while ((nextBeatTime >= 0) && (nextBeatTime < soundTime)) {
				nextBeatTime = data.songChart.beatsMap.getFirstBeatAfter((int) soundTime).position;
				if (metronome) {
					tickPlayer.queuePlaying();
				}
			}

			if ((player != null) && player.isStopped()) {
				stopMusic();
			}
		} else {
			final int speed = (FrameLength * (shift ? 10 : 2)) / (ctrl ? 10 : 1);
			setNextTime((data.nextT - (left ? speed : 0)) + (right ? speed : 0));
		}

		String title;
		if (data.isEmpty) {
			title = LogCharterRSMain.TITLE + " : No project";
		} else {
			title = LogCharterRSMain.TITLE + " : " + data.songChart.artistName + " - " + data.songChart.title + " : "//
					+ data.editMode.name//
					+ (data.changed ? "*" : "");
		}

		frame.setTitle(title);
	}

	public boolean isAlt() {
		return alt;
	}

	public boolean isCtrl() {
		return ctrl;
	}

	public boolean isShift() {
		return shift;
	}

	private void handleSpace() {
		if (!data.isEmpty && (player == null) && !left && !right) {
			// TODO note claps
//			if (data.currentInstrument.type.isVocalsType()) {
//				nextNoteId = data.findClosestVocalForTime(data.nextT);
//				if ((nextNoteId > 0) && (nextNoteId < data.s.v.lyrics.size()) //
//						&& (data.s.v.lyrics.get(nextNoteId).pos < data.nextT)) {
//					nextNoteId++;
//				}
//				if (nextNoteId >= data.s.v.lyrics.size()) {
//					nextNoteId = -1;
//				}
//			} else {
//				nextNoteId = data.findClosestNoteForTime(data.nextT);
//				if ((nextNoteId > 0) && (nextNoteId < data.currentNotes.size()) //
//						&& (data.currentNotes.get(nextNoteId).pos < data.nextT)) {
//					nextNoteId++;
//				}
//				if (nextNoteId >= data.currentNotes.size()) {
//					nextNoteId = -1;
//				}
//			}

			nextBeatTime = data.songChart.beatsMap.getFirstBeatAfter((int) (data.nextT - Config.delay)).position;

			if (ctrl) {
				data.music.setSlow(2);
			} else {
				data.music.setSlow(1);
			}

			playMusic();
		} else {
			stopMusic();
		}
	}

	private void handleHome() {
		if (!ctrl) {
			setNextTime(0);
			return;
		}

		if (data.editMode == GUITAR) {
			final Level currentLevel = data.getCurrentArrangementLevel();
			final List<Chord> chords = currentLevel.chords;
			final List<Note> notes = currentLevel.notes;

			if (!chords.isEmpty()) {
				if (!notes.isEmpty()) {
					setNextTime(min(chords.get(0).position, notes.get(0).position));
				} else {
					setNextTime(chords.get(0).position);
				}
			} else if (!notes.isEmpty()) {
				setNextTime(notes.get(0).position);
			} else {
				setNextTime(0);
			}

		} else if (data.editMode == VOCALS) {
			final List<Vocal> vocals = data.songChart.vocals.vocals;
			if (!vocals.isEmpty()) {
				setNextTime(vocals.get(0).time);
			} else {
				setNextTime(0);
			}
		}
	}

	private void handleEnd() {
		if (!ctrl) {
			setNextTime(data.music.msLength());
			return;
		}

		if (data.editMode == GUITAR) {
			final Level currentLevel = data.getCurrentArrangementLevel();
			final ArrayList2<Chord> chords = currentLevel.chords;
			final ArrayList2<Note> notes = currentLevel.notes;
			if (!chords.isEmpty()) {
				if (!notes.isEmpty()) {
					setNextTime(max(chords.getLast().position, notes.getLast().position));
				} else {
					setNextTime(chords.getLast().position);
				}
			} else if (!notes.isEmpty()) {
				setNextTime(notes.getLast().position);
			} else {
				setNextTime(data.music.msLength());
			}
		} else if (data.editMode == VOCALS) {
			final ArrayList2<Vocal> vocals = data.songChart.vocals.vocals;
			if (!vocals.isEmpty()) {
				setNextTime(vocals.getLast().time);
			} else {
				setNextTime(data.music.msLength());
			}
		}
	}

	private void handleUp() {
		if (data.editMode == GUITAR) {
			data.moveSelectedOneStringUp();
			setChanged();
		}
	}

	private void handleDown() {
		if (data.editMode == GUITAR) {
			data.moveSelectedOneStringDown();
			setChanged();
		}
	}

	private void handleLeft() {
		if (alt) {
			setNextTime(data.songChart.beatsMap.getLastBeatBefore((int) data.nextT).position);
		} else {
			left = true;
		}
	}

	private void handleRight() {
		if (alt) {
			setNextTime(data.songChart.beatsMap.getFirstBeatAfter((int) data.nextT).position);
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
		} else if (isInTempos(data.my)) {// TODO
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
			stopMusic();
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

	public void toggleClaps() {
		claps = !claps;
	}

	public void toggleMetronome() {
		metronome = !metronome;
	}

	public void delete() {
		data.deleteSelected();
		setChanged();
	}

	public void undo() {
		data.undo();
		setChanged();
	}

	public void redo() {
		data.redo();
		setChanged();
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
		setChanged();
	}

	public void toggleHammerOn() {
		data.toggleSelectedHammerOn(false, -1);
		setChanged();
	}

	public void toggleCrazy() {
		data.toggleSelectedCrazy();
		setChanged();
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
			final int pos = data.songChart.vocals.vocals.get(noteId).time;
			new LyricPane(frame, IdOrPos.fromId(noteId, pos));
		}
		setChanged();
	}

	public void toggleVocalsWordPart() {
		data.toggleSelectedVocalsWordPart();
		setChanged();
	}

	public void toggleVocalsPhraseEnd() {
		data.toggleSelectedVocalsPhraseEnd();
		setChanged();
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

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (releaseCancelled) {
			clickCancelsRelease = false;
			return;
		}

		if (e.getButton() == MouseEvent.BUTTON1) {// TODO
			final int y = e.getY();
			if (isInTempos(y)) {// TODO select tempo
//				int newTempoMeasures = -1;
//				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(data.mx));
//				while ((newTempoMeasures < 0) || (newTempoMeasures > 1000)) {
//					try {
//						final String value = JOptionPane.showInputDialog("Measures in beat",
//								"" + ((Tempo) tempoData[0]).beats);
//						if (value == null) {
//							return;
//						}
//						newTempoMeasures = Integer.valueOf(value);
//					} catch (final Exception exception) {
//					}
//				}
//
//				if (tempoData != null) {
//					data.changeTempoBeatsInMeasure((Tempo) tempoData[1], (boolean) tempoData[3], newTempoMeasures);
//					setChanged();
//				}
			}
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (data.isEmpty) {
			return;
		}

		cancelAllActions();
		if (clickCancelsRelease) {
			releaseCancelled = true;
			return;
		} else {
			clickCancelsRelease = true;
			releaseCancelled = false;
		}

		data.mx = e.getX();
		data.my = e.getY();

		final int x = e.getX();
		final int y = e.getY();
		if (e.getButton() == MouseEvent.BUTTON1) {// TODO
			if (isInTempos(y)) {
//				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(x));
//				if (tempoData != null) {
//					data.startTempoDrag((Tempo) tempoData[0], (Tempo) tempoData[1], (Tempo) tempoData[2],
//							(boolean) tempoData[3]);
//				}
			} else if (isInLanes(y)) {
				data.mousePressX = data.mx;
				data.mousePressY = data.my;
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			if (isInLanes(y)) {
				data.startNoteAdding(x, y);
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (data.isEmpty) {
			return;
		}

		clickCancelsRelease = false;
		if (releaseCancelled) {
			return;
		}

		data.mx = e.getX();
		data.my = e.getY();

		switch (e.getButton()) {// TODO
		case MouseEvent.BUTTON1:
//			if (data.draggedTempo != null) {
//				data.stopTempoDrag();
//				setChanged();
//			} else if (data.isNoteDrag) {
//				data.endNoteDrag();
//			} else if ((data.my > (ChartPanel.sectionNamesY - 5)) && (data.my < ChartPanel.spY)) {
//				editSection(data.mx);
//			} else if (ChartPanel.isInLanes(data.my)) {
//				selectNotes(data.mx);
//			}
			break;
		case MouseEvent.BUTTON3:
			if (data.isNoteAdd) {
				data.endNoteAdding();
				setChanged();
			}
			break;
		default:
			break;
		}

		cancelAllActions();
	}

	private void playMusic() {
		player = SoundPlayer.play(data.music, data.time);
		playStartT = data.time;
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

	public void setChanged() {
		if (!data.isEmpty) {
			data.changed = true;
		}
	}

	public void setNextTime(final double t) {
		if ((frame != null) && (frame.scrollBar != null)) {
			final int songLength = data.music.msLength();
			final double songPart = songLength == 0 ? 0 : t / songLength;
			frame.scrollBar.setValue((int) (songPart * frame.scrollBar.getMaximum()));
		}
		setNextTimeWithoutScrolling(t);
	}

	public void setNextTimeWithoutScrolling(final double t) {
		data.nextT = t;
		if (data.nextT < 0) {
			data.nextT = 0;
		}
	}

	public void showPopup(final String msg) {
		JOptionPane.showMessageDialog(frame, msg);
	}

	public void stopMusic() {
		if (player != null) {
			final Player p = player;
			player = null;
			p.stop();
		}
	}
}
