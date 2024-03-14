package log.charter.services.audio;

import static log.charter.data.config.Config.sfxVolume;
import static log.charter.util.CollectionUtils.firstAfter;

import java.util.List;
import java.util.function.Supplier;

import log.charter.data.song.position.IPosition;
import log.charter.data.song.position.Position;
import log.charter.sound.IPlayer;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.RotatingRepeatingPlayer;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioDataShort;

public class TickPlayer {
	private final IPlayer tickPlayer;
	private final Supplier<List<? extends IPosition>> positionsSupplier;

	public boolean on = false;
	private Integer nextSoundTime = null;

	public TickPlayer(final AudioDataShort tick, final Supplier<List<? extends IPosition>> positionsSupplier) {
		this(tick, 1, positionsSupplier);
	}

	public TickPlayer(final AudioDataShort tick, final int players,
			final Supplier<List<? extends IPosition>> positionsSupplier) {
		final Supplier<AudioData<?>> tickSupplier = () -> tick.volume(sfxVolume);
		if (players == 1) {
			tickPlayer = new RepeatingPlayer(tickSupplier);
		} else {
			tickPlayer = new RotatingRepeatingPlayer(tickSupplier, players);
		}

		this.positionsSupplier = positionsSupplier;
	}

	public void nextTime(final int t) {
		if (nextSoundTime != null && nextSoundTime < t) {
			tickPlayer.play();
			nextSoundTime = null;
		}

		if (on && nextSoundTime == null) {
			final IPosition nextPosition = firstAfter(positionsSupplier.get(), new Position(t)).find();
			if (nextPosition != null) {
				nextSoundTime = nextPosition.position();
			}
		}
	}

	public void stop() {
		nextSoundTime = null;
	}
}