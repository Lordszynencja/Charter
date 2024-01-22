package log.charter.gui.panes;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.copySystem.data.FullGuitarCopyData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;

public class GuitarSpecialPastePane extends ParamsPane {
	private static boolean pasteSectionsDefault = true;
	private static boolean pastePhrasesDefault = true;
	private static boolean pasteEventsDefault = true;
	private static boolean pasteToneChangesDefault = true;
	private static boolean pasteAnchorsDefault = true;
	private static boolean pasteSoundsDefault = true;
	private static boolean pasteHandShapesDefault = true;

	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 300;

		return sizes;
	}

	private final ChartData data;
	private final SelectionManager selectionManager;
	private final UndoSystem undoSystem;

	private final FullGuitarCopyData copyData;

	private boolean pasteSections = pasteSectionsDefault;
	private boolean pastePhrases = pastePhrasesDefault;
	private boolean pasteEvents = pasteEventsDefault;
	private boolean pasteToneChanges = pasteToneChangesDefault;
	private boolean pasteAnchors = pasteAnchorsDefault;
	private boolean pasteSounds = pasteSoundsDefault;
	private boolean pasteHandShapes = pasteHandShapesDefault;

	public GuitarSpecialPastePane(final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final UndoSystem undoSystem,
			final FullGuitarCopyData fullGuitarCopyData) {
		super(frame, Label.SPECIAL_GUITAR_PASTE_PANE, getSizes());

		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		copyData = fullGuitarCopyData;

		int row = 0;
		addConfigCheckbox(row, 20, 0, null, pasteSections, val -> pasteSections = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_SECTIONS);
		addConfigCheckbox(row, 20, 0, null, pastePhrases, val -> pastePhrases = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_PHRASES);
		addConfigCheckbox(row, 20, 0, null, pasteEvents, val -> pasteEvents = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_EVENTS);
		addConfigCheckbox(row, 20, 0, null, pasteToneChanges, val -> pasteToneChanges = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_TONE_CHANGES);
		addConfigCheckbox(row, 20, 0, null, pasteAnchors, val -> pasteAnchors = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_ANCHORS);
		addConfigCheckbox(row, 20, 0, null, pasteSounds, val -> pasteSounds = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_SOUNDS);
		addConfigCheckbox(row, 20, 0, null, pasteHandShapes, val -> pasteHandShapes = val);
		addLabel(row++, 50, Label.SPECIAL_GUITAR_PASTE_HAND_SHAPES);

		row++;
		addDefaultFinish(row, this::saveAndExit);
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
			copyData.beats.paste(data, pasteSections, pastePhrases, pasteEvents);
		}
		if (pasteToneChanges) {
			copyData.toneChanges.paste(data);
		}
		if (pasteAnchors) {
			copyData.anchors.paste(data);
		}
		if (pasteSounds) {
			copyData.sounds.paste(data);
		}
		if (pasteHandShapes) {
			copyData.handShapes.paste(data);
		}
	}
}
