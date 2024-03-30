package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.data.config.Localization.Label.TONE_NAME_CANT_BE_EMPTY;
import static log.charter.data.config.Localization.Label.TONE_NAME_PAST_LIMIT;
import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.util.CollectionUtils.filter;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ToneChange;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.AutocompleteInput;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.collections.HashSet2;

public class ToneChangeSelectionEditor implements DocumentListener {
	private ChartData chartData;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private FieldWithLabel<AutocompleteInput<String>> toneNameField;
	private boolean error;
	private Color toneNameInputBackgroundColor;

	public void addTo(final CurrentSelectionEditor selectionEditor) {
		int row = 0;
		final AutocompleteInput<String> toneNameInput = new AutocompleteInput<String>(selectionEditor, 200, "",
				this::getPossibleValues, s -> s, this::onSelect);
		addSelectTextOnFocus(toneNameInput);
		toneNameInput.getDocument().addDocumentListener(this);

		toneNameField = new FieldWithLabel<>(Label.TONE_CHANGE_TONE_NAME, 100, 200, 20, toneNameInput,
				LabelPosition.LEFT);
		toneNameField.setLocation(10, selectionEditor.sizes.getY(row++));
		selectionEditor.add(toneNameField);

		hideFields();
	}

	public void showFields() {
		toneNameField.setVisible(true);
	}

	public void hideFields() {
		toneNameField.setVisible(false);
	}

	public void selectionChanged(final ISelectionAccessor<ToneChange> selectedToneChangesAccessor) {
		final List<ToneChange> selectedToneChanges = selectedToneChangesAccessor.getSelectedElements();

		final String toneName = getSingleValue(selectedToneChanges, toneChange -> toneChange.toneName, "");
		toneNameField.field.setTextWithoutUpdate(toneName == null ? "" : toneName);
	}

	private List<String> getPossibleValues(final String name) {
		return filter(chartData.currentArrangement().tones,
				toneName -> toneName.toLowerCase().contains(name.toLowerCase()));
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

		final Arrangement arrangement = chartData.currentArrangement();
		if (name.isBlank()) {
			setError(TONE_NAME_CANT_BE_EMPTY);
			return;
		}
		if (arrangement.tones.size() >= 4 && !arrangement.tones.contains(name)) {
			setError(TONE_NAME_PAST_LIMIT);
			return;
		}

		undoSystem.addUndo();

		final List<ToneChange> selected = selectionManager.getSelectedElements(PositionType.TONE_CHANGE);
		for (final ToneChange toneChange : selected) {
			toneChange.toneName = name;
		}
		arrangement.tones = arrangement.toneChanges.stream()//
				.map(t -> t.toneName)//
				.collect(Collectors.toCollection(HashSet2::new));
	}

	private void onSelect(final String name) {
		toneNameField.field.setTextWithoutUpdate(name);
	}
}
