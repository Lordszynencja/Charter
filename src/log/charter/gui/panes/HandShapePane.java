package log.charter.gui.panes;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ChordTemplateEditor;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;

public class HandShapePane extends ChordTemplateEditor {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.width = 400;

		return sizes;
	}

	private static ChordTemplate prepareTemplateFromData(final ChartData data, final HandShape handShape) {
		return handShape.chordId == -1 ? new ChordTemplate()
				: new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(handShape.chordId));
	}

	private final UndoSystem undoSystem;

	private final HandShape handShape;

	public HandShapePane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final HandShape handShape) {
		super(data, frame, Label.HAND_SHAPE_PANE, 8 + data.getCurrentArrangement().tuning.strings, getSizes(),
				prepareTemplateFromData(data, handShape));
		this.undoSystem = undoSystem;

		this.handShape = handShape;

		addChordNameSuggestionButton(100, 0);
		addChordNameInput(100, 1);
		addConfigCheckbox(2, 20, 70, Label.ARPEGGIO, chordTemplate.arpeggio, val -> chordTemplate.arpeggio = val);
		addChordTemplateEditor(4);

		addDefaultFinish(7 + data.getCurrentArrangement().tuning.strings, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();
		handShape.chordId = getSavedTemplateId();
		dispose();
	}
}
