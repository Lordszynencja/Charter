package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.max;
import static log.charter.data.config.Config.showChordIds;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.data.config.GraphicalConfig.noteWidth;
import static log.charter.data.song.notes.ChordOrNote.findPreviousSoundOnString;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData.fromChord;
import static log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData.fromNote;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer.getHighwayDrawer;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Font;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;
import log.charter.util.data.Position2D;

public class GuitarDrawer {
	public static void reloadGraphics() {
		HighwayDrawer.reloadGraphics();
	}

	public static final BigDecimal bendStepSize = new BigDecimal("10");

	private static boolean isPastRightEdge(final int x, final int width) {
		return x > (width + noteWidth / 2);
	}

	private static boolean isOnScreen(final int x, final int length) {
		return x + max(length, noteWidth / 2) >= 0;
	}

	private BeatsDrawer beatsDrawer;
	private ChartPanel chartPanel;
	private LyricLinesDrawer lyricLinesDrawer;
	private WaveFormDrawer waveFormDrawer;

	public void lyricLinesDrawer(final LyricLinesDrawer lyricLinesDrawer) {
		this.lyricLinesDrawer = lyricLinesDrawer;
	}

	private void drawGuitarLanes(final FrameData frameData, final int panelWidth) {
		final int strings = frameData.arrangement.tuning.strings();
		final int x = max(0, timeToX(0, frameData.time));

		for (int lane = 0; lane < strings; lane++) {
			frameData.g.setColor(getStringBasedColor(StringColorLabelType.LANE, lane, strings));
			final int y = getLaneY(getStringPosition(lane, strings));
			frameData.g.drawLine(x, y, panelWidth, y);
		}
	}

	private boolean addChord(final FrameData frameData, final HighwayDrawer highwayDrawer, final int panelWidth,
			final Chord chord, final boolean selected, final int highlightedString, final boolean lastWasLinkNext,
			final boolean wrongLinkNext) {
		final int x = timeToX(chord.position(), frameData.time);
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		final int length = timeToXLength(chord.position(), chord.length());
		if (!isOnScreen(x, length)) {
			return true;
		}

		final ChordTemplate chordTemplate = frameData.arrangement.chordTemplates.get(chord.templateId());
		for (final EditorNoteDrawingData noteData : fromChord(chord, chordTemplate, x, selected, highlightedString,
				lastWasLinkNext, wrongLinkNext, frameData.ctrlPressed)) {
			highwayDrawer.addNote(noteData);
		}

		String chordName = chordTemplate.chordName;
		if (showChordIds) {
			chordName = (chordName == null || chordName.isBlank()) ? "[" + chord.templateId() + "]"
					: chordName + " [" + chord.templateId() + "]";
		}
		if (chordName != null) {
			highwayDrawer.addChordName(x, chordName);

		}

		return true;
	}

	private boolean addNote(final int time, final HighwayDrawer highwayDrawer, final int panelWidth, final Note note,
			final boolean selected, final boolean highlighted, final boolean lastWasLinkNext,
			final boolean wrongLinkNext) {
		final int x = timeToX(note.position(), time);
		final int length = timeToXLength(note.position(), note.length());
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		if (!isOnScreen(x, length)) {
			return true;
		}

		highwayDrawer.addNote(fromNote(x, note, selected, highlighted, lastWasLinkNext, wrongLinkNext));

		return true;
	}

	private boolean addChordOrNote(final FrameData frameData, final HighwayDrawer highwayDrawer, final int panelWidth,
			final ChordOrNote chordOrNote, final boolean selected, final int highlightedString,
			final boolean lastWasLinkNext, final boolean wrongLinkNext) {
		if (chordOrNote.isChord()) {
			return addChord(frameData, highwayDrawer, panelWidth, chordOrNote.chord(), selected, highlightedString,
					lastWasLinkNext, wrongLinkNext);
		}
		if (chordOrNote.isNote()) {
			return addNote(frameData.time, highwayDrawer, panelWidth, chordOrNote.note(), selected,
					highlightedString == chordOrNote.note().string, lastWasLinkNext, wrongLinkNext);
		}

		return true;
	}

