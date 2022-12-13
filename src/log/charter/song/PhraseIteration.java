package log.charter.song;

import static java.util.stream.Collectors.toCollection;

import java.util.List;

import log.charter.io.rs.xml.song.ArrangementPhrase;
import log.charter.io.rs.xml.song.ArrangementPhraseIteration;
import log.charter.util.CollectionUtils.ArrayList2;

public class PhraseIteration extends Position {
	public static ArrayList2<PhraseIteration> fromArrangementPhraseIterations(
			final List<ArrangementPhrase> arrangementPhrases,
			final List<ArrangementPhraseIteration> arrangementPhraseIterations) {
		return arrangementPhraseIterations.stream()//
				.map(arrangementPhraseIteration -> {
					final String phraseName = arrangementPhrases.get(arrangementPhraseIteration.phraseId).name;
					return new PhraseIteration(arrangementPhraseIteration.time, phraseName);
				})//
				.collect(toCollection(ArrayList2::new));
	}

	public String phraseName;

	public PhraseIteration(final int pos, final String phraseName) {
		super(pos);
		this.phraseName = phraseName;
	}

	public PhraseIteration(final PhraseIteration other) {
		super(other);
		phraseName = other.phraseName;
	}
}
