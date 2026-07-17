package log.charter.util;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalComboBoxButton;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.Localization.Label;
import log.charter.io.Logger;
import log.charter.sound.SoundFileType;
import log.charter.util.NativeFileDialogs.DialogFilter;

public class FileChooseUtils {
	private static String extension(final String fileName) {
		final int dotIndex = fileName.lastIndexOf('.');
		return fileName.substring(dotIndex + 1).toLowerCase();
	}

	// turns [".gp3", ".gp4"] into the "gp3,gp4" form the native dialog wants
	private static String nativeFilterSpec(final String... extensions) {
		final List<String> cleaned = new ArrayList<>();
		for (final String extension : extensions) {
			cleaned.add(extension.startsWith(".") ? extension.substring(1) : extension);
		}

		return String.join(",", cleaned);
	}

	private static File showDialog(final Component parent, final JFileChooser chooser) {
		final int chosenOption = chooser.showOpenDialog(parent);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFile();
	}

	public static File chooseMusicFile(final Component parent, final String startingDir) {
		final File file = chooseMusicFileInternal(parent, startingDir);
		if (file == null) {
			return null;
		}
		if (SoundFileType.fromExtension(extension(file.getName())) == null) {
			showPopup(parent, Label.UNSUPPORTED_MUSIC_FORMAT);
			return null;
		}

		return file;
	}

	private static File chooseMusicFileInternal(final Component parent, final String startingDir) {
		if (NativeFileDialogs.available()) {
			try {
				final List<String> extensions = new ArrayList<>();
				final List<DialogFilter> filters = new ArrayList<>();
				for (final SoundFileType type : SoundFileType.values()) {
					extensions.add(type.extension);
					filters.add(new DialogFilter(type.name, type.extension));
				}
				// combined filter goes first so every format shows up by default
				filters.add(0, new DialogFilter(Label.SUPPORTED_MUSIC_FILE.label(), String.join(",", extensions)));

				return NativeFileDialogs.openFile(startingDir, filters);
			} catch (final Throwable t) {
				Logger.error("native file dialog failed, using the swing one", t);
			}
		}

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

		return showDialog(parent, chooser);
	}

	public static File chooseFile(final Component parent, final String startingDir, final String[] extensions,
			final String description) {
		if (NativeFileDialogs.available()) {
			try {
				final DialogFilter filter = new DialogFilter(description, nativeFilterSpec(extensions));
				return NativeFileDialogs.openFile(startingDir, List.of(filter));
			} catch (final Throwable t) {
				Logger.error("native file dialog failed, using the swing one", t);
			}
		}

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
		if (NativeFileDialogs.available()) {
			try {
				final List<DialogFilter> filters = new ArrayList<>();
				for (int i = 0; i < extensions.length; i++) {
					filters.add(new DialogFilter(descriptions[i], nativeFilterSpec(extensions[i])));
				}

				return NativeFileDialogs.openFile(startingDir, filters);
			} catch (final Throwable t) {
				Logger.error("native file dialog failed, using the swing one", t);
			}
		}

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

	private static void setComboBoxesBackgrounds(final Container container) {
		for (final Component component : container.getComponents()) {
			if (MetalComboBoxButton.class.isAssignableFrom(component.getClass())) {
				final MetalComboBoxButton button = (MetalComboBoxButton) component;
				button.setBackground(ColorLabel.BASE_BG_3.color());
			} else if (Container.class.isAssignableFrom(component.getClass())) {
				setComboBoxesBackgrounds((Container) component);
			}
		}
	}

	public static File chooseDirectory(final Component parent, final String startingPath) {
		if (NativeFileDialogs.available()) {
			try {
				return NativeFileDialogs.pickFolder(startingPath);
			} catch (final Throwable t) {
				Logger.error("native file dialog failed, using the swing one", t);
			}
		}

		final JFileChooser chooser = new JFileChooser(new File(startingPath));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setApproveButtonText(Label.SAVE_AS.label());

		setComboBoxesBackgrounds(chooser);

		final int chosenOption = chooser.showOpenDialog(parent);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return chooser.getSelectedFile();
	}
}
