package log.charter.gui.components.preview3D.shapes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL30;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.Point3D;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;

public class NoteStatusModels {
	private static class FullMuteModel implements Model {
		private static final List<Point3D> points = new ArrayList<>();
		static {
			final double width = FrettedNoteModel.height * 0.1;
			final double x = FrettedNoteModel.height * 0.05;
			final double y = FrettedNoteModel.height * 0.5;
			final double z = -FrettedNoteModel.depth / 2 - 0.01;

			points.add(new Point3D(-x - width / 10, y, z));
			points.add(new Point3D(-x, y + width, z));
			points.add(new Point3D(x + width / 10, -y, z));
			points.add(new Point3D(x, -y - width, z));

			points.add(new Point3D(x + width / 10, y, z));
			points.add(new Point3D(x, y + width, z));
			points.add(new Point3D(-x - width / 10, -y, z));
			points.add(new Point3D(-x, -y - width, z));
		}

		@Override
		public List<Point3D> getPoints() {
			return points;
		}

		@Override
		public int getDrawMode() {
			return GL30.GL_QUADS;
		}
	}

	private static class PalmMuteModel implements Model {
		private static final List<Point3D> points = new ArrayList<>();
		static {
			final double width = FrettedNoteModel.height * 0.15;
			final double x = FrettedNoteModel.width * 0.5;
			final double y = FrettedNoteModel.height * 0.5;
			final double z = -FrettedNoteModel.depth / 2 - 0.01;

			points.add(new Point3D(-x - width / 10, y, z));
			points.add(new Point3D(-x, y + width, z));
			points.add(new Point3D(x + width / 10, -y, z));
			points.add(new Point3D(x, -y - width, z));

			points.add(new Point3D(x + width / 10, y, z));
			points.add(new Point3D(x, y + width, z));
			points.add(new Point3D(-x - width / 10, -y, z));
			points.add(new Point3D(-x, -y - width, z));
		}

		@Override
		public List<Point3D> getPoints() {
			return points;
		}

		@Override
		public int getDrawMode() {
			return GL30.GL_QUADS;
		}
	}

	private static class HOModel implements Model {
		private static final List<Point3D> points = new ArrayList<>();
		static {
			final double x = FrettedNoteModel.width * 0.2;
			final double y = FrettedNoteModel.height * 0.5;
			final double z = -FrettedNoteModel.depth / 2 - 0.01;

			points.add(new Point3D(-x, -y, z));
			points.add(new Point3D(0, y, z));
			points.add(new Point3D(x, -y, z));
		}

		@Override
		public List<Point3D> getPoints() {
			return points;
		}

		@Override
		public int getDrawMode() {
			return GL30.GL_TRIANGLES;
		}
	}

	private static class POModel implements Model {
		private static final List<Point3D> points = new ArrayList<>();
		static {
			final double x = FrettedNoteModel.width * 0.2;
			final double y = FrettedNoteModel.height * 0.5;
			final double z = -FrettedNoteModel.depth / 2 - 0.01;

			points.add(new Point3D(-x, y, z));
			points.add(new Point3D(0, -y, z));
			points.add(new Point3D(x, y, z));
		}

		@Override
		public List<Point3D> getPoints() {
			return points;
		}

		@Override
		public int getDrawMode() {
			return GL30.GL_TRIANGLES;
		}
	}

	private static class TapModel implements Model {
		private static final List<Point3D> points = new ArrayList<>();
		static {

			final double x = FrettedNoteModel.width * 0.5;
			final double y = FrettedNoteModel.height * 0.5;
			final double z = -FrettedNoteModel.depth / 2 - 0.01;

			points.add(new Point3D(-x, y, z));
			points.add(new Point3D(-x, y - 0.1, z));
			points.add(new Point3D(0, -y + 0.1, z));
			points.add(new Point3D(0, -y, z));
			points.add(new Point3D(x, y, z));
			points.add(new Point3D(x, y - 0.1, z));
		}

		@Override
		public List<Point3D> getPoints() {
			return points;
		}

		@Override
		public int getDrawMode() {
			return GL30.GL_TRIANGLE_STRIP;
		}
	}

