package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;
import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.util.List;

import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.FHP;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.SelectionManager;

public class FHPSelectionEditor {
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private boolean undoAdded = false;

	private FieldWithLabel<TextInputWithValidation> fhpFret;
	private FieldWithLabel<TextInputWithValidation> fhpWidth;

	public void addTo(final CurrentSelectionEditor currentSelectionEditor) {
		final RowedPosition position = new RowedPosition(10, currentSelectionEditor.sizes);
		final TextInputWithValidation fhpFretInput = generateForInt(1, 20, //
				new IntValueValidator(1, Config.instrument.frets), this::changeFHPFret, false);
		fhpFretInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(fhpFretInput);
		fhpFret = new FieldWithLabel<>(Label.FRET, 100, 30, 20, fhpFretInput, LabelPosition.LEFT);
		currentSelectionEditor.add(fhpFret, position, 140);
		position.newRow();

		final TextInputWithValidation fhpWidthInput = generateForInt(4, 20, //
				new IntValueValidator(4, Config.instrument.frets), //
				this::changeFHPWidth, //
				false);
		fhpWidthInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(fhpWidthInput);
		fhpWidth = new FieldWithLabel<>(Label.FHP_WIDTH, 100, 30, 20, fhpWidthInput, LabelPosition.LEFT);
		currentSelectionEditor.add(fhpWidth, position, 140);
	}

	private void addUndo() {
		if (undoAdded) {
			return;
		}

		undoSystem.addUndo();
		undoAdded = true;
	}

	private List<FHP> getItems() {
		return selectionManager.getSelectedElements(PositionType.FHP);
	}

	private void changeFHPFret(final int newFret) {
		addUndo();
		getItems().forEach(fhp -> fhp.fret = newFret);
	}

	private void changeFHPWidth(final int newWidth) {
		addUndo();
		getItems().forEach(fhp -> fhp.width = newWidth);
	}

	public void hide() {
		fhpFret.setVisible(false);
		fhpWidth.setVisible(false);
	}

	public void selectionChanged(final ISelectionAccessor<FHP> selectedFHPsAccessor) {
		final List<FHP> selectedFHPs = selectedFHPsAccessor.getSelectedElements();

		final Integer fret = getSingleValue(selectedFHPs, fhp -> fhp.fret, null);
		fhpFret.field.setTextWithoutEvent(fret == null ? "" : (fret + ""));
		fhpFret.setVisible(true);

		final Integer width = getSingleValue(selectedFHPs, fhp -> fhp.width, null);
		fhpWidth.field.setTextWithoutEvent(width == null ? "" : (width + ""));
		fhpWidth.setVisible(true);
	}
}
