package log.charter.gui.panes.songEdits;

import javax.swing.JCheckBox;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.tabs.selectionEditor.chords.ChordTemplateEditor;

public class HandShapePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static ChordTemplate prepareTemplateFromData(final ChartData data, final HandShape handShape) {
		return handShape.templateId == null ? new ChordTemplate()
				: new ChordTemplate(data.currentArrangement().chordTemplates.get(handShape.templateId));
	}

	private final ChartData data;

	private final HandShape handShape;
	private final ChordTemplate template;

	private final ChordTemplateEditor editor;
	private final JCheckBox arpeggioCheckBox;

	public HandShapePane(final ChartData data, final CharterFrame frame, final HandShape handShape,
			final Runnable onCancel) {
		super(frame, Label.HAND_SHAPE_PANE, 600);

		this.data = data;

		this.handShape = handShape;
		template = prepareTemplateFromData(data, handShape);

		editor = new ChordTemplateEditor(this.new RowedPanelEmulator());
		editor.init(data, frame, null, () -> template, this::onChordTemplateChange);

		editor.addChordNameSuggestionButton(100, 0);
		editor.addChordNameInput(100, 1);

		addConfigCheckbox(2, 20, 70, Label.ARPEGGIO, template.arpeggio, val -> template.arpeggio = val);
		arpeggioCheckBox = (JCheckBox) getLastPart();

		editor.addChordTemplateEditor(20, 4);
		editor.hideFields();
		editor.showFields();
		editor.setCurrentValuesInInputs();

		addDefaultFinish(6 + data.currentArrangement().tuning.strings(), this::saveAndExit, onCancel);
	}

	private void onChordTemplateChange() {
		arpeggioCheckBox.setSelected(template.arpeggio);
	}

	private void saveAndExit() {
		handShape.templateId = data.currentArrangement().getChordTemplateIdWithSave(template);
	}
}
