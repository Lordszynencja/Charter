package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;

public class GP7BarsConverter extends GP7ItemWithIdConverter {
	private final GP7BarConverter gp7BarConverter = new GP7BarConverter();

	@Override
	protected Converter getItemConverter() {
		return gp7BarConverter;
	}

}
