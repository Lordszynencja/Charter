package log.charter.data.config;

public class ZoomUtils {
	public static double zoom = Math.pow(0.99, Config.zoomLvl);

	public static void changeZoom(final int change) {
		Config.zoomLvl -= change;
		Config.markChanged();

		setZoomBasedOnConfig();
	}

	private static void setZoomBasedOnConfig() {
		zoom = Math.pow(0.99, Config.zoomLvl);
	}
}
