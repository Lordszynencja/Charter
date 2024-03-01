package log.charter.gui.components.selectionEditor;

import static log.charter.data.config.Localization.Label.TONE_NAME_CANT_BE_EMPTY;
import static log.charter.data.config.Localization.Label.TONE_NAME_PAST_LIMIT;
import static log.charter.gui.components.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.awt.Color;
import java.util.stream.Collectors;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.AutocompleteInput;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.song.Arrangement;
import log.charter.song.ToneChange;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class ToneChangeSelectionEditor implements DocumentListener {

	private ChartData data;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private FieldWithLabel<AutocompleteInput<String>> toneNameField;
	private boolean error;
	private Color toneNameInputBackgroundColor;

	public void init(final CurrentSelectionEditor selectionEditor, final ChartData data,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		int row = 0;
		final AutocompleteInput<String> toneNameInput = new AutocompleteInput<String>(selectionEditor, 200, "",
				this::getPossibleValues, s -> s, this::onSelect);
		addSelectTextOnFocus(toneNameInput);
		toneNameInput.getDocument().addDocumentListener(this);

		toneNameField = new FieldWithLabel<>(Label.TONE_CHANGE_TONE_NAME, 100, 200, 20, toneNameInput,
				LabelPosition.LEFT);
		toneNameField.setLocation(10, selectionEditor.getY(row++));
		selectionEditor.add(toneNameField);

		hideFields();
	}

	public void showFields() {
		toneNameField.setVisible(true);
	}

	public void hideFields() {
		toneNameField.setVisible(false);
	}

	public void selectionChanged(final SelectionAccessor<ToneChange> selectedToneChangesAccessor) {
		final HashSet2<Selection<ToneChange>> selectedToneChanges = selectedToneChangesAccessor.getSelectedSet();

		final String toneName = getSingleValue(selectedToneChanges, selection -> selection.selectable.toneName, "");
		toneNameField.field.setTextWithoutUpdate(toneName == null ? "" : toneName);
	}

	private ArrayList2<String> getPossibleValues(final String name) {
		return data.getCurrentArrangement().tones.stream()//
				.filter(toneName -> toneName.toLowerCase().contains(name.toLowerCase()))//
				.collect(Collectors.toCollection(ArrayList2::new));
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	private void clearError() {
		if (error) {
			toneNameField.field.setToolTipText(null);
			toneNameField.field.setBackground(toneNameInputBackgroundColor);
			error = false;
		}
	}

	private void setError(final Label label) {
		error = true;
		toneNameInputBackgroundColor = toneNameField.field.getBackground();
		toneNameField.field.setBackground(TextInputWithValidation.errorBackground);
		toneNameField.field.setToolTipText(label.label());
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		clearError();

		final String name = toneNameField.field.getText();

		final Arrangement arrangement = data.getCurrentArrangement();
		if (name.isBlank()) {
			setError(TONE_NAME_CANT_BE_EMPTY);
			return;
		}
		if (arrangement.tones.size() >= 4 && !arrangement.tones.contains(name)) {
			setError(TONE_NAME_PAST_LIMIT);
			return;
		}

		undoSystem.addUndo();

		final SelectionAccessor<ToneChange> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.TONE_CHANGE);
		for (final Selection<ToneChange> a : selectionAccessor.getSelectedSet()) {
			a.selectable.toneName = name;
		}
		arrangement.tones = new HashSet2<>(arrangement.toneChanges.map(t -> t.toneName));
	}

	private void onSelect(final String name) {
		toneNameField.field.setTextWithoutUpdate(name);
	}
}
