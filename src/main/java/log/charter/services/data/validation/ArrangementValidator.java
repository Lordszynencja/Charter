package log.charter.services.data.validation;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;

public class ArrangementValidator implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;
	private ErrorsTab errorsTab;

	private final PhrasesValidator phrasesValidator = new PhrasesValidator();
	private final SectionsValidator sectionsValidator = new SectionsValidator();

	@Override
	public void init() {
		charterContext.initObject(phrasesValidator);
		charterContext.initObject(sectionsValidator);
	}

	/**
	 * @return true if validation passed
	 */
	public boolean validate() {
		errorsTab.clearErrors();

		final List<Arrangement> arrangements = chartData.songChart.arrangements;
		for (int i = 0; i < arrangements.size(); i++) {
			final Arrangement arrangement = arrangements.get(i);
			phrasesValidator.validatePhrases(i, arrangement);
			sectionsValidator.validateSections(i, arrangement);
		}

		return true;
	}

}
