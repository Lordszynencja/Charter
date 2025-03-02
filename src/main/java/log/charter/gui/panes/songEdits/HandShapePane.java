package log.charter.gui.panes.songEdits;

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
import log.charter.gui.components.utils.RowedPosition;

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
	private JCheckBox arpeggioCheckBox;
	private JCheckBox forceArpeggioInRSCheckbox;

	public HandShapePane(final ChartData data, final CharterFrame frame,
			final ChordTemplatesEditorTab chordTemplatesEditorTab, final HandShape handShape, final Runnable onCancel) {
		super(frame, Label.HAND_SHAPE_PANE, 600);

		this.data = data;
		this.chordTemplatesEditorTab = chordTemplatesEditorTab;

		this.handShape = handShape;
		template = prepareTemplateFromData(data, handShape);

		initChordTemplateEditor();

		final RowedPosition position = new RowedPosition(20, panel.sizes);
		editor.addChordNameSuggestionButton(position.addX(80).x(), position.row());

		position.newRow();
		editor.addChordNameInput(position.x(), position.row());

		position.newRow();
		addArpeggio(position);
		addForceArpeggioInRS(position);

		position.newRows(2);
		addEditor(position);

		position.newRows(2 + data.currentArrangement().tuning.strings());
		addDefaultFinish(position.y(), SaverWithStatus.defaultFor(this::saveAndExit),
				SaverWithStatus.defaultFor(onCancel), true);
	}

	private void onChordTemplateChange() {
		arpeggioCheckBox.setSelected(template.arpeggio);
	}

	private void initChordTemplateEditor() {
		editor = new ChordTemplateEditor(panel);
		editor.init(data, frame, null, () -> template, this::onChordTemplateChange);
	}

	private void onArpeggioChanged(final boolean newArpeggio) {
		template.arpeggio = newArpeggio;

		if (!newArpeggio) {
			template.forceArpeggioInRS = false;
			forceArpeggioInRSCheckbox.setSelected(false);
		}

		forceArpeggioInRSCheckbox.setEnabled(newArpeggio);
	}

	private void addArpeggio(final RowedPosition position) {
		arpeggioCheckBox = new JCheckBox();
		arpeggioCheckBox.setSelected(template.arpeggio);
		arpeggioCheckBox.addActionListener(e -> onArpeggioChanged(arpeggioCheckBox.isSelected()));

		final FieldWithLabel<JCheckBox> field = new FieldWithLabel<>(Label.ARPEGGIO, 70, 20, 20, arpeggioCheckBox,
				LabelPosition.LEFT);

		panel.add(field, position);
	}

	private void onForceArpeggioInRSChanged(final boolean newValue) {
		template.forceArpeggioInRS = newValue;
	}

	private void addForceArpeggioInRS(final RowedPosition position) {
		forceArpeggioInRSCheckbox = new JCheckBox();
		forceArpeggioInRSCheckbox.setSelected(template.forceArpeggioInRS);
		forceArpeggioInRSCheckbox.setEnabled(template.arpeggio);
		forceArpeggioInRSCheckbox
				.addActionListener(e -> onForceArpeggioInRSChanged(forceArpeggioInRSCheckbox.isSelected()));

		final FieldWithLabel<JCheckBox> field = new FieldWithLabel<>(Label.FORCE_ARPEGGIO_IN_RS, 30, 20, 20,
				forceArpeggioInRSCheckbox, LabelPosition.LEFT_CLOSE);

		panel.add(field, position);
	}

	private void addEditor(final RowedPosition position) {
		editor.addChordTemplateEditor(position.x(), position.row());
		editor.showFields();
		editor.setCurrentValuesInInputs();
	}

	private void saveAndExit() {
		handShape.templateId = data.currentArrangement().getChordTemplateIdWithSave(template);
		chordTemplatesEditorTab.refreshTemplates();
	}
}
