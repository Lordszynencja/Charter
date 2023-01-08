package log.charter.song;

import static java.util.stream.Collectors.toCollection;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.io.rs.xml.song.ArrangementPhrase;
import log.charter.io.rs.xml.song.ArrangementPhraseIteration;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("phraseIteration")
public class PhraseIteration extends OnBeat {
	public static ArrayList2<PhraseIteration> fromArrangementPhraseIterations(final ArrayList2<Beat> beats,
			final List<ArrangementPhrase> arrangementPhrases,
			final List<ArrangementPhraseIteration> arrangementPhraseIterations) {
		return arrangementPhraseIterations.stream()//
				.map(arrangementPhraseIteration -> {
					final String phraseName = arrangementPhrases.get(arrangementPhraseIteration.phraseId).name;
					return new PhraseIteration(beats, arrangementPhraseIteration.time, phraseName);
				})//
				.collect(toCollection(ArrayList2::new));
	}

	@XStreamAsAttribute
	public String phraseName;

	public PhraseIteration(final Beat beat, final String phraseName) {
		super(beat);
		this.phraseName = phraseName;
	}

	private PhraseIteration(final ArrayList2<Beat> beats, final int time, final String phraseName) {
		super(beats, time);
		this.phraseName = phraseName;
	}

	public PhraseIteration(final ArrayList2<Beat> beats, final PhraseIteration other) {
		super(beats, other);
		phraseName = other.phraseName;
	}
}
