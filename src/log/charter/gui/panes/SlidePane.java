package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.GuitarSound;

public class SlidePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 300;

		return sizes;
	}

	private final UndoSystem undoSystem;

	private final GuitarSound slideable;

	private Integer slideTo;
	private boolean unpitched;

	public SlidePane(final CharterFrame frame, final UndoSystem undoSystem, final ChordOrNote chordOrNote) {
		super(frame, Label.SLIDE_PANE.label(), 4, getSizes());
		this.undoSystem = undoSystem;

		slideable = chordOrNote.asGuitarSound();

		slideTo = slideable.slideTo;
		unpitched = slideable.unpitchedSlide;

		addIntegerConfigValue(0, 20, 0, Label.SLIDE_PANE_FRET, slideTo, 50, createIntValidator(1, 100, true),
				val -> slideTo = val, false);
		final JTextField input = (JTextField) components.getLast();
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);
		addConfigCheckbox(1, Label.SLIDE_PANE_UNPITCHED, unpitched, val -> unpitched = val);

		addDefaultFinish(3, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		slideable.slideTo = slideTo;
		slideable.unpitchedSlide = unpitched;
	}
}
