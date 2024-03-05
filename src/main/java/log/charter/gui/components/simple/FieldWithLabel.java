package log.charter.gui.components.simple;

import static log.charter.gui.components.utils.ComponentUtils.setComponentBounds;

import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class FieldWithLabel<T extends Component> extends Container {
	public enum LabelPosition {
		LEFT, LEFT_CLOSE, LEFT_PACKED, RIGHT_PACKED, RIGHT_CLOSE, RIGHT
	}

	private static final long serialVersionUID = 1L;

	public ColorLabel backgroundColor = null;

	private final LabelPosition labelPosition;
	public final JLabel label;
	public final T field;

	public FieldWithLabel(final Label label, final int labelWidth, final int inputWidth, final int height,
			final T field, final LabelPosition labelPosition) {
		this(label.label(), labelWidth, inputWidth, height, field, labelPosition);
	}

	public FieldWithLabel(final String label, int labelWidth, final int inputWidth, final int height, final T field,
			final LabelPosition labelPosition) {
		super();
		setLayout(null);

		this.labelPosition = labelPosition;
		this.field = field;
		int totalSize = 0;

		switch (labelPosition) {
			case LEFT:
				this.label = addLabel(label, 0, labelWidth, height, SwingConstants.LEFT);
				this.addField(field, labelWidth + 5, inputWidth, height);
				totalSize = labelWidth + inputWidth + 5;
				break;
			case LEFT_CLOSE:
				this.label = addLabel(label, 0, labelWidth, height, SwingConstants.RIGHT);
				this.addField(field, labelWidth + 5, inputWidth, height);
				totalSize = labelWidth + inputWidth + 5;
				break;
			case LEFT_PACKED:
				this.label = addLabel(label, 0, 999, height, SwingConstants.LEFT);
				labelWidth += getTextWidth();
				this.label.setSize(labelWidth, height);
				this.addField(field, labelWidth, inputWidth, height);
				totalSize = labelWidth + inputWidth;
				break;
			case RIGHT_PACKED:
				this.addField(field, 0, inputWidth, height);
				this.label = addLabel(label, inputWidth + labelWidth, 999, height, SwingConstants.LEFT);
				totalSize = inputWidth + labelWidth + getTextWidth();
				break;
			case RIGHT_CLOSE:
				this.addField(field, 0, inputWidth, height);
				this.label = addLabel(label, inputWidth + 2, labelWidth, height, SwingConstants.LEFT);
				totalSize = labelWidth + inputWidth + 2;
				break;
			case RIGHT:
				this.addField(field, 0, inputWidth, height);
				this.label = addLabel(label, inputWidth + 2, labelWidth, height, SwingConstants.RIGHT);
				totalSize = labelWidth + inputWidth + 2;
				break;
			default:
				throw new RuntimeException("Unknown label position " + labelPosition);
		}

		this.setSize(totalSize, height);

		if (JToggleButton.class.isAssignableFrom(field.getClass())) {
			this.label.addMouseListener(makeMouseListener(((JToggleButton) field)::doClick));
		}
	}

	private MouseListener makeMouseListener(final Runnable onClick) {
		return new MouseListener() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				onClick.run();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
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
		};
	}

	private int getTextWidth() {
		final String text = label.getText();
		if (text == null) {
			return 0;
		}

		final Graphics2D gs = (Graphics2D) label.getGraphics();

		final FontMetrics fm = label.getFontMetrics(label.getFont());

		if (gs == null) {
			return fm.stringWidth(text);
		}

		final Rectangle2D rect = fm.getStringBounds(text, gs);
		final double w = rect.getWidth();
		gs.dispose();
		return (int) w;
	}

	private JLabel addLabel(final String label, final int x, final int w, final int h, final int labelAlignment) {
		final JLabel labelComponent = new JLabel(label, labelAlignment);
		labelComponent.setAlignmentY(CENTER_ALIGNMENT);

		setComponentBounds(labelComponent, x, 0, w, h);
		this.add(labelComponent);

		return labelComponent;
	}

	private void addField(final T field, final int x, final int w, final int h) {
		setComponentBounds(field, x, 0, w, h);
		this.add(field);
	}

	private int getLabelWidth() {
		final Icon icon = label.getIcon();
		if (icon != null) {
			return icon.getIconWidth();
		}

		if (labelPosition == LabelPosition.LEFT_CLOSE) {
			return label.getWidth();
		}

		return getTextWidth();
	}

	@Override
	public void paint(final Graphics g) {
		label.setSize(getLabelWidth(), label.getHeight());

		if (backgroundColor != null) {
			g.setColor(backgroundColor.color());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		super.paint(g);
	}
}
