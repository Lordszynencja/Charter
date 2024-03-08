package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.data.types.PositionType.ANCHOR;
import static log.charter.data.types.PositionType.GUITAR_NOTE;
import static log.charter.data.types.PositionType.HAND_SHAPE;
import static log.charter.data.types.PositionType.NONE;
import static log.charter.data.types.PositionType.TONE_CHANGE;
import static log.charter.data.types.PositionType.VOCAL;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.managers.CharterContext;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.song.Anchor;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.HashSet2;

public class CurrentSelectionEditor extends RowedPanel implements Initiable {
	private static final long serialVersionUID = 1L;

	public static <T extends IPosition, U> U getSingleValue(final HashSet2<Selection<T>> selected,
			final Function<Selection<T>, U> mapper, final U defaultValue) {
		final List<U> values = selected.stream()//
				.map(mapper)//
				.distinct()//
				.collect(Collectors.toList());

		return values.size() == 1 ? values.get(0) : defaultValue;
	}

	public static <T extends IPosition, U> U getSingleValueWithoutNulls(final HashSet2<Selection<T>> selected,
			final Function<Selection<T>, U> mapper, final U defaultValue) {
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

	private final AnchorSelectionEditor anchorSelectionEditor = new AnchorSelectionEditor();
	private final GuitarSoundSelectionEditor guitarSoundSelectionEditor = new GuitarSoundSelectionEditor(this);
	private final HandShapeSelectionEditor handShapeSelectionEditor = new HandShapeSelectionEditor(this);
	private final ToneChangeSelectionEditor toneChangeSelectionEditor = new ToneChangeSelectionEditor();
	private final VocalSelectionEditor vocalSelectionEditor = new VocalSelectionEditor();

	public CurrentSelectionEditor() {
		super(new PaneSizesBuilder(0).build());

		setOpaque(true);
		setBackground(ColorLabel.BASE_BG_2.color());
	}

	@Override
	public void init() {
		charterContext.initObject(anchorSelectionEditor);
		anchorSelectionEditor.addTo(this);

		charterContext.initObject(guitarSoundSelectionEditor);
		guitarSoundSelectionEditor.addTo(this);

		charterContext.initObject(handShapeSelectionEditor);
		handShapeSelectionEditor.addTo(this);

		charterContext.initObject(toneChangeSelectionEditor);
		toneChangeSelectionEditor.addTo(this);

		charterContext.initObject(vocalSelectionEditor);
		vocalSelectionEditor.addTo(this);

		addKeyListener(keyboardHandler);
	}

	private void hideAllfieldsExcept(final PositionType type) {
		if (type != ANCHOR) {
			anchorSelectionEditor.hideFields();
		} else {
			anchorSelectionEditor.showFields();
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

		if (type != TONE_CHANGE) {
			toneChangeSelectionEditor.hideFields();
		} else {
			toneChangeSelectionEditor.showFields();
		}

		if (type != VOCAL) {
			vocalSelectionEditor.hideFields();
		} else {
			vocalSelectionEditor.showFields();
		}
	}

	@SuppressWarnings("unchecked")
	public void selectionChanged(final boolean stringsCouldChange) {
		final SelectionAccessor<IPosition> selected = selectionManager.getCurrentlySelectedAccessor();
		if (selected == null || !selected.isSelected()) {
			hideAllfieldsExcept(NONE);
		}

		hideAllfieldsExcept(selected.type);

		if (selected.type == ANCHOR) {
			final SelectionAccessor<Anchor> selectedAnchorsAccessor = (SelectionAccessor<Anchor>) (SelectionAccessor<?>) selected;
			anchorSelectionEditor.selectionChanged(selectedAnchorsAccessor);
		}
		if (selected.type == GUITAR_NOTE) {
			final SelectionAccessor<ChordOrNote> selectedChordsOrNotesAccessor = (SelectionAccessor<ChordOrNote>) (SelectionAccessor<?>) selected;
			guitarSoundSelectionEditor.selectionChanged(selectedChordsOrNotesAccessor, stringsCouldChange);
		}
		if (selected.type == HAND_SHAPE) {
			final SelectionAccessor<HandShape> selectedAnchorsAccessor = (SelectionAccessor<HandShape>) (SelectionAccessor<?>) selected;
			handShapeSelectionEditor.selectionChanged(selectedAnchorsAccessor);
		}
		if (selected.type == TONE_CHANGE) {
			final SelectionAccessor<ToneChange> selectedToneChangesAccessor = (SelectionAccessor<ToneChange>) (SelectionAccessor<?>) selected;
			toneChangeSelectionEditor.selectionChanged(selectedToneChangesAccessor);
		}
		if (selected.type == VOCAL) {
			final SelectionAccessor<Vocal> selectedAnchorsAccessor = (SelectionAccessor<Vocal>) (SelectionAccessor<?>) selected;
			vocalSelectionEditor.selectionChanged(selectedAnchorsAccessor);
		}

		repaint();
	}

	public List<Integer> getSelectedStrings() {
		return guitarSoundSelectionEditor.getSelectedStrings();
	}
}
