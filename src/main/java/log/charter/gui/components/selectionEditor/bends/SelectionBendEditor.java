package log.charter.gui.components.selectionEditor.bends;

import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.data.PaneSizesBuilder;
import log.charter.song.BeatsMap;
import log.charter.song.BendValue;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.CommonNoteWithFret;
import log.charter.util.CollectionUtils.ArrayList2;

public class SelectionBendEditor extends RowedPanel {
	private static final long serialVersionUID = 6095874968137603127L;

	private static void invert(final boolean[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = !values[i];
		}
	}

	private final ChartData data;
	private final SelectionManager selectionManager;
	private final UndoSystem undoSystem;

	private final BendEditorGraph bendEditorGraph;

	private ButtonGroup stringsGroup;
	private List<JRadioButton> strings;
	private int lastStringsAmount = maxStrings;

	private ChordOrNote getCurrentlySelectedSound() {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		return selectionAccessor.getSortedSelected().get(0).selectable;
	}

	public SelectionBendEditor(final RowedPanel parent, final ChartData data, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		super(new PaneSizesBuilder(500).build(), 2);

		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		addRadioButtons();

		bendEditorGraph = new BendEditorGraph(new BeatsMap(1), this::onChangeBends);

		final JScrollPane scrollPane = new JScrollPane(bendEditorGraph, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.validate();
		this.add(scrollPane, 20, sizes.getY(1), 440, BendEditorGraph.height + 20);

		setSize(500, sizes.getY(2) + BendEditorGraph.height);
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
			radioButton.setForeground(getStringBasedColor(StringColorLabelType.NOTE, string, maxStrings));
			radioButton.addActionListener(e -> onSelectString(string));
			this.add(radioButton, 20 + 40 * i, sizes.getY(0), 40, 20);

			stringsGroup.add(radioButton);
			strings.add(radioButton);
		}
	}

	private void onSelectString(final int string) {
		final ChordOrNote sound = getCurrentlySelectedSound();
		if (sound.isChord()) {
			bendEditorGraph.setBendValues(string, sound.chord().chordNotes.get(string).bendValues);
		}
	}

	private void doActionOnStringButtons(final boolean[] shouldActionBeDone,
			final BiConsumer<JRadioButton, Integer> action) {
		for (int i = 0; i < maxStrings; i++) {
			if (shouldActionBeDone[i]) {
				action.accept(strings.get(i), i);
			}
		}
	}

	private void doActionOnStringButtons(final boolean[] shouldActionBeDone, final Consumer<JRadioButton> action) {
		for (int i = 0; i < maxStrings; i++) {
			if (shouldActionBeDone[i]) {
				action.accept(strings.get(i));
			}
		}
	}

	public void enableAndSelectStrings(final ChordOrNote sound) {
		final boolean[] stringsForAction = new boolean[maxStrings];
		final int lowestString = sound.notesWithFrets(data.getCurrentArrangement().chordTemplates)//
				.map(CommonNoteWithFret::string)//
				.peek(string -> stringsForAction[string] = true)//
				.collect(Collectors.minBy(Integer::compare)).get();

		doActionOnStringButtons(stringsForAction, button -> button.setEnabled(true));
		invert(stringsForAction);
		doActionOnStringButtons(stringsForAction, button -> button.setEnabled(false));

		strings.get(lowestString).setSelected(true);

		if (sound.isNote()) {
			bendEditorGraph.setNote(sound.note(), data.currentStrings());
			return;
		} else {
			bendEditorGraph.setChord(sound.chord(), lowestString, data.currentStrings());
		}
	}

	private void resetColors() {
		if (lastStringsAmount != data.currentStrings()) {
			lastStringsAmount = data.currentStrings();
			final boolean[] stringsForAction = new boolean[maxStrings];
			for (int i = 0; i < lastStringsAmount; i++) {
				stringsForAction[i] = true;
			}
			doActionOnStringButtons(stringsForAction, (button, i) -> {
				button.setForeground(getStringBasedColor(StringColorLabelType.NOTE, i, lastStringsAmount));
			});
		}
	}

	private void resetVisibleStrings() {
		final boolean[] stringsForAction = new boolean[maxStrings];
		for (int i = 0; i < lastStringsAmount; i++) {
			stringsForAction[i] = true;
		}
		doActionOnStringButtons(stringsForAction, button -> button.setVisible(true));
		for (int i = 0; i < maxStrings; i++) {
			stringsForAction[i] = !stringsForAction[i];
		}
		doActionOnStringButtons(stringsForAction, button -> button.setVisible(false));
	}

	public void onChangeSelection(final ChordOrNote selected) {
		bendEditorGraph.setBeatsMap(data.songChart.beatsMap);

		strings.forEach(button -> button.setVisible(false));

		resetColors();
		resetVisibleStrings();
		enableAndSelectStrings(selected);
	}

	private void onChangeBends(final int string, final ArrayList2<BendValue> newBends) {
		undoSystem.addUndo();

		getCurrentlySelectedSound().getString(string).ifPresent(note -> note.bendValues(newBends));
	}
}
