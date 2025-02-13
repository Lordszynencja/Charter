package log.charter.services.editModes;

import static log.charter.data.ChordTemplateFingerSetter.setSuggestedFingers;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.util.CollectionUtils.lastBefore;
import static log.charter.util.Utils.nvl;

import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.song.FHP;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.panes.songEdits.FHPPane;
import log.charter.gui.panes.songEdits.GuitarEventPointPane;
import log.charter.gui.panes.songEdits.HandShapePane;
import log.charter.gui.panes.songEdits.ToneChangePane;
import log.charter.services.data.ChartItemsHandler;
import log.charter.services.data.GuitarSoundsStatusesHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId;
import log.charter.util.collections.Pair;

public class GuitarModeHandler extends ModeHandler {
	private static final long scrollTimeoutForUndo = 1000;

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private ChartItemsHandler chartItemsHandler;
	private CharterFrame charterFrame;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private CurrentSelectionEditor currentSelectionEditor;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private HighlightManager highlightManager;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private long lastScrollTime = -scrollTimeoutForUndo;

	private void rightClickFHP(final PositionWithIdAndType fhpPosition) {
		selectionManager.clear();

		if (fhpPosition.fhp != null) {
			new FHPPane(chartData, charterFrame, undoSystem, fhpPosition.fhp, () -> {});
			return;
		}

		undoSystem.addUndo();

		final FHP fhp = new FHP(fhpPosition.toFraction(chartData.beats()).position());
		final List<FHP> fhps = chartData.currentFHPs();
		fhps.add(fhp);
		fhps.sort(IConstantFractionalPosition::compareTo);

		new FHPPane(chartData, charterFrame, undoSystem, fhp, () -> {
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
		final EventPoint eventPoint = new EventPoint(eventPointPosition.toFraction(chartData.beats()).position());
		final List<EventPoint> eventPoints = chartData.currentEventPoints();
		eventPoints.add(eventPoint);
		eventPoints.sort(IConstantFractionalPosition::compareTo);

		new GuitarEventPointPane(chartData, charterFrame, undoSystem, eventPoint, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void addNewHandShape(final FractionalPosition position) {
		final FractionalPosition endPosition = chartData.beats().addGrid(position, 1).toFraction(chartData.beats())
				.position();

		final HandShape handShape = new HandShape(position, endPosition);
		final List<HandShape> handShapes = chartData.currentHandShapes();
		handShapes.add(handShape);
		handShapes.sort(IConstantFractionalPosition::compareTo);

		new HandShapePane(chartData, charterFrame, chordTemplatesEditorTab, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void rightClickHandShape(final PositionWithIdAndType handShapePosition) {
		selectionManager.clear();

		undoSystem.addUndo();
		if (handShapePosition.handShape != null) {
			chartData.currentArrangementLevel().handShapes.remove((int) handShapePosition.id);
			return;
		}

		addNewHandShape(handShapePosition.toFraction(chartData.beats()).position());
	}

	private ChordOrNote addSound(final Note note) {
		final List<ChordOrNote> sounds = chartData.currentSounds();
		final ChordOrNote sound = ChordOrNote.from(note);
		final Integer previousId = lastBefore(sounds, note).findId();
		final int id = nvl(previousId, -1) + 1;
		sounds.add(id, sound);

		if (previousId != null) {
			arrangementFixer.fixSoundLength(previousId, sounds);
		}
		selectionManager.addSoundSelection(id);

		final Pair<Integer, ChordOrNote> previousSound = ChordOrNote.findPreviousSoundWithIdOnString(note.string,
				id - 1, chartData.currentSounds());
		if (previousSound != null) {
			guitarSoundsStatusesHandler.updateLinkedNote(previousSound.a);
		}

		return sound;
	}

	private int addOrRemoveSingleNote(final FractionalPosition position, final int string, final Integer id,
			final ChordOrNote chordOrNote) {
		if (string < 0 || string >= chartData.currentStrings()) {
			return 0;
		}

		if (chordOrNote == null) {
			addSound(new Note(position, string, 0));
			return 1;
		}

		if (chordOrNote.isNote() && chordOrNote.note().string == string) {
			chartData.currentSounds().remove((int) id);
			final Pair<Integer, ChordOrNote> previousSound = ChordOrNote.findPreviousSoundWithIdOnString(string, id - 1,
					chartData.currentSounds());
			if (previousSound != null) {
				arrangementFixer.fixNoteLengths(chartData.currentSounds(), previousSound.a, previousSound.a);
				guitarSoundsStatusesHandler.updateLinkedNote(previousSound.a);
			}

			return -1;
		}

		final List<ChordOrNote> sounds = chartData.currentSounds();

		if (chordOrNote.isChord()) {
			final List<ChordTemplate> chordTemplates = chartData.currentChordTemplates();
			final ChordTemplate chordTemplate = new ChordTemplate(chordTemplates.get(chordOrNote.chord().templateId()));
			if (chordTemplate.frets.containsKey(string)) {
				chordTemplate.frets.remove(string);
			} else {
				final int fret = chordTemplate.frets.values().stream().collect(Collectors.minBy(Integer::compare))
						.orElse(0);
				chordTemplate.frets.put(string, fret);
			}

			setSuggestedFingers(chordTemplate);

			final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(chordTemplate);
			chordOrNote.chord().updateTemplate(newTemplateId, chordTemplate);
			if (chordTemplate.frets.size() == 1) {
				sounds.set(id, chordOrNote.asNote(chordTemplates));
			}
		} else {
			final ChordTemplate chordTemplate = new ChordTemplate();
			chordTemplate.frets.put(chordOrNote.note().string, chordOrNote.note().fret);
			chordTemplate.frets.put(string, chordOrNote.note().fret);
			setSuggestedFingers(chordTemplate);

			final int chordId = chartData.currentArrangement().getChordTemplateIdWithSave(chordTemplate);
			sounds.set(id, chordOrNote.asChord(chordId, chordTemplate));
		}

		selectionManager.addSoundSelection(id);
		return 0;
	}

	private void addSingleNote(final MouseButtonPressReleaseData clickData) {
		final FractionalPosition position = clickData.pressHighlight.toFraction(chartData.beats()).position();
		final int string = yToString(clickData.pressPosition.y, chartData.currentStrings());
		addOrRemoveSingleNote(position, string, clickData.pressHighlight.id, clickData.pressHighlight.chordOrNote);
	}

	private void addMultipleNotes(final MouseButtonPressReleaseData clickData) {
		final List<PositionWithStringOrNoteId> positions = highlightManager.getPositionsWithStrings(
				clickData.pressHighlight.toPosition(chartData.beats()).position(), //
				clickData.releaseHighlight.toPosition(chartData.beats()).position(), //
				clickData.pressPosition.y, //
				clickData.releasePosition.y);

		int noteIdChange = 0;
		final ImmutableBeatsMap beats = chartData.beats();
		for (final PositionWithStringOrNoteId position : positions) {
			noteIdChange += addOrRemoveSingleNote(position.toFraction(beats).position(), position.string,
					position.noteId == null ? null : (position.noteId + noteIdChange), position.sound);
		}
	}

	private void rightClickGuitarNote(final MouseButtonPressReleaseData clickData) {
		selectionManager.clear();
		undoSystem.addUndo();

		if (!clickData.isXDrag()) {
			addSingleNote(clickData);
		} else {
			addMultipleNotes(clickData);
		}

		chordTemplatesEditorTab.refreshTemplates();
	}

	private void rightClickToneChange(final PositionWithIdAndType toneChangePosition) {
		selectionManager.clear();

		if (toneChangePosition.toneChange != null) {
			new ToneChangePane(chartData, charterFrame, undoSystem, toneChangePosition.toneChange, () -> {});
			return;
		}

		undoSystem.addUndo();
		final ToneChange toneChange = new ToneChange(toneChangePosition.toFraction(chartData.beats()).position(), "");
		final List<ToneChange> toneChanges = chartData.currentToneChanges();
		toneChanges.add(toneChange);
		toneChanges.sort(IConstantFractionalPosition::compareTo);

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
		if (clickData.pressHighlight.type == PositionType.FHP) {
			if (clickData.isXDrag()) {
				return;
			}

			rightClickFHP(clickData.pressHighlight);
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
		chartItemsHandler.changeSoundsLength(selectionManager.getSelected(PositionType.GUITAR_NOTE), change,
				currentSelectionEditor.getSelectedStrings());
	}

	private void changeHandShapesLength(final int change) {
		chartItemsHandler.changePositionsWithLengthsByGrid(
				selectionManager.getSelectedElements(PositionType.HAND_SHAPE), chartData.currentHandShapes(), change);
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
