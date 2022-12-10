package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("vocals")
@XStreamInclude(value = { Vocal.class })
public class Vocals {

	public ArrayList2<Vocal> vocals = new ArrayList2<>();

	public Vocals() {
	}

	public Vocals(final ArrayList2<Vocal> vocals) {
		this.vocals = vocals;
	}

	public int insertNote(final int pos, final String text, final boolean wordPart, final boolean phraseEnd) {
		final Vocal vocal = new Vocal(pos, text, wordPart, phraseEnd);

		if (vocals.isEmpty() || vocals.getLast().time < pos) {
			vocals.add(vocal);
			return vocals.size() - 1;
		}

		for (int i = vocals.size() - 1; i >= 0; i--) {
			if (vocals.get(i).time < pos) {
				vocals.add(i + 1, vocal);
				return i + 1;
			}
		}

		vocals.add(0, vocal);
		return 0;
	}

	public void removeNote(final int id) {
		vocals.remove(id);
	}
}