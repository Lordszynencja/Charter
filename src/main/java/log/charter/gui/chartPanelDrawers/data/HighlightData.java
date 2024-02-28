package log.charter.gui.chartPanelDrawers.data;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.types.PositionType.BEAT;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.HandShape;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IConstantPositionWithLength;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.IPositionWithLength;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;

public class HighlightData {
	public static class TemporaryHighlighPosition implements IPositionWithLength {
		private int position;
		private int length;
		private final ChordOrNote originalSound;
		private final int string;

		public TemporaryHighlighPosition(final IPosition position) {
			this(position.position(), 0, 0);
		}

		public TemporaryHighlighPosition(final IPositionWithLength positionWithLength) {
			this(positionWithLength.position(), positionWithLength.length(), 0);
		}

		public TemporaryHighlighPosition(final ChordOrNote sound) {
			position = sound.position();
			length = sound.length();
			originalSound = sound;
			string = 0;
		}

		public TemporaryHighlighPosition(final int position) {
			this(position, 0, 0);
		}

		public TemporaryHighlighPosition(final int position, final int length) {
			this(position, length, 0);
		}

		public TemporaryHighlighPosition(final int position, final int length, final int string) {
			this.position = position;
			this.length = length;
			originalSound = null;
			this.string = string;
		}

		@Override
		public int position() {
			return position;
		}

		@Override
		public int length() {
			return length;
		}

		@Override
		public void position(final int newPosition) {
			position = newPosition;
		}

		@Override
		public void length(final int newLength) {
			length = newLength;
		}

		public HighlightPosition asConstant() {
			return new HighlightPosition(position, length, originalSound, string);
		}

	}

	public static class HighlightPosition implements IConstantPositionWithLength {
		public final int position;
		public final int length;
		public final ChordOrNote originalSound;
		public final int string;

		public HighlightPosition(final int position, final int length) {
			this(position, length, null, 0);
		}

		public HighlightPosition(final int position) {
			this(position, 0, null, 0);
		}

		public HighlightPosition(final int position, final int length, final ChordOrNote originalSound,
				final int string) {
			this.position = position;
			this.length = length;
			this.originalSound = originalSound;
			this.string = string;
		}

		@Override
		public int position() {
			return position;
		}

		@Override
		public int length() {
			return length;
		}
	}

