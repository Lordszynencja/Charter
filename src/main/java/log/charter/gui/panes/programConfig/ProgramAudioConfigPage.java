package log.charter.gui.panes.programConfig;

import static log.charter.data.config.SystemType.WINDOWS;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.synthbot.jasiohost.AsioDriver;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.SystemType;
import log.charter.data.config.values.AudioConfig;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.io.Logger;
import log.charter.sound.system.AudioSystemType;
import log.charter.sound.system.SoundSystem;

public class ProgramAudioConfigPage implements Page {
	private static class AudioOutputData {
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

		@Override
		public int hashCode() {
			return Objects.hash(name, type);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final AudioOutputData other = (AudioOutputData) obj;
			return Objects.equals(name, other.name) && type == other.type;
		}
	}

	private AudioSystemType audioOutSystem = AudioConfig.outSystem;
	private String audioOutSystemName = AudioConfig.outSystemName;
	private int leftOutChannelId = AudioConfig.leftOutChannelId;
	private int rightOutChannelId = AudioConfig.rightOutChannelId;

	private int audioBufferedMs = AudioConfig.bufferedMs;
	private int delay = AudioConfig.delay;
	private int midiDelay = AudioConfig.midiDelay;

	private FieldWithLabel<CharterSelect<AudioOutputData>> audioOutSystemField;
	private FieldWithLabel<TextInputWithValidation> leftOutChannelIdField;
	private FieldWithLabel<TextInputWithValidation> rightOutChannelIdField;
	private FieldWithLabel<TextInputWithValidation> audioBufferMsField;
	private FieldWithLabel<TextInputWithValidation> delayField;
	private FieldWithLabel<TextInputWithValidation> midiDelayField;

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		if (SystemType.is(WINDOWS)) {
			addAudioOutputSystemSelect(panel, position);
			position.newRow();

			addLeftOutChannelId(panel, position);
			addRightOutChannelId(panel, position);
			position.newRow();
		}

		addAudioBufferMs(panel, position);
		position.newRow();

		addDelay(panel, position);
		addMidiDelay(panel, position);
		position.newRow();
	}

	private List<AudioOutputData> getASIOOutputsList() {
		try {
			final List<AudioOutputData> asioOutputs = new LinkedList<>();
			for (final String asioDriverName : AsioDriver.getDriverNames()) {
				asioOutputs.add(new AudioOutputData(AudioSystemType.ASIO, asioDriverName));
			}
			return asioOutputs;
		} catch (final Exception e) {
			Logger.error("asio library error", e);
		} catch (final Error e) {
			Logger.error("asio library error", e);
		}

		return new ArrayList<>();
	}

	private void showChannelIdsFields(final boolean visibility) {
		leftOutChannelIdField.setVisible(visibility);
		rightOutChannelIdField.setVisible(visibility);
	}

	private void addAudioOutputSystemSelect(final RowedPanel panel, final RowedPosition position) {
		final List<AudioOutputData> inputs = new ArrayList<>();
		inputs.add(new AudioOutputData(AudioSystemType.DEFAULT, null));
		inputs.addAll(getASIOOutputsList());

		final AudioOutputData selected = new AudioOutputData(audioOutSystem, audioOutSystemName);

		final CharterSelect<AudioOutputData> select = new CharterSelect<>(inputs, selected, t -> t.toString(), t -> {
			audioOutSystem = t.type;
			audioOutSystemName = t.name;

			showChannelIdsFields(t.type == AudioSystemType.ASIO);
		});

		audioOutSystemField = new FieldWithLabel<>(Label.AUDIO_OUTPUT, 100, 200, 20, select, LabelPosition.LEFT);
		panel.add(audioOutSystemField, position);
	}

	private void addLeftOutChannelId(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForInt(leftOutChannelId, 30,
				new IntValueValidator(0, 255), i -> leftOutChannelId = i, true);
		leftOutChannelIdField = new FieldWithLabel<>(Label.AUDIO_OUTPUT_L_ID, 130, 30, 20, input, LabelPosition.LEFT);
		panel.add(leftOutChannelIdField, position);
		leftOutChannelIdField.setVisible(audioOutSystem == AudioSystemType.ASIO);
	}

	private void addRightOutChannelId(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForInt(rightOutChannelId, 30,
				new IntValueValidator(0, 255), i -> rightOutChannelId = i, true);
		rightOutChannelIdField = new FieldWithLabel<>(Label.AUDIO_OUTPUT_R_ID, 130, 30, 20, input, LabelPosition.LEFT);
		panel.add(rightOutChannelIdField, position);
		rightOutChannelIdField.setVisible(audioOutSystem == AudioSystemType.ASIO);
	}

	private void addAudioBufferMs(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(audioBufferedMs, 50, new IntValueValidator(1, 1000),
				v -> audioBufferedMs = v, false);

		audioBufferMsField = new FieldWithLabel<>(Label.BUFFER_SIZE_MS, 150, 50, 20, input, LabelPosition.LEFT);
		panel.add(audioBufferMsField, position);
	}

	private void addDelay(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(delay, 50, new IntValueValidator(1, 10_000),
				v -> delay = v, false);

		delayField = new FieldWithLabel<>(Label.SOUND_DELAY, 125, 50, 20, input, LabelPosition.LEFT);
		panel.add(delayField, position);
	}

	private void addMidiDelay(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(midiDelay, 50, new IntValueValidator(1, 10_000),
				v -> midiDelay = v, false);

		midiDelayField = new FieldWithLabel<>(Label.MIDI_SOUND_DELAY, 125, 50, 20, input, LabelPosition.LEFT);
		panel.add(midiDelayField, position);
	}

	@Override
	public Label label() {
		return Label.CONFIG_AUDIO;
	}

	@Override
	public void setVisible(final boolean visibility) {
		if (SystemType.is(WINDOWS)) {
			audioOutSystemField.setVisible(visibility);
			showChannelIdsFields(visibility && audioOutSystem == AudioSystemType.ASIO);
		}
		audioBufferMsField.setVisible(visibility);
		delayField.setVisible(visibility);
		midiDelayField.setVisible(visibility);
	}

	public void save() {
		AudioConfig.outSystem = audioOutSystem;
		AudioConfig.outSystemName = audioOutSystemName;
		AudioConfig.leftOutChannelId = leftOutChannelId;
		AudioConfig.rightOutChannelId = rightOutChannelId;

		AudioConfig.bufferedMs = audioBufferedMs;
		AudioConfig.delay = delay;
		AudioConfig.midiDelay = midiDelay;

		SoundSystem.setCurrentSoundSystem();
	}
}
