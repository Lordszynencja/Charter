package log.charter.gui.components.preview3D;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shapes.Texture;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.io.Logger;

public class Preview3DPanel extends AWTGLCanvas implements MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private ChartData data;
	private ModeManager modeManager;

	private final ShadersHolder shadersHolder = new ShadersHolder();
	private final TexturesHolder texturesHolder = new TexturesHolder();

	private final Preview3DAnchorsDrawer anchorsDrawer = new Preview3DAnchorsDrawer();
	private final Preview3DBeatsDrawer beatsDrawer = new Preview3DBeatsDrawer();
	private final Preview3DCameraHandler cameraHandler = new Preview3DCameraHandler();
	private final Preview3DFingeringDrawer fingeringDrawer = new Preview3DFingeringDrawer();
	private final Preview3DFretLanesDrawer fretLanesDrawer = new Preview3DFretLanesDrawer();
	private final Preview3DHandShapesDrawer handShapesDrawer = new Preview3DHandShapesDrawer();
	private final Preview3DGuitarSoundsDrawer soundsDrawer = new Preview3DGuitarSoundsDrawer();
	private final Preview3DStringsDrawer stringsDrawer = new Preview3DStringsDrawer();
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

	public void init(final ChartData data, final KeyboardHandler keyboardHandler, final ModeManager modeManager) {
		this.data = data;
		this.modeManager = modeManager;

		anchorsDrawer.init(data);
		beatsDrawer.init(data);
		cameraHandler.init(data);
		fingeringDrawer.init(data, texturesHolder);
		fretLanesDrawer.init(data);
		handShapesDrawer.init(data);
		soundsDrawer.init(data);
		stringsDrawer.init(data);
		videoDrawer.init(data);

		addMouseMotionListener(this);
		addKeyListener(keyboardHandler);

		mouseX = 500;
		mouseY = 200;
	}

//
//	private void drawFretNumbers(final Drawable3DShapesListForScene shapesList) {
//		final Color color = ColorLabel.BASE_TEXT.color();
//		final double y = getStringPosition(7);
//		for (int i = 1; i <= Config.frets; i++) {
//			shapesList.addText(color, 9.8, new Point3D(getFretMiddlePosition(i), y, 1), "" + i);
//		}
//	}

	private int mouseX = 0;
	private int mouseY = 0;

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

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	Texture sprite;

	@Override
	public void initGL() {
		try {
			GL.createCapabilities();

			shadersHolder.init();
			texturesHolder.init();

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
			GL30.glViewport(0, 0, getWidth(), getHeight());
			GL30.glClearColor(0.25f, 0.25f, 0.25f, 1);
			GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

			if (data == null || data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
				swapBuffers();
				return;
			}

			cameraHandler.updateCamera(1.0 * getWidth() / getHeight(), ((double) mouseX) / getWidth(),
					((double) mouseY) / getHeight());
			shadersHolder.setSceneMatrix(cameraHandler.currentMatrix);

			videoDrawer.draw(shadersHolder, getWidth(), getHeight());
			stringsDrawer.draw(shadersHolder);
			fretLanesDrawer.draw(shadersHolder);
			anchorsDrawer.draw(shadersHolder);
			handShapesDrawer.draw(shadersHolder);
			beatsDrawer.draw(shadersHolder);
			soundsDrawer.draw(shadersHolder);
			fingeringDrawer.draw(shadersHolder);

//		drawFretNumbers(shapesList);

			shadersHolder.clearShader();

			swapBuffers();
		} catch (final Exception e) {
			Logger.error("Exception in paintGL", e);
			throw e;
		} catch (final Error error) {
			error.printStackTrace();
		}
	}
}
