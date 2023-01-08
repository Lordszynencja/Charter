package log.charter.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;

public class FileChooseUtils {
	private static File showDialog(final CharterFrame frame, final JFileChooser chooser) {

		final int chosenOption = chooser.showOpenDialog(frame);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFile();
	}

	public static File chooseMusicFile(final CharterFrame frame, final String startingDir) {
		final JFileChooser chooser = new JFileChooser(new File(startingDir));
		chooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				final String name = f.getName().toLowerCase();
				return f.isDirectory() || name.endsWith(".mp3") || name.endsWith(".ogg");
			}

			@Override
			public String getDescription() {
				return Label.MP3_OR_OGG_FILE.label();
			}
		});

		return showDialog(frame, chooser);
	}

	public static File chooseFile(final CharterFrame frame, final String startingDir, final String[] extensions,
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

		return showDialog(frame, chooser);
	}

	public static File chooseDirectory(final CharterFrame frame, final String startingPath) {
		final JFileChooser chooser = new JFileChooser(new File(startingPath));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int chosenOption = chooser.showOpenDialog(frame);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return chooser.getSelectedFile();
	}
}
