package log.charter.data;

public class IdOrPos {
	public static IdOrPos fromId(final int id, final int pos) {
		return new IdOrPos(id, pos);
	}

	public static IdOrPos fromPos(final int pos) {
		return new IdOrPos(-1, pos);
	}

	public final int id;
	public final int pos;

	public IdOrPos(final int id, final int pos) {
		this.id = id;
		this.pos = pos;
	}

	public IdOrPos(final IdOrPos idOrPos) {
		id = idOrPos.id;
		pos = idOrPos.pos;
	}

	public boolean isId() {
		return id >= 0;
	}

	public boolean isPos() {
		return id < 0;
	}
}