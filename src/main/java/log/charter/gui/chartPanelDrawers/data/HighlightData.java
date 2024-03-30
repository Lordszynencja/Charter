package log.charter.gui.chartPanelDrawers.data;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.types.PositionType.BEAT;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.util.CollectionUtils.contains;
import static log.charter.util.CollectionUtils.map;
import static log.charter.util.ScalingUtils.xToPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.IConstantPositionWithLength;
import log.charter.data.song.position.time.IPosition;
import log.charter.data.song.position.time.IPositionWithLength;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPosition;
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
	private interface ITemporaryHighlightPosition extends IVirtualPosition {
		HighlightPosition asConstant(ImmutableBeatsMap beats);
	}

	public static class TemporaryHighlightPosition
			implements IPosition, IConstantPositionWithLength, ITemporaryHighlightPosition {
		private int position;
		private final int length;
		private final int string;

		public TemporaryHighlightPosition(final int position, final int length, final int string) {
			this.position = position;
			this.length = length;
			this.string = string;
		}

		public <P extends IConstantPosition> TemporaryHighlightPosition(final P position) {
			this(position.position(), 0, 0);
		}

		public TemporaryHighlightPosition(final IPositionWithLength positionWithLength) {
			this(positionWithLength.position(), positionWithLength.length(), 0);
		}

		public TemporaryHighlightPosition(final int position) {
			this(position, 0, 0);
		}

		public TemporaryHighlightPosition(final int position, final int length) {
			this(position, length, 0);
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
		public HighlightPosition asConstant(final ImmutableBeatsMap beats) {
			final FractionalPosition highlightPosition = FractionalPosition.fromTime(beats, position);
			final FractionalPosition highlightEndPosition = FractionalPosition.fromTime(beats, position + length);

			return new HighlightPosition(highlightPosition, highlightEndPosition, position, length, null, string,
					false);
		}

		@Override
		public IConstantPositionWithLength toPosition(final ImmutableBeatsMap beats) {
			return this;
		}

		@Override
		public boolean isFraction() {
			return false;
		}

		@Override
		public boolean isPosition() {
			return true;
		}
	}

	public static class TemporaryFractionalHighlighPosition
			implements IFractionalPositionWithEnd, ITemporaryHighlightPosition {
		private FractionalPosition position;
		private FractionalPosition endPosition;
		private final ChordOrNote originalSound;
		private final int string;
		private final boolean drawOriginalStrings;

		public TemporaryFractionalHighlighPosition(final FractionalPosition position,
				final FractionalPosition endPosition, final ChordOrNote originalSound, final int string,
				final boolean drawOriginalStrings) {
			this.position = position;
			this.endPosition = endPosition;
			this.originalSound = originalSound;
			this.string = string;
			this.drawOriginalStrings = drawOriginalStrings;
		}

		public <P extends IConstantFractionalPosition> TemporaryFractionalHighlighPosition(final P position) {
			this.position = position.position();
			endPosition = this.position;
			originalSound = null;
			string = 0;
			drawOriginalStrings = false;
		}

		public <P extends IConstantFractionalPositionWithEnd> TemporaryFractionalHighlighPosition(final P position) {
			this.position = position.position();
			endPosition = position.endPosition();
			originalSound = null;
			string = 0;
			drawOriginalStrings = false;
		}

		public TemporaryFractionalHighlighPosition(final ChordOrNote sound) {
			position = sound.position();
			endPosition = sound.endPosition();
			originalSound = sound;
			string = 0;
			drawOriginalStrings = true;
		}

		@Override
		public FractionalPosition position() {
			return position;
		}

		@Override
		public void position(final FractionalPosition newPosition) {
			position = newPosition;
		}

		@Override
		public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
			return this;
		}

		@Override
		public HighlightPosition asConstant(final ImmutableBeatsMap beats) {
			final int position = this.position(beats);
			final int length = this.endPosition(beats) - position;

			return new HighlightPosition(this.position, endPosition, position, length, originalSound, string,
					drawOriginalStrings);
		}

		@Override
		public FractionalPosition endPosition() {
			return endPosition;
		}

		@Override
		public void endPosition(final FractionalPosition newEndPosition) {
			endPosition = newEndPosition;
		}
	}

	public static class HighlightPosition implements IConstantFractionalPositionWithEnd {
		public final FractionalPosition fractionalPosition;
		public final FractionalPosition fractionalEndPosition;
		public final Integer position;
		public final int length;
		public final Optional<ChordOrNote> originalSound;
		public final int string;
		public final boolean drawOriginalStrings;

		public HighlightPosition(final ImmutableBeatsMap beats, final int position, final int length) {
			this(FractionalPosition.fromTime(beats, position), FractionalPosition.fromTime(beats, position + length),
					position, length, null, 0, false);
		}

		public HighlightPosition(final ImmutableBeatsMap beats, final int position) {
			this(FractionalPosition.fromTime(beats, position), FractionalPosition.fromTime(beats, position), position,
					0, null, 0, false);
		}

		public HighlightPosition(final ImmutableBeatsMap beats, final FractionalPosition position) {
			this(position, position, position.position(beats), 0, null, 0, false);
		}

		public HighlightPosition(final ImmutableBeatsMap beats, final int position, final int length,
				final ChordOrNote originalSound, final int string, final boolean drawOriginalStrings) {
			this(FractionalPosition.fromTime(beats, position), FractionalPosition.fromTime(beats, position + length),
					position, length, originalSound, string, drawOriginalStrings);
		}

		public HighlightPosition(final FractionalPosition fractionalPosition,
				final FractionalPosition fractionalEndPosition, final int position, final int length,
				final ChordOrNote originalSound, final int string, final boolean drawOriginalStrings) {
			this.fractionalPosition = fractionalPosition;
			this.fractionalEndPosition = fractionalEndPosition;
			this.position = position;
			this.length = length;
			this.originalSound = Optional.ofNullable(originalSound);
			this.string = string;
			this.drawOriginalStrings = drawOriginalStrings;
		}

		@Override
		public FractionalPosition position() {
			return fractionalPosition;
		}

		@Override
		public FractionalPosition endPosition() {
			return fractionalEndPosition;
		}

		@Override
		public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
			return this;
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
			final Collection<? extends IVirtualPosition> positions, final MouseButtonPressData press, final int x) {
		final IConstantFractionalPosition dragStart = press.highlight.toFraction(chartData.beats());
		final IConstantFractionalPosition dragEnd = chartData.beats()
				.getPositionFromGridClosestTo(new Position(xToPosition(x, time))).toFraction(chartData.beats());

		chartData.beats().movePositions(positions, dragStart.movementTo(dragEnd));
	}

	private static HighlightData getDraggedPositions(final int time, final ChartData chartData,
			final SelectionManager selectionManager, final MouseButtonPressData press, final int x) {
		if (press.highlight.type == PositionType.NONE || press.highlight.type == BEAT) {
			return null;
		}

		List<Selection<IVirtualConstantPosition>> selectedPositions = selectionManager
				.getSelected(press.highlight.type);

		if (press.highlight.existingPosition//
				&& !contains(selectedPositions, selection -> selection.id == press.highlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(press.highlight.type, press.highlight.id);
			selectedPositions = selectionManager.getSelected(press.highlight.type);
		}
		if (selectedPositions.isEmpty()) {
			return null;
		}

		final Function<Selection<IVirtualConstantPosition>, ITemporaryHighlightPosition> mapper = switch (press.highlight.type) {
			case ANCHOR -> s -> new TemporaryFractionalHighlighPosition((Anchor) s.selectable);
			case BEAT -> s -> new TemporaryHighlightPosition((Beat) s.selectable);
			case EVENT_POINT -> s -> new TemporaryFractionalHighlighPosition((EventPoint) s.selectable);
			case GUITAR_NOTE -> s -> new TemporaryFractionalHighlighPosition((ChordOrNote) s.selectable);
			case HAND_SHAPE -> s -> new TemporaryFractionalHighlighPosition((HandShape) s.selectable);
			case TONE_CHANGE -> s -> new TemporaryFractionalHighlighPosition((ToneChange) s.selectable);
			case VOCAL -> s -> new TemporaryFractionalHighlighPosition((Vocal) s.selectable);
			default -> s -> new TemporaryHighlightPosition(0);
		};
		final List<ITemporaryHighlightPosition> positions = map(selectedPositions, mapper);

		moveSelectedPositions(time, chartData, positions, press, x);

		final ImmutableBeatsMap beats = chartData.beats();
		final List<HighlightPosition> highlightedPositions = positions.stream()//
				.map(p -> p.asConstant(beats))//
				.collect(Collectors.toList());

		return new HighlightData(press.highlight.type, highlightedPositions);
	}

	private static HighlightData getDraggedBeats(final ImmutableBeatsMap beats, final int time,
			final MouseHandler mouseHandler) {
		final int position = xToPosition(mouseHandler.getMouseX(), time);
		return new HighlightData(PositionType.BEAT, new HighlightPosition(beats, position));
	}

	private static HighlightData getDragHighlight(final int time, final ChartData chartData,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final MouseHandler mouseHandler, final SelectionManager selectionManager) {
		final MouseButtonPressData leftPressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.LEFT_BUTTON);
		if (leftPressPosition == null || leftPressPosition.highlight == null) {
			return null;
		}

		if (leftPressPosition.highlight.type == BEAT) {
			if (modeManager.getMode() != EditMode.TEMPO_MAP) {
				return null;
			}

			return getDraggedBeats(chartData.beats(), time, mouseHandler);
		}

		if (abs(leftPressPosition.position.x - mouseHandler.getMouseX()) > 5) {
			return getDraggedPositions(time, chartData, selectionManager, leftPressPosition, mouseHandler.getMouseX());
		}

		return null;
	}

	private static HighlightData getNoteAdditionHighlight(final ImmutableBeatsMap beats, final int time,
			final HighlightManager highlightManager, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler, final int x, final int y) {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return null;
		}

		final MouseButtonPressData pressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.RIGHT_BUTTON);
		if (pressPosition == null || pressPosition.highlight.type != PositionType.GUITAR_NOTE) {
			return null;
		}

		final int pressXTime = min(pressPosition.highlight.toPosition(beats).position(),
				xToPosition(pressPosition.position.x, time));
		final int pressY = pressPosition.position.y;

		final Position2D startPosition = pressPosition.position;
		final Position2D endPosition = new Position2D(x, y);
		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);

		final List<PositionWithStringOrNoteId> positionsWithStrings = highlightManager
				.getPositionsWithStrings(pressXTime, highlight.toPosition(beats).position(), pressY, y);
		final List<HighlightPosition> dragPositions = map(positionsWithStrings,
				h -> new HighlightPosition(beats, h.position(beats), 0, null, h.string, false));

		return new HighlightData(PositionType.GUITAR_NOTE, dragPositions,
				new HighlightLine(startPosition, endPosition));
	}

	public static HighlightData getCurrentHighlight(final int time, final ChartData chartData,
			final HighlightManager highlightManager, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler, final MouseHandler mouseHandler,
			final SelectionManager selectionManager) {
		if (modeManager.getMode() == EditMode.EMPTY) {
			return new HighlightData();
		}

		final HighlightData dragHighlight = getDragHighlight(time, chartData, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager);
		if (dragHighlight != null) {
			return dragHighlight;
		}

		final int x = mouseHandler.getMouseX();
		final int y = mouseHandler.getMouseY();
		final HighlightData noteAddDragHighlight = getNoteAdditionHighlight(chartData.beats(), time, highlightManager,
				modeManager, mouseButtonPressReleaseHandler, x, y);
		if (noteAddDragHighlight != null) {
			return noteAddDragHighlight;
		}

		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		if (highlight.existingPosition) {
			final IdHighlightPosition id = switch (highlight.type) {
				case GUITAR_NOTE -> new IdHighlightPosition(highlight.id, yToString(y, chartData.currentStrings()));
				default -> new IdHighlightPosition(highlight.id);
			};
			return new HighlightData(highlight.type, id);
		}

		final PositionType type = highlight.type;
		final int position = highlight.toPosition(chartData.beats()).position();
		if (highlight.type == PositionType.GUITAR_NOTE) {
			final int string = yToString(y, chartData.currentStrings());
			return new HighlightData(type, new HighlightPosition(chartData.beats(), position, 0, null, string, false));
		}
		if (highlight.type == PositionType.HAND_SHAPE || highlight.type == PositionType.VOCAL) {
			final int length = chartData.beats().addGrid(highlight, 1).toPosition(chartData.beats()).position()
					- position;
			return new HighlightData(type, new HighlightPosition(chartData.beats(), position, length));
		}

		return new HighlightData(type, new HighlightPosition(chartData.beats(), position));
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
