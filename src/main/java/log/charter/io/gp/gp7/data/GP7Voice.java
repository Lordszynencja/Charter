package log.charter.io.gp.gp7.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7IntegerListConverter;
import log.charter.io.gp.gp7.converters.GP7VoiceConverter;

@XStreamAlias("Voice")
@XStreamConverter(GP7VoiceConverter.class)
public class GP7Voice {
	@XStreamAlias("Beats")
	@XStreamConverter(GP7IntegerListConverter.class)
	public List<Integer> beats;

	@Override
	public String toString() {
		return "GP7Voice {beats=" + beats + "}";
	}
}
