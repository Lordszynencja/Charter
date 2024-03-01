package log.charter.gui.components.selectionEditor.chords;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.ChordTemplateFingerSetter.setSuggestedFingers;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.containers.RowedPanel.setComponentBounds;
import static log.charter.gui.components.simple.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.song.ChordTemplate.fingerIds;
import static log.charter.song.ChordTemplate.fingerNames;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.chordTemplatePane.ChordTemplatePreview;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.AutocompleteInput;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.simple.AutocompleteInput.PopupComponentMouseListener;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.song.ChordTemplate;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ChordTemplateEditor implements MouseListener {
	private static class ChordSuggestion extends JComponent {
		private static final long serialVersionUID = -1894016649405114312L;
		private static final int shapeNoteSize = 4;
		private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);

		private boolean focused = false;
		private final String formattedText;
		private final BufferedImage chordShape;

		public ChordSuggestion(final int strings, final String formattedText,
				final AutocompleteInput<ChordTemplate> input, final ChordTemplate template) {
			super();
			this.formattedText = formattedText;

			chordShape = generateChordShapeImage(template, strings);
			final int width = chordShape.getWidth() + 7 + (int) (font.getSize() * 0.63 * formattedText.length());
			setSize(width, max(20, strings * shapeNoteSize + 4));

			addMouseListener(new PopupComponentMouseListener<>(input, template, this::onFocus, this::onDefocus));
		}

		private static BufferedImage generateChordShapeImage(final ChordTemplate template, final int strings) {
			final int[] frets = new int[strings];
			for (int i = 0; i < strings; i++) {
				frets[i] = -1;
			}
			int minUsedString = strings;
			int maxUsedString = 0;
			int minNonzeroFret = maxStrings;
			int maxNonzeroFret = 0;

			for (final Entry<Integer, Integer> entry : template.frets.entrySet()) {
				final int string = entry.getKey();
				final int fret = entry.getValue();

				frets[string] = fret;
				minUsedString = min(minUsedString, string);
				maxUsedString = max(maxUsedString, string);
				if (fret == 0) {
					continue;
				}

				minNonzeroFret = min(minNonzeroFret, fret);
				maxNonzeroFret = max(maxNonzeroFret, fret);
			}
			if (maxNonzeroFret < minNonzeroFret) {
				minNonzeroFret = maxNonzeroFret;
			}

			final int width = shapeNoteSize * max(4, maxNonzeroFret - minNonzeroFret);
			final BufferedImage image = new BufferedImage(width, strings * shapeNoteSize, BufferedImage.TYPE_INT_ARGB);
			final Graphics g = image.getGraphics();
			for (int string = 0; string < strings; string++) {
				final int fret = frets[string];
				if (fret == -1) {
					continue;
				}

				g.setColor(getStringBasedColor(StringColorLabelType.NOTE, string, strings));

				final int y = getStringPosition(string, strings) * shapeNoteSize;
				if (fret == 0) {
					final int height = (int) (shapeNoteSize * 0.6);
					g.fillRect(0, y + (shapeNoteSize - height) / 2, width, height);
					continue;
				}

				final int x = min(4, fret - minNonzeroFret) * shapeNoteSize;
				g.fillRect(x, y, shapeNoteSize, shapeNoteSize);
			}

			return image;
		}

		private void onFocus() {
			focused = true;
			repaint();
		}

		private void onDefocus() {
			focused = false;
			repaint();
		}

		private void drawBorder(final Graphics g) {
			g.setColor(ColorLabel.BASE_BG_4.color());
			g.drawLine(0, 0, getWidth(), 0);
			g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
			g.drawLine(0, 1, 0, getHeight() - 1);
			g.drawLine(getWidth() - 1, 1, getWidth() - 1, getHeight() - 1);
		}

		private void drawBackground(final Graphics g) {
			final Color bgColor = (focused ? ColorLabel.BASE_BG_4 : ColorLabel.BASE_BG_2).color();
			g.setColor(bgColor);
			g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
		}

		private void drawChordShape(final Graphics g) {
			final int x = 3;
			final int y = (getHeight() - chordShape.getHeight()) / 2;
			g.drawImage(chordShape, x, y, null);
		}

		private void drawText(final Graphics g) {
			g.setColor(ColorLabel.BASE_TEXT.color());
			g.setFont(font);

			g.drawString(formattedText, chordShape.getWidth() + 5, getHeight() / 2 + font.getSize() / 3);
		}

		@Override
		public void paint(final Graphics g) {
			super.paint(g);

			drawBorder(g);
			drawBackground(g);
			drawChordShape(g);
			drawText(g);
		}
	}

	private static final int width = 440;

	protected final RowedPanel parent;
	private boolean parentListenerAdded = false;
	private Runnable onChange;

	private ChartData data;

	private Supplier<ChordTemplate> chordTemplateSupplier;
	private boolean editingChordName = false;
	private int chordTemplateEditorRow;

	private ChordNameAdviceButton chordNameAdviceButton;
	private JLabel chordNameLabel;
	private AutocompleteInput<ChordTemplate> chordNameInput;
	private JLabel fretsLabel;
	private JLabel fingersLabel;
	private final ArrayList2<TextInputWithValidation> fretInputs = new ArrayList2<>();
	private final ArrayList2<TextInputWithValidation> fingerInputs = new ArrayList2<>();
	private ChordTemplatePreview chordTemplatePreview;

	private int chordIdWidth = 1;
	private int chordNameWidth = 1;
	private int chordFretsWidth = 1;

	protected ChordTemplateEditor(final RowedPanel parent) {
		this.parent = parent;
	}

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler,
			final Supplier<ChordTemplate> chordTemplateSupplier, final Runnable onChange) {
		this.onChange = onChange;

		this.data = data;

		this.chordTemplateSupplier = chordTemplateSupplier;
		chordTemplatePreview = new ChordTemplatePreview(parent, this, data, frame, keyboardHandler,
				chordTemplateSupplier);
	}

	private int calculateHeight(final int strings) {
		return 22 + strings * parent.rowHeight;
	}

	private void setCorrectPositions() {
		final int strings = data.currentStrings();

		final int x = chordTemplatePreview.getX();
		int y = chordTemplatePreview.getY();
		setComponentBounds(chordTemplatePreview, x, y, width, calculateHeight(strings));

		for (int i = 0; i < strings; i++) {
			y = parent.getY(chordTemplateEditorRow + 1 + getStringPosition(i, strings));

			final TextInputWithValidation fretInput = fretInputs.get(i);
			setComponentBounds(fretInput, fretInput.getX(), y, fretInput.getWidth(), fretInput.getHeight());

			final TextInputWithValidation fingerInput = fingerInputs.get(i);
			setComponentBounds(fingerInput, fingerInput.getX(), y, fingerInput.getWidth(), fingerInput.getHeight());
		}
	}

	protected void addChordNameSuggestionButton(final int x, final int row) {
		chordNameAdviceButton = new ChordNameAdviceButton(Label.CHORD_NAME_ADVICE, parent,
				() -> data.getCurrentArrangement().tuning, () -> chordTemplateSupplier.get().frets,
				this::onChordNameSelect);
		parent.add(chordNameAdviceButton, x, parent.getY(row), 150, 20);
	}

	protected void onChordNameSelect(final String newName) {
		chordTemplateSupplier.get().chordName = newName;
		chordNameInput.setTextWithoutUpdate(newName);
		onChange.run();

		parent.repaint();
	}

	private String formatChordTemplateName(final ChordTemplate template) {
		final String id = ("%" + chordIdWidth + "d")
				.formatted(data.getCurrentArrangement().chordTemplates.indexOf(template));
		final String name = ("%-" + chordNameWidth + "s").formatted(template.name());
		final String frets = template.getTemplateFrets(data.currentStrings(), chordFretsWidth);
		final String fingers = template.getTemplateFingers(data.currentStrings());
		final String format = "[%s] %s (%s) {%s}";

		return format.formatted(id, name, frets, fingers);
	}

	protected void addChordNameInput(final int x, final int row) {
		parent.addLabel(1, x - 80, Label.CHORD_NAME, 80);
		chordNameLabel = (JLabel) parent.components.getLast();

		chordNameInput = new AutocompleteInput<>(parent, 130, "", this::getPossibleChords,
				this::formatChordTemplateName, this::onChordTemplateChange);
		chordNameInput.setLabelGenerator(
				value -> new ChordSuggestion(data.currentStrings(), value.text, chordNameInput, value.value));
		chordNameInput.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(final DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				if (chordNameInput.isDisableDocumentUpdateHandling()) {
					return;
				}

				editingChordName = true;
				final String newName = chordNameInput.getText();
				chordTemplateSupplier.get().chordName = newName;
				onChange.run();
				editingChordName = false;
			}
		});

		parent.add(chordNameInput, x, parent.getY(row), 150, 20);
	}

	private String templateSearchName(final ChordTemplate template) {
		final int id = data.getCurrentArrangement().chordTemplates.indexOf(template);
		return id + " " + template.chordName.toLowerCase() + " " + template.getTemplateFrets(data.currentStrings());
	}

	private ArrayList2<ChordTemplate> getPossibleChords(final String filter) {
		final String filterLower = filter.toLowerCase();

		return data.getCurrentArrangement().chordTemplates.stream()//
				.filter(chordTemplate -> !chordTemplate.equals(chordTemplateSupplier.get())//
						&& templateSearchName(chordTemplate).contains(filterLower))//
				.collect(Collectors.toCollection(ArrayList2::new));
	}

	private void recalculateIdWidth() {
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;
		chordIdWidth = 1;
		int sizing = 10;
		while (sizing <= chordTemplates.size()) {
			sizing *= 10;
			chordIdWidth++;
		}
	}

	private void recalculateNameWidth() {
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;
		chordNameWidth = 0;
		for (final ChordTemplate template : chordTemplates) {
			chordNameWidth = max(chordNameWidth, template.name().length());
		}
	}

	private void recalculateFretsWidth() {
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;
		chordFretsWidth = 0;
		for (final ChordTemplate template : chordTemplates) {
			for (final int fret : template.frets.values()) {
				chordFretsWidth = max(chordFretsWidth, fret >= 10 ? 2 : 1);
			}
		}
	}

	private void recalculateSuggestionPartsWidths() {
		recalculateIdWidth();
		recalculateNameWidth();
		recalculateFretsWidth();
	}

	protected void setCurrentValuesInInputs() {
		final ChordTemplate chordTemplate = chordTemplateSupplier.get();
		chordNameInput.setTextWithoutUpdate(chordTemplate.chordName);
		chordNameInput.removePopups();

		for (int i = 0; i < data.currentStrings(); i++) {
			final Integer fret = chordTemplate.frets.get(i);
			final TextInputWithValidation input = fretInputs.get(i);
			input.setTextWithoutEvent(fret == null ? "" : fret.toString());
			input.repaint();

			final Integer finger = chordTemplate.fingers.get(i);
			final TextInputWithValidation fingerInput = fingerInputs.get(i);
			fingerInput.setTextWithoutEvent(fingerNames.get(finger));
			fingerInput.repaint();
		}

		chordTemplatePreview.changeStringsAmount();
		recalculateSuggestionPartsWidths();
		setCorrectPositions();
		chordTemplatePreview.repaint();
	}

	private void onChordTemplateChange(final ChordTemplate newChordTemplate) {
		final ChordTemplate chordTemplate = chordTemplateSupplier.get();

		chordTemplate.chordName = newChordTemplate.chordName;
		chordTemplate.arpeggio = newChordTemplate.arpeggio;
		chordTemplate.fingers = new HashMap2<>(newChordTemplate.fingers);
		chordTemplate.frets = new HashMap2<>(newChordTemplate.frets);

		setCurrentValuesInInputs();

		onChange.run();
		parent.repaint();
	}

	public void addChordTemplateEditor(final int baseX, final int row) {
		chordTemplateEditorRow = row;
		int x = baseX;

		final int fretLabelWidth = parent.addLabel(row, x, Label.FRET, 50);
		fretsLabel = (JLabel) parent.components.getLast();
		fretsLabel.setHorizontalAlignment(JLabel.CENTER);
		final int fretInputX = x + fretLabelWidth / 2 - 15;
		for (int i = 0; i < maxStrings; i++) {
			final int string = i;

			final TextInputWithValidation input = new TextInputWithValidation(null, 40,
					createIntValidator(0, Config.frets, true), (final Integer val) -> updateFretValue(string, val),
					false);
			input.setHorizontalAlignment(JTextField.CENTER);
			addSelectTextOnFocus(input);

			fretInputs.add(input);
			parent.add(input, fretInputX, parent.getY(row + 1 + getStringPosition(i, maxStrings)), 30, 20);
		}

		x += 5 + max(fretLabelWidth, 40);

		final int fingerLabelWidth = parent.addLabel(row, x, Label.CHORD_TEMPLATE_FINGER, 50);
		fingersLabel = (JLabel) parent.components.getLast();
		fingersLabel.setHorizontalAlignment(JLabel.CENTER);

		final int fingerInputX = x + fingerLabelWidth / 2 - 10;
		for (int i = 0; i < maxStrings; i++) {
			final int string = i;

			final TextInputWithValidation input = new TextInputWithValidation("", 40, this::validateFinger,
					val -> updateFingerValue(string, val), false);
			input.setHorizontalAlignment(JTextField.CENTER);
			addSelectTextOnFocus(input);

			fingerInputs.add(input);
			parent.add(input, fingerInputX, parent.getY(row + 1 + getStringPosition(string, maxStrings)), 20, 20);
		}
		x += 5 + max(fingerLabelWidth, 20);

		final int y = parent.getY(row);
		parent.add(chordTemplatePreview, x, y, width, calculateHeight(maxStrings));
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

	private void updateFretValue(final int string, final Integer fret) {
		if (fret == null) {
			chordTemplateSupplier.get().frets.remove(string);
		} else {
			chordTemplateSupplier.get().frets.put(string, Integer.valueOf(fret));
		}

		updateFingers();
		onChange.run();

		chordTemplatePreview.repaint();
	}

	private void updateFingerValue(final int string, final String finger) {
		if (finger == null || finger.isEmpty()) {
			chordTemplateSupplier.get().fingers.remove(string);
		} else {
			chordTemplateSupplier.get().fingers.put(string, fingerIds.get(finger));
		}

		onChange.run();

		chordTemplatePreview.repaint();
	}

	private void updateFingers() {
		setSuggestedFingers(chordTemplateSupplier.get());
		for (int i = 0; i < Config.maxStrings; i++) {
			fingerUpdated(i, chordTemplateSupplier.get().fingers.get(i));
		}
	}

	public void fretUpdated(final int string, final Integer fret) {
		final TextInputWithValidation input = fretInputs.get(string);
		input.setTextWithoutEvent(fret == null ? "" : fret.toString());
		input.repaint();
		updateFingers();

		onChange.run();

		chordTemplatePreview.repaint();
	}

	public void fingerUpdated(final int string, final Integer finger) {
		final TextInputWithValidation input = fingerInputs.get(string);
		input.setTextWithoutEvent(fingerNames.get(finger));
		input.repaint();

		onChange.run();

		chordTemplatePreview.repaint();
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getComponent() != null) {
			e.getComponent().requestFocus();
		} else {
			parent.requestFocus();
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

	public void showFields() {
		chordNameAdviceButton.setVisible(true);
		chordNameLabel.setVisible(true);
		chordNameInput.setVisible(true);
		fretsLabel.setVisible(true);
		fingersLabel.setVisible(true);

		for (int i = 0; i < data.getCurrentArrangement().tuning.strings(); i++) {
			fretInputs.get(i).setVisible(true);
			fingerInputs.get(i).setVisible(true);
		}
		for (int i = data.getCurrentArrangement().tuning.strings(); i < maxStrings; i++) {
			fretInputs.get(i).setVisible(false);
			fingerInputs.get(i).setVisible(false);
		}

		if (!parentListenerAdded) {
			parent.addMouseListener(this);
			parentListenerAdded = true;
		}

		chordTemplatePreview.showFields();
	}

	public void hideFields() {
		chordNameAdviceButton.setVisible(false);
		chordNameAdviceButton.removePopup();
		chordNameLabel.setVisible(false);
		chordNameInput.setVisible(false);
		chordNameInput.removePopups();
		fretsLabel.setVisible(false);
		fingersLabel.setVisible(false);

		for (final TextInputWithValidation fretInput : fretInputs) {
			fretInput.setVisible(false);
		}
		for (final TextInputWithValidation fingerInput : fingerInputs) {
			fingerInput.setVisible(false);
		}

		if (parentListenerAdded) {
			parent.removeMouseListener(this);
			parentListenerAdded = false;
		}

		chordTemplatePreview.hideFields();
	}

	public void clearChordName() {
		if (editingChordName) {
			return;
		}

		chordNameInput.setTextWithoutUpdate("");
		chordNameInput.removePopups();
	}
}
