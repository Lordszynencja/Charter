package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.util.CollectionUtils.firstAfterEqual;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.selectionEditor.chords.ChordTemplateEditor;
import log.charter.services.data.fixers.DuplicatedChordTemplatesRemover;
import log.charter.services.data.fixers.UnusedChordTemplatesRemover;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;

public class HandShapeSelectionEditor extends ChordTemplateEditor {

	private JButton setTemplateButton;
	private JLabel arpeggioLabel;
	private JCheckBox arpeggioCheckBox;

	private ChartData chartData;
	private CharterFrame charterFrame;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private ChordTemplate chordTemplate = new ChordTemplate();

	public HandShapeSelectionEditor(final CurrentSelectionEditor parent) {
		super(parent);
	}

	public void addTo(final CurrentSelectionEditor parent) {
		super.init(chartData, charterFrame, keyboardHandler, () -> chordTemplate, this::templateEdited);

		addChordNameSuggestionButton(100, 0);
		addSetTemplateButton(300, 0);

		addChordNameInput(100, 1);
		parent.addCheckbox(2, 20, 70, Label.ARPEGGIO, false, val -> {
			chordTemplate.arpeggio = val;
			templateEdited();
		});
		arpeggioLabel = (JLabel) parent.getPart(parent.getPartsSize() - 2);
		arpeggioCheckBox = (JCheckBox) parent.getLastPart();
		addChordTemplateEditor(20, 4);

		hideFields();
	}

	private void addSetTemplateButton(final int x, final int row) {
		setTemplateButton = new JButton(Label.SET_TEMPLATE_ON_CHORDS.label());
		setTemplateButton.addActionListener(a -> setTemplateForChordsInsideSelectedHandShapes());

		parent.addWithSettingSize(setTemplateButton, x, parent.sizes.getY(row), 150, 20);
	}

	private void templateEdited() {
		undoSystem.addUndo();

		final Arrangement arrangement = chartData.currentArrangement();
		final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);

		final ISelectionAccessor<HandShape> selectionAccessor = selectionManager.accessor(PositionType.HAND_SHAPE);
		for (final Selection<HandShape> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.templateId = templateId;
		}

		DuplicatedChordTemplatesRemover.remove(arrangement);
		UnusedChordTemplatesRemover.remove(arrangement);
	}

	private void setTemplateForChordsIn(final HandShape handShape) {
		final ChordTemplate template = chartData.currentArrangement().chordTemplates.get(handShape.templateId);
		final List<ChordOrNote> sounds = chartData.currentArrangementLevel().sounds;

		final Integer from = firstAfterEqual(sounds, handShape).findId();
		final Integer to = lastBeforeEqual(sounds, handShape.asEndPosition()).findId();
		if (from == null || to == null) {
			return;
		}

		for (int i = from; i <= to; i++) {
			final ChordOrNote sound = sounds.get(i);
			if (!sound.isChord()) {
				continue;
			}

			sound.chord().updateTemplate(handShape.templateId, template);
		}
	}

	private void setTemplateForChordsInsideSelectedHandShapes() {
		final ISelectionAccessor<HandShape> selectionAccessor = selectionManager.accessor(PositionType.HAND_SHAPE);
		for (final Selection<HandShape> selection : selectionAccessor.getSelectedSet()) {
			setTemplateForChordsIn(selection.selectable);
		}
	}

	@Override
	public void showFields() {
		super.showFields();

		setTemplateButton.setVisible(true);
		arpeggioLabel.setVisible(true);
		arpeggioCheckBox.setVisible(true);
	}

	@Override
	public void hideFields() {
		super.hideFields();

		setTemplateButton.setVisible(false);
		arpeggioLabel.setVisible(false);
		arpeggioCheckBox.setVisible(false);
	}

	private void setEmptyTemplate() {
		chordTemplate = new ChordTemplate();
		setCurrentValuesInInputs();
		arpeggioCheckBox.setSelected(false);
	}

	public void selectionChanged(final ISelectionAccessor<HandShape> selectedHandShapesAccessor) {
		final Set<Selection<HandShape>> selected = selectedHandShapesAccessor.getSelectedSet();

		final Integer templateId = getSingleValue(selected, selection -> selection.selectable.templateId, null);
		if (templateId == null) {
			setEmptyTemplate();
			return;
		}

		chordTemplate = new ChordTemplate(chartData.currentArrangement().chordTemplates.get(templateId));
		setCurrentValuesInInputs();
		arpeggioCheckBox.setSelected(chordTemplate.arpeggio);
	}
}
