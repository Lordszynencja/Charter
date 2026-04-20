package log.charter.gui.panes.songEdits;

import static log.charter.data.config.GraphicalConfig.inputSize;

import javax.swing.JCheckBox;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.chords.ChordTemplateEditor;
import log.charter.gui.components.utils.ComponentUtils;

public class HandShapePane extends RowedDialog {
	private static final long serialVersionUID = -4754359602173894487L;

	private static ChordTemplate prepareTemplateFromData(final ChartData data, final HandShape handShape) {
		return handShape.templateId == null ? new ChordTemplate()
				: new ChordTemplate(data.currentArrangement().chordTemplates.get(handShape.templateId));
	}

	private final ChartData data;
	private final ChordTemplatesEditorTab chordTemplatesEditorTab;

	private final HandShape handShape;
	private final ChordTemplate template;

	private ChordTemplateEditor editor;
	private FieldWithLabel<JCheckBox> arpeggio;
	private FieldWithLabel<JCheckBox> forceArpeggioInRS;

	public HandShapePane(final ChartData data, final CharterFrame frame,
			final ChordTemplatesEditorTab chordTemplatesEditorTab, final HandShape handShape, final Runnable onCancel) {
		super(frame, Label.HAND_SHAPE_PANE, inputSize * 30);

		this.data = data;
		this.chordTemplatesEditorTab = chordTemplatesEditorTab;

		this.handShape = handShape;
		template = prepareTemplateFromData(data, handShape);

		initChordTemplateEditor();

		editor.addChordNameSuggestionButton();
		editor.addChordNameInput();
		addArpeggio();
		addForceArpeggioInRS();
		addEditor();

		addDefaultFinish(inputSize * 16, SaverWithStatus.defaultFor(this::saveAndExit),
				SaverWithStatus.defaultFor(onCancel), true);
	}

	private void onChordTemplateChange() {
		arpeggio.field.setSelected(template.arpeggio);
	}

	private void initChordTemplateEditor() {
		editor = new ChordTemplateEditor(panel, true);
		editor.init(data, frame, null, () -> template, this::onChordTemplateChange);
	}

	private void onArpeggioChanged(final boolean newArpeggio) {
		template.arpeggio = newArpeggio;

		if (!newArpeggio) {
			template.forceArpeggioInRS = false;
			forceArpeggioInRS.field.setSelected(false);
		}

		forceArpeggioInRS.field.setEnabled(newArpeggio);
	}

	private void addArpeggio() {
		final JCheckBox arpeggioCheckBox = new JCheckBox();
		arpeggioCheckBox.setSelected(template.arpeggio);
		arpeggioCheckBox.addActionListener(e -> onArpeggioChanged(arpeggioCheckBox.isSelected()));

		arpeggio = new FieldWithLabel<>(Label.ARPEGGIO, 70, 20, 20, arpeggioCheckBox, LabelPosition.LEFT);
		ComponentUtils.resize(arpeggio, inputSize, inputSize * 3, inputSize * 4, inputSize);

		panel.add(arpeggio);
	}

	private void onForceArpeggioInRSChanged(final boolean newValue) {
		template.forceArpeggioInRS = newValue;
	}

	private void addForceArpeggioInRS() {
		final JCheckBox forceArpeggioInRSCheckbox = new JCheckBox();
		forceArpeggioInRSCheckbox.setSelected(template.forceArpeggioInRS);
		forceArpeggioInRSCheckbox.setEnabled(template.arpeggio);
		forceArpeggioInRSCheckbox
				.addActionListener(e -> onForceArpeggioInRSChanged(forceArpeggioInRSCheckbox.isSelected()));

		forceArpeggioInRS = new FieldWithLabel<>(Label.FORCE_ARPEGGIO_IN_RS, 30, 20, 20, forceArpeggioInRSCheckbox,
				LabelPosition.LEFT_CLOSE);
		ComponentUtils.resize(forceArpeggioInRS, inputSize * 7, inputSize * 3, inputSize * 2, inputSize);

		panel.add(forceArpeggioInRS);
	}

	private void addEditor() {
		editor.addChordTemplateEditor(0, 0);
		editor.recalculateSizesWithReposition(inputSize / 2, inputSize / 2);
		editor.setCurrentValuesInInputs();
		editor.showFields();
	}

	private void saveAndExit() {
		handShape.templateId = data.currentArrangement().getChordTemplateIdWithSave(template);
		chordTemplatesEditorTab.refreshTemplates();
	}
}
