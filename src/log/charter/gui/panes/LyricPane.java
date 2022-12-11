package log.charter.gui.panes;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.data.ChartData;
import log.charter.data.IdOrPos;
import log.charter.gui.CharterFrame;
import log.charter.song.Vocal;

public class LyricPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private String text;
	private boolean wordPart;
	private boolean phraseEnd;

	private final ChartData data;

	public LyricPane(final CharterFrame frame, final ChartData data, final IdOrPos idOrPos) {
		super(frame, "Lyrics edit", 5);
		this.data = data;

		final Vocal vocal;
		if (idOrPos.isId()) {
			vocal = data.songChart.vocals.vocals.get(idOrPos.id);
			text = vocal.getText();
			wordPart = vocal.isWordPart();
			phraseEnd = vocal.isPhraseEnd();
		} else {
			vocal = null;
			text = "";
			wordPart = false;
			phraseEnd = false;
		}

		addConfigValue(0, "Lyric", text, 200, null, val -> text = val, true);
		addConfigCheckbox(1, "Word part", wordPart, val -> {
			wordPart = val;
			components.get(2).setEnabled(!val);
		});
		addConfigCheckbox(2, "Phrase end", phraseEnd, val -> {
			phraseEnd = val;
			components.get(1).setEnabled(!val);
		});

		addButtons(4, e -> {
			saveAndExit(idOrPos, vocal);
		});
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> saveAndExit(idOrPos, vocal),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	private void saveAndExit(final IdOrPos idOrPos, final Vocal vocal) {
		if (!wordPart && (text != null) && text.endsWith("-")) {
			text = text.substring(0, text.length() - 1);
			wordPart = true;
		}
		if (!phraseEnd && (text != null) && text.endsWith("+")) {
			text = text.substring(0, text.length() - 1);
			phraseEnd = true;
		}

		if (idOrPos.isId()) {
			if ("".equals(text)) {
				data.removeVocalNote(idOrPos.id);
			} else {
				vocal.lyric = text;
				vocal.setWordPart(wordPart);
				vocal.setPhraseEnd(phraseEnd);
			}
		} else {
			if (!"".equals(text) && (text != null)) {
				data.addVocalNote(idOrPos.pos, text, wordPart, phraseEnd);
			}
		}

		dispose();
	}

}
