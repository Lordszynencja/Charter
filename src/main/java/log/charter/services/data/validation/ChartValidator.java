package log.charter.services.data.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Level;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.gui.CharterFrame.TabType;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;

public class ChartValidator implements Initiable {
	public static List<Integer> overlappingPositions(final List<? extends IFractionalPosition> positions) {
		final List<Integer> doubledPositions = new ArrayList<>();

		boolean lastAdded = false;
		for (int i = 0; i < positions.size() - 1; i++) {
			if (positions.get(i).position().equals(positions.get(i + 1).position())) {
				if (!lastAdded) {
					doubledPositions.add(i);
				}
				doubledPositions.add(i + 1);

				lastAdded = true;
			} else {
				lastAdded = false;
			}
		}

		return doubledPositions;
	}

	private CharterContext charterContext;
	private ChartData chartData;
	private ChartPositionGenerator chartPositionGenerator;
	private ErrorsTab errorsTab;

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
			long startTime = System.currentTimeMillis();
			while (!validationThread.isInterrupted()) {
				validate();

				while (startTime <= System.currentTimeMillis()) {
					startTime += 1000;
				}
				try {
					Thread.sleep(startTime - System.currentTimeMillis());
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
			final ChartPosition position = chartPositionGenerator.position().tempoMap(0).build();
			errorsTab.addError(new ChartError(Label.FIRST_BEAT_BEFORE_10_SECONDS, position));
		}
	}

	private void validateOverlapping(final List<? extends IFractionalPosition> positions, final Label label,
			final Function<Integer, ChartPosition> positionSetter) {
		for (final Integer id : ChartValidator.overlappingPositions(positions)) {
			final ChartPosition position = positionSetter.apply(id).tab(TabType.QUICK_EDIT).build();

			errorsTab.addError(new ChartError(label, position));
		}
	}

	private void validate() {
		if (chartData.isEmpty) {
			return;
		}

		validateBeatMap();

		validateOverlapping(chartData.showlights(), Label.OVERLAPPING_SHOWLIGHT,
				id -> chartPositionGenerator.position().showlight(id));

		final List<Arrangement> arrangements = chartData.songChart.arrangements;
		for (int arrangementId = 0; arrangementId < arrangements.size(); arrangementId++) {
			final Arrangement arrangement = arrangements.get(arrangementId);

			final ChartPosition errorPosition = chartPositionGenerator.position().arrangement(arrangementId);
			validateOverlapping(arrangement.eventPoints, Label.OVERLAPPING_EVENT_POINT,
					id -> errorPosition.clone().event(id));
			validateOverlapping(arrangement.toneChanges, Label.OVERLAPPING_TONE_CHANGE,
					id -> errorPosition.clone().toneChange(id));

			chordTemplatesValidator.validate(arrangementId, arrangement);
			phrasesValidator.validate(arrangementId, arrangement);
			sectionsValidator.validate(arrangementId, arrangement);

			for (int levelId = 0; levelId < arrangement.levels.size(); levelId++) {
				final Level level = arrangement.getLevel(levelId);

				fhpsValidator.validate(arrangementId, arrangement, levelId, level);
				guitarSoundsValidator.validate(arrangementId, arrangement, levelId, level);

				errorPosition.level(levelId);
				validateOverlapping(level.fhps, Label.OVERLAPPING_FHP, id -> errorPosition.clone().fhp(id));
				validateOverlapping(level.sounds, Label.OVERLAPPING_SOUND, id -> errorPosition.clone().sound(id));
				validateOverlapping(level.handShapes, Label.OVERLAPPING_HAND_SHAPE,
						id -> errorPosition.clone().handShape(id));
			}
		}

		errorsTab.swapBuffer();
	}

}
