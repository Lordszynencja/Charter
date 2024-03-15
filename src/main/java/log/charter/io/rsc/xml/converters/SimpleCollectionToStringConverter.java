package log.charter.io.rsc.xml.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import log.charter.data.song.EventType;

public class SimpleCollectionToStringConverter<C extends Collection<T>, T> implements SingleValueConverter {
	private final Supplier<C> collectionConstructor;
	private final Function<String, T> elementFromStringConverter;
	private final Function<T, String> elementToStringConverter;

	public SimpleCollectionToStringConverter(final Supplier<C> collectionConstructor,
			final Function<String, T> elementFromStringConverter, final Function<T, String> elementToStringConverter) {
		this.collectionConstructor = collectionConstructor;
		this.elementFromStringConverter = elementFromStringConverter;
		this.elementToStringConverter = elementToStringConverter;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(final Object obj) {
		return ((C) obj).stream()//
				.map(elementToStringConverter)//
				.collect(Collectors.joining(","));
	}

	@Override
	public C fromString(final String str) {
		final C collection = collectionConstructor.get();
		if (str == null || str.isBlank()) {
			return collection;
		}

		for (final String s : str.split(",")) {
			collection.add(elementFromStringConverter.apply(s));
		}

		return collection;
	}

	public static class EventTypesListConverter extends SimpleCollectionToStringConverter<List<EventType>, EventType> {
		public EventTypesListConverter() {
			super(ArrayList::new, EventType::valueOf, EventType::name);
		}
	}

}
