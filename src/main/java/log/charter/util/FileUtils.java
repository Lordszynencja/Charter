package log.charter.util;

import java.io.File;
import java.util.List;
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
		return Stream.of(new File(dir).listFiles(f -> f.isDirectory()))//
				.map(file -> file.getName())//
				.sorted()//
				.collect(Collectors.toList());
	}

	public static Stream<String> listFiles(final String dir) {
		return Stream.of(new File(dir).listFiles(f -> f.isFile()))//
				.map(file -> file.getName())//
				.sorted();
	}

	public static Stream<String> listFiles(final String dir, final Predicate<File> filter) {
		if (filter == null) {
			return listFiles(dir);
		}

		return Stream.of(new File(dir).listFiles(f -> f.isFile()))//
				.filter(filter)//
				.map(file -> file.getName())//
				.sorted();
	}
}
