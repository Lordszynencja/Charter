package log.charter.gui.panes.programConfig;

import javax.swing.JCheckBox;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.SecretsConfig;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.RowedPosition;

public class ProgramSecretsConfigPage implements Page {
	private boolean explosions = SecretsConfig.explosions;
	private boolean explosionsShakyCam = SecretsConfig.explosionsShakyCam;

	private FieldWithLabel<JCheckBox> explosionsField;
	private FieldWithLabel<JCheckBox> explosionsShakyCamField;

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addExplosions(panel, position);
		addExplosionsShakyCam(panel, position);
	}

	private void setExplosions(final boolean newExplosions) {
		explosions = newExplosions;
		explosionsShakyCamField.field.setEnabled(explosions);
		if (!explosions) {
			explosionsShakyCam = false;
			explosionsShakyCamField.field.setSelected(false);
		}
	}

	private void addExplosions(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> setExplosions(input.isSelected()));
		input.setSelected(explosions);

		explosionsField = new FieldWithLabel<>(Label.EXPLOSIONS, 125, 20, 20, input, LabelPosition.LEFT);
		panel.add(explosionsField, position);
	}

	private void addExplosionsShakyCam(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> explosionsShakyCam = input.isSelected());
		input.setSelected(explosionsShakyCam);
		input.setEnabled(explosions);

		explosionsShakyCamField = new FieldWithLabel<>(Label.EXPLOSIONS_SHAKY_CAM, 125, 20, 20, input,
				LabelPosition.LEFT);
		panel.add(explosionsShakyCamField, position);
	}

	@Override
	public Label label() {
		return Label.CONFIG_SECRETS;
	}

	@Override
	public void setVisible(final boolean visibility) {
		explosionsField.setVisible(visibility);
		explosionsShakyCamField.setVisible(visibility);
	}

	public void save() {
		SecretsConfig.explosions = explosions;
		SecretsConfig.explosionsShakyCam = explosionsShakyCam;
	}

}
