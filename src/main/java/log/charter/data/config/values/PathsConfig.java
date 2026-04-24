package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.StringValueAccessor.forString;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import log.charter.data.config.values.accessors.ValueAccessor;

public class PathsConfig {
	private static String lastDir = "";
	private static String lastPath = "";
	public static String musicPath = System.getProperty("user.home") + File.separator + "Music";
	public static String songsPath = System.getProperty("user.home") + File.separator + "Documents";
	public static String gpFilesPath = songsPath;
	public static List<String> lastPaths = new ArrayList<>();

	public static String lastDir() {
		return lastDir;
	}

	public static String lastPath() {
		return lastPath;
	}

	public static void lastPath(final String dir, final String file) {
		lastDir = dir;
		final String path = new File(dir, file).getAbsolutePath();
		lastPath = path;

		lastPaths.removeIf(p -> p.equals(path) || !new File(p).exists());
		lastPaths.add(0, path);

		while (lastPaths.size() > 20) {
			lastPaths.remove(lastPaths.size() - 1);
		}
	}

	private static void lastPaths(final String lastPathsString) {
		lastPaths.clear();
		for (final String path : lastPathsString.split(";")) {
			if (!new File(path).exists()) {
				continue;
			}

			lastPaths.add(path);
		}
	}

	private static String lastPaths() {
		return String.join(";", lastPaths);
	}

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".lastDir", forString(v -> lastDir = v, () -> lastDir, lastDir));
		valueAccessors.put(name + ".lastPath", forString(v -> lastPath = v, () -> lastPath, lastPath));
		valueAccessors.put(name + ".musicPath", forString(v -> musicPath = v, () -> musicPath, musicPath));
		valueAccessors.put(name + ".songsPath", forString(v -> songsPath = v, () -> songsPath, songsPath));
		valueAccessors.put(name + ".gpFilesPath", forString(v -> gpFilesPath = v, () -> gpFilesPath, gpFilesPath));
		valueAccessors.put(name + ".lastPaths", forString(PathsConfig::lastPaths, PathsConfig::lastPaths, ""));
	}
}
