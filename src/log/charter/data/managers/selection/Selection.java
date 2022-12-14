package log.charter.data.managers.selection;

import log.charter.song.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class Selection<T extends Position> {
	public static <T extends Position> ArrayList2<Selection<T>> getSortedCopy(final ArrayList2<Selection<T>> list) {
		final ArrayList2<Selection<T>> copy = new ArrayList2<>(list);
		copy.sort((selection0, selection1) -> Integer.compare(selection0.selectable.position,
				selection1.selectable.position));

		return copy;
	}

	public final int id;
	public final T selectable;

	public Selection(final int id, final T selectable) {
		this.id = id;
		this.selectable = selectable;
	}
}