package log.charter.data;

public class IdOrPosWithLane extends IdOrPos {
	public static IdOrPosWithLane fromId(final int id, final int pos, final int lane) {
		return new IdOrPosWithLane(id, pos, lane);
	}

	public static IdOrPosWithLane fromPos(final int pos, final int lane) {
		return new IdOrPosWithLane(-1, pos, lane);
	}

	public final int lane;

	public IdOrPosWithLane(final int id, final int pos, final int lane) {
		super(id, pos);
		this.lane = lane;
	}

	public IdOrPosWithLane(final IdOrPos idOrPos, final int lane) {
		super(idOrPos);
		this.lane = lane;
	}

}
