package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forBoolean;
import static log.charter.data.config.values.ValueAccessor.forInteger;
import static log.charter.data.config.values.ValueAccessor.forString;

import java.util.Map;

import log.charter.data.GridType;

public class GridConfig {
	public static boolean showGrid = true;
	public static GridType gridType = GridType.BEAT;
	public static int gridSize = 4;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".showGrid", forBoolean(v -> showGrid = v, () -> showGrid));
		valueAccessors.put(name + ".gridType", forString(v -> gridType = GridType.valueOf(v), () -> gridType.name()));
		valueAccessors.put(name + ".gridSize", forInteger(v -> gridSize = v, () -> gridSize));
	}
}
