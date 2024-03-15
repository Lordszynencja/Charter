package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.validators.ValueValidator;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;

public class VocalSelectionEditor {
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private FieldWithLabel<TextInputWithValidation> vocalText;
	private FieldWithLabel<JCheckBox> vocalWordPart;
	private FieldWithLabel<JCheckBox> vocalPhraseEnd;

	public void addTo(final CurrentSelectionEditor selectionEditor) {
		int row = 0;
		final ValueValidator textValidator = s -> s == null || s.isBlank() ? "" : null;
		final TextInputWithValidation vocalTextInput = new TextInputWithValidation("", 100, textValidator,
				this::changeText, false);
		vocalTextInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(vocalTextInput);
		vocalText = new FieldWithLabel<>(Label.VOCAL_PANE_LYRIC, 100, 100, 20, vocalTextInput, LabelPosition.LEFT);
		vocalText.setLocation(10, selectionEditor.sizes.getY(row++));
		selectionEditor.add(vocalText);

		final JCheckBox vocalWordPartInput = new JCheckBox();
		vocalWordPartInput.addActionListener(a -> changeWordPart(vocalWordPartInput.isSelected()));
		vocalWordPart = new FieldWithLabel<>(Label.VOCAL_PANE_WORD_PART, 100, 20, 20, vocalWordPartInput,
				LabelPosition.LEFT);
		vocalWordPart.setLocation(10, selectionEditor.sizes.getY(row++));
		selectionEditor.add(vocalWordPart);

		final JCheckBox vocalPhraseEndInput = new JCheckBox();
		vocalPhraseEndInput.addActionListener(a -> changePhraseEnd(vocalPhraseEndInput.isSelected()));
		vocalPhraseEnd = new FieldWithLabel<>(Label.VOCAL_PANE_PHRASE_END, 100, 20, 20, vocalPhraseEndInput,
				LabelPosition.LEFT);
		vocalPhraseEnd.setLocation(10, selectionEditor.sizes.getY(row++));
		selectionEditor.add(vocalPhraseEnd);

		hideFields();
	}

	private void changeText(final String newText) {
		undoSystem.addUndo();

		for (final Selection<Vocal> selection : selectionManager.getSelectedVocals()) {
			selection.selectable.text(newText);
		}
	}

	private void setFlag(final VocalFlag flag) {
		for (final Selection<Vocal> selection : selectionManager.getSelectedVocals()) {
			selection.selectable.flag(flag);
		}
	}

	private void changeWordPart(final boolean newWordPart) {
		undoSystem.addUndo();
		if (newWordPart) {
			vocalPhraseEnd.field.setEnabled(false);
			vocalPhraseEnd.field.setSelected(false);
			setFlag(VocalFlag.WORD_PART);
		} else {
			vocalPhraseEnd.field.setEnabled(true);
			setFlag(VocalFlag.NONE);
		}
	}

	private void changePhraseEnd(final boolean newPhraseEnd) {
		undoSystem.addUndo();
		if (newPhraseEnd) {
			vocalWordPart.field.setEnabled(false);
			vocalWordPart.field.setSelected(false);
			setFlag(VocalFlag.PHRASE_END);
		} else {
			vocalWordPart.field.setEnabled(true);
			setFlag(VocalFlag.NONE);
		}
	}

	public void showFields() {
		vocalWordPart.setVisible(true);
		vocalPhraseEnd.setVisible(true);
	}

	public void hideFields() {
		vocalText.setVisible(false);
		vocalWordPart.setVisible(false);
		vocalPhraseEnd.setVisible(false);
	}

	public void selectionChanged(final ISelectionAccessor<Vocal> selectedVocalsAccessor) {
		final List<Vocal> selectedVocals = selectedVocalsAccessor.getSelectedElements();
		if (selectedVocals.size() == 1) {
			final String text = selectedVocals.get(0).text();
			vocalText.field.setTextWithoutEvent(text);
			vocalText.setVisible(true);
		} else {
			vocalText.setVisible(false);
		}

		final VocalFlag flag = getSingleValue(selectedVocals, vocal -> vocal.flag(), VocalFlag.NONE);

		vocalWordPart.field.setEnabled(flag != VocalFlag.PHRASE_END);
		vocalWordPart.field.setSelected(flag == VocalFlag.WORD_PART);

		vocalPhraseEnd.field.setEnabled(flag != VocalFlag.WORD_PART);
		vocalPhraseEnd.field.setSelected(flag == VocalFlag.PHRASE_END);
	}
}
