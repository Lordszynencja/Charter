package log.charter.gui.components.preview3D.shapes;

import static java.lang.Math.min;
import static log.charter.data.config.Config.texturePack;
import static log.charter.gui.components.preview3D.glUtils.TexturesHolder.texturePacksPath;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import log.charter.gui.components.preview3D.data.NoteDrawData;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.io.Logger;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;

public class NoteStatusModels {
	private static class NoteStatusData {
		public final HOPO hopo;
		public final Mute mute;
		public final Harmonic harmonic;

		public NoteStatusData(final NoteDrawData note) {
			hopo = note.hopo;
			mute = note.mute;
			harmonic = note.harmonic;
		}

		public String name() {
			return "note_" + hopo + "_" + mute + "_" + harmonic;
		}

		public String onlyStatusesName() {
			return "status_" + hopo + "_" + mute + "_" + harmonic;
		}

		@Override
		public int hashCode() {
			return Objects.hash(harmonic, hopo, mute);
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
			return harmonic == other.harmonic && hopo == other.hopo && mute == other.mute;
		}
	}

	private TexturesHolder texturesHolder;

	private int size = 1;
	private final BufferedImage[][] noteStatusesTextureAtlas = new BufferedImage[4][4];
	private final Map<NoteStatusData, Integer> noteStatusesTextures = new HashMap<>();
	private final Map<NoteStatusData, Integer> noteOnlyStatusesTextures = new HashMap<>();
	private Integer noteAnticipationTextureId = null;

	public void reload() {
		String path = texturePacksPath + texturePack + "/notes.png";
		File f = new File(path);
		if (!f.exists()) {
			path = texturePacksPath + "default/notes.png";
			f = new File(path);
		}

		BufferedImage noteStatusesTexture;
		try {
			noteStatusesTexture = ImageIO.read(f);
		} catch (final IOException e) {
			Logger.error("Couldn't read notes texture atlas! path: " + path, e);
			noteStatusesTexture = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
		}

		size = noteStatusesTexture.getWidth() / 4;
		for (int i = 0; i < 4; i++) {
			final int x = size * i;
			for (int j = 0; j < 4; j++) {
				final int y = size * j;
				noteStatusesTextureAtlas[i][j] = noteStatusesTexture.getSubimage(x, y, size, size);
			}
		}

		noteStatusesTextures.clear();
		noteAnticipationTextureId = null;
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

		try {
			final File f = new File("C:/users/szymon/desktop/test0.png");
			if (!f.exists()) {
				ImageIO.write(img1, "png", f);
			}
		} catch (final IOException e) {
			e.printStackTrace();
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

	private BufferedImage getImage(final HOPO hopo) {
		switch (hopo) {
		case HAMMER_ON:
			return noteStatusesTextureAtlas[0][1];
		case PULL_OFF:
			return noteStatusesTextureAtlas[1][1];
		case TAP:
			return noteStatusesTextureAtlas[2][1];
		case NONE:
		default:
			return null;
		}
	}

	private BufferedImage getImage(final Mute mute) {
		switch (mute) {
		case PALM:
			return noteStatusesTextureAtlas[0][2];
		case FULL:
			return noteStatusesTextureAtlas[1][2];
		case NONE:
		default:
			return null;
		}
	}

	private BufferedImage getImage(final Harmonic harmonic) {
		switch (harmonic) {
		case NORMAL:
			return noteStatusesTextureAtlas[0][3];
		case PINCH:
			return noteStatusesTextureAtlas[1][3];
		case NONE:
		default:
			return null;
		}
	}

	public int getTextureId(final NoteDrawData note) {
		final NoteStatusData noteStatusData = new NoteStatusData(note);
		if (noteStatusesTextures.containsKey(noteStatusData)) {
			return noteStatusesTextures.get(noteStatusData);
		}

		BufferedImage img;
		if (noteStatusData.hopo == HOPO.NONE && noteStatusData.mute == Mute.NONE
				&& noteStatusData.harmonic == Harmonic.NONE) {
			img = noteStatusesTextureAtlas[0][0];
		} else {
			img = copyImage(noteStatusesTextureAtlas[2][0]);

			addImage(img, getImage(noteStatusData.hopo));
			addImage(img, getImage(noteStatusData.mute));
			addImage(img, getImage(noteStatusData.harmonic));
		}

		final int textureId = texturesHolder.addTexture(noteStatusData.name(), img, true);
		noteStatusesTextures.put(noteStatusData, textureId);
		return textureId;
	}

	public int getTextureIdForOnlyStatuses(final NoteDrawData note) {
		if (note.hopo == HOPO.NONE && note.mute == Mute.NONE && note.harmonic == Harmonic.NONE) {
			return texturesHolder.getTextureId("error");
		}

		final NoteStatusData noteStatusData = new NoteStatusData(note);
		if (noteOnlyStatusesTextures.containsKey(noteStatusData)) {
			return noteOnlyStatusesTextures.get(noteStatusData);
		}

		final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		addImage(img, getImage(noteStatusData.hopo));
		addImage(img, getImage(noteStatusData.mute));
		addImage(img, getImage(noteStatusData.harmonic));

		final int textureId = texturesHolder.addTexture(noteStatusData.onlyStatusesName(), img, true);
		noteOnlyStatusesTextures.put(noteStatusData, textureId);
		return textureId;
	}

	public int getNoteAnticipationTextureId() {
		if (noteAnticipationTextureId != null) {
			return noteAnticipationTextureId;
		}

		return texturesHolder.addTexture("note_anticipation", noteStatusesTextureAtlas[1][0], true);
	}
}
