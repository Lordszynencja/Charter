package log.charter.gui.panes.songEdits;

import java.util.List;

import javax.swing.JCheckBox;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;

public class VocalPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private String text;
	private boolean wordPart;
	private boolean phraseEnd;

	private final ChartData data;
	private final CharterFrame frame;
	private final SelectionManager selectionManager;
	private final UndoSystem undoSystem;

	private VocalPane(final Label label, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		super(frame, label, 360);
		this.data = data;
		this.frame = frame;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public VocalPane(final int position, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this(Label.VOCAL_PANE_CREATION, data, frame, selectionManager, undoSystem);

		text = "";
		wordPart = false;
		phraseEnd = false;

		createElementsAndShow(() -> createAndExit(position));
	}

	public VocalPane(final int id, final Vocal vocal, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this(Label.VOCAL_PANE_EDIT, data, frame, selectionManager, undoSystem);

		text = vocal.getText();
		wordPart = vocal.isWordPart();
		phraseEnd = vocal.isPhraseEnd();

		createElementsAndShow(() -> saveAndExit(id, vocal));
	}

	public VocalPane(final int id, final Vocal vocal, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final UndoSystem undoSystem,
			final List<Selection<Vocal>> remainingVocals) {
		this(Label.VOCAL_PANE_EDIT, data, frame, selectionManager, undoSystem);

		text = vocal.getText();
		wordPart = vocal.isWordPart();
		phraseEnd = vocal.isPhraseEnd();

		createElementsAndShow(() -> saveAndExit(id, vocal, remainingVocals));
	}

	private void createElementsAndShow(final Runnable onSave) {
		addStringConfigValue(0, 20, 70, Label.VOCAL_PANE_LYRIC, text, 200, null, val -> text = val, true);
		addConfigCheckbox(1, 20, 70, Label.VOCAL_PANE_WORD_PART, wordPart, val -> {
			wordPart = val;

			phraseEnd = false;
			final JCheckBox phraseEndCheckbox = (JCheckBox) getPart(5);
			phraseEndCheckbox.setSelected(false);
			phraseEndCheckbox.setEnabled(!val);
		});
		addConfigCheckbox(2, 20, 70, Label.VOCAL_PANE_PHRASE_END, phraseEnd, val -> {
			phraseEnd = val;

			wordPart = false;
			final JCheckBox wordPartCheckbox = (JCheckBox) getPart(3);
			wordPartCheckbox.setSelected(false);
			wordPartCheckbox.setEnabled(!val);
		});

		addDefaultFinish(4, onSave);
	}

	private void createAndExit(final int position) {
		if (text == null || "".equals(text)) {
			return;
		}

		undoSystem.addUndo();
		selectionManager.clear();

		final int vocalId = data.songChart.vocals.insertNote(position, text, wordPart, phraseEnd);
		selectionManager.addSelection(PositionType.VOCAL, vocalId);
	}

	private void saveAndExit(final int id, final Vocal vocal) {
		if (text == null || "".equals(text)) {
			undoSystem.addUndo();
			data.songChart.vocals.removeNote(id);
			return;
		}

		if (vocal.getText() != text || vocal.isWordPart() != wordPart || vocal.isPhraseEnd() != phraseEnd) {
			undoSystem.addUndo();
			vocal.lyric = text;
			vocal.setWordPart(wordPart);
			vocal.setPhraseEnd(phraseEnd);
		}
	}

	private void saveAndExit(final int id, final Vocal vocal, final List<Selection<Vocal>> remainingVocals) {
		if (text == null || "".equals(text)) {
			undoSystem.addUndo();
			data.songChart.vocals.removeNote(id);
			return;
		}

		if (vocal.getText() != text || vocal.isWordPart() != wordPart || vocal.isPhraseEnd() != phraseEnd) {
			undoSystem.addUndo();
			vocal.lyric = text;
			vocal.setWordPart(wordPart);
			vocal.setPhraseEnd(phraseEnd);
		}

		if (!remainingVocals.isEmpty()) {
			final Selection<Vocal> nextSelectedVocal = remainingVocals.remove(0);
			new VocalPane(nextSelectedVocal.id, nextSelectedVocal.selectable, data, frame, selectionManager, undoSystem,
					remainingVocals);
		}
	}

}
