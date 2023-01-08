package log.charter.data.config;

public class Zoom {
	public static double zoom = Math.pow(0.99, Config.zoomLvl);

	public static void addZoom(final int change) {
		Config.zoomLvl -= change;
		Config.markChanged();

		setZoomBasedOnConfig();
	}

	private static void setZoomBasedOnConfig() {
		zoom = Math.pow(0.99, Config.zoomLvl);
	}
}
