package log.charter.gui.handlers;

import static java.util.Arrays.asList;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.io.Logger;
import log.charter.song.Beat;
import log.charter.song.Position;

public class KeyboardHandler implements KeyListener {
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;
	private boolean left = false;
	private boolean right = false;

	public void init(final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final ModeManager modeManage, final MouseHandler mouseHandler) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		modeManager = modeManage;
		this.mouseHandler = mouseHandler;
	}

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		left = false;
		right = false;
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

	private void handleUp() {
//		if (data.editMode == GUITAR) {
//			data.moveSelectedOneStringUp();
//			data.setChanged();
//		}
	}

	private void handleDown() {
//		if (data.editMode == GUITAR) {
//			data.moveSelectedOneStringDown();
////			data.setChanged();
//		}
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

	private void numberPressed(final int num) {
//		if (DrawerUtils.isInTempos(data.my)) {// TODO
//			if (num != 0) {
//				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(data.mx));
//				if (tempoData != null) {
//					data.changeTempoBeatsInMeasure((Tempo) tempoData[1], (boolean) tempoData[3], num);
//					setChanged();
//				}
//			}
//		} else if (shift && (num >= 0) && (num <= data.getCurrentArrangement().arrangementType.strings)) {
//			data.toggleSelectedNotes(num);
//		}
	}

	private final Map<Integer, Runnable> keyPressBehaviors = new HashMap<>();

	{
		keyPressBehaviors.put(KeyEvent.VK_SPACE, () -> audioHandler.switchMusicPlayStatus());
		keyPressBehaviors.put(KeyEvent.VK_CONTROL, () -> ctrl = true);
		keyPressBehaviors.put(KeyEvent.VK_ALT, () -> alt = true);
		keyPressBehaviors.put(KeyEvent.VK_SHIFT, () -> shift = true);
		keyPressBehaviors.put(KeyEvent.VK_HOME, () -> modeManager.getHandler().handleHome());
		keyPressBehaviors.put(KeyEvent.VK_END, () -> modeManager.getHandler().handleEnd());
		keyPressBehaviors.put(KeyEvent.VK_UP, this::handleUp);
		keyPressBehaviors.put(KeyEvent.VK_DOWN, this::handleDown);
		keyPressBehaviors.put(KeyEvent.VK_LEFT, this::handleLeft);
		keyPressBehaviors.put(KeyEvent.VK_RIGHT, this::handleRight);
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

	private static final List<Integer> keysNotClearingMousePressesOnPress = asList(//
			KeyEvent.VK_CONTROL, //
			KeyEvent.VK_ALT, //
			KeyEvent.VK_SHIFT, //
			KeyEvent.VK_LEFT, //
			KeyEvent.VK_RIGHT);
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

		if (!keysNotClearingMousePressesOnPress.contains(keyCode)) {
			mouseHandler.cancelAllActions();
		}

		if (!keysNotStoppingMusicOnPress.contains(keyCode)) {
			audioHandler.stopMusic();
		}

		keyPressBehaviors.getOrDefault(keyCode, () -> {
		}).run();
	}

	public void delete() {
		data.deleteSelected();
//		data.setChanged();
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
//		data.setChanged();
	}

	public void toggleHammerOn() {
		data.toggleSelectedHammerOn(false, -1);
//		data.setChanged();
	}

	public void togglePullOff() {
		data.toggleSelectedHammerOn(false, -1);
//		data.setChanged();
	}

	public void toggleCrazy() {
		data.toggleSelectedCrazy();
//		data.setChanged();
	}

	public void toggleVocalsWordPart() {
		data.toggleSelectedVocalsWordPart();
//		data.setChanged();
	}

	public void toggleVocalsPhraseEnd() {
		data.toggleSelectedVocalsPhraseEnd();
//		data.setChanged();
	}

	public void snapNotes() {// TODO
//		if (data.currentInstrument.type.isVocalsType()) {
//			data.snapSelectedVocals();
//		} else {
//			data.snapSelectedNotes();
//		}
//		setChanged();
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
		default:
			break;
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	public void selectAll() {
		data.selectAll();
	}

	public void copy() {
		data.copy();
	}
}
