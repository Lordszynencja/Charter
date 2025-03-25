package log.charter.gui.panes.songEdits;

import static java.lang.Math.min;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.io.File;

import com.breakfastquay.rubberband.RubberBandStretcher;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.LoadingDialog;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.DoubleValueValidator;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.SoundFileType;
import log.charter.sound.data.AudioData;
import log.charter.sound.utils.FloatSamplesUtils;

public class ChangeSongPitchPane extends RowedDialog {
	private static final long serialVersionUID = -4754359602173894487L;

	private static final int flags = RubberBandStretcher.OptionProcessOffline //
			| RubberBandStretcher.OptionChannelsTogether//
			| RubberBandStretcher.OptionEngineFiner//
			| RubberBandStretcher.OptionPhaseIndependent//
			| RubberBandStretcher.OptionThreadingNever//
			| RubberBandStretcher.OptionPitchHighQuality//
			| RubberBandStretcher.OptionChannelsApart//
			| RubberBandStretcher.OptionWindowLong;

	private static final int bufferSize = 1024 * 8;

	private final ProjectAudioHandler projectAudioHandler;

	private double pitchFrom = 440;
	private double pitchTo = 432;

	public ChangeSongPitchPane(final CharterFrame frame, final ProjectAudioHandler projectAudioHandler) {
		super(frame, Label.CHANGE_SONG_PITCH, 100);
		this.projectAudioHandler = projectAudioHandler;

		final RowedPosition position = new RowedPosition(20, panel.sizes);

		addPitchFrom(position);
		addPitchTo(position);

		position.newRow();
		position.newRow();
		addDefaultFinish(position.y(), SaverWithStatus.defaultFor(this::saveAndExit), null, true);
	}

	private void addPitchFrom(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(pitchFrom, 40,
				new DoubleValueValidator(220, 880, false), v -> pitchFrom = v, false);
		addSelectTextOnFocus(input);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PITCH_FROM, 100, 40, 25, input,
				LabelPosition.LEFT);

		panel.add(field, position);
	}

	private void addPitchTo(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(pitchTo, 40,
				new DoubleValueValidator(220, 880, false), v -> pitchTo = v, false);
		addSelectTextOnFocus(input);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PITCH_TO, 15, 40, 25, input,
				LabelPosition.LEFT);

		panel.add(field, position);
	}

	private static long getTimeElapsed(final long startTime) {
		return (System.nanoTime() - startTime) / 1_000_000_000;
	}

	private static String formatTime(final long t) {
		return t / 60 + ":%02d".formatted(t % 60);
	}

	private static long getExpectedTime(final long startTime, final int progress, final int l) {
		if (progress == 0) {
			return 60;
		}

		final long timeElapsed = System.nanoTime() - startTime;

		return timeElapsed * l / progress / 1_000_000_000;
	}

	private static String getTime(final long startTime, final int progress, final int l) {
		return formatTime(getTimeElapsed(startTime)) + "/" + formatTime(getExpectedTime(startTime, progress, l));
	}

	private void studyAudio(final LoadingDialog loadingDialog, final RubberBandStretcher pitchShifter,
			final float[][] samples, final int l) {
		final long startTime = System.nanoTime();
		loadingDialog.changeMaxProgress(l);
		loadingDialog.setProgress(0, Label.STUDYING_AUDIO.format(getTime(startTime, 0, l)));
		for (int i = 0; i < l; i += bufferSize) {
			pitchShifter.study(samples, i, min(l - i, bufferSize), i + bufferSize >= l);
			loadingDialog.setProgress(i, Label.STUDYING_AUDIO.format(getTime(startTime, min(l, i + bufferSize), l)));
		}
	}

	private void processAudio(final LoadingDialog loadingDialog, final RubberBandStretcher pitchShifter,
			final float[][] samples, final int l) {
		final long startTime = System.nanoTime();
		int r = 0;

		loadingDialog.changeMaxProgress(l);
		loadingDialog.setProgress(0, Label.PITCH_SHIFTING_AUDIO.format(getTime(startTime, 0, l)));
		for (int i = 0; i < l; i += bufferSize) {
			pitchShifter.process(samples, i, min(l - i, bufferSize), i + bufferSize >= l);
			r += pitchShifter.retrieve(samples, r, min(l - r, pitchShifter.available()));
			loadingDialog.setProgress(i,
					Label.PITCH_SHIFTING_AUDIO.format(getTime(startTime, min(l, i + bufferSize), l)));
		}
	}

	private void shiftAudio(final LoadingDialog loadingDialog, final float[][] samples, final int sampleRate,
			final int channels) {
		final double shift = pitchTo / pitchFrom;
		final RubberBandStretcher pitchShifter = new RubberBandStretcher(sampleRate, channels, flags, 1, shift);

		final int l = samples[0].length;
		for (final float[] channel : samples) {
			for (int i = 0; i < l; i++) {
				channel[i] *= 0.98;
			}
		}
		studyAudio(loadingDialog, pitchShifter, samples, l);
		processAudio(loadingDialog, pitchShifter, samples, l);

		pitchShifter.dispose();
	}

	private void saveAndExit() {
		setVisible(false);
		LoadingDialog.doWithLoadingDialog(frame, 4, loadingDialog -> {
			loadingDialog.setProgress(0, "Reading samples");
			final AudioData audio = projectAudioHandler.getAudio();
			final int sampleSize = audio.format.getSampleSizeInBits() / 8;
			final int channels = audio.format.getChannels();
			final float[][] samples = FloatSamplesUtils.splitAudioFloat(audio.data, sampleSize, channels);

			shiftAudio(loadingDialog, samples, (int) audio.format.getSampleRate(), channels);

			loadingDialog.changeMaxProgress(4);
			loadingDialog.setProgress(2, "Writing samples");
			final byte[] bytes = FloatSamplesUtils.toBytes(samples, sampleSize, channels);
			final AudioData shiftedAudio = new AudioData(bytes, audio.format.getSampleRate(), sampleSize, channels);
			final File file = new File("C:/users/szymon/desktop/" + pitchFrom + "_" + pitchTo + ".wav");

			SoundFileType.WAV.write(loadingDialog, shiftedAudio, file);
		}, Label.SAVING_AUDIO.label());
	}
}
