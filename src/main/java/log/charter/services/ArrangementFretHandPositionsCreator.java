package log.charter.services;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.frets;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.List;

import log.charter.data.config.Config;
import log.charter.data.song.Anchor;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.util.data.IntRange;

public class ArrangementFretHandPositionsCreator {
	private static class FretRange implements IConstantFractionalPosition {
		public final FractionalPosition position;
		public final boolean isNote;
		public final boolean isTap;
		public final IntRange fretRange;

		public FretRange(final FractionalPosition position, final int fret, final boolean isTap) {
			this.position = position;
			isNote = true;
			this.isTap = isTap;
			fretRange = new IntRange(fret, fret);
		}

		public FretRange(final FractionalPosition position, final int minFret, final int maxFret) {
			this.position = position;
			isNote = false;
			isTap = false;
			fretRange = new IntRange(minFret, maxFret);
		}

		@Override
		public FractionalPosition position() {
			return position;
		}
	}

	private static FretRange fretRangeFromSound(final ImmutableBeatsMap beats, final List<ChordTemplate> chordTemplates,
			final ChordOrNote sound) {
		final FractionalPosition position = sound.position();
		if (sound.isNote()) {
			return new FretRange(position, sound.note().fret, sound.note().hopo == HOPO.TAP);
		}

		final ChordTemplate template = chordTemplates.get(sound.chord().templateId());
		int minFret = Config.frets;
		int maxFret = 0;
		for (final int fret : template.frets.values()) {
			if (fret == 0) {
				continue;
			}

			minFret = min(minFret, fret);
			maxFret = max(maxFret, fret);
		}

		if (maxFret == 0) {
			return new FretRange(position, 0, false);
		}

		return new FretRange(position, minFret, maxFret);
	}

	private static void addFHP(final ImmutableBeatsMap beats, final FretRange fretRange, final int index,
			final List<Anchor> anchors) {
		int baseFret = Math.min(frets - 3, max(1, fretRange.fretRange.min));

		if (baseFret <= 0) {
			baseFret = 0;
		}
		if (baseFret > Config.frets - 3) {
			baseFret = Config.frets - 3;
		}
		final int width = 1 + max(3, fretRange.fretRange.max - fretRange.fretRange.min);

		anchors.add(index, new Anchor(fretRange.position(), baseFret, width));
	}

	private static boolean canBeExtended(final Anchor anchor, final int fret) {
		int maxWidth;
		if (anchor.fret >= 18) {
			maxWidth = 6;
		} else if (anchor.fret >= 13) {
			maxWidth = 5;
		} else if (anchor.fret >= 7) {
			maxWidth = 4;
		} else {
			maxWidth = 3;
		}

		return fret <= anchor.fret + maxWidth;
	}

	private static void addFHPIfNeeded(final ImmutableBeatsMap beats, final FretRange fretRange,
			final List<Anchor> anchors) {
		final Integer currentAnchorId = lastBeforeEqual(anchors, fretRange).findId();
		if (currentAnchorId == null) {
			addFHP(beats, fretRange, 0, anchors);
			return;
		}

		if (fretRange.fretRange.max == 0) {
			return;
		}

		final Anchor current = anchors.get(currentAnchorId);
		if (fretRange.isNote) {
			final int fret = fretRange.fretRange.min;
			if (fret < current.fret || fret > current.topFret()) {
				if (fretRange.isTap || (fret > current.fret && canBeExtended(current, fret))) {
					current.width = fret - current.fret + 1;
				} else {
					addFHP(beats, fretRange, currentAnchorId + 1, anchors);
				}
			}

			return;
		}

		if (current.fret != fretRange.fretRange.min) {
			addFHP(beats, fretRange, currentAnchorId + 1, anchors);
			return;
		}

		if (current.topFret() < fretRange.fretRange.max) {
			current.width = fretRange.fretRange.max - current.fret + 1;
		}
	}

	public static void createFretHandPositions(final ImmutableBeatsMap beats, final List<ChordTemplate> chordTemplates,
			final List<ChordOrNote> sounds, final List<Anchor> anchors) {
		for (final ChordOrNote sound : sounds) {
			final FretRange fretRange = fretRangeFromSound(beats, chordTemplates, sound);
			addFHPIfNeeded(beats, fretRange, anchors);
		}
	}
}
