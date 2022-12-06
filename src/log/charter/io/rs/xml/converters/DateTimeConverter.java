package log.charter.io.rs.xml.converters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class DateTimeConverter implements SingleValueConverter {
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M-d-yy H:mm");

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(LocalDateTime.class);
	}

	@Override
	public String toString(final Object obj) {
		if (obj == null) {
			return null;
		}

		final LocalDateTime value = (LocalDateTime) obj;
		return (value == null ? LocalDateTime.now() : value).format(formatter);
	}

	@Override
	public Object fromString(final String str) {
		if (str == null || str.length() == 0) {
			return null;
		}

		return LocalDateTime.parse(str, formatter);
	}

}
