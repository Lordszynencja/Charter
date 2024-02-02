package log.charter.data.managers.modes;

import static log.charter.data.ChordTemplateFingerSetter.setSuggestedFingers;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.song.notes.IConstantPosition.findClosestId;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;
import static log.charter.song.notes.IPositionWithLength.changePositionsWithLengthsLength;

import java.util.stream.Collectors;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.PositionWithStringOrNoteId;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.AnchorPane;
import log.charter.gui.panes.GuitarEventPointPane;
import log.charter.gui.panes.HandShapePane;
import log.charter.gui.panes.ToneChangePane;
import log.charter.song.Anchor;
import log.charter.song.ChordTemplate;
import log.charter.song.EventPoint;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPositionWithLength;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class GuitarModeHandler extends ModeHandler {
	private static final long scrollTimeoutForUndo = 1000;

	private CurrentSelectionEditor currentSelectionEditor;
	private ChartData data;
	private CharterFrame frame;
	private HighlightManager highlightManager;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private long lastScrollTime = -scrollTimeoutForUndo;

	public void init(final CurrentSelectionEditor currentSelectionEditor, final ChartData data,
			final CharterFrame frame, final HighlightManager highlightManager, final KeyboardHandler keyboardHandler,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.currentSelectionEditor = currentSelectionEditor;
		this.data = data;
		this.frame = frame;
		this.highlightManager = highlightManager;
		this.keyboardHandler = keyboardHandler;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	@Override
	public void handleEnd() {
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().chordsAndNotes;
		if (!sounds.isEmpty()) {
			frame.setNextTime(sounds.getLast().position());
		}
	}

	@Override
	public void handleHome() {
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().chordsAndNotes;

		if (!sounds.isEmpty()) {
			frame.setNextTime(sounds.get(0).position());
		}
	}

	private void rightClickAnchor(final PositionWithIdAndType anchorPosition) {
		selectionManager.clear();

		if (anchorPosition.anchor != null) {
			new AnchorPane(data, frame, undoSystem, anchorPosition.anchor, () -> {
			});
			return;
		}

		undoSystem.addUndo();
		final Anchor anchor = new Anchor(anchorPosition.position(), 0);
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;
		anchors.add(anchor);
		anchors.sort(null);

		new AnchorPane(data, frame, undoSystem, anchor, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void rightClickEventPoint(final PositionWithIdAndType eventPointPosition) {
		selectionManager.clear();

		if (eventPointPosition.eventPoint != null) {
			new GuitarEventPointPane(data, frame, undoSystem, eventPointPosition.eventPoint, () -> {
			});
			return;
		}

		undoSystem.addUndo();
		final EventPoint eventPoint = new EventPoint(eventPointPosition.position());
		final ArrayList2<EventPoint> eventPoints = data.getCurrentArrangement().eventPoints;
		eventPoints.add(eventPoint);
		eventPoints.sort(null);

		new GuitarEventPointPane(data, frame, undoSystem, eventPoint, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void rightClickHandShape(final PositionWithIdAndType handShapePosition) {
		selectionManager.clear();
		if (handShapePosition.handShape != null) {
			data.getCurrentArrangementLevel().handShapes.remove((int) handShapePosition.id);
			return;
		}

		undoSystem.addUndo();

		final int endPosition = data.songChart.beatsMap.getNextPositionFromGridAfter(handShapePosition.position());

		final HandShape handShape = new HandShape(handShapePosition.position(),
				endPosition - handShapePosition.position());
		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		handShapes.add(handShape);
		handShapes.sort(null);

		new HandShapePane(data, frame, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private ChordOrNote addSound(final Note note) {
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().chordsAndNotes;
		final ChordOrNote sound = new ChordOrNote(note);
		sounds.add(new ChordOrNote(note));
		sounds.sort(null);

		final int previousId = findLastIdBefore(sounds, note.position());
		if (previousId != -1) {
			ArrangementFixer.fixSoundLength(previousId, sounds);
		}

		selectionManager.addSoundSelection(findClosestId(sounds, note));

		return sound;
	}

	private int addOrRemoveSingleNote(final int string, final int position, final Integer id,
			final ChordOrNote chordOrNote) {
		if (string < 0 || string >= data.currentStrings()) {
			return 0;
		}

		if (chordOrNote == null) {
			addSound(new Note(position, string, 0));
			return 1;
		}

		if (chordOrNote.isNote() && chordOrNote.note.string == string) {
			data.getCurrentArrangementLevel().chordsAndNotes.remove((int) id);
			return -1;
		}

		if (chordOrNote.isChord()) {
			final ChordTemplate chordTemplate = new ChordTemplate(
					data.getCurrentArrangement().chordTemplates.get(chordOrNote.chord.templateId()));
			if (chordTemplate.frets.containsKey(string)) {
				chordTemplate.frets.remove(string);
			} else {
				final int fret = chordTemplate.frets.values().stream().collect(Collectors.minBy(Integer::compare))
						.orElse(0);
				chordTemplate.frets.put(string, fret);
			}

			setSuggestedFingers(chordTemplate);

			final int newTemplateId = data.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);
			chordOrNote.chord.updateTemplate(newTemplateId, chordTemplate);
			if (chordTemplate.frets.size() == 1) {
				chordOrNote.turnToNote(chordTemplate);
			}
		} else {
			final ChordTemplate chordTemplate = new ChordTemplate();
			chordTemplate.frets.put(chordOrNote.note.string, chordOrNote.note.fret);
			chordTemplate.frets.put(string, chordOrNote.note.fret);
			setSuggestedFingers(chordTemplate);

			final int chordId = data.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);
			chordOrNote.turnToChord(chordId, chordTemplate);
		}

		selectionManager.addSoundSelection(id);
		return 0;
	}

	private void addSingleNote(final MouseButtonPressReleaseData clickData) {
		final int string = yToString(clickData.pressPosition.y, data.currentStrings());
		addOrRemoveSingleNote(string, clickData.pressHighlight.position(), clickData.pressHighlight.id,
				clickData.pressHighlight.chordOrNote);
	}

	private void addMultipleNotes(final MouseButtonPressReleaseData clickData) {
		final ArrayList2<PositionWithStringOrNoteId> positions = highlightManager.getPositionsWithStrings(
				clickData.pressHighlight.position(), clickData.releaseHighlight.position(), clickData.pressPosition.y,
				clickData.releasePosition.y);

		int noteIdChange = 0;
		for (final PositionWithStringOrNoteId position : positions) {
			noteIdChange += addOrRemoveSingleNote(position.string, position.position(),
					position.noteId == null ? null : (position.noteId + noteIdChange), position.sound);
		}
	}

	private void rightClickGuitarNote(final MouseButtonPressReleaseData clickData) {
		selectionManager.clear();
		undoSystem.addUndo();

		if (!clickData.isXDrag()) {
			addSingleNote(clickData);
			return;
		}

		addMultipleNotes(clickData);
	}

	private void rightClickToneChange(final PositionWithIdAndType toneChangePosition) {
		selectionManager.clear();

		if (toneChangePosition.toneChange != null) {
			new ToneChangePane(data, frame, undoSystem, toneChangePosition.toneChange, () -> {
			});
			return;
		}

		undoSystem.addUndo();
		final ToneChange toneChange = new ToneChange(toneChangePosition.position(), "");
		final ArrayList2<ToneChange> toneChanges = data.getCurrentArrangement().toneChanges;
		toneChanges.add(toneChange);
		toneChanges.sort(null);

		new ToneChangePane(data, frame, undoSystem, toneChange, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.pressHighlight.type == PositionType.EVENT_POINT) {
			if (clickData.isXDrag()) {
				return;
			}

			rightClickEventPoint(clickData.pressHighlight);
			return;
		}
		if (clickData.pressHighlight.type == PositionType.TONE_CHANGE) {
			if (clickData.isXDrag()) {
				return;
			}

			rightClickToneChange(clickData.pressHighlight);
			return;
		}
		if (clickData.pressHighlight.type == PositionType.ANCHOR) {
			if (clickData.isXDrag()) {
				return;
			}

			rightClickAnchor(clickData.pressHighlight);
			return;
		}

		if (clickData.pressHighlight.type == PositionType.GUITAR_NOTE) {
			rightClickGuitarNote(clickData);
			return;
		}

		if (clickData.pressHighlight.type == PositionType.HAND_SHAPE) {
			if (clickData.isXDrag()) {
				return;
			}

			rightClickHandShape(clickData.pressHighlight);
			return;
		}
	}

	private void changeNotesLength(final int change) {
		final SelectionAccessor<ChordOrNote> selectedNotes = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);

		IPositionWithLength.changeSoundsLength(data.songChart.beatsMap, selectedNotes.getSortedSelected(),
				data.getCurrentArrangementLevel().chordsAndNotes, change, !keyboardHandler.alt(),
				currentSelectionEditor.getSelectedStrings());
	}

	private void changeHandShapesLength(final int change) {
		final SelectionAccessor<HandShape> selectedNotes = selectionManager
				.getSelectedAccessor(PositionType.HAND_SHAPE);
		changePositionsWithLengthsLength(data.songChart.beatsMap, selectedNotes.getSortedSelected(),
				data.getCurrentArrangementLevel().handShapes, change);
	}

	@Override
	public void changeLength(int change) {
		if (keyboardHandler.shift()) {
			change *= 4;
		}

		if (System.currentTimeMillis() - lastScrollTime > scrollTimeoutForUndo) {
			undoSystem.addUndo();
		}

		changeNotesLength(change);
		changeHandShapesLength(change);

		frame.selectionChanged(false);
		lastScrollTime = System.currentTimeMillis();
	}
}
