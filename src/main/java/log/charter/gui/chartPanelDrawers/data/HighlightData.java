package log.charter.gui.chartPanelDrawers.data;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.types.PositionType.BEAT;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.util.CollectionUtils.contains;
import static log.charter.util.CollectionUtils.map;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.Beat;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.IConstantFractionalPosition;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.IConstantPositionWithLength;
import log.charter.data.song.position.IPosition;
import log.charter.data.song.position.IPositionWithLength;
import log.charter.data.song.position.Position;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.services.mouseAndKeyboard.MouseHandler;
import log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId;
import log.charter.util.data.Position2D;

public class HighlightData {
	public static class TemporaryHighlighPosition implements IPositionWithLength {
		private int position;
		private int length;
		private final ChordOrNote originalSound;
		private final int string;
		private final boolean drawOriginalStrings;

		public <P extends IConstantPosition> TemporaryHighlighPosition(final P position) {
			this(position.position(), 0, 0);
		}

		public TemporaryHighlighPosition(final IPositionWithLength positionWithLength) {
			this(positionWithLength.position(), positionWithLength.length(), 0);
		}

		public TemporaryHighlighPosition(final ChordOrNote sound) {
			position = sound.position();
			length = sound.length();
			originalSound = sound;
			string = -1;
			drawOriginalStrings = true;
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
			drawOriginalStrings = false;
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
			return new HighlightPosition(position, length, originalSound, string, drawOriginalStrings);
		}

	}

	public static class HighlightPosition implements IConstantPositionWithLength {
		public final int position;
		public final int length;
		public final Optional<ChordOrNote> originalSound;
		public final int string;
		public final boolean drawOriginalStrings;

		public HighlightPosition(final int position, final int length) {
			this(position, length, null, 0, false);
		}

		public HighlightPosition(final int position) {
			this(position, 0, null, 0, false);
		}

