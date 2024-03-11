package log.charter.gui.components.simple;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.util.collections.ArrayList2;

public class AutocompleteInputForPane<T> extends JTextField implements DocumentListener, MouseListener {
	private static class AutocompleteValue<T> {
		public final String text;
		public final T value;

		private AutocompleteValue(final String text, final T value) {
			this.text = text;
			this.value = value;
		}
	}

	private class PopupLabelMouseListener implements MouseListener {
		private final JLabel label;
		private final AutocompleteValue<T> value;

		private PopupLabelMouseListener(final JLabel label, final AutocompleteValue<T> value) {
			this.label = label;
			this.value = value;
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			onSelect.accept(value.value);
			removePopup();
		}

		@Override
		public void mousePressed(final MouseEvent e) {
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
		}

		@Override
		public void mouseEntered(final MouseEvent e) {
			label.setBackground(ColorLabel.BASE_BG_4.color());
			label.repaint();
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			label.setBackground(ColorLabel.BASE_BG_2.color());
			label.repaint();
		}
	}

	private static final long serialVersionUID = 2783139051300279130L;

	private final ParamsPane parent;

	private final ArrayList2<JLabel> popups = new ArrayList2<>();

	private final Function<String, ArrayList2<T>> possibleValuesGetter;
	private final Function<T, String> formatter;
	private final Consumer<T> onSelect;

	private boolean disableDocumentUpdateHandling = false;

	public AutocompleteInputForPane(final ParamsPane parent, final int columns, final String text,
			final Function<String, ArrayList2<T>> possibleValuesGetter, final Function<T, String> formatter,
			final Consumer<T> onSelect) {
		super(columns);
		this.parent = parent;

		this.possibleValuesGetter = possibleValuesGetter;
		this.formatter = formatter;
		this.onSelect = onSelect;

		setText(text);

		parent.addMouseListener(this);
		addMouseListener(this);
		getDocument().addDocumentListener(this);
	}

	@Override
	protected void processKeyEvent(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !popups.isEmpty()) {
			removePopup();
			e.consume();
			return;
		}

		super.processKeyEvent(e);
	}

	private void removePopup() {
		popups.forEach(parent::remove);
		parent.repaint();
		popups.clear();
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getComponent() != this && e.getComponent() != null) {
			e.getComponent().requestFocus();
			removePopup();
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	private void addLabel(final int x, final int y, final AutocompleteValue<T> autocompleteValue) {
		final JLabel label = new JLabel(autocompleteValue.text);
		label.setOpaque(true);
		label.setBorder(new LineBorder(ColorLabel.BASE_BG_4.color()));
		label.setForeground(ColorLabel.BASE_TEXT.color());
		label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
		label.addMouseListener(new PopupLabelMouseListener(label, autocompleteValue));
		parent.addTop(label, x, y, getWidth(), 20);
		popups.add(label);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (disableDocumentUpdateHandling) {
			return;
		}

		removePopup();

		final String text = getText();

		final ArrayList2<AutocompleteValue<T>> valuesToShow = possibleValuesGetter.apply(text).stream()//
				.map(value -> new AutocompleteValue<>(formatter.apply(value), value))//
				.limit(10)//
				.collect(Collectors.toCollection(ArrayList2::new));

		final int x = getX();
		int y = getY() + getHeight();
		for (final AutocompleteValue<T> value : valuesToShow) {
			addLabel(x, y, value);
			y += 20;
		}

		parent.repaint();
	}

	public void setTextWithoutUpdate(final String text) {
		disableDocumentUpdateHandling = true;
		setText(text);
		disableDocumentUpdateHandling = false;
	}
}
