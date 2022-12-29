package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.song.Anchor;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.Event;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Phrase;
import log.charter.song.PhraseIteration;
import log.charter.song.Section;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class GuitarUndoState implements UndoState {
	private final int arrangementId;
	private final int levelId;

	private final BeatsMapUndoState beatsMapUndoState;

	private final ArrayList2<Section> sections;
	private final HashMap2<String, Phrase> phrases;
	private final ArrayList2<PhraseIteration> phraseIterations;
	private final ArrayList2<Event> events;

	private final ArrayList2<ChordTemplate> chordTemplates;
	private final ArrayList2<ChordTemplate> fretHandMuteTemplates;

	private final ArrayList2<Anchor> anchors;
	private final ArrayList2<ChordOrNote> chordsAndNotes;
	private final ArrayList2<HandShape> handShapes;

	private GuitarUndoState(final ChartData data, final int arrangementId, final int levelId,
			final BeatsMapUndoState beatsMapUndoState) {
		this.arrangementId = arrangementId;
		this.levelId = levelId;
		final ArrangementChart arrangement = data.songChart.arrangements.get(arrangementId);
		final Level level = arrangement.levels.get(levelId);

		sections = arrangement.sections.map(section -> new Section(beatsMapUndoState.beatsMap.beats, section));
		phrases = arrangement.phrases.map(phraseName -> phraseName, Phrase::new);
		phraseIterations = arrangement.phraseIterations
				.map(phraseIteration -> new PhraseIteration(beatsMapUndoState.beatsMap.beats, phraseIteration));
		events = arrangement.events.map(event -> new Event(beatsMapUndoState.beatsMap.beats, event));

		chordTemplates = arrangement.chordTemplates.map(ChordTemplate::new);
		fretHandMuteTemplates = arrangement.fretHandMuteTemplates.map(ChordTemplate::new);

		anchors = level.anchors.map(Anchor::new);
		chordsAndNotes = level.chordsAndNotes.map(ChordOrNote::new);
		handShapes = level.handShapes.map(HandShape::new);

		this.beatsMapUndoState = beatsMapUndoState;
	}

	public GuitarUndoState(final ChartData data) {
		this(data, data.currentArrangement, data.currentLevel, new BeatsMapUndoState(data));
	}

	@Override
	public GuitarUndoState undo(final ChartData data) {
		final GuitarUndoState redo = new GuitarUndoState(data, arrangementId, levelId, beatsMapUndoState.undo(data));

		final ArrangementChart arrangement = data.songChart.arrangements.get(arrangementId);
		final Level level = arrangement.levels.get(levelId);

		arrangement.sections = sections;
		arrangement.phrases = phrases;
		arrangement.phraseIterations = phraseIterations;
		arrangement.events = events;

		arrangement.chordTemplates = chordTemplates;
		arrangement.fretHandMuteTemplates = fretHandMuteTemplates;

		level.anchors = anchors;
		level.chordsAndNotes = chordsAndNotes;
		level.handShapes = handShapes;

		return redo;
	}
}
