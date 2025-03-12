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
			try {
				Runtime.getRuntime().exec("cmd /c start \"\" update.bat " + newVersion);
				charterContext.forceExit();
			} catch (final IOException e) {
				Logger.error("Couldn't autoupdate", e);
			}
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
			try {
				final File tmpFile = new File("tmp_update.bat");
				RW.copy(new File("update.bat"), tmpFile);

				final String baseCmd = "cmd.exe /c tmp_update.bat " + newVersion + " " + CharterMain.VERSION;
				Runtime.getRuntime().exec(baseCmd);
				charterContext.forceExit();
			} catch (final IOException e) {
				Logger.error("Couldn't autoupdate", e);
			}
		} catch (final Exception e) {
			Logger.error("Couldn't open browser", e);
		}
	}

	public void checkForUpdates() {
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
