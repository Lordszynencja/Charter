package log.charter.sound.system;

import java.util.function.DoubleSupplier;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.asio.ASIOHandler;
import log.charter.sound.data.AudioData;
import log.charter.sound.effects.Effect;
import log.charter.sound.system.data.ISoundSystem;
import log.charter.sound.system.data.Player;

public class SoundSystem {
	private static int playerId = 0;

	private static ISoundSystem currentSoundSystem = new StandardSoundSystem();

	public static void setCurrentSoundSystem() {
		try {
			currentSoundSystem = switch (Config.audioOutSystemType) {
				case ASIO -> new ASIOSoundSystem();
				default -> new StandardSoundSystem();
			};

			ASIOHandler.refresh();
		} catch (final Throwable t) {
			Logger.error("Couldn't initialize audio system, setting output to default", t);
			Config.audioOutSystemType = AudioSystemType.DEFAULT;
			setCurrentSoundSystem();
		}
	}

	public static ISoundSystem getCurrentSoundSystem() {
		return currentSoundSystem;
	}

	public static Player play(final AudioData audioData, final DoubleSupplier volumeSupplier, final int speed) {
		return play(audioData, volumeSupplier, speed, 0, Effect.emptyEffect);
	}

	public static Player play(final AudioData audioData, final DoubleSupplier volumeSupplier, final int speed,
			final double startTime, final Effect effect) {
		return new Player(audioData, volumeSupplier, speed, effect).start(startTime, playerId++);
	}
}