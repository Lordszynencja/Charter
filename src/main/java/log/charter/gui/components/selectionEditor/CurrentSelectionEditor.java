package log.charter.gui.components.selectionEditor;

import static log.charter.data.types.PositionType.ANCHOR;
import static log.charter.data.types.PositionType.HAND_SHAPE;
import static log.charter.data.types.PositionType.NONE;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.RowedPanel;
import log.charter.song.Anchor;
import log.charter.song.HandShape;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.HashSet2;

public class CurrentSelectionEditor extends RowedPanel {
	private static final long serialVersionUID = 1L;

	public static <T extends IPosition, U> U getSingleValue(final HashSet2<Selection<T>> selected,
			final Function<Selection<T>, U> mapper) {
		final List<U> values = selected.stream()//
				.map(mapper)//
				.distinct()//
				.collect(Collectors.toList());

		return values.size() == 1 ? values.get(0) : null;
	}

	private SelectionManager selectionManager;

	private final AnchorSelectionEditor anchorSelectionEditor = new AnchorSelectionEditor();
	private final HandShapeSelectionEditor handShapeSelectionEditor = new HandShapeSelectionEditor(this);

	public CurrentSelectionEditor() {
		super(25);

		setOpaque(true);
		setBackground(ColorLabel.BASE_BG_2.color());
	}

	public void init(final ChartData data, final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.selectionManager = selectionManager;

		anchorSelectionEditor.init(this, selectionManager, undoSystem);
		handShapeSelectionEditor.init(data, selectionManager, undoSystem);
	}

	private void hideAllfieldsExcept(final PositionType type) {
		if (type != ANCHOR) {
			anchorSelectionEditor.hideFields();
		} else {
			anchorSelectionEditor.showFields();
		}

		if (type != HAND_SHAPE) {
			handShapeSelectionEditor.hideFields();
		} else {
			handShapeSelectionEditor.showFields();
		}
	}

	@SuppressWarnings("unchecked")
	public void selectionChanged() {
		final SelectionAccessor<IPosition> selected = selectionManager.getCurrentlySelectedAccessor();
		if (selected == null || !selected.isSelected()) {
			hideAllfieldsExcept(NONE);
		}

		hideAllfieldsExcept(selected.type);

		if (selected.type == ANCHOR) {
			final SelectionAccessor<Anchor> selectedAnchorsAccessor = (SelectionAccessor<Anchor>) (SelectionAccessor<?>) selected;
			anchorSelectionEditor.selectionChanged(selectedAnchorsAccessor);
		}
		if (selected.type == HAND_SHAPE) {
			final SelectionAccessor<HandShape> selectedAnchorsAccessor = (SelectionAccessor<HandShape>) (SelectionAccessor<?>) selected;
			handShapeSelectionEditor.selectionChanged(selectedAnchorsAccessor);
		}

		repaint();
	}

}
