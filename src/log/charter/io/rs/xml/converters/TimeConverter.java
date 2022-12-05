package log.charter.io.rs.xml.converters;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class TimeConverter implements SingleValueConverter {
	public static final TimeConverter instance = new TimeConverter();

	private final NumberFormat format = new DecimalFormat("000");

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Integer.class);
	}

	@Override
	public String toString(final Object obj) {
		if (obj == null) {
			return null;
		}

		final int value = (int) obj;
		return value / 1000 + "." + format.format(value % 1000);
	}

	@Override
	public Object fromString(final String str) {
		if (str == null || str.length() == 0) {
			return null;
		}

		return Integer.parseInt(str.replaceAll("\\.", ""));
	}

}
