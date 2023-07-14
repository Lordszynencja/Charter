package log.charter.gui.components.selectionEditor;

import static log.charter.gui.components.selectionEditor.CurrentSelectionEditor.getSingleValue;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.util.CollectionUtils.HashSet2;

public class HandShapeSelectionEditor extends ChordTemplateEditor {

	private JLabel arpeggioLabel;
	private JCheckBox arpeggioCheckBox;

	private ArrangementFixer arrangementFixer;
	private ChartData data;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private ChordTemplate chordTemplate = new ChordTemplate();

	public HandShapeSelectionEditor(final CurrentSelectionEditor parent) {
		super(parent);
	}

	public void init(final CurrentSelectionEditor parent, final ArrangementFixer arrangementFixer, final ChartData data,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		super.init(data, () -> chordTemplate, this::templateEdited);

		this.arrangementFixer = arrangementFixer;
		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		addChordNameSuggestionButton(100, 0);
		addChordNameInput(100, 1);
		parent.addCheckbox(2, 20, 70, Label.ARPEGGIO, false, val -> {
			chordTemplate.arpeggio = val;
			templateEdited();
		});
		arpeggioLabel = (JLabel) parent.components.get(parent.components.size() - 2);
		arpeggioCheckBox = (JCheckBox) parent.components.getLast();
		addChordTemplateEditor(20, 4);

		hideFields();
	}

	private void templateEdited() {
		undoSystem.addUndo();

		final ArrangementChart arrangement = data.getCurrentArrangement();
		final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);

		final SelectionAccessor<HandShape> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.HAND_SHAPE);
		for (final Selection<HandShape> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.templateId = templateId;
		}

		arrangementFixer.fixDuplicatedChordTemplates(arrangement);
		arrangementFixer.removeUnusedChordTemplates(arrangement);
	}

	@Override
	public void showFields() {
		super.showFields();

		arpeggioLabel.setVisible(true);
		arpeggioCheckBox.setVisible(true);
	}

	@Override
	public void hideFields() {
		super.hideFields();

		arpeggioLabel.setVisible(false);
		arpeggioCheckBox.setVisible(false);
	}

	public void selectionChanged(final SelectionAccessor<HandShape> selectedHandShapesAccessor) {
		final HashSet2<Selection<HandShape>> selected = selectedHandShapesAccessor.getSelectedSet();

		final Integer templateId = getSingleValue(selected, selection -> selection.selectable.templateId);
		if (templateId != null) {
			chordTemplate = new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(templateId));
			setCurrentValuesInInputs();
			arpeggioCheckBox.setSelected(chordTemplate.arpeggio);
			return;
		}

		chordTemplate = new ChordTemplate();
		setCurrentValuesInInputs();
		arpeggioCheckBox.setSelected(false);
	}
}
