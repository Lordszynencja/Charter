package log.charter.util;

import java.util.ArrayList;
import java.util.List;

public class ExitActions {
	private static final List<Runnable> onExit = new ArrayList<>();

	public static void addOnExit(final Runnable r) {
		onExit.add(r);
	}

	public static void exit() {
		onExit.forEach(Runnable::run);
	}
}
