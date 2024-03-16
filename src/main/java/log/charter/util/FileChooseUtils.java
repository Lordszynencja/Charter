package log.charter.util;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import log.charter.data.config.Localization.Label;
import log.charter.sound.SoundFileType;

public class FileChooseUtils {
	private static String extension(final String fileName) {
		final int dotIndex = fileName.lastIndexOf('.');
		return fileName.substring(dotIndex + 1).toLowerCase();
	}

	private static File showDialog(final Component parent, final JFileChooser chooser) {
		final int chosenOption = chooser.showOpenDialog(parent);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFile();
	}

	public static File chooseMusicFile(final Component parent, final String startingDir) {
		final JFileChooser chooser = new JFileChooser(new File(startingDir));
		chooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				if (SoundFileType.fromExtension(extension(f.getName())) != null) {
					return true;
				}

				return f.isDirectory();
			}

			@Override
			public String getDescription() {
				return Label.SUPPORTED_MUSIC_FILE.label();
			}
		});

		final File file = showDialog(parent, chooser);
		if (SoundFileType.fromExtension(extension(file.getName())) == null) {
			showPopup(parent, Label.UNSUPPORTED_MUSIC_FORMAT);
			return null;
		}

		return file;
	}

	public static File chooseFile(final Component parent, final String startingDir, final String[] extensions,
			final String description) {
		final JFileChooser chooser = new JFileChooser(new File(startingDir));
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				if (f.isDirectory()) {
					return true;
				}

				for (final String extension : extensions) {
					if (f.getName().toLowerCase().endsWith(extension)) {
						return true;
					}
				}

				return false;
			}

			@Override
			public String getDescription() {
				return description;
			}
		});

		return showDialog(parent, chooser);
	}

	public static File chooseFile(final Component parent, final String startingDir, final String[] extensions,
			final String[] descriptions) {
		final JFileChooser chooser = new JFileChooser(new File(startingDir));
		chooser.setAcceptAllFileFilterUsed(false);

		for (int i = 0; i < extensions.length; i++) {
			final String extension = extensions[i];
			final String description = descriptions[i];
			chooser.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(final File f) {
					if (f.isDirectory()) {
						return true;
					}

					return f.getName().toLowerCase().endsWith(extension);
				}

				@Override
				public String getDescription() {
					return description;
				}
			});
		}

		return showDialog(parent, chooser);
	}

	public static File chooseDirectory(final Component parent, final String startingPath) {
		final JFileChooser chooser = new JFileChooser(new File(startingPath));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int chosenOption = chooser.showOpenDialog(parent);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return chooser.getSelectedFile();
	}
}
