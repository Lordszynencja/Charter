package log.charter.data.types;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.handShapesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;

import log.charter.data.ChartData;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.types.positions.AnchorPositionTypeManager;
import log.charter.data.types.positions.BeatPositionTypeManager;
import log.charter.data.types.positions.GuitarNotePositionTypeManager;
import log.charter.data.types.positions.HandShapePositionTypeManager;
import log.charter.data.types.positions.NonePositionTypeManager;
import log.charter.data.types.positions.PositionTypeManager;
import log.charter.data.types.positions.VocalPositionTypeManager;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public enum PositionType {
	ANCHOR(new AnchorPositionTypeManager()), //
	BEAT(new BeatPositionTypeManager()), //
	GUITAR_NOTE(new GuitarNotePositionTypeManager()), //
	HAND_SHAPE(new HandShapePositionTypeManager()), //
	NONE(new NonePositionTypeManager()), //
	VOCAL(new VocalPositionTypeManager());

	public final PositionTypeManager<?> manager;

	private <T extends IPosition> PositionType(final PositionTypeManager<T> manager) {
		this.manager = manager;
	}

	private static PositionType fromYGuitar(final int y) {
		if (y < beatTextY) {
			return NONE;
		}
		if (y < anchorY) {
			return BEAT;
		}
		if (y < lanesTop) {
			return ANCHOR;
		}
		if (y >= lanesTop && y < lanesBottom) {
			return GUITAR_NOTE;
		}
		if (y >= lanesBottom && y < handShapesY) {
			return HAND_SHAPE;
		}

		return NONE;
	}

	private static PositionType fromYTempoMap(final int y) {
		if (y < beatTextY) {
			return NONE;
		}

		if (y < lanesBottom) {
			return BEAT;
		}

		return NONE;
	}

	private static PositionType fromYVocals(final int y) {
		if (y < lanesTop) {
			return NONE;
		}
		if (y >= lanesTop && y < lanesBottom) {
			return VOCAL;
		}

		return NONE;
	}

	public static PositionType fromY(final int y, final EditMode mode) {
		if (mode == EditMode.GUITAR) {
			return fromYGuitar(y);
		}

		if (mode == EditMode.TEMPO_MAP) {
			return fromYTempoMap(y);
		}

		if (mode == EditMode.VOCALS) {
			return fromYVocals(y);
		}

		return NONE;
	}

	@SuppressWarnings("unchecked")
	public <T extends IPosition> ArrayList2<T> getPositions(final ChartData data) {
		return (ArrayList2<T>) manager.getPositions(data);
	}
}