package log.charter.gui.panes;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.data.ChartData;
import log.charter.data.managers.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.song.Vocal;

public class LyricPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private String text;
	private boolean wordPart;
	private boolean phraseEnd;

	private final ChartData data;
	private final SelectionManager selectionManager;
	private final UndoSystem undoSystem;

	private LyricPane(final String name, final CharterFrame frame, final ChartData data,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		super(frame, name, 5);
		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	private void createElementsAndShow(final ActionListener saveAction) {
		addConfigValue(0, "Lyric", text, 200, null, val -> text = val, true);
		addConfigCheckbox(1, "Word part", wordPart, val -> {
			wordPart = val;
			components.get(2).setEnabled(!val);
		});
		addConfigCheckbox(2, "Phrase end", phraseEnd, val -> {
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

	public LyricPane(final int position, final CharterFrame frame, final ChartData data,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this("Vocal creation", frame, data, selectionManager, undoSystem);

		text = "";
		wordPart = false;
		phraseEnd = false;

		createElementsAndShow(e -> createAndExit(position));
	}

	public LyricPane(final int id, final Vocal vocal, final CharterFrame frame, final ChartData data,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this("Vocal edit", frame, data, selectionManager, undoSystem);

		text = vocal.getText();
		wordPart = vocal.isWordPart();
		phraseEnd = vocal.isPhraseEnd();

		createElementsAndShow(e -> saveAndExit(id, vocal));
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

	private void saveAndExit(final int id, final Vocal vocal) {
		dispose();
		if (text == null || "".equals(text)) {
			undoSystem.addUndo();
			data.songChart.vocals.removeNote(id);
			return;
		}

		vocal.lyric = text;
		vocal.setWordPart(wordPart);
		vocal.setPhraseEnd(phraseEnd);
	}

}
