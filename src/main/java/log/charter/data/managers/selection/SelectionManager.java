package log.charter.data.managers.selection;

import static log.charter.song.notes.IConstantPosition.findClosestId;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.Map;
import java.util.Set;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.EventPoint;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.Position;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class SelectionManager {
	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;

	private TypeSelectionManager<Anchor> anchorsManager;
	private TypeSelectionManager<Beat> beatsManager;
	private TypeSelectionManager<ChordOrNote> chordsNotesManager;
	private TypeSelectionManager<EventPoint> eventPointsManager;
	private TypeSelectionManager<HandShape> handShapesManager;
	private TypeSelectionManager<IPosition> noneManager;
	private TypeSelectionManager<ToneChange> toneChangesManager;
	private TypeSelectionManager<Vocal> vocalsManager;

	private final Map<PositionType, TypeSelectionManager<?>> typeSelectionManagers = new HashMap2<>();

	public void init(final ChartData data, final CharterFrame frame, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		this.data = data;
		this.frame = frame;
		this.modeManager = modeManager;

		anchorsManager = new AnchorsSelectionManager(data, mouseButtonPressReleaseHandler);
		beatsManager = new BeatsSelectionManager(data, mouseButtonPressReleaseHandler);
		chordsNotesManager = new ChordsNotesSelectionManager(data, mouseButtonPressReleaseHandler);
		eventPointsManager = new EventPointsSelectionManager(data, mouseButtonPressReleaseHandler);
		handShapesManager = new HandShapesSelectionManager(data, mouseButtonPressReleaseHandler);
		noneManager = new NoneTypeSelectionManager();
		toneChangesManager = new ToneChangeSelectionManager(data, mouseButtonPressReleaseHandler);
		vocalsManager = new VocalsSelectionManager(data, mouseButtonPressReleaseHandler);

		typeSelectionManagers.put(PositionType.ANCHOR, anchorsManager);
		typeSelectionManagers.put(PositionType.BEAT, beatsManager);
		typeSelectionManagers.put(PositionType.EVENT_POINT, eventPointsManager);
		typeSelectionManagers.put(PositionType.GUITAR_NOTE, chordsNotesManager);
		typeSelectionManagers.put(PositionType.HAND_SHAPE, handShapesManager);
		typeSelectionManagers.put(PositionType.NONE, noneManager);
		typeSelectionManagers.put(PositionType.TONE_CHANGE, toneChangesManager);
		typeSelectionManagers.put(PositionType.VOCAL, vocalsManager);
	}

	private void clearSelectionsExcept(final PositionType typeNotToClear) {
		typeSelectionManagers.forEach((type, manager) -> {
			if (type != typeNotToClear) {
				manager.clear();
			}
		});
	}

	private static class PositionWithLink extends Position {
		public static ArrayList2<PositionWithLink> fromPositionsWithIdAndType(
				final ArrayList2<PositionWithIdAndType> positions) {
			final ArrayList2<PositionWithLink> newPositions = new ArrayList2<>(positions.size() * 2);

			for (final PositionWithIdAndType position : positions) {
				newPositions.add(new PositionWithLink(position.position(), position));
				newPositions.add(new PositionWithLink(position.endPosition, position));
			}

			return newPositions;
		}

		public final PositionWithIdAndType link;

		public PositionWithLink(final int position, final PositionWithIdAndType link) {
			super(position);
			this.link = link;
		}
	}

	private PositionWithIdAndType findExistingLong(final int x, final ArrayList2<PositionWithIdAndType> positions) {
		final ArrayList2<PositionWithLink> positionsWithLinks = PositionWithLink.fromPositionsWithIdAndType(positions);
		final int position = xToTime(x, data.time);
		final Integer id = findClosestId(positionsWithLinks, position);
		if (id == null) {
			return null;
		}

		final PositionWithIdAndType closest = positionsWithLinks.get(id).link;
		if (x - timeToX(closest.position(), data.time) < -20 || x - timeToX(closest.endPosition, data.time) > 20) {
			return null;
		}

		return closest;
	}

	private PositionWithIdAndType findClosestExistingPoint(final int x,
			final ArrayList2<PositionWithIdAndType> positions) {
		final int position = xToTime(x, data.time);
		final Integer id = findClosestId(positions, position);
		if (id == null) {
			return null;
		}

		final PositionWithIdAndType closest = positions.get(id);
		if (x - timeToX(closest.position(), data.time) < -20 || x - timeToX(closest.position(), data.time) > 20) {
			return null;
		}

		return closest;
	}

	public PositionWithIdAndType findExistingPosition(final int x, final int y) {
		final PositionType positionType = PositionType.fromY(y, modeManager.getMode());
		final ArrayList2<PositionWithIdAndType> positions = positionType.manager.getPositionsWithIdsAndTypes(data);

		if (positionType == PositionType.HAND_SHAPE || positionType == PositionType.VOCAL) {
			return findExistingLong(x, positions);
		}

		return findClosestExistingPoint(x, positions);
	}

	public void click(final MouseButtonPressReleaseData clickData, final boolean ctrl, final boolean shift) {
		if (data.isEmpty) {
			return;
		}

		if (!clickData.pressHighlight.existingPosition) {
			if (!ctrl) {
				clearSelectionsExcept(PositionType.NONE);
			}

			frame.selectionChanged(true);
			return;
		}

		clearSelectionsExcept(clickData.pressHighlight.type);

		final TypeSelectionManager<?> manager = typeSelectionManagers.get(clickData.pressHighlight.type);
		if (manager == null) {
			frame.selectionChanged(true);
			return;
		}

		manager.addSelection(clickData.pressHighlight, clickData.pressPosition.x, clickData.pressPosition.y, ctrl,
				shift);
		frame.selectionChanged(true);
	}

	public void clear() {
		clearSelectionsExcept(PositionType.NONE);
		frame.selectionChanged(true);
	}

	@SuppressWarnings("unchecked")
	public <T extends IPosition> SelectionAccessor<T> getSelectedAccessor(final PositionType type) {
		final TypeSelectionManager<?> typeSelectionManager = typeSelectionManagers.get(type);
		if (typeSelectionManager == null) {
			return new SelectionAccessor<>(PositionType.NONE, () -> new ArrayList2<>());
		}

		return (SelectionAccessor<T>) typeSelectionManager.getAccessor();
	}

	@SuppressWarnings("unchecked")
	public <T extends IPosition> SelectionAccessor<T> getCurrentlySelectedAccessor() {
		for (final PositionType positionType : PositionType.values()) {
			final TypeSelectionManager<?> typeSelectionManager = typeSelectionManagers.get(positionType);
			final SelectionAccessor<T> accessor = (SelectionAccessor<T>) typeSelectionManager.getAccessor();
			if (accessor.isSelected()) {
				return accessor;
			}
		}

		return (SelectionAccessor<T>) noneManager.getAccessor();
	}

	public void selectAllNotes() {
		if (data.isEmpty) {
			return;
		}

		if (modeManager.getMode() == EditMode.GUITAR) {
			chordsNotesManager.addAll();
		} else if (modeManager.getMode() == EditMode.VOCALS) {
			vocalsManager.addAll();
		}

		frame.selectionChanged(true);
	}

	public void addSelection(final PositionType type, final int id) {
		typeSelectionManagers.get(type).add(id);
		frame.selectionChanged(true);
	}

	public void addSoundSelection(final int id) {
		addSelection(PositionType.GUITAR_NOTE, id);
	}

	public void addSoundSelection(final ArrayList2<Integer> ids) {
		chordsNotesManager.add(ids);
		frame.selectionChanged(true);
	}

	public void addSelectionForPositions(final PositionType type, final Set<Integer> positions) {
		typeSelectionManagers.get(type).addPositions(positions);
		frame.selectionChanged(type == PositionType.GUITAR_NOTE);
	}

}
