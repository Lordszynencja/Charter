package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.EnumValueAccessor.forEnum;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;

import java.util.Map;

import log.charter.data.config.values.accessors.ValueAccessor;
import log.charter.data.song.BeatsMap.DistanceType;

public class NoteDistanceConfig {
	public static DistanceType minSpaceType = DistanceType.NOTES;
	public static int minSpaceFactor = 32;
	public static DistanceType minLengthType = DistanceType.NOTES;
	public static int minLengthFactor = 8;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".minSpaceType",
				forEnum(DistanceType.class, v -> minSpaceType = v, () -> minSpaceType, minSpaceType));
		valueAccessors.put(name + ".minSpaceFactor",
				forInteger(v -> minSpaceFactor = v, () -> minSpaceFactor, minSpaceFactor));
		valueAccessors.put(name + ".minLengthType",
				forEnum(DistanceType.class, v -> minLengthType = v, () -> minLengthType, minLengthType));
		valueAccessors.put(name + ".minLengthFactor",
				forInteger(v -> minLengthFactor = v, () -> minLengthFactor, minLengthFactor));
	}
}
