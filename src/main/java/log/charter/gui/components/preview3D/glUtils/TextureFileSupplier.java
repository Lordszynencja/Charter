package log.charter.gui.components.preview3D.glUtils;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextureFileSupplier {
	private final String dir;
	private final Supplier<String> nameSupplier;
	private final Function<String, String> fileNameFunction;

	public TextureFileSupplier(final String dir, final Supplier<String> nameSupplier,
			final Function<String, String> fileNameFunction) {
		this.dir = dir;
		this.nameSupplier = nameSupplier;
		this.fileNameFunction = fileNameFunction;
	}

	public File getFile() {
		final File f = new File(dir + fileNameFunction.apply(nameSupplier.get()));
		if (f.exists()) {
			return f;
		}

		return new File(dir + fileNameFunction.apply("default"));
	}
}