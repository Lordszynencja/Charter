package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.data.types.PositionType.GUITAR_NOTE;
import static log.charter.data.types.PositionType.HAND_SHAPE;
import static log.charter.data.types.PositionType.NONE;
import static log.charter.gui.components.utils.ComponentUtils.setComponentSize;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.virtual.IVirtualPosition;
import log.charter.data.types.PositionType;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.lookAndFeel.CharterCheckBox;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;

public class CurrentSelectionEditor extends RowedPanel implements Initiable {
	private static final long serialVersionUID = 1L;

	public static <T, U> U getSingleValue(final Collection<T> selected, final Function<T, U> mapper,
			final U defaultValue) {
		final List<U> values = selected.stream()//
				.map(mapper)//
				.distinct()//
				.collect(Collectors.toList());

		return values.size() == 1 ? values.get(0) : defaultValue;
	}

	public static <T, U> U getSingleValueWithoutNulls(final Collection<T> selected, final Function<T, U> mapper,
			final U defaultValue) {
		final List<U> values = selected.stream()//
				.map(mapper)//
				.distinct()//
				.filter(v -> v != null)//
				.collect(Collectors.toList());

		return values.size() == 1 ? values.get(0) : defaultValue;
	}

	private ChartData chartData;
	private CharterContext charterContext;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;

	private final Map<PositionType, SelectionEditorPart<?>> parts = new HashMap<>();

	private final GuitarSoundSelectionEditor guitarSoundSelectionEditor = new GuitarSoundSelectionEditor(this);
	private final HandShapeSelectionEditor handShapeSelectionEditor = new HandShapeSelectionEditor(this);

	private List<JCheckBox> stringSelects;

	public CurrentSelectionEditor() {
		super(new PaneSizesBuilder(0).build());

		setOpaque(true);
		setBackground(ColorLabel.BASE_BG_2.color());

		setMinimumSize(new Dimension(925, sizes.getHeight(10)));

		parts.put(PositionType.EVENT_POINT, new EventPointSelectionEditor());
		parts.put(PositionType.FHP, new FHPSelectionEditor());
		parts.put(PositionType.SHOWLIGHT, new ShowlightSelectionEditor());
		parts.put(PositionType.TONE_CHANGE, new ToneChangeSelectionEditor());
		parts.put(PositionType.VOCAL, new VocalSelectionEditor());
	}

	private void addStringsSelection() {
		stringSelects = new ArrayList<>();

		for (int i = 0; i < InstrumentConfig.maxStrings; i++) {
			final JCheckBox stringCheckbox = new JCheckBox();
			stringCheckbox.setFocusable(false);
			stringCheckbox.setVisible(false);
			addWithSettingSize(stringCheckbox, 0, 0, 20, 20);
			stringSelects.add(stringCheckbox);
		}
	}

	public void addStringChangeOperation(final Runnable action) {
		stringSelects.forEach(s -> s.addActionListener(e -> action.run()));
	}

	public void setStringSelectPosition(final int string, final int x, final int y) {
		if (string < 0 || string >= stringSelects.size()) {
			return;
		}

		final JCheckBox stringSelect = stringSelects.get(string);
		stringSelect.setLocation(x, y);
		stringSelect.setIcon(new CharterCheckBox.CheckBoxIcon(
				getStringBasedColor(StringColorLabelType.NOTE, string, chartData.currentStrings())));
		stringSelect.setVisible(true);
	}

	@Override
	public void init() {
		addStringsSelection();

		for (final SelectionEditorPart<?> part : parts.values()) {
			charterContext.initObject(part);
			part.addTo(this);
		}

		charterContext.initObject(guitarSoundSelectionEditor);
		guitarSoundSelectionEditor.addTo(this);

		charterContext.initObject(handShapeSelectionEditor);
		handShapeSelectionEditor.addTo(this);

		recalculateSizes();

		addKeyListener(keyboardHandler);
	}

	private void hideAllfieldsExcept(final PositionType type) {
		stringSelects.forEach(s -> s.setVisible(false));
		parts.forEach((partType, part) -> part.show(type == partType));

		if (type != GUITAR_NOTE) {
			guitarSoundSelectionEditor.hideFields();
		} else {
			guitarSoundSelectionEditor.showFields();
		}

		if (type != HAND_SHAPE) {
			handShapeSelectionEditor.hideFields();
		} else {
			handShapeSelectionEditor.showFields();
		}
	}

	@SuppressWarnings("unchecked")
	public void selectionChanged(final boolean stringsCouldChange) {
		if (stringsCouldChange) {
			stringSelects.forEach(s -> s.setSelected(false));
		}

		final ISelectionAccessor<? extends IVirtualPosition> selected = selectionManager.selectedAccessor();
		if (selected == null || !selected.isSelected()) {
			hideAllfieldsExcept(NONE);
			return;
		}

		hideAllfieldsExcept(selected.type());

		final SelectionEditorPart<?> part = parts.get(selected.type());
		if (part != null) {
			part.selectionChanged();
		} else {
			switch (selected.type()) {
				case GUITAR_NOTE:
					guitarSoundSelectionEditor.selectionChanged((ISelectionAccessor<ChordOrNote>) selected,
							stringsCouldChange);
					break;
				case HAND_SHAPE:
					handShapeSelectionEditor.selectionChanged((ISelectionAccessor<HandShape>) selected);
					break;
				default:
					break;
			}
		}

		repaint();
	}

	public boolean isSelected(final int string) {
		return stringSelects.get(string).isSelected();
	}

	public boolean isEdited(final int string) {
		return stringSelects.stream().allMatch(checkBox -> !checkBox.isSelected()) || isSelected(string);
	}

	public Set<Integer> getEditedStrings() {
		final Set<Integer> editedStrings = new HashSet<>();

		if (stringSelects.stream().allMatch(checkBox -> !checkBox.isSelected())) {
			for (int i = 0; i < stringSelects.size(); i++) {
				editedStrings.add(i);
			}
		} else {
			for (int i = 0; i < stringSelects.size(); i++) {
				if (stringSelects.get(i).isSelected()) {
					editedStrings.add(i);
				}
			}
		}

		return editedStrings;
	}

	public void toggleString(final int string) {
		if (string < 0 || string >= stringSelects.size()) {
			return;
		}

		final JCheckBox stringSelected = stringSelects.get(string);
		stringSelected.setSelected(!stringSelected.isSelected());
		stringSelected.repaint();
	}

	public void recalculateSizes() {
		for (final JCheckBox stringSelect : stringSelects) {
			setComponentSize(stringSelect, inputSize, inputSize);
		}

		guitarSoundSelectionEditor.recalculateSizes();
		handShapeSelectionEditor.recalculateSizes();

		for (final SelectionEditorPart<?> parts : parts.values()) {
			parts.recalculateSizes();
		}

		final ISelectionAccessor<? extends IVirtualPosition> selected = selectionManager.selectedAccessor();
		if (selected == null || !selected.isSelected()) {
			hideAllfieldsExcept(NONE);
			return;
		}

		hideAllfieldsExcept(selected.type());

		ComponentUtils.resize(this, 0, 0, inputSize * 76, inputSize * 62 / 4);
	}
}
