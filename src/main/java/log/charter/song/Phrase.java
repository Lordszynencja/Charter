package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("phrase")
public class Phrase {
	@XStreamAsAttribute
	public int maxDifficulty;
	@XStreamAsAttribute
	public boolean solo;

	public Phrase() {
		this(0, false);
	}

	public Phrase(final int maxDifficulty, final boolean solo) {
		this.maxDifficulty = maxDifficulty;
		this.solo = solo;
	}

	public Phrase(final Phrase other) {
		maxDifficulty = other.maxDifficulty;
		solo = other.solo;
	}
}
