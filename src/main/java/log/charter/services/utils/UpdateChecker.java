package log.charter.services.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;

import log.charter.CharterMain;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.SystemType;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.io.Logger;
import log.charter.services.CharterContext;
import log.charter.util.RW;
import log.charter.util.Utils;

public class UpdateChecker {
	private static final URI latestVersionLink = URI.create("https://github.com/Lordszynencja/Charter/releases/latest");

	private static HttpResponse<String> getLatestVersionRedirect() {
		final HttpClient client = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder(latestVersionLink).build();
		final HttpResponse<String> response = client.sendAsync(request, BodyHandlers.ofString())//
				.join();
		if (response.statusCode() != 302) {
			Logger.error("Couldn't check latest version, GitHub response: " + response.statusCode());
			return null;
		}

		return response;
	}

	private static String getVersion(final HttpResponse<String> response) {
		final Optional<String> locationHeader = response.headers()//
				.firstValue("location");
		if (locationHeader.isEmpty()) {
			Logger.error("Location header was not present");
			return null;
		}

		final String location = locationHeader.get();
		final int slashPosition = location.lastIndexOf('/');
		if (slashPosition == -1) {
			Logger.error("There was no slash in redirect location: " + location);
			return null;
		}
		if (slashPosition == location.length() - 1) {
			Logger.error("There was no version in redirect location: " + location);
			return null;
		}

		return location.substring(slashPosition + 1);
	}

	private static void makeTempUpdateFile() {
		final File updateScriptFile = new File(RW.getJarDirectory(), "update.bat");
		final File tmpFile = new File(RW.getJarDirectory(), "tmp_update.bat");

		RW.copy(updateScriptFile, tmpFile);
	}

	private static void runUpdate(final String oldVersion, final String newVersion) throws IOException {
		makeTempUpdateFile();

		new ProcessBuilder()//
				.command("cmd.exe", "/c", "START \"\" tmp_update.bat " + newVersion + " " + oldVersion)//
				.directory(RW.getJarDirectory()).start();
	}

	private CharterContext charterContext;
	private CharterFrame charterFrame;

	private void informUserAboutNewVersion(final String newVersion) {
		final ConfirmAnswer answer = ComponentUtils.askYesNo(charterFrame, Label.NEW_VERSION,
				Label.NEW_VERSION_AVAILABLE_DOWNLOAD, newVersion, CharterMain.VERSION);

		if (answer != ConfirmAnswer.YES) {
			return;
		}

		try {
			Desktop.getDesktop().browse(latestVersionLink);
		} catch (final Exception e) {
			Logger.error("Couldn't open browser", e);
		}
	}

	private void informUserAboutNewVersionWindows(final String newVersion) {
		final ConfirmAnswer answer = ComponentUtils.askYesNo(charterFrame, Label.NEW_VERSION,
				Label.NEW_VERSION_AVAILABLE_UPDATE, newVersion, CharterMain.VERSION);

		if (answer != ConfirmAnswer.YES) {
			return;
		}

		try {
			runUpdate(CharterMain.VERSION, newVersion);
			charterContext.forceExit();
		} catch (final IOException e) {
			Logger.error("Couldn't autoupdate", e);
		}
	}

	public void checkForUpdates() {
		if (Utils.isDevEnv) {
			return;
		}

		final HttpResponse<String> response = getLatestVersionRedirect();
		if (response == null) {
			return;
		}

		final String version = getVersion(response);
		if (version == null || CharterMain.VERSION.equals(version)) {
			return;
		}

		if (SystemType.is(SystemType.WINDOWS)) {
			informUserAboutNewVersionWindows(version);
		} else {
			informUserAboutNewVersion(version);
		}
	}
}
