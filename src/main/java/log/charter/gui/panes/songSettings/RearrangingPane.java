package log.charter.gui.panes.songSettings;

import static log.charter.data.config.GraphicalConfig.inputSize;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.services.editModes.ModeManager;
import log.charter.util.collections.Pair;

public class RearrangingPane<T> extends RowedDialog {
	private static final long serialVersionUID = 1L;

	private final ModeManager modeManager;

	private final List<T> elements;

	private final List<Integer> indexes;

	public RearrangingPane(final CharterFrame charterFrame, final ModeManager modeManager, final Label title,
			final List<T> elements, final List<String> names) {
		super(charterFrame, title);

		this.modeManager = modeManager;

		this.elements = elements;

		indexes = new ArrayList<>(names.size());
		final DefaultListModel<String> model = new DefaultListModel<>();
		for (int i = 0; i < names.size(); i++) {
			indexes.add(i);
			model.addElement(names.get(i));
		}

		final JList<String> orderList = new JList<>(model);
		orderList.setFixedCellHeight(panel.sizes.rowHeight);
		ComponentUtils.setDefaultFontSize(orderList);

		orderList.setDragEnabled(true);
		orderList.setDropMode(DropMode.ON);

		orderList.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			private Integer indexFrom = 0;

			@Override
			public int getSourceActions(final JComponent comp) {
				return MOVE;
			}

			@Override
			public Transferable createTransferable(final JComponent comp) {
				indexFrom = orderList.getSelectedIndex();
				return new StringSelection(model.get(indexFrom));
			}

			@Override
			public void exportDone(final JComponent comp, final Transferable trans, final int action) {
				indexFrom = null;
			}

			@Override
			public boolean canImport(final TransferSupport support) {
				return indexFrom != null;
			}

			@Override
			public boolean importData(final TransferSupport support) {
				final int indexTo = ((JList.DropLocation) support.getDropLocation()).getIndex();

				final Pair<Integer, String> a = new Pair<>(indexes.get(indexFrom), model.get(indexFrom));
				final Pair<Integer, String> b = new Pair<>(indexes.get(indexTo), model.get(indexTo));

				indexes.set(indexFrom, b.a);
				model.set(indexFrom, b.b);
				indexes.set(indexTo, a.a);
				model.set(indexTo, a.b);

				return true;
			}
		});

		final int width = inputSize * 20;
		final int height = panel.sizes.rowHeight * names.size();

		panel.addWithSettingSize(orderList, inputSize / 2, inputSize / 2, width, height);

		addDefaultFinish(height + inputSize * 3 / 2, SaverWithStatus.defaultFor(this::onSave), null, true);
	}

	private void onSave() {
		final List<T> elementsCopy = new ArrayList<>(elements);
		for (int i = 0; i < indexes.size(); i++) {
			elements.set(i, elementsCopy.get(indexes.get(i)));
		}

		modeManager.reloadCurrent();
	}
}
