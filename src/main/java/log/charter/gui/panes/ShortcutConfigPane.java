package log.charter.gui.panes;

import static java.util.Arrays.asList;
import static log.charter.gui.components.utils.ComponentUtils.setComponentBounds;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.containers.ScrollableRowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.ShortcutEditor;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.services.Action;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;
import log.charter.util.collections.Pair;

public final class ShortcutConfigPane extends ParamsPane implements ComponentListener {
	private static final long serialVersionUID = -3193534671039163160L;

	private static final List<Pair<Label, List<Action>>> actionGroups = new ArrayList<>();

	static {
		actionGroups.add(new Pair<>(Label.TIME_MOVEMENT, asList(//
				Action.PLAY_AUDIO, //
				Action.MOVE_BACKWARD, //
				Action.MOVE_FORWARD, //
				Action.FAST_BACKWARD, //
				Action.FAST_FORWARD, //
				Action.SLOW_BACKWARD, //
				Action.SLOW_FORWARD, //
				Action.MOVE_TO_START, //
				Action.MOVE_TO_END, //
				Action.MOVE_TO_FIRST_ITEM, //
				Action.MOVE_TO_LAST_ITEM, //
				Action.NEXT_ITEM, //
				Action.PREVIOUS_ITEM, //
				Action.NEXT_ITEM_WITH_SELECT, //
				Action.PREVIOUS_ITEM_WITH_SELECT, //
				Action.NEXT_GRID, //
				Action.PREVIOUS_GRID, //
				Action.NEXT_BEAT, //
				Action.PREVIOUS_BEAT, //
				Action.SPEED_DECREASE, //
				Action.SPEED_DECREASE_FAST, //
				Action.SPEED_DECREASE_PRECISE, //
				Action.SPEED_INCREASE, //
				Action.SPEED_INCREASE_FAST, //
				Action.SPEED_INCREASE_PRECISE, //
				Action.TOGGLE_REPEATER, //
				Action.TOGGLE_REPEAT_START, //
				Action.TOGGLE_REPEAT_END)));
		actionGroups.add(new Pair<>(Label.EDITING, asList(//
				Action.COPY, //
				Action.PASTE, //
				Action.SPECIAL_PASTE, //
				Action.DELETE, //
				Action.UNDO, //
				Action.REDO, //
				Action.SELECT_ALL_NOTES, //
				Action.REDO)));
		actionGroups.add(new Pair<>(Label.VOCAL_EDITING, asList(//
				Action.EDIT_VOCALS, //
				Action.TOGGLE_PHRASE_END, //
				Action.TOGGLE_WORD_PART)));
		actionGroups.add(new Pair<>(Label.GUITAR_EDITING, asList(//
				Action.MOVE_STRING_UP, //
				Action.MOVE_STRING_DOWN, //
				Action.MOVE_STRING_UP_SIMPLE, //
				Action.MOVE_STRING_DOWN_SIMPLE, //
				Action.MOVE_FRET_UP, //
				Action.MOVE_FRET_DOWN, //
				Action.MARK_HAND_SHAPE, //
				Action.DOUBLE_GRID, //
				Action.HALVE_GRID, //
				Action.SNAP_SELECTED, //
				Action.SNAP_ALL, //
				Action.TOGGLE_ACCENT, //
				Action.TOGGLE_ACCENT_INDEPENDENTLY, //
				Action.TOGGLE_HARMONIC, //
				Action.TOGGLE_HARMONIC_INDEPENDENTLY, //
				Action.TOGGLE_HOPO, //
				Action.TOGGLE_HOPO_INDEPENDENTLY, //
				Action.TOGGLE_LINK_NEXT, //
				Action.TOGGLE_LINK_NEXT_INDEPENDENTLY, //
				Action.TOGGLE_MUTE, //
				Action.TOGGLE_MUTE_INDEPENDENTLY, //
				Action.TOGGLE_TREMOLO, //
				Action.TOGGLE_TREMOLO_INDEPENDENTLY, //
				Action.TOGGLE_VIBRATO, //
				Action.TOGGLE_VIBRATO_INDEPENDENTLY)));
		actionGroups.add(new Pair<>(Label.OTHER, asList(//
				Action.NEW_PROJECT, //
				Action.OPEN_PROJECT, //
				Action.SAVE, //
				Action.SAVE_AS, //
				Action.TOGGLE_PREVIEW_WINDOW, //
				Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW, //
				Action.TOGGLE_MIDI, //
				Action.TOGGLE_CLAPS, //
				Action.TOGGLE_METRONOME, //
				Action.TOGGLE_WAVEFORM_GRAPH, //
				Action.EXIT)));
	}

	private final CharterMenuBar charterMenuBar;

	private final ScrollableRowedPanel panel;
	private final Map<Action, ShortcutEditor> editors = new HashMap<>();

	private final JButton saveButton;
	private final JButton cancelButton;

	public ShortcutConfigPane(final CharterMenuBar charterMenuBar, final CharterFrame frame) {
		super(frame, Label.SHORTCUT_CONFIG_PANE, 420);
		this.charterMenuBar = charterMenuBar;

		int row = 0;
		panel = makePanel();
		for (final Pair<Label, List<Action>> group : actionGroups) {
			addLabel(group.a, row++);
			for (final Action action : group.b) {
				addEditFor(action, row++);
			}
		}

		this.add(panel);

		addDefaultFinish(10, this.getDefaultAction(this::validateSaveAndExit), getDefaultAction(), false);
		saveButton = (JButton) getPart(getPartsSize() - 2);
		cancelButton = (JButton) getLastPart();

		resize();
		addComponentListener(this);
		setVisible(true);
	}

	private ScrollableRowedPanel makePanel() {
		final int rows = actionGroups.stream().collect(Collectors.summingInt(group -> 1 + group.b.size()));
		return new ScrollableRowedPanel(400, rows);
	}

	private void addLabel(final Label label, final int row) {
		final JLabel groupLabel = new JLabel(label.label(), JLabel.CENTER);
		groupLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		panel.add(groupLabel, 20, row, 360, 20);
	}

	private void addEditFor(final Action action, final int row) {
		final ShortcutEditor editor = new ShortcutEditor(action);
		final FieldWithLabel<ShortcutEditor> fieldWithLabel = new FieldWithLabel<>(action.label, 200, 150, 20, editor,
				LabelPosition.LEFT);
		panel.add(fieldWithLabel, 20, row, 360, 20);

		editors.put(action, fieldWithLabel.field);
	}

	private boolean validateShortcuts() {
		// TODO check if no keybinds repeat in given groups
		return true;
	}

	private boolean validateSaveAndExit() {
		if (!validateShortcuts()) {
			return false;
		}

		editors.forEach((action, editor) -> ShortcutConfig.setShortcut(action, editor.shortcut));
		ShortcutConfig.markChanged();
		charterMenuBar.refreshMenus();

		return true;
	}

	private void resize() {
		final Insets insets = getInsets();
		final int w = getWidth() - insets.left - insets.right;
		final int middleX = w / 2;
		final int scrollEndY = getHeight() - 50 - insets.top - insets.bottom;

		setComponentBounds(panel, 0, 0, w, scrollEndY);
		setComponentBounds(saveButton, middleX - 110, scrollEndY + 20, 100, 20);
		setComponentBounds(cancelButton, middleX + 10, scrollEndY + 20, 100, 20);
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		resize();
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}
}
