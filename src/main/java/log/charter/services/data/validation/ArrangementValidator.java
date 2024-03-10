package log.charter.services.data.validation;

import log.charter.data.ChartData;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.song.Arrangement;
import log.charter.util.CollectionUtils.ArrayList2;

public class ArrangementValidator implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;

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
		final ArrayList2<Arrangement> arrangements = chartData.songChart.arrangements;
		for (int i = 0; i < arrangements.size(); i++) {
			final Arrangement arrangement = arrangements.get(i);
			if (!phrasesValidator.validatePhrases(i, arrangement)) {
				return false;
			}
			if (!sectionsValidator.validateSections(i, arrangement)) {
				return false;
			}
		}

		return true;
	}

}
