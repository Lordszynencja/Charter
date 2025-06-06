package log.charter.gui.components.simple;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import log.charter.data.config.ChartPanelColors.ColorLabel;
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

	public static <T> T load(final CharterFrame charterFrame, final int steps,
			final Function<LoadingDialog, T> operation, final String operationName) {
		final LoadingDialog dialog = new LoadingDialog(charterFrame, steps);

		final SwingWorker<T, Void> mySwingWorker = new SwingWorker<T, Void>() {
			@Override
			protected T doInBackground() throws Exception {
				T result = null;
				try {
					result = operation.apply(dialog);
				} catch (final Throwable t) {
					Logger.error("error when executing operation " + operationName, t);
				}

				dialog.dispose();
				return result;
			}
		};

		mySwingWorker.execute();
		dialog.setVisible(true);
		while (mySwingWorker.getState() != StateValue.DONE) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			return mySwingWorker.get();
		} catch (InterruptedException | ExecutionException e) {
			return null;
		}
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
		progressBar.setBackground(ColorLabel.BASE_BG_2.color());
		progressBar.setBounds(50, 100, getWidth() - 100, 30);
		add(progressBar);

		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	public void setProgress(final int progress, final Label label) {
		setProgress(progress, label.label());
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