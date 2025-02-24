package log.charter.gui.components.toolbar;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInteger;
import static log.charter.gui.components.utils.ComponentUtils.addRightPressListener;
import static log.charter.gui.components.utils.ComponentUtils.setIcon;
import static log.charter.util.FileUtils.imagesFolder;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import log.charter.data.ChartData;
import log.charter.data.GridType;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.validators.IntegerValueValidator;
import log.charter.gui.lookAndFeel.CharterSliderUI;
import log.charter.io.Logger;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.RepeatManager;
import log.charter.services.audio.AudioHandler;
import log.charter.services.audio.ClapsHandler;
import log.charter.services.audio.MetronomeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.util.ImageUtils;

public class ChartToolbar extends JToolBar implements IChartToolbar, Initiable {
	private static final long serialVersionUID = 1L;

	private static final int verticalSpacing = 8;
	private static final int elementHeight = 20;
	public static final int height = elementHeight + 2 * verticalSpacing;

	private static final int horizontalSpacing = 5;

	private static final BufferedImage repeaterIcon = ImageUtils.loadSafe(imagesFolder + "toolbarRepeater.png");
	private static final BufferedImage gridBeatTypeIcon = ImageUtils.loadSafe(imagesFolder + "toolbarGridTypeBeat.png");
	private static final BufferedImage gridNoteTypeIcon = ImageUtils.loadSafe(imagesFolder + "toolbarGridTypeNote.png");
	private static final BufferedImage volumeIcon = ImageUtils.loadSafe(imagesFolder + "toolbarVolume.png");
	private static final BufferedImage sfxVolumeIcon = ImageUtils.loadSafe(imagesFolder + "toolbarSFXVolume.png");

	private ActionHandler actionHandler;
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ClapsHandler clapsHandler;
	private KeyboardHandler keyboardHandler;
	private MetronomeHandler metronomeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private RepeatManager repeatManager;
	private WaveFormDrawer waveFormDrawer;

	private JToggleButton midi;
	private JToggleButton claps;
	private JToggleButton metronome;

	private JToggleButton waveformGraph;
	private JToggleButton intensityRMSIndicator;

	private JToggleButton repeater;

	private FieldWithLabel<TextInputWithValidation> gridSize;
	private JToggleButton beatGridType;
	private JToggleButton noteGridType;

	private FieldWithLabel<JSlider> volume;

	private FieldWithLabel<TextInputWithValidation> playbackSpeed;
	private JToggleButton lowPassFilter;
	private JToggleButton highPassFilter;
	private JToggleButton bandPassFilter;

	private JButton playButton;

	private int newSpeed = Config.stretchedMusicSpeed;

	public ChartToolbar() {
		super();

		setLayout(null);

		setFocusable(true);
		setFloatable(false);
		setBackground(ColorLabel.BASE_BG_2.color());
	}

	private void setComponentBounds(final Component c, final int x, final int y, final int w, final int h) {
		ComponentUtils.setComponentBounds(c, x, y, w, h);
		c.validate();
		c.repaint();
	}

	private void add(final AtomicInteger x, final int horizontalSpace, final Component c) {
		setComponentBounds(c, x.getAndAdd(c.getWidth() + horizontalSpace), verticalSpacing, c.getWidth(),
				c.getHeight());
		add(c);
	}

	private void add(final AtomicInteger x, final Component c) {
		add(x, horizontalSpacing, c);
	}

	private JToggleButton addToggleButton(final AtomicInteger x, final int horizontalSpacing, final Label label,
			final Runnable onClick, final int buttonWidth) {
		final JToggleButton toggleButton = new JToggleButton(label.label());
		toggleButton.addActionListener(a -> onClick.run());
		toggleButton.setFocusable(false);

		final int width = (buttonWidth > 0) ? buttonWidth : toggleButton.getPreferredSize().width;
		toggleButton.setBounds(x.get(), 0, width, elementHeight);
		add(x, horizontalSpacing, toggleButton);

		return toggleButton;
	}

