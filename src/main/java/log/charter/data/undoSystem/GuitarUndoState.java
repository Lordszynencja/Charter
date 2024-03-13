package log.charter.data.undoSystem;

import static log.charter.util.CollectionUtils.map;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.Phrase;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.services.data.ChartTimeHandler;

public class GuitarUndoState extends UndoState {
	private final int arrangementId;
	private final int levelId;

	private final Map<String, Phrase> phrases;
	private final List<EventPoint> eventPoints;
	private final List<ChordTemplate> chordTemplates;
	private final List<ToneChange> toneChanges;

	private final List<Anchor> anchors;
	private final List<ChordOrNote> chordsAndNotes;
	private final List<HandShape> handShapes;

	public GuitarUndoState(final ChartData data, final int arrangementId, final int levelId) {
		this.arrangementId = arrangementId;
		this.levelId = levelId;

		final Arrangement arrangement = data.songChart.arrangements.get(arrangementId);
		final Level level = arrangement.getLevel(levelId);

		phrases = map(arrangement.phrases, name -> name, Phrase::new);
		eventPoints = map(arrangement.eventPoints, EventPoint::new);
		chordTemplates = map(arrangement.chordTemplates, ChordTemplate::new);
		toneChanges = map(arrangement.toneChanges, ToneChange::new);

		anchors = map(level.anchors, Anchor::new);
		chordsAndNotes = map(level.sounds, ChordOrNote::from);
		handShapes = map(level.handShapes, HandShape::new);
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
		arrangement.tones = new HashSet<>(map(toneChanges, toneChange -> toneChange.toneName));

		arrangement.toneChanges = toneChanges;
		level.anchors = anchors;
		level.sounds = chordsAndNotes;
		level.handShapes = handShapes;

		return redo;
	}
}