		public HighlightPosition(final int position, final int length, final ChordOrNote originalSound,
				final int string, final boolean drawOriginalStrings) {
			this.position = position;
			this.length = length;
			this.originalSound = Optional.ofNullable(originalSound);
			this.string = string;
			this.drawOriginalStrings = drawOriginalStrings;
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

	public static class IdHighlightPosition {
		public final int id;
		public final OptionalInt string;

		public IdHighlightPosition(final int id, final int string) {
			this.id = id;
			this.string = OptionalInt.of(string);
		}

		public IdHighlightPosition(final int id) {
			this.id = id;
			string = OptionalInt.empty();
		}
	}

	public static class HighlightLine {
		public final Position2D lineStart;
		public final Position2D lineEnd;

		public HighlightLine(final Position2D lineStart, final Position2D lineEnd) {
			this.lineStart = lineStart;
			this.lineEnd = lineEnd;
		}
	}

	private static void moveSelectedPositions(final int time, final ChartData chartData,
			final Collection<? extends IPosition> positions, final MouseButtonPressData press, final int x) {
		final IConstantFractionalPosition dragStart = press.highlight.positionAsFraction(chartData.beats());
		final IConstantFractionalPosition dragEnd = chartData.beats()
				.getPositionFromGridClosestTo(new Position(xToTime(x, time))).positionAsFraction(chartData.beats());

		chartData.beats().movePositions(positions, dragStart.movementTo(dragEnd));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HighlightData getDraggedPositions(final int time, final ChartData data,
			final SelectionManager selectionManager, final MouseButtonPressData press, final int x) {
		if (press.highlight.type == PositionType.NONE || press.highlight.type == BEAT) {
			return null;
		}

		Set<Selection<?>> selectedPositions = (Set) selectionManager.accessor(press.highlight.type).getSelectedSet();

		if (press.highlight.existingPosition//
				&& !contains(selectedPositions, selection -> selection.id == press.highlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(press.highlight.type, press.highlight.id);
			selectedPositions = (Set) selectionManager.accessor(press.highlight.type)//
					.getSelectedSet();
		}
		if (selectedPositions.isEmpty()) {
			return null;
		}

		final Function<Selection<?>, TemporaryHighlighPosition> mapper = switch (press.highlight.type) {
			case ANCHOR -> s -> new TemporaryHighlighPosition(((Anchor) s.selectable).position(data.beats()));
			case BEAT -> s -> new TemporaryHighlighPosition((Beat) s.selectable);
			case EVENT_POINT -> s -> new TemporaryHighlighPosition((EventPoint) s.selectable);
			case GUITAR_NOTE -> s -> new TemporaryHighlighPosition((ChordOrNote) s.selectable);
			case HAND_SHAPE -> s -> new TemporaryHighlighPosition((HandShape) s.selectable);
			case TONE_CHANGE -> s -> new TemporaryHighlighPosition((ToneChange) s.selectable);
			case VOCAL -> s -> new TemporaryHighlighPosition((Vocal) s.selectable);
			default -> s -> new TemporaryHighlighPosition(0);
		};
		final Set<TemporaryHighlighPosition> positions = selectedPositions.stream()//
				.map(mapper)//
				.collect(Collectors.toSet());

		moveSelectedPositions(time, data, positions, press, x);

		final List<HighlightPosition> highlightedPositions = positions.stream()//
				.map(TemporaryHighlighPosition::asConstant)//
				.collect(Collectors.toList());

		return new HighlightData(press.highlight.type, highlightedPositions);
	}

	private static HighlightData getDraggedBeats(final int time, final MouseHandler mouseHandler) {
		final int position = xToTime(mouseHandler.getMouseX(), time);
		return new HighlightData(PositionType.BEAT, new HighlightPosition(position));
	}

	private static HighlightData getDragHighlight(final int time, final ChartData data, final ModeManager modeManager,
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

			return getDraggedBeats(time, mouseHandler);
		}

		if (abs(leftPressPosition.position.x - mouseHandler.getMouseX()) > 5) {
			return getDraggedPositions(time, data, selectionManager, leftPressPosition, mouseHandler.getMouseX());
		}

		return null;
	}

	private static HighlightData getNoteAdditionHighlight(final int time, final HighlightManager highlightManager,
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

		final int pressXTime = min(pressPosition.highlight.position(), xToTime(pressPosition.position.x, time));
		final int pressY = pressPosition.position.y;

		final Position2D startPosition = pressPosition.position;
		final Position2D endPosition = new Position2D(x, y);
		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);

		final List<PositionWithStringOrNoteId> positionsWithStrings = highlightManager
				.getPositionsWithStrings(pressXTime, highlight.position(), pressY, y);
		final List<HighlightPosition> dragPositions = map(positionsWithStrings,
				position -> new HighlightPosition(position.position(), 0, null, position.string, false));

		return new HighlightData(PositionType.GUITAR_NOTE, dragPositions,
				new HighlightLine(startPosition, endPosition));
	}

	public static HighlightData getCurrentHighlight(final int time, final ChartData data,
			final HighlightManager highlightManager, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler, final MouseHandler mouseHandler,
			final SelectionManager selectionManager) {
		if (modeManager.getMode() == EditMode.EMPTY) {
			return new HighlightData();
		}

		final HighlightData dragHighlight = getDragHighlight(time, data, modeManager, mouseButtonPressReleaseHandler,
				mouseHandler, selectionManager);
		if (dragHighlight != null) {
			return dragHighlight;
		}

		final int x = mouseHandler.getMouseX();
		final int y = mouseHandler.getMouseY();
		final HighlightData noteAddDragHighlight = getNoteAdditionHighlight(time, highlightManager, modeManager,
				mouseButtonPressReleaseHandler, x, y);
		if (noteAddDragHighlight != null) {
			return noteAddDragHighlight;
		}

		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		if (highlight.existingPosition) {
			final IdHighlightPosition id = switch (highlight.type) {
				case GUITAR_NOTE -> new IdHighlightPosition(highlight.id, yToString(y, data.currentStrings()));
				default -> new IdHighlightPosition(highlight.id);
			};
			return new HighlightData(highlight.type, id);
		}

		final PositionType type = highlight.type;
		final int position = highlight.position();
		if (highlight.type == PositionType.GUITAR_NOTE) {
			final int string = yToString(y, data.currentStrings());
			return new HighlightData(type, new HighlightPosition(position, 0, null, string, false));
		}
		if (highlight.type == PositionType.HAND_SHAPE || highlight.type == PositionType.VOCAL) {
			final int length = data.songChart.beatsMap.getNextPositionFromGrid(position) - position;
			return new HighlightData(type, new HighlightPosition(position, length));
		}

		return new HighlightData(type, new HighlightPosition(position));
	}

	public final PositionType type;
	public final Optional<IdHighlightPosition> id;
	public final List<HighlightPosition> highlightedNonIdPositions;
	public final Optional<HighlightLine> line;

	public HighlightData() {
		type = PositionType.NONE;
		id = Optional.empty();
		highlightedNonIdPositions = new ArrayList<>();
		line = Optional.empty();
	}

	public HighlightData(final PositionType type, final IdHighlightPosition id) {
		this.type = type;
		this.id = Optional.of(id);
		highlightedNonIdPositions = new ArrayList<>();
		line = Optional.empty();
	}

	public HighlightData(final PositionType type, final HighlightPosition highlightPosition) {
		this.type = type;
		id = Optional.empty();
		highlightedNonIdPositions = asList(highlightPosition);
		line = Optional.empty();
	}

	public HighlightData(final PositionType type, final IdHighlightPosition id,
			final List<HighlightPosition> highlightedNonIdPositions) {
		this.type = type;
		this.id = Optional.of(id);
		this.highlightedNonIdPositions = highlightedNonIdPositions;
		line = Optional.empty();
	}

	public HighlightData(final PositionType type, final List<HighlightPosition> highlightedNonIdPositions) {
		this.type = type;
		id = Optional.empty();
		this.highlightedNonIdPositions = highlightedNonIdPositions;
		line = Optional.empty();
	}

	public HighlightData(final PositionType type, final List<HighlightPosition> highlightedNonIdPositions,
			final HighlightLine line) {
		this.type = type;
		id = Optional.empty();
		this.highlightedNonIdPositions = highlightedNonIdPositions;
		this.line = Optional.of(line);
	}

	public int getId(final PositionType type) {
		if (this.type != type) {
			return -1;
		}

		return id.map(id -> id.id).orElse(-1);
	}

	public boolean hasStringOf(final ChordOrNote sound) {
		return id.map(id -> sound.hasString(id.string.orElse(-1))).orElse(false);
	}
}