	private JToggleButton addToggleButton(final AtomicInteger x, final Label label, final Runnable onClick,
			final int buttonWidth) {
		return addToggleButton(x, horizontalSpacing, label, onClick, buttonWidth);
	}

	private JToggleButton addToggleButton(final AtomicInteger x, final Label label, final Runnable onClick) {
		return addToggleButton(x, label, onClick, 0);
	}

	private JToggleButton addToggleButton(final AtomicInteger x, final Label label, final BufferedImage icon,
			final Runnable onClick) {
		final JToggleButton toggleButton = addToggleButton(x, label, onClick);
		setIcon(toggleButton, icon);

		return toggleButton;
	}

	private void addSeparator(final AtomicInteger x) {
		x.addAndGet(horizontalSpacing);
		final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
		separator.setForeground(ColorLabel.BASE_BG_2.color());
		separator.setSize(4, elementHeight);
		add(x, separator);
		x.addAndGet(horizontalSpacing);
	}

	private FieldWithLabel<TextInputWithValidation> createNumberField(final Label label,
			final LabelPosition labelPosition, final int inputWidth, //
			final Integer value, final int min, final int max, final boolean allowEmpty,
			final Consumer<Integer> onChange) {
		final TextInputWithValidation input = generateForInteger(value, inputWidth, //
				new IntegerValueValidator(min, max, allowEmpty), onChange, false);

		final FieldWithLabel<TextInputWithValidation> field = //
				new FieldWithLabel<>(label, 0, inputWidth, elementHeight, input, labelPosition);
		field.setBackground(getBackground());

		return field;
	}

	private void addGridSizeInput(final AtomicInteger x) {
		gridSize = createNumberField(Label.TOOLBAR_GRID_SIZE, LabelPosition.LEFT_PACKED, 25, //
				Config.gridSize, 1, 128, false, newGridSize -> {
					Config.gridSize = newGridSize;
					Config.markChanged();
				});
		add(x, 1, gridSize);
	}

	private JButton generateGridSizeButton(final Font font, final String text, final ActionListener actionListener) {
		final JButton gridChangeButton = new JButton(text);
		gridChangeButton.setSize(24, elementHeight / 2);
		gridChangeButton.setFont(font);
		gridChangeButton.setFocusable(false);
		gridChangeButton.addActionListener(actionListener);

		return gridChangeButton;
	}

	private void halveGridSize() {
		if (Config.gridSize % 2 != 0) {
			return;
		}

		Config.gridSize /= 2;
		Config.markChanged();
		updateValues();
	}

	private void doubleGridSize() {
		if (Config.gridSize > 64) {
			return;
		}

		Config.gridSize *= 2;
		Config.markChanged();
		updateValues();
	}

	private void addGridSizeButtons(final AtomicInteger x) {
		final Font miniFont = new Font(Font.DIALOG, Font.PLAIN, 8);

		final JButton gridDoubleButton = generateGridSizeButton(miniFont, "+", a -> doubleGridSize());
		setComponentBounds(gridDoubleButton, x.get(), verticalSpacing, gridDoubleButton.getWidth(),
				gridDoubleButton.getHeight());
		add(gridDoubleButton);

		final JButton gridHalveButton = generateGridSizeButton(miniFont, "-", a -> halveGridSize());
		setComponentBounds(gridHalveButton, x.getAndAdd(gridHalveButton.getWidth() + horizontalSpacing),
				verticalSpacing + gridDoubleButton.getHeight(), gridHalveButton.getWidth(),
				gridHalveButton.getHeight());
		add(gridHalveButton);
	}

	private void onGridTypeChange(final GridType newGridType) {
		Config.gridType = newGridType;
		Config.markChanged();
	}

