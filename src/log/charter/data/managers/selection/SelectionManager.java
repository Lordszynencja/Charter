package log.charter.data.managers.selection;

import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.HandShape;
import log.charter.song.Position;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class SelectionManager {
	private ChartData data;
	private ModeManager modeManager;

	private TypeSelectionManager<Anchor> anchorsManager;
	private TypeSelectionManager<Beat> beatsManager;
	private TypeSelectionManager<ChordOrNote> chordsNotesManager;
	private TypeSelectionManager<HandShape> handShapesManager;
	private TypeSelectionManager<Vocal> vocalsManager;

	private final Map<PositionType, TypeSelectionManager<?>> typeSelectionManagers = new HashMap2<>();

	public void init(final ChartData data, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		this.data = data;
		this.modeManager = modeManager;

		anchorsManager = new AnchorsSelectionManager(data, mouseButtonPressReleaseHandler);
		beatsManager = new BeatsSelectionManager(data, mouseButtonPressReleaseHandler);
		chordsNotesManager = new ChordsNotesSelectionManager(data, mouseButtonPressReleaseHandler);
		handShapesManager = new HandShapesSelectionManager(data, mouseButtonPressReleaseHandler);
		vocalsManager = new VocalsSelectionManager(data, mouseButtonPressReleaseHandler);

		typeSelectionManagers.put(PositionType.ANCHOR, anchorsManager);
		typeSelectionManagers.put(PositionType.BEAT, beatsManager);
		typeSelectionManagers.put(PositionType.GUITAR_NOTE, chordsNotesManager);
		typeSelectionManagers.put(PositionType.HAND_SHAPE, handShapesManager);
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
				newPositions.add(new PositionWithLink(position.position, position));
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
		final Integer id = Position.findClosest(positionsWithLinks, position);
		if (id == null) {
			return null;
		}

		final PositionWithIdAndType closest = positionsWithLinks.get(id).link;
		if (x - timeToX(closest.position, data.time) < -20 || x - timeToX(closest.endPosition, data.time) > 20) {
			return null;
		}

		return closest;
	}

	private PositionWithIdAndType findExistingPoint(final int x, final ArrayList2<PositionWithIdAndType> positions) {
		final int position = xToTime(x, data.time);
		final Integer id = Position.findClosest(positions, position);
		if (id == null) {
			return null;
		}

		final PositionWithIdAndType closest = positions.get(id);
		if (x - timeToX(closest.position, data.time) < -20 || x - timeToX(closest.position, data.time) > 20) {
			return null;
		}

		return closest;
	}

	public PositionWithIdAndType findExistingPosition(final int x, final int y) {
		final PositionType positionType = PositionType.fromY(y, modeManager.editMode);
		final ArrayList2<PositionWithIdAndType> positions = positionType.manager.getPositionsWithIdsAndTypes(data);

		if (positionType == PositionType.HAND_SHAPE || positionType == PositionType.VOCAL) {
			return findExistingLong(x, positions);
		}

		return findExistingPoint(x, positions);
	}

	public void click(final int x, final int y, final boolean ctrl, final boolean shift) {
		if (data.isEmpty) {
			return;
		}

		final PositionWithIdAndType closestPosition = findExistingPosition(x, y);
		if (closestPosition == null) {
			if (!ctrl) {
				clearSelectionsExcept(PositionType.NONE);
			}
			return;
		}

		clearSelectionsExcept(closestPosition.type);

		final TypeSelectionManager<?> manager = typeSelectionManagers.get(closestPosition.type);
		if (manager == null) {
			return;
		}

		manager.addSelection(closestPosition, x, y, ctrl, shift);
	}

	public void clear() {
		clearSelectionsExcept(PositionType.NONE);
	}

	@SuppressWarnings("unchecked")
	public <T extends Position> SelectionAccessor<T> getSelectedAccessor(final PositionType type) {
		final TypeSelectionManager<?> typeSelectionManager = typeSelectionManagers.get(type);
		if (typeSelectionManager == null) {
			return new SelectionAccessor<>(() -> new ArrayList2<>());
		}

		return (SelectionAccessor<T>) typeSelectionManager.getAccessor();
	}

	public void selectAllNotes() {
		if (modeManager.editMode == EditMode.GUITAR) {
			chordsNotesManager.addAll();
		} else if (modeManager.editMode == EditMode.VOCALS) {
			vocalsManager.addAll();
		}
	}
}
