package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.validators.ValueValidator;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.HashSet2;

public class VocalSelectionEditor {
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private FieldWithLabel<TextInputWithValidation> vocalText;
	private FieldWithLabel<JCheckBox> vocalWordPart;
	private FieldWithLabel<JCheckBox> vocalPhraseEnd;

	public void init(final CurrentSelectionEditor selectionEditor, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

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

		final SelectionAccessor<Vocal> vocalSelectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.VOCAL);
		for (final Selection<Vocal> anchorSelection : vocalSelectionAccessor.getSelectedSet()) {
			final boolean phraseEnd = anchorSelection.selectable.isPhraseEnd();
			final boolean wordPart = anchorSelection.selectable.isWordPart();
			anchorSelection.selectable.lyric = newText;
			anchorSelection.selectable.setPhraseEnd(phraseEnd);
			anchorSelection.selectable.setWordPart(wordPart);
		}
	}

	private void setWordPart(final boolean newWordPart) {
		final SelectionAccessor<Vocal> vocalSelectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.VOCAL);
		for (final Selection<Vocal> anchorSelection : vocalSelectionAccessor.getSelectedSet()) {
			anchorSelection.selectable.setWordPart(newWordPart);
		}
	}

	private void setPhraseEnd(final boolean newWordPart) {
		final SelectionAccessor<Vocal> vocalSelectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.VOCAL);
		for (final Selection<Vocal> anchorSelection : vocalSelectionAccessor.getSelectedSet()) {
			anchorSelection.selectable.setPhraseEnd(newWordPart);
		}
	}

	private void changeWordPart(final boolean newWordPart) {
		undoSystem.addUndo();
		if (newWordPart) {
			vocalPhraseEnd.field.setEnabled(false);
			vocalPhraseEnd.field.setSelected(false);
			setPhraseEnd(false);
		} else {
			vocalPhraseEnd.field.setEnabled(true);
		}

		setWordPart(newWordPart);
	}

	private void changePhraseEnd(final boolean newPhraseEnd) {
		undoSystem.addUndo();
		if (newPhraseEnd) {
			vocalWordPart.field.setEnabled(false);
			vocalWordPart.field.setSelected(false);
			setWordPart(false);
		} else {
			vocalWordPart.field.setEnabled(true);
		}

		setPhraseEnd(newPhraseEnd);
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

	public void selectionChanged(final SelectionAccessor<Vocal> selectedVocalsAccessor) {
		final HashSet2<Selection<Vocal>> selectedVocals = selectedVocalsAccessor.getSelectedSet();
		if (selectedVocals.size() == 1) {
			final String text = selectedVocals.stream().findFirst().get().selectable.getText();
			vocalText.field.setTextWithoutEvent(text);
			vocalText.setVisible(true);
		} else {
			vocalText.setVisible(false);
		}

		final Boolean wordPart = getSingleValue(selectedVocals, selection -> selection.selectable.isWordPart(), false);
		final Boolean phraseEnd = getSingleValue(selectedVocals, selection -> selection.selectable.isPhraseEnd(),
				false);

		vocalWordPart.field.setEnabled(wordPart || !phraseEnd);
		vocalWordPart.field.setSelected(wordPart);

		vocalPhraseEnd.field.setEnabled(phraseEnd || !wordPart);
		vocalPhraseEnd.field.setSelected(phraseEnd);
	}
}
