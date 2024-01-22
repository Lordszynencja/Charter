package log.charter.io.gp.gp5;

import java.util.ArrayList;
import java.util.List;

public class GPLyrics {
	private enum LyricState {
		IgnoreSpaces, Begin, Text, Comment, Dash
	}

	public final int startBar;
	public final String text;

	public GPLyrics(final int startBar, final String text) {
		this.startBar = startBar;
		this.text = text;
	}

	public List<String> getChunks(final boolean skipEmptyEntries) {
		if (text == null) {
			return new ArrayList<>();
		}

		final List<String> chunks = new ArrayList<>();
		LyricState state = LyricState.Begin;
		LyricState next = LyricState.Begin;
		boolean skipSpace = false;
		int p = 0;
		int start = 0;

		while (p < text.length()) {
			final char c = text.charAt(p);
			switch (state) {
			case IgnoreSpaces:
				switch (c) {
				case '\r':
				case '\n':
				case '\t':
					break;
				case ' ':
					if (!skipSpace) {
						state = next;
						continue;
					}
					break;
				default:
					skipSpace = false;
					state = next;
					continue;
				}
				break;
			case Begin:
				switch (c) {
				case '[':
					state = LyricState.Comment;
					break;
				default:
					start = p;
					state = LyricState.Text;
					continue;
				}
				break;
			case Comment:
				switch (c) {
				case ']':
					state = LyricState.Begin;
					break;
				}
				break;
			case Text:
				switch (c) {
				case '-':
					state = LyricState.Dash;
					break;
				case '\r':
				case '\n':
				case ' ':
					addChunk(chunks, text.substring(start, p - start), skipEmptyEntries);
					state = LyricState.IgnoreSpaces;
					next = LyricState.Begin;
					break;
				}
				break;
			case Dash:
				switch (c) {
				case '-':
					break;
				default:
					addChunk(chunks, text.substring(start, p - start), skipEmptyEntries);
					skipSpace = true;
					state = LyricState.IgnoreSpaces;
					next = LyricState.Begin;
					continue;
				}
				break;
			}
			p += 1;
		}

		if (state == LyricState.Text) {
			if (p != start) {
				addChunk(chunks, text.substring(start), skipEmptyEntries);
			}
		}

		return chunks;
	}

	private void addChunk(final List<String> chunks, String txt, final boolean skipEmptyEntries) {
		txt = prepareChunk(txt);
		if (!skipEmptyEntries || (txt.length() > 0 && txt.equals("-"))) {
			chunks.add(txt);
		}
	}

	private static String prepareChunk(final String text) {
		final String chunk = String.join(" ", text.split("+"));

		int endLength = chunk.length();
		while (endLength > 0 && chunk.charAt(endLength - 1) == '_') {
			endLength--;
		}

		return endLength != chunk.length() ? chunk.substring(0, endLength) : chunk;
	}
}
