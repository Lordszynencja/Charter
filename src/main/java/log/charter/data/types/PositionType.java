package log.charter.data.types;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.timingY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;

import java.util.function.BiFunction;

import log.charter.data.ChartData;
import log.charter.data.song.position.IPosition;
import log.charter.services.editModes.EditMode;
import log.charter.util.CollectionUtils.ArrayList2;

public enum PositionType {
	ANCHOR(chartData -> chartData.getCurrentArrangementLevel().anchors, PositionWithIdAndType::new), //
	BEAT(chartData -> chartData.songChart.beatsMap.beats, PositionWithIdAndType::new), //
	EVENT_POINT(chartData -> chartData.getCurrentArrangement().eventPoints, PositionWithIdAndType::new), //
	GUITAR_NOTE(chartData -> chartData.getCurrentArrangementLevel().sounds, PositionWithIdAndType::new), //
	HAND_SHAPE(chartData -> chartData.getCurrentArrangementLevel().handShapes, PositionWithIdAndType::new), //
	NONE(chartData -> new ArrayList2<>(), (id, item) -> PositionWithIdAndType.forNone()), //
	TONE_CHANGE(chartData -> chartData.getCurrentArrangement().toneChanges, PositionWithIdAndType::new), //
	VOCAL(chartData -> chartData.songChart.vocals.vocals, PositionWithIdAndType::new);

	private static interface PositionTypeItemsSupplier<T> {
		ArrayList2<T> items(ChartData chartData);
	}

	private final PositionTypeItemsSupplier<IPosition> itemsSupplier;
	private final BiFunction<Integer, IPosition, PositionWithIdAndType> itemMapper;

	@SuppressWarnings("unchecked")
	private <T extends IPosition> PositionType(final PositionTypeItemsSupplier<T> itemsSupplier,
			final BiFunction<Integer, T, PositionWithIdAndType> itemMapper) {
		this.itemsSupplier = (PositionTypeItemsSupplier<IPosition>) itemsSupplier;
		this.itemMapper = (BiFunction<Integer, IPosition, PositionWithIdAndType>) itemMapper;
	}

	private static PositionType fromYGuitar(final int y) {
		if (y < toneChangeY) {
			return EVENT_POINT;
		}
		if (y < anchorY) {
			return TONE_CHANGE;
		}
		if (y < lanesTop) {
			return ANCHOR;
		}
		if (y < lanesBottom) {
			return GUITAR_NOTE;
		}
		if (y < timingY + 2) {
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
		return switch (mode) {
			case GUITAR -> fromYGuitar(y);
			case TEMPO_MAP -> fromYTempoMap(y);
			case VOCALS -> fromYVocals(y);
			case EMPTY -> NONE;
			default -> NONE;
		};
	}

	@SuppressWarnings("unchecked")
	public <T extends IPosition> ArrayList2<T> getPositions(final ChartData data) {
		return (ArrayList2<T>) itemsSupplier.items(data);
	}

	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId((BiFunction<Integer, IPosition, PositionWithIdAndType>) itemMapper);
	}
}