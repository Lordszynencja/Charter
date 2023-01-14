package log.charter.gui.menuHandlers;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.KeyboardHandler;

class GuitarMenuHandler extends CharterMenuHandler {
	private ChartData data;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;

	public void init(final ChartData data, final KeyboardHandler keyboardHandler, final ModeManager modeManager) {
		this.data = data;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty && modeManager.editMode == EditMode.GUITAR;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.GUITAR_MENU.label());

		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_UP, "Up", keyboardHandler::moveNotesUpKeepFrets));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_DOWN, "Down", keyboardHandler::moveNotesDownKeepFrets));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_UP_KEEP_FRETS, "Ctrl-Up", keyboardHandler::moveNotesUp));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_DOWN_KEEP_FRETS, "Ctrl-Down",
				keyboardHandler::moveNotesDown));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_MUTES, "M", keyboardHandler::toggleMute));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_HOPO, "H", keyboardHandler::toggleHOPO));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_HARMONIC, "O", keyboardHandler::toggleHarmonic));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_SET_SLIDE, "S", keyboardHandler::editSlide));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_ACCENT, "A", keyboardHandler::toggleAccent));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_VIBRATO, "V", keyboardHandler::toggleVibrato));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_TREMOLO, "T", keyboardHandler::toggleTremolo));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_LINK_NEXT, "L", keyboardHandler::toggleLinkNext));

		menu.addSeparator();
		final JMenuItem noteOptions = new SpecialMenuItem(Label.GUITAR_MENU_NOTE_OPTIONS, "W",
				keyboardHandler::editNote);
		noteOptions.setToolTipText(Label.GUITAR_MENU_NOTE_OPTIONS_TOOLTIP.label());
		menu.add(noteOptions);
		final JMenuItem chordOptions = new SpecialMenuItem(Label.GUITAR_MENU_CHORD_OPTIONS, "Q",
				keyboardHandler::editNoteAsChord);
		chordOptions.setToolTipText(Label.GUITAR_MENU_CHORD_OPTIONS_TOOLTIP.label());
		menu.add(chordOptions);
		final JMenuItem singleNoteOptions = new SpecialMenuItem(Label.GUITAR_MENU_SINGLE_NOTE_OPTIONS, "E",
				keyboardHandler::editNoteAsSingleNote);
		singleNoteOptions.setToolTipText(Label.GUITAR_MENU_SINGLE_NOTE_OPTIONS_TOOLTIP.label());
		menu.add(singleNoteOptions);
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_BEND_SETTINGS, "B", keyboardHandler::editBend));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_MARK_HAND_SHAPE, "Shift-H", keyboardHandler::markHandShape));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_HAND_SHAPE_OPTIONS, "Ctrl-H", keyboardHandler::editHandShape));

		return menu;
	}
}
