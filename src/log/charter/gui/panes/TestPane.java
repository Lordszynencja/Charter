package log.charter.gui.panes;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.util.CollectionUtils.ArrayList2;

public class TestPane extends ParamsPane {
	private static final long serialVersionUID = 1L;

	public TestPane(final CharterFrame frame) {
		super(frame, "test", 10);

		final ArrayList2<String> vals = new ArrayList2<>("A", "B", "", "ABC");
		vals.sort(null);
//		final AutocompleteInput input = new AutocompleteInput(this, 50, vals, true);
//		this.add(input, 20, 40, 100, 20);

		addDefaultFinish(9, this::dispose);

		getRootPane().registerKeyboardAction(e -> {
			dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

}