	private boolean isLinkNextIncorrect(final List<ChordOrNote> sounds, final List<ChordTemplate> chordTemplates,
			final int id, final int string, final int fret) {
		final ChordOrNote previousSound = findPreviousSoundOnString(string, id - 1, sounds);

		if (previousSound == null) {
			return true;
		}
		if (previousSound.isChord()) {
			final ChordNote chordNote = previousSound.chord().chordNotes.get(string);
			if (chordNote == null) {
				return true;
			}

			if (chordNote.slideTo == null) {
				final ChordTemplate chordTemplate = chordTemplates.get(previousSound.chord().templateId());
				if (chordTemplate.frets.getOrDefault(string, -1) != fret) {
					return true;
				}
			} else {
				if (chordNote.unpitchedSlide) {
					return true;
				}
				if (chordNote.slideTo != fret) {
					return true;
				}
			}
		} else {
			if (previousSound.note().string != string) {
				return true;
			}
			if (previousSound.note().slideTo == null) {
				if (previousSound.note().fret != fret) {
					return true;
				}
			} else {
				if (previousSound.note().unpitchedSlide) {
					return true;
				}
				if (previousSound.note().slideTo != fret) {
					return true;
				}
			}
		}

		return false;
	}

	private void addGuitarNoteAddLineHighlight(final List<ChordTemplate> chordTemplates, final int time,
			final HighwayDrawer highwayDrawer, final HighlightData highlightData) {
		if (highlightData.type != PositionType.GUITAR_NOTE) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = timeToX(highlightPosition.position, time);
			final Optional<ChordTemplate> template = highlightPosition.originalSound//
					.filter(ChordOrNote::isChord)//
					.map(s -> chordTemplates.get(s.chord().templateId()));
			highwayDrawer.addSoundHighlight(x, highlightPosition.originalSound, template, highlightPosition.string,
					highlightPosition.drawOriginalStrings);
		}

