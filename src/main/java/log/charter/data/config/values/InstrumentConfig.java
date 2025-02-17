package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forBoolean;
import static log.charter.data.config.values.ValueAccessor.forInteger;

import java.util.Map;

public class InstrumentConfig implements ConfigValue {
	public boolean leftHanded = false;
	public int maxStrings = 9;
	public int frets = 28;
	/**
	 * in half steps
	 */
	public int maxBendValue = 6;

	@Override
	public void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".leftHanded", forBoolean(v -> leftHanded = v, () -> leftHanded));
		valueAccessors.put(name + ".maxStrings", forInteger(v -> maxStrings = v, () -> maxStrings));
		valueAccessors.put(name + ".frets", forInteger(v -> frets = v, () -> frets));
		valueAccessors.put(name + ".maxBendValue", forInteger(v -> maxBendValue = v, () -> maxBendValue));
	}

}
