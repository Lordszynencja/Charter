package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;

public class GP7BeatsConverter extends GP7ItemWithIdConverter {
	private final GP7BeatConverter gp7BeatConverter = new GP7BeatConverter();

	@Override
	protected Converter getItemConverter() {
		return gp7BeatConverter;
	}
}