		if (highlightData.line.isPresent()) {
			highwayDrawer.addNoteAdditionLine(highlightData.line.get());
		}
	}

	private void addGuitarNotes(final FrameData frameData, final int panelWidth, final HighwayDrawer highwayDrawer) {
		final List<ChordOrNote> sounds = frameData.level.sounds;
		final List<ChordTemplate> chordTemplates = frameData.arrangement.chordTemplates;
		final Set<Integer> selectedNoteIds = frameData.selection.getSelectedIds(PositionType.GUITAR_NOTE);
		final int highlightId = frameData.highlightData.getId(PositionType.GUITAR_NOTE);

		boolean lastWasLinkNext = false;
		for (int i = 0; i < sounds.size(); i++) {
			final ChordOrNote sound = sounds.get(i);
			final boolean wrongLinkNext;
			if (sound.isNote() && lastWasLinkNext) {
				wrongLinkNext = isLinkNextIncorrect(sounds, chordTemplates, i, sound.note().string, sound.note().fret);
			} else {
				wrongLinkNext = false;
			}

			final boolean selected = selectedNoteIds.contains(i);
			final int highlightedString = i != highlightId ? -1//
					: frameData.highlightData.id.map(id -> id.string.orElse(-1)).orElse(-1);
			addChordOrNote(frameData, highwayDrawer, panelWidth, sound, selected, highlightedString, lastWasLinkNext,
					wrongLinkNext);

			lastWasLinkNext = sound.chord() != null ? sound.chord().linkNext() : sound.note().linkNext;

			if (i == highlightId && !frameData.highlightData.hasStringOf(sound)) {
				final int x = timeToX(sound.position(), frameData.time);
				final Optional<ChordTemplate> template = sound.isChord()//
						? Optional.of(chordTemplates.get(sound.chord().templateId()))//
						: Optional.empty();
				highwayDrawer.addSoundHighlight(x, Optional.of(sound), template,
						frameData.highlightData.id.get().string.orElse(0), false);
			}
		}

		addGuitarNoteAddLineHighlight(chordTemplates, frameData.time, highwayDrawer, frameData.highlightData);
	}

	private void addHandShapes(final FrameData frameData, final int panelWidth, final HighwayDrawer highwayDrawer) {
		final Set<Integer> selectedHandShapeIds = frameData.selection.getSelectedIds(PositionType.HAND_SHAPE);
		final int highlightId = frameData.highlightData.getId(PositionType.HAND_SHAPE);

		for (int i = 0; i < frameData.level.handShapes.size(); i++) {
			final HandShape handShape = frameData.level.handShapes.get(i);
			final int x = timeToX(handShape.position(), frameData.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final int length = timeToXLength(handShape.position(), handShape.length());
			if (!isOnScreen(x, length)) {
				continue;
			}

			final ChordTemplate chordTemplate;
			if (handShape.templateId >= 0 && frameData.arrangement.chordTemplates.size() > handShape.templateId) {
				chordTemplate = frameData.arrangement.chordTemplates.get(handShape.templateId);
			} else {
				chordTemplate = new ChordTemplate();
			}

			final boolean selected = selectedHandShapeIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addHandShape(x, length, selected, highlighted, handShape, chordTemplate);
		}

		if (frameData.highlightData.type == PositionType.HAND_SHAPE) {
			frameData.highlightData.highlightedNonIdPositions.forEach(highlightPosition -> highwayDrawer
					.addHandShapeHighlight(timeToX(highlightPosition.position, frameData.time),
							timeToXLength(highlightPosition.position, highlightPosition.length)));
		}
	}

	public void drawGuitar(final FrameData frameData) {
		final int strings = frameData.arrangement.tuning.strings();
		final int panelWidth = chartPanel.getWidth();
		final HighwayDrawer highwayDrawer = getHighwayDrawer(frameData.g, strings, frameData.time);

		GuitarEventPointsDrawer.addEventPoints(frameData, panelWidth, highwayDrawer);
		GuitarToneChangeDrawer.addToneChanges(frameData, panelWidth, highwayDrawer);
		GuitarAnchorsDrawer.addAnchors(frameData, panelWidth, highwayDrawer);
		drawGuitarLanes(frameData, panelWidth);
		addGuitarNotes(frameData, panelWidth, highwayDrawer);
		addHandShapes(frameData, panelWidth, highwayDrawer);

		highwayDrawer.draw(frameData.g);
	}

	public void drawStringNames(final FrameData frameData) {
		final int strings = frameData.arrangement.tuning.strings();
		final String[] stringNames = frameData.arrangement.getSimpleStringNames();

		final int fontSize = (int) (noteHeight * 0.5);
		final Font stringNameFont = new Font(Font.DIALOG, Font.BOLD, fontSize);
		final int x = fontSize * 5 / 6;

		final int width = fontSize * 5 / 3;

		frameData.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		frameData.g.setColor(ColorLabel.LANE.color());
		frameData.g.fillRect(0, lanesTop, width + 3, lanesHeight + 1);

		for (int string = 0; string < stringNames.length; string++) {
			final int stringPosition = getStringPosition(string, stringNames.length);
			final int y = getLaneY(stringPosition);
			new CenteredText(new Position2D(x, y), stringNameFont, stringNames[string],
					getStringBasedColor(StringColorLabelType.LANE, string, strings)).draw(frameData.g);
		}
	}

	public void draw(final FrameData frameData) {
		try {
			waveFormDrawer.draw(frameData);
			beatsDrawer.draw(frameData);
			lyricLinesDrawer.draw(frameData);

			drawGuitar(frameData);
			drawStringNames(frameData);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
