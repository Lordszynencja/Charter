package log.charter.data.types;

import static log.charter.data.song.position.virtual.IVirtualPosition.fractionalPositionManager;
import static log.charter.data.song.position.virtual.IVirtualPosition.positionManager;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.fhpY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.timingY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.CollectionUtils.mapWithId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPosition.PositionDataTypeManager;
import log.charter.services.editModes.EditMode;

public enum PositionType {
	FHP(false, fractionalPositionManager, ChartData::currentFHPs, PositionWithIdAndType::of), //
	BEAT(false, positionManager, ChartData::beats, PositionWithIdAndType::of), //
	EVENT_POINT(false, fractionalPositionManager, ChartData::currentEventPoints, PositionWithIdAndType::of), //
	GUITAR_NOTE(true, fractionalPositionManager, ChartData::currentSounds, PositionWithIdAndType::of), //
	HAND_SHAPE(true, fractionalPositionManager, ChartData::currentHandShapes, PositionWithIdAndType::of), //
	NONE(false, positionManager, chartData -> new ArrayList<>(), (beats, id, item) -> PositionWithIdAndType.none()), //
	TONE_CHANGE(false, fractionalPositionManager, ChartData::currentToneChanges, PositionWithIdAndType::of), //
	SHOWLIGHT(false, fractionalPositionManager, chartData -> chartData.songChart.showlights, PositionWithIdAndType::of), //
	VOCAL(true, fractionalPositionManager, chartData -> chartData.currentVocals().vocals, PositionWithIdAndType::of);

	private static interface PositionTypeItemsSupplier<T> {
		List<T> items(ChartData chartData);
	}

	private static interface PositionWithIdMapper<T> {
		PositionWithIdAndType map(ImmutableBeatsMap beats, Integer id, T value);
	}

	public static class PositionTypeManager<C extends IVirtualConstantPosition, P extends C, T extends P> {
		private final PositionDataTypeManager<C, P> positionDataTypeManager;
		private final PositionTypeItemsSupplier<T> itemsSupplier;
		private final PositionWithIdMapper<T> itemMapper;

		public PositionTypeManager(final PositionDataTypeManager<C, P> positionDataTypeManager,
				final PositionTypeItemsSupplier<T> itemsSupplier, final PositionWithIdMapper<T> itemMapper) {
			this.positionDataTypeManager = positionDataTypeManager;
			this.itemsSupplier = itemsSupplier;
			this.itemMapper = itemMapper;
		}

		public Comparator<C> comparator() {
			return positionDataTypeManager.comparator();
		}

		public List<C> asConstant(final Collection<IVirtualConstantPosition> items) {
			return asConstant(items.stream());
		}

		public List<C> asConstant(final Stream<IVirtualConstantPosition> items) {
			return positionDataTypeManager.constantOf(items);
		}

		public List<T> getItems(final ChartData chartData) {
			return itemsSupplier.items(chartData);
		}

		public List<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData chartData) {
			return mapWithId(itemsSupplier.items(chartData), (id, item) -> itemMapper.map(chartData.beats(), id, item));
		}

		private <A extends C> Optional<Integer> findIdForPosition(final List<T> items, final A position,
				final Comparator<C> comparator) {
			final Integer id = lastBeforeEqual(items, position, comparator).findId();
			if (id == null) {
				return Optional.empty();
			}
			if (comparator.compare(items.get(id), position) != 0) {
				return Optional.empty();
			}

			return Optional.of(id);
		}

		public List<Integer> getIdsForPositions(final ChartData chartData, final Collection<C> positions) {
			final List<T> items = itemsSupplier.items(chartData);
			final Comparator<C> comparator = comparator();
			return positions.stream()//
					.flatMap(p -> findIdForPosition(items, p, comparator).stream())//
					.collect(Collectors.toList());
		}
	}

	public final boolean isWithEnd;
	private final PositionTypeManager<?, ?, ?> manager;

	private <C extends IVirtualConstantPosition, P extends C, T extends P> PositionType(final boolean isWithEnd,
			final PositionDataTypeManager<C, P> positionDataTypeManager,
			final PositionTypeItemsSupplier<T> itemsSupplier, final PositionWithIdMapper<T> itemMapper) {
		this.isWithEnd = isWithEnd;
		manager = new PositionTypeManager<>(positionDataTypeManager, itemsSupplier, itemMapper);
	}

	private static PositionType fromYGuitar(final int y) {
		if (y < toneChangeY) {
			return EVENT_POINT;
		}
		if (y < fhpY) {
			return TONE_CHANGE;
		}
		if (y < lanesTop) {
			return FHP;
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

	private static PositionType fromYShowlights(final int y) {
		if (y < lanesTop) {
			return NONE;
		}
		if (y < lanesBottom) {
			return SHOWLIGHT;
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
			case SHOWLIGHTS -> fromYShowlights(y);
			case VOCALS -> fromYVocals(y);
			case EMPTY -> NONE;
			default -> NONE;
		};
	}

	@SuppressWarnings("unchecked")
	public <C extends IVirtualConstantPosition, P extends C, T extends P> PositionTypeManager<C, P, T> manager() {
		return (PositionTypeManager<C, P, T>) manager;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getPositions(final ChartData data) {
		return (List<T>) manager.getItems(data);
	}

	public List<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return manager.getPositionsWithIdsAndTypes(data);
	}
}