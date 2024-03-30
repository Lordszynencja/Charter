package log.charter.io.rsc.xml;

import log.charter.services.editModes.EditMode;

public class ChartProjectVerion2Updater {
	public static void update(final ChartProject project) {
		if (project.chartFormatVersion >= 2) {
			return;
		}

		project.editMode = EditMode.GUITAR;
		project.arrangement = 0;
		project.level = 0;
		project.time = 0;

		project.chartFormatVersion = 2;
	}
}
