package log.charter.gui.components.preview3D.camera;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.topStringPosition;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.cameraMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.rotationXMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.scaleMatrix;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.song.Anchor;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DCameraHandler {

	private static final Matrix4 baseCameraPerspectiveMatrix = cameraMatrix(-0.3, -0.3, -0.3, -1)//
			.multiply(scaleMatrix(1, 1, -1));

	private final static int fretFocusWindowStartOffset = 0;
	private final static int fretFocusWindowEndOffset = 3000;
	private static final double focusingSpeed = pow(0.99, 0.2);

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

	public void updateFretFocus() {
		if (data.getCurrentArrangementLevel() == null) {
			return;
		}

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

		camX = camX * focusingSpeed
				+ (getFretPosition(maxFret) + getFretPosition(minFret - 1)) / 2 * (1 - focusingSpeed);
		fretSpan = fretSpan * focusingSpeed + (maxFret - minFret + 1) * (1 - focusingSpeed);
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
