package log.charter.services.editModes;

import static log.charter.data.ChordTemplateFingerSetter.setSuggestedFingers;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.song.notes.IConstantPosition.findClosestId;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;
import static log.charter.song.notes.IPositionWithLength.changePositionsWithLengthsLength;

import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.panes.songEdits.AnchorPane;
import log.charter.gui.panes.songEdits.GuitarEventPointPane;
import log.charter.gui.panes.songEdits.HandShapePane;
import log.charter.gui.panes.songEdits.ToneChangePane;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.SelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
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

	private ChartData chartData;
	private CharterFrame charterFrame;
	private CurrentSelectionEditor currentSelectionEditor;
	private HighlightManager highlightManager;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private long lastScrollTime = -scrollTimeoutForUndo;

	private void rightClickAnchor(final PositionWithIdAndType anchorPosition) {
		selectionManager.clear();

		if (anchorPosition.anchor != null) {
			new AnchorPane(chartData, charterFrame, undoSystem, anchorPosition.anchor, () -> {});
			return;
		}

		undoSystem.addUndo();
		final Anchor anchor = new Anchor(anchorPosition.position(), 1);
		final ArrayList2<Anchor> anchors = chartData.getCurrentArrangementLevel().anchors;
		anchors.add(anchor);
		anchors.sort(null);

		new AnchorPane(chartData, charterFrame, undoSystem, anchor, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void rightClickEventPoint(final PositionWithIdAndType eventPointPosition) {
		selectionManager.clear();

		if (eventPointPosition.eventPoint != null) {
			new GuitarEventPointPane(chartData, charterFrame, undoSystem, eventPointPosition.eventPoint, () -> {});
			return;
		}

		undoSystem.addUndo();
		final EventPoint eventPoint = new EventPoint(eventPointPosition.position());
		final ArrayList2<EventPoint> eventPoints = chartData.getCurrentArrangement().eventPoints;
		eventPoints.add(eventPoint);
		eventPoints.sort(null);

		new GuitarEventPointPane(chartData, charterFrame, undoSystem, eventPoint, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void rightClickHandShape(final PositionWithIdAndType handShapePosition) {
		selectionManager.clear();
		if (handShapePosition.handShape != null) {
			chartData.getCurrentArrangementLevel().handShapes.remove((int) handShapePosition.id);
			return;
		}

		undoSystem.addUndo();

		final int endPosition = chartData.songChart.beatsMap.getNextPositionFromGridAfter(handShapePosition.position());

		final HandShape handShape = new HandShape(handShapePosition.position(),
				endPosition - handShapePosition.position());
		final ArrayList2<HandShape> handShapes = chartData.getCurrentArrangementLevel().handShapes;
		handShapes.add(handShape);
		handShapes.sort(null);

		new HandShapePane(chartData, charterFrame, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private ChordOrNote addSound(final Note note) {
		final ArrayList2<ChordOrNote> sounds = chartData.getCurrentArrangementLevel().sounds;
		final ChordOrNote sound = ChordOrNote.from(note);
		sounds.add(sound);
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
		if (string < 0 || string >= chartData.currentStrings()) {
			return 0;
		}

		if (chordOrNote == null) {
			addSound(new Note(position, string, 0));
			return 1;
		}

		if (chordOrNote.isNote() && chordOrNote.note().string == string) {
			chartData.getCurrentArrangementLevel().sounds.remove((int) id);
			return -1;
		}

		final ArrayList2<ChordOrNote> sounds = chartData.getCurrentArrangementLevel().sounds;

		if (chordOrNote.isChord()) {
			final ArrayList2<ChordTemplate> chordTemplates = chartData.getCurrentArrangement().chordTemplates;
			final ChordTemplate chordTemplate = new ChordTemplate(chordTemplates.get(chordOrNote.chord().templateId()));
			if (chordTemplate.frets.containsKey(string)) {
				chordTemplate.frets.remove(string);
			} else {
				final int fret = chordTemplate.frets.values().stream().collect(Collectors.minBy(Integer::compare))
						.orElse(0);
				chordTemplate.frets.put(string, fret);
			}

			setSuggestedFingers(chordTemplate);

			final int newTemplateId = chartData.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);
			chordOrNote.chord().updateTemplate(newTemplateId, chordTemplate);
			if (chordTemplate.frets.size() == 1) {
				sounds.set(id, chordOrNote.asNote(chordTemplates));
			}
		} else {
			final ChordTemplate chordTemplate = new ChordTemplate();
			chordTemplate.frets.put(chordOrNote.note().string, chordOrNote.note().fret);
			chordTemplate.frets.put(string, chordOrNote.note().fret);
			setSuggestedFingers(chordTemplate);

			final int chordId = chartData.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);
			sounds.set(id, chordOrNote.asChord(chordId, chordTemplate));
		}

		selectionManager.addSoundSelection(id);
		return 0;
	}

	private void addSingleNote(final MouseButtonPressReleaseData clickData) {
		final int string = yToString(clickData.pressPosition.y, chartData.currentStrings());
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
			new ToneChangePane(chartData, charterFrame, undoSystem, toneChangePosition.toneChange, () -> {});
			return;
		}

		undoSystem.addUndo();
		final ToneChange toneChange = new ToneChange(toneChangePosition.position(), "");
		final ArrayList2<ToneChange> toneChanges = chartData.getCurrentArrangement().toneChanges;
		toneChanges.add(toneChange);
		toneChanges.sort(null);

		new ToneChangePane(chartData, charterFrame, undoSystem, toneChange, () -> {
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

		IPositionWithLength.changeSoundsLength(chartData.songChart.beatsMap, selectedNotes.getSortedSelected(),
				chartData.getCurrentArrangementLevel().sounds, change, !keyboardHandler.alt(),
				currentSelectionEditor.getSelectedStrings());
	}

	private void changeHandShapesLength(final int change) {
		final SelectionAccessor<HandShape> selectedNotes = selectionManager
				.getSelectedAccessor(PositionType.HAND_SHAPE);
		changePositionsWithLengthsLength(chartData.songChart.beatsMap, selectedNotes.getSortedSelected(),
				chartData.getCurrentArrangementLevel().handShapes, change);
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

		currentSelectionEditor.selectionChanged(false);
		lastScrollTime = System.currentTimeMillis();
	}
}
