package log.charter.gui.panes;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.util.Slideable;

public class SlidePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final UndoSystem undoSystem;

	private final Slideable slideable;

	private Integer fret;
	private boolean pitched;

	public SlidePane(final CharterFrame frame, final UndoSystem undoSystem, final ChordOrNote chordOrNote) {
		super(frame, Label.SLIDE_PANE.label(), 5);
		this.undoSystem = undoSystem;

		slideable = Slideable.create(chordOrNote);
		fret = slideable.fret();
		pitched = slideable.pitched();

		addConfigValue(0, Label.SLIDE_PANE_FRET, fret, 50, createIntValidator(1, 100, true),
				val -> fret = val == null || val.isEmpty() ? null : Integer.valueOf(val), false);
		addConfigCheckbox(1, Label.SLIDE_PANE_PITCHED, pitched, val -> pitched = val);

		addButtons(4, e -> saveAndExit());
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> saveAndExit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	private void saveAndExit() {
		undoSystem.addUndo();
		slideable.set(fret, pitched);

		dispose();
	}
}
