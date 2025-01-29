package log.charter.io.gp.gp7.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7IntegerListConverter;
import log.charter.io.gp.gp7.converters.GP7TimeSignatureConverter;
import log.charter.util.data.TimeSignature;

@XStreamAlias("MasterBar")
public class GP7MasterBar {
	@XStreamAlias("Time")
	@XStreamConverter(GP7TimeSignatureConverter.class)
	public TimeSignature timeSignature;
	@XStreamAlias("Bars")
	@XStreamConverter(GP7IntegerListConverter.class)
	public List<Integer> bars;

	@Override
	public String toString() {
		return "GP7MasterBar {timeSignature=" + timeSignature + ", bars=" + bars + "}";
	}

}
