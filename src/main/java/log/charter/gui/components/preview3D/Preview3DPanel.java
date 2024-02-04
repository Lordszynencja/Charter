package log.charter.gui.components.preview3D;

import java.awt.Color;
import java.awt.Graphics;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.RepeatManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.Framer;
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
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.io.Logger;
import log.charter.util.Timer;

public class Preview3DPanel extends AWTGLCanvas {
	private static final long serialVersionUID = 1L;

	private ChartData data;
	private ModeManager modeManager;

	private Framer cameraUpdater;
	private RepeatManager repeatManager;
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

	private static GLData prepareGLData() {
		final GLData data = new GLData();
		data.majorVersion = 3;
		data.minorVersion = 0;
		return data;
	}

	public Preview3DPanel() {
		super(prepareGLData());
	}

	public void init(final ChartData data, final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final RepeatManager repeatManager) {
		this.data = data;
		this.repeatManager = repeatManager;
		this.modeManager = modeManager;

		anchorsDrawer.init(data);
		beatsDrawer.init(data, textTexturesHolder);
		cameraHandler.init(data);
		fingeringDrawer.init(data, texturesHolder);
		guitarSoundsDrawer.init(data, texturesHolder);
		handShapesDrawer.init(data);
		inlayDrawer.init(data, texturesHolder);
		laneBordersDrawer.init(data);
		lyricsDrawer.init(data, textTexturesHolder);
		stringsFretsDrawer.init(data);
		videoDrawer.init(data);

		addKeyListener(keyboardHandler);

		cameraUpdater = new Framer(cameraHandler::updateFretFocus, 100);
		cameraUpdater.start();
	}

	@Override
	public void paint(final Graphics g) {
		try {
			if (!isValid()) {
				GL.setCapabilities(null);
				return;
			}

			render();
		} catch (final Exception e) {
			Logger.error("Exception in paint", e);
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

			if (data == null || data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
				swapBuffers();
				return;
			}
			timer.addTimestamp("clearing");

			cameraHandler.updateCamera(1.0 * getWidth() / getHeight());
			shadersHolder.setSceneMatrix(cameraHandler.currentMatrix);
			timer.addTimestamp("updating camera");

			videoDrawer.draw(shadersHolder, getWidth(), getHeight());
			timer.addTimestamp("videoDrawer");

			final Preview3DDrawData drawData = new Preview3DDrawData(data, repeatManager);
			timer.addTimestamp("preparing draw data");

			beatsDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("beatsDrawer");
			laneBordersDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("laneBordersDrawer");
			anchorsDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("anchorsDrawer");
			stringsFretsDrawer.draw(shadersHolder);
			timer.addTimestamp("stringsFretsDrawer");
			handShapesDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("handShapesDrawer");
			guitarSoundsDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("guitarSoundsDrawer");
			inlayDrawer.draw(shadersHolder);
			timer.addTimestamp("inlayDrawer");
			fingeringDrawer.draw(shadersHolder, drawData);
			timer.addTimestamp("fingeringDrawer");

			lyricsDrawer.draw(shadersHolder, 1.0 * getHeight() / getWidth(),
					getHeight() < 500 ? 500.0 / getHeight() : 1);

			shadersHolder.clearShader();

			swapBuffers();
			timer.addTimestamp("finish");

			// timer.print("paintGL timings:", "%20s: %d");
		} catch (final Exception e) {
			Logger.error("Exception in paintGL", e);
			throw e;
		} catch (final Error error) {
			error.printStackTrace();
		}
	}

	public void reloadTextures() {
		texturesHolder.reloadTextures();
	}
}
