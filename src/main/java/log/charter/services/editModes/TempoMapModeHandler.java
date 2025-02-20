package log.charter.services.editModes;

import static java.lang.System.nanoTime;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.TempoBeatPane;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public class TempoMapModeHandler implements ModeHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private HighlightManager highlightManager;
	private UndoSystem undoSystem;

	private boolean typingDenominator = false;
	private int lastNumber = 0;
	private long typingTimer = 0;

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.isXDrag() || clickData.pressHighlight.beat == null) {
			return;
		}

		new TempoBeatPane(chartData, charterFrame, undoSystem, chartTimeHandler.maxTime(),
				clickData.pressHighlight.beat);
	}

	@Override
	public void changeLength(final int change) {
	}

	private void updateTypingTimer() {
		typingTimer = nanoTime() / 1_000_000 + 2000;
	}

	@Override
	public void handleNumber(final int number) {
		if (nanoTime() / 1_000_000 <= typingTimer) {
			if (lastNumber * 10 + number <= 32) {
				lastNumber = lastNumber * 10 + number;
			} else {
				lastNumber = number;
			}
		} else {
			lastNumber = number;
			typingDenominator = false;
		}

		updateTypingTimer();

		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		if (!highlight.existingPosition || highlight.type != PositionType.BEAT) {
			return;
		}

		final int beatId = highlight.id;
		final ImmutableBeatsMap beats = chartData.beats();
		if (typingDenominator) {
			for (int i = beatId; i < beats.size(); i++) {
				beats.get(i).noteDenominator = lastNumber;
			}
		} else {
			for (int i = beatId; i < beats.size(); i++) {
				beats.get(i).beatsInMeasure = lastNumber;
			}
		}
	}

	@Override
	public void clearNumbers() {
		lastNumber = 0;
		typingTimer = 0;
		typingDenominator = false;
	}

	public void switchTSTypingPart() {
		if (nanoTime() / 1_000_000 <= typingTimer) {
			typingDenominator = true;
		} else {
			typingDenominator = !typingDenominator;
		}
		lastNumber = 0;

		updateTypingTimer();
	}
}
