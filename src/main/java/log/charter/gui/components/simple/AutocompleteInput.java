package log.charter.gui.components.simple;

import java.awt.Container;
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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.util.data.Position2D;

public class AutocompleteInput<T> extends JTextField
		implements DocumentListener, MouseListener, FocusListener, KeyListener {
	public static class LabelComponent {
		public final JComponent component;
		private final Runnable onComponentHighlight;
		private final Runnable onComponentLoseHighlight;

		public LabelComponent(final JComponent component, final Runnable onComponentHighlight,
				final Runnable onComponentLosingHighlight) {
			this.component = component;
			this.onComponentHighlight = onComponentHighlight;
			onComponentLoseHighlight = onComponentLosingHighlight;
		}
	}

	public static class AutocompleteValue<T> {
		public final String text;
		public final T value;

		private AutocompleteValue(final String text, final T value) {
			this.text = text;
			this.value = value;
		}
	}

	private class AutocompleteLabel implements MouseListener {
		private final int id;
		private final T value;
		public final JComponent component;
		private final Runnable onComponentHighlight;
		private final Runnable onComponentLosingHighlight;

		public AutocompleteLabel(final int id, final AutocompleteValue<T> autocompleteValue,
				final LabelComponent labelComponent) {
			this.id = id;
			this.value = autocompleteValue.value;
			this.component = labelComponent.component;
			this.onComponentHighlight = labelComponent.onComponentHighlight;
			this.onComponentLosingHighlight = labelComponent.onComponentLoseHighlight;

			component.addMouseListener(this);
		}

		public void accept() {
			onSelect.accept(value);
			removeLabels();
		}

		public void highlight() {
			if (highlightedLabel >= 0 && highlightedLabel < labels.size()) {
				labels.get(highlightedLabel).loseHighlight();
			}

			highlightedLabel = id;
			onComponentHighlight.run();
		}

		public void loseHighlight() {
			highlightedLabel = -1;
			onComponentLosingHighlight.run();
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

	private final Container parent;

	private final List<AutocompleteLabel> labels = new ArrayList<>();

	private final Function<String, List<T>> possibleValuesGetter;
	private final Function<T, String> formatter;
	private final Consumer<T> onSelect;
	private Consumer<String> onTextChange;

	private boolean disableDocumentUpdateHandling = false;
	private int highlightedLabel = -1;

	private Function<AutocompleteValue<T>, LabelComponent> labelGenerator = this::generateDefaultLabel;

	public AutocompleteInput(final Container parent, final int columns, final String text,
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

	public void setLabelGenerator(final Function<AutocompleteValue<T>, LabelComponent> labelGenerator) {
		if (labelGenerator == null) {
			return;
		}

		this.labelGenerator = labelGenerator;
	}

	public void setTextChangeListener(final Consumer<String> onTextChange) {
		this.onTextChange = onTextChange;
	}

	@Override
	protected void processKeyEvent(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !labels.isEmpty()) {
			removeLabels();
			e.consume();
			return;
		}

		super.processKeyEvent(e);
	}

	public void setHighlight(final int id) {
		this.highlightedLabel = id;
	}

	public void clearHighlight() {
		highlightedLabel = -1;
	}

	public void removeLabels() {
		labels.forEach(label -> parent.remove(label.component));
		labels.clear();
		clearHighlight();
		parent.repaint();
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getComponent() != this && e.getComponent() != null) {
			e.getComponent().requestFocus();
			removeLabels();
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

	private LabelComponent generateDefaultLabel(final AutocompleteValue<T> autocompleteValue) {
		final JLabel label = new JLabel(autocompleteValue.text);

		label.setOpaque(true);
		label.setBorder(new LineBorder(ColorLabel.BASE_BG_4.color()));
		label.setForeground(ColorLabel.BASE_TEXT.color());
		label.setFont(AutocompleteInput.this.getFont());
		label.setSize(getWidth(), 20);

		return new LabelComponent(label, () -> {
			label.setBackground(ColorLabel.BASE_BG_4.color());
			label.repaint();
		}, () -> {
			label.setBackground(ColorLabel.BASE_BG_2.color());
			label.repaint();
		});
	}

	private List<AutocompleteValue<T>> getValuesToShow(final String text) {
		return possibleValuesGetter.apply(text).stream()//
				.map(value -> new AutocompleteValue<>(formatter.apply(value), value))//
				.limit(10)//
				.collect(Collectors.toList());
	}

	private Position2D calculateFirstLabelPosition() {
		int x = getX();
		int y = getY() + getHeight();
		Container immediateParent = getParent();
		final Container contentPane = parent instanceof ParamsPane ? ((ParamsPane) parent).getContentPane() : parent;
		while (immediateParent != contentPane) {
			x += immediateParent.getX();
			y += immediateParent.getY();
			immediateParent = immediateParent.getParent();
		}

		return new Position2D(x, y);
	}

	private Position2D addLabel(final int id, final Position2D position, final AutocompleteValue<T> autocompleteValue) {
		final AutocompleteLabel label = new AutocompleteLabel(id, autocompleteValue,
				this.labelGenerator.apply(autocompleteValue));
		final int w = label.component.getWidth();
		final int h = label.component.getHeight();

		if (parent instanceof RowedPanel) {
			((RowedPanel) parent).addWithSettingSizeTop(label.component, position.x, position.y, w, h);
		} else if (parent instanceof ParamsPane) {
			((ParamsPane) parent).addTop(label.component, position.x, position.y, w, h);
		}

		labels.add(label);

		return position.move(0, h);
	}

	private void addLabels() {
		final String text = getText();
		if (text.isEmpty()) {
			return;
		}

		final List<AutocompleteValue<T>> valuesToShow = getValuesToShow(text);
		if (valuesToShow.isEmpty()) {
			return;
		}

		Position2D labelPosition = calculateFirstLabelPosition();
		int id = 0;
		for (final AutocompleteValue<T> v : valuesToShow) {
			labelPosition = addLabel(id++, labelPosition, v);
		}

		parent.repaint();
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (disableDocumentUpdateHandling) {
			return;
		}

		removeLabels();
		if (onTextChange != null) {
			onTextChange.accept(getText());
		}
		addLabels();
	}

	public void setTextWithoutUpdate(final String text) {
		disableDocumentUpdateHandling = true;
		setText(text);
		disableDocumentUpdateHandling = false;
	}

	public boolean isDisableDocumentUpdateHandling() {
		return disableDocumentUpdateHandling;
	}

	@Override
	public void focusGained(final FocusEvent e) {
	}

	@Override
	public void focusLost(final FocusEvent e) {
		this.removeLabels();
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (labels.isEmpty()) {
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				addLabels();
				e.consume();
			}

			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (highlightedLabel + 1 < labels.size()) {
				labels.get(highlightedLabel + 1).highlight();
			}

			e.consume();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (highlightedLabel > 0) {
				labels.get(highlightedLabel - 1).highlight();
			}

			e.consume();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (highlightedLabel >= 0 && highlightedLabel < labels.size()) {
				labels.get(highlightedLabel).accept();
			} else {
				removeLabels();
			}

			e.consume();
			return;
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}
}
