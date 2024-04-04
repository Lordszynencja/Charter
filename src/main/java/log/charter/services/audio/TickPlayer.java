package log.charter.services.audio;

import static log.charter.data.config.Config.sfxVolume;
import static log.charter.util.CollectionUtils.firstAfter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.sound.data.AudioDataShort;
import log.charter.sound.system.SoundSystem;

public class TickPlayer {
	private final AudioDataShort tick;
	private final Supplier<List<? extends IVirtualConstantPosition>> positionsSupplier;
	private final Supplier<ImmutableBeatsMap> beatsSupplier;

	public boolean on = false;
	private Integer nextSoundTime = null;

	public TickPlayer(final AudioDataShort tick,
			final Supplier<List<? extends IVirtualConstantPosition>> positionsSupplier,
			final Supplier<ImmutableBeatsMap> beatsSupplier) {
		this.tick = tick;
		this.positionsSupplier = positionsSupplier;
		this.beatsSupplier = beatsSupplier;
	}

	public void nextTime(final int t) {
		if (nextSoundTime != null && nextSoundTime < t) {
			SoundSystem.play(tick, () -> sfxVolume);
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