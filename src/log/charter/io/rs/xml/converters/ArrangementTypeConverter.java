package log.charter.io.rs.xml.converters;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import log.charter.io.rs.xml.song.ArrangementType;

public class ArrangementTypeConverter implements SingleValueConverter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(ArrangementType.class);
	}

	@Override
	public String toString(final Object obj) {
		if (obj == null) {
			return ArrangementType.Combo.name();
		}

		final ArrangementType value = (ArrangementType) obj;
		return value.name();
	}

	@Override
	public Object fromString(final String str) {
		try {
			return ArrangementType.valueOf(str);
		} catch (final IllegalArgumentException e) {
			return ArrangementType.Combo;
		}
	}

}