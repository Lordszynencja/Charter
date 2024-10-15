package log.charter.gui.components.tabs.errorsTab;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.services.data.ChartTimeHandler;

public class ErrorsTab extends RowedPanel implements ComponentListener {
	private static final long serialVersionUID = 1L;

	private ChartTimeHandler chartTimeHandler;

	private DefaultTableModel tableModel;
	private JTable errorsTable;
	private CharterScrollPane scrollTable;

	private final List<ChartError> errors = new ArrayList<>();

	public ErrorsTab() {
		super(new PaneSizesBuilder(0).build());

		final RowedPosition position = new RowedPosition(20, sizes);

		createTable(position);

		addComponentListener(this);
	}

	private void createTable(final RowedPosition position) {
		tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 8030767262155998676L;

			@Override
			public boolean isCellEditable(final int row, final int column) {
				return false;
			}
		};
		tableModel.addColumn(Label.ERRORS_TAB_POSITION.label());
		tableModel.addColumn(Label.ERRORS_TAB_SEVERITY.label());
		tableModel.addColumn(Label.ERRORS_TAB_DESCRIPTION.label());

		errorsTable = new JTable(tableModel);
		errorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		errorsTable.setRowHeight(20);
		errorsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		errorsTable.getColumnModel().getColumn(0).setMinWidth(200);
		errorsTable.getColumnModel().getColumn(0).setMaxWidth(200);
		errorsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		errorsTable.getColumnModel().getColumn(1).setMinWidth(100);
		errorsTable.getColumnModel().getColumn(1).setMaxWidth(100);
		errorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		errorsTable.setOpaque(true);
		errorsTable.getTableHeader().setOpaque(false);
		errorsTable.getTableHeader().setBackground(ColorLabel.BASE_BG_3.color());
		errorsTable.setBackground(ColorLabel.BASE_BG_3.color());
		errorsTable.setForeground(ColorLabel.BASE_TEXT.color());
		errorsTable.setGridColor(ColorLabel.BASE_BG_4.color());

		errorsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					final int row = errorsTable.rowAtPoint(mouseEvent.getPoint());
					errors.get(row).position.goTo(chartTimeHandler);
				}
			}
		});

		scrollTable = new CharterScrollPane(errorsTable);
		scrollTable.setMinimumSize(new Dimension(300, 80));
		this.add(scrollTable, position);
	}

	public void clearErrors() {
		if (errorsTable.getCellEditor() != null) {
			errorsTable.getCellEditor().cancelCellEditing();
		}
		errorsTable.clearSelection();

		tableModel.setRowCount(0);
		errors.clear();
	}

	public void addError(final ChartError chartError) {
		final Vector<Object> row = new Vector<>();
		row.add(chartError.position.description);
		row.add(chartError.severity.name());
		row.add(chartError.message);

		tableModel.addRow(row);
		errors.add(chartError);
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		scrollTable.setSize(getWidth() - 40, getHeight() - 40);
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}
}
