package log.charter.util;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
	private static final File graphicsFolder = new File(RW.getJarDirectory(), "graphics");
	public static final File colorSetsFolder = new File(graphicsFolder, "colorSets");
	public static final File imagesFolder = new File(graphicsFolder, "images");
	public static final File texturesFolder = new File(graphicsFolder, "textures");
	public static final File inlaysFolder = new File(graphicsFolder, "inlays");

	public static List<String> listDirectories(final File dir) {
		return Stream.of(dir.listFiles(f -> f.isDirectory()))//
				.map(file -> file.getName())//
				.sorted()//
				.collect(Collectors.toList());
	}

	public static Stream<String> listFiles(final File dir) {
		return Stream.of(dir.listFiles(f -> f.isFile()))//
				.map(file -> file.getName())//
				.sorted();
	}

	public static Stream<String> listFiles(final File dir, final Predicate<File> filter) {
		if (filter == null) {
			return listFiles(dir);
		}

		return Stream.of(dir.listFiles(f -> f.isFile()))//
				.filter(filter)//
				.map(file -> file.getName())//
				.sorted();
	}

	public static String cleanFileName(final String fileName) {
		return fileName.replaceAll("[^a-zA-Z0-9_\\- ]", "");
	}

	public static String getExtension(final File file) {
		final String name = file.getName();
		final int dotPosition = name.lastIndexOf('.');
		return dotPosition < 0 ? "" : name.substring(dotPosition + 1);
	}

	public static String getFileNameWithoutExtension(final File file) {
		final String name = file.getName();
		final int dotPosition = name.lastIndexOf('.');
		return dotPosition < 0 ? name : name.substring(0, dotPosition);
	}
}
