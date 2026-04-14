package log.charter.services.editModes;

import static java.lang.System.nanoTime;
import static log.charter.data.ChordTemplateFingerSetter.setSuggestedFingers;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.util.CollectionUtils.lastBefore;
import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.Utils.nvl;

import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.FHP;
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
import log.charter.services.data.ChartItemsHandler.Insertable;
import log.charter.services.data.GuitarSoundsHandler;
import log.charter.services.data.GuitarSoundsStatusesHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId;
import log.charter.util.collections.Pair;

public class GuitarModeHandler implements ModeHandler {
	private static final long scrollTimeoutForUndo = 1000;

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private ChartItemsHandler chartItemsHandler;
	private CharterFrame charterFrame;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private CurrentSelectionEditor currentSelectionEditor;
	private GuitarSoundsHandler guitarSoundsHandler;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private HighlightManager highlightManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private long lastScrollTime = -scrollTimeoutForUndo;
	private int lastFretNumber = 0;
	private long fretNumberTimer = 0;
	private int typingNumber = 0;

	private void addOrUpdateEventPoint(final Insertable<EventPoint> insertable) {
		selectionManager.clear();

		if (insertable.item != null) {
			new GuitarEventPointPane(chartData, charterFrame, undoSystem, insertable.item, () -> {});
			return;
		}

		undoSystem.addUndo();
		final EventPoint eventPoint = new EventPoint(insertable.position);
		final List<EventPoint> eventPoints = chartData.currentEventPoints();
		final int id = insertable.itemId == null ? 0 : insertable.itemId + 1;
		eventPoints.add(id, eventPoint);

		new GuitarEventPointPane(chartData, charterFrame, undoSystem, eventPoint, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	public void insertEventPoint() {
		addOrUpdateEventPoint(chartItemsHandler.getItemForInsert(chartData.currentEventPoints()));
	}

	private void rightClickEventPoint(final PositionWithIdAndType eventPointPosition) {
		final FractionalPosition position = eventPointPosition.toFraction(chartData.beats()).position();
		Integer itemId = eventPointPosition.id;
		if (itemId == null) {
			itemId = lastBeforeEqual(chartData.currentEventPoints(), position).findId();
		}
		final EventPoint item = eventPointPosition.eventPoint;

		addOrUpdateEventPoint(new Insertable<>(position, itemId, item));
	}

	private void addOrUpdateToneChange(final Insertable<ToneChange> insertable) {
		selectionManager.clear();

		if (insertable.item != null) {
			new ToneChangePane(chartData, charterFrame, undoSystem, insertable.item, () -> {});
			return;
		}

		undoSystem.addUndo();
		final ToneChange toneChange = new ToneChange(insertable.position);
		final List<ToneChange> toneChanges = chartData.currentToneChanges();
		final int id = insertable.itemId == null ? 0 : insertable.itemId + 1;
		toneChanges.add(id, toneChange);

		new ToneChangePane(chartData, charterFrame, undoSystem, toneChange, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	public void insertToneChange() {
		addOrUpdateToneChange(chartItemsHandler.getItemForInsert(chartData.currentToneChanges()));
	}

	private void rightClickToneChange(final PositionWithIdAndType toneChangePosition) {
		final FractionalPosition position = toneChangePosition.toFraction(chartData.beats()).position();
		Integer itemId = toneChangePosition.id;
		if (itemId == null) {
			itemId = lastBeforeEqual(chartData.currentToneChanges(), position).findId();
		}
		final ToneChange item = toneChangePosition.toneChange;

		addOrUpdateToneChange(new Insertable<>(position, itemId, item));
	}

	private void addOrUpdateFHP(final Insertable<FHP> insertable) {
		selectionManager.clear();

		if (insertable.item != null) {
			new FHPPane(chartData, charterFrame, undoSystem, insertable.item, () -> {});
			return;
		}

		undoSystem.addUndo();
		final FHP fhp = new FHP(insertable.position);
		final List<FHP> fhps = chartData.currentFHPs();
		final int id = insertable.itemId == null ? 0 : insertable.itemId + 1;
		fhps.add(id, fhp);

		new FHPPane(chartData, charterFrame, undoSystem, fhp, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	public void insertFHP() {
		addOrUpdateFHP(chartItemsHandler.getItemForInsert(chartData.currentFHPs()));
	}

	private void rightClickFHP(final PositionWithIdAndType fhpPosition) {
		final FractionalPosition position = fhpPosition.toFraction(chartData.beats()).position();
		Integer itemId = fhpPosition.id;
		if (itemId == null) {
			itemId = lastBeforeEqual(chartData.currentFHPs(), position).findId();
		}
		final FHP item = fhpPosition.fhp;

		addOrUpdateFHP(new Insertable<>(position, itemId, item));
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

	private int toggleStringForSound(final FractionalPosition position, final int string, final Integer id,
			final ChordOrNote chordOrNote) {
		if (string < 0 || string >= chartData.currentStrings()) {
			selectionManager.addSoundSelection(id);
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

	private int toggleStringForHandShape(final int string, final Integer id, final HandShape handShape) {
		if (string < 0 || string >= chartData.currentStrings()) {
			selectionManager.addSelection(PositionType.HAND_SHAPE, id);
			return 0;
		}

		final List<ChordTemplate> chordTemplates = chartData.currentChordTemplates();
		final ChordTemplate chordTemplate = new ChordTemplate(chordTemplates.get(handShape.templateId));

		if (chordTemplate.frets.size() == 1 && chordTemplate.frets.containsKey(string)) {
			chartData.currentHandShapes().remove((int) id);
			return -1;
		}

		if (chordTemplate.frets.containsKey(string)) {
			chordTemplate.frets.remove(string);
		} else {
			final int fret = chordTemplate.frets.values().stream().collect(Collectors.minBy(Integer::compare))
					.orElse(0);
			chordTemplate.frets.put(string, fret);
		}

		setSuggestedFingers(chordTemplate);

		handShape.templateId = chartData.currentArrangement().getChordTemplateIdWithSave(chordTemplate);

		selectionManager.addSelection(PositionType.HAND_SHAPE, id);
		return 0;
	}

	private void toggleSingleNote(final MouseButtonPressReleaseData clickData) {
		final FractionalPosition position = clickData.pressHighlight.toFraction(chartData.beats()).position();
		final int string = yToString(clickData.pressPosition.y, chartData.currentStrings());
		toggleStringForSound(position, string, clickData.pressHighlight.id, clickData.pressHighlight.chordOrNote);
	}

	private void toggleMultipleNotes(final MouseButtonPressReleaseData clickData) {
		final List<PositionWithStringOrNoteId> positions = highlightManager.getPositionsWithStrings(
				clickData.pressHighlight.toPosition(chartData.beats()).position(), //
				clickData.releaseHighlight.toPosition(chartData.beats()).position(), //
				clickData.pressPosition.y, //
				clickData.releasePosition.y);

		int noteIdChange = 0;
		final ImmutableBeatsMap beats = chartData.beats();
		for (final PositionWithStringOrNoteId position : positions) {
			noteIdChange += toggleStringForSound(position.toFraction(beats).position(), position.string,
					position.noteId == null ? null : (position.noteId + noteIdChange), position.sound);
		}
	}

	private void rightClickGuitarNote(final MouseButtonPressReleaseData clickData) {
		selectionManager.clear();
		undoSystem.addUndo();

		if (!clickData.isXDrag()) {
			toggleSingleNote(clickData);
		} else {
			toggleMultipleNotes(clickData);
		}

		chordTemplatesEditorTab.refreshTemplates();
	}

	private void toggleStringInSounds(final List<Selection<ChordOrNote>> soundSelections, final int string) {
		selectionManager.clear();
		int idDifference = 0;
		for (final Selection<ChordOrNote> selection : soundSelections) {
			idDifference += toggleStringForSound(selection.selectable.position(), string, selection.id + idDifference,
					selection.selectable);
		}
	}

	private void toggleStringInHandShapes(final List<Selection<HandShape>> handShapeSelections, final int string) {
		selectionManager.clear();
		int idDifference = 0;
		for (final Selection<HandShape> selection : handShapeSelections) {
			idDifference += toggleStringForHandShape(string, selection.id + idDifference, selection.selectable);
		}
	}

	private void toggleStringForInsertable(final Insertable<ChordOrNote> insertable, final int string) {
		selectionManager.clear();
		toggleStringForSound(insertable.position, string, insertable.itemId, insertable.item);
	}

	public void toggleString(final int string) {
		switch (selectionManager.selectedType()) {
			case GUITAR_NOTE:
				final List<Selection<ChordOrNote>> selectedSounds = selectionManager
						.getSelected(PositionType.GUITAR_NOTE);
				if (selectedSounds.size() > 1) {
					toggleStringInSounds(selectedSounds, string);
					return;
				}
				break;
			case HAND_SHAPE:
				toggleStringInHandShapes(selectionManager.getSelected(PositionType.HAND_SHAPE), string);
				return;
			default:
				break;
		}

		toggleStringForInsertable(chartItemsHandler.getItemForInsert(chartData.currentSounds()), string);
	}

	public void insertHandShape() {
		final Insertable<HandShape> insertable = chartItemsHandler.getItemForInsert(chartData.currentHandShapes());

		selectionManager.clear();

		if (insertable.item != null) {
			new HandShapePane(chartData, charterFrame, chordTemplatesEditorTab, insertable.item, () -> {});
			return;
		}

		undoSystem.addUndo();

		final FractionalPosition endPosition = chartData.beats().getMinEndPositionAfter(insertable.position)
				.toFraction(chartData.beats()).position();

		final HandShape handShape = new HandShape(insertable.position, endPosition);
		final List<HandShape> handShapes = chartData.currentHandShapes();
		final int id = insertable.itemId == null ? 0 : insertable.itemId + 1;
		handShapes.add(id, handShape);
		arrangementFixer.fixLengths(handShapes);

		new HandShapePane(chartData, charterFrame, chordTemplatesEditorTab, handShape, () -> {
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
				currentSelectionEditor.getEditedStrings());
	}

	private void changeHandShapesLength(final int change) {
		chartItemsHandler.changePositionsWithLengthsByGrid(
				selectionManager.getSelectedElements(PositionType.HAND_SHAPE), chartData.currentHandShapes(), change);
	}

	@Override
	public void changeLength(final int change) {
		if (System.currentTimeMillis() - lastScrollTime > scrollTimeoutForUndo) {
			undoSystem.addUndo();
		}

		changeNotesLength(change);
		changeHandShapesLength(change);

		currentSelectionEditor.selectionChanged(false);
		lastScrollTime = System.currentTimeMillis();
	}

	private void setFretNumberTimer() {
		fretNumberTimer = nanoTime() / 1_000_000 + 2000;
	}

	@Override
	public void handleNumber(final int number) {
		if (nanoTime() / 1_000_000 <= fretNumberTimer) {
			if (lastFretNumber * 10 + number <= InstrumentConfig.frets) {
				lastFretNumber = lastFretNumber * 10 + number;
			} else {
				lastFretNumber = number;
			}
		} else {
			clearNumbers();
			lastFretNumber = number;
		}

		setFretNumberTimer();

		guitarSoundsHandler.setFret(lastFretNumber, typingNumber);
	}

	@Override
	public void clearNumbers() {
		fretNumberTimer = 0;
		lastFretNumber = 0;
		typingNumber = 0;
	}

	public void switchTypingPart() {
		typingNumber++;
		lastFretNumber = 0;
		setFretNumberTimer();
	}
}
