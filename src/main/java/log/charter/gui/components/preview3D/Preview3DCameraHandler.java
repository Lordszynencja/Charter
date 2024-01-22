package log.charter.gui.components.preview3D;

import static java.lang.Math.min;
import static log.charter.gui.components.preview3D.Matrix4.cameraMatrix;
import static log.charter.gui.components.preview3D.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.Matrix4.rotationXMatrix;
import static log.charter.gui.components.preview3D.Matrix4.scaleMatrix;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTopStringYPosition;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.song.Anchor;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DCameraHandler {
	private final static int fretFocusWindowStartOffset = 1000;
	private final static int fretFocusWindowEndOffset = 5000;
	private final static double focusingSpeedMultiplier = 0.002;

	private final static double minScreenScaleX = 0.5;
	private final static double screenScaleXMultiplier = 1;
	private final static double minScreenScaleY = 1;
	private final static double screenScaleYMultiplier = 0.5;

	private ChartData data;

	private double camX = 2;
	private double fretSpan = 4;

	public Matrix4 currentMatrix;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void updateFretFocus() {
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;
		int minFret = Config.frets;
		int maxFret = 1;

		int anchorsFrom = findLastIdBeforeEqual(anchors, data.time + fretFocusWindowStartOffset);
		if (anchorsFrom == -1) {
			anchorsFrom = 0;
		}
		final int anchorsTo = findLastIdBeforeEqual(anchors, data.time + fretFocusWindowEndOffset);
		if (anchorsTo == -1) {
			return;
		}

		for (int i = anchorsFrom; i <= anchorsTo; i++) {
			final Anchor anchor = anchors.get(i);
			if (anchor.fret < minFret) {
				minFret = anchor.fret;
			}
			if (anchor.topFret() > maxFret) {
				maxFret = anchor.topFret();
			}
		}

		final double focusingSpeed = Math.pow(0.99, Config.FPS * focusingSpeedMultiplier);
		camX = camX * focusingSpeed
				+ (getFretPosition(maxFret) + getFretPosition(minFret - 1)) / 2 * (1 - focusingSpeed);
		fretSpan = fretSpan * focusingSpeed + (maxFret - minFret + 1) * (1 - focusingSpeed);
	}

	public void updateCamera(final double aspectRatio, final double x, final double y) {
		updateFretFocus();

		final double camY = 1 + getTopStringYPosition() + (fretSpan - 4) * 0.2;
		final double camZ = -2.6 + (fretSpan - 4) * 0.005;
		final double camRotationX = 0.2 + (fretSpan - 4) * 0.015;

		final double screenScaleX = min(minScreenScaleX, screenScaleXMultiplier / aspectRatio);
		final double screenScaleY = min(minScreenScaleY, screenScaleYMultiplier * aspectRatio);

		currentMatrix = scaleMatrix(screenScaleX, screenScaleY, 1 / 10.0)//
				.multiply(cameraMatrix(-0.3, -0.3, -0.3, -1))//
				.multiply(scaleMatrix(10, 1, -1))//
				.multiply(rotationXMatrix(camRotationX))//
				.multiply(moveMatrix(-camX, -camY, -camZ))//
				.multiply(scaleMatrix(1, 1, 1));
	}
}
