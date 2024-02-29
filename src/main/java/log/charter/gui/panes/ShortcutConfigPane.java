package log.charter.gui.panes;

import static java.util.Arrays.asList;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.components.ShortcutEditor;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.ShortcutConfig;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.util.CollectionUtils.Pair;

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
				Action.NEXT_GRID, //
				Action.PREVIOUS_GRID, //
				Action.NEXT_BEAT, //
				Action.PREVIOUS_BEAT, //
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

	private static final int labelWidth = 200;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.width = 420;

		return sizes;
	}

	private final CharterMenuBar charterMenuBar;

	private final JScrollPane scrollPane;
	private final Map<Action, ShortcutEditor> editors = new HashMap<>();

	private final JButton saveButton;
	private final JButton cancelButton;

	private void setComponentBounds(final Component component, final int x, final int y, final int w, final int h) {
		component.setBounds(x, y, w, h);
		final Dimension size = new Dimension(w, h);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
	}

	public ShortcutConfigPane(final CharterMenuBar charterMenuBar, final CharterFrame frame) {
		super(frame, Label.SHORTCUT_CONFIG_PANE, getSizes());
		this.charterMenuBar = charterMenuBar;

		final JPanel panel = new JPanel(null);
		panel.setOpaque(true);
		int row = 0;
		for (final Pair<Label, List<Action>> group : actionGroups) {
			addLabel(panel, group.a, row++);
			for (final Action action : group.b) {
				addEditFor(panel, action, row++);
			}
		}
		setComponentBounds(panel, 0, 0, 400, getY(row));

		scrollPane = new JScrollPane(panel);
		this.add(scrollPane);

		addDefaultFinish(10, this.getDefaultAction(this::validateSaveAndExit), getDefaultAction(), false);
		saveButton = (JButton) components.get(components.size() - 2);
		cancelButton = (JButton) components.get(components.size() - 1);

		resize();
		addComponentListener(this);
		setVisible(true);
	}

	private void addLabel(final JPanel panel, final Label label, final int row) {
		final JLabel groupLabel = new JLabel(label.label(), JLabel.CENTER);
		groupLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		setComponentBounds(groupLabel, 20, getY(row), 360, 20);
		panel.add(groupLabel);
	}

	private void addEditFor(final JPanel panel, final Action action, final int row) {
		final ShortcutEditor editor = new ShortcutEditor(action);
		final FieldWithLabel<ShortcutEditor> fieldWithLabel = new FieldWithLabel<>(action.label, labelWidth, 150, 20,
				editor, LabelPosition.LEFT);
		setComponentBounds(fieldWithLabel, 20, getY(row), 360, 20);
		panel.add(fieldWithLabel);

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

		editors.forEach((action, editor) -> { ShortcutConfig.setShortcut(action, editor.shortcut); });
		ShortcutConfig.markChanged();
		charterMenuBar.refreshMenus();

		return true;
	}

	private void resize() {
		final Insets insets = getInsets();
		final int w = getWidth() - insets.left - insets.right;
		final int middleX = w / 2;
		final int scrollEndY = getHeight() - 50 - insets.top - insets.bottom;

		setComponentBounds(scrollPane, 0, 0, w, scrollEndY);

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
