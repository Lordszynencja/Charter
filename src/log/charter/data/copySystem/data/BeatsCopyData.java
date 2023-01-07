package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedEventPosition;
import log.charter.data.copySystem.data.positions.CopiedPhraseIterationPosition;
import log.charter.data.copySystem.data.positions.CopiedSectionPosition;
import log.charter.song.ArrangementChart;
import log.charter.song.Phrase;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

@XStreamAlias("beatsCopyData")
public class BeatsCopyData implements ICopyData {
	public final ArrayList2<CopiedSectionPosition> sections;
	public final HashMap2<String, Phrase> phrases;
	public final ArrayList2<CopiedPhraseIterationPosition> phraseIterations;
	public final ArrayList2<CopiedEventPosition> events;

	public BeatsCopyData(final ArrayList2<CopiedSectionPosition> sections, final HashMap2<String, Phrase> phrases,
			final ArrayList2<CopiedPhraseIterationPosition> phraseIterations,
			final ArrayList2<CopiedEventPosition> events) {
		this.sections = sections;
		this.phrases = phrases;
		this.phraseIterations = phraseIterations;
		this.events = events;
	}

	@Override
	public boolean isEmpty() {
		return sections.isEmpty() && phrases.isEmpty() && phraseIterations.isEmpty() && events.isEmpty();
	}

	@Override
	public void paste(final ChartData data) {
		final ArrangementChart arrangement = data.getCurrentArrangement();
		for (final CopiedPhraseIterationPosition phraseIteration : phraseIterations) {
			final String phraseName = phraseIteration.phraseName;
			if (!arrangement.phrases.containsKey(phraseName)) {
				arrangement.phrases.put(phraseName, phrases.get(phraseName));
			}
		}

		ICopyData.simplePasteOnBeat(data.songChart.beatsMap.beats, data.time, data.getCurrentArrangement().sections,
				sections);
		ICopyData.simplePasteOnBeat(data.songChart.beatsMap.beats, data.time,
				data.getCurrentArrangement().phraseIterations, phraseIterations);
		ICopyData.simplePasteOnBeat(data.songChart.beatsMap.beats, data.time, data.getCurrentArrangement().events,
				events);
	}
}
