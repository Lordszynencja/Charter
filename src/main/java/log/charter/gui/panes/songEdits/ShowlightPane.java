package log.charter.gui.panes.songEdits;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.CharterSelect.ItemHolder;

public class ShowlightPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	public static List<ShowlightType> getAvailableShowlightTypes() {
		final List<ShowlightType> availableTypes = new ArrayList<>();
		availableTypes.add(null);
		for (final ShowlightType type : ShowlightType.values()) {
			availableTypes.add(type);
		}

		availableTypes.sort((a, b) -> a == null ? -1 : b == null ? 1 : a.label.label().compareTo(b.label.label()));

		return availableTypes;
	}

	private final ChartData data;
	private final UndoSystem undoSystem;

	private DefaultTableModel tableModel;
	private JTable showlightTypesTable;

	private final Showlight showlight;

	public ShowlightPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final Showlight showlight, final Runnable onCancel) {
		super(frame, Label.GUITAR_BEAT_PANE, 400);
		this.data = data;
		this.undoSystem = undoSystem;

		this.showlight = showlight;

		final AtomicInteger row = new AtomicInteger(0);
		prepareShowlightTypesList(row);

		setOnFinish(this::saveAndExit, onCancel);
		addDefaultFinish(row.incrementAndGet());
	}

	private void prepareShowlightTypesTable(final AtomicInteger row) {
		final List<ShowlightType> availableTypes = getAvailableShowlightTypes();

		final CharterSelect<ShowlightType> input = new CharterSelect<>(availableTypes, null,
				e -> e == null ? "" : e.label.label(), null);
		input.setMinimumSize(new Dimension(100, 20));
		input.setMaximumRowCount(15);

		tableModel = new DefaultTableModel();
		tableModel.setRowCount(showlight.types.size());
		tableModel.setColumnCount(1);
		showlightTypesTable = new JTable(tableModel);
		showlightTypesTable.setShowGrid(false);
		showlightTypesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		showlightTypesTable.setRowHeight(20);
		showlightTypesTable.setTableHeader(new JTableHeader());

		final TableColumn column = showlightTypesTable.getColumnModel().getColumn(0);
		column.setCellEditor(new DefaultCellEditor(input));

		final CharterScrollPane scrollTable = new CharterScrollPane(showlightTypesTable);
		scrollTable.setColumnHeader(null);
		scrollTable.setMinimumSize(new Dimension(100, 80));

		for (int tableRow = 0; tableRow < showlight.types.size(); tableRow++) {
			final ShowlightType event = showlight.types.get(tableRow);
			final ItemHolder<ShowlightType> value = new ItemHolder<>(event, event.label.label());
			tableModel.setValueAt(value, tableRow, 0);
		}

		this.add(scrollTable, 50, getY(row.getAndIncrement()), 170, 120);
	}

	private void addAddRowButton(final AtomicInteger row) {
		final JButton rowAddButton = new JButton(Label.SHOWLIGHT_ADD.label());
		rowAddButton.addActionListener(e -> {
			tableModel.addRow(new Vector<Object>(1));
			rowAddButton.setEnabled(tableModel.getRowCount() < 3);
		});
		rowAddButton.setEnabled(tableModel.getRowCount() < 3);
		this.add(rowAddButton, 230, getY(row.getAndAdd(2)), 150, 20);
	}

	private void addRemoveRowButton(final AtomicInteger row) {
		final JButton rowRemoveButton = new JButton(Label.SHOWLIGHT_REMOVE.label());
		rowRemoveButton.setEnabled(tableModel.getRowCount() > 0);
		rowRemoveButton.addActionListener(e -> {
			int rowToRemove = showlightTypesTable.getEditingRow();
			if (rowToRemove == -1) {
				rowToRemove = showlightTypesTable.getSelectedRow();
				if (rowToRemove == -1) {
					tableModel.removeRow(tableModel.getRowCount() - 1);
					return;
				}
			}

			if (showlightTypesTable.getCellEditor() != null) {
				showlightTypesTable.getCellEditor().cancelCellEditing();
			}
			showlightTypesTable.clearSelection();
			tableModel.removeRow(rowToRemove);
			rowRemoveButton.setEnabled(tableModel.getRowCount() > 0);
		});

		this.add(rowRemoveButton, 230, getY(row.getAndAdd(2)), 150, 20);
	}

	private void prepareShowlightTypesList(final AtomicInteger row) {
		prepareShowlightTypesTable(row);
		addAddRowButton(row);
		addRemoveRowButton(row);
	}

	@SuppressWarnings("unchecked")
	private boolean hasValues() {
		if (showlightTypesTable.getRowCount() == 0) {
			return false;
		}

		for (int row = 0; row < showlightTypesTable.getRowCount(); row++) {
			final ItemHolder<ShowlightType> value = (ItemHolder<ShowlightType>) showlightTypesTable.getValueAt(row, 0);
			if (value != null && value.item != null) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private void saveAndExit() {
		undoSystem.addUndo();

		if (!hasValues()) {
			data.showlights().remove(showlight);
			return;
		}

		showlight.types.clear();
		for (int row = 0; row < showlightTypesTable.getRowCount(); row++) {
			final ItemHolder<ShowlightType> value = (ItemHolder<ShowlightType>) showlightTypesTable.getValueAt(row, 0);
			if (value != null && value.item != null) {
				showlight.types.add(value.item);
			}
		}
	}
}
