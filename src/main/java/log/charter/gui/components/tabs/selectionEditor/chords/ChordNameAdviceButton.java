package log.charter.gui.components.tabs.selectionEditor.chords;

import static log.charter.util.chordRecognition.ChordNameSuggester.suggestChordNames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.song.configs.Tuning;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ChordNameAdviceButton extends JButton implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L;

	private class PopupLabelMouseListener implements MouseListener {
		private final JLabel label;
		private final String value;

		private PopupLabelMouseListener(final JLabel label, final String value) {
			this.label = label;
			this.value = value;
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			removePopup();
			onChoose.accept(value);
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

	private final RowedPanel parent;
	private final ArrayList2<JLabel> popups = new ArrayList2<>();
	private final Supplier<Tuning> tuningSupplier;
	private final Supplier<HashMap2<Integer, Integer>> fretsSupplier;
	private final Consumer<String> onChoose;

	public ChordNameAdviceButton(final Label label, final RowedPanel parent, final Supplier<Tuning> tuningSupplier,
			final Supplier<HashMap2<Integer, Integer>> fretsSupplier, final Consumer<String> onChoose) {
		super(label.label());
		this.parent = parent;
		this.tuningSupplier = tuningSupplier;
		this.fretsSupplier = fretsSupplier;
		this.onChoose = onChoose;

		addActionListener(this);

		parent.addMouseListener(this);
		addMouseListener(this);

		setFocusable(false);
	}

	public void removePopup() {
		popups.forEach(parent::remove);
		parent.repaint();
		popups.clear();
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

	private void addLabel(final int x, final int y, final String value) {
		final JLabel label = new JLabel(value);
		label.setOpaque(true);
		label.setBorder(new LineBorder(ColorLabel.BASE_BG_4.color()));
		label.addMouseListener(new PopupLabelMouseListener(label, value));
		parent.addWithSettingSizeTop(label, x, y, getWidth(), 20);
		popups.add(label);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		removePopup();

		final ArrayList2<String> suggestedChordNames = suggestChordNames(tuningSupplier.get(), fretsSupplier.get());

		final int x = getX();
		int y = getY() + getHeight();
		for (final String chordName : suggestedChordNames) {
			addLabel(x, y, chordName);
			y += 20;
		}

		parent.repaint();
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
}
