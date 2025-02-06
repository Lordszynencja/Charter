package log.charter.data.config;

public enum SystemType {
	LINUX, MAC, WINDOWS, OTHER;

	public static final SystemType systemType = MAC;

	private static SystemType findSystemType() {
		final String os = System.getProperty("os.name").toLowerCase();
		return os.startsWith("windows") ? WINDOWS//
				: os.startsWith("mac") ? MAC//
						: LINUX;
	}

	public static boolean is(final SystemType... types) {
		for (final SystemType type : types) {
			if (systemType == type) {
				return true;
			}
		}

		return false;
	}

	public static boolean not(final SystemType... types) {
		return !is(types);
	}
}
