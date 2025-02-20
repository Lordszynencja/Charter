package log.charter.gui.components.simple;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
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

public class AutocompleteInputForPane<T> extends JTextField
		implements DocumentListener, MouseListener, FocusListener, KeyListener {
	private static class AutocompleteValue<T> {
		public final String text;
		public final T value;

		private AutocompleteValue(final String text, final T value) {
			this.text = text;
			this.value = value;
		}
	}

	private class AutocompleteLabel extends JLabel implements MouseListener {
		private static final long serialVersionUID = 3953397200799878564L;

		private final int id;
		private final T value;

		public AutocompleteLabel(final int id, final AutocompleteValue<T> autocompleteValue) {
			super(autocompleteValue.text);

			this.id = id;
			this.value = autocompleteValue.value;

			setOpaque(true);
			setBorder(new LineBorder(ColorLabel.BASE_BG_4.color()));
			setForeground(ColorLabel.BASE_TEXT.color());
			setFont(AutocompleteInputForPane.this.getFont());
			addMouseListener(this);
		}

		public void accept() {
			onSelect.accept(value);
			removePopup();
		}

		public void highlight() {
			if (highlightedLabel >= 0 && highlightedLabel < popups.size()) {
				popups.get(highlightedLabel).loseHighlight();
			}

			highlightedLabel = id;
			setBackground(ColorLabel.BASE_BG_4.color());
			repaint();
		}

		public void loseHighlight() {
			highlightedLabel = -1;
			setBackground(ColorLabel.BASE_BG_2.color());
			repaint();
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			accept();
		}

		@Override
		public void mousePressed(final MouseEvent e) {
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
		}

		@Override
		public void mouseEntered(final MouseEvent e) {
			highlight();
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			loseHighlight();
		}
	}

	private static final long serialVersionUID = 2783139051300279130L;

	private final ParamsPane parent;

	private final List<AutocompleteLabel> popups = new ArrayList<>();

	private final Function<String, List<T>> possibleValuesGetter;
	private final Function<T, String> formatter;
	private final Consumer<T> onSelect;

	private boolean disableDocumentUpdateHandling = false;
	private int highlightedLabel = -1;

	public AutocompleteInputForPane(final ParamsPane parent, final int columns, final String text,
			final Function<String, List<T>> possibleValuesGetter, final Function<T, String> formatter,
			final Consumer<T> onSelect) {
		super(columns);
		this.parent = parent;

		this.possibleValuesGetter = possibleValuesGetter;
		this.formatter = formatter;
		this.onSelect = onSelect;

		setText(text);

		addKeyListener(this);
		addFocusListener(this);
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

		highlightedLabel = -1;
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

	private void addLabel(final int id, final int x, final int y, final AutocompleteValue<T> autocompleteValue) {
		final AutocompleteLabel label = new AutocompleteLabel(id, autocompleteValue);
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

		final List<AutocompleteValue<T>> valuesToShow = possibleValuesGetter.apply(text).stream()//
				.map(value -> new AutocompleteValue<>(formatter.apply(value), value))//
				.limit(10)//
				.collect(Collectors.toCollection(ArrayList::new));

		final int x = getX();
		final int y = getY() + getHeight();
		for (int i = 0; i < valuesToShow.size(); i++) {
			final AutocompleteValue<T> value = valuesToShow.get(i);
			addLabel(i, x, y + i * 20, value);
		}

		parent.repaint();
	}

	public void setTextWithoutUpdate(final String text) {
		disableDocumentUpdateHandling = true;
		setText(text);
		disableDocumentUpdateHandling = false;
	}

	@Override
	public void focusGained(final FocusEvent e) {
	}

	@Override
	public void focusLost(final FocusEvent e) {
		this.removePopup();
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (popups.isEmpty()) {
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (highlightedLabel + 1 < popups.size()) {
				popups.get(highlightedLabel + 1).highlight();
			}

			e.consume();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (highlightedLabel > 0) {
				popups.get(highlightedLabel - 1).highlight();
			}

			e.consume();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (highlightedLabel >= 0 && highlightedLabel < popups.size()) {
				popups.get(highlightedLabel).accept();
			} else {
				removePopup();
			}

			e.consume();
			return;
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}
}
