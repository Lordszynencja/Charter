package log.charter.gui.components.utils;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JOptionPane;

import log.charter.data.config.Localization.Label;

public class ComponentUtils {
	public enum ConfirmAnswer {
		YES, NO, CANCEL, EXIT;

		public static ConfirmAnswer fromJOptionPaneResult(final int result) {
			return switch (result) {
				case JOptionPane.YES_OPTION -> ConfirmAnswer.YES;
				case JOptionPane.NO_OPTION -> ConfirmAnswer.NO;
				case JOptionPane.CANCEL_OPTION -> ConfirmAnswer.NO;
				default -> ConfirmAnswer.EXIT;
			};
		}
	}

	public static void setComponentBounds(final Component component, final int x, final int y, final int w,
			final int h) {
		component.setBounds(x, y, w, h);
		final Dimension size = new Dimension(w, h);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
	}

	public static ConfirmAnswer askYesNo(final Component parent, final Label title, final Label message) {
		final int result = JOptionPane.showConfirmDialog(parent, message.label(), title.label(),
				JOptionPane.YES_NO_OPTION);

		return ConfirmAnswer.fromJOptionPaneResult(result);
	}

	public static ConfirmAnswer askYesNoCancel(final Component parent, final Label title, final Label message) {
		final int result = JOptionPane.showConfirmDialog(parent, message.label(), title.label(),
				JOptionPane.YES_NO_CANCEL_OPTION);

		return ConfirmAnswer.fromJOptionPaneResult(result);
	}

	public static String askForInput(final Component parent, final Label message, final String initialValue) {
		return JOptionPane.showInputDialog(parent, message.label(), initialValue);
	}

}
