package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.ChartData;

@XStreamAlias("stem")
public class Stem {
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	private final String path;
	@XStreamAsAttribute
	private final boolean local;
	@XStreamAsAttribute
	public double volume = 1;
	@XStreamAsAttribute
	public double offset = 0;

	public Stem(final String name, final String path, final boolean local) {
		this.name = name;
		this.path = path;
		this.local = local;
	}

	public String getPath(final ChartData chartData) {
		if (local) {
			return chartData.path + path;
		}

		return path;
	}
}
