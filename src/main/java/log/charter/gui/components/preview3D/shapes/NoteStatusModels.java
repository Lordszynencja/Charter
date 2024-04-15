package log.charter.gui.components.preview3D.shapes;

import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.texturePack;
import static log.charter.util.FileUtils.texturesFolder;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.gui.components.preview3D.data.NoteDrawData;
import log.charter.gui.components.preview3D.glUtils.TextureFileSupplier;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.io.Logger;

public class NoteStatusModels {
	public enum TextureAtlasPosition {
		NOTE_HEAD(0, 0, "note_head"), //
		NOTE_ANTICIPATION(1, 0, "note_anticipation"), //
		TECH_NOTE_HEAD(2, 0, "tech_note_head"), //
		ARPEGGIO_FRET_BRACKET(3, 0, "arpeggio_fret_bracket"), //

		HAMMER_ON(0, 1, "hammer_on"), //
		PULL_OFF(1, 1, "pull_off"), //
		TAP(2, 1, "tap"), //
		ARPEGGIO_OPEN_BRACKET(3, 1, "arpeggio_open_bracket"), //

		PALM_MUTE(0, 2, "palm_mute"), //
		FULL_MUTE(1, 2, "full_mute"), //
		ACCENT(2, 2, "accent"), //
		EMPTY_A(3, 2, "empty_A"), //

		HARMONIC(0, 3, "harmonic"), //
		PINCH_HARMONIC(1, 3, "pinch_harmonic"), //
		EMPTY_B(2, 3, "empty_B"), //
		EMPTY_C(3, 3, "empty_C"),//
		;

		public final int x;
		public final int y;
		public final String textureName;

		TextureAtlasPosition(final int x, final int y, final String textureName) {
			this.x = x;
			this.y = y;
			this.textureName = textureName;
		}

	}

	private static class NoteStatusData {
		public final boolean palmMute;
		public final Harmonic harmonic;
		public final boolean tap;
		public final boolean accent;
		public final boolean isLeftHandTechniquePresent;

		public NoteStatusData(final NoteDrawData note) {
			palmMute = note.mute == Mute.PALM;
			harmonic = note.harmonic;
			tap = note.hopo == HOPO.TAP;
			accent = note.accent;
			isLeftHandTechniquePresent = note.mute == Mute.FULL || note.harmonic == Harmonic.NORMAL
					|| note.hopo == HOPO.HAMMER_ON || note.hopo == HOPO.PULL_OFF;
		}

		public String name() {
			return "note_" + palmMute + "_" + harmonic + "_" + tap + "_" + accent + "_" + isLeftHandTechniquePresent;
		}

		@Override
		public int hashCode() {
			return Objects.hash(palmMute, harmonic, tap, accent, isLeftHandTechniquePresent);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final NoteStatusData other = (NoteStatusData) obj;
			return palmMute == other.palmMute && harmonic == other.harmonic && tap == other.tap
					&& accent == other.accent && isLeftHandTechniquePresent == other.isLeftHandTechniquePresent;
		}
	}

	private TexturesHolder texturesHolder;

	private int size = 1;
	/**
	 * atlas description:<br>
	 * (0, 0) - standard note head<br>
	 * (1, 0) - note anticipation<br>
	 * (2, 0) - tech note head<br>
	 * (3, 0) - arpeggio fretted note bracket<br>
	 * <br>
	 * (0, 1) - hammer on<br>
	 * (1, 1) - pull off<br>
	 * (2, 1) - tap<br>
	 * (3, 1) - arpeggio open note bracket<br>
	 * <br>
	 * (0, 2) - palm mute<br>
	 * (1, 2) - full mute<br>
	 * (2, 2) - accent<br>
	 * (3, 2) -<br>
	 * <br>
	 * (0, 3) - harmonic<br>
	 * (1, 3) - pinch harmonic<br>
	 * (2, 3) -<br>
	 * (3, 3) -
	 */
	private final BufferedImage[][] noteStatusesTextureAtlas = new BufferedImage[4][4];

	private final Map<NoteStatusData, Integer> noteStatusesTextureIds = new HashMap<>();
	private final Map<TextureAtlasPosition, Integer> textureIds = new HashMap<>();

	private static final TextureFileSupplier textureAtlasSupplier = new TextureFileSupplier(texturesFolder,
			() -> texturePack, name -> name + "/notes.png");

