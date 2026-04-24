package log.charter.io.rsc.xml.converters;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.util.collections.Pair;

public class MapConverter<K, V> implements Converter {
	public static final class MapConverterIntDouble extends MapConverter<Integer, Double> {
		public MapConverterIntDouble() {
			super(MapConverter::toInt, MapConverter::toDouble);
		}
	}

	private static Object readBasicEntry(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		reader.moveDown();
		final Object value = switch (reader.getNodeName()) {
			case "int" -> Integer.parseInt(reader.getValue());
			case "double" -> Double.parseDouble(reader.getValue());
			default -> reader.getValue();
		};
		reader.moveUp();
		return value;
	}

	public static Integer toInt(final Object o) {
		if (o instanceof Integer) {
			return (Integer) o;
		}
		if (o instanceof Number) {
			return ((Number) o).intValue();
		}
		if (o instanceof String) {
			return Integer.valueOf((String) o);
		}

		return null;
	}

	public static Double toDouble(final Object o) {
		if (o instanceof Double) {
			return (Double) o;
		}
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof String) {
			return Double.valueOf((String) o);
		}

		return null;
	}

	private final Function<Object, K> keyReader;
	private final Function<K, String> keyWriter;
	private final Function<Object, V> valueReader;
	private final Function<V, String> valueWriter;

	public MapConverter(final Function<Object, K> keyReader, final Function<K, String> keyWriter,
			final Function<Object, V> valueReader, final Function<V, String> valueWriter) {
		this.keyReader = keyReader;
		this.keyWriter = keyWriter;
		this.valueReader = valueReader;
		this.valueWriter = valueWriter;
	}

	public MapConverter(final Function<Object, K> keyReader, final Function<Object, V> valueReader) {
		this(keyReader, Object::toString, valueReader, Object::toString);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return Map.class.isAssignableFrom(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Map<K, V> map = (Map<K, V>) source;

		for (final Entry<K, V> entry : map.entrySet()) {
			writer.startNode("entry");
			writer.addAttribute("key", keyWriter.apply(entry.getKey()));
			writer.addAttribute("value", valueWriter.apply(entry.getValue()));
			writer.endNode();
		}
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final Map<K, V> map = new HashMap<>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			Pair<Object, Object> entryValues;
			if (reader.getAttribute("key") != null) {
				entryValues = new Pair<>(reader.getAttribute("key"), reader.getAttribute("value"));
			} else {
				final Object k = readBasicEntry(reader, context);
				final Object v = readBasicEntry(reader, context);
				entryValues = new Pair<>(k, v);
			}

			final K key = keyReader.apply(entryValues.a);
			final V value = valueReader.apply(entryValues.b);
			reader.moveUp();
			if (key != null) {
				map.put(key, value);
			}
		}

		return map;
	}

}
