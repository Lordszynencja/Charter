package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forString;

import java.io.File;
import java.util.Map;

public class PathsConfig {
	public static String lastDir = "";
	public static String lastPath = "";
	public static String musicPath = System.getProperty("user.home") + File.separator + "Music";
	public static String songsPath = System.getProperty("user.home") + File.separator + "Documents";
	public static String gpFilesPath = songsPath;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".lastDir", forString(v -> lastDir = v, () -> lastDir));
		valueAccessors.put(name + ".lastPath", forString(v -> lastPath = v, () -> lastPath));
		valueAccessors.put(name + ".musicPath", forString(v -> musicPath = v, () -> musicPath));
		valueAccessors.put(name + ".songsPath", forString(v -> songsPath = v, () -> songsPath));
		valueAccessors.put(name + ".gpFilesPath", forString(v -> gpFilesPath = v, () -> gpFilesPath));
	}
}
