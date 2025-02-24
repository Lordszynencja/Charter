package log.charter.gui.components.containers;

import static log.charter.gui.components.containers.SaverWithStatus.emptySaver;

import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
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
		final int width = panel.getWidth() + insets.left + insets.right;
		final int height = panel.getHeight() + insets.top + insets.bottom;
		setSize(width, height);
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
		final int x0 = center - 110;
		final int x1 = center + 10;

		addDefaultButton(x0, y, Label.BUTTON_SAVE, onSave);
		addDefaultButton(x1, y, Label.BUTTON_CANCEL, onCancel);
	}

	private void addDefaultButton(final int x, final int y, final Label label, final Disposer onClick) {
		final JButton button = new JButton(label.label());
		button.addActionListener(e -> onClick.fire());
		panel.addWithSettingSize(button, x, y, 100, 20);
	}

	private void addDefaultKeybinds(final Disposer onSave, final Disposer onCancel) {
		getRootPane().registerKeyboardAction(e -> onSave.fire(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> onCancel.fire(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void setLocation() {
		final int x = Config.window.x + frame.getWidth() / 2 - getWidth() / 2;
		final int y = Config.window.y + frame.getHeight() / 2 - getHeight() / 2;
		setLocation(x, y);
	}

	protected void finishInit() {
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
