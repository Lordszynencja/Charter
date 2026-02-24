package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.data.config.Localization.Label.TONE_NAME_CANT_BE_EMPTY;
import static log.charter.data.config.Localization.Label.TONE_NAME_PAST_LIMIT;
import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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

public class ToneChangeSelectionEditor extends SelectionEditorPart<ToneChange> {
	private ChartData chartData;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private FieldWithLabel<AutocompleteInput<String>> toneNameField;
	private boolean error;
	private Color toneNameInputBackgroundColor;

	public ToneChangeSelectionEditor() {
		super(PositionType.TONE_CHANGE);
	}

	@Override
	public void addTo(final CurrentSelectionEditor selectionEditor) {
		int row = 0;
		final AutocompleteInput<String> toneNameInput = new AutocompleteInput<>(selectionEditor, 200, "",
				this::getPossibleValues, s -> s, this::onSelect);
		addSelectTextOnFocus(toneNameInput);
		toneNameInput.setTextChangeListener(this::onToneNameChange);

		toneNameField = new FieldWithLabel<>(Label.TONE_CHANGE_TONE_NAME, 100, 200, 20, toneNameInput,
				LabelPosition.LEFT);
		toneNameField.setLocation(10, selectionEditor.sizes.getY(row++));
		selectionEditor.add(toneNameField);
	}

	@Override
	public void show(final boolean visibility) {
		toneNameField.setVisible(visibility);
	}

	public void selectionChanged(final ISelectionAccessor<ToneChange> selectedToneChangesAccessor) {
		final List<ToneChange> selectedToneChanges = selectedToneChangesAccessor.getSelectedElements();

		final String toneName = getSingleValue(selectedToneChanges, toneChange -> toneChange.toneName, "");
		toneNameField.field.setTextWithoutUpdate(toneName == null ? "" : toneName);
	}

	private List<String> getPossibleValues(final String name) {
		final List<String> tones = chartData.currentArrangement().tones.stream()//
				.filter(toneName -> !toneName.equals(name) && toneName.toLowerCase().contains(name.toLowerCase()))//
				.collect(Collectors.toCollection(ArrayList::new));
		if (!tones.contains(chartData.currentArrangement().startingTone)
				&& !name.equals(chartData.currentArrangement().startingTone)) {
			tones.add(chartData.currentArrangement().startingTone);
		}

		return tones;
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

	public void onToneNameChange(final String name) {
		clearError();

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
				.collect(Collectors.toCollection(HashSet::new));
	}

	private void onSelect(final String name) {
		toneNameField.field.setTextWithoutUpdate(name);
	}
}
