package log.charter.gui.components.simple;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.util.CollectionUtils.ArrayList2;

public class AutocompleteInput<T> extends JTextField implements DocumentListener, MouseListener {
	public static class AutocompleteValue<T> {
		public final String text;
		public final T value;

		private AutocompleteValue(final String text, final T value) {
			this.text = text;
			this.value = value;
		}
	}

	public static class PopupComponentMouseListener<T> implements MouseListener {
		private final AutocompleteInput<T> input;
		private final T value;
		private final Runnable onFocus;
		private final Runnable onDefocus;

		public PopupComponentMouseListener(final AutocompleteInput<T> input, final T value, final Runnable onFocus,
				final Runnable onDefocus) {
			this.input = input;
			this.value = value;
			this.onFocus = onFocus;
			this.onDefocus = onDefocus;
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			input.onSelect.accept(value);
			input.removePopups();
		}

		@Override
		public void mousePressed(final MouseEvent e) {
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
		}

		@Override
		public void mouseEntered(final MouseEvent e) {
			onFocus.run();
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			onDefocus.run();
		}
	}

	private class PopupLabelMouseListener extends PopupComponentMouseListener<T> {
		private PopupLabelMouseListener(final JLabel label, final AutocompleteValue<T> value) {
			super(AutocompleteInput.this, value.value, () -> onFocus(label), () -> onDefocus(label));
		}

		private static void onFocus(final JLabel label) {
			label.setBackground(ColorLabel.BASE_BG_4.color());
			label.repaint();
		}

		private static void onDefocus(final JLabel label) {
			label.setBackground(ColorLabel.BASE_BG_2.color());
			label.repaint();
		}
	}

	private static final long serialVersionUID = 2783139051300279130L;

	private final RowedPanel parent;

	private final ArrayList2<JComponent> popups = new ArrayList2<>();

	private final Function<String, ArrayList2<T>> possibleValuesGetter;
	private final Function<T, String> formatter;
	private final Consumer<T> onSelect;

	private boolean disableDocumentUpdateHandling = false;

	private Function<AutocompleteValue<T>, JComponent> labelGenerator = this::generateDefaultLabel;

	public AutocompleteInput(final RowedPanel parent, final int columns, final String text,
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

	public void setLabelGenerator(final Function<AutocompleteValue<T>, JComponent> labelGenerator) {
		if (labelGenerator == null) {
			return;
		}

		this.labelGenerator = labelGenerator;
	}

	@Override
	protected void processKeyEvent(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !popups.isEmpty()) {
			removePopups();
			e.consume();
			return;
		}

		super.processKeyEvent(e);
	}

	public void removePopups() {
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
			removePopups();
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

	private JComponent generateDefaultLabel(final AutocompleteValue<T> autocompleteValue) {
		final JLabel label = new JLabel(autocompleteValue.text);
		label.setOpaque(true);
		label.setBorder(new LineBorder(ColorLabel.BASE_BG_4.color()));
		label.setForeground(ColorLabel.BASE_TEXT.color());
		label.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
		label.addMouseListener(new PopupLabelMouseListener(label, autocompleteValue));
		label.setSize(getWidth(), 20);

		return label;
	}

	private void addLabel(final int x, final AtomicInteger y, final AutocompleteValue<T> autocompleteValue) {
		final JComponent label = labelGenerator.apply(autocompleteValue);
		parent.addTop(label, x, y.getAndAdd(label.getHeight()), label.getWidth(), label.getHeight());
		popups.add(label);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (disableDocumentUpdateHandling) {
			return;
		}

		removePopups();

		final String text = getText();
		if (text.isEmpty()) {
			return;
		}

		final ArrayList2<AutocompleteValue<T>> valuesToShow = possibleValuesGetter.apply(text).stream()//
				.map(value -> new AutocompleteValue<>(formatter.apply(value), value))//
				.limit(10)//
				.collect(Collectors.toCollection(ArrayList2::new));

		final int x = getX();
		final AtomicInteger y = new AtomicInteger(getY() + getHeight());
		valuesToShow.forEach(value -> addLabel(x, y, value));

		parent.repaint();
	}

	public void setTextWithoutUpdate(final String text) {
		disableDocumentUpdateHandling = true;
		setText(text);
		disableDocumentUpdateHandling = false;
	}

	public boolean isDisableDocumentUpdateHandling() {
		return disableDocumentUpdateHandling;
	}
}
