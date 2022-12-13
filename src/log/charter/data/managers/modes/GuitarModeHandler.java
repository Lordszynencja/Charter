package log.charter.data.managers.modes;

import java.util.function.Function;

import log.charter.data.ChartData;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.song.Chord;
import log.charter.song.Note;
import log.charter.song.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class GuitarModeHandler extends ModeHandler {
	private static interface BetterValueGetter {
		int getBetterValue(int a, int b);
	}

	protected ChartData data;
	protected CharterFrame frame;
	protected KeyboardHandler keyboardHandler;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
	}

	private int getTimeValue(final int defaultValue,
			final Function<ArrayList2<? extends Position>, Integer> positionFromListGetter,
			final BetterValueGetter betterValueGetter) {
		if (!keyboardHandler.ctrl()) {
			return defaultValue;
		}

		final ArrayList2<Chord> chords = data.getCurrentArrangementLevel().chords;
		final ArrayList2<Note> notes = data.getCurrentArrangementLevel().notes;

		if (chords.isEmpty() && notes.isEmpty()) {
			return defaultValue;
		}
		if (chords.isEmpty()) {
			return positionFromListGetter.apply(notes);
		}
		if (notes.isEmpty()) {
			return positionFromListGetter.apply(chords);
		}
		return betterValueGetter.getBetterValue(positionFromListGetter.apply(chords),
				positionFromListGetter.apply(notes));
	}

	@Override
	public void handleEnd() {
		frame.setNextTime(getTimeValue(data.music.msLength(), list -> list.getLast().position, Math::max));
	}

	@Override
	public void handleHome() {
		frame.setNextTime(getTimeValue(0, list -> list.get(0).position, Math::min));
	}

	@Override
	public void snapNotes() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		// TODO add/remove notes based on highlight

	}
}
