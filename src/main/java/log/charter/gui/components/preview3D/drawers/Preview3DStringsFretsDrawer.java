package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.preview3D.Preview3DUtils.fretThickness;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.stringDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.topStringPosition;
import static log.charter.song.notes.ChordOrNote.isLinkedToPrevious;
import static log.charter.song.notes.IConstantPosition.findFirstIdAfter;
import static log.charter.song.notes.IConstantPosition.findLastBeforeEqual;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;
import static log.charter.util.ColorUtils.mix;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.song.Anchor;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DStringsFretsDrawer {
	private static final int highlightTime = 100;
	private static final int activeTime = 500;

	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void addStrings(final BaseShaderDrawData drawData) {
		final double x0 = getFretPosition(0);
		final double x1 = getFretPosition(Config.frets);

		for (int i = 0; i < data.currentStrings(); i++) {
			final Color stringColor = getStringBasedColor(StringColorLabelType.LANE, i, data.currentStrings());
			final double y = getStringPosition(i, data.currentStrings());
			drawData.addVertex(new Point3D(x0, y, 0), stringColor)//
					.addVertex(new Point3D(x1, y, 0), stringColor);
		}
	}

	private boolean[] getActiveFrets() {
		final boolean[] active = new boolean[Config.frets + 1];
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;
		final int idFrom = findLastIdBefore(anchors, data.time);
		if (idFrom < 0) {
			return active;
		}
		final int idTo = findLastIdBefore(anchors, data.time + activeTime);
		for (int i = idFrom; i <= idTo; i++) {
			final Anchor anchor = anchors.get(i);
			for (int fret = max(0, anchor.fret - 1); fret <= Math.min(Config.frets, anchor.topFret()); fret++) {
				active[fret] = true;
			}
		}

		return active;
	}

	private double[] getFretHighlight() {
		final int[] highlightValues = new int[Config.frets + 1];
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().chordsAndNotes;
		final int idFrom = findFirstIdAfter(sounds, data.time - highlightTime);
		if (idFrom < 0) {
			return new double[highlightValues.length];
		}

		final int idTo = findLastIdBefore(sounds, data.time);
		for (int i = idFrom; i <= idTo; i++) {
			final ChordOrNote sound = sounds.get(i);
			if (isLinkedToPrevious(sound, i, sounds)) {
				continue;
			}

			final int highlightValue = highlightTime - data.time + sound.position();
			if (sound.isNote() && sound.note.fret != 0) {
				highlightValues[sound.note.fret - 1] = highlightValue;
				highlightValues[sound.note.fret] = highlightValue;
			} else {
				final Anchor anchor = findLastBeforeEqual(anchors, sound.position());
				if (anchor != null) {
					highlightValues[anchor.fret - 1] = highlightValue;
					highlightValues[anchor.topFret()] = highlightValue;
				}
			}
		}

		final double[] highlight = new double[highlightValues.length];
		for (int fret = 0; fret < highlight.length; fret++) {
			highlight[fret] = Math.sqrt(1.0 * highlightValues[fret] / highlightTime);
		}

		return highlight;
	}

	private void addFrets(final ShadersHolder shadersHolder) {
		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();
		final boolean[] activeFrets = getActiveFrets();
		final double[] fretHighlight = getFretHighlight();
		final Color inactiveColor = ColorLabel.PREVIEW_3D_FRET.color();
		final Color activeColor = ColorLabel.PREVIEW_3D_ACTIVE_FRET.color();
		final Color highlightColor = ColorLabel.PREVIEW_3D_HIGHLIGHTED_FRET.color();

		final double y0 = topStringPosition + stringDistance / 2;
		final double y1 = topStringPosition - stringDistance * (data.currentStrings() - 0.5);

		for (int fret = 0; fret <= Config.frets; fret++) {
			Color fretColor = activeFrets[fret] ? activeColor : inactiveColor;
			double offset = fretThickness;
			if (fretHighlight[fret] > 0) {
				fretColor = mix(fretColor, highlightColor, fretHighlight[fret]);
				offset *= 1 + 3 * fretHighlight[fret];
			}

			final double x = getFretPosition(fret);

			drawData.addVertex(new Point3D(x - offset, y0, -0.01), fretColor)//
					.addVertex(new Point3D(x - offset, y1, -0.01), fretColor)//
					.addVertex(new Point3D(x + offset, y1, -0.01), fretColor)//
					.addVertex(new Point3D(x + offset, y0, -0.01), fretColor);
		}

		drawData.draw(GL30.GL_QUADS, Matrix4.identity);
	}

	public void draw(final ShadersHolder shadersHolder) {
		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();

		addStrings(drawData);
		addFrets(shadersHolder);

		GL30.glLineWidth(2);
		drawData.draw(GL30.GL_LINES, Matrix4.identity);
	}
}
