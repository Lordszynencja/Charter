package log.charter.data.managers.modes;

import java.util.function.Function;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.song.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class GuitarModeHandler extends ModeHandler {
	protected ChartData data;
	protected CharterFrame frame;
	protected KeyboardHandler keyboardHandler;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
	}

	private int getTimeValue(final int defaultValue,
			final Function<ArrayList2<? extends Position>, Integer> positionFromListGetter) {
		if (!keyboardHandler.ctrl()) {
			return defaultValue;
		}

		final ArrayList2<ChordOrNote> chordsAndNotes = data.getCurrentArrangementLevel().chordsAndNotes;

		if (chordsAndNotes.isEmpty()) {
			return defaultValue;
		}

		return positionFromListGetter.apply(chordsAndNotes);
	}

	@Override
	public void handleEnd() {
		frame.setNextTime(getTimeValue(data.music.msLength(), list -> list.getLast().position));
	}

	@Override
	public void handleHome() {
		frame.setNextTime(getTimeValue(0, list -> list.get(0).position));
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
