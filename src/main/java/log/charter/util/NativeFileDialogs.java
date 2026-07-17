package log.charter.util;

import static org.lwjgl.util.nfd.NativeFileDialog.NFD_CANCEL;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_FreePath;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_GetError;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_Init;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_OKAY;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_OpenDialog;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PickFolder;

import java.io.File;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;

import log.charter.data.config.SystemType;
import log.charter.io.Logger;

// native file dialogs through lwjgl-nfd. on windows that is the modern
// IFileDialog (the one browsers open), on linux it goes through GTK when
// available. macOS is skipped because NFD wants the AppKit main thread there
// and we call from the swing EDT, so mac keeps the swing chooser
public class NativeFileDialogs {
	public static class DialogFilter {
		public final String name;
		// comma separated extensions without dots, like "gp3,gp4,gp5"
		public final String extensions;

		public DialogFilter(final String name, final String extensions) {
			this.name = name;
			this.extensions = extensions;
		}
	}

	private static Boolean initialized = null;

	public static boolean available() {
		if (SystemType.is(SystemType.MAC)) {
			return false;
		}

		if (initialized == null) {
			try {
				initialized = NFD_Init() == NFD_OKAY;
				if (!initialized) {
					Logger.error("couldn't initialize native file dialogs: " + NFD_GetError());
				}
			} catch (final Throwable t) {
				// no natives for this platform or no GTK, swing chooser takes over
				Logger.error("couldn't initialize native file dialogs", t);
				initialized = false;
			}
		}

		return initialized;
	}

	// returns the picked file, or null when the user canceled
	public static File openFile(final String startingDir, final List<DialogFilter> filters) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			NFDFilterItem.Buffer filterItems = null;
			if (filters != null && !filters.isEmpty()) {
				filterItems = NFDFilterItem.malloc(filters.size(), stack);
				for (int i = 0; i < filters.size(); i++) {
					filterItems.get(i)//
							.name(stack.UTF8(filters.get(i).name))//
							.spec(stack.UTF8(filters.get(i).extensions));
				}
			}

			final PointerBuffer path = stack.mallocPointer(1);
			return handleResult(NFD_OpenDialog(path, filterItems, existingDirOrNull(startingDir)), path);
		}
	}

	// returns the picked folder, or null when the user canceled
	public static File pickFolder(final String startingDir) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final PointerBuffer path = stack.mallocPointer(1);
			return handleResult(NFD_PickFolder(path, existingDirOrNull(startingDir)), path);
		}
	}

	// NFD misbehaves when the default path doesn't exist
	private static String existingDirOrNull(final String dir) {
		if (dir == null) {
			return null;
		}

		final File file = new File(dir);
		return file.isDirectory() ? file.getAbsolutePath() : null;
	}

	private static File handleResult(final int result, final PointerBuffer path) {
		if (result == NFD_CANCEL) {
			return null;
		}
		if (result != NFD_OKAY) {
			throw new IllegalStateException("native file dialog failed: " + NFD_GetError());
		}

		final File file = new File(path.getStringUTF8(0));
		NFD_FreePath(path.get(0));
		return file;
	}
}
