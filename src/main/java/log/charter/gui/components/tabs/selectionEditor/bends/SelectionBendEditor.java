package log.charter.gui.components.tabs.selectionEditor.bends;

import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.data.config.GraphicalConfig.inputSize;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.BendValue;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNoteWithFret;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.lookAndFeel.CharterRadioButton;
import log.charter.services.data.GuitarSoundsStatusesHandler;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;

public class SelectionBendEditor {
	private static void invert(final boolean[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = !values[i];
		}
	}

	private final ChartData chartData;
	private final GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private final SelectionManager selectionManager;
	private final UndoSystem undoSystem;

	private final CharterScrollPane scrollPane;
	private final BendEditorGraph bendEditorGraph;

	private ButtonGroup stringsGroup;
	private List<JRadioButton> strings;
	private int lastStringsAmount = InstrumentConfig.maxStrings;

	private Selection<ChordOrNote> getCurrentSelection() {
		final ISelectionAccessor<ChordOrNote> selectionAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		return selectionAccessor.getSelected().get(0);
	}

	public SelectionBendEditor(final RowedPanel parent, final ChartData chartData,
			final GuitarSoundsStatusesHandler guitarSoundsStatusesHandler, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.chartData = chartData;
		this.guitarSoundsStatusesHandler = guitarSoundsStatusesHandler;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		addRadioButtons(parent);

		bendEditorGraph = new BendEditorGraph(this::onChangeBends);

		scrollPane = new CharterScrollPane(bendEditorGraph, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		scrollPane.validate();
		parent.add(scrollPane);
	}

	private void addRadioButtons(final RowedPanel parent) {
		stringsGroup = new ButtonGroup();
		strings = new ArrayList<>();
		for (int i = 0; i < InstrumentConfig.maxStrings; i++) {
			final int string = i;
			final JRadioButton radioButton = new JRadioButton();
			radioButton.addActionListener(e -> onSelectString(string));
			parent.add(radioButton);

			stringsGroup.add(radioButton);
			strings.add(radioButton);
		}
	}

	private void onSelectString(final int string) {
		final Selection<ChordOrNote> selection = getCurrentSelection();
		if (selection.selectable.isChord()) {
			bendEditorGraph.setBendValues(string, selection.selectable.chord().chordNotes.get(string).bendValues);
		}
	}

	private void doActionOnStringButtons(final boolean[] shouldActionBeDone,
			final BiConsumer<JRadioButton, Integer> action) {
		for (int i = 0; i < InstrumentConfig.maxStrings; i++) {
			if (shouldActionBeDone[i]) {
				action.accept(strings.get(i), i);
			}
		}
	}

	private void doActionOnStringButtons(final boolean[] shouldActionBeDone, final Consumer<JRadioButton> action) {
		for (int i = 0; i < InstrumentConfig.maxStrings; i++) {
			if (shouldActionBeDone[i]) {
				action.accept(strings.get(i));
			}
		}
	}

	public void enableAndSelectStrings(final ChordOrNote sound) {
		final boolean[] stringsForAction = new boolean[InstrumentConfig.maxStrings];
		final int lowestString = sound.notesWithFrets(chartData.currentArrangement().chordTemplates)//
				.map(CommonNoteWithFret::string)//
				.peek(string -> stringsForAction[string] = true)//
				.collect(Collectors.minBy(Integer::compare)).get();

		doActionOnStringButtons(stringsForAction, button -> button.setEnabled(true));
		invert(stringsForAction);
		doActionOnStringButtons(stringsForAction, button -> button.setEnabled(false));

		strings.get(lowestString).setSelected(true);

		if (sound.isNote()) {
			bendEditorGraph.setNote(sound.note(), chartData.currentStrings());
			return;
		} else {
			bendEditorGraph.setChord(sound.chord(), lowestString, chartData.currentStrings());
		}
	}

	private void resetColors() {
		if (lastStringsAmount != chartData.currentStrings()) {
			lastStringsAmount = chartData.currentStrings();
			final boolean[] stringsForAction = new boolean[InstrumentConfig.maxStrings];
			for (int i = 0; i < lastStringsAmount; i++) {
				stringsForAction[i] = true;
			}
			doActionOnStringButtons(stringsForAction, (button, i) -> {
				button.setIcon(new CharterRadioButton.RadioIcon(
						getStringBasedColor(StringColorLabelType.NOTE, i, lastStringsAmount)));
			});
		}
	}

	private void resetVisibleStrings() {
		final boolean[] stringsForAction = new boolean[InstrumentConfig.maxStrings];
		for (int i = 0; i < lastStringsAmount; i++) {
			stringsForAction[i] = true;
		}
		doActionOnStringButtons(stringsForAction, button -> button.setVisible(true));
		for (int i = 0; i < InstrumentConfig.maxStrings; i++) {
			stringsForAction[i] = !stringsForAction[i];
		}
		doActionOnStringButtons(stringsForAction, button -> button.setVisible(false));
	}

	public void onChangeSelection(final ChordOrNote selected) {
		strings.forEach(button -> button.setVisible(false));

		resetColors();
		resetVisibleStrings();
		enableAndSelectStrings(selected);
	}

	private void onChangeBends(final int string, final List<BendValue> newBends) {
		undoSystem.addUndo();

		final Selection<ChordOrNote> selection = getCurrentSelection();
		selection.selectable.getString(string).ifPresent(note -> note.bendValues(newBends));
		guitarSoundsStatusesHandler.updateLinkedNote(selection.id);
	}

	public void setVisibility(final boolean visibility) {
		scrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		scrollPane.setVisible(visibility);
		for (int i = 0; i < strings.size(); i++) {
			strings.get(i).setVisible(visibility && i < chartData.currentStrings());
		}
	}

	public void recalculateSizesAndReposition(final int x, final int y) {
		ComponentUtils.resize(scrollPane, x, y + inputSize, inputSize * 30, inputSize * 9);
		ComponentUtils.resize(bendEditorGraph, 0, 0, inputSize * 10, inputSize * 7);

		for (int i = 0; i < strings.size(); i++) {
			ComponentUtils.resize(strings.get(i), x + inputSize * i * 3 / 2, y, inputSize * 3 / 2);
		}
	}
}
