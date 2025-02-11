package log.charter.gui.panes.songEdits;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.services.data.copy.data.FullGuitarCopyData;
import log.charter.services.data.selection.SelectionManager;

public class GuitarSpecialPastePane extends ParamsPane {
	private static boolean pasteSectionsDefault = true;
	private static boolean pastePhrasesDefault = true;
	private static boolean pasteEventsDefault = true;
	private static boolean pasteToneChangesDefault = true;
	private static boolean pasteAnchorsDefault = true;
	private static boolean pasteSoundsDefault = true;
	private static boolean pasteHandShapesDefault = true;

	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartData chartData;
	private final ChordTemplatesEditorTab chordTemplatesEditorTab;
	private final SelectionManager selectionManager;
	private final UndoSystem undoSystem;

	private final FractionalPosition basePosition;

	private final FullGuitarCopyData copyData;

	private boolean pasteUsingBeats = true;

	private boolean pasteSections = pasteSectionsDefault;
	private boolean pastePhrases = pastePhrasesDefault;
	private boolean pasteEvents = pasteEventsDefault;
	private boolean pasteToneChanges = pasteToneChangesDefault;
	private boolean pasteAnchors = pasteAnchorsDefault;
	private boolean pasteSounds = pasteSoundsDefault;
	private boolean pasteHandShapes = pasteHandShapesDefault;

	public GuitarSpecialPastePane(final ChartData data, final CharterFrame frame,
			final ChordTemplatesEditorTab chordTemplatesEditorTab, final SelectionManager selectionManager,
			final UndoSystem undoSystem, final FractionalPosition basePosition,
			final FullGuitarCopyData fullGuitarCopyData) {
		super(frame, Label.SPECIAL_GUITAR_PASTE_PANE, 300);

		chartData = data;
		this.chordTemplatesEditorTab = chordTemplatesEditorTab;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		this.basePosition = basePosition;

		copyData = fullGuitarCopyData;

		int row = 0;
		addConfigCheckbox(row, 20, 0, null, pasteUsingBeats, val -> pasteUsingBeats = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_USE_BEATS, 0);
		row++;

		addConfigCheckbox(row, 20, 0, null, pasteSections, val -> pasteSections = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_SECTIONS, 0);
		addConfigCheckbox(row, 20, 0, null, pastePhrases, val -> pastePhrases = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_PHRASES, 0);
		addConfigCheckbox(row, 20, 0, null, pasteEvents, val -> pasteEvents = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_EVENTS, 0);
		addConfigCheckbox(row, 20, 0, null, pasteToneChanges, val -> pasteToneChanges = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_TONE_CHANGES, 0);
		addConfigCheckbox(row, 20, 0, null, pasteAnchors, val -> pasteAnchors = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_ANCHORS, 0);
		addConfigCheckbox(row, 20, 0, null, pasteSounds, val -> pasteSounds = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_SOUNDS, 0);
		addConfigCheckbox(row, 20, 0, null, pasteHandShapes, val -> pasteHandShapes = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_HAND_SHAPES, 0);

		row++;
		this.setOnFinish(this::saveAndExit, null);
		addDefaultFinish(row);
	}

	private void saveAndExit() {
		undoSystem.addUndo();
		selectionManager.clear();

		pasteSectionsDefault = pasteSections;
		pastePhrasesDefault = pastePhrases;
		pasteEventsDefault = pasteEvents;
		pasteToneChangesDefault = pasteToneChanges;
		pasteAnchorsDefault = pasteAnchors;
		pasteSoundsDefault = pasteSounds;
		pasteHandShapesDefault = pasteHandShapes;

		if (pasteSections || pastePhrases || pasteEvents) {
			copyData.beats.paste(chartData, selectionManager, basePosition, pasteSections, pastePhrases, pasteEvents,
					pasteUsingBeats);
		}
		if (pasteToneChanges) {
			copyData.toneChanges.paste(chartData, selectionManager, basePosition, pasteUsingBeats);
		}
		if (pasteAnchors) {
			copyData.anchors.paste(chartData, selectionManager, basePosition, pasteUsingBeats);
		}
		if (pasteHandShapes) {
			copyData.handShapes.paste(chartData, selectionManager, basePosition, pasteUsingBeats);
		}
		if (pasteSounds) {
			copyData.sounds.paste(chartData, selectionManager, basePosition, pasteUsingBeats);
			chordTemplatesEditorTab.refreshTemplates();
		}
	}
}
