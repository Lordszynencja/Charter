package log.charter.data.undoSystem;

import static java.util.stream.Collectors.toCollection;

import log.charter.data.ChartData;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.song.Anchor;
import log.charter.song.Arrangement;
import log.charter.song.ChordTemplate;
import log.charter.song.EventPoint;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Phrase;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;

public class GuitarUndoState extends UndoState {
	private final int arrangementId;
	private final int levelId;

	private final HashMap2<String, Phrase> phrases;
	private final ArrayList2<EventPoint> eventPoints;
	private final ArrayList2<ChordTemplate> chordTemplates;
	private final ArrayList2<ToneChange> toneChanges;

	private final ArrayList2<Anchor> anchors;
	private final ArrayList2<ChordOrNote> chordsAndNotes;
	private final ArrayList2<HandShape> handShapes;

	public GuitarUndoState(final ChartData data, final int arrangementId, final int levelId) {
		this.arrangementId = arrangementId;
		this.levelId = levelId;

		final Arrangement arrangement = data.songChart.arrangements.get(arrangementId);
		final Level level = arrangement.getLevel(levelId);

		phrases = arrangement.phrases.map(name -> name, Phrase::new);
		eventPoints = arrangement.eventPoints.map(EventPoint::new);
		chordTemplates = arrangement.chordTemplates.map(ChordTemplate::new);
		toneChanges = arrangement.toneChanges.map(ToneChange::new);

		anchors = level.anchors.map(Anchor::new);
		chordsAndNotes = level.sounds.map(ChordOrNote::from);
		handShapes = level.handShapes.map(HandShape::new);
	}

	public GuitarUndoState(final ChartData data) {
		this(data, data.currentArrangement, data.currentLevel);
	}

	@Override
	public GuitarUndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler) {
		final GuitarUndoState redo = new GuitarUndoState(data, arrangementId, levelId);

		final Arrangement arrangement = data.songChart.arrangements.get(arrangementId);
		final Level level = arrangement.levels.get(levelId);

		arrangement.phrases = phrases;
		arrangement.eventPoints = eventPoints;
		arrangement.chordTemplates = chordTemplates;
		arrangement.tones = toneChanges.stream().map(toneChange -> toneChange.toneName)
				.collect(toCollection(HashSet2::new));

		arrangement.toneChanges = toneChanges;
		level.anchors = anchors;
		level.sounds = chordsAndNotes;
		level.handShapes = handShapes;

		return redo;
	}
}
