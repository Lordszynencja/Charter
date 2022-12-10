package log.charter.data;

public class UndoState {// TODO
//	private final Vocals vocals;
//	private final Instrument instrument;
//	private final Map<Integer, String> sections;
//	private final TempoMap tempoMap;

	public UndoState(final ChartData data) {// TODO
//		if (data.currentInstrument.type.isVocalsType()) {
//			vocals = new Vocals(data.s.v);
//			instrument = null;
//		} else {
//			vocals = null;
//			instrument = new Instrument(data.currentInstrument);
//		}
//		sections = new HashMap<>();
//		for (final Entry<Integer, String> entry : data.s.sections.entrySet()) {
//			sections.put(entry.getKey(), entry.getValue());
//		}
//		tempoMap = new TempoMap(data.s.tempoMap);
	}

	public UndoState undo(final ChartData data) {// TODO
		final UndoState undo = new UndoState(data);
//		if (vocals != null) {
//			data.s.v = vocals;
//		} else {
//			data.s.setInstrument(instrument);
//			data.currentInstrument = instrument;
//			data.currentNotes = instrument.notes.get(data.currentDiff);
//		}
//		data.s.sections = sections;
//		data.s.tempoMap = tempoMap;
		return undo;
	}
}
