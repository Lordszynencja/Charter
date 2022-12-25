package log.charter.gui.panes;

import static java.lang.Math.max;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.util.Utils.getStringPosition;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.AutocompleteInput;
import log.charter.gui.components.ChordNameAdviceButton;
import log.charter.gui.components.ChordTemplatePreview;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class HandShapePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static final HashMap2<String, Integer> fingerIds = new HashMap2<>();
	private static final HashMap2<Integer, String> fingerNames = new HashMap2<>();

	static {
		fingerIds.put("T", 0);
		fingerIds.put("1", 1);
		fingerIds.put("2", 2);
		fingerIds.put("3", 3);
		fingerIds.put("4", 4);

		fingerIds.forEach((name, id) -> fingerNames.put(id, name));
		fingerNames.put(null, "");
	}

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.width = 400;

		return sizes;
	}

	private final ChartData data;

	private final HandShape handShape;
	private final ChordTemplate chordTemplate;

	private AutocompleteInput<ChordTemplate> chordNameInput;
	private ChordTemplatePreview chordTemplatePreview;
	private final ArrayList2<JTextField> fretInputs = new ArrayList2<>();
	private final ArrayList2<JTextField> fingerInputs = new ArrayList2<>();

	public HandShapePane(final ChartData data, final CharterFrame frame, final HandShape handShape) {
		super(frame, Label.HAND_SHAPE_PANE.label(), 8 + data.getCurrentArrangement().tuning.strings, getSizes());
		this.data = data;

		this.handShape = handShape;
		chordTemplate = handShape.chordId == -1 ? new ChordTemplate()
				: new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(handShape.chordId));

		createChordNameSuggestionButton();
		createChordNameInput();
		addConfigCheckbox(2, 20, 70, Label.ARPEGGIO, chordTemplate.arpeggio, val -> chordTemplate.arpeggio = val);
		createChordTemplateInputs();

		addDefaultFinish(7 + data.getCurrentArrangement().tuning.strings, this::saveAndExit);
	}

	private void createChordNameSuggestionButton() {
		this.add(new ChordNameAdviceButton(Label.CHORD_NAME_ADVICE, this, data, chordTemplate, this::onChordNameSelect),
				100, getY(0), 150, 20);
	}

	private void createChordNameInput() {
		addLabel(1, 20, Label.CHORD_NAME);

		final int strings = data.getCurrentArrangement().tuning.strings;
		final Function<ChordTemplate, String> formatter = chordTemplate -> chordTemplate.getNameWithFrets(strings);
		chordNameInput = new AutocompleteInput<>(this, 80, chordTemplate.chordName, this::getPossibleChords, formatter,
				this::onChordTemplateChange);
		this.add(chordNameInput, 100, getY(1), 150, 20);
	}

	private void createChordTemplateInputs() {
		int x = 20;
		final int strings = data.getCurrentArrangement().tuning.strings;

		final int fretLabelWidth = addLabel(4, x, Label.FRET);
		final int fretInputX = x + fretLabelWidth / 2 - 15;
		for (int i = 0; i < strings; i++) {
			final int string = getStringPosition(i, strings);
			final Integer fret = chordTemplate.frets.get(string);

			final JTextField input = new TextInputWithValidation(fret == null ? "" : fret.toString(), 40,
					createIntValidator(0, 28, true), val -> updateFretValue(string, val), false);

			fretInputs.add(input);
			this.add(input, fretInputX, getY(5 + i), 30, 20);
		}
		x += 5 + max(fretLabelWidth, 40);

		final int fingerLabelWidth = addLabel(4, x, Label.CHORD_TEMPLATE_FINGER);
		final int fingerInputX = x + fingerLabelWidth / 2 - 10;
		for (int i = 0; i < strings; i++) {
			final int string = getStringPosition(i, strings);
			final Integer finger = chordTemplate.fingers.get(string);

			final JTextField input = new TextInputWithValidation(fingerNames.get(finger), 40,
					val -> fingerIds.containsKey(val) ? null : "wrong finger name, must be one of (T, 1, 2, 3, 4)",
					val -> updateFingerValue(string, val), false);

			fingerInputs.add(input);
			this.add(input, fingerInputX, getY(5 + i), 20, 20);
		}
		x += 5 + max(fingerLabelWidth, 20);

		final int y = getY(4);
		final int height = 20 + getY(5 + data.getCurrentArrangement().tuning.strings) - y;
		chordTemplatePreview = new ChordTemplatePreview(data, chordTemplate);
		add(chordTemplatePreview, x, y, 240, height);
	}

	private void updateFretValue(final int string, final String fret) {
		if (fret == null || fret.isEmpty()) {
			chordTemplate.frets.remove(string);
		} else {
			chordTemplate.frets.put(string, Integer.valueOf(fret));
		}
		chordTemplatePreview.repaint();
	}

	private void updateFingerValue(final int string, final String finger) {
		if (finger == null || finger.isEmpty()) {
			chordTemplate.fingers.remove(string);
		} else {
			chordTemplate.fingers.put(string, fingerIds.get(finger));
		}
		chordTemplatePreview.repaint();
	}

	private ArrayList2<ChordTemplate> getPossibleChords(final String filter) {
		return data.getCurrentArrangement().chordTemplates.stream()//
				.filter(chordTemplate -> chordTemplate.chordName.toLowerCase().contains(filter.toLowerCase()))//
				.collect(Collectors.toCollection(ArrayList2::new));
	}

	private void onChordTemplateChange(final ChordTemplate newChordTemplate) {
		chordTemplate.chordName = newChordTemplate.chordName;
		chordTemplate.arpeggio = newChordTemplate.arpeggio;
		chordTemplate.fingers = new HashMap2<>(newChordTemplate.fingers);
		chordTemplate.frets = new HashMap2<>(newChordTemplate.frets);

		repaint();
	}

	private void onChordNameSelect(final String newName) {
		chordTemplate.chordName = newName;
		chordNameInput.setTextWithoutUpdate(newName);
		repaint();
	}

	private void setTemplate() {
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;
		for (int i = 0; i < chordTemplates.size(); i++) {
			final ChordTemplate existingChordTemplate = chordTemplates.get(i);
			if (existingChordTemplate.equals(chordTemplate)) {
				handShape.chordId = i;
				return;
			}
		}

		chordTemplates.add(chordTemplate);
		handShape.chordId = chordTemplates.size() - 1;
	}

	private void saveAndExit() {
		setTemplate();
		dispose();
	}
}
