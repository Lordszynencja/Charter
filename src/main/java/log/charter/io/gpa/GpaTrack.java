package log.charter.io.gpa;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("track")
public class GpaTrack {
	@XStreamAsAttribute
	public int id;
	@XStreamAsAttribute
	public String title;
	@XStreamAsAttribute
	public String artist;

	public String scoreUrl;
	public String audioUrl;
	@XStreamAlias("sync")
	@XStreamConverter(GpaSyncPointsConverter.class)
	public List<GpaSyncPoint> syncPoints;
}
