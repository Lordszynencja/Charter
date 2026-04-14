package log.charter.gui.components.tabs.selectionEditor.chords;

import static log.charter.util.chordRecognition.ChordNameSuggester.suggestChordNames;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.configs.Tuning;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.util.data.Position2D;

public class ChordNameAdviceButton extends JButton
		implements ActionListener, MouseListener, FocusListener, KeyListener {
	private static final long serialVersionUID = 1L;

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

	private class AutocompleteLabel implements MouseListener {
		private final int id;
		private final String value;
		public final JComponent component;
		private final Runnable onComponentHighlight;
		private final Runnable onComponentLosingHighlight;

		public AutocompleteLabel(final int id, final String name, final LabelComponent labelComponent) {
			this.id = id;
			value = name;
			component = labelComponent.component;
			onComponentHighlight = labelComponent.onComponentHighlight;
			onComponentLosingHighlight = labelComponent.onComponentLoseHighlight;

			component.addMouseListener(this);
		}

		public void accept() {
			onChoose.accept(value);
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

	private final RowedPanel parent;
	private final List<AutocompleteLabel> labels = new ArrayList<>();
	private final Supplier<Tuning> tuningSupplier;
	private final Supplier<Map<Integer, Integer>> fretsSupplier;
	private final Consumer<String> onChoose;

	private int highlightedLabel = -1;

	public ChordNameAdviceButton(final Label label, final RowedPanel parent, final Supplier<Tuning> tuningSupplier,
			final Supplier<Map<Integer, Integer>> fretsSupplier, final Consumer<String> onChoose) {
		super(label.label());
		this.parent = parent;
		this.tuningSupplier = tuningSupplier;
		this.fretsSupplier = fretsSupplier;
		this.onChoose = onChoose;

		addActionListener(this);

		addKeyListener(this);
		addFocusListener(this);
		parent.addMouseListener(this);
		addMouseListener(this);
	}

	public void setHighlight(final int id) {
		highlightedLabel = id;
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
	protected void processKeyEvent(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !labels.isEmpty()) {
			removeLabels();
			e.consume();
			return;
		}

		super.processKeyEvent(e);
	}

	private LabelComponent generateLabel(final String name) {
		final JLabel label = new JLabel(name);

		label.setOpaque(true);
		label.setBorder(new LineBorder(ColorLabel.BASE_BG_4.color()));
		label.setForeground(ColorLabel.BASE_TEXT.color());
		label.setFont(getFont());
		label.setSize(getWidth(), 20);

		return new LabelComponent(label, () -> {
			label.setBackground(ColorLabel.BASE_BG_4.color());
			label.repaint();
		}, () -> {
			label.setBackground(ColorLabel.BASE_BG_2.color());
			label.repaint();
		});
	}

	private Position2D calculateFirstLabelPosition() {
		int x = getX();
		int y = getY() + getHeight();
		Container immediateParent = getParent();
		while (immediateParent != parent) {
			x += immediateParent.getX();
			y += immediateParent.getY();
			immediateParent = immediateParent.getParent();
		}

		return new Position2D(x, y);
	}

	private Position2D addLabel(final int id, final Position2D position, final String name) {
		final AutocompleteLabel label = new AutocompleteLabel(id, name, generateLabel(name));
		final int w = label.component.getWidth();
		final int h = label.component.getHeight();

		parent.addWithSettingSizeTop(label.component, position.x, position.y, w, h);

		labels.add(label);

		return position.move(0, h);
	}

	private void addLabels() {
		final List<String> names = suggestChordNames(tuningSupplier.get(), fretsSupplier.get());
		if (names.isEmpty()) {
			return;
		}

		Position2D labelPosition = calculateFirstLabelPosition();
		int id = 0;
		for (final String name : names) {
			labelPosition = addLabel(id++, labelPosition, name);
		}

		parent.repaint();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		removeLabels();
		addLabels();
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
	public void focusGained(final FocusEvent e) {
	}

	@Override
	public void focusLost(final FocusEvent e) {
		removeLabels();
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
