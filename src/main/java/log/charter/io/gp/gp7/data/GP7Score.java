package log.charter.io.gp.gp7.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Score")
public class GP7Score {
	@XStreamAlias("Title")
	public String title;
	@XStreamAlias("Artist")
	public String artist;
	@XStreamAlias("Album")
	public String album;

	@Override
	public String toString() {
		return "GP7Score {"//
				+ "\n  title=" + title //
				+ "\n  artist=" + artist //
				+ "\n  album=" + album + "}";
	}

}