	private static void moveSelectedPositions(final ChartData data, final Collection<? extends IPosition> positions,
			final MouseButtonPressData press, final int x) {
		final int dragFrom = press.highlight.position();
		final int dragTo = data.songChart.beatsMap.getPositionFromGridClosestTo(xToTime(x, data.time));
		data.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);
	}

	private static HighlightData getDraggedPositions(final ChartData data, final SelectionManager selectionManager,
			final MouseButtonPressData press, final int x) {
		if (press.highlight.type == PositionType.NONE || press.highlight.type == BEAT) {
			return null;
		}

		HashSet2<Selection<IPosition>> selectedPositions = selectionManager.getSelectedAccessor(press.highlight.type)
				.getSelectedSet();

		if (press.highlight.existingPosition//
				&& !selectedPositions.contains(selection -> selection.id == press.highlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(press.highlight.type, press.highlight.id);
			selectedPositions = selectionManager.getSelectedAccessor(press.highlight.type)//
					.getSelectedSet();
		}
		if (selectedPositions.isEmpty()) {
			return null;
		}

		final Function<Selection<IPosition>, TemporaryHighlighPosition> mapper = switch (press.highlight.type) {
			case ANCHOR -> selection -> new TemporaryHighlighPosition(selection.selectable);
			case EVENT_POINT -> selection -> new TemporaryHighlighPosition(selection.selectable);
			case GUITAR_NOTE -> selection -> new TemporaryHighlighPosition((ChordOrNote) selection.selectable);
			case HAND_SHAPE -> selection -> new TemporaryHighlighPosition((HandShape) selection.selectable);
			case TONE_CHANGE -> selection -> new TemporaryHighlighPosition(selection.selectable);
			case VOCAL -> selection -> new TemporaryHighlighPosition((Vocal) selection.selectable);
			default -> selection -> new TemporaryHighlighPosition(selection.selectable.position());
		};
		final HashSet2<TemporaryHighlighPosition> positions = selectedPositions.map(mapper);

		moveSelectedPositions(data, positions, press, x);

		final ArrayList<HighlightPosition> highlightedPositions = new ArrayList<>(
				positions.map(TemporaryHighlighPosition::asConstant));

		return new HighlightData(press.highlight.type, null, highlightedPositions);
	}

	private static HighlightData getDraggedBeats(final ChartData data, final MouseHandler mouseHandler) {
		final int position = xToTime(mouseHandler.getMouseX(), data.time);
		return new HighlightData(PositionType.BEAT, new HighlightPosition(position));
	}

	private static HighlightData getDragHighlight(final ChartData data, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler, final MouseHandler mouseHandler,
			final SelectionManager selectionManager) {
		final MouseButtonPressData leftPressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.LEFT_BUTTON);
		if (leftPressPosition == null || leftPressPosition.highlight == null) {
			return null;
		}

		if (leftPressPosition.highlight.type == BEAT) {
			if (modeManager.getMode() != EditMode.TEMPO_MAP) {
				return null;
			}

			return getDraggedBeats(data, mouseHandler);
		}

		if (abs(leftPressPosition.position.x - mouseHandler.getMouseX()) > 5) {
			return getDraggedPositions(data, selectionManager, leftPressPosition, mouseHandler.getMouseX());
		}

		return null;
	}

	private static HighlightData getNoteAdditionHighlight(final ChartData data, final HighlightManager highlightManager,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final int x, final int y) {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return null;
		}

		final MouseButtonPressData pressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.RIGHT_BUTTON);
		if (pressPosition == null || pressPosition.highlight.type != PositionType.GUITAR_NOTE) {
			return null;
		}

		final int pressXTime = min(pressPosition.highlight.position(), xToTime(pressPosition.position.x, data.time));
		final int pressY = pressPosition.position.y;

		final Position2D startPosition = pressPosition.position;
		final Position2D endPosition = new Position2D(x, y);
		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);

		final ArrayList2<HighlightPosition> dragPositions = highlightManager
				.getPositionsWithStrings(pressXTime, highlight.position(), pressY, y)//
				.map(position -> new HighlightPosition(position.position(), 0, null, position.string));

		return new HighlightData(PositionType.GUITAR_NOTE, dragPositions, startPosition, endPosition);
	}

	public static HighlightData getCurrentHighlight(final ChartData data, final HighlightManager highlightManager,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final MouseHandler mouseHandler, final SelectionManager selectionManager) {
		final HighlightData dragHighlight = getDragHighlight(data, modeManager, mouseButtonPressReleaseHandler,
				mouseHandler, selectionManager);
		if (dragHighlight != null) {
			return dragHighlight;
		}

		final int x = mouseHandler.getMouseX();
		final int y = mouseHandler.getMouseY();
		final HighlightData noteAddDragHighlight = getNoteAdditionHighlight(data, highlightManager, modeManager,
				mouseButtonPressReleaseHandler, x, y);
		if (noteAddDragHighlight != null) {
			return noteAddDragHighlight;
		}

		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		if (highlight.existingPosition) {
			return new HighlightData(highlight.type, highlight.id);
		}

		final PositionType type = highlight.type;
		final int position = highlight.position();
		if (highlight.type == PositionType.GUITAR_NOTE) {
			final int string = yToString(y, data.currentStrings());
			return new HighlightData(type, new HighlightPosition(position, 0, null, string));
		}
		if (highlight.type == PositionType.HAND_SHAPE || highlight.type == PositionType.VOCAL) {
			final int length = data.songChart.beatsMap.getNextPositionFromGridAfter(position) - position;
			return new HighlightData(type, new HighlightPosition(position, length));
		}

		return new HighlightData(type, new HighlightPosition(position));
	}

	public final PositionType highlightType;
	public final Integer highlightedId;
	public final List<HighlightPosition> highlightedNonIdPositions;
	public final Position2D highlightLineStart;
	public final Position2D highlightLineEnd;

	public HighlightData(final PositionType highlightType, final Integer highlightedId) {
		this.highlightType = highlightType;
		this.highlightedId = highlightedId;
		highlightedNonIdPositions = new ArrayList<>();
		highlightLineStart = null;
		highlightLineEnd = null;
	}

	public HighlightData(final PositionType highlightType, final HighlightPosition highlightPosition) {
		this.highlightType = highlightType;
		highlightedId = null;
		highlightedNonIdPositions = asList(highlightPosition);
		highlightLineStart = null;
		highlightLineEnd = null;
	}

	public HighlightData(final PositionType highlightType, final Integer highlightedId,
			final List<HighlightPosition> highlightedNonIdPositions) {
		this.highlightType = highlightType;
		this.highlightedId = highlightedId;
		this.highlightedNonIdPositions = highlightedNonIdPositions;
		highlightLineStart = null;
		highlightLineEnd = null;
	}

	public HighlightData(final PositionType highlightType,
			final ArrayList2<HighlightPosition> highlightedNonIdPositions, final Position2D highlightLineStart,
			final Position2D highlightLineEnd) {
		this.highlightType = highlightType;
		highlightedId = null;
		this.highlightedNonIdPositions = highlightedNonIdPositions;
		this.highlightLineStart = highlightLineStart;
		this.highlightLineEnd = highlightLineEnd;
	}
}
