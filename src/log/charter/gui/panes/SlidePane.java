package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.util.Slideable;

public class SlidePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 300;

		return sizes;
	}

	private final UndoSystem undoSystem;

	private final Slideable slideable;

	private Integer slideTo;
	private boolean unpitched;

	public SlidePane(final CharterFrame frame, final UndoSystem undoSystem, final ChordOrNote chordOrNote) {
		super(frame, Label.SLIDE_PANE.label(), 4, getSizes());
		this.undoSystem = undoSystem;

		slideable = chordOrNote.chord != null ? chordOrNote.chord : chordOrNote.note;

		slideTo = slideable.slideTo();
		unpitched = slideable.unpitched();

		addIntegerConfigValue(0, 20, 0, Label.SLIDE_PANE_FRET, slideTo, 50, createIntValidator(1, 100, true),
				val -> slideTo = val, false);
		addConfigCheckbox(1, Label.SLIDE_PANE_UNPITCHED, unpitched, val -> unpitched = val);

		addButtons(3, this::saveAndExit);
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> saveAndExit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	private void saveAndExit() {
		undoSystem.addUndo();
		slideable.setSlide(slideTo, unpitched);

		dispose();
	}
}
