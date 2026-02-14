package log.charter.gui.components.tabs.errorsTab;

import static log.charter.util.FileUtils.imagesFolder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.CharterTabbedPane.Tab;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.util.ImageUtils;

public class ErrorsTab extends RowedPanel implements ComponentListener {
	private static final long serialVersionUID = 1L;

	private static final Icon warningIcon;
	static {
		final BufferedImage warningImage = ImageUtils.loadSafeFromDir(imagesFolder, "/warning.png");
		final BufferedImage iconImage = new BufferedImage(12, 12, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g = iconImage.getGraphics();
		((Graphics2D) g).addRenderingHints(Map.of(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
				RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		if (warningImage != null) {
			g.drawImage(warningImage, 0, 0, iconImage.getWidth(), iconImage.getHeight(), null);
		} else {
			g.setColor(Color.RED);
			g.fillPolygon(new int[] { 1, 7, 4 }, new int[] { 7, 7, 1 }, 3);
		}
		warningIcon = new ImageIcon(iconImage);
	}

	private Tab tab;

	private DefaultTableModel tableModel;
	private JTable errorsTable;
	private CharterScrollPane scrollTable;

	private List<ChartError> errorsBuffer = new ArrayList<>();
	private List<ChartError> internalBuffer = new ArrayList<>();
	private final List<ChartError> errors = new ArrayList<>();

	public ErrorsTab() {
		super(new PaneSizesBuilder(0).build());

		final RowedPosition position = new RowedPosition(20, sizes);

		createTable(position);

		addComponentListener(this);
	}

	public void setTab(final Tab tab) {
		this.tab = tab;
	}

	private void addColumnsToTable() {
		tableModel.addColumn(Label.ERRORS_TAB_POSITION.label());
		tableModel.addColumn(Label.ERRORS_TAB_SEVERITY.label());
		tableModel.addColumn(Label.ERRORS_TAB_DESCRIPTION.label());
	}

	private void setColumnSizes() {
		errorsTable.setRowHeight(20);
		errorsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		errorsTable.getColumnModel().getColumn(0).setMinWidth(200);
		errorsTable.getColumnModel().getColumn(0).setMaxWidth(200);
		errorsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		errorsTable.getColumnModel().getColumn(1).setMinWidth(100);
		errorsTable.getColumnModel().getColumn(1).setMaxWidth(100);
		errorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	}

	private void setTableColors() {
		errorsTable.setOpaque(true);
		errorsTable.getTableHeader().setOpaque(false);
		errorsTable.getTableHeader().setBackground(ColorLabel.BASE_BG_3.color());
		errorsTable.setBackground(ColorLabel.BASE_BG_3.color());
		errorsTable.setForeground(ColorLabel.BASE_TEXT.color());
		errorsTable.setGridColor(ColorLabel.BASE_BG_4.color());
	}

	private void createTable(final RowedPosition position) {
		tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 8030767262155998676L;

			@Override
			public boolean isCellEditable(final int row, final int column) {
				return false;
			}
		};

		addColumnsToTable();

		errorsTable = new JTable(tableModel);
		errorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnSizes();
		setTableColors();

		errorsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					final int row = errorsTable.rowAtPoint(mouseEvent.getPoint());
					errors.get(row).position.goTo();
				}
			}
		});

		scrollTable = new CharterScrollPane(errorsTable);
		scrollTable.setMinimumSize(new Dimension(300, 80));
		this.add(scrollTable, position);
	}

	private void clearErrors() {
		if (errorsTable.getCellEditor() != null) {
			errorsTable.getCellEditor().cancelCellEditing();
		}
		errorsTable.clearSelection();

		tableModel.setRowCount(0);
		errors.clear();
	}

	public void addError(final ChartError chartError) {
		errorsBuffer.add(chartError);
	}

	public void swapBuffer() {
		internalBuffer = errorsBuffer;
		errorsBuffer = new ArrayList<>();

		if (tab != null) {
			final boolean hasErrors = internalBuffer.stream()
					.anyMatch(error -> error.severity == ChartErrorSeverity.ERROR);
			if (!hasErrors) {
				tab.clearTextColorOVerride();
				tab.icon = null;
			} else {
				tab.setTextColorOverride(Color.RED);
				tab.icon = warningIcon;
			}
		}
	}

	private void swapInternalBuffer() {
		final int selected = errorsTable.getSelectedRow();
		final boolean reselect = internalBuffer.size() == errors.size();
		clearErrors();

		for (final ChartError chartError : internalBuffer) {
			final Vector<Object> row = new Vector<>();
			row.add(chartError.position.description);
			row.add(chartError.severity.name());
			row.add(chartError.message);

			tableModel.addRow(row);
			errors.add(chartError);
		}

		if (reselect && selected != -1) {
			errorsTable.addRowSelectionInterval(selected, selected);
		}
	}

	@Override
	public void paint(final Graphics g) {
		swapInternalBuffer();
		super.paint(g);
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		scrollTable.setSize(getWidth() - 40, getHeight() - 40);

		repaint();
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
