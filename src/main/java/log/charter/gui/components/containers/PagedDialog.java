package log.charter.gui.components.containers;

import static java.util.Arrays.asList;
import static log.charter.data.config.GraphicalConfig.inputSize;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.RowedPosition;

public abstract class PagedDialog extends RowedDialog {
	private static final long serialVersionUID = -3193534671039163160L;

	private Page currentPage = null;

	private final List<Page> pages = new ArrayList<>();

	private void setCurrentPage(final Page page) {
		if (page == currentPage) {
			return;
		}

		if (currentPage != null) {
			currentPage.setVisible(false);
		}
		page.setVisible(true);
		currentPage = page;
	}

	private void addPage(final RowedPosition position, final Page page, final ButtonGroup buttonGroup) {
		final JToggleButton pageButton = new JToggleButton(page.label().label());
		pageButton.addActionListener(e -> setCurrentPage(page));
		ComponentUtils.setDefaultFontSize(pageButton);

		buttonGroup.add(pageButton);
		panel.addWithSettingSize(pageButton, position.getAndAddX(inputSize * 6), position.y(), inputSize * 5,
				inputSize);

		page.init(panel, position.copy().newRows(2).startFromHere());

		pages.add(page);
	}

	public PagedDialog(final CharterFrame frame, final Label title, final Page... pages) {
		this(frame, title, asList(pages));
	}

	public PagedDialog(final CharterFrame frame, final Label title, final List<Page> pages) {
		super(frame, title);

		final ButtonGroup buttonGroup = new ButtonGroup();

		final RowedPosition position = new RowedPosition(inputSize / 2, panel.sizes);
		for (final Page page : pages) {
			if (page != null) {
				addPage(position, page, buttonGroup);
			}
		}
		panel.resizeToFit(position.x() - inputSize / 2, position.y());

		while (position.y() < panel.getHeight()) {
			position.newRow();
		}

		this.pages.forEach(p -> p.setVisible(false));
		if (!this.pages.isEmpty()) {
			buttonGroup.getElements().nextElement().setSelected(true);
			setCurrentPage(this.pages.get(0));
		}

		addDefaultFinish(position.y(), this::save, this::cancel, false);
	}

	abstract protected boolean save();

	abstract protected boolean cancel();
}
