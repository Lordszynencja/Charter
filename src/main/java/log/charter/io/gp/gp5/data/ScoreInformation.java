package log.charter.io.gp.gp5.data;

public class ScoreInformation {
	public final String title;
	public final String subtitle;
	public final String artist;
	public final String album;
	public final String words;
	public final String music;
	public final String copyright;
	public final String tab;
	public final String instructions;
	public final String notices;

	public ScoreInformation(final String title, final String subtitle, final String artist, final String album,
			final String words, final String music, final String copyright, final String tab, final String instructions,
			final String notices) {
		this.title = title;
		this.subtitle = subtitle;
		this.artist = artist;
		this.album = album;
		this.words = words;
		this.music = music;
		this.copyright = copyright;
		this.tab = tab;
		this.instructions = instructions;
		this.notices = notices;
	}

	@Override
	public String toString() {
		return "ScoreInformation [title=" + title + ", subtitle=" + subtitle + ", artist=" + artist + ", album=" + album
				+ ", words=" + words + ", music=" + music + ", copyright=" + copyright + ", tab=" + tab
				+ ", instructions=" + instructions + ", notices=" + notices + "]";
	}

}
