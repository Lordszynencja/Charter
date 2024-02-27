package log.charter.io.midi;

import static log.charter.io.Logger.error;

import java.util.ArrayList;
import java.util.List;

public class TempoMap {
	private static List<Tempo> getJoinedTempos(final List<Tempo> tempos) {
		final List<Tempo> newTempos = new ArrayList<>(tempos.size());
		for (int i = 0; i < tempos.size(); i++) {
			final Tempo tmp = tempos.get(i);
			if (i < tempos.size() - 1) {
				Tempo nextTmp = tempos.get(i + 1);
				while (tmp.pos == nextTmp.pos) {
					tmp.kbpm = tmp.kbpm > 0 ? tmp.kbpm : nextTmp.kbpm;
					tmp.numerator = tmp.numerator > 0 ? tmp.numerator : nextTmp.numerator;
					tmp.denominator = tmp.denominator > 0 ? tmp.denominator : nextTmp.denominator;

					i++;
					nextTmp = tempos.get(i + 1);
				}
			}

			newTempos.add(tmp);
		}

		fillMissingTempoData(newTempos);

		return newTempos;
	}

	private static void fillMissingTempoData(final List<Tempo> newTempos) {
		int lastKbpm = 120000;
		int lastNumerator = 4;
		int lastDenominator = 4;

		for (int i = 0; i < newTempos.size(); i++) {
			final Tempo tempo = newTempos.get(i);
			if (tempo.kbpm <= 0) {
				tempo.kbpm = lastKbpm;
			}
			if (tempo.numerator <= 0) {
				tempo.numerator = lastNumerator;
			}
			if (tempo.denominator <= 0) {
				tempo.denominator = lastDenominator;
			}

			lastKbpm = tempo.kbpm;
			lastNumerator = tempo.numerator;
			lastDenominator = tempo.denominator;
		}
	}

	public final List<Tempo> tempos = new ArrayList<>();
	public boolean isMs = false;

	public TempoMap() {
		tempos.add(new Tempo());
	}

	public TempoMap(final List<Tempo> tempos) {
		if (tempos.isEmpty()) {
			this.tempos.add(new Tempo());
		} else {
			this.tempos.addAll(tempos);
			tempos.sort((a, b) -> Integer.compare(a.id, b.id));
		}
	}

	public void convertToMs() {
		if (isMs) {
			return;
		}
		if (tempos.get(0).pos != 0) {
			error("first beat is not on zero: " + tempos.get(0));
			return;
		}

		Tempo previous = tempos.get(0);
		for (int i = 1; i < tempos.size(); i++) {
			final Tempo tempo = tempos.get(i);
			tempo.pos = previous.pos + (tempo.id - previous.id) * (60_000_000.0 / previous.kbpm);
			previous = tempo;
		}

		isMs = true;
	}

	public void join() {
		final List<Tempo> newTempos = getJoinedTempos(tempos);
		tempos.clear();
		tempos.addAll(newTempos);
	}
}
