package log.charter.song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.io.Logger;
import log.charter.song.Instrument.InstrumentType;

public class Song {

	public static void addNote(final List<List<Note>> list, final Note n, final int diff) {
		if ((diff < 0) || (diff > 255)) {
			return;
		}

		while (list.size() < diff) {
			list.add(new ArrayList<>());
		}

		list.get(diff).add(n);
	}

	public Instrument g;
	public Instrument gc;
	public Instrument gr;
	public Instrument b;
	public Instrument d;
	public Instrument k;
	public final Instrument vInstrument = new Instrument(InstrumentType.VOCALS);
	public Vocals v;

	public Map<Integer, String> sections = new HashMap<>();
	public TempoMap tempoMap;

	public Song() {
		g = new Instrument(InstrumentType.GUITAR);
		gc = new Instrument(InstrumentType.GUITAR_COOP);
		gr = new Instrument(InstrumentType.GUITAR_RHYTHM);
		b = new Instrument(InstrumentType.BASS);
		d = new Instrument(InstrumentType.DRUMS);
		k = new Instrument(InstrumentType.KEYS);
		v = new Vocals();
		tempoMap = new TempoMap(new ArrayList<>());
	}

	public Song(final Song s) {
		g = new Instrument(s.g);
		gc = new Instrument(s.gc);
		gr = new Instrument(s.gr);
		b = new Instrument(s.b);
		d = new Instrument(s.d);
		k = new Instrument(s.k);
		v = new Vocals(s.v);
		s.sections.forEach((id, sec) -> sections.put(id, sec));

		tempoMap = new TempoMap(s.tempoMap);
	}

	public Instrument getInstrument(final InstrumentType type) {
		switch (type) {
		case GUITAR:
			return g;
		case GUITAR_COOP:
			return gc;
		case GUITAR_RHYTHM:
			return gr;
		case BASS:
			return b;
		case DRUMS:
			return d;
		case KEYS:
			return k;
		case VOCALS:
			return vInstrument;
		default:
			Logger.error("Wrong instrument type: " + type);
			return null;
		}
	}

	public Instrument[] instruments() {
		return new Instrument[] { g, gc, gr, b, d, k };
	}

	public void setInstrument(final Instrument instrument) {
		switch (instrument.type) {
		case GUITAR:
			g = instrument;
			return;
		case GUITAR_COOP:
			gc = instrument;
			return;
		case GUITAR_RHYTHM:
			gr = instrument;
			return;
		case BASS:
			b = instrument;
			return;
		case DRUMS:
			d = instrument;
			return;
		case KEYS:
			k = instrument;
			return;
		case VOCALS:
			return;
		default:
			Logger.error("Wrong instrument type: " + instrument.type);
			return;
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Song{g: ").append(g)//
				.append(",\n\tgc: ").append(gc)//
				.append(",\n\tgr: ").append(gr)//
				.append(",\n\tb: ").append(b)//
				.append(",\n\td: ").append(d)//
				.append(",\n\tk: ").append(k)//
				.append(",\n\tv: ").append(v)//
				.append(",\n\tsections: [");

		boolean first = true;
		for (final Entry<Integer, String> pair : sections.entrySet()) {
			sb.append(first ? "" : ",\n\t\t").append("(" + pair.getKey() + "," + pair.getValue() + ")");
			first = false;
		}
		sb.append("],\n\ttempoMap: ").append(tempoMap.toString().replaceAll("\n\t", "\n\t\t\t")).append("}");

		return sb.toString();
	}

}
