package log.charter.data.undoSystem;

import static log.charter.song.notes.IPosition.findClosestId;

import log.charter.data.ChartData;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.song.BeatsMap;
import log.charter.song.Event;
import log.charter.song.Event.EventType;
import log.charter.song.PhraseIteration;
import log.charter.song.Section;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

public class BeatsMapUndoState extends UndoState {
	private static class ArrangementSectionsPhraseIterationsEvents {
		private final ArrayList2<Pair<Integer, SectionType>> sections;
		private final ArrayList2<Pair<Integer, String>> phraseIterations;
		private final ArrayList2<Pair<Integer, EventType>> events;

		public ArrangementSectionsPhraseIterationsEvents(final ArrangementChart arrangementChart,
				final ArrayList2<Beat> beats) {
			sections = arrangementChart.sections//
					.map(section -> new Pair<>(findClosestId(beats, section.beat.position()), section.type));
			phraseIterations = arrangementChart.phraseIterations//
					.map(phraseIteration -> new Pair<>(findClosestId(beats, phraseIteration.beat.position()),
							phraseIteration.phraseName));
			events = arrangementChart.events//
					.map(event -> new Pair<>(findClosestId(beats, event.beat.position()), event.type));
		}

		public void setFor(final ArrangementChart arrangementChart, final ArrayList2<Beat> beats) {
			arrangementChart.sections = sections.map(section -> new Section(beats.get(section.a), section.b));
			arrangementChart.phraseIterations = phraseIterations
					.map(phraseIteration -> new PhraseIteration(beats.get(phraseIteration.a), phraseIteration.b));
			arrangementChart.events = events.map(event -> new Event(beats.get(event.a), event.b));
		}
	}

	private final BeatsMap beatsMap;
	private final ArrayList2<Pair<Integer, ArrangementSectionsPhraseIterationsEvents>> arrangementsEvents;

	private BeatsMapUndoState(final ChartData data, final BeatsMap beatsMap) {
		this.beatsMap = beatsMap;

		arrangementsEvents = data.songChart.arrangements//
				.mapWithId((id, arrangement) -> new Pair<>(id,
						new ArrangementSectionsPhraseIterationsEvents(arrangement, beatsMap.beats)));
	}

	public BeatsMapUndoState(final ChartData data) {
		this(data, new BeatsMap(data.songChart.beatsMap));
	}

	@Override
	public BeatsMapUndoState undo(final ChartData data) {
		final BeatsMapUndoState redo = new BeatsMapUndoState(data, data.songChart.beatsMap);

		data.songChart.beatsMap = beatsMap;

		arrangementsEvents.forEach(arrangementEvents -> arrangementEvents.b
				.setFor(data.songChart.arrangements.get(arrangementEvents.a), data.songChart.beatsMap.beats));

		return redo;
	}

}
