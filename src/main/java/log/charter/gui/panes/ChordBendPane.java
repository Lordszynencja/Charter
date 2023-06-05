package log.charter.gui.panes;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.BendEditor;
import log.charter.gui.components.ParamsPane;
import log.charter.song.BeatsMap;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.notes.Chord;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ChordBendPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 500;

		return sizes;
	}

	private final UndoSystem undoSystem;

	private final BendEditor bendEditor;

	private final Chord chord;

	private int string;
	private int strings;
	private HashMap2<Integer, ArrayList2<BendValue>> bendValues;

	public ChordBendPane(final BeatsMap beatsMap, final CharterFrame frame, final UndoSystem undoSystem,
			final Chord chord, final ChordTemplate chordTemplate, final int strings) {
		super(frame, Label.BEND_OPTIONS_PANE, 11, getSizes());
		this.undoSystem = undoSystem;
		this.strings = strings;

		this.chord = chord;
		bendValues = chord.bendValues.map(i -> i, bendValues -> bendValues.map(BendValue::new));

		final List<Integer> possibleStrings = new ArrayList<>();
		for (final Entry<Integer, Integer> fret : chordTemplate.frets.entrySet()) {
			if (fret.getValue() > 0) {
				possibleStrings.add(fret.getKey());
			}
		}

		if (possibleStrings.isEmpty()) {
			bendEditor = null;
			dispose();
			return;
		}

		possibleStrings.sort(null);
		addRadioButtons(possibleStrings);
		int width = 40 + possibleStrings.size() * 40;

		string = possibleStrings.get(0);
		bendEditor = new BendEditor(beatsMap, chord.position(), chord.length(), string, bendValues.get(string),
				strings);

		final int maxWidth = frame.getWidth() - 40;
		if (bendEditor.getWidth() < maxWidth) {
			bendEditor.setLocation(20, getY(1));
			width = max(width, bendEditor.getWidth() + 40);
			this.add(bendEditor);
		} else {
			final JScrollPane scrollPane = new JScrollPane(bendEditor, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollPane.validate();
			final int height = scrollPane.getPreferredSize().height;
			width = maxWidth;
			this.add(scrollPane, 20, getY(1), maxWidth - 40, height);
		}
		setWidthWithInsets(width);

		addDefaultFinish(10, this::saveAndExit);
	}

	private void addRadioButtons(final List<Integer> possibleStrings) {
		final ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < possibleStrings.size(); i++) {
			final int string = possibleStrings.get(i);
			final JRadioButton radioButton = new JRadioButton((string + 1) + "");
			radioButton.setForeground(ChartPanelColors.getStringBasedColor(StringColorLabelType.NOTE, string, strings));
			radioButton.addActionListener(e -> changeString(string));
			if (i == 0) {
				radioButton.setSelected(true);
			}
			group.add(radioButton);
			this.add(radioButton, 20 + 40 * i, getY(0), 40, 20);
		}
	}

	private void changeString(final int newString) {
		bendValues.put(string, bendEditor.getBendValues());
		string = newString;
		bendEditor.setBendValues(string, bendValues.get(string));
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		bendValues.put(string, bendEditor.getBendValues());
		chord.bendValues = new HashMap2<>();

		for (final Entry<Integer, ArrayList2<BendValue>> stringBendValues : bendValues.entrySet()) {
			if (stringBendValues.getValue().isEmpty()) {
				continue;
			}

			chord.bendValues.put(stringBendValues.getKey(), stringBendValues.getValue());
		}
	}
}
