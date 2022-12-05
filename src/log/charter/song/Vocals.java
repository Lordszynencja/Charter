package log.charter.song;

import java.util.ArrayList;
import java.util.List;

public class Vocals {
	public final List<Lyric> lyrics;
	public final List<Event> lyricLines;

	public Vocals() {
		lyrics = new ArrayList<>();
		lyricLines = new ArrayList<>();
	}

	public Vocals(final Vocals vocals) {
		lyrics = new ArrayList<>(vocals.lyrics.size());
		for (final Lyric l : vocals.lyrics) {
			lyrics.add(new Lyric(l));
		}

		lyricLines = new ArrayList<>(vocals.lyricLines.size());
		for (final Event e : vocals.lyricLines) {
			lyricLines.add(new Event(e));
		}
	}

	public void fixNotes() {
		sort();

		final int size = lyrics.size();
		for (int j = 0; j < (size - 1); j++) {
			final Lyric l0 = lyrics.get(j);
			final Lyric l1 = lyrics.get(j + 1);
			if ((l0.pos + l0.getLength()) >= l1.pos) {
				l0.setLength(l1.pos - l0.pos);
			}
		}
	}

	public boolean hasNotes() {
		return lyrics.isEmpty();
	}

	public void sort() {
		lyrics.sort(null);
		lyricLines.sort(null);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Vocals{notes: [");

		boolean first = true;
		for (final Lyric l : lyrics) {
			sb.append(first ? "" : ",\n\t\t\t\t").append(l);
			first = false;
		}
		sb.append("]},\n\t\tlyricLines: [");

		first = true;
		for (final Event e : lyricLines) {
			sb.append(first ? "" : ",\n\t\t").append(e);
			first = false;
		}
		sb.append("]}");

		return sb.toString();
	}

}
