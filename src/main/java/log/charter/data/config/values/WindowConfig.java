package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forBoolean;
import static log.charter.data.config.values.ValueAccessor.forInteger;

import java.util.Map;

import javax.swing.JFrame;

public class WindowConfig implements ConfigValue {

	public int x = 100;
	public int y = 100;
	public int width = 1200;
	public int height = 700;
	public int extendedState = JFrame.NORMAL;

	public int previewX = 200;
	public int previewY = 200;
	public int previewWidth = 1200;
	public int previewHeight = 700;
	public int previewExtendedState = JFrame.NORMAL;
	public boolean previewBorderless = false;

	@Override
	public void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".x", forInteger(v -> x = v, () -> x));
		valueAccessors.put(name + ".y", forInteger(v -> y = v, () -> y));
		valueAccessors.put(name + ".width", forInteger(v -> width = v, () -> width));
		valueAccessors.put(name + ".height", forInteger(v -> height = v, () -> height));
		valueAccessors.put(name + ".extendedState", forInteger(v -> extendedState = v, () -> extendedState));

		valueAccessors.put(name + ".previewX", forInteger(v -> previewX = v, () -> previewX));
		valueAccessors.put(name + ".previewY", forInteger(v -> previewY = v, () -> previewY));
		valueAccessors.put(name + ".previewWidth", forInteger(v -> previewWidth = v, () -> previewWidth));
		valueAccessors.put(name + ".previewHeight", forInteger(v -> previewHeight = v, () -> previewHeight));
		valueAccessors.put(name + ".previewExtendedState",
				forInteger(v -> previewExtendedState = v, () -> previewExtendedState));
		valueAccessors.put(name + ".previewBorderless",
				forBoolean(v -> previewBorderless = v, () -> previewBorderless));
	}

}
