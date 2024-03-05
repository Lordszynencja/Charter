package log.charter.gui.components.containers;

public interface SaverWithStatus {
	public static final SaverWithStatus emptySaver = () -> true;

	public static SaverWithStatus defaultFor(final Runnable action) {
		if (action == null) {
			return emptySaver;
		}

		return () -> {
			action.run();
			return true;
		};
	}

	boolean save();
}