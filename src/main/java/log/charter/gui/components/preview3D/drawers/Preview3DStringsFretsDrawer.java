package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.data.song.notes.ChordOrNote.isLinkedToPrevious;
import static log.charter.gui.components.preview3D.Preview3DUtils.fretThickness;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.stringDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.topStringPosition;
import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBefore;
import static log.charter.util.ColorUtils.mix;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.Position;
import log.charter.gui.components.preview3D.data.FHPDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.util.data.IntRange;

public class Preview3DStringsFretsDrawer {
	private static final int highlightTime = 100;
	private static final int activeTime = 500;

	private ChartData chartData;

	public void init(final ChartData chartData) {
		this.chartData = chartData;
	}

	private void addStrings(final BaseShaderDrawData drawData) {
		final double x0 = getFretPosition(0);
		final double x1 = getFretPosition(InstrumentConfig.frets);

		for (int i = 0; i < chartData.currentStrings(); i++) {
			final Color stringColor = getStringBasedColor(StringColorLabelType.LANE, i, chartData.currentStrings());
			final double y = getStringPosition(i, chartData.currentStrings());
			drawData.addVertex(new Point3D(x0, y, 0), stringColor)//
					.addVertex(new Point3D(x1, y, 0), stringColor);
		}
	}

	private boolean[] getActiveFrets(final Preview3DDrawData drawData) {
		final boolean[] active = new boolean[InstrumentConfig.frets + 1];

		final Integer idTo = lastBefore(drawData.fhps, new Position(drawData.time + activeTime)).findId();
		if (idTo != null) {
			for (int i = 0; i <= idTo; i++) {
				final FHPDrawData fhp = drawData.fhps.get(i);
				for (int fret = max(0, fhp.fretFrom); fret <= min(InstrumentConfig.frets, fhp.fretTo); fret++) {
					active[fret] = true;
				}
			}
		}

		final IntRange frets = drawData.getFrets(drawData.time);
		if (frets != null) {
			for (int fret = frets.min - 1; fret <= frets.max; fret++) {
				active[fret] = true;
			}
		}

		return active;
	}

	private double[] getFretHighlight(final Preview3DDrawData drawData) {
		if (chartData.currentArrangementLevel() == null) {
			return new double[InstrumentConfig.frets + 1];
		}

		final ImmutableBeatsMap beats = chartData.beats();
		final double[] highlightValues = new double[InstrumentConfig.frets + 1];
		final List<ChordOrNote> sounds = chartData.currentSounds();
		final Integer idFrom = firstAfter(sounds, FractionalPosition.fromTime(beats, drawData.time - highlightTime))
				.findId();
		final Integer idTo = lastBefore(sounds, FractionalPosition.fromTime(beats, drawData.time)).findId();
		if (idFrom == null || idTo == null) {
			return new double[InstrumentConfig.frets + 1];
		}

		for (int i = idFrom; i <= idTo; i++) {
			final ChordOrNote sound = sounds.get(i);
			if (isLinkedToPrevious(sound, i, sounds)) {
				continue;
			}

			final double highlightValue = highlightTime - drawData.time + sound.position(beats);
			if (sound.isNote() && sound.note().fret != 0) {
				highlightValues[sound.note().fret - 1] = highlightValue;
				highlightValues[sound.note().fret] = highlightValue;
			} else {
				final IntRange frets = drawData.getFrets(sound.position(beats));
				if (frets != null) {
					highlightValues[frets.min - 1] = highlightValue;
					highlightValues[frets.max] = highlightValue;
				}
			}
		}

		final double[] highlight = new double[highlightValues.length];
		for (int fret = 0; fret < highlight.length; fret++) {
			highlight[fret] = Math.sqrt(1.0 * highlightValues[fret] / highlightTime);
		}

		return highlight;
	}

	private void addFrets(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final BaseShaderDrawData shaderdrawData = shadersHolder.new BaseShaderDrawData();
		final boolean[] activeFrets = getActiveFrets(drawData);
		final double[] fretHighlight = getFretHighlight(drawData);
		final Color inactiveColor = ColorLabel.PREVIEW_3D_FRET.color();
		final Color activeColor = ColorLabel.PREVIEW_3D_ACTIVE_FRET.color();
		final Color highlightColor = ColorLabel.PREVIEW_3D_HIGHLIGHTED_FRET.color();

		final double y0 = topStringPosition + stringDistance / 2;
		final double y1 = topStringPosition - stringDistance * (chartData.currentStrings() - 0.5);

		for (int fret = 0; fret <= InstrumentConfig.frets; fret++) {
			Color fretColor = activeFrets[fret] ? activeColor : inactiveColor;
			double offset = fretThickness;
			if (fretHighlight[fret] > 0) {
				fretColor = mix(fretColor, highlightColor, fretHighlight[fret]);
				offset *= 1 + 3 * fretHighlight[fret];
			}

			final double x = getFretPosition(fret);

			shaderdrawData.addVertex(new Point3D(x - offset, y0, -0.01), fretColor)//
					.addVertex(new Point3D(x - offset, y1, -0.01), fretColor)//
					.addVertex(new Point3D(x + offset, y1, -0.01), fretColor)//
					.addVertex(new Point3D(x + offset, y0, -0.01), fretColor);
		}

		shaderdrawData.draw(GL30.GL_QUADS, Matrix4.identity);
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final BaseShaderDrawData shaderDrawData = shadersHolder.new BaseShaderDrawData();

		addStrings(shaderDrawData);
		addFrets(shadersHolder, drawData);

		GL30.glLineWidth(2);
		shaderDrawData.draw(GL30.GL_LINES, Matrix4.identity);
	}
}
