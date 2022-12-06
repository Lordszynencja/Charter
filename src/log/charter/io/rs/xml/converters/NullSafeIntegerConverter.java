package log.charter.io.rs.xml.converters;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class NullSafeIntegerConverter implements SingleValueConverter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Integer.class);
	}

	@Override
	public String toString(final Object obj) {
		if (obj == null) {
			return "";
		}

		return obj.toString();
	}

	@Override
	public Object fromString(final String str) {
		if (str == null || str.length() == 0) {
			return null;
		}

		return Integer.parseInt(str);
	}

}
