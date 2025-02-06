package log.charter.gui.components.simple;

import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;

public class LoadingDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public static void doWithLoadingDialog(final CharterFrame charterFrame, final int steps,
			final Consumer<LoadingDialog> operation, final String operationName) {
		final LoadingDialog dialog = new LoadingDialog(charterFrame, steps);

		final SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					operation.accept(dialog);
				} catch (final Throwable t) {
					Logger.error("error when executing operation " + operationName, t);
				}

				dialog.dispose();
				return null;
			}
		};

		mySwingWorker.execute();
		dialog.setVisible(true);
	}

	private final JLabel text;
	private final JProgressBar progressBar;

	public LoadingDialog(final CharterFrame frame, final int steps) {
		super(frame, Label.LOADING.label());
		setLayout(null);
		setSize(300, 200);
		setLocation(frame.getX() + frame.getWidth() / 2 - getWidth() / 2,
				frame.getY() + frame.getHeight() / 2 - getHeight() / 2);

		text = new JLabel(Label.LOADING.label());
		text.setHorizontalAlignment(JLabel.CENTER);
		text.setVerticalAlignment(JLabel.CENTER);
		text.setBounds(0, 30, 300, 60);
		add(text);

		progressBar = new JProgressBar(0, steps);
		progressBar.setBounds(50, 100, getWidth() - 100, 30);
		add(progressBar);

		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	public void setProgress(final int progress, final String description) {
		progressBar.setValue(progress);
		text.setText(description);
		repaint();
	}

	public void changeMaxProgress(final int newMaxProgress) {
		progressBar.setMaximum(newMaxProgress);
	}

	public void addProgress(final Label label) {
		setProgress(progressBar.getValue() + 1, label.label());
	}
}