package log.charter.services.data.validation;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Level;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ChartPositionOnTempoMap;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;

public class ChartValidator implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ErrorsTab errorsTab;
	private ModeManager modeManager;

	private Thread validationThread;

	private final ChordTemplatesValidator chordTemplatesValidator = new ChordTemplatesValidator();
	private final FHPsValidator fhpsValidator = new FHPsValidator();
	private final GuitarSoundsValidator guitarSoundsValidator = new GuitarSoundsValidator();
	private final PhrasesValidator phrasesValidator = new PhrasesValidator();
	private final SectionsValidator sectionsValidator = new SectionsValidator();

	@Override
	public void init() {
		charterContext.initObject(chordTemplatesValidator);
		charterContext.initObject(fhpsValidator);
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

	private void validateBeatMap() {
		final ImmutableBeatsMap beats = chartData.songChart.beatsMap.immutable;

		final double firstBeatPosition = beats.get(0).position();
		if (firstBeatPosition < 10_000) {
			final ChartPosition errorPosition = new ChartPositionOnTempoMap(firstBeatPosition, chartTimeHandler,
					modeManager);
			errorsTab.addError(
					new ChartError(Label.FIRST_BEAT_BEFORE_10_SECONDS, ChartErrorSeverity.ERROR, errorPosition));
		}
	}

	private void validate() {
		validateBeatMap();

		final List<Arrangement> arrangements = chartData.songChart.arrangements;
		for (int arrangementId = 0; arrangementId < arrangements.size(); arrangementId++) {
			final Arrangement arrangement = arrangements.get(arrangementId);
			chordTemplatesValidator.validate(arrangementId, arrangement);
			phrasesValidator.validate(arrangementId, arrangement);
			sectionsValidator.validate(arrangementId, arrangement);

			for (int levelId = 0; levelId < arrangement.levels.size(); levelId++) {
				final Level level = arrangement.getLevel(levelId);

				fhpsValidator.validate(arrangementId, arrangement, levelId, level);
				guitarSoundsValidator.validate(arrangementId, arrangement, levelId, level);
			}

		}

		errorsTab.swapBuffer();
	}

}