	private void addGridTypes(final AtomicInteger x) {
		beatGridType = addToggleButton(x, 1, Label.BEAT_GRID_TYPE, () -> onGridTypeChange(GridType.BEAT), 25);
		setIcon(beatGridType, gridBeatTypeIcon);

		noteGridType = addToggleButton(x, Label.NOTE_GRID_TYPE, () -> onGridTypeChange(GridType.NOTE), 25);
		setIcon(noteGridType, gridNoteTypeIcon);

		final ButtonGroup gridTypeGroup = new ButtonGroup();
		gridTypeGroup.add(beatGridType);
		gridTypeGroup.add(noteGridType);
	}

	private int getVolumeAsInteger(final double volume) {
		if (volume <= 0) {
			return 0;
		}
		if (volume >= 1) {
			return 100;
		}

		return (int) (volume * 100);
	}

	private void changeSFXVolume(final double newVolume) {
		Config.audio.sfxVolume = newVolume;
		Config.markChanged();
	}

	private void addVolumeSlider(final AtomicInteger x, final Label label, final BufferedImage icon, final double value,
			final DoubleConsumer volumeSetter) {
		final JSlider volumeSlider = new JSlider(0, 100, getVolumeAsInteger(value));
		volumeSlider.addChangeListener(e -> volumeSetter.accept(volumeSlider.getValue() / 100.0));
		volumeSlider.setFocusable(false);
		volumeSlider.setBackground(getBackground());

		volume = new FieldWithLabel<>(label, icon.getWidth(), 72, elementHeight, volumeSlider,
				LabelPosition.LEFT_CLOSE);
		setIcon(volume.label, icon);
		ComponentUtils.addRightPressListener(volumeSlider,
				() -> new AudioStemsSettings(chartData, charterFrame, projectAudioHandler));
		ComponentUtils.addRightPressListener(volume,
				() -> new AudioStemsSettings(chartData, charterFrame, projectAudioHandler));

		add(x, volume);
		volumeSlider.setUI(new CharterSliderUI());
	}

	private void changeSpeed(final int newSpeed) {
		this.newSpeed = newSpeed;
		new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
				Logger.error("error on changing speed", e);
			}
			if (this.newSpeed != newSpeed) {
				return;
			}

