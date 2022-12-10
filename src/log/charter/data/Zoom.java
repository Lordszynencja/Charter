package log.charter.data;

public class Zoom {
	public static double zoom = Math.pow(0.99, Config.zoomLvl);

	public static void addZoom(final int change) {
		Config.zoomLvl += change;
		setZoomBasedOnConfig();
	}

	private static void setZoomBasedOnConfig() {
		zoom = Math.pow(0.99, Config.zoomLvl);
	}
}
