package log.charter.gui.components.tabs.selectionEditor;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.types.PositionType;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.CharterSelect.ItemHolder;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.panes.songEdits.ShowlightPane;

public class ShowlightSelectionEditor extends SelectionEditorPart<Showlight> {
	private DefaultTableModel tableModel;
	private JTable showlightTypesTable;
	private CharterScrollPane eventsTableScroll;
	private JButton addShowlightButton;
	private JButton removeShowlightButton;

	private boolean settingData = false;

	public ShowlightSelectionEditor() {
		super(PositionType.SHOWLIGHT);
	}

	@Override
	public void addTo(final CurrentSelectionEditor currentSelectionEditor) {
		final RowedPosition position = new RowedPosition(10, currentSelectionEditor.sizes);

		addShowlightTypes(currentSelectionEditor, position);
		addAddShowlightButton(currentSelectionEditor, position.copy());
		addRemoveEventButton(currentSelectionEditor, position.newRowsInPlace(2));
	}

	@SuppressWarnings("unchecked")
	private List<ShowlightType> readShowlightTypesFromTable() {
		final List<ShowlightType> types = new ArrayList<>();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			final ItemHolder<ShowlightType> eventType = (ItemHolder<ShowlightType>) showlightTypesTable.getValueAt(i,
					0);
			if (eventType != null && eventType.item != null) {
				types.add(eventType.item);
			}
		}

		return types;
	}

	private void onShowlightTypesChange() {
		if (settingData) {
			return;
		}

		addUndo();

		final List<ShowlightType> types = readShowlightTypesFromTable();

		for (final Showlight showlight : getItems()) {
			showlight.types.clear();
			showlight.types.addAll(types);
		}
	}

	private void addShowlightTypes(final CurrentSelectionEditor currentSelectionEditor, final RowedPosition position) {
		final List<ShowlightType> showlightTypes = ShowlightPane.getAvailableShowlightTypes();

		final CharterSelect<ShowlightType> input = new CharterSelect<>(showlightTypes, null, null,
				v -> onShowlightTypesChange());
		input.setMinimumSize(new Dimension(100, 20));
		input.setMaximumRowCount(15);

		tableModel = new DefaultTableModel();
		tableModel.setRowCount(0);
		tableModel.setColumnCount(1);
		showlightTypesTable = new JTable(tableModel);
		showlightTypesTable.setShowGrid(false);
		showlightTypesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		showlightTypesTable.setRowHeight(20);
		showlightTypesTable.setTableHeader(new JTableHeader());

		final TableColumn column = showlightTypesTable.getColumnModel().getColumn(0);
		column.setCellEditor(new DefaultCellEditor(input));

		eventsTableScroll = new CharterScrollPane(showlightTypesTable);
		eventsTableScroll.setColumnHeader(null);

		currentSelectionEditor.addWithSettingSize(eventsTableScroll, position, 300, 20, 200);
	}

	private void addAddShowlightButton(final CurrentSelectionEditor currentSelectionEditor,
			final RowedPosition position) {
		addShowlightButton = new JButton(Label.SHOWLIGHT_ADD.label());
		addShowlightButton.addActionListener(e -> {
			tableModel.addRow(new Vector<Object>(1));
			addShowlightButton.setEnabled(tableModel.getRowCount() < 3);
		});

		currentSelectionEditor.addWithSettingSize(addShowlightButton, position, 150, 10, 30);
	}

	private int getRowToRemove() {
		int rowToRemove = showlightTypesTable.getEditingRow();
		if (rowToRemove >= 0) {
			return rowToRemove;
		}

		rowToRemove = showlightTypesTable.getSelectedRow();
		if (rowToRemove >= 0) {
			return rowToRemove;
		}

		return tableModel.getRowCount() - 1;
	}

	private void addRemoveEventButton(final CurrentSelectionEditor currentSelectionEditor,
			final RowedPosition position) {
		removeShowlightButton = new JButton(Label.GUITAR_BEAT_PANE_EVENT_REMOVE.label());
		removeShowlightButton.addActionListener(e -> {
			final int rowToRemove = getRowToRemove();

			if (showlightTypesTable.getCellEditor() != null) {
				showlightTypesTable.getCellEditor().cancelCellEditing();
			}
			showlightTypesTable.clearSelection();
			tableModel.removeRow(rowToRemove);
			onShowlightTypesChange();
			removeShowlightButton.setEnabled(tableModel.getRowCount() > 0);
		});

		currentSelectionEditor.addWithSettingSize(removeShowlightButton, position, 150, 10, 30);
	}

	@Override
	public void show(final boolean visibility) {
		eventsTableScroll.setVisible(visibility);
		addShowlightButton.setVisible(visibility);
		removeShowlightButton.setVisible(visibility);
	}

	private void setShowlightTypes(final List<ShowlightType> events) {
		tableModel.setRowCount(events.size());
		for (int i = 0; i < events.size(); i++) {
			final ShowlightType event = events.get(i);
			final ItemHolder<ShowlightType> value = new ItemHolder<>(event, event.label.label());
			tableModel.setValueAt(value, i, 0);
		}
	}

	@Override
	public void selectionChanged() {
		super.selectionChanged();

		final List<Showlight> items = getItems();

		settingData = true;

		if (items.size() == 1) {
			setShowlightTypes(items.get(0).types);
		} else {
			setShowlightTypes(new ArrayList<>());
		}
		addShowlightButton.setEnabled(tableModel.getRowCount() < 3);
		removeShowlightButton.setEnabled(tableModel.getRowCount() > 0);

		settingData = false;
	}

}
