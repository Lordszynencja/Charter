package log.charter.gui;

import static log.charter.gui.ChartPanel.isInLanes;
import static log.charter.gui.ChartPanel.isInTempos;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import log.charter.data.ChartData;
import log.charter.data.Config;
import log.charter.data.IdOrPos;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.io.Logger;
import log.charter.main.LogCharterMain;
import log.charter.song.Event;
import log.charter.song.Instrument;
import log.charter.song.Instrument.InstrumentType;
import log.charter.song.Tempo;
import log.charter.sound.HighPassFilter.PassType;
import log.charter.sound.MusicData;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.SoundPlayer;
import log.charter.sound.SoundPlayer.Player;

public class ChartEventsHandler implements KeyListener, MouseListener {
	public static final int FL = 10;

	private final RepeatingPlayer tickPlayer = new RepeatingPlayer(MusicData.generateSound(4000, 0.01, 1));
	private final RepeatingPlayer notePlayer = new RepeatingPlayer(MusicData.generateSound(1000, 0.02, 0.8));

	public final ChartData data;
	public final CharterFrame frame;

	private int currentFrame = 0;
	private int framesDone = 0;
	private Player player = null;
	private int playStartT = 0;
	private int nextNoteId = -1;
	private boolean claps = false;
	private double nextTempoTime = -1;
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

