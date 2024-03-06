package log.charter.gui.components.selectionEditor;

import static log.charter.data.config.Config.frets;
import static log.charter.gui.components.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JTextField;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.IntValueValidator;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.song.Anchor;
import log.charter.util.CollectionUtils.HashSet2;

public class AnchorSelectionEditor {
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private FieldWithLabel<TextInputWithValidation> anchorFret;
	private FieldWithLabel<TextInputWithValidation> anchorWidth;

	public void init(final CurrentSelectionEditor selectionEditor, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		final RowedPosition position = new RowedPosition(10, selectionEditor.sizes);
		final TextInputWithValidation anchorFretInput = generateForInt(1, 20, //
				new IntValueValidator(1, frets), this::changeAnchorFret, false);
		anchorFretInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(anchorFretInput);
		anchorFret = new FieldWithLabel<>(Label.FRET, 100, 30, 20, anchorFretInput, LabelPosition.LEFT);
		selectionEditor.add(anchorFret, position, 140);
		position.newRow();

		final TextInputWithValidation anchorWidthInput = generateForInt(4, 20, //
				new IntValueValidator(4, frets), //
				this::changeAnchorWidth, //
				false);
		anchorWidthInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(anchorWidthInput);
		anchorWidth = new FieldWithLabel<>(Label.ANCHOR_WIDTH, 100, 30, 20, anchorWidthInput, LabelPosition.LEFT);
		selectionEditor.add(anchorWidth, position, 140);

		hideFields();
	}

	private void changeAnchorFret(final int newFret) {
		undoSystem.addUndo();

		final SelectionAccessor<Anchor> anchorSelectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.ANCHOR);
		for (final Selection<Anchor> anchorSelection : anchorSelectionAccessor.getSelectedSet()) {
			anchorSelection.selectable.fret = newFret;
		}
	}

	private void changeAnchorWidth(final int newWidth) {
		undoSystem.addUndo();

		final SelectionAccessor<Anchor> anchorSelectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.ANCHOR);
		for (final Selection<Anchor> anchorSelection : anchorSelectionAccessor.getSelectedSet()) {
			anchorSelection.selectable.width = newWidth;
		}
	}

	public void showFields() {
		anchorFret.setVisible(true);
		anchorWidth.setVisible(true);
	}

	public void hideFields() {
		anchorFret.setVisible(false);
		anchorWidth.setVisible(false);
	}

	public void selectionChanged(final SelectionAccessor<Anchor> selectedAnchorsAccessor) {
		final HashSet2<Selection<Anchor>> selectedAnchors = selectedAnchorsAccessor.getSelectedSet();

		final Integer fret = getSingleValue(selectedAnchors, selection -> selection.selectable.fret, null);
		anchorFret.field.setTextWithoutEvent(fret == null ? "" : (fret + ""));

		final Integer width = getSingleValue(selectedAnchors, selection -> selection.selectable.width, null);
		anchorWidth.field.setTextWithoutEvent(width == null ? "" : (width + ""));
	}
}
