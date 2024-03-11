package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.GraphicalConfig.anchorInfoHeight;
import static log.charter.data.config.GraphicalConfig.chordHeight;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredImage;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.AccentIconGenerator.generateAccentIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.AccentIconGenerator.generateHarmonicAccentIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.HOPOIconGenerator.generateHammerOnIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.HOPOIconGenerator.generatePopIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.HOPOIconGenerator.generatePullOffIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.HOPOIconGenerator.generateSlapIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.HOPOIconGenerator.generateTapIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.MuteIconGenerator.generateFullMuteIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.MuteIconGenerator.generatePalmMuteIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.NoteHeadIconGenerator.generateHarmonicNoteIcon;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.NoteHeadIconGenerator.generateNoteIcon;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.Utils.getStringPosition;
import static log.charter.util.Utils.stringId;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Optional;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNoteWithFret;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredImage;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackgroundAndBorder;
import log.charter.gui.chartPanelDrawers.drawableShapes.Line;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeNotes;
import log.charter.util.data.Position2D;

public class ModernThemeNotes implements ThemeNotes {
	private static BufferedImage[] noteIcons = new BufferedImage[maxStrings];
	private static BufferedImage[] linkedNoteIcons = new BufferedImage[maxStrings];
	private static BufferedImage noteSelectIcon = null;
	private static BufferedImage noteHighlightIcon = null;
	private static BufferedImage[] harmonicNoteIcons = new BufferedImage[maxStrings];
	private static BufferedImage harmonicNoteSelectIcon = null;
	private static BufferedImage harmonicNoteHighlightIcon = null;
	private static BufferedImage[] accentIcons = new BufferedImage[maxStrings];
	private static BufferedImage[] harmonicAccentIcons = new BufferedImage[maxStrings];

	private static BufferedImage hammerOnIcon = null;
	private static BufferedImage pullOffIcon = null;
	private static BufferedImage tapIcon = null;
	private static BufferedImage slapIcon = null;
	private static BufferedImage popIcon = null;

	private static BufferedImage palmMuteIcon = null;
	private static BufferedImage fullMuteIcon = null;

