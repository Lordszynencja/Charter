package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static java.lang.Math.max;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredImage;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.ColorUtils.setAlpha;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.Utils.getStringPosition;
import static log.charter.util.Utils.stringId;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredImage;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackgroundAndBorder;
import log.charter.gui.chartPanelDrawers.drawableShapes.Line;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawerData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeNotes;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.Position2D;

public class ModernThemeNotes implements ThemeNotes {
	private static BufferedImage noteIcons[] = new BufferedImage[maxStrings];
	private static BufferedImage linkedNoteIcons[] = new BufferedImage[maxStrings];
	private static BufferedImage noteSelectIcon = null;
	private static BufferedImage noteHighlightIcon = null;
	private static BufferedImage harmonicNoteIcons[] = new BufferedImage[maxStrings];
	private static BufferedImage harmonicNoteSelectIcon = null;
	private static BufferedImage harmonicNoteHighlightIcon = null;

	private static BufferedImage hammerOnIcon = null;
	private static BufferedImage pullOffIcon = null;
	private static BufferedImage tapIcon = null;
	private static BufferedImage slapIcon = null;
	private static BufferedImage popIcon = null;

	private static BufferedImage palmMuteIcon = null;
	private static BufferedImage fullMuteIcon = null;

	private static Font chordNameFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);
	private static Font fretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight * 2 / 3);
	private static Font smallFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);

	private static BufferedImage generateNoteIcon(final Color innerColor, final Color borderColor) {
		final int size = noteHeight;

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D graphics = (Graphics2D) icon.getGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int borderSize = max(2, size / 15);
		final Ellipse2D inner = new Ellipse2D.Double(borderSize, borderSize, size - 2 * borderSize,
				size - 2 * borderSize);
		if (innerColor != null) {
			graphics.setColor(innerColor);
			graphics.fill(inner);
		}

		if (borderColor != null) {
			final Ellipse2D outer = new Ellipse2D.Double(0, 0, size, size);
			final Area area = new Area(outer);
			area.subtract(new Area(inner));
			graphics.setColor(borderColor);
			graphics.fill(area);
		}

		return icon;
	}

	private static BufferedImage generateHarmonicNoteIcon(final Color innerColor, final Color borderColor) {
		final int size = noteHeight;

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (innerColor != null) {
			g.setColor(innerColor);
			g.fill(new Polygon(new int[] { 1, size / 2, size - 1, size / 2 },
					new int[] { size / 2, size - 1, size / 2, 1 }, 4));
		}

		if (borderColor != null) {
			final Area area = new Area(new Polygon(new int[] { 0, size / 2, size, size / 2 },
					new int[] { size / 2, size, size / 2, 0 }, 4));
			area.subtract(new Area(new Polygon(new int[] { 2, size / 2, size - 2, size / 2 },
					new int[] { size / 2, size - 2, size / 2, 2 }, 4)));
			g.setColor(borderColor);
			g.fill(area);
		}

		return icon;
	}

	private static BufferedImage generateHammerOnIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { 1, h - 1, 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { 0, h, 0 }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { 1, h - 2, 1 }, 3)));
		g.setColor(Color.BLACK);
		g.fill(area);

		return icon;
	}

	private static BufferedImage generatePullOffIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { h - 1, 1, h - 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { h, 0, h }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { h - 1, 2, h - 1 }, 3)));
		g.setColor(Color.BLACK);
		g.fill(area);

		return icon;
	}

	private static BufferedImage generateTapIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.BLACK);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { 1, h - 1, 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { 0, h, 0 }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { 1, h - 2, 1 }, 3)));
		g.setColor(Color.LIGHT_GRAY);
		g.fill(area);
		return icon;
	}

	private static BufferedImage generateSingleLetterIcon(final String letter, final Color color) {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(color);
		g.setFont(new Font(Font.DIALOG, Font.BOLD, h + 1));
		g.drawString(letter, 0, h - 1);

		return icon;
	}

	private static Polygon generateX(final int size, final int space) {
		return new Polygon(new int[] { //
				0, size / 2 - space, 0, //
				space, size / 2, size - space - 1, //
				size - 1, size / 2 + space, size - 1, //
				size - space - 1, size / 2, space,//
		}, //
				new int[] { //
						space, size / 2, size - space - 1, //
						size - 1, size / 2 + space, size - 1, //
						size - space - 1, size / 2, space, //
						0, size / 2 - space, 0, //
				}, 12);
	}

	private static BufferedImage generatePalmMuteIcon() {
		final int size = max(16, noteHeight);
		final int space = max(2, size / 8);
		final int borderWidth = max(1, space / 3);
		final Color borderColor = Color.GRAY;
		final Color innerColor = Color.BLACK.brighter().brighter().brighter();

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Polygon inner = generateX(size - 2, space - 1);
		inner.translate(1, 1);
		g.setColor(innerColor);
		g.fill(inner);

		final Polygon outer = generateX(size, space);
		final Polygon outerSubtract = generateX(size - 4, space - borderWidth);
		outerSubtract.translate(2, 2);
		final Area borderArea = new Area(outer);
		borderArea.subtract(new Area(outerSubtract));
		g.setColor(borderColor);
		g.fill(borderArea);

		return icon;
	}

	private static BufferedImage generateFullMuteIcon() {
		final int size = max(16, noteHeight);
		final int space = max(2, size / 8);
		final int borderWidth = max(1, space / 3);
		final Color borderColor = Color.GRAY;
		final Color innerColor = Color.WHITE;

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Polygon inner = generateX(size - 2, space - 1);
		inner.translate(1, 1);
		g.setColor(innerColor);
		g.fill(inner);

		final Polygon outer = generateX(size, space);
		final Polygon outerSubtract = generateX(size - 4, space - borderWidth);
		outerSubtract.translate(2, 2);
		final Area borderArea = new Area(outer);
		borderArea.subtract(new Area(outerSubtract));
		g.setColor(borderColor);
		g.fill(borderArea);

		return icon;
	}

	public static void reloadGraphics() {
		for (int string = 0; string < maxStrings; string++) {

			final int stringId = stringId(string, maxStrings);
			final Color borderColor = getStringBasedColor(StringColorLabelType.LANE, string, maxStrings);
			final Color innerColor = borderColor.darker().darker();
			noteIcons[stringId] = generateNoteIcon(innerColor, borderColor);
			linkedNoteIcons[stringId] = generateNoteIcon(setAlpha(innerColor, 128), setAlpha(borderColor, 128));
			harmonicNoteIcons[stringId] = generateHarmonicNoteIcon(innerColor, borderColor);
		}

		noteSelectIcon = generateNoteIcon(null, ColorLabel.SELECT.color());
		noteHighlightIcon = generateNoteIcon(null, ColorLabel.HIGHLIGHT.color());
		harmonicNoteSelectIcon = generateHarmonicNoteIcon(null, ColorLabel.SELECT.color());
		harmonicNoteHighlightIcon = generateHarmonicNoteIcon(null, ColorLabel.HIGHLIGHT.color());

		hammerOnIcon = generateHammerOnIcon();
		pullOffIcon = generatePullOffIcon();
		tapIcon = generateTapIcon();
		slapIcon = generateSingleLetterIcon("S", Color.BLACK);
		popIcon = generateSingleLetterIcon("P", Color.BLACK);

		palmMuteIcon = generatePalmMuteIcon();
		fullMuteIcon = generateFullMuteIcon();

		chordNameFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);
		fretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight * 2 / 3);
		smallFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);

		ModernThemeBends.reloadGraphics();
		ModernThemeNoteTails.reloadGraphics();
	}

	private final HighwayDrawerData data;

	private final int[] stringPositions;

	private final ModernThemeBends bends;
	private final ModernThemeNoteTails noteTails;

	public ModernThemeNotes(final HighwayDrawerData data) {
		this.data = data;

		stringPositions = new int[data.strings];
		for (int string = 0; string < data.strings; string++) {
			stringPositions[string] = getLaneY(getStringPosition(string, data.strings));
		}

		bends = new ModernThemeBends(data);
		noteTails = new ModernThemeNoteTails(data);
	}

	private void addHopoOrBassTechIcon(final EditorNoteDrawingData note, final int noteY) {
		BufferedImage img = switch (note.bassPickingTech) {
			case POP -> popIcon;
			case SLAP -> img = slapIcon;
			default -> null;
		};
		if (img != null) {
			data.notes.add(centeredImage(new Position2D(note.x + noteHeight / 2, noteY - (int) (noteHeight / 3)), img));
			return;
		}

		img = switch (note.hopo) {
			case HAMMER_ON -> img = hammerOnIcon;
			case PULL_OFF -> img = pullOffIcon;
			case TAP -> img = tapIcon;
			default -> null;
		};
		if (img == null) {
			return;
		}

		final int iconX = note.x - noteHeight / 2;
		final int iconY = noteY - (note.hopo == HOPO.PULL_OFF ? noteHeight / 2 : noteHeight / 3);
		data.notes.add(centeredImage(new Position2D(iconX, iconY), img));
	}

	private void addNoteHighlight(final Harmonic harmonic, final int x, final int y) {
		final BufferedImage icon = switch (harmonic) {
			case NORMAL -> harmonicNoteHighlightIcon;
			case PINCH -> harmonicNoteHighlightIcon;
			default -> noteHighlightIcon;
		};

		data.notes.add(new CenteredImage(new Position2D(x, y), icon));
	}

	private void addNoteSelection(final Harmonic harmonic, final int x, final int y) {
		final BufferedImage icon = switch (harmonic) {
			case NORMAL -> harmonicNoteSelectIcon;
			case PINCH -> harmonicNoteSelectIcon;
			default -> noteSelectIcon;
		};

		data.notes.add(new CenteredImage(new Position2D(x, y), icon));
	}

	private void addLinkedNoteHeadShape(final EditorNoteDrawingData note, final int y) {
		final int stringId = stringId(note.string, data.strings);
		final BufferedImage icon = linkedNoteIcons[stringId];

		data.notes.add(new CenteredImage(new Position2D(note.x, y), icon));

		if (note.highlighted) {
			addNoteHighlight(note.harmonic, note.x, y);
		} else if (note.selected) {
			addNoteSelection(note.harmonic, note.x, y);
		}
	}

	private void addLinkedFretNumber(final EditorNoteDrawingData note, final int y) {
		final Font font = note.fretNumber < 10 ? fretFont : smallFretFont;
		data.notes.add(new CenteredText(new Position2D(note.x, y), font, note.fretNumber + "", Color.WHITE));
	}

	private void addNoteHeadShape(final EditorNoteDrawingData note, final int y) {
		final int stringId = stringId(note.string, data.strings);
		final BufferedImage icon = switch (note.harmonic) {
			case NORMAL -> harmonicNoteIcons[stringId];
			case PINCH -> harmonicNoteIcons[stringId];
			default -> noteIcons[stringId];
		};

		data.notes.add(new CenteredImage(new Position2D(note.x, y), icon));
		if (note.harmonic == Harmonic.PINCH) {
			final int x0 = note.x - noteHeight / 2;
			final int y0 = y - noteHeight / 2;
			final int y1 = y + noteHeight / 2;
			final Color color = getStringBasedColor(StringColorLabelType.LANE, note.string, data.strings);
			data.notes.add(new Line(new Position2D(x0, y0), new Position2D(x0, y1), color, 3));
		}

		if (note.highlighted) {
			addNoteHighlight(note.harmonic, note.x, y);
		} else if (note.selected) {
			addNoteSelection(note.harmonic, note.x, y);
		}
	}

	private void addMuteIcon(final EditorNoteDrawingData note, final int y) {
		final BufferedImage icon = switch (note.mute) {
			case FULL -> fullMuteIcon;
			case PALM -> palmMuteIcon;
			default -> null;
		};

		if (icon == null) {
			return;
		}

		data.notes.add(new CenteredImage(new Position2D(note.x, y), icon));
	}

	private void addFretNumber(final EditorNoteDrawingData note, final int y) {
		final Font font = note.fretNumber < 10 ? fretFont : smallFretFont;

		if (note.mute == Mute.FULL) {
			data.notes.add(new CenteredTextWithBackgroundAndBorder(new Position2D(note.x, y), font,
					note.fretNumber + "", Color.WHITE, Color.GRAY, Color.LIGHT_GRAY));
		} else {
			data.notes.add(new CenteredText(new Position2D(note.x, y), font, note.fretNumber + "", Color.WHITE));
		}
	}

	private void addNoteShape(final EditorNoteDrawingData note, final int y) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteHeight, noteHeight)//
				.centered();

		noteTails.addNoteTail(note, y);
		bends.addBendValues(note, y);

		if (note.linkPrevious && !note.wrongLink) {
			addLinkedNoteHeadShape(note, y);
			addLinkedFretNumber(note, y);
			return;
		}

		addNoteHeadShape(note, y);
		addMuteIcon(note, y);
		addFretNumber(note, y);
		addHopoOrBassTechIcon(note, y);

		if (note.accent) {
			final Color accentColor = getStringBasedColor(StringColorLabelType.NOTE_ACCENT, note.string, data.strings);
			data.notes.add(strokedRectangle(position.resized(-2, -2, 3, 3), accentColor, 1));
		}
	}

	@Override
	public void addNote(final EditorNoteDrawingData note) {
		if (note.string >= stringPositions.length) {
			return;
		}

		final int y = stringPositions[note.string];
		addNoteShape(note, y);
	}

	@Override
	public void addChordName(final int x, final String chordName) {
		data.chordNames.add(
				new Text(new Position2D(x + 2, lanesTop - 1), chordNameFont, chordName, ColorLabel.BASE_DARK_TEXT));
	}

	@Override
	public void addSoundHighlight(final int x, final ChordOrNote originalSound, final ChordTemplate template,
			final int string) {
		if (originalSound == null) {
			addNoteHighlight(Harmonic.NONE, x, stringPositions[string]);
			return;
		}

		if (originalSound.isNote()) {
			final Note note = originalSound.note;
			final int length = timeToXLength(note.position(), note.length());
			final int y = stringPositions[originalSound.note.string];
			final boolean slide = note.slideTo != null;
			final boolean slideUp = slide && note.slideTo >= note.fret;
			noteTails.addTailShapeBox(x, length, y, ColorLabel.HIGHLIGHT, slide, slideUp);
			addNoteHighlight(originalSound.note.harmonic, x, y);
			return;
		}

		final int chordPosition = originalSound.chord.position();
		originalSound.chord.chordNotes.forEach((chordString, chordNote) -> {
			final int length = timeToXLength(chordPosition, chordNote.length);
			final int y = stringPositions[chordString];
			final boolean slide = chordNote.slideTo != null;
			final boolean slideUp = slide && chordNote.slideTo >= template.frets.get(chordString);
			noteTails.addTailShapeBox(x, length, y, ColorLabel.HIGHLIGHT, slide, slideUp);

			addNoteHighlight(chordNote.harmonic, x, stringPositions[chordString]);
		});
	}

	@Override
	public void addNoteAdditionLine(final Position2D from, final Position2D to) {
		data.notes.add(new Line(from, to, ColorLabel.NOTE_ADD_LINE));
	}
}