	private static class HarmonicModel implements Model {
		private static final List<Point3D> points = new ArrayList<>();
		static {
			final double d0 = FrettedNoteModel.height * 0.45;
			final double d1 = FrettedNoteModel.height * 0.6;
			final double z = -FrettedNoteModel.depth / 2 - 0.01;

			points.add(new Point3D(d0 / 10, 0, z));
			points.add(new Point3D(d1 / 10, 0, z));
			points.add(new Point3D(0, d0, z));
			points.add(new Point3D(0, d1, z));
			points.add(new Point3D(-d0 / 10, 0, z));
			points.add(new Point3D(-d1 / 10, 0, z));
			points.add(new Point3D(0, -d0, z));
			points.add(new Point3D(0, -d1, z));
			points.add(new Point3D(d0 / 10, 0, z));
			points.add(new Point3D(d1 / 10, 0, z));
		}

		@Override
		public List<Point3D> getPoints() {
			return points;
		}

		@Override
		public int getDrawMode() {
			return GL30.GL_TRIANGLE_STRIP;
		}
	}

	private static class PinchHarmonicModel implements Model {
		private static final List<Point3D> points = new ArrayList<>();
		static {
			final double x0 = FrettedNoteModel.width * 0.3;
			final double x1 = FrettedNoteModel.width * 0.6;
			final double y0 = FrettedNoteModel.height * 0.3;
			final double y1 = FrettedNoteModel.height * 0.6;
			final double z = -FrettedNoteModel.depth / 2 - 0.01;

			points.add(new Point3D(x0, 0, z));
			points.add(new Point3D(x1, 0, z));
			points.add(new Point3D(0, y0, z));
			points.add(new Point3D(0, y1, z));
			points.add(new Point3D(-x0, 0, z));
			points.add(new Point3D(-x1, 0, z));
			points.add(new Point3D(0, -y0, z));
			points.add(new Point3D(0, -y1, z));
			points.add(new Point3D(x0, 0, z));
			points.add(new Point3D(x1, 0, z));
		}

		@Override
		public List<Point3D> getPoints() {
			return points;
		}

		@Override
		public int getDrawMode() {
			return GL30.GL_TRIANGLE_STRIP;
		}
	}

	public static final Model fullMuteModel = new FullMuteModel();
	public static final Model palmMuteModel = new PalmMuteModel();

	public static final Map<Mute, Model> mutesModels = new HashMap<>();
	public static final Map<Mute, ColorLabel> mutesColors = new HashMap<>();
	static {
		mutesModels.put(Mute.FULL, fullMuteModel);
		mutesColors.put(Mute.FULL, ColorLabel.FULL_MUTE);

		mutesModels.put(Mute.PALM, palmMuteModel);
		mutesColors.put(Mute.PALM, ColorLabel.PALM_MUTE);
	}

	public static final Model hoModel = new HOModel();
	public static final Model poModel = new POModel();
	public static final Model tapModel = new TapModel();

	public static final Map<HOPO, Model> hoposModels = new HashMap<>();
	public static final Map<HOPO, ColorLabel> hoposColors = new HashMap<>();
	static {
		hoposModels.put(HOPO.HAMMER_ON, hoModel);
		hoposColors.put(HOPO.HAMMER_ON, ColorLabel.HAMMER_ON);

		hoposModels.put(HOPO.PULL_OFF, poModel);
		hoposColors.put(HOPO.PULL_OFF, ColorLabel.PULL_OFF);

		hoposModels.put(HOPO.TAP, tapModel);
		hoposColors.put(HOPO.TAP, ColorLabel.TAP);
	}

	public static final Model harmonicModel = new HarmonicModel();
	public static final Model pinchHarmonicModel = new PinchHarmonicModel();

	public static final Map<Harmonic, Model> harmonicsModels = new HashMap<>();
	public static final Map<Harmonic, ColorLabel> harmonicsColors = new HashMap<>();
	static {
		harmonicsModels.put(Harmonic.NORMAL, harmonicModel);
		harmonicsColors.put(Harmonic.NORMAL, ColorLabel.HARMONIC);

		harmonicsModels.put(Harmonic.PINCH, pinchHarmonicModel);
		harmonicsColors.put(Harmonic.PINCH, ColorLabel.PINCH_HARMONIC);
	}
}