	private static Font chordNameFont = new Font(Font.SANS_SERIF, Font.PLAIN, chordHeight);
	private static Font fretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);
	private static Font smallFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);

	public static void reloadGraphics() {
		for (int string = 0; string < maxStrings; string++) {
			final int stringId = stringId(string, maxStrings);
			final Color borderInnerColor = getStringBasedColor(StringColorLabelType.LANE, string, maxStrings)
					.brighter();
			final Color innerColor = borderInnerColor.darker().darker();
			final Color borderOuterColor = ColorLabel.NOTE_BACKGROUND.color();
			noteIcons[stringId] = generateNoteIcon(innerColor, borderInnerColor, borderOuterColor);
			linkedNoteIcons[stringId] = generateNoteIcon(innerColor.darker().darker(), borderInnerColor,
					borderOuterColor);
			harmonicNoteIcons[stringId] = generateHarmonicNoteIcon(innerColor, borderInnerColor, borderOuterColor);

			final Color accentColor = borderInnerColor.brighter().brighter();
			accentIcons[stringId] = generateAccentIcon(accentColor);
			harmonicAccentIcons[stringId] = generateHarmonicAccentIcon(accentColor);
		}

		noteSelectIcon = generateNoteIcon(null, null, ColorLabel.SELECT.color());
		noteHighlightIcon = generateNoteIcon(null, null, ColorLabel.HIGHLIGHT.color());
		harmonicNoteSelectIcon = generateHarmonicNoteIcon(null, null, ColorLabel.SELECT.color());
		harmonicNoteHighlightIcon = generateHarmonicNoteIcon(null, null, ColorLabel.HIGHLIGHT.color());

		hammerOnIcon = generateHammerOnIcon();
		pullOffIcon = generatePullOffIcon();
		tapIcon = generateTapIcon();
		slapIcon = generateSlapIcon();
		popIcon = generatePopIcon();

		palmMuteIcon = generatePalmMuteIcon();
		fullMuteIcon = generateFullMuteIcon();

		chordNameFont = new Font(Font.SANS_SERIF, Font.BOLD, chordHeight);
		fretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);
		smallFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);

		ModernThemeBends.reloadGraphics();
		ModernThemeNoteTails.reloadGraphics();
	}

	private final HighwayDrawData data;

	private final int[] stringPositions;

	private final ModernThemeBends bends;
	private final ModernThemeNoteTails noteTails;

	public ModernThemeNotes(final HighwayDrawData data) {
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
		data.notes.add(new CenteredText(new Position2D(note.x, y), font, "\uD83D\uDD17", Color.WHITE));
	}

	private void addAccent(final EditorNoteDrawingData note, final int y) {
		if (!note.accent) {
			return;
		}

		final int stringId = stringId(note.string, data.strings);
		final BufferedImage accentIcon = (note.harmonic == Harmonic.NONE ? accentIcons : harmonicAccentIcons)[stringId];
		data.notes.add(new CenteredImage(new Position2D(note.x, y), accentIcon));
	}

	private void addNoteHeadShape(final EditorNoteDrawingData note, final int y) {
		final int stringId = stringId(note.string, data.strings);
		final BufferedImage icon = switch (note.harmonic) {
			case NORMAL -> harmonicNoteIcons[stringId];
			case PINCH -> harmonicNoteIcons[stringId];
			default -> noteIcons[stringId];
		};

		data.notes.add(new CenteredImage(new Position2D(note.x, y), icon));

		if (note.highlighted) {
			addNoteHighlight(note.harmonic, note.x, y);
		} else if (note.selected) {
			addNoteSelection(note.harmonic, note.x, y);
		}

		if (note.harmonic == Harmonic.PINCH) {
			final int x0 = note.x - noteHeight / 2;
			final int y0 = y - noteHeight / 2;
			final int y1 = y + noteHeight / 2;
			final Color color = getStringBasedColor(StringColorLabelType.LANE, note.string, data.strings).brighter();
			data.notes.add(new Line(new Position2D(x0, y0), new Position2D(x0, y1), color, 3));
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
		noteTails.addNoteTail(note, y);
		bends.addBendValues(note, y);

		if (note.linkPrevious && !note.wrongLink) {
			addLinkedNoteHeadShape(note, y);
			addLinkedFretNumber(note, y);
			return;
		}

		addAccent(note, y);
		addNoteHeadShape(note, y);
		addMuteIcon(note, y);
		addFretNumber(note, y);
		addHopoOrBassTechIcon(note, y);
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
		data.chordNames.add(new Text(new Position2D(x + 2, anchorY + anchorInfoHeight), chordNameFont, chordName,
				ColorLabel.BASE_DARK_TEXT));
	}

	private void drawHighlightForNote(final int x, final CommonNoteWithFret note) {
		final int length = timeToXLength(note.position(), note.length());
		final int y = stringPositions[note.string()];
		final boolean slide = note.slideTo() != null;
		final boolean slideUp = slide && note.slideTo() >= note.fret();

		noteTails.addTailShapeBox(x, length, y, ColorLabel.HIGHLIGHT, slide, slideUp);
		addNoteHighlight(note.harmonic(), x, y);
	}

	private void drawHighlightWithoutNote(final int x, final int string) {
		addNoteHighlight(Harmonic.NONE, x, stringPositions[string]);
	}

	@Override
	public void addSoundHighlight(final int x, final Optional<ChordOrNote> originalSound,
			final Optional<ChordTemplate> template, final int string) {
		originalSound.flatMap(sound -> sound.noteWithFrets(string, template.orElse(null)))//
				.ifPresentOrElse(note -> drawHighlightForNote(x, note), //
						() -> drawHighlightWithoutNote(x, string));
	}

	@Override
	public void addNoteAdditionLine(final Position2D from, final Position2D to) {
		data.notes.add(new Line(from, to, ColorLabel.NOTE_ADD_LINE));
	}
}
