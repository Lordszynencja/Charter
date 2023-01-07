package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.Beat;
import log.charter.song.PhraseIteration;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("copiedPhraseIteration")
public class CopiedPhraseIterationPosition extends CopiedOnBeatPosition<PhraseIteration> {
	public final String phraseName;

	public CopiedPhraseIterationPosition(final ArrayList2<Beat> beats, final int baseBeat,
			final PhraseIteration onBeat) {
		super(beats, baseBeat, onBeat);
		phraseName = onBeat.phraseName;
	}

	@Override
	protected PhraseIteration createValue() {
		return new PhraseIteration(null, phraseName);
	}
}