	private static BufferedImage loadTextureAtlas() {
		final File f = textureAtlasSupplier.getFile();

		try {
			return ImageIO.read(f);
		} catch (final IOException e) {
			Logger.error("Couldn't read notes texture atlas! path: " + f.getPath(), e);
			return new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
		}
	}

	public void reload() {
		final BufferedImage noteStatusesTexture = loadTextureAtlas();

		size = noteStatusesTexture.getWidth() / 4;
		for (int i = 0; i < 4; i++) {
			final int x = size * i;
			for (int j = 0; j < 4; j++) {
				final int y = size * j;
				noteStatusesTextureAtlas[i][j] = noteStatusesTexture.getSubimage(x, y, size, size);
			}
		}

		noteStatusesTextureIds.clear();
		textureIds.clear();
	}

	public void init(final TexturesHolder texturesHolder) {
		this.texturesHolder = texturesHolder;
		reload();
	}

	private int joinPixel(final int rgb0, final int rgb1) {
		final int r0 = (rgb0 >> 16) & 0xFF;
		final int g0 = (rgb0 >> 8) & 0xFF;
		final int b0 = rgb0 & 0xFF;
		final int r1 = (rgb1 >> 16) & 0xFF;
		final int g1 = (rgb1 >> 8) & 0xFF;
		final int b1 = rgb1 & 0xFF;

		if (rgb1 != -16777216) {
		}

		final int multA = (255 - b1);
		final int b = min(255, b0 * multA / 255 + b1);

		final int multB = multA * b0;
		final int r = min(255, r0 * multB / 255 / 255 + r1 * b1 / 255);
		final int g = min(255, g0 * multB / 255 / 255 + g1 * b1 / 255);

		return ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
	}

	private void addImage(final BufferedImage img0, final BufferedImage img1) {
		if (img1 == null) {
			return;
		}

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				img0.setRGB(x, y, joinPixel(img0.getRGB(x, y), img1.getRGB(x, y)));
			}
		}
	}

	private BufferedImage copyImage(final BufferedImage source) {
		final BufferedImage copy = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		final Graphics g = copy.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();

		return copy;
	}

	private BufferedImage getImage(final TextureAtlasPosition atlasPosition) {
		return noteStatusesTextureAtlas[atlasPosition.x][atlasPosition.y];
	}

	private BufferedImage getHarmonicImage(final Harmonic harmonic) {
		switch (harmonic) {
			case NORMAL:
				return getImage(TextureAtlasPosition.HARMONIC);
			case PINCH:
				return getImage(TextureAtlasPosition.PINCH_HARMONIC);
			case NONE:
			default:
				return null;
		}
	}

	private BufferedImage getBaseNoteImage(final NoteStatusData noteStatusData) {
		final TextureAtlasPosition baseNotePosition = noteStatusData.isLeftHandTechniquePresent//
				? TextureAtlasPosition.TECH_NOTE_HEAD//
				: TextureAtlasPosition.NOTE_HEAD;

		return copyImage(getImage(baseNotePosition));
	}

	private void addTechImages(final BufferedImage img, final NoteStatusData noteStatusData) {
		addImage(img, getHarmonicImage(noteStatusData.harmonic));
		if (noteStatusData.palmMute) {
			addImage(img, getImage(TextureAtlasPosition.PALM_MUTE));
		}
		if (noteStatusData.tap) {
			addImage(img, getImage(TextureAtlasPosition.TAP));
		}
		if (noteStatusData.accent) {
			addImage(img, getImage(TextureAtlasPosition.ACCENT));
		}
	}

	public int getFrettedNoteTextureId(final NoteDrawData note) {
		final NoteStatusData noteStatusData = new NoteStatusData(note);
		if (!noteStatusesTextureIds.containsKey(noteStatusData)) {
			final BufferedImage img = getBaseNoteImage(noteStatusData);
			addTechImages(img, noteStatusData);

			noteStatusesTextureIds.put(noteStatusData, texturesHolder.addTexture(noteStatusData.name(), img, true));
		}

		return noteStatusesTextureIds.get(noteStatusData);
	}

	public int getTextureId(final TextureAtlasPosition atlasPosition) {
		if (!textureIds.containsKey(atlasPosition)) {
			final int textureId = texturesHolder.addTexture(atlasPosition.textureName,
					noteStatusesTextureAtlas[atlasPosition.x][atlasPosition.y], true);
			textureIds.put(atlasPosition, textureId);
		}

		return textureIds.get(atlasPosition);
	}
}
