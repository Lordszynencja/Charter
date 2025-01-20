package log.charter.services.data.validation;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Level;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;

public class ArrangementValidator implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;
	private ErrorsTab errorsTab;

	private Thread validationThread;

	private final GuitarSoundsValidator guitarSoundsValidator = new GuitarSoundsValidator();
	private final PhrasesValidator phrasesValidator = new PhrasesValidator();
	private final SectionsValidator sectionsValidator = new SectionsValidator();

	@Override
	public void init() {
		charterContext.initObject(guitarSoundsValidator);
		charterContext.initObject(phrasesValidator);
		charterContext.initObject(sectionsValidator);

		startThread();
	}

	private void startThread() {
		validationThread = new Thread(() -> {
			while (!validationThread.isInterrupted()) {
				validate();

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					return;
				}
			}
		});
		validationThread.setName("Validator");
		validationThread.start();
	}

	public void validate() {
		final List<Arrangement> arrangements = chartData.songChart.arrangements;
		for (int i = 0; i < arrangements.size(); i++) {
			final Arrangement arrangement = arrangements.get(i);
			for (int j = 0; j < arrangement.levels.size(); j++) {
				final Level level = arrangement.getLevel(j);
				guitarSoundsValidator.validateGuitarSounds(i, arrangement, j, level);
			}

			phrasesValidator.validatePhrases(i, arrangement);
			sectionsValidator.validateSections(i, arrangement);
		}

		errorsTab.swapBuffers();
	}

}
