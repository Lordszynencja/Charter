package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;

public class GP7RhythmsConverter extends GP7ItemWithIdConverter {
	private final GP7RhythmConverter gp7RhythmConverter = new GP7RhythmConverter();

	@Override
	protected Converter getItemConverter() {
		return gp7RhythmConverter;
	}

}
