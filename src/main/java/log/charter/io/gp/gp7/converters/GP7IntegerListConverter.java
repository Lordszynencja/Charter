package log.charter.io.gp.gp7.converters;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class GP7IntegerListConverter implements SingleValueConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return true;
	}

	@Override
	public String toString(final Object obj) {
		return null;
	}

	@Override
	public List<Integer> fromString(final String str) {
		final List<Integer> ids = new ArrayList<>();
		for (final String id : str.split(" ")) {
			ids.add(Integer.valueOf(id));
		}

		return ids;
	}

}
