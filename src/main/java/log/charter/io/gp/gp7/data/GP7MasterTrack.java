package log.charter.io.gp.gp7.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7IntegerListConverter;

@XStreamAlias("MasterTrack")
public class GP7MasterTrack {
	@XStreamAlias("Tracks")
	@XStreamConverter(GP7IntegerListConverter.class)
	public List<Integer> tracks;
	@XStreamAlias("Automations")
	public List<GP7Automation> automations;

	@Override
	public String toString() {
		return "GP7MasterTrack [tracks=" + tracks + ", automations=" + automations + "]";
	}

}
