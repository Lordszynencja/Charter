package log.charter.gui.components.preview3D;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.camera.Preview3DCameraHandler;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.drawers.Preview3DAnchorsDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DBeatsDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DFingeringDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DGuitarSoundsDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DHandShapesDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DInlayDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DLaneBordersDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DLyricsDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DStringsFretsDrawer;
import log.charter.gui.components.preview3D.drawers.Preview3DVideoDrawer;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.glUtils.Texture;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels;
import log.charter.io.Logger;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.services.utils.Framer;
import log.charter.util.Timer;

public class Preview3DPanel extends AWTGLCanvas implements Initiable {
	private static final long serialVersionUID = 1L;

	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;

	private Framer cameraUpdater;
	private RepeatManager repeatManager;

	private final NoteStatusModels noteStatusModels = new NoteStatusModels();

	private final ShadersHolder shadersHolder = new ShadersHolder();
	private final TextTexturesHolder textTexturesHolder = new TextTexturesHolder();
	private final TexturesHolder texturesHolder = new TexturesHolder();

	private final Preview3DAnchorsDrawer anchorsDrawer = new Preview3DAnchorsDrawer();
	private final Preview3DBeatsDrawer beatsDrawer = new Preview3DBeatsDrawer();
	private final Preview3DCameraHandler cameraHandler = new Preview3DCameraHandler();
	private final Preview3DFingeringDrawer fingeringDrawer = new Preview3DFingeringDrawer();
	private final Preview3DGuitarSoundsDrawer guitarSoundsDrawer = new Preview3DGuitarSoundsDrawer();
	private final Preview3DHandShapesDrawer handShapesDrawer = new Preview3DHandShapesDrawer();
	private final Preview3DInlayDrawer inlayDrawer = new Preview3DInlayDrawer();
	private final Preview3DLaneBordersDrawer laneBordersDrawer = new Preview3DLaneBordersDrawer();
	private final Preview3DLyricsDrawer lyricsDrawer = new Preview3DLyricsDrawer();
	private final Preview3DStringsFretsDrawer stringsFretsDrawer = new Preview3DStringsFretsDrawer();
	private final Preview3DVideoDrawer videoDrawer = new Preview3DVideoDrawer();

	private boolean active = true;

	private static GLData prepareGLData() {
		final GLData data = new GLData();
		data.majorVersion = 3;
		data.minorVersion = 0;
		data.samples = Config.antialiasingSamples;

		return data;
	}

	public Preview3DPanel() {
		super(prepareGLData());
	}

	@Override
	public void init() {
		noteStatusModels.init(texturesHolder);

		anchorsDrawer.init(chartData);
		beatsDrawer.init(chartData, textTexturesHolder);
		cameraHandler.init(chartTimeHandler, chartData);
		fingeringDrawer.init(chartData, noteStatusModels, texturesHolder);
		guitarSoundsDrawer.init(chartData, noteStatusModels, texturesHolder);
		handShapesDrawer.init(chartData);
		inlayDrawer.init(chartData, texturesHolder);
		laneBordersDrawer.init(chartData);
		lyricsDrawer.init(chartData, textTexturesHolder);
		stringsFretsDrawer.init(chartData);
		videoDrawer.init(chartData);

		addKeyListener(keyboardHandler);

		cameraUpdater = new Framer(cameraHandler::updateFretFocus, 100);
		cameraUpdater.start();
	}

	@Override
	public void paint(final Graphics g) {
		if (!active) {
			return;
		}

		try {
			if (!isValid()) {
				GL.setCapabilities(null);
				return;
			}

			render();
		} catch (final Exception e) {
			Logger.error("Exception in paint", e);
			if (e instanceof AWTException) {
				Logger.error("stopping painting of GL component");
				active = false;
			}
		}
	}

	Texture sprite;

	@Override
	public void initGL() {
		try {
			GL.createCapabilities();

			shadersHolder.initGL();
			textTexturesHolder.initGL();
			texturesHolder.initGL();

			videoDrawer.initGL();

			GL30.glEnable(GL30.GL_DEPTH_TEST);
			GL30.glDepthFunc(GL30.GL_GEQUAL);
			GL30.glClearDepth(0);

			GL30.glEnable(GL30.GL_BLEND);
			GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

			if (Config.antialiasingSamples > 1) {
				GL30.glEnable(GL30.GL_MULTISAMPLE);
			}
		} catch (final Exception e) {
			Logger.error("Exception in initGL", e);
			throw e;
		}
	}

	@Override
	public void paintGL() {
		try {
			final Timer timer = new Timer();
			GL30.glViewport(0, 0, getWidth(), getHeight());

			final Color backgroundColor = ColorLabel.PREVIEW_3D_BACKGROUND.color();
			GL30.glClearColor(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f,
					backgroundColor.getBlue() / 255f, backgroundColor.getAlpha() / 255f);
			GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
			GL30.glDepthFunc(GL30.GL_GEQUAL);

			if (chartData == null || chartData.isEmpty) {
				swapBuffers();
				return;
			}
			timer.addTimestamp("clearing");

			cameraHandler.updateCamera(1.0 * getWidth() / getHeight());
			shadersHolder.setSceneMatrix(cameraHandler.currentMatrix);
			timer.addTimestamp("updating camera");

			videoDrawer.draw(shadersHolder, getWidth(), getHeight());
			timer.addTimestamp("videoDrawer");

			final Preview3DDrawData drawData = new Preview3DDrawData(chartTimeHandler, chartData, repeatManager);
			timer.addTimestamp("preparing draw data");

			beatsDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("beatsDrawer");
			laneBordersDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("laneBordersDrawer");
			anchorsDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("anchorsDrawer");

			if (modeManager.getMode() == EditMode.GUITAR) {
				handShapesDrawer.draw(shadersHolder, drawData);
				timer.addTimestamp("handShapesDrawer");
				guitarSoundsDrawer.draw(shadersHolder, drawData);
				timer.addTimestamp("guitarSoundsDrawer");
			}

			stringsFretsDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("stringsFretsDrawer");

			inlayDrawer.draw(shadersHolder);
			timer.addTimestamp("inlayDrawer");

			if (modeManager.getMode() == EditMode.GUITAR) {
				fingeringDrawer.draw(shadersHolder, drawData);
				timer.addTimestamp("fingeringDrawer");
			}

			lyricsDrawer.draw(shadersHolder, drawData.time, 1.0 * getHeight() / getWidth(),
					getHeight() < 500 ? 500.0 / getHeight() : 1);

			shadersHolder.clearShader();

			swapBuffers();
			timer.addTimestamp("finish");

			// timer.print("paintGL timings:", "%20s: %d");
		} catch (final Exception e) {
			Logger.error("Exception in paintGL", e);
		} catch (final Error error) {
			Logger.error("Error in paintGL", error);
		}
	}

	public void reloadTextures() {
		noteStatusModels.reload();
		texturesHolder.reloadTextures();
	}
}
