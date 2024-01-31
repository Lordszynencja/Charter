package log.charter.gui.panes;

import static java.lang.Math.max;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.ChordTemplateFingerSetter;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.song.ArrangementChart;
import log.charter.song.ArrangementChart.ArrangementSubtype;
import log.charter.song.ChordTemplate;
import log.charter.song.configs.Tuning;
import log.charter.song.configs.Tuning.TuningType;
import log.charter.util.CollectionUtils.ArrayList2;

public class ArrangementSettingsPane extends ParamsPane {
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

	private final CharterMenuBar charterMenuBar;
	private final ChartData data;
	private final CharterFrame frame;
	private final SelectionManager selectionManager;

	private JComboBox<TuningTypeHolder> tuningSelect;
	private final int tuningInputsRow;
	private final List<TextInputWithValidation> tuningInputs = new ArrayList<>();
	private FieldWithLabel<JCheckBox> moveFrets;

	public ArrangementType arrangementType;
	public ArrangementSubtype arrangementSubtype;
	private String baseTone;
	private Tuning tuning;
	private int capo;

	boolean ignoreEvents = false;

	public ArrangementSettingsPane(final CharterMenuBar charterMenuBar, final ChartData data, final CharterFrame frame,
			final SelectionManager selectionManager, final Runnable onCancel, final boolean newArrangement) {
		super(frame, Label.ARRANGEMENT_OPTIONS_PANE, getSizes());

		this.charterMenuBar = charterMenuBar;
		this.data = data;
		this.frame = frame;
		this.selectionManager = selectionManager;

		final ArrangementChart arrangement = data.getCurrentArrangement();
		arrangementType = arrangement.arrangementType;
		arrangementSubtype = arrangement.getSubType();
		baseTone = arrangement.baseTone;
		tuning = new Tuning(arrangement.tuning);
		capo = arrangement.capo;

		final AtomicInteger row = new AtomicInteger(0);
		addArrangmentType(row);
		addArrangmentSubtype(row);

		addConfigValue(row.getAndIncrement(), 20, 0, Label.ARRANGEMENT_OPTIONS_BASE_TONE, baseTone, 100,
				this::validateBaseTone, val -> baseTone = val, false);

		row.incrementAndGet();
		addTuningSelect(row);
		addStringsCapo(row);

		tuningInputsRow = row.getAndIncrement();
		addTuningInputs();
		if (newArrangement) {
			moveFrets = null;
		} else {
			addMoveFretsCheckbox(row);
		}

		addDefaultFinish(row.incrementAndGet(), this::saveAndExit, onCancel);
	}

	private String validateBaseTone(final String text) {
		if (text == null || text.isEmpty()) {
			return Label.VALUE_CANT_BE_EMPTY.label();
		}

		return null;
	}

	private void addArrangmentType(final AtomicInteger row) {
		final JComboBox<ArrangementType> arrangementTypeInput = new JComboBox<>(ArrangementType.values());
		arrangementTypeInput.setSelectedItem(arrangementType);
		arrangementTypeInput.addActionListener(e -> {
			arrangementType = (ArrangementType) arrangementTypeInput.getSelectedItem();
		});
		addLabel(row.get(), 20, Label.ARRANGEMENT_OPTIONS_TYPE);
		add(arrangementTypeInput, 150, getY(row.getAndIncrement()), 100, 20);
	}

	private void addArrangmentSubtype(final AtomicInteger row) {
		final JComboBox<ArrangementSubtype> arrangementSubtypeInput = new JComboBox<>(ArrangementSubtype.values());
		arrangementSubtypeInput.setSelectedItem(arrangementSubtype);
		arrangementSubtypeInput.addActionListener(e -> {
			arrangementSubtype = (ArrangementSubtype) arrangementSubtypeInput.getSelectedItem();
		});
		addLabel(row.get(), 20, Label.ARRANGEMENT_OPTIONS_SUBTYPE);
		add(arrangementSubtypeInput, 150, getY(row.getAndIncrement()), 100, 20);
	}

	private void addTuningSelect(final AtomicInteger row) {
		final List<TuningTypeHolder> values = new ArrayList2<>(TuningType.values()).map(TuningTypeHolder::new);
		tuningSelect = new JComboBox<>(values.toArray(new TuningTypeHolder[0]));
		tuningSelect.setSelectedIndex(tuning.tuningType.ordinal());
		tuningSelect.addActionListener(
				e -> onTuningSelected(((TuningTypeHolder) tuningSelect.getSelectedItem()).tuningType));
		addLabel(row.get(), 20, Label.ARRANGEMENT_OPTIONS_TUNING_TYPE);
		this.add(tuningSelect, 75, getY(row.getAndIncrement()), 200, 20);
	}

