package log.charter.gui.panes.songSettings;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.vocals.VocalPath;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.gui.panes.colorConfig.ColorPicker;
import log.charter.services.data.selection.SelectionManager;

public class VocalPathSettingsPane extends RowedDialog {
	private static final long serialVersionUID = -3193534671039163160L;

	private final ChartData chartData;
	private final CharterMenuBar charterMenuBar;
	private final CharterFrame frame;
	private final SelectionManager selectionManager;

	private final VocalPath vocalPath;
	private final boolean newPath;

	private final JTextField nameInput;
	private final ColorPicker colorPicker;

	public VocalPathSettingsPane(final ChartData chartData, final CharterMenuBar charterMenuBar,
			final CharterFrame frame, final SelectionManager selectionManager, final VocalPath vocalPath,
			final boolean newPath) {
		super(frame, Label.VOCAL_PATH_OPTIONS, 400);

		this.chartData = chartData;
		this.charterMenuBar = charterMenuBar;
		this.frame = frame;
		this.selectionManager = selectionManager;

		this.vocalPath = vocalPath;
		this.newPath = newPath;

		final RowedPosition position = new RowedPosition(20, panel.sizes);
		nameInput = addNameInput(position);

		position.newRow();
		colorPicker = addColorPicker(position);

		position.newRow();
		position.newRow();
		addDefaultFinish(position.getY(), SaverWithStatus.defaultFor(this::saveAndExit), null, true);
	}

	private JTextField addNameInput(final RowedPosition position) {
		final JTextField input = new JTextField(vocalPath.name, 200);
		final FieldWithLabel<JTextField> field = new FieldWithLabel<>(Label.VOCAL_PATH_NAME, 75, 150, 20, input,
				LabelPosition.LEFT);
		panel.add(field, position);

		return input;
	}

	private ColorPicker addColorPicker(final RowedPosition position) {
		final ColorPicker input = new ColorPicker(Label.VOCAL_PATH_COLOR, vocalPath.color);
		final FieldWithLabel<ColorPicker> field = new FieldWithLabel<>(Label.VOCAL_PATH_COLOR, 75, 120, 20, input,
				LabelPosition.LEFT);
		panel.add(field, position);

		return input;
	}

	private void saveAndExit() {
		vocalPath.name = nameInput.getText();
		vocalPath.color = colorPicker.color();

		if (newPath) {
			chartData.addVocals(vocalPath);
			selectionManager.clear();
			frame.updateSizes();
		}

		charterMenuBar.refreshMenus();
	}
}
