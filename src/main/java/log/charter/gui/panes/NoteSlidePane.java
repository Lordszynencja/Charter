package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.notes.Note;

public class NoteSlidePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 250;

		return sizes;
	}

	private final UndoSystem undoSystem;

	private final Note note;

	private Integer slideTo;
	private boolean unpitched;

	public NoteSlidePane(final CharterFrame frame, final UndoSystem undoSystem, final Note note) {
		super(frame, Label.SLIDE_PANE, 4, getSizes());
		this.undoSystem = undoSystem;

		this.note = note;
		slideTo = note.slideTo;
		unpitched = note.unpitchedSlide;

		addIntegerConfigValue(0, 60, 0, Label.SLIDE_PANE_FRET, slideTo, 50, createIntValidator(1, 100, true),
				val -> slideTo = val, false);
		final JTextField input = (JTextField) components.getLast();
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);
		addConfigCheckbox(1, 60, 0, Label.SLIDE_PANE_UNPITCHED, unpitched, val -> unpitched = val);

		addDefaultFinish(3, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		note.slideTo = slideTo;
		note.unpitchedSlide = unpitched;
	}
}
