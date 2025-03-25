package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;
import static log.charter.data.config.values.accessors.EnumValueAccessor.forEnum;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;

import java.util.Map;

import log.charter.data.GridType;
import log.charter.data.config.values.accessors.ValueAccessor;

public class GridConfig {
	public static boolean showGrid = true;
	public static GridType gridType = GridType.BEAT;
	public static int gridSize = 4;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".showGrid", forBoolean(v -> showGrid = v, () -> showGrid, showGrid));
		valueAccessors.put(name + ".gridType", forEnum(GridType.class, v -> gridType = v, () -> gridType, gridType));
		valueAccessors.put(name + ".gridSize", forInteger(v -> gridSize = v, () -> gridSize, gridSize));
	}
}
