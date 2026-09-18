package log.charter.services.data.files;

import static log.charter.io.rsc.xml.ChartProjectXStreamHandler.writeChartProject;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.components.tabs.TextTab;
import log.charter.io.Logger;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.ModeManager;
import log.charter.util.RW;

public class SongFilesBackuper implements Initiable {
	private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

	private static String getCurrentTimeString() {
		return timeFormat.format(LocalDateTime.now());
	}

	private static File getBackupDirsFile(final String dir) {
		return new File(new File(dir), "backups");
	}

	private static File makeSureBackupFolderExists(final String dir) {
		final File backupFolder = new File(getBackupDirsFile(dir), getCurrentTimeString());
		backupFolder.mkdirs();

		return backupFolder;
	}

	public static void makeBackupsForFiles(final String dir, final List<String> fileNames) {
		final File backupFolder = makeSureBackupFolderExists(dir);

		for (final String fileName : fileNames) {
			final File f = new File(dir, fileName);
			if (f.exists()) {
				RW.writeB(new File(backupFolder, fileName), RW.readB(f));
			}
		}
	}

	public static void makeAudioBackup(final File file) {
		final File backupFolder = makeSureBackupFolderExists(file.getParent());
		final String backupfileName = getCurrentTimeString() + " " + file.getName();
		RW.writeB(new File(backupFolder, backupfileName), RW.readB(file));
	}

	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private TextTab textTab;

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

	public void makeDefaultBackups() {
		if (chartData.isEmpty) {
			return;
		}

		try {
			final File backupFolder = makeSureBackupFolderExists(chartData.path);
			final ChartProject project = new ChartProject(chartTimeHandler.time(), modeManager.getMode(), chartData,
					chartData.songChart, projectAudioHandler.getSelectedStem(), textTab.getText());
			final String xml = writeChartProject(project);
			RW.write(new File(backupFolder, chartData.projectFileName), xml, "UTF-8");
		} catch (final Exception e) {
			Logger.error("Couldn't save a backup!", e);
		}
	}
}
