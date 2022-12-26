package log.charter.gui.handlers;

import static java.lang.Math.max;
import static java.lang.Math.min;
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
import log.charter.song.Beat;
import log.charter.song.enums.Position;

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

	public void moveFromArrowKeys() {
		if (!left && !right) {
			return;
		}

		final int speed = (Framer.frameLength * (shift ? 20 : 2)) / (ctrl ? 2 : 1);
		int nextTime = data.time - (left ? speed : 0) + (right ? speed : 0);
		nextTime = max(0, min(data.music.msLength(), nextTime));
		frame.setNextTime(nextTime);
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
		if (keyCode == KeyEvent.VK_UNDEFINED) {
			return;
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

		if (!keysNotClearingMousePressesOnPress.contains(keyCode)) {
			mouseHandler.cancelAllActions();
		}

		if (!keysNotStoppingMusicOnPress.contains(keyCode)) {
			audioHandler.stopMusic();
		}

		keyPressBehaviors.getOrDefault(keyCode, () -> {
		}).run();
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
}
