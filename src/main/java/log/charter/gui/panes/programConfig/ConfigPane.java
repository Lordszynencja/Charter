package log.charter.gui.panes.programConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.synthbot.jasiohost.AsioDriver;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.io.Logger;
import log.charter.services.utils.Framer;
import log.charter.sound.SoundFileType;
import log.charter.sound.system.AudioSystemType;
import log.charter.sound.system.SoundSystem;

public final class ConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private AudioSystemType audioSystemType = Config.audioSystemType;
	private String audioSystemName = Config.audioSystemName;
	private int leftOutChannelId = Config.leftOutChannelId;
	private int rightOutChannelId = Config.rightOutChannelId;
	private SoundFileType baseAudioFormat = Config.baseAudioFormat;
	private int delay = Config.delay;
	private int audioBufferMs = Config.audioBufferMs;
	private int midiDelay = Config.midiDelay;

	public ConfigPane(final CharterFrame frame, final Framer framer) {
		super(frame, Label.CONFIG_PANE_TITLE, 600);

		final RowedPosition position = new RowedPosition(20, sizes);
		int row = 0;

		addAudioOutputSelect(position);
		row++;
		row++;

		addIntConfigValue(row, 20, 0, Label.SOUND_DELAY, delay, 50, //
				new IntValueValidator(1, 10000), v -> delay = v, false);
		addIntConfigValue(row++, 220, 0, Label.BUFFER_SIZE_MS, delay, 50, //
				new IntValueValidator(1, 250), v -> audioBufferMs = v, false);

		addIntConfigValue(row, 20, 0, Label.MIDI_SOUND_DELAY, midiDelay, 50, //
				new IntValueValidator(1, 10000), v -> midiDelay = v, false);
		addBaseAudioFormatSelect(220, row++);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void addAudioOutputSelect(final RowedPosition position) {
		try {
			class AudioOutputData {
				private final AudioSystemType type;
				private final String name;

				public AudioOutputData(final AudioSystemType type, final String name) {
					this.type = type;
					this.name = name;
				}

				@Override
				public String toString() {
					return switch (type) {
						case DEFAULT -> name == null ? "Default" : name;
						case ASIO -> "ASIO: " + name;
						default -> "???";
					};
				}
			}

			final List<AudioOutputData> inputs = new ArrayList<>();
			inputs.add(new AudioOutputData(AudioSystemType.DEFAULT, null));

			for (final String asioDriverName : AsioDriver.getDriverNames()) {
				inputs.add(new AudioOutputData(AudioSystemType.ASIO, asioDriverName));
			}

			final AudioOutputData selected = new AudioOutputData(audioSystemType, audioSystemName);

			final CharterSelect<AudioOutputData> select = new CharterSelect<>(inputs, selected, t -> t.toString(),
					t -> {
						audioSystemType = t.type;
						audioSystemName = t.name;
					});

			final FieldWithLabel<CharterSelect<AudioOutputData>> field = new FieldWithLabel<>(Label.AUDIO_OUTPUT, 100,
					200, 20, select, LabelPosition.LEFT);
			add(field, position.getX(), position.getY(), field.getPreferredSize().width, 20);
			position.newRow();

			final TextInputWithValidation leftOutChannelIdInput = TextInputWithValidation.generateForInt(
					leftOutChannelId, 30, new IntValueValidator(0, 255), i -> leftOutChannelId = i, true);
			final FieldWithLabel<TextInputWithValidation> leftOutField = new FieldWithLabel<>(Label.AUDIO_OUTPUT_L_ID,
					130, 30, 20, leftOutChannelIdInput, LabelPosition.LEFT);
			add(leftOutField, position.getAndAddX(leftOutField.getPreferredSize().width + 20), position.getY(),
					leftOutField.getPreferredSize().width, 20);

			final TextInputWithValidation rightOutChannelIdInput = TextInputWithValidation.generateForInt(
					rightOutChannelId, 30, new IntValueValidator(0, 255), i -> rightOutChannelId = i, true);
			final FieldWithLabel<TextInputWithValidation> rightOutField = new FieldWithLabel<>(Label.AUDIO_OUTPUT_R_ID,
					130, 30, 20, rightOutChannelIdInput, LabelPosition.LEFT);
			add(rightOutField, position.getX(), position.getY(), rightOutField.getPreferredSize().width, 20);
			position.newRow();
		} catch (final Exception e) {
			Logger.error("asio library error", e);
		} catch (final Error e) {
			Logger.error("asio library error", e);
		}
	}

	private void addBaseAudioFormatSelect(final int x, final int row) {
		final Stream<SoundFileType> possibleValues = Stream.of(SoundFileType.values())//
				.filter(type -> type.writer != null && type.loader != null);

		final CharterSelect<SoundFileType> select = new CharterSelect<>(possibleValues, baseAudioFormat, t -> t.name,
				t -> baseAudioFormat = t);
		final FieldWithLabel<CharterSelect<SoundFileType>> field = new FieldWithLabel<>(Label.BASE_AUDIO_FORMAT, 100,
				100, 20, select, LabelPosition.LEFT);

		add(field, x, getY(row), field.getWidth(), field.getHeight());
	}

	private void saveAndExit() {
		Config.audioSystemType = audioSystemType;
		Config.audioSystemName = audioSystemName;
		Config.leftOutChannelId = leftOutChannelId;
		Config.rightOutChannelId = rightOutChannelId;
		Config.baseAudioFormat = baseAudioFormat;
		Config.delay = delay;
		Config.audioBufferMs = audioBufferMs;
		Config.midiDelay = midiDelay;

		Config.markChanged();
		Config.save();

		SoundSystem.setCurrentSoundSystem();
	}
}
