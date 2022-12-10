package log.charter.io.rs.xml.song;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.PhraseIteration;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("phraseIteration")
public class ArrangementPhraseIteration {
	public static ArrayList2<ArrangementPhraseIteration> fromPhraseIterations(
			final ArrayList2<ArrangementPhrase> arrangementPhrases,
			final ArrayList2<PhraseIteration> phraseIterations) {
		final Map<String, Integer> phraseIds = new HashMap<>();
		for (int i = 0; i < arrangementPhrases.size(); i++) {
			phraseIds.put(arrangementPhrases.get(i).name, i);
		}

		return phraseIterations.map(phraseIteration -> new ArrangementPhraseIteration(phraseIteration.position,
				phraseIds.get(phraseIteration.phraseName)));
	}

	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public int phraseId;

	public ArrangementPhraseIteration() {
	}

	private ArrangementPhraseIteration(final int time, final int phraseId) {
		this.time = time;
		this.phraseId = phraseId;
	}
}
