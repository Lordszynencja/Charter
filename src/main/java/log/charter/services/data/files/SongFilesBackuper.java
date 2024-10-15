package log.charter.services.data.files;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.services.CharterContext.Initiable;
import log.charter.util.RW;

public class SongFilesBackuper implements Initiable {
	private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

	private static String getCurrentTimeString() {
		return timeFormat.format(LocalDateTime.now());
	}

	private static File getBackupDirsFile(final String dir) {
		return new File(new File(dir), "backups");
	}

	public static void makeBackups(final String dir, final List<String> fileNames) {
		final File backupFolder = new File(getBackupDirsFile(dir), getCurrentTimeString());
		backupFolder.mkdirs();

		for (final String fileName : fileNames) {
			final File f = new File(dir, fileName);
			if (f.exists()) {
				RW.writeB(new File(backupFolder, fileName), RW.readB(f));
			}
		}
	}

	public static void makeAudioBackup(final File file) {
		final File backupFolder = getBackupDirsFile(file.getParent());
		final String backupfileName = getCurrentTimeString() + " " + file.getName();
		RW.writeB(new File(backupFolder, backupfileName), RW.readB(file));
	}

	private ChartData chartData;

	@Override
	public void init() {
		final Thread t = new Thread(() -> {
			while (true) {
				try {
					if (Config.backupDelay > 0) {
						Thread.sleep(Config.backupDelay * 1000);
					}
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				makeDefaultBackups();
			}
		});

		t.setName("Song files backupper");

		t.start();
	}

	private void makeDefaultBackups() {
		if (chartData.isEmpty) {
			return;
		}

		final List<String> filesToBackup = new ArrayList<>();
		filesToBackup.add(chartData.projectFileName);
		Logger.debug("Doing backup of " + chartData.path + ", files: " + filesToBackup);

		makeBackups(chartData.path, filesToBackup);
	}
}
