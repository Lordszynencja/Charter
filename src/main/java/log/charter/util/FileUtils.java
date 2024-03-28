package log.charter.util;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
	public static final String graphicsFolder = "graphics/";
	public static final String colorSetsFolder = graphicsFolder + "colorSets/";
	public static final String imagesFolder = graphicsFolder + "images/";
	public static final String texturesFolder = graphicsFolder + "textures/";
	public static final String inlaysFolder = graphicsFolder + "inlays/";

	public static List<String> listDirectories(final String dir) {
		return Stream.of(Objects.requireNonNull(new File(dir).listFiles(File::isDirectory)))//
				.map(File::getName)//
				.sorted()//
				.collect(Collectors.toList());
	}

	public static Stream<String> listFiles(final String dir) {
		return Stream.of(Objects.requireNonNull(new File(dir).listFiles(File::isFile)))//
				.map(File::getName)//
				.sorted();
	}

	public static Stream<String> listFiles(final String dir, final Predicate<File> filter) {
		if (filter == null) {
			return listFiles(dir);
		}

		return Stream.of(Objects.requireNonNull(new File(dir).listFiles(File::isFile)))//
				.filter(filter)//
				.map(File::getName)//
				.sorted();
	}

	public static String cleanFileName(final String fileName) {
		return fileName.replaceAll("[^a-zA-Z0-9_\\- ]", "");
	}
}
