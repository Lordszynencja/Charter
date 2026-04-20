package log.charter.gui.components.containers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.components.containers.SaverWithStatus.emptySaver;
import static log.charter.gui.components.utils.ComponentUtils.setDefaultFontSize;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.WindowStateConfig;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.PaneSizes;
import log.charter.gui.components.utils.PaneSizesBuilder;

public class RowedDialog extends JDialog implements ComponentListener {
	private static final long serialVersionUID = -3193534671039163160L;

	private class Disposer {
		private final SaverWithStatus action;

		public Disposer(final SaverWithStatus action) {
			this.action = action;
		}

		public void fire() {
			if (action.save()) {
				dispose();
			}
		}
	}

	protected final CharterFrame frame;
	protected final RowedPanel panel;

	public RowedDialog(final CharterFrame frame, final Label title) {
		this(frame, title, 250);
	}

	public RowedDialog(final CharterFrame frame, final Label title, final int width) {
		this(frame, title, new PaneSizesBuilder(width).build());
	}

	public RowedDialog(final CharterFrame frame, final Label title, final PaneSizes sizes) {
		super(frame, title.label(), true);

		this.frame = frame;
		panel = new RowedPanel(sizes);
		this.add(panel);

		pack();

		setSizeWithInsets();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		setLayout(null);

		addComponentListener(this);
	}

	private void setSizeWithInsets() {
		final Insets insets = getInsets();
		final Dimension minPanelSize = panel.getMinimumSize();
		final int minimumWidth = minPanelSize.width + insets.left + insets.right;
		final int minimumHeight = minPanelSize.height + insets.top + insets.bottom;
		final int width = max(minimumWidth, getWidth());
		final int height = max(minimumHeight, getHeight());

		final int panelWidth = width - insets.left - insets.right;
		final int panelHeight = height - insets.top - insets.bottom;
		panel.setSize(panelWidth, panelHeight);
	}

	protected void addDefaultFinish(final int y, final SaverWithStatus onSave, final SaverWithStatus onCancel,
			final boolean finishInit) {
		if (onSave == null) {
			addDefaultFinish(y, emptySaver, onCancel, finishInit);
			return;
		}
		if (onCancel == null) {
			addDefaultFinish(y, onSave, emptySaver, finishInit);
			return;
		}

		final Disposer paneOnSave = new Disposer(onSave);
		final Disposer paneOnCancel = new Disposer(onCancel);

		addDefaultButtons(y, paneOnSave, paneOnCancel);
		addDefaultKeybinds(paneOnSave, paneOnCancel);

		if (finishInit) {
			finishInit();
		}
	}

	private void addDefaultButtons(final int y, final Disposer onSave, final Disposer onCancel) {
		final int center = panel.getWidth() / 2;
		final int x0 = max(center - inputSize * 11 / 2, inputSize);
		final int x1 = x0 + inputSize * 6;

		addDefaultButton(x0, y, Label.BUTTON_SAVE, onSave);
		addDefaultButton(x1, y, Label.BUTTON_CANCEL, onCancel);
	}

	private void addDefaultButton(final int x, final int y, final Label label, final Disposer onClick) {
		final JButton button = new JButton(label.label());
		setDefaultFontSize(button);
		button.addActionListener(e -> onClick.fire());
		panel.addWithSettingSize(button, x, y, inputSize * 5, inputSize);
	}

	private void addDefaultKeybinds(final Disposer onSave, final Disposer onCancel) {
		getRootPane().registerKeyboardAction(e -> onSave.fire(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> onCancel.fire(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void setLocation() {
		final DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDisplayMode();
		final int x = min(displayMode.getWidth() * 99 / 100,
				max(displayMode.getWidth() / 100, WindowStateConfig.x + frame.getWidth() / 2 - getWidth() / 2));
		final int y = min(displayMode.getHeight() * 99 / 100,
				max(displayMode.getWidth() / 100, WindowStateConfig.y + frame.getHeight() / 2 - getHeight() / 2));

		setLocation(x, y);
	}

	protected void finishInit() {
		final Insets insets = getInsets();
		final Dimension minPanelSize = panel.getMinimumSize();
		final int minWidth = minPanelSize.width + insets.left + insets.right;
		final int minHeight = minPanelSize.height + insets.top + insets.bottom;
		setMinimumSize(new Dimension(minWidth, minHeight));

		final DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDisplayMode();
		final int maximumWidth = dm.getWidth() * 49 / 50;
		final int maximumHeight = dm.getHeight() * 49 / 50;

		final int w = getWidth();
		final int h = getHeight();
		if (w > maximumWidth || h > maximumHeight) {
			final int width = min(maximumWidth, getWidth());
			final int height = min(maximumHeight, getHeight());
			if (width != w || height != h) {
				// setSize(width, height);
			}
		}
		setSizeWithInsets();
		setLocation();
		validate();
		setVisible(true);
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		setSizeWithInsets();
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
