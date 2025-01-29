package log.charter.io.gp.gp7.data;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7BeatConverter;

@XStreamAlias("Beat")
@XStreamConverter(GP7BeatConverter.class)
public class GP7Beat {
	public int rhythmReference = -1;
	public boolean tremolo;
	public List<Integer> notes = new ArrayList<>();

	@Override
	public String toString() {
		return "GP7Beat [rhythmReference=" + rhythmReference + ", tremolo=" + tremolo + ", notes=" + notes + "]";
	}

}
