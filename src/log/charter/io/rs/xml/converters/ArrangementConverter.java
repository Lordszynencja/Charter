package log.charter.io.rs.xml.converters;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import log.charter.io.rs.xml.song.Arrangement;

public class ArrangementConverter implements SingleValueConverter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Arrangement.class);
	}

	@Override
	public String toString(final Object obj) {
		if (obj == null) {
			return Arrangement.Combo.name();
		}

		final Arrangement value = (Arrangement) obj;
		return value.name();
	}

	@Override
	public Object fromString(final String str) {
		try {
			return Arrangement.valueOf(str);
		} catch (final IllegalArgumentException e) {
			return Arrangement.Combo;
		}
	}

}