package log.charter.gui.components.containers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.RowedPosition;

public abstract class PagedDialog extends RowedDialog {
	private static final long serialVersionUID = -3193534671039163160L;

	private final List<Page> pages = new ArrayList<>();

	private void addPage(final RowedPosition position, final Page page, final ButtonGroup buttonGroup) {
		final JToggleButton themeConfigSwitch = new JToggleButton(page.label().label());
		themeConfigSwitch.addActionListener(e -> {
			pages.forEach(Page::hide);
			page.show();
		});
		buttonGroup.add(themeConfigSwitch);
		panel.addWithSettingSize(themeConfigSwitch, position.getAndAddX(120), position.getY(), 100, 20);

		page.init(panel, position.copy().newRows(2).startFromHere());

		pages.add(page);
	}

	public PagedDialog(final CharterFrame frame, final Label title, final Page... pages) {
		super(frame, title);

		final ButtonGroup buttonGroup = new ButtonGroup();

		final RowedPosition position = new RowedPosition(10, panel.sizes);
		for (final Page page : pages) {
			addPage(position, page, buttonGroup);
		}
		panel.resizeToFit(position.getX() - 10, position.getY());

		while (position.getY() < panel.getHeight()) {
			position.newRow();
		}

		this.pages.forEach(Page::hide);
		if (!this.pages.isEmpty()) {
			this.pages.get(0).show();
		}

		addDefaultFinish(position.getY(), this::save, this::cancel, false);
	}

	abstract protected boolean save();

	abstract protected boolean cancel();
}
