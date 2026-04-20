package log.charter.gui.panes.shortcuts;

import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.components.utils.ComponentUtils.setComponentBounds;

import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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
import log.charter.gui.components.utils.ComponentUtils;
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
		super(frame, Label.SHORTCUT_CONFIG_PANE, new PaneSizesBuilder(inputSize * 21).build());
		this.charterMenuBar = charterMenuBar;

		setDefaultShortcutsButton = new JButton(Label.SHORTCUTS_SET_CHARTER_DEFAULT.label());
		ComponentUtils.setDefaultFontSize(setDefaultShortcutsButton);
		setDefaultShortcutsButton.addActionListener(e -> setDefaultShortcuts());

		setEoFShortcutsButton = new JButton(Label.SHORTCUTS_SET_EOF_DEFAULT.label());
		ComponentUtils.setDefaultFontSize(setEoFShortcutsButton);
		setEoFShortcutsButton.addActionListener(e -> setDefaultEoFShortcuts());

		setCurrentShortcutsButton = new JButton(Label.SHORTCUTS_SET_CURRENT.label());
		ComponentUtils.setDefaultFontSize(setCurrentShortcutsButton);
		setCurrentShortcutsButton.addActionListener(e -> setCurrentShortcuts());

		add(setDefaultShortcutsButton, 0, 0, inputSize * 5, inputSize);
		add(setEoFShortcutsButton, inputSize * 11 / 2, 0, inputSize * 5, inputSize);
		add(setCurrentShortcutsButton, inputSize * 11, 0, inputSize * 5, inputSize);

		panel = makePanel();
		populatePanel();

		this.add(panel);

		this.setOnFinish(this::validateSaveAndExit, null);

		addDefaultFinish(20, false);
		saveButton = (JButton) getPart(-2);
		cancelButton = (JButton) getPart(-1);

		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final int width = min(getWidth(), gd.getDisplayMode().getWidth() * 49 / 50);
		final int height = min(getHeight(), gd.getDisplayMode().getHeight() * 49 / 50);
		setSize(width, height);
		resize();

		setResizable(true);
		addComponentListener(this);
		setVisible(true);
	}

	private ScrollableRowedPanel makePanel() {
		final int rows = ShortcutConfigGroup.groups.stream()
				.collect(Collectors.summingInt(group -> 1 + group.actions.size()));
		final ScrollableRowedPanel panel = new ScrollableRowedPanel(inputSize * 20, rows);
		panel.getVerticalScrollBar().setUnitIncrement(20);

		return panel;
	}

	private void addLabel(final Label label, final int row) {
		final JLabel groupLabel = new JLabel(label.label(), JLabel.CENTER);
		groupLabel.setFont(new Font(Font.DIALOG, Font.BOLD, inputSize));
		panel.addWithYOffset(groupLabel, inputSize, row, -inputSize / 4, inputSize * 18, inputSize * 3 / 2);
	}

	private void addEditFor(final Action action, final int row) {
		final ShortcutEditor editor = new ShortcutEditor(this, action);
		final FieldWithLabel<ShortcutEditor> fieldWithLabel = new FieldWithLabel<>(action.label, inputSize * 10,
				inputSize * 15 / 2, inputSize, editor, LabelPosition.LEFT);
		panel.add(fieldWithLabel, inputSize, row, inputSize * 18, inputSize);

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
		setComponentBounds(setDefaultShortcutsButton, middleX - inputSize * 17 / 2, defaultButtonsTop, inputSize * 5,
				inputSize);
		setComponentBounds(setEoFShortcutsButton, middleX - inputSize * 5 / 2, defaultButtonsTop, inputSize * 5,
				inputSize);
		setComponentBounds(setCurrentShortcutsButton, middleX + inputSize * 7 / 2, defaultButtonsTop, inputSize * 5,
				inputSize);

		final int h = getHeight() - insets.top - insets.bottom;
		final int scrollTop = sizes.getY(2);
		final int scrollBottom = h - inputSize * 5 / 2;

		if (w > inputSize * 21) {
			setComponentBounds(panel, middleX - inputSize * 10, scrollTop, inputSize * 21, scrollBottom - scrollTop);
		} else {
			setComponentBounds(panel, 0, scrollTop, w, scrollBottom - scrollTop);
		}

		final int finishButtonsTop = scrollBottom + inputSize;
		setComponentBounds(saveButton, middleX - inputSize * 11 / 2, finishButtonsTop, inputSize * 5, inputSize);
		setComponentBounds(cancelButton, middleX + inputSize / 2, finishButtonsTop, inputSize * 5, inputSize);
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
