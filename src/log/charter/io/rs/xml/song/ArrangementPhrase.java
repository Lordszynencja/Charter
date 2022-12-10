package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Phrase;

@XStreamAlias("phrase")
public class ArrangementPhrase {
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	public int maxDifficulty;
	@XStreamAsAttribute
	public Integer solo;

	public ArrangementPhrase() {
	}

	public ArrangementPhrase(final String name, final Phrase phrase) {
		this.name = name;
		maxDifficulty = phrase.maxDifficulty;
		solo = phrase.solo ? 1 : null;
	}
}
