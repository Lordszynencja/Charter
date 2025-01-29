package log.charter.io.gp.gp7.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7BarConverter;
import log.charter.io.gp.gp7.converters.GP7IntegerListConverter;

@XStreamAlias("Bar")
@XStreamConverter(GP7BarConverter.class)
public class GP7Bar {
	@XStreamAlias("Voices")
	@XStreamConverter(GP7IntegerListConverter.class)
	public List<Integer> voices;

	@Override
	public String toString() {
		return "GP7Bar {voices=" + voices + "}";
	}

}
