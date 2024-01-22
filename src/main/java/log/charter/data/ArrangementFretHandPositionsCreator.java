package log.charter.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.frets;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import log.charter.data.config.Config;
import log.charter.song.Anchor;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.Position;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.IntRange;

public class ArrangementFretHandPositionsCreator {
	private static class FretRange extends Position {
		public final boolean isNote;
		public final boolean isTap;
		public final IntRange fretRange;

		public FretRange(final IPosition position, final int fret, final boolean isTap) {
			super(position);
			isNote = true;
			this.isTap = isTap;
			fretRange = new IntRange(fret, fret);
		}

		public FretRange(final IPosition position, final int minFret, final int maxFret) {
			super(position);
			isNote = false;
			isTap = false;
			fretRange = new IntRange(minFret, maxFret);
		}
	}

	private static FretRange fretRangeFromSound(final ArrayList2<ChordTemplate> chordTemplates,
			final ChordOrNote sound) {
		if (sound.isNote()) {
			return new FretRange(sound, sound.note.fret, sound.note.hopo == HOPO.TAP);
		}

		final ChordTemplate template = chordTemplates.get(sound.chord.templateId());
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
			return new FretRange(sound, 0, false);
		}

		return new FretRange(sound, minFret, maxFret);
	}

	private static void addFHP(final FretRange fretRange, final int index, final ArrayList2<Anchor> anchors) {
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

	private static void addFHPIfNeeded(final FretRange fretRange, final ArrayList2<Anchor> anchors) {
		final int currentAnchorId = findLastIdBeforeEqual(anchors, fretRange);
		if (currentAnchorId == -1) {
			addFHP(fretRange, 0, anchors);
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
					addFHP(fretRange, currentAnchorId + 1, anchors);
				}
			}

			return;
		}

		if (current.fret != fretRange.fretRange.min) {
			addFHP(fretRange, currentAnchorId + 1, anchors);
			return;
		}

		if (current.topFret() < fretRange.fretRange.max) {
			current.width = fretRange.fretRange.max - current.fret + 1;
		}
	}

	public static void createFretHandPositions(final ArrayList2<ChordTemplate> chordTemplates,
			final ArrayList2<ChordOrNote> sounds, final ArrayList2<Anchor> anchors) {
		for (final ChordOrNote sound : sounds) {
			final FretRange fretRange = fretRangeFromSound(chordTemplates, sound);
			addFHPIfNeeded(fretRange, anchors);
		}
	}

}
