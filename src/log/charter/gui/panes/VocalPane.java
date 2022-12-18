package log.charter.gui.panes;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

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
		super(frame, label.label(), 5);
		this.data = data;
		this.frame = frame;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	private void createElementsAndShow(final ActionListener saveAction) {
		addConfigValue(0, Label.VOCAL_PANE_LYRIC, text, 200, null, val -> text = val, true);
		addConfigCheckbox(1, Label.VOCAL_PANE_WORD_PART, wordPart, val -> {
			wordPart = val;
			components.get(2).setEnabled(!val);
		});
		addConfigCheckbox(2, Label.VOCAL_PANE_PHRASE_END, phraseEnd, val -> {
			phraseEnd = val;
			components.get(1).setEnabled(!val);
		});

		addButtons(4, saveAction);
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(saveAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	public VocalPane(final int position, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this(Label.VOCAL_PANE_CREATION, data, frame, selectionManager, undoSystem);

		text = "";
		wordPart = false;
		phraseEnd = false;

		createElementsAndShow(e -> createAndExit(position));
	}

	public VocalPane(final int id, final Vocal vocal, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final UndoSystem undoSystem,
			final ArrayList2<Selection<Vocal>> remainingVocals) {
		this(Label.VOCAL_PANE_EDIT, data, frame, selectionManager, undoSystem);

		text = vocal.getText();
		wordPart = vocal.isWordPart();
		phraseEnd = vocal.isPhraseEnd();

		createElementsAndShow(e -> saveAndExit(id, vocal, remainingVocals));
	}

	private void createAndExit(final int position) {
		dispose();
		if (text == null || "".equals(text)) {
			return;
		}

		undoSystem.addUndo();
		selectionManager.clear();

		data.songChart.vocals.insertNote(position, text, wordPart, phraseEnd);
	}

	private void saveAndExit(final int id, final Vocal vocal, final ArrayList2<Selection<Vocal>> remainingVocals) {
		dispose();
		if (text == null || "".equals(text)) {
			undoSystem.addUndo();
			data.songChart.vocals.removeNote(id);
			return;
		}

		vocal.lyric = text;
		vocal.setWordPart(wordPart);
		vocal.setPhraseEnd(phraseEnd);

		if (!remainingVocals.isEmpty()) {
			final Selection<Vocal> nextSelectedVocal = remainingVocals.remove(0);
			new VocalPane(nextSelectedVocal.id, nextSelectedVocal.selectable, data, frame, selectionManager, undoSystem,
					remainingVocals);
		}
	}

}