			setSpeed(newSpeed);
		}).start();
	}

	private void setSpeed(final int newSpeed) {
		audioHandler.stopMusic();

		Config.stretchedMusicSpeed = newSpeed;
		Config.markChanged();
	}

	private void addPlaybackSpeed(final AtomicInteger x) {
		playbackSpeed = createNumberField(Label.TOOLBAR_SLOWED_PLAYBACK_SPEED, LabelPosition.LEFT_PACKED, 30, //
				Config.stretchedMusicSpeed, 1, 500, false, this::changeSpeed);
		this.add(x, playbackSpeed);

		playbackSpeed.field.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
			}

			@Override
			public void focusLost(final FocusEvent e) {
				setSpeed(newSpeed);
			}
		});
	}

	private void addTimeControls(final AtomicInteger x) {
		final JButton rewindButton = new JButton("⏮");
		rewindButton.setFocusable(false);
		rewindButton.setSize(30, 20);
		rewindButton.addChangeListener(e -> {
			if (rewindButton.getModel().isPressed()) {
				keyboardHandler.setRewind();
			} else {
				keyboardHandler.clearRewind();
			}
		});

		add(x, 0, rewindButton);

		playButton = new JButton();
		playButton.setFocusable(false);
		playButton.setSize(30, 20);
		playButton.addActionListener(e -> actionHandler.fireAction(Action.PLAY_AUDIO));
		setPlayButtonIcon();

		add(x, 0, playButton);

		final JButton fastForwardButton = new JButton("⏩");
		fastForwardButton.setFocusable(false);
		fastForwardButton.setSize(30, 20);
		fastForwardButton.addChangeListener(e -> {
			if (fastForwardButton.getModel().isPressed()) {
				keyboardHandler.setRewind();
				keyboardHandler.setFastForward();
			} else {
				keyboardHandler.clearFastForward();
			}
		});

		add(x, 0, fastForwardButton);
	}

	public void setPlayButtonIcon() {
		playButton.setText(audioHandler.isPlaying() ? "⏸" : "▶️");
	}

	private void showLowPassSettings() {
		audioHandler.stopMusic();

		new LowPassSettings(charterFrame);
	}

	private void showHighPassSettings() {
		audioHandler.stopMusic();

		new HighPassSettings(charterFrame);
	}

	private void showBandPassSettings() {
		audioHandler.stopMusic();

		new BandPassSettings(charterFrame);
	}

	@Override
	public void init() {
		final AtomicInteger x = new AtomicInteger(5);

		midi = addToggleButton(x, Label.TOOLBAR_MIDI, audioHandler::toggleMidiNotes);
		claps = addToggleButton(x, Label.TOOLBAR_CLAPS, clapsHandler::toggleClaps);
		metronome = addToggleButton(x, Label.TOOLBAR_METRONOME, metronomeHandler::toggleMetronome);

		addSeparator(x);

		waveformGraph = addToggleButton(x, Label.TOOLBAR_WAVEFORM_GRAPH, waveFormDrawer::toggle);
		intensityRMSIndicator = addToggleButton(x, Label.TOOLBAR_RMS_INDICATOR, waveFormDrawer::toggleRMS);
		intensityRMSIndicator.setEnabled(false);

		addSeparator(x);

		repeater = addToggleButton(x, Label.TOOLBAR_REPEATER, repeaterIcon, repeatManager::toggle);

		addSeparator(x);

		addGridSizeInput(x);
		addGridSizeButtons(x);
		addGridTypes(x);

		addSeparator(x);

		addVolumeSlider(x, Label.TOOLBAR_VOLUME, volumeIcon, projectAudioHandler.getVolume(),
				projectAudioHandler::setVolume);
		addVolumeSlider(x, Label.TOOLBAR_SFX_VOLUME, sfxVolumeIcon, Config.audio.sfxVolume, this::changeSFXVolume);
		addPlaybackSpeed(x);
		addTimeControls(x);

		addSeparator(x);

		lowPassFilter = addToggleButton(x, 1, Label.LOW_PASS, () -> audioHandler.toggleLowPassFilter(), 40);
		addRightPressListener(lowPassFilter, this::showLowPassSettings);
		bandPassFilter = addToggleButton(x, 1, Label.BAND_PASS, () -> audioHandler.toggleBandPassFilter(), 40);
		addRightPressListener(bandPassFilter, this::showBandPassSettings);
		highPassFilter = addToggleButton(x, 1, Label.HIGH_PASS, () -> audioHandler.toggleHighPassFilter(), 40);
		addRightPressListener(highPassFilter, this::showHighPassSettings);

		updateValues();
		setSize(getWidth(), height);

		addKeyListener(keyboardHandler);
	}

	@Override
	public void updateValues() {
		midi.setSelected(audioHandler.midiNotesPlaying);
		midi.setEnabled(modeManager.getMode() != EditMode.TEMPO_MAP);
		claps.setSelected(clapsHandler.claps());
		metronome.setSelected(metronomeHandler.metronome());
		waveformGraph.setSelected(waveFormDrawer.drawing());
		waveformGraph.setEnabled(modeManager.getMode() != EditMode.TEMPO_MAP);
		intensityRMSIndicator.setEnabled(waveFormDrawer.drawing());
		intensityRMSIndicator.setSelected(waveFormDrawer.rms());
		repeater.setSelected(repeatManager.isOn());

		gridSize.field.setTextWithoutEvent(Config.gridSize + "");
		switch (Config.gridType) {
			case BEAT:
				beatGridType.setSelected(true);
				break;
			case NOTE:
				noteGridType.setSelected(true);
				break;
			default:
				Logger.error("Wrong grid type for toolbar " + Config.gridType);
				break;
		}

		playbackSpeed.field.setTextWithoutEvent(Config.stretchedMusicSpeed + "");

		lowPassFilter.setSelected(audioHandler.lowPassFilterEnabled);
		bandPassFilter.setSelected(audioHandler.bandPassFilterEnabled);
		highPassFilter.setSelected(audioHandler.highPassFilterEnabled);
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
	}
}
