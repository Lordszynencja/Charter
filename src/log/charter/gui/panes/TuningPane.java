package log.charter.gui.panes;

import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.song.ArrangementChart;
import log.charter.song.configs.Tuning;
import log.charter.song.configs.Tuning.TuningType;
import log.charter.util.CollectionUtils.ArrayList2;

public final class TuningPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.width = 300;

		return sizes;
	}

	private class TuningTypeHolder {
		public final TuningType tuningType;

		private TuningTypeHolder(final TuningType tuningType) {
			this.tuningType = tuningType;
		}

		@Override
		public String toString() {
			return tuningType.nameWithValues(tuning.strings);
		}
	}

	private final ChartData data;

	private final JComboBox<TuningTypeHolder> tuningSelect;
	private final int tuningInputsRow;
	private final List<TextInputWithValidation> tuningInputs = new ArrayList<>();

	private Tuning tuning;
	private int capo;

	boolean ignoreEvents = false;

	public TuningPane(final ChartData data, final CharterFrame frame) {
		super(frame, Label.TUNING_PANE.label(), 5, getSizes());

		this.data = data;
		final ArrangementChart arrangement = data.getCurrentArrangement();
		tuning = new Tuning(arrangement.tuning);
		capo = arrangement.capo;

		int row = 0;
		tuningSelect = addTuningSelect(row++);

		addIntegerConfigValue(row, 20, 0, Label.TUNING_STRINGS, tuning.strings, 20,
				createIntValidator(1, maxStrings, false), //
				this::onTuningStringsChanged, false);
		final TextInputWithValidation stringsInput = (TextInputWithValidation) components.getLast();
		stringsInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(stringsInput);

		addIntegerConfigValue(row++, 120, 0, Label.TUNING_CAPO, capo, 30, createIntValidator(0, Config.frets, false), //
				val -> capo = val, false);
		final TextInputWithValidation capoInput = (TextInputWithValidation) components.getLast();
		capoInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(capoInput);

		tuningInputsRow = row++;
		addTuningInputs();

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private JComboBox<TuningTypeHolder> addTuningSelect(final int row) {
		final List<TuningTypeHolder> values = new ArrayList2<>(TuningType.values()).map(TuningTypeHolder::new);
		final JComboBox<TuningTypeHolder> tuningSelect = new JComboBox<>(values.toArray(new TuningTypeHolder[0]));
		tuningSelect.addActionListener(
				e -> onTuningSelected(((TuningTypeHolder) tuningSelect.getSelectedItem()).tuningType));
		addLabel(row, 20, Label.TUNING_TYPE);
		this.add(tuningSelect, 75, getY(row), 200, 20);

		return tuningSelect;
	}

	private void addTuningInputs() {
		final int[] tuningValues = tuning.getTuning(maxStrings);
		for (int i = 0; i < maxStrings; i++) {
			final int string = i;
			addIntegerConfigValue(tuningInputsRow, 20 + i * 40, 0, null, tuningValues[i], 30,
					createIntValidator(-24, 24, false), //
					val -> onTuningValueChanged(string, val), false);
			final TextInputWithValidation tuningInput = (TextInputWithValidation) components.getLast();
			tuningInput.setHorizontalAlignment(JTextField.CENTER);
			addSelectTextOnFocus(tuningInput);
			tuningInputs.add(tuningInput);

			if (i >= tuning.strings) {
				this.remove(tuningInput);
			}
		}
	}

	private void onTuningSelected(final TuningType newTuningType) {
		if (ignoreEvents) {
			return;
		}

		ignoreEvents = true;
		tuning = new Tuning(newTuningType, tuning.strings);

		final int[] tuningValues = tuning.getTuning();
		for (int i = 0; i < tuning.strings; i++) {
			tuningInputs.get(i).setTextWithoutEvent(tuningValues[i] + "");
		}

		ignoreEvents = false;
	}

	private void onTuningStringsChanged(final int newStrings) {
		if (ignoreEvents) {
			return;
		}

		ignoreEvents = true;
		final int oldStrings = tuning.strings;
		tuning.strings(newStrings);

		for (int i = oldStrings; i < tuning.strings; i++) {
			this.add(tuningInputs.get(i), 20 + i * 40, getY(tuningInputsRow), 30, 20);
		}

		for (int i = tuning.strings; i < oldStrings; i++) {
			this.remove(tuningInputs.get(i));
		}

		repaint();

		ignoreEvents = false;
	}

	private void onTuningValueChanged(final int string, final int newValue) {
		if (ignoreEvents) {
			return;
		}

		ignoreEvents = true;
		tuning.changeTuning(string, newValue);
		tuningSelect.setSelectedIndex(tuning.tuningType.ordinal());

		ignoreEvents = false;
	}

	private void saveAndExit() {
		final ArrangementChart arrangement = data.getCurrentArrangement();
		arrangement.tuning = tuning;
		arrangement.capo = capo;
	}
}
