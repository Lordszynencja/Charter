package log.charter.gui.components.containers;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
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
		final JToggleButton themeConfigSwitch = new JToggleButton(page.label().label());
		themeConfigSwitch.addActionListener(e -> setCurrentPage(page));
		buttonGroup.add(themeConfigSwitch);
		panel.addWithSettingSize(themeConfigSwitch, position.getAndAddX(120), position.y(), 100, 20);

		page.init(panel, position.copy().newRows(2).startFromHere());

		pages.add(page);
	}

	public PagedDialog(final CharterFrame frame, final Label title, final Page... pages) {
		this(frame, title, asList(pages));
	}

	public PagedDialog(final CharterFrame frame, final Label title, final List<Page> pages) {
		super(frame, title);

		final ButtonGroup buttonGroup = new ButtonGroup();

		final RowedPosition position = new RowedPosition(10, panel.sizes);
		for (final Page page : pages) {
			if (page != null) {
				addPage(position, page, buttonGroup);
			}
		}
		panel.resizeToFit(position.x() - 10, position.y());

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