	private void addStringsCapo(final AtomicInteger row) {
		addIntegerConfigValue(row.get(), 20, 0, Label.ARRANGEMENT_OPTIONS_STRINGS, tuning.strings, 20,
				createIntValidator(1, maxStrings, false), //
				this::onTuningStringsChanged, false);
		final TextInputWithValidation stringsInput = (TextInputWithValidation) components.getLast();
		stringsInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(stringsInput);

		addIntegerConfigValue(row.getAndIncrement(), 120, 0, Label.ARRANGEMENT_OPTIONS_CAPO, capo, 30,
				createIntValidator(0, Config.frets, false), //
				val -> capo = val, false);
		final TextInputWithValidation capoInput = (TextInputWithValidation) components.getLast();
		capoInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(capoInput);
	}

	private void setTuningValues() {
		final int[] tuningValues = tuning.getTuning();
		for (int i = 0; i < tuningValues.length; i++) {
			tuningInputs.get(i).setTextWithoutEvent("" + tuningValues[i]);
		}
	}

	private void addTuningInputs() {
		for (int i = 0; i < Config.maxStrings; i++) {
			final int string = i;
			addIntegerConfigValue(tuningInputsRow, 20 + i * 40, 0, null, 0, 30, createIntValidator(-24, 24, false), //
					val -> onTuningValueChanged(string, val), false);
			final TextInputWithValidation tuningInput = (TextInputWithValidation) components.getLast();
			tuningInput.setHorizontalAlignment(JTextField.CENTER);
			addSelectTextOnFocus(tuningInput);
			tuningInputs.add(tuningInput);

			if (i >= tuning.strings) {
				this.remove(tuningInput);
			}
		}

		setTuningValues();
	}

	private void addMoveFretsCheckbox(final AtomicInteger row) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(true);

		moveFrets = new FieldWithLabel<JCheckBox>(Label.ARRANGEMENT_OPTIONS_MOVE_FRETS, 5, 20, 20, checkbox,
				LabelPosition.RIGHT_PACKED);
		add(moveFrets, 20, getY(row.getAndIncrement()), 200, 20);
	}

	private void onTuningSelected(final TuningType newTuningType) {
		if (ignoreEvents) {
			return;
		}

		ignoreEvents = true;
		tuning = new Tuning(newTuningType, tuning.strings);

		setTuningValues();

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
		setTuningValues();

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

	private void changeChordTemplate(final ChordTemplate chordTemplate, final int[] fretsDifference) {
		boolean templateChanged = false;
		for (final int string : new ArrayList<>(chordTemplate.frets.keySet())) {
			if (string >= tuning.strings) {
				chordTemplate.frets.remove(string);
				chordTemplate.fingers.remove(string);
			} else if (fretsDifference[string] != 0) {
				templateChanged = true;
				int newFret = chordTemplate.frets.get(string) + fretsDifference[string];
				if (newFret < 0) {
					newFret = 0;
				}
				chordTemplate.frets.put(string, newFret);
			}
		}

		if (templateChanged) {
			ChordTemplateFingerSetter.setSuggestedFingers(chordTemplate);
		}
	}

	private void saveAndExit() {
		final ArrangementChart arrangement = data.getCurrentArrangement();

		if (moveFrets != null && moveFrets.field.isSelected()) {
			final int[] fretsDifference = new int[tuning.strings];
			final int[] tuningBefore = arrangement.tuning.getTuning();
			final int[] tuningAfter = tuning.getTuning();
			for (int string = 0; string < tuning.strings; string++) {
				fretsDifference[string] = arrangement.tuning.strings <= string ? 0
						: tuningBefore[string] - tuningAfter[string];
			}

			arrangement.chordTemplates.forEach(chordTemplate -> changeChordTemplate(chordTemplate, fretsDifference));

			arrangement.levels.values().forEach(level -> {
				level.chordsAndNotes.forEach(sound -> {
					if (sound.isNote()) {
						if (sound.note.string >= tuning.strings) {
							sound.note.string = tuning.strings - 1;
						} else {
							sound.note.fret = max(0, sound.note.fret + fretsDifference[sound.note.string]);
						}
					}
				});
			});
		}

		arrangement.arrangementType = arrangementType;
		arrangement.setSubType(arrangementSubtype);
		arrangement.baseTone = baseTone;
		arrangement.tuning = tuning;
		arrangement.capo = capo;

		selectionManager.clear();

		charterMenuBar.refreshMenus();
		frame.updateEditAreaSizes();
	}
}
