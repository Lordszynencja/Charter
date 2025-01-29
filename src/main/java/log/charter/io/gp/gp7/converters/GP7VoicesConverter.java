package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;

public class GP7VoicesConverter extends GP7ItemWithIdConverter {
	private final GP7VoiceConverter gp7VoiceConverter = new GP7VoiceConverter();

	@Override
	protected Converter getItemConverter() {
		return gp7VoiceConverter;
	}
}
