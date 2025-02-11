package log.charter.gui.panes.shortcuts;

import static log.charter.gui.components.utils.ComponentUtils.setComponentBounds;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.containers.ScrollableRowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.ShortcutEditor;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.services.Action;
import log.charter.services.mouseAndKeyboard.Shortcut;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;
import log.charter.services.mouseAndKeyboard.shortcuts.DefaultEofShortcuts;
import log.charter.services.mouseAndKeyboard.shortcuts.DefaultShortcuts;
import log.charter.services.mouseAndKeyboard.shortcuts.ShortcutList;

public final class ShortcutConfigPane extends ParamsPane implements ComponentListener {
	private static final long serialVersionUID = -3193534671039163160L;

	private final CharterMenuBar charterMenuBar;

	private final JButton setDefaultShortcutsButton;
	private final JButton setEoFShortcutsButton;
	private final JButton setCurrentShortcutsButton;

	private boolean setDefaultShortcuts = false;
	private boolean setEoFShortcuts = false;

	private final ScrollableRowedPanel panel;
	private final Map<Action, ShortcutEditor> editors = new HashMap<>();

	private final JButton saveButton;
	private final JButton cancelButton;

	public ShortcutConfigPane(final CharterMenuBar charterMenuBar, final CharterFrame frame) {
		super(frame, Label.SHORTCUT_CONFIG_PANE, new PaneSizesBuilder(420).rowHeight(23).build());
		this.charterMenuBar = charterMenuBar;

		setDefaultShortcutsButton = new JButton(Label.SHORTCUTS_SET_CHARTER_DEFAULT.label());
		setDefaultShortcutsButton.addActionListener(e -> setDefaultShortcuts());
		setEoFShortcutsButton = new JButton(Label.SHORTCUTS_SET_EOF_DEFAULT.label());
		setEoFShortcutsButton.addActionListener(e -> setDefaultEoFShortcuts());
		setCurrentShortcutsButton = new JButton(Label.SHORTCUTS_SET_CURRENT.label());
		setCurrentShortcutsButton.addActionListener(e -> setCurrentShortcuts());

		add(setDefaultShortcutsButton, 0, 0, 100, 20);
		add(setEoFShortcutsButton, 110, 0, 100, 20);
		add(setCurrentShortcutsButton, 220, 0, 100, 20);

		panel = makePanel();
		populatePanel();

		this.add(panel);

		this.setOnFinish(this::validateSaveAndExit, null);

		addDefaultFinish(20, false);
		saveButton = (JButton) getPart(-2);
		cancelButton = (JButton) getPart(-1);

		resize();

		setResizable(true);
		addComponentListener(this);
		setVisible(true);
	}

	private ScrollableRowedPanel makePanel() {
		final int rows = ShortcutConfigGroup.groups.stream()
				.collect(Collectors.summingInt(group -> 1 + group.actions.size()));
		final ScrollableRowedPanel panel = new ScrollableRowedPanel(400, rows);
		panel.getVerticalScrollBar().setUnitIncrement(5);

		return panel;
	}

	private void addLabel(final Label label, final int row) {
		final JLabel groupLabel = new JLabel(label.label(), JLabel.CENTER);
		groupLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		panel.addWithYOffset(groupLabel, 20, row, -5, 360, 30);
	}

	private void addEditFor(final Action action, final int row) {
		final ShortcutEditor editor = new ShortcutEditor(this, action);
		final FieldWithLabel<ShortcutEditor> fieldWithLabel = new FieldWithLabel<>(action.label, 200, 150, 20, editor,
				LabelPosition.LEFT);
		panel.add(fieldWithLabel, 20, row, 360, 20);

		editors.put(action, fieldWithLabel.field);
	}

	private void populatePanel() {
		int row = 0;

		for (final ShortcutConfigGroup group : ShortcutConfigGroup.groups) {
			addLabel(group.label, row++);

			for (final Action action : group.actions) {
				addEditFor(action, row++);
			}
		}
	}

	private void setShortcutsFromList(final ShortcutList list) {
		for (final Entry<Action, ShortcutEditor> editorEntry : editors.entrySet()) {
			editorEntry.getValue().setShortcut(list.get(editorEntry.getKey()));
		}

		validateShortcuts();
	}

	private void setDefaultShortcuts() {
		setDefaultShortcuts = true;
		setEoFShortcuts = false;

		setShortcutsFromList(DefaultShortcuts.instance);
	}

	private void setDefaultEoFShortcuts() {
		setDefaultShortcuts = false;
		setEoFShortcuts = true;

		setShortcutsFromList(DefaultEofShortcuts.instance);
	}

	private void setCurrentShortcuts() {
		setDefaultShortcuts = false;
		setEoFShortcuts = false;

		setShortcutsFromList(ShortcutConfig.shortcuts);
	}

	public boolean validShortcut(final Action action, final Shortcut shortcut) {
		for (final Action otherAction : Action.values()) {
			if (otherAction == action) {
				continue;
			}
			if (!otherAction.editModes.stream().anyMatch(editMode -> action.editModes.contains(editMode))) {
				continue;
			}

			final Shortcut otherShortcut;
			if (editors.containsKey(otherAction)) {
				otherShortcut = editors.get(otherAction).shortcut;
			} else {
				otherShortcut = ShortcutConfig.shortcuts.get(otherAction);
				if (otherShortcut == null) {
					continue;
				}
			}

			if (otherShortcut != null && otherShortcut.equals(shortcut)) {
				return false;
			}
		}

		return true;
	}

	public void validateShortcuts() {
		for (final ShortcutEditor editor : editors.values()) {
			editor.validateShortcut();
		}
	}

	private boolean validateSaveAndExit() {
		for (final ShortcutEditor editor : editors.values()) {
			if (!editor.isValidShortcut()) {
				return false;
			}
		}

		if (setDefaultShortcuts) {
			Config.defaultEofShortcuts = false;
		} else if (setEoFShortcuts) {
			Config.defaultEofShortcuts = true;
		}

		ShortcutConfig.resetDefaultShortcuts();
		editors.forEach((action, editor) -> ShortcutConfig.shortcuts.set(action, editor.shortcut));
		ShortcutConfig.resetEditModeActions();

		ShortcutConfig.markChanged();
		charterMenuBar.refreshMenus();

		return true;
	}

	private void resize() {
		final Insets insets = getInsets();
		final int w = getWidth() - insets.left - insets.right;
		final int middleX = w / 2;

		final int defaultButtonsTop = sizes.getY(1);
		setComponentBounds(setDefaultShortcutsButton, middleX - 170, defaultButtonsTop, 100, 20);
		setComponentBounds(setEoFShortcutsButton, middleX - 50, defaultButtonsTop, 100, 20);
		setComponentBounds(setCurrentShortcutsButton, middleX + 70, defaultButtonsTop, 100, 20);

		final int scrollTop = sizes.getY(2);
		final int scrollBottom = sizes.getY(19);

		if (w > 420) {
			setComponentBounds(panel, middleX - 200, scrollTop, 420, scrollBottom - scrollTop);
		} else {
			setComponentBounds(panel, 0, scrollTop, w, scrollBottom - scrollTop);
		}

		final int finishButtonsTop = sizes.getY(20);
		setComponentBounds(saveButton, middleX - 110, finishButtonsTop, 100, 20);
		setComponentBounds(cancelButton, middleX + 10, finishButtonsTop, 100, 20);
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
