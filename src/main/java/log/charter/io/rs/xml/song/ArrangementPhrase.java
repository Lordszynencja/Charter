package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.Phrase;

@XStreamAlias("phrase")
public class ArrangementPhrase {
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	public int maxDifficulty = 0;
	@XStreamAsAttribute
	public Integer solo = null;

	public ArrangementPhrase(final String name) {
		this.name = name;
		maxDifficulty = 0;
		solo = null;
	}

	public ArrangementPhrase(final String name, final Phrase phrase) {
		this.name = name;
		maxDifficulty = phrase.maxDifficulty;
		if (phrase.solo) {
			solo = 1;
		}
	}
}
