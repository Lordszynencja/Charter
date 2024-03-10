package log.charter.gui.components.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
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

	public static void setComponentBoundsWithValidateRepaint(final Component component, final int x, final int y,
			final int w, final int h) {
		setComponentBounds(component, x, y, w, h);
		component.validate();
		component.repaint();
	}

	public static void showPopup(final Component parent, final Label message, final String... messageParams) {
		if (messageParams.length > 0) {
			showPopup(parent, message.label().formatted((Object[]) messageParams));
		} else {
			showPopup(parent, message.label());
		}
	}

	public static int showOptionsPopup(final Component parent, final Label title, final Label message,
			final Label... options) {
		final String[] optionStrings = new String[options.length];
		for (int i = 0; i < options.length; i++) {
			optionStrings[i] = options[i].label();
		}

		return showOptionsPopup(parent, title, message, optionStrings);
	}

	public static int showOptionsPopup(final Component parent, final Label title, final Label message,
			final String... options) {
		if (options.length == 0) {
			return -1;
		}

		return JOptionPane.showOptionDialog(parent, message.label(), title.label(), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
	}

	public static void showPopup(final Component parent, final String message) {
		JOptionPane.showMessageDialog(parent, message);
	}

	public static ConfirmAnswer askYesNo(final Component parent, final Label title, final Label message) {
		final int result = JOptionPane.showConfirmDialog(parent, message.label(), title.label(),
				JOptionPane.YES_NO_OPTION);

		return ConfirmAnswer.fromJOptionPaneResult(result);
	}

	public static ConfirmAnswer askYesNoCancel(final Component parent, final Label title, final Label message,
			final Object... messageParams) {
		String formattedMessage = message.label();
		if (messageParams.length > 0) {
			formattedMessage = formattedMessage.formatted((Object[]) messageParams);
		}
		final int result = JOptionPane.showConfirmDialog(parent, formattedMessage, title.label(),
				JOptionPane.YES_NO_CANCEL_OPTION);

		return ConfirmAnswer.fromJOptionPaneResult(result);
	}

	public static String askForInput(final Component parent, final Label message, final String initialValue) {
		return JOptionPane.showInputDialog(parent, message.label(), initialValue);
	}

	public static class ComponentWithOffset {
		private final Component component;
		private final int offset;

		public ComponentWithOffset(final Component component, final int offset) {
			this.component = component;
			this.offset = offset;
		}

		public void setLocationFor(final int middle) {
			component.setLocation(getX(middle), component.getY());
		}

		public int getX(final int middle) {
			return middle - component.getWidth() / 2 - offset;
		}
	}

	public static void addComponentCenteringOnResize(final Container parent, final ComponentWithOffset... components) {
		parent.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(final ComponentEvent e) {
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				final Insets insets = parent.getInsets();
				final int w = parent.getWidth() - insets.left - insets.right;
				final int middleX = w / 2;

				for (final ComponentWithOffset component : components) {
					component.setLocationFor(middleX);
				}
			}

			@Override
			public void componentMoved(final ComponentEvent e) {
			}

			@Override
			public void componentHidden(final ComponentEvent e) {
			}
		});
	}

	public static void setIcon(final JLabel label, final BufferedImage icon) {
		if (icon == null) {
			return;
		}

		label.setText(null);
		label.setIcon(new ImageIcon(icon));
	}

	public static void setIcon(final AbstractButton button, final BufferedImage icon) {
		if (icon == null) {
			return;
		}

		button.setText(null);
		button.setIcon(new ImageIcon(icon));
	}
}
