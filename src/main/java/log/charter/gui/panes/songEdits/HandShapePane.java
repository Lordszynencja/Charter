package log.charter.gui.panes.songEdits;

import javax.swing.JCheckBox;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.chords.ChordTemplateEditor;

public class HandShapePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static ChordTemplate prepareTemplateFromData(final ChartData data, final HandShape handShape) {
		return handShape.templateId == null ? new ChordTemplate()
				: new ChordTemplate(data.currentArrangement().chordTemplates.get(handShape.templateId));
	}

	private final ChartData data;
	private final ChordTemplatesEditorTab chordTemplatesEditorTab;

	private final HandShape handShape;
	private final ChordTemplate template;

	private final ChordTemplateEditor editor;
	private final JCheckBox arpeggioCheckBox;
	private final JCheckBox forceArpeggioInRSCheckbox;

	public HandShapePane(final ChartData data, final CharterFrame frame,
			final ChordTemplatesEditorTab chordTemplatesEditorTab, final HandShape handShape, final Runnable onCancel) {
		super(frame, Label.HAND_SHAPE_PANE, 600);

		this.data = data;
		this.chordTemplatesEditorTab = chordTemplatesEditorTab;

		this.handShape = handShape;
		template = prepareTemplateFromData(data, handShape);

		editor = new ChordTemplateEditor(this.new RowedPanelEmulator());
		editor.init(data, frame, null, () -> template, this::onChordTemplateChange);

		editor.addChordNameSuggestionButton(100, 0);
		editor.addChordNameInput(100, 1);

		addConfigCheckbox(2, 20, 70, Label.ARPEGGIO, template.arpeggio, this::onArpeggioChanged);
		arpeggioCheckBox = (JCheckBox) getPart(-1);
		addConfigCheckbox(2, 130, 30, Label.FORCE_ARPEGGIO_IN_RS, template.forceArpeggioInRS,
				v -> template.forceArpeggioInRS = v);
		forceArpeggioInRSCheckbox = (JCheckBox) getPart(-1);
		forceArpeggioInRSCheckbox.setEnabled(template.arpeggio);

		editor.addChordTemplateEditor(20, 4);
		editor.hideFields();
		editor.showFields();
		editor.setCurrentValuesInInputs();

		setOnFinish(this::saveAndExit, onCancel);
		addDefaultFinish(6 + data.currentArrangement().tuning.strings());
	}

	private void onArpeggioChanged(final boolean newArpeggio) {
		template.arpeggio = newArpeggio;

		if (!newArpeggio) {
			template.forceArpeggioInRS = false;
			forceArpeggioInRSCheckbox.setSelected(false);
		}

		forceArpeggioInRSCheckbox.setEnabled(newArpeggio);
	}

	private void onChordTemplateChange() {
		arpeggioCheckBox.setSelected(template.arpeggio);
	}

	private void saveAndExit() {
		handShape.templateId = data.currentArrangement().getChordTemplateIdWithSave(template);
		chordTemplatesEditorTab.refreshTemplates();
	}
}
