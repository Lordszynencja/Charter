package log.charter.gui.chartPanelDrawers.data;

import java.awt.Graphics2D;
import java.util.Map;

import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Level;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.vocals.Vocals;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.util.collections.Pair;

public class FrameData {
	public final ImmutableBeatsMap beats;
	public final Map<Integer, Double> bookmarks;
	public final Vocals vocals;
	public final Arrangement arrangement;
	public final Level level;
	public final Pair<Double, Double> repeaterSpan;
	public final ISelectionAccessor<? extends IVirtualConstantPosition> selection;
	public final double time;
	public final Graphics2D g;
	public final HighlightData highlightData;
	public final boolean ctrlPressed;

	public FrameData(final ImmutableBeatsMap beats, final Map<Integer, Double> bookmarks, final Vocals vocals,
			final Arrangement arrangement, final Level level, final Pair<Double, Double> repeaterSpan,
			final ISelectionAccessor<? extends IVirtualConstantPosition> selection, final double time,
			final Graphics2D g, final HighlightData highlightData, final boolean ctrlPressed) {
		this.beats = beats;
		this.bookmarks = bookmarks;
		this.vocals = vocals;
		this.arrangement = arrangement;
		this.level = level;
		this.repeaterSpan = repeaterSpan;
		this.selection = selection;
		this.time = time;
		this.g = g;
		this.highlightData = highlightData;
		this.ctrlPressed = ctrlPressed;
	}

	public FrameData spawnSubData(final Graphics2D g) {
		return new FrameData(beats, bookmarks, vocals, arrangement, level, repeaterSpan, selection, time, g,
				highlightData, ctrlPressed);
	}

}
