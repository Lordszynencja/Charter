package log.charter.io.gp.gp7.data;

import java.util.List;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias("Track")
@XStreamInclude({ GP7Staff.class })
public class GP7Track {
	@XStreamAlias("Name")
	public String name;
	@XStreamAlias("Staves")
	public List<GP7Staff> staves;

	@Override
	public String toString() {
		return "GP7Track {staves=" + staves.stream().map(GP7Staff::toString).collect(Collectors.joining("\n  "))//
				+ "}";
	}
}
