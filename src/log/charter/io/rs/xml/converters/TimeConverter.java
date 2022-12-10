package log.charter.io.rs.xml.converters;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class TimeConverter implements SingleValueConverter {
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

		int value = (int) obj;
		String result = "";
		if (value < 0) {
			value = -value;
			result = "-";
		}
		result += value / 1000 + "." + format.format(value % 1000);

		return result;
	}

	@Override
	public Object fromString(final String str) {
		if (str == null || str.length() == 0) {
			return null;
		}

		return Integer.parseInt(str.replaceAll("\\.", ""));
	}

}
