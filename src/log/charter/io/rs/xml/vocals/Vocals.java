package log.charter.io.rs.xml.vocals;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias("vocals")
@XStreamInclude(value = { Vocal.class })
public class Vocals {
	public Vocals(final List<Vocal> vocals) {
		this.vocals = vocals;
	}

	public List<Vocal> vocals = new ArrayList<>();
}