package log.charter.services.audio;

import static log.charter.util.CollectionUtils.firstAfter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import log.charter.data.config.values.AudioConfig;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.sound.data.AudioData;
import log.charter.sound.system.SoundSystem;
import log.charter.sound.system.data.ISoundLine;

public class TickPlayer {
	private final AudioData tick;
	private final Supplier<List<? extends IVirtualConstantPosition>> positionsSupplier;
	private final Supplier<ImmutableBeatsMap> beatsSupplier;

	public boolean on = false;
	private Double nextSoundTime = null;
	private Integer nextSoundId;
	private ISoundLine line;

	public TickPlayer(final AudioData tick, final Supplier<List<? extends IVirtualConstantPosition>> positionsSupplier,
			final Supplier<ImmutableBeatsMap> beatsSupplier) {
		this.tick = tick;
		this.positionsSupplier = positionsSupplier;
		this.beatsSupplier = beatsSupplier;
	}

	private void setNextSoundForTime(final double t) {
		final ImmutableBeatsMap beats = beatsSupplier.get();
		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(beats);
		nextSoundId = firstAfter(positionsSupplier.get(), new Position(t), comparator).findId();
		if (nextSoundId != null) {
			nextSoundTime = positionsSupplier.get().get(nextSoundId).toPosition(beats).position();
		}
	}

	private synchronized void closeLineIfNeeded() {
		if (line != null) {
			line.stop();
			line.close();
			line = null;
		}
	}

	public void start(final double t) {
		closeLineIfNeeded();
		line = SoundSystem.getCurrentSoundSystem().getNewLine(tick.format);
		setNextSoundForTime(t);
	}

	public void nextTime(final double t) {
		if (line == null || nextSoundTime == null || nextSoundTime > t) {
			return;
		}

		if (on) {
			line.write(tick.withVolume((float) (AudioConfig.getSfxVolumeWithMute() * 0.5)).data);
		}

		nextSoundId++;
		final List<? extends IVirtualConstantPosition> positions = positionsSupplier.get();
		if (nextSoundId >= positions.size()) {
			stop();
		} else {
			nextSoundTime = positionsSupplier.get().get(nextSoundId).toPosition(beatsSupplier.get()).position();
		}
	}

	public void reset(final double t) {
		setNextSoundForTime(t);
	}

	public void stop() {
		closeLineIfNeeded();
		nextSoundId = null;
		nextSoundTime = null;
	}
}