	public ChartEventsHandler(final CharterFrame frame) {
		this.frame = frame;
		data = new ChartData();
		data.handler = this;
		songFileHandler = new SongFileHandler(this);

		new Thread(() -> {
			try {
				while (true) {
					currentFrame++;
					Thread.sleep(FL);
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

	public void copyFrom(final InstrumentType instrumentType, final int diff) {
		data.copyFrom(instrumentType, diff);
		setChanged();
	}

	private void editSection(final int x) {
		final int id = data.s.tempoMap.findBeatId(data.xToTime(x + 10));
		final String newSectionName = JOptionPane.showInputDialog(frame, "Section name:", data.s.sections.get(id));
		if (newSectionName == null) {
			return;
		}

		if (newSectionName.trim().equals("")) {
			data.s.sections.remove(id);
		} else {
			data.s.sections.put(id, newSectionName);
		}
	}

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

			final List<? extends Event> notes = data.currentInstrument.type.isVocalsType() ? data.s.v.lyrics
					: data.currentNotes;

			while ((nextNoteId != -1) && (notes.get(nextNoteId).pos < soundTime)) {
				nextNoteId++;
				if (nextNoteId >= notes.size()) {
					nextNoteId = -1;
				}
				if (claps) {
					notePlayer.queuePlaying();
				}
			}

			while ((nextTempoTime >= 0) && (nextTempoTime < soundTime)) {
				nextTempoTime = data.s.tempoMap.findNextBeatTime((int) soundTime);
				if (metronome) {
					tickPlayer.queuePlaying();
				}
			}

			if ((player != null) && player.isStopped()) {
				stopMusic();
			}
		} else {
			final int speed = (FL * (shift ? 10 : 2)) / (ctrl ? 10 : 1);
			setNextTime((data.nextT - (left ? speed : 0)) + (right ? speed : 0));
		}

		final String title = LogCharterMain.TITLE + " : " + data.ini.artist + " - " + data.ini.name + " : "//
				+ (data.currentInstrument.type.isVocalsType() ? "Vocals"
						: data.currentInstrument.type.name + " " + Instrument.diffNames[data.currentDiff])//
				+ (data.changed ? "*" : "");
		frame.setTitle(title);
	}

	public boolean isAlt() {
		return alt;
	}

	public boolean isCtrl() {
		return ctrl;
	}

	public boolean isGPressed() {
		return gPressed;
	}

	public boolean isLeft() {
		return left;
	}

	public boolean isRight() {
		return right;
	}

	public boolean isShift() {
		return shift;
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if ((keyCode != KeyEvent.VK_CONTROL) && (keyCode != KeyEvent.VK_ALT) && (keyCode != KeyEvent.VK_SHIFT)
				&& (keyCode != KeyEvent.VK_F5) && (keyCode != KeyEvent.VK_C) && (keyCode != KeyEvent.VK_M)
				&& (keyCode != KeyEvent.VK_SPACE)) {
			stopMusic();
		}

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

		switch (keyCode) {
		case KeyEvent.VK_SPACE:
			if (!data.isEmpty && (player == null) && !left && !right) {
				if (data.currentInstrument.type.isVocalsType()) {
					nextNoteId = data.findClosestVocalForTime(data.nextT);
					if ((nextNoteId > 0) && (nextNoteId < data.s.v.lyrics.size()) //
							&& (data.s.v.lyrics.get(nextNoteId).pos < data.nextT)) {
						nextNoteId++;
					}
					if (nextNoteId >= data.s.v.lyrics.size()) {
						nextNoteId = -1;
					}
				} else {
					nextNoteId = data.findClosestNoteForTime(data.nextT);
					if ((nextNoteId > 0) && (nextNoteId < data.currentNotes.size()) //
							&& (data.currentNotes.get(nextNoteId).pos < data.nextT)) {
						nextNoteId++;
					}
					if (nextNoteId >= data.currentNotes.size()) {
						nextNoteId = -1;
					}
				}

				nextTempoTime = data.s.tempoMap.findNextBeatTime((int) (data.nextT - Config.delay));

				if (ctrl) {
					data.music.setSlow(2);
				} else {
					data.music.setSlow(1);
				}

				playMusic();
			} else {
				stopMusic();
			}
			break;
		case KeyEvent.VK_HOME:
			if (data.currentInstrument.type.isVocalsType()) {
				setNextTime(ctrl ? (int) (data.s.v.lyrics.isEmpty() ? 0 : data.s.v.lyrics.get(0).pos) : 0);
			} else {
				setNextTime(ctrl ? (int) (data.currentNotes.isEmpty() ? 0 : data.currentNotes.get(0).pos) : 0);
			}
			break;
		case KeyEvent.VK_END:
			if (data.currentInstrument.type.isVocalsType()) {
				setNextTime(ctrl
						? (int) (data.s.v.lyrics.isEmpty() ? 0 : data.s.v.lyrics.get(data.s.v.lyrics.size() - 1).pos)
						: data.music.msLength());
			} else {
				setNextTime(ctrl ? (int) (data.currentNotes.isEmpty() ? 0
						: data.currentNotes.get(data.currentNotes.size() - 1).pos) : data.music.msLength());
			}
			break;
		case KeyEvent.VK_UP:
			if (!data.currentInstrument.type.isVocalsType()) {
				data.moveSelectedDown();
				setChanged();
			}
			break;
		case KeyEvent.VK_DOWN:
			if (!data.currentInstrument.type.isVocalsType()) {
				data.moveSelectedUp();
				setChanged();
			}
			break;
		case KeyEvent.VK_LEFT:
			if (alt) {
				setNextTime(data.s.tempoMap.findBeatTime(data.nextT - 1));
			} else {
				left = true;
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (alt) {
				setNextTime(data.s.tempoMap.findNextBeatTime(data.nextT));
			} else {
				right = true;
			}
			break;
		case KeyEvent.VK_CONTROL:
			ctrl = true;
			break;
		case KeyEvent.VK_ALT:
			alt = true;
			break;
		case KeyEvent.VK_SHIFT:
			shift = true;
			break;
		case KeyEvent.VK_0:
			numberPressed(0);
			break;
		case KeyEvent.VK_1:
			numberPressed(1);
			break;
		case KeyEvent.VK_2:
			numberPressed(2);
			break;
		case KeyEvent.VK_3:
			numberPressed(3);
			break;
		case KeyEvent.VK_4:
			numberPressed(4);
			break;
		case KeyEvent.VK_5:
			numberPressed(5);
			break;
		case KeyEvent.VK_6:
			numberPressed(6);
			break;
		case KeyEvent.VK_7:
			numberPressed(7);
			break;
		case KeyEvent.VK_8:
			numberPressed(8);
			break;
		case KeyEvent.VK_9:
			numberPressed(9);
			break;
		case KeyEvent.VK_G:
			gPressed = true;
			break;
		default:
			break;
		}
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

	public void toggleHOPO() {
		data.toggleSelectedHopo(false, -1);
		setChanged();
	}

	public void toggleHOPOByDistance() {
		double maxHOPODist = -1;
		while ((maxHOPODist < 0) || (maxHOPODist > 10000)) {
			try {
				final String value = JOptionPane.showInputDialog("Max distance between notes to make HOPO",
						"" + Config.lastMaxHOPODist);
				if (value == null) {
					return;
				}
				maxHOPODist = Double.parseDouble(value);
			} catch (final Exception exception) {
			}
		}
		Config.lastMaxHOPODist = maxHOPODist;
		data.toggleSelectedHopo(true, maxHOPODist);
		setChanged();
	}

	public void toggleCrazy() {
		data.toggleSelectedCrazy();
		setChanged();
	}

	public void setSPSection() {
		data.changeSPSections();
		setChanged();
	}

	public void setTapSection() {
		data.changeTapSections();
		setChanged();
	}

	public void setSoloSection() {
		data.changeSoloSections();
		setChanged();
	}

	public void setDrumRollSection() {
		data.changeDrumRollSections();
		setChanged();
	}

	public void setSpecialDrumRollSection() {
		data.changeSpecialDrumRollSections();
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

	public void toggleExpertPlus() {
		if (data.currentDiff == 3) {
			data.toggleSelectedNotesExpertPlus();
			setChanged();
		}
	}

	public void toggleYellowTom() {
		data.toggleSelectedNotesYellowTom();
		setChanged();
	}

	public void toggleBlueTom() {
		data.toggleSelectedNotesBlueTom();
		setChanged();
	}

	public void toggleGreenTom() {
		data.toggleSelectedNotesGreenTom();
		setChanged();
	}

	public void toggleYellowTomCymbal() {
		data.toggleSelectedNotesYellowTomCymbal();
		setChanged();
	}

	public void toggleBlueTomCymbal() {
		data.toggleSelectedNotesBlueTomCymbal();
		setChanged();
	}

	public void toggleGreenTomCymbal() {
		data.toggleSelectedNotesGreenTomCymbal();
		setChanged();
	}

	public void editLyric() {
		if (data.selectedNotes.size() == 1) {
			final int noteId = data.selectedNotes.get(0);
			final double pos = data.s.v.lyrics.get(noteId).pos;
			new LyricPane(frame, IdOrPos.fromId(noteId, pos));
		}
		setChanged();
	}

	public void setLyricLine() {
		data.changeLyricLines();
		setChanged();
	}

	public void toggleLyricConnected() {
		data.toggleSelectedLyricConnected();
		setChanged();
	}

	public void toggleLyricToneless() {
		data.toggleSelectedLyricToneless();
		setChanged();
	}

	public void toggleLyricWordPart() {
		data.toggleSelectedVocalsWordPart();
		setChanged();
	}

	public void snapNotes() {
		if (data.currentInstrument.type.isVocalsType()) {
			data.snapSelectedVocals();
		} else {
			data.snapSelectedNotes();
		}
		setChanged();
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

		if (e.getButton() == MouseEvent.BUTTON1) {
			final int y = e.getY();
			if (isInTempos(y)) {
				int newTempoMeasures = -1;
				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(data.mx));
				while ((newTempoMeasures < 0) || (newTempoMeasures > 1000)) {
					try {
						final String value = JOptionPane.showInputDialog("Measures in beat",
								"" + ((Tempo) tempoData[0]).beats);
						if (value == null) {
							return;
						}
						newTempoMeasures = Integer.valueOf(value);
					} catch (final Exception exception) {
					}
				}

				if (tempoData != null) {
					data.changeTempoBeatsInMeasure((Tempo) tempoData[1], (boolean) tempoData[3], newTempoMeasures);
					setChanged();
				}
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
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (isInTempos(y)) {
				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(x));
				if (tempoData != null) {
					data.startTempoDrag((Tempo) tempoData[0], (Tempo) tempoData[1], (Tempo) tempoData[2],
							(boolean) tempoData[3]);
				}
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

		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			if (data.draggedTempo != null) {
				data.stopTempoDrag();
				setChanged();
			} else if (data.isNoteDrag) {
				data.endNoteDrag();
			} else if ((data.my > (ChartPanel.sectionNamesY - 5)) && (data.my < ChartPanel.spY)) {
				editSection(data.mx);
			} else if (ChartPanel.isInLanes(data.my)) {
				selectNotes(data.mx);
			}
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

	private void numberPressed(final int num) {
		if (gPressed) {
			if (num != 0) {
				data.gridSize = num;
				data.useGrid = true;
			}
		} else if (isInTempos(data.my)) {
			if (num != 0) {
				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(data.mx));
				if (tempoData != null) {
					data.changeTempoBeatsInMeasure((Tempo) tempoData[1], (boolean) tempoData[3], num);
					setChanged();
				}
			}
		} else if (shift && (num >= 0) && (num <= data.currentInstrument.type.lanes)) {
			data.toggleSelectedNotes(num);
		}
	}

	private void playMusic() {
		player = SoundPlayer.play(data.music, data.t);
		playStartT = data.t;
	}

	private void selectNotes(final int x) {
		final IdOrPos idOrPos = data.currentInstrument.type.isVocalsType() ? data.findClosestVocalIdOrPosForX(x)
				: data.findClosestIdOrPosForX(x);

		final int[] newSelectedNotes;
		final Integer last;
		if (shift) {
			if (idOrPos.isId() && (data.lastSelectedNote != null)) {
				last = idOrPos.id;
				final int start;
				final int n;
				if (data.lastSelectedNote < idOrPos.id) {
					start = data.lastSelectedNote;
					n = (idOrPos.id - data.lastSelectedNote) + 1;
				} else {
					start = idOrPos.id;
					n = (data.lastSelectedNote - idOrPos.id) + 1;
				}
				newSelectedNotes = new int[n];
				for (int i = 0; i < n; i++) {
					newSelectedNotes[i] = start + i;
				}
			} else {
				last = null;
				newSelectedNotes = new int[0];
			}
		} else {
			if (idOrPos.isId()) {
				last = idOrPos.id;
				newSelectedNotes = new int[] { idOrPos.id };
			} else {
				last = null;
				newSelectedNotes = new int[0];
			}
		}
		if (!ctrl) {
			data.deselect();
		}

		for (final Integer id : newSelectedNotes) {
			if (!data.selectedNotes.remove(id)) {
				data.selectedNotes.add(id);
			}
		}
		data.selectedNotes.sort(null);
		data.lastSelectedNote = last;
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

	private void addNotesFromMusic(final float frequency, final float resonance, final PassType type,
			final int noteColor, final int gridSnap) {
		data.undoSystem.addUndo();
		for (final Double pos : data.music.pass(frequency, resonance, type).positionsOfHighs()) {
			data.addNote(pos * 1000, noteColor);
		}

		final List<Integer> notes = new ArrayList<>();
		for (int i = 0; i < data.currentNotes.size(); i++) {
			notes.add(i);
		}

		notes.removeIf(noteId -> (data.currentNotes.get(noteId).notes & 1 << noteColor) == 0);
		data.snapNotes(notes, gridSnap);
		data.selectAll();
	}

	public void generateKickDrumFromMusic() {
		new ParamsPane(frame, "set data", 6, 500) {
			private static final long serialVersionUID = 1L;

			private float frequency = 80;
			private float resonance = 0.2f;
			private int gridSnap = 8;

			{
				addConfigValue(0, "Frequency", frequency + "", 50, createFloatValidator(20f, 20_000f, false),
						val -> frequency = Float.valueOf(val), false);
				addConfigValue(1, "Resonance (higher -> less notes will match)", resonance + "", 50,
						createFloatValidator(0.01f, 1f, false), val -> resonance = Float.valueOf(val), false);
				addConfigValue(2, "Grid snap", gridSnap + "", 50, createIntValidator(1, 999, false),
						val -> gridSnap = Integer.valueOf(val), false);

				addButtons(4, "Generate bass", e -> {
					addNotesFromMusic(frequency, resonance, PassType.Lowpass, 0, gridSnap);
					dispose();
				});

				validate();
				setVisible(true);
			}
		};
	}

	public void generateSnareDrumFromMusic() {
		new ParamsPane(frame, "set data", 6, 500) {
			private static final long serialVersionUID = 1L;

			private float frequency = 150;
			private float resonance = 0.2f;
			private int gridSnap = 8;

			{
				addConfigValue(0, "Frequency", frequency + "", 50, createFloatValidator(20f, 20_000f, false),
						val -> frequency = Float.valueOf(val), false);
				addConfigValue(1, "Resonance (higher -> less notes will match)", resonance + "", 50,
						createFloatValidator(0.01f, 1f, false), val -> resonance = Float.valueOf(val), false);
				addConfigValue(2, "Grid snap", gridSnap + "", 50, createIntValidator(1, 999, false),
						val -> gridSnap = Integer.valueOf(val), false);

				addButtons(4, "Generate snare", e -> {
					addNotesFromMusic(frequency, resonance, PassType.Highpass, 1, gridSnap);
					dispose();
				});

				validate();
				setVisible(true);
			}
		};
	}
}
