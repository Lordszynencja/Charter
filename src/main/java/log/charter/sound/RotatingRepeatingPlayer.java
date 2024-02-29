package log.charter.sound;

import java.util.function.Supplier;

import log.charter.sound.data.MusicData;

public class RotatingRepeatingPlayer implements IPlayer {

	private int nextPlayer = 0;
	private final RepeatingPlayer[] players;

	public RotatingRepeatingPlayer(final Supplier<MusicData<?>> musicDataSupplier, final int players) {
		this.players = new RepeatingPlayer[players];
		for (int i = 0; i < players; i++) {
			this.players[i] = new RepeatingPlayer(musicDataSupplier);
		}
	}

	@Override
	public void play() {
		players[nextPlayer++].play();
		nextPlayer = nextPlayer % players.length;
	}
}
