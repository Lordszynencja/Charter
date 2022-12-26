package log.charter.data.managers.modes;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToLane;

import java.util.function.Function;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.ChordOptionsPane;
import log.charter.gui.panes.HandShapePane;
import log.charter.song.HandShape;
import log.charter.song.enums.Position;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class GuitarModeHandler extends ModeHandler {
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler,
			final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.undoSystem = undoSystem;
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

	private void rightClickHandShape(final PositionWithIdAndType handShapePosition) {
		undoSystem.addUndo();

		if (handShapePosition.handShape != null) {
			data.getCurrentArrangementLevel().handShapes.remove((int) handShapePosition.id);
			return;
		}

		final int endPosition = data.songChart.beatsMap.getNextPositionFromGridAfter(handShapePosition.position);

		final HandShape handShape = new HandShape(handShapePosition.position, endPosition - handShapePosition.position);
		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		handShapes.add(handShape);
		handShapes.sort(null);

		new HandShapePane(data, frame, undoSystem, handShape);
	}

	private void rightClickGuitarNote(final MouseButtonPressReleaseData clickData) {
		undoSystem.addUndo();

		if (!clickData.isDrag()) {
			final int string = yToLane(clickData.pressPosition.y, data.currentStrings());
			if (string < 0 || string >= data.currentStrings()) {
				return;
			}

			if (clickData.pressHighlight.chordOrNote != null) {
				final ChordOrNote chordOrNote = clickData.pressHighlight.chordOrNote;
				if (chordOrNote.isChord() || chordOrNote.note.string != string) {
					new ChordOptionsPane(data, frame, undoSystem, new ArrayList2<>(chordOrNote));
					return;
				}

				data.getCurrentArrangementLevel().chordsAndNotes.remove(clickData.pressHighlight.chordOrNote);
				return;
			}

			final Note note = new Note(clickData.pressHighlight.position, string, 0);
			data.getCurrentArrangementLevel().chordsAndNotes.add(new ChordOrNote(note));
			data.getCurrentArrangementLevel().chordsAndNotes.sort(null);
		}

	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.pressHighlight.type == PositionType.HAND_SHAPE) {
			rightClickHandShape(clickData.pressHighlight);
			return;
		}

		if (clickData.pressHighlight.type == PositionType.ANCHOR) {
			return;
		}

		if (clickData.pressHighlight.type == PositionType.GUITAR_NOTE) {
			rightClickGuitarNote(clickData);
			return;
		}
		// TODO add/remove notes based on highlight

	}
}
