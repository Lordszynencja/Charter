package log.charter.gui.components;

import static java.lang.Math.max;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.util.Utils.getStringPosition;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.song.ChordTemplate;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ChordTemplateEditor extends ParamsPane implements MouseListener {
	private static final long serialVersionUID = 1L;

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

	protected final ChartData data;
	protected final ChordTemplate chordTemplate;

	private AutocompleteInput<ChordTemplate> chordNameInput;
	private final ArrayList2<TextInputWithValidation> fretInputs = new ArrayList2<>();
	private final ArrayList2<TextInputWithValidation> fingerInputs = new ArrayList2<>();
	private ChordTemplatePreview chordTemplatePreview;

	protected ChordTemplateEditor(final ChartData data, final CharterFrame frame, final Label title, final int rows,
			final PaneSizes sizes, final ChordTemplate chordTemplate) {
		super(frame, title.label(), rows, sizes);
		this.data = data;
		this.chordTemplate = chordTemplate;

		addMouseListener(this);

		setFocusable(true);
	}

	protected void addChordNameSuggestionButton(final int x, final int row) {
		this.add(new ChordNameAdviceButton(Label.CHORD_NAME_ADVICE, this, data, chordTemplate, this::onChordNameSelect),
				x, getY(row), 150, 20);
	}

	private void onChordNameSelect(final String newName) {
		chordTemplate.chordName = newName;
		chordNameInput.setTextWithoutUpdate(newName);
		repaint();
	}

	protected void addChordNameInput(final int x, final int row) {
		addLabel(1, x - 80, Label.CHORD_NAME);

		final int strings = data.getCurrentArrangement().tuning.strings;
		final Function<ChordTemplate, String> formatter = chordTemplate -> chordTemplate.getNameWithFrets(strings);
		chordNameInput = new AutocompleteInput<>(this, 80, chordTemplate.chordName, this::getPossibleChords, formatter,
				this::onChordTemplateChange);
		this.add(chordNameInput, x, getY(row), 150, 20);
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

	public void addChordTemplateEditor(final int row) {
		int x = 20;
		final int strings = data.getCurrentArrangement().tuning.strings;

		final int fretLabelWidth = addLabel(row, x, Label.FRET);
		final int fretInputX = x + fretLabelWidth / 2 - 15;
		for (int i = 0; i < strings; i++) {
			final int string = i;
			final Integer fret = chordTemplate.frets.get(string);

			final TextInputWithValidation input = new TextInputWithValidation(fret == null ? "" : fret.toString(), 40,
					createIntValidator(0, 28, true), val -> updateFretValue(string, val), false);

			fretInputs.add(input);
			add(input, fretInputX, getY(row + 1 + getStringPosition(i, strings)), 30, 20);
		}
		x += 5 + max(fretLabelWidth, 40);

		final int fingerLabelWidth = addLabel(row, x, Label.CHORD_TEMPLATE_FINGER);
		final int fingerInputX = x + fingerLabelWidth / 2 - 10;
		for (int i = 0; i < strings; i++) {
			final int string = i;
			final Integer finger = chordTemplate.fingers.get(string);

			final TextInputWithValidation input = new TextInputWithValidation(fingerNames.get(finger), 40,
					this::validateFinger, val -> updateFingerValue(string, val), false);

			fingerInputs.add(input);
			this.add(input, fingerInputX, getY(row + 1 + getStringPosition(string, strings)), 20, 20);
		}
		x += 5 + max(fingerLabelWidth, 20);

		final int y = getY(row);
		final int height = 22 + getY(row + data.getCurrentArrangement().tuning.strings) - y;
		chordTemplatePreview = new ChordTemplatePreview(this, data, chordTemplate, height);
		add(chordTemplatePreview, x, y, 240, height);
	}

	private String validateFinger(final String val) {
		if (val == null || val.isEmpty()) {
			return null;
		}
		if (fingerIds.containsKey(val)) {
			return null;
		}

		return Label.WRONG_FINGER_VALUE.label();
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

	public void fretUpdated(final int string, final Integer fret) {
		final TextInputWithValidation input = fretInputs.get(string);
		input.setTextWithoutEvent(fret == null ? "" : fret.toString());
		input.repaint();
		chordTemplatePreview.repaint();
	}

	public void fingerUpdated(final int string, final Integer finger) {
		final TextInputWithValidation input = fingerInputs.get(string);
		input.setTextWithoutEvent(fingerNames.get(finger));
		input.repaint();
		chordTemplatePreview.repaint();
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getComponent() != null) {
			e.getComponent().requestFocus();
		} else {
			this.requestFocus();
		}
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

	protected Integer getSavedTemplateId() {
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;
		for (int i = 0; i < chordTemplates.size(); i++) {
			final ChordTemplate existingChordTemplate = chordTemplates.get(i);
			if (existingChordTemplate.equals(chordTemplate)) {
				return i;
			}
		}

		chordTemplates.add(chordTemplate);
		return chordTemplates.size() - 1;
	}
}
