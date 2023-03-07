package log.charter.gui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import log.charter.data.config.Localization.Label;

public class FieldWithLabel<T extends Component> extends Container {
	public enum LabelPosition {
		LEFT, LEFT_CLOSE, RIGHT_CLOSE, RIGHT
	}

	private static final long serialVersionUID = 1L;

	public final JLabel label;
	public final T field;

	public FieldWithLabel(final Label label, final int labelWidth, final int inputWidth, final int height,
			final T field, final LabelPosition labelPosition) {
		super();
		setLayout(null);

		this.field = field;

		switch (labelPosition) {
		case LEFT:
			this.label = addLabel(label, 0, labelWidth, height, SwingConstants.LEFT);
			this.addField(field, labelWidth + 3, inputWidth, height);
			break;
		case LEFT_CLOSE:
			this.label = addLabel(label, 0, labelWidth, height, SwingConstants.RIGHT);
			this.addField(field, labelWidth + 3, inputWidth, height);
			break;
		case RIGHT_CLOSE:
			this.addField(field, 0, inputWidth, height);
			this.label = addLabel(label, inputWidth + 3, labelWidth, height, SwingConstants.LEFT);
			break;
		case RIGHT:
			this.addField(field, 0, inputWidth, height);
			this.label = addLabel(label, inputWidth + 3, labelWidth, height, SwingConstants.RIGHT);
			break;
		default:
			throw new RuntimeException("Unknown label position " + labelPosition);
		}

		this.setSize(labelWidth + inputWidth + 3, height);
	}

	private JLabel addLabel(final Label label, final int x, final int w, final int h, final int labelAlignment) {
		final JLabel labelComponent = new JLabel(label.label(), labelAlignment);
		labelComponent.setBounds(x, 0, w, h);
		final Dimension size = new Dimension(w, h);
		labelComponent.setMinimumSize(size);
		labelComponent.setPreferredSize(size);
		labelComponent.setMaximumSize(size);

		this.add(labelComponent);

		return labelComponent;
	}

	private void addField(final T field, final int x, final int w, final int h) {
		field.setBounds(x, 0, w, h);
		final Dimension size = new Dimension(w, h);
		field.setMinimumSize(size);
		field.setPreferredSize(size);
		field.setMaximumSize(size);

		this.add(field);
	}

}
