package log.charter.gui.components.preview3D.camera;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.topStringPosition;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.cameraMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.rotationXMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.scaleMatrix;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.Anchor;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.Position;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.services.data.ChartTimeHandler;

public class Preview3DCameraHandler {

	private static final Matrix4 baseCameraPerspectiveMatrix = cameraMatrix(-0.3, -0.3, -0.3, -1)//
			.multiply(scaleMatrix(1, 1, -1));

	private final static int fretFocusWindowStartOffset = 0;
	private final static int fretFocusWindowEndOffset = 3000;
	private static final double focusingSpeed = 0.7;

	private final static double minScreenScaleX = 0.5;
	private final static double screenScaleXMultiplier = 1;
	private final static double minScreenScaleY = 1;
	private final static double screenScaleYMultiplier = 0.5;

	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;

	private double camX = 2;
	private double fretSpan = 4;

	public Matrix4 currentMatrix;

	public void init(final ChartTimeHandler chartTimeHandler, final ChartData data) {
		this.chartTimeHandler = chartTimeHandler;
		chartData = data;
	}

	private double mix(final double a, final double b, final double mix) {
		return a * mix + b * (1 - mix);
	}

	public void updateFretFocus(final double frameTime) {
		final List<Anchor> anchors = chartData.currentArrangementLevel().anchors;
		int minFret = Config.frets;
		int maxFret = 1;

		final FractionalPosition start = new Position(chartTimeHandler.time() + fretFocusWindowStartOffset)
				.positionAsFraction(chartData.beats());
		final int anchorsFrom = lastBeforeEqual(anchors, start).findId(0);

		final FractionalPosition end = new Position(chartTimeHandler.time() + fretFocusWindowEndOffset)
				.positionAsFraction(chartData.beats());
		final Integer anchorsTo = lastBeforeEqual(anchors, end).findId();
		if (anchorsTo == null) {
			return;
		}
		// TODO add weighted average instead of focusing speed

		for (int i = anchorsFrom; i <= anchorsTo; i++) {
			final Anchor anchor = anchors.get(i);
			if (anchor.fret < minFret) {
				minFret = anchor.fret;
			}
			if (anchor.topFret() > maxFret) {
				maxFret = anchor.topFret();
			}
		}

		final double focusSpeed = 1 - pow(1 - focusingSpeed, frameTime);
		final double targetCamX = (getFretPosition(maxFret) + getFretPosition(minFret - 1)) / 2;
		camX = mix(targetCamX, camX, focusSpeed);
		final double targetFretSpan = (maxFret - minFret + 1);
		fretSpan = mix(targetFretSpan, fretSpan, focusSpeed);
	}

	public void updateCamera(final double aspectRatio) {
		final double camY = 1 + topStringPosition + (fretSpan - 4) * 0.2;
		final double camZ = -3.6 + (fretSpan - 4) * 0.005;
		final double camRotationX = 0.2 + (fretSpan - 4) * 0.01;

		final double screenScaleX = min(minScreenScaleX, screenScaleXMultiplier / aspectRatio);
		final double screenScaleY = min(minScreenScaleY, screenScaleYMultiplier * aspectRatio);

		currentMatrix = scaleMatrix(screenScaleX, screenScaleY, 1 / 10.0)//
				.multiply(baseCameraPerspectiveMatrix)//
				.multiply(rotationXMatrix(camRotationX))//
				.multiply(moveMatrix(-camX, -camY, -camZ));
	}
}
