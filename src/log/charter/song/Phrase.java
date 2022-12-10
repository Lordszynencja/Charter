package log.charter.song;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.song.ArrangementPhrase;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.Pair;

@XStreamAlias("phrase")
public class Phrase {
	public static HashMap2<String, Phrase> fromArrangementPhrases(final List<ArrangementPhrase> arrangementPhrases) {
		return new ArrayList2<>(arrangementPhrases)
				.toMap(arrangementPhrase -> new Pair<>(arrangementPhrase.name, new Phrase(arrangementPhrase)));
	}

	public int maxDifficulty;
	public boolean solo;

	public Phrase(final int maxDifficulty, final boolean solo) {
		this.maxDifficulty = maxDifficulty;
		this.solo = solo;
	}

	private Phrase(final ArrangementPhrase arrangementPhrase) {
		maxDifficulty = arrangementPhrase.maxDifficulty;
		solo = arrangementPhrase.solo != null && arrangementPhrase.solo == 1;
	}
}
