package log.charter.io.midi;

import java.util.HashMap;
import java.util.Map;

import log.charter.song.Instrument.InstrumentType;

public class NoteIds {
	public static final int EVENT_SOLO = 103;
	public static final int EVENT_SP = 116;
	public static final int EVENT_DRUM_ROLL = 126;
	public static final int EVENT_SPECIAL_DRUM_ROLL = 127;

	private static final Map<Integer, ?> emptyMap = new HashMap<>();

	@SuppressWarnings("unchecked")
	private static <M> Map<Integer, M> emptyMap() {
		return (Map<Integer, M>) emptyMap;
	}

	private static final Map<Integer, Map<Integer, Map<Integer, Integer>>> typeDiffLaneToId = new HashMap<>();
	private static final Map<Integer, Map<Integer, Integer>> typeIdToLane = new HashMap<>();
	private static final Map<Integer, Map<Integer, Integer>> typeIdToDiff = new HashMap<>();

	private static <M0, M1, M2> Map<M1, M2> addPath(final Map<M0, Map<M1, M2>> map, final M0 key) {
		map.put(key, map.getOrDefault(key, new HashMap<>()));
		return map.get(key);
	}

	private static void addNoteId(final int id, final int type, final int diff, final int lane) {
		final Map<Integer, Integer> idtoLane = addPath(typeIdToLane, type);
		idtoLane.put(id, lane);

		final Map<Integer, Integer> idtoDiff = addPath(typeIdToDiff, type);
		idtoDiff.put(id, diff);

		final Map<Integer, Map<Integer, Integer>> diffLaneToId = addPath(typeDiffLaneToId, type);
		final Map<Integer, Integer> laneToId = addPath(diffLaneToId, diff);
		laneToId.put(lane, id);
	}

	static {
		// Guitar
		addNoteId(60, 0, 0, 0);
		addNoteId(61, 0, 0, 1);
		addNoteId(62, 0, 0, 2);
		addNoteId(63, 0, 0, 3);
		addNoteId(64, 0, 0, 4);
		addNoteId(65, 0, 0, 5);
		addNoteId(66, 0, 0, 6);

		addNoteId(72, 0, 1, 0);
		addNoteId(73, 0, 1, 1);
		addNoteId(74, 0, 1, 2);
		addNoteId(75, 0, 1, 3);
		addNoteId(76, 0, 1, 4);
		addNoteId(77, 0, 1, 5);
		addNoteId(78, 0, 1, 6);

		addNoteId(84, 0, 2, 0);
		addNoteId(85, 0, 2, 1);
		addNoteId(86, 0, 2, 2);
		addNoteId(87, 0, 2, 3);
		addNoteId(88, 0, 2, 4);
		addNoteId(89, 0, 2, 5);
		addNoteId(90, 0, 2, 6);

		addNoteId(96, 0, 3, 0);
		addNoteId(97, 0, 3, 1);
		addNoteId(98, 0, 3, 2);
		addNoteId(99, 0, 3, 3);
		addNoteId(100, 0, 3, 4);
		addNoteId(101, 0, 3, 5);
		addNoteId(102, 0, 3, 6);

		// Drums
		final int drumsType = getType(InstrumentType.DRUMS);
		for (int diff = 0; diff < 4; diff++) {
			for (int c = 0; c < 5; c++) {
				final int id = 60 + diff * 12 + c;
				addNoteId(id, drumsType, diff, c);
			}
			for (int c = 2; c < 5; c++) {
				addNoteId(108 + c, drumsType, diff, 4 + c);
			}
		}
		addNoteId(95, drumsType, 3, 5);// expert+ bass

//		addNoteId(60, 1, 0, 0);
//		addNoteId(61, 1, 0, 1);
//		addNoteId(62, 1, 0, 2);
//		addNoteId(63, 1, 0, 3);
//		addNoteId(64, 1, 0, 4);
//		addNoteId(110, 1, 0, 6);
//		addNoteId(111, 1, 0, 7);
//		addNoteId(112, 1, 0, 8);

//		addNoteId(72, 1, 1, 0);
//		addNoteId(73, 1, 1, 1);
//		addNoteId(74, 1, 1, 2);
//		addNoteId(75, 1, 1, 3);
//		addNoteId(76, 1, 1, 4);
//		addNoteId(110, 1, 1, 6);
//		addNoteId(111, 1, 1, 7);
//		addNoteId(112, 1, 1, 8);

//		addNoteId(84, 1, 2, 0);
//		addNoteId(85, 1, 2, 1);
//		addNoteId(86, 1, 2, 2);
//		addNoteId(87, 1, 2, 3);
//		addNoteId(88, 1, 2, 4);
//		addNoteId(110, 1, 2, 6);
//		addNoteId(111, 1, 2, 7);
//		addNoteId(112, 1, 2, 8);

//		addNoteId(96, 1, 3, 0);
//		addNoteId(97, 1, 3, 1);
//		addNoteId(98, 1, 3, 2);
//		addNoteId(99, 1, 3, 3);
//		addNoteId(100, 1, 3, 4);
//		addNoteId(110, 1, 3, 6);
//		addNoteId(111, 1, 3, 7);
//		addNoteId(112, 1, 3, 8);

		// Keys
		addNoteId(60, 2, 0, 0);
		addNoteId(61, 2, 0, 1);
		addNoteId(62, 2, 0, 2);
		addNoteId(63, 2, 0, 3);
		addNoteId(64, 2, 0, 4);

		addNoteId(72, 2, 1, 0);
		addNoteId(73, 2, 1, 1);
		addNoteId(74, 2, 1, 2);
		addNoteId(75, 2, 1, 3);
		addNoteId(76, 2, 1, 4);

		addNoteId(84, 2, 2, 0);
		addNoteId(85, 2, 2, 1);
		addNoteId(86, 2, 2, 2);
		addNoteId(87, 2, 2, 3);
		addNoteId(88, 2, 2, 4);

		addNoteId(96, 2, 3, 0);
		addNoteId(97, 2, 3, 1);
		addNoteId(98, 2, 3, 2);
		addNoteId(99, 2, 3, 3);
		addNoteId(100, 2, 3, 4);
	}

	private static int getType(final InstrumentType instrumentType) {
		if (instrumentType.isGuitarType()) {
			return 0;
		}
		if (instrumentType.isDrumsType()) {
			return 1;
		}
		if (instrumentType.isKeysType()) {
			return 2;
		}
		return -1;
	}

	public static int getNoteId(final InstrumentType instrumentType, final int diff, final int lane) {
		return typeDiffLaneToId.getOrDefault(getType(instrumentType), emptyMap())//
				.getOrDefault(diff, emptyMap())//
				.getOrDefault(lane, -1);
	}

	public static int getLane(final InstrumentType instrumentType, final int id) {
		return typeIdToLane.getOrDefault(getType(instrumentType), emptyMap())//
				.getOrDefault(id, -1);
	}

	public static int getDiff(final InstrumentType instrumentType, final int id) {
		return typeIdToDiff.getOrDefault(getType(instrumentType), emptyMap())//
				.getOrDefault(id, -1);
	}
}
