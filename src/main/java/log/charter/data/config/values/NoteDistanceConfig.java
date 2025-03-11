package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forInteger;
import static log.charter.data.config.values.ValueAccessor.forString;

import java.util.Map;

import log.charter.data.song.BeatsMap.DistanceType;

public class NoteDistanceConfig {
	public static DistanceType minSpaceType = DistanceType.NOTES;
	public static int minSpaceFactor = 32;
	public static DistanceType minLengthType = DistanceType.NOTES;
	public static int minLengthFactor = 8;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".minSpaceType",
				forString(v -> minSpaceType = DistanceType.valueOf(v), () -> minSpaceType.name()));
		valueAccessors.put(name + ".minSpaceFactor", forInteger(v -> minSpaceFactor = v, () -> minSpaceFactor));
		valueAccessors.put(name + ".minLengthType",
				forString(v -> minLengthType = DistanceType.valueOf(v), () -> minLengthType.name()));
		valueAccessors.put(name + ".minLengthFactor", forInteger(v -> minLengthFactor = v, () -> minLengthFactor));
	}
}
