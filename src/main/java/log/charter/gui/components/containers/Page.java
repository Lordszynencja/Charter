package log.charter.gui.components.containers;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.utils.RowedPosition;

public interface Page {
	void init(RowedPanel panel, RowedPosition position);

	Label label();

	void setVisible(boolean visibility);
}
