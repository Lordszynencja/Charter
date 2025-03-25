package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;

import java.util.Map;

import javax.swing.JFrame;

import log.charter.data.config.values.accessors.ValueAccessor;

public class WindowStateConfig {
	public static int x = 100;
	public static int y = 100;
	public static int width = 1200;
	public static int height = 700;
	public static int extendedState = JFrame.NORMAL;

	public static int previewX = 200;
	public static int previewY = 200;
	public static int previewWidth = 1200;
	public static int previewHeight = 700;
	public static int previewExtendedState = JFrame.MAXIMIZED_BOTH;
	public static boolean previewBorderless = false;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".x", forInteger(v -> x = v, () -> x, x));
		valueAccessors.put(name + ".y", forInteger(v -> y = v, () -> y, y));
		valueAccessors.put(name + ".width", forInteger(v -> width = v, () -> width, width));
		valueAccessors.put(name + ".height", forInteger(v -> height = v, () -> height, height));
		valueAccessors.put(name + ".extendedState",
				forInteger(v -> extendedState = v, () -> extendedState, extendedState));

		valueAccessors.put(name + ".previewX", forInteger(v -> previewX = v, () -> previewX, previewX));
		valueAccessors.put(name + ".previewY", forInteger(v -> previewY = v, () -> previewY, previewY));
		valueAccessors.put(name + ".previewWidth", forInteger(v -> previewWidth = v, () -> previewWidth, previewWidth));
		valueAccessors.put(name + ".previewHeight",
				forInteger(v -> previewHeight = v, () -> previewHeight, previewHeight));
		valueAccessors.put(name + ".previewExtendedState",
				forInteger(v -> previewExtendedState = v, () -> previewExtendedState, previewExtendedState));
		valueAccessors.put(name + ".previewBorderless",
				forBoolean(v -> previewBorderless = v, () -> previewBorderless, previewBorderless));
	}

}
