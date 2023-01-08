package log.charter.gui.panes;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.util.CollectionUtils.ArrayList2;

public class TestPane extends ParamsPane {
	private static final long serialVersionUID = 1L;

	public TestPane(final CharterFrame frame) {
		super(frame, Label.ACCENT, 10);

		final ArrayList2<String> vals = new ArrayList2<>("A", "B", "", "ABC");
		vals.sort(null);
//		final AutocompleteInput input = new AutocompleteInput(this, 50, vals, true);
//		this.add(input, 20, 40, 100, 20);

		addDefaultFinish(9, this::dispose);
	}

}
