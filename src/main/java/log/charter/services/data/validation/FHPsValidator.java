package log.charter.services.data.validation;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.FHP;
import log.charter.data.song.Level;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ChartPositionOnLevel;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;

public class FHPsValidator {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ErrorsTab errorsTab;
	private ModeManager modeManager;

	public void validate(final int arrangementId, final Arrangement arrangement, final int levelId, final Level level) {
		for (final FHP fhp : level.fhps) {
			if (fhp.fret <= arrangement.capo) {
				final ChartPosition errorPosition = new ChartPositionOnLevel(chartData, arrangementId, levelId,
						fhp.position(), chartTimeHandler, modeManager);
				errorsTab.addError(
						new ChartError(Label.FHP_STARTS_ON_WRONG_FRET, ChartErrorSeverity.ERROR, errorPosition));
			}
		}
	}
}
