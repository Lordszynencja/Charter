package log.charter.services.data;

import static log.charter.util.CollectionUtils.firstAfterEqual;
import static log.charter.util.CollectionUtils.lastBefore;
import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;

public class LevelSquisher {
	private static <T extends IFractionalPosition> List<T> getFromTo(final List<T> items, final FractionalPosition from,
			final FractionalPosition to) {
		final int fromId = firstAfterEqual(items, from).findId(items.size() - 1);
		final int toId = lastBefore(items, to).findId(-1);

		if (fromId >= toId) {
			return new ArrayList<>();
		}

		return items.subList(fromId, toId + 1);
	}

	public static void squish(final Arrangement arrangement) {
		final Level squished = new Level();

		final List<EventPoint> phrases = arrangement.eventPoints.stream()//
				.filter(e -> e.phrase != null)//
				.collect(Collectors.toList());

		for (int i = -1; i < phrases.size(); i++) {
			final int levelId = i < 0 ? 0 : arrangement.phrases.get(phrases.get(i).phrase).maxDifficulty;
			final FractionalPosition from = i < 0 ? null : phrases.get(i).position();
			final FractionalPosition to = i + 1 < phrases.size() ? phrases.get(i + 1).position() : null;

			final Level level = arrangement.levels.get(levelId);
			squished.fhps.addAll(map(getFromTo(level.fhps, from, to), FHP::new));
			squished.sounds.addAll(map(getFromTo(level.sounds, from, to), ChordOrNote::from));
			squished.handShapes.addAll(map(getFromTo(level.handShapes, from, to), HandShape::new));
		}

		arrangement.levels.clear();
		arrangement.setLevel(0, squished);
	}
}
