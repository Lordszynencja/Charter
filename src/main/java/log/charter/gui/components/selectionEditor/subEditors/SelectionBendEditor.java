package log.charter.gui.components.selectionEditor.subEditors;

import static log.charter.data.config.Config.maxStrings;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.RowedPanel;
import log.charter.song.BeatsMap;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class SelectionBendEditor extends RowedPanel {
	private static final long serialVersionUID = 6095874968137603127L;

	private final ChartData data;
	private final SelectionManager selectionManager;
	private final UndoSystem undoSystem;

	private final BendEditorGraph bendEditorGraph;

	private ButtonGroup stringsGroup;
	private List<JRadioButton> strings;

	private ChordOrNote getCurrentlySelectedSound() {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		return selectionAccessor.getSortedSelected().get(0).selectable;
	}

	public SelectionBendEditor(final RowedPanel parent, final ChartData data, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		super(500, 25, 2);

		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		addRadioButtons();

		bendEditorGraph = new BendEditorGraph(new BeatsMap(1), this::onChangeBends);

		final JScrollPane scrollPane = new JScrollPane(bendEditorGraph, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.validate();
		this.add(scrollPane, 20, getY(1), 440, BendEditorGraph.height + 20);

		setSize(500, getY(2) + BendEditorGraph.height);
		setMinimumSize(getSize());
		setPreferredSize(getSize());
		setMaximumSize(getSize());
	}

	private void addRadioButtons() {
		stringsGroup = new ButtonGroup();
		strings = new ArrayList<>();
		for (int i = 0; i < maxStrings; i++) {
			final int string = i;
			final JRadioButton radioButton = new JRadioButton((string + 1) + "");
			radioButton.setForeground(ColorLabel.valueOf("NOTE_" + string).color());
			radioButton.addActionListener(e -> onSelectString(string));
			this.add(radioButton, 20 + 40 * i, getY(0), 40, 20);

			stringsGroup.add(radioButton);
			strings.add(radioButton);
		}
	}

	private void onSelectString(final int string) {
		final ChordOrNote sound = getCurrentlySelectedSound();
		if (sound.isChord()) {
			bendEditorGraph.setBendValues(string, sound.chord.bendValues.get(string));
		}
	}

	private void enableAndSelectStringsForNote(final Note note) {
		final int string = note.string;

		for (int i = 0; i < string; i++) {
			strings.get(i).setEnabled(false);
		}
		strings.get(string).setEnabled(true);
		strings.get(string).setSelected(true);
		for (int i = string + 1; i < data.currentStrings(); i++) {
			strings.get(i).setEnabled(false);
		}

		bendEditorGraph.setNote(note);
	}

	private void enableAndSelectStringsForChord(final Chord chord) {
		final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.chordId);
		final int string = chordTemplate.frets.keySet().stream().min(Integer::compare).get();

		for (int i = 0; i < data.currentStrings(); i++) {
			strings.get(i).setEnabled(chordTemplate.frets.containsKey(i));
		}
		strings.get(string).setSelected(true);

		bendEditorGraph.setChord(chord, string);
	}

	public void enableAndSelectStrings(final ChordOrNote sound) {
		if (sound.isNote()) {
			enableAndSelectStringsForNote(sound.note);
			return;
		}

		enableAndSelectStringsForChord(sound.chord);
	}

	public void onChangeSelection(final ChordOrNote selected) {
		bendEditorGraph.setBeatsMap(data.songChart.beatsMap);

		final int stringsAmount = data.currentStrings();
		for (int i = 0; i < stringsAmount; i++) {
			strings.get(i).setVisible(true);
		}
		for (int i = stringsAmount; i < maxStrings; i++) {
			strings.get(i).setVisible(false);
		}

		enableAndSelectStrings(selected);
	}

	private void onChangeBends(final int string, final ArrayList2<BendValue> newBends) {
		undoSystem.addUndo();

		final ChordOrNote sound = getCurrentlySelectedSound();
		if (sound.isNote()) {
			sound.note.bendValues = newBends;
		} else {
			sound.chord.bendValues.put(string, newBends);
		}
	}
}
