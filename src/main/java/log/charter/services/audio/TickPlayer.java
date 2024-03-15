package log.charter.services.audio;

import static log.charter.data.config.Config.sfxVolume;
import static log.charter.util.CollectionUtils.firstAfter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.sound.IPlayer;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.RotatingRepeatingPlayer;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioDataShort;

public class TickPlayer {
	private final IPlayer tickPlayer;
	private final Supplier<List<? extends IVirtualConstantPosition>> positionsSupplier;
	private final Supplier<ImmutableBeatsMap> beatsSupplier;

	public boolean on = false;
	private Integer nextSoundTime = null;

	public TickPlayer(final AudioDataShort tick,
			final Supplier<List<? extends IVirtualConstantPosition>> positionsSupplier,
			final Supplier<ImmutableBeatsMap> beatsSupplier) {
		this(tick, 1, positionsSupplier, beatsSupplier);
	}

	public TickPlayer(final AudioDataShort tick, final int players,
			final Supplier<List<? extends IVirtualConstantPosition>> positionsSupplier,
			final Supplier<ImmutableBeatsMap> beatsSupplier) {
		final Supplier<AudioData<?>> tickSupplier = () -> tick.volume(sfxVolume);
		if (players == 1) {
			tickPlayer = new RepeatingPlayer(tickSupplier);
		} else {
			tickPlayer = new RotatingRepeatingPlayer(tickSupplier, players);
		}

		this.positionsSupplier = positionsSupplier;
		this.beatsSupplier = beatsSupplier;
	}

	public void nextTime(final int t) {
		if (nextSoundTime != null && nextSoundTime < t) {
			tickPlayer.play();
			nextSoundTime = null;
		}

		if (on && nextSoundTime == null) {
			final ImmutableBeatsMap beats = beatsSupplier.get();
			final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(beats);
			final IVirtualConstantPosition nextPosition = firstAfter(positionsSupplier.get(), new Position(t),
					comparator).find();
			if (nextPosition != null) {
				nextSoundTime = nextPosition.toPosition(beats).position();
			}
		}
	}

	public void stop() {
		nextSoundTime = null;
	}
}