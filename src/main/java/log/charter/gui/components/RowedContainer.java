package log.charter.gui.components;

import java.awt.Component;

import log.charter.gui.components.ComponentWithFields.PaneSizes;
import log.charter.util.CollectionUtils.ArrayList2;

public interface RowedContainer {
	ArrayList2<Component> components();

	PaneSizes sizes();
}