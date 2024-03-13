package log.charter.io.xstream.converter;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import log.charter.data.song.position.FractionalPosition;

public class FractionalPositionConverter implements SingleValueConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(FractionalPosition.class);
	}

	@Override
	public String toString(final Object obj) {
		return ((FractionalPosition) obj).asString();
	}

	@Override
	public Object fromString(final String str) {
		return FractionalPosition.fromString(str);
	}

}
