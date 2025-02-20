package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.data.types.PositionType.GUITAR_NOTE;
import static log.charter.data.types.PositionType.HAND_SHAPE;
import static log.charter.data.types.PositionType.NONE;
import static log.charter.data.types.PositionType.VOCAL;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.virtual.IVirtualPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.PaneSizesBuilder;
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

	private CharterContext charterContext;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;

	private final Map<PositionType, SelectionEditorPart<?>> parts = new HashMap<>();

	private final FHPSelectionEditor fhpSelectionEditor = new FHPSelectionEditor();
	private final GuitarSoundSelectionEditor guitarSoundSelectionEditor = new GuitarSoundSelectionEditor(this);
	private final HandShapeSelectionEditor handShapeSelectionEditor = new HandShapeSelectionEditor(this);
	private final ToneChangeSelectionEditor toneChangeSelectionEditor = new ToneChangeSelectionEditor();
	private final VocalSelectionEditor vocalSelectionEditor = new VocalSelectionEditor();

	public CurrentSelectionEditor() {
		super(new PaneSizesBuilder(0).build());

		setOpaque(true);
		setBackground(ColorLabel.BASE_BG_2.color());

		setMinimumSize(new Dimension(925, sizes.getHeight(10)));

		parts.put(PositionType.EVENT_POINT, new EventPointSelectionEditor());
	}

	@Override
	public void init() {
		for (final SelectionEditorPart<?> part : parts.values()) {
			charterContext.initObject(part);
			part.addTo(this);
		}

		charterContext.initObject(fhpSelectionEditor);
		fhpSelectionEditor.addTo(this);

		charterContext.initObject(guitarSoundSelectionEditor);
		guitarSoundSelectionEditor.addTo(this);

		charterContext.initObject(handShapeSelectionEditor);
		handShapeSelectionEditor.addTo(this);

		charterContext.initObject(toneChangeSelectionEditor);
		toneChangeSelectionEditor.addTo(this);

		charterContext.initObject(vocalSelectionEditor);
		vocalSelectionEditor.addTo(this);

		hideAllfieldsExcept(null);

		addKeyListener(keyboardHandler);
	}

	private void hideAllfieldsExcept(final PositionType type) {
		parts.forEach((partType, part) -> {
			if (type != partType) {
				part.hide();
			}
		});

		if (type != PositionType.FHP) {
			fhpSelectionEditor.hide();
		}

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

		toneChangeSelectionEditor.setFieldsVisible(type == PositionType.TONE_CHANGE);

		if (type != VOCAL) {
			vocalSelectionEditor.hideFields();
		} else {
			vocalSelectionEditor.showFields();
		}
	}

	@SuppressWarnings("unchecked")
	public void selectionChanged(final boolean stringsCouldChange) {
		final ISelectionAccessor<? extends IVirtualPosition> selected = selectionManager.selectedAccessor();
		if (selected == null || !selected.isSelected()) {
			hideAllfieldsExcept(NONE);
		}

		hideAllfieldsExcept(selected.type());

		final SelectionEditorPart<?> part = parts.get(selected.type());
		if (part != null) {
			part.selectionChanged();
		} else {
			switch (selected.type()) {
				case FHP:
					fhpSelectionEditor.selectionChanged((ISelectionAccessor<FHP>) selected);
					break;
				case GUITAR_NOTE:
					guitarSoundSelectionEditor.selectionChanged((ISelectionAccessor<ChordOrNote>) selected,
							stringsCouldChange);
					break;
				case HAND_SHAPE:
					handShapeSelectionEditor.selectionChanged((ISelectionAccessor<HandShape>) selected);
					break;
				case TONE_CHANGE:
					toneChangeSelectionEditor.selectionChanged((ISelectionAccessor<ToneChange>) selected);
					break;
				case VOCAL:
					vocalSelectionEditor.selectionChanged((ISelectionAccessor<Vocal>) selected);
					break;
				default:
					break;
			}
		}

		repaint();
	}

	public List<Integer> getSelectedStrings() {
		return guitarSoundSelectionEditor.getSelectedStrings();
	}
}
