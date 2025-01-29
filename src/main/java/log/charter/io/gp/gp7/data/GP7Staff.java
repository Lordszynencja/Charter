package log.charter.io.gp.gp7.data;

import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7PropertiesConverter;

@XStreamAlias("Staff")
public class GP7Staff {
	@XStreamAlias("Properties")
	@XStreamConverter(GP7PropertiesConverter.class)
	public Map<String, Map<String, String>> properties;

	public int capoFret() {
		try {
			return Integer.valueOf(properties.get("CapoFret").get("Fret"));
		} catch (final Exception e) {
			return 0;
		}
	}

	public int fretCount() {
		try {
			return Integer.valueOf(properties.get("FretCount").get("Number"));
		} catch (final Exception e) {
			return 24;
		}
	}

	public int partialCapoFret() {
		try {
			return Integer.valueOf(properties.get("PartialCapoFret").get("Fret"));
		} catch (final Exception e) {
			return 0;
		}
	}

	public boolean[] partialCapoStringFlags() {
		try {
			final String flags = properties.get("PartialCapoStringFlags").get("Bitset");
			final boolean[] stringFlags = new boolean[flags.length()];
			for (int i = 0; i < flags.length(); i++) {
				stringFlags[i] = flags.charAt(i) == '1';
			}
			return stringFlags;
		} catch (final Exception e) {
			return new boolean[0];
		}
	}

	public int[] getTuningValues() {
		try {
			final String[] tuningValuesStrings = properties.get("Tuning").get("Pitches").split(" ");
			final int[] tuningValues = new int[tuningValuesStrings.length];
			for (int i = 0; i < tuningValuesStrings.length; i++) {
				tuningValues[i] = Integer.valueOf(tuningValuesStrings[i]);
			}
			return tuningValues;
		} catch (final Exception e) {
			return new int[0];
		}
	}

	public String getInstrument() {
		try {
			return properties.get("Tuning").get("Instrument");
		} catch (final Exception e) {
			return "";
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GP7Stave {");
		for (final Entry<String, Map<String, String>> property : properties.entrySet()) {
			sb.append("\n    " + property.getKey() + "={");
			for (final Entry<String, String> value : property.getValue().entrySet()) {
				sb.append("\n     " + value.getKey() + "=" + value.getValue());
			}
			sb.append("}");
		}

		return sb.append("}").toString();
	}
}
