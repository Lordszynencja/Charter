package log.charter.gui.components.toolbar;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInteger;
import static log.charter.gui.components.utils.ComponentUtils.addRightPressListener;
import static log.charter.gui.components.utils.ComponentUtils.numericFilter;
import static log.charter.gui.components.utils.ComponentUtils.setIcon;
import static log.charter.util.FileUtils.imagesFolder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import log.charter.data.ChartData;
import log.charter.data.GridType;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.AudioConfig;
import log.charter.data.config.values.GridConfig;
import log.charter.gui.ChartPanel;
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
	private static interface BooleanConsumer {
		void consume(boolean value);
	}

	private static final long serialVersionUID = 1L;

	private static int horizontalSpacing = GraphicalConfig.inputSize / 4;
	private static int verticalSpacing = GraphicalConfig.inputSize / 2;
	private static final int elementHeight = 20;

	private static final BufferedImage repeaterIcon = ImageUtils.loadSafeFromDir(imagesFolder, "toolbarRepeater.png");
	private static final BufferedImage gridBeatTypeIcon = ImageUtils.loadSafeFromDir(imagesFolder,
			"toolbarGridTypeBeat.png");
	private static final BufferedImage gridNoteTypeIcon = ImageUtils.loadSafeFromDir(imagesFolder,
			"toolbarGridTypeNote.png");
	private static final BufferedImage chartLocked = ImageUtils.loadSafeFromDir(imagesFolder, "toolbarChartLocked.png");
	private static final BufferedImage chartUnlocked = ImageUtils.loadSafeFromDir(imagesFolder,
			"toolbarChartUnlocked.png");
	private static final BufferedImage volumeIcon = ImageUtils.loadSafeFromDir(imagesFolder, "toolbarVolume.png");
	private static final BufferedImage volumeMuteIcon = ImageUtils.loadSafeFromDir(imagesFolder,
			"toolbarVolumeMute.png");
	private static final BufferedImage sfxVolumeIcon = ImageUtils.loadSafeFromDir(imagesFolder, "toolbarSFXVolume.png");
	private static final BufferedImage sfxVolumeMuteIcon = ImageUtils.loadSafeFromDir(imagesFolder,
			"toolbarSFXVolumeMute.png");

	private ActionHandler actionHandler;
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartPanel chartPanel;
	private ChartToolbar chartToolbar;
	private ClapsHandler clapsHandler;
	private KeyboardHandler keyboardHandler;
	private MetronomeHandler metronomeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private RepeatManager repeatManager;
	private WaveFormDrawer waveFormDrawer;

	private final KeyListener defocusingInput = new KeyListener() {
		@Override
		public void keyTyped(final KeyEvent e) {
		}

		@Override
		public void keyPressed(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				chartPanel.requestFocusInWindow();
			}
		}

		@Override
		public void keyReleased(final KeyEvent e) {
		}
	};

	private JToggleButton midi;
	private JToggleButton claps;
	private JToggleButton metronome;

	private JToggleButton waveformGraph;
	private JToggleButton rms;

	private JToggleButton repeater;

	private FieldWithLabel<TextInputWithValidation> gridSize;
	private JButton gridDoubleButton;
	private JButton gridHalveButton;
	private JToggleButton beatGridType;
	private JToggleButton noteGridType;

	private JButton chartLock;

	private FieldWithLabel<JSlider> volume;
	private FieldWithLabel<JSlider> sfxVolume;

	private FieldWithLabel<TextInputWithValidation> playbackSpeed;
	private JButton rewindButton;
	private JButton playButton;
	private JButton fastForwardButton;

	private JToggleButton lowPassFilter;
	private JToggleButton highPassFilter;
	private JToggleButton bandPassFilter;

	public ChartToolbar() {
		super();

		setLayout(null);

		setFocusable(true);
		setFloatable(false);
		setBackground(ColorLabel.BASE_BG_2.color());
	}

	private JToggleButton addToggleButton(final Label label, final Label tooltipLabel, final Runnable onClick) {
		final JToggleButton toggleButton = new JToggleButton(label.label());
		toggleButton.setToolTipText(tooltipLabel.label());
		toggleButton.addActionListener(a -> onClick.run());
		toggleButton.setFocusable(false);

		add(toggleButton);

		return toggleButton;
	}

	private JToggleButton addToggleButton(final Label label, final Label tooltipLabel, final BufferedImage icon,
			final Runnable onClick) {
		final JToggleButton toggleButton = addToggleButton(label, tooltipLabel, onClick);
		setIcon(toggleButton, icon);

		return toggleButton;
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
		field.field.addKeyListener(numericFilter);

		return field;
	}

	private void addGridSizeInput() {
		gridSize = createNumberField(Label.TOOLBAR_GRID_SIZE, LabelPosition.LEFT_PACKED, 25, //
				GridConfig.gridSize, 1, 128, false, newGridSize -> {
					GridConfig.gridSize = newGridSize;
					Config.markChanged();
				});

		gridSize.field.addKeyListener(defocusingInput);

		add(gridSize);
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
		if (GridConfig.gridSize % 2 != 0) {
			return;
		}

		GridConfig.gridSize /= 2;
		Config.markChanged();
		updateValues();
	}

	private void doubleGridSize() {
		if (GridConfig.gridSize > 64) {
			return;
		}

		GridConfig.gridSize *= 2;
		Config.markChanged();
		updateValues();
	}

	private void addGridSizeButtons() {
		final Font miniFont = new Font(Font.DIALOG, Font.PLAIN, 8);

		gridDoubleButton = generateGridSizeButton(miniFont, "+", a -> doubleGridSize());
		add(gridDoubleButton);

		gridHalveButton = generateGridSizeButton(miniFont, "-", a -> halveGridSize());
		add(gridHalveButton);
	}

	private void onGridTypeChange(final GridType newGridType) {
		GridConfig.gridType = newGridType;
		Config.markChanged();
	}

	private void addGridTypes() {
		beatGridType = addToggleButton(Label.GRID_TYPE_BEAT, Label.GRID_TYPE_BEAT_TOOLTIP,
				() -> onGridTypeChange(GridType.BEAT));
		setIcon(beatGridType, gridBeatTypeIcon);

		noteGridType = addToggleButton(Label.GRID_TYPE_NOTE, Label.GRID_TYPE_NOTE_TOOLTIP,
				() -> onGridTypeChange(GridType.NOTE));
		setIcon(noteGridType, gridNoteTypeIcon);

		final ButtonGroup gridTypeGroup = new ButtonGroup();
		gridTypeGroup.add(beatGridType);
		gridTypeGroup.add(noteGridType);
	}

	public void setChartLockIcon() {
		setIcon(chartLock, keyboardHandler.scrollLock() ? chartLocked : chartUnlocked);
	}

	private void addChartLock() {
		chartLock = new JButton(new ImageIcon(chartUnlocked));
		chartLock.setToolTipText(Label.CHART_LOCK_TOOLTIP.label());
		chartLock.createToolTip().setForeground(Color.WHITE);
		chartLock.setEnabled(false);
		chartLock.setBackground(Color.RED);
		setChartLockIcon();

		add(chartLock);
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
		AudioConfig.sfxVolume = newVolume;
		Config.markChanged();
	}

	private void openStemSettings() {
		new AudioStemsSettings(chartData, charterFrame, chartToolbar, projectAudioHandler);
	}

	private void toggleMute(final JLabel label, final JSlider volumeSlider, final BooleanConsumer muteSetter,
			final BufferedImage icon, final BufferedImage mutedIcon) {
		final boolean newMuted = volumeSlider.isEnabled();

		volumeSlider.setEnabled(!newMuted);
		volumeSlider.setForeground((newMuted ? ColorLabel.BASE_BORDER : ColorLabel.BASE_HIGHLIGHT).color());

		muteSetter.consume(newMuted);
		setIcon(label, newMuted ? mutedIcon : icon);
		label.repaint();
	}

	private FieldWithLabel<JSlider> addVolumeSlider(final Label label, final Label tooltip, final BufferedImage icon,
			final BufferedImage mutedIcon, final double value, final DoubleConsumer volumeSetter, final boolean muted,
			final BooleanConsumer muteSetter) {
		final JSlider volumeSlider = new JSlider(0, 100, getVolumeAsInteger(value));
		volumeSlider.addChangeListener(e -> volumeSetter.accept(volumeSlider.getValue() / 100.0));
		volumeSlider.setFocusable(false);
		volumeSlider.setBackground(getBackground());
		volumeSlider.setToolTipText(tooltip.label());
		volumeSlider.setEnabled(!muted);

		final FieldWithLabel<JSlider> field = new FieldWithLabel<>(label, icon.getWidth(), 72, elementHeight,
				volumeSlider, LabelPosition.LEFT_CLOSE);
		field.label.setToolTipText(tooltip.label());
		setIcon(field.label, icon);
		ComponentUtils.addLeftPressListener(field.label,
				() -> toggleMute(field.label, volumeSlider, muteSetter, icon, mutedIcon));
		ComponentUtils.addRightPressListener(volumeSlider, this::openStemSettings);
		ComponentUtils.addRightPressListener(field, this::openStemSettings);

		add(field);
		volumeSlider.setUI(new CharterSliderUI());

		return field;
	}

	private void changeSpeed(final int newSpeed) {
		audioHandler.stopMusic();

		Config.stretchedMusicSpeed = newSpeed;
		Config.markChanged();
	}

	private void addPlaybackSpeed() {
		playbackSpeed = createNumberField(Label.TOOLBAR_SLOWED_PLAYBACK_SPEED, LabelPosition.LEFT_PACKED, 30, //
				Config.stretchedMusicSpeed, 1, 500, false, this::changeSpeed);

		playbackSpeed.field.addKeyListener(defocusingInput);

		this.add(playbackSpeed);
	}

	private void addTimeControls() {
		rewindButton = new JButton("⏮");
		rewindButton.setHorizontalAlignment(CENTER);
		rewindButton.setToolTipText(Label.REWIND_TOOLTIP.label());
		rewindButton.setFocusable(false);
		rewindButton.addChangeListener(e -> {
			if (rewindButton.getModel().isPressed()) {
				keyboardHandler.setRewind();
			} else {
				keyboardHandler.clearRewind();
			}
		});

		add(rewindButton);

		playButton = new JButton();
		playButton.setHorizontalAlignment(CENTER);
		playButton.setToolTipText(Label.PLAY_TOOLTIP.label());
		playButton.setFocusable(false);
		playButton.addActionListener(e -> actionHandler.fireAction(Action.PLAY_AUDIO));
		setPlayButtonIcon();

		add(playButton);

		fastForwardButton = new JButton("⏩");
		fastForwardButton.setHorizontalAlignment(CENTER);
		fastForwardButton.setToolTipText(Label.FAST_FORWARD_TOOLTIP.label());
		fastForwardButton.setFocusable(false);
		fastForwardButton.addChangeListener(e -> {
			if (fastForwardButton.getModel().isPressed()) {
				keyboardHandler.setRewind();
				keyboardHandler.setFastForward();
			} else {
				keyboardHandler.clearFastForward();
			}
		});

		add(fastForwardButton);
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
		midi = addToggleButton(Label.TOOLBAR_MIDI, Label.TOOLBAR_MIDI_TOOLTIP, audioHandler::toggleMidiNotes);
		claps = addToggleButton(Label.TOOLBAR_CLAPS, Label.TOOLBAR_CLAPS_TOOLTIP, clapsHandler::toggleClaps);
		metronome = addToggleButton(Label.TOOLBAR_METRONOME, Label.TOOLBAR_METRONOME_TOOLTIP,
				metronomeHandler::toggleMetronome);

		waveformGraph = addToggleButton(Label.TOOLBAR_WAVEFORM, Label.TOOLBAR_WAVEFORM_TOOLTIP, waveFormDrawer::toggle);
		rms = addToggleButton(Label.TOOLBAR_RMS, Label.TOOLBAR_RMS_TOOLTIP, waveFormDrawer::toggleRMS);
		rms.setEnabled(false);

		repeater = addToggleButton(Label.TOOLBAR_REPEATER, Label.TOOLBAR_REPEATER_TOOLTIP, repeaterIcon,
				repeatManager::toggle);

		addGridSizeInput();
		addGridSizeButtons();
		addGridTypes();

		addChartLock();

		volume = addVolumeSlider(Label.TOOLBAR_VOLUME, Label.TOOLBAR_VOLUME_TOOLTIP, volumeIcon, volumeMuteIcon,
				projectAudioHandler.getVolume(), projectAudioHandler::setVolume, AudioConfig.volumeMute,
				v -> AudioConfig.volumeMute = v);
		sfxVolume = addVolumeSlider(Label.TOOLBAR_SFX_VOLUME, Label.TOOLBAR_SFX_VOLUME_TOOLTIP, sfxVolumeIcon,
				sfxVolumeMuteIcon, AudioConfig.sfxVolume, this::changeSFXVolume, AudioConfig.sfxVolumeMute,
				v -> AudioConfig.sfxVolumeMute = v);
		addPlaybackSpeed();
		addTimeControls();

		lowPassFilter = addToggleButton(Label.LOW_PASS, Label.LOW_PASS_TOOLTIP,
				() -> audioHandler.toggleLowPassFilter());
		addRightPressListener(lowPassFilter, this::showLowPassSettings);
		bandPassFilter = addToggleButton(Label.BAND_PASS, Label.BAND_PASS_TOOLTIP,
				() -> audioHandler.toggleBandPassFilter());
		addRightPressListener(bandPassFilter, this::showBandPassSettings);
		highPassFilter = addToggleButton(Label.HIGH_PASS, Label.HIGH_PASS_TOOLTIP,
				() -> audioHandler.toggleHighPassFilter());
		addRightPressListener(highPassFilter, this::showHighPassSettings);

		updateValues();
		recalculateSizes();

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
		rms.setEnabled(waveFormDrawer.drawing());
		rms.setSelected(waveFormDrawer.rms());
		repeater.setSelected(repeatManager.isOn());

		gridSize.field.setTextWithoutEvent(GridConfig.gridSize + "");
		switch (GridConfig.gridType) {
			case BEAT:
				beatGridType.setSelected(true);
				break;
			case NOTE:
				noteGridType.setSelected(true);
				break;
			default:
				Logger.error("Wrong grid type for toolbar " + GridConfig.gridType);
				break;
		}

		volume.field.setValue(getVolumeAsInteger(projectAudioHandler.getVolume()));
		playbackSpeed.field.setTextWithoutEvent(Config.stretchedMusicSpeed + "");

		lowPassFilter.setSelected(audioHandler.lowPassFilterEnabled);
		bandPassFilter.setSelected(audioHandler.bandPassFilterEnabled);
		highPassFilter.setSelected(audioHandler.highPassFilterEnabled);
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
	}

	@Override
	public void focusGrid() {
		gridSize.field.requestFocusInWindow();
		gridSize.field.selectAll();
	}

	private void resizeButton(final AtomicInteger x, final AbstractButton b) {
		b.setFont(b.getFont().deriveFont(GraphicalConfig.inputSize / 1.6f));
		final int width = BasicGraphicsUtils.getPreferredButtonSize(b, b.getIconTextGap()).width;
		b.setBounds(x.getAndAdd(horizontalSpacing + width), verticalSpacing, width, GraphicalConfig.inputSize);
	}

	private void resizeIconButton(final AtomicInteger x, final AbstractButton b) {
		b.setFont(b.getFont().deriveFont(GraphicalConfig.inputSize / 1.6f));
		b.setBounds(x.getAndAdd(GraphicalConfig.inputSize), verticalSpacing, GraphicalConfig.inputSize,
				GraphicalConfig.inputSize);
	}

	private void resizeGridInputs(final AtomicInteger x) {
		gridSize.label.setFont(gridSize.label.getFont().deriveFont(GraphicalConfig.inputSize / 1.6f));
		final int labelWidth = (int) Math.ceil(BasicGraphicsUtils.getStringWidth(gridSize.label,
				gridSize.label.getFontMetrics(gridSize.label.getFont()), gridSize.label.getText()));
		gridSize.label.setSize(labelWidth, GraphicalConfig.inputSize);

		gridSize.field.setFont(gridSize.field.getFont().deriveFont(GraphicalConfig.inputSize / 1.6f));
		gridSize.field.setLocation(gridSize.label.getWidth(), 0);
		gridSize.field.setSize(GraphicalConfig.inputSize * 3 / 2, GraphicalConfig.inputSize);

		gridSize.setLocation(x.get(), verticalSpacing);
		gridSize.setSize(gridSize.label.getWidth() + gridSize.field.getWidth(), GraphicalConfig.inputSize);
		x.getAndAdd(gridSize.getWidth() + 1);

		gridDoubleButton.setFont(gridDoubleButton.getFont().deriveFont(GraphicalConfig.inputSize / 2.5f));
		gridDoubleButton.setLocation(x.get(), verticalSpacing);
		gridDoubleButton.setSize(GraphicalConfig.inputSize, GraphicalConfig.inputSize / 2);

		gridHalveButton.setFont(gridHalveButton.getFont().deriveFont(GraphicalConfig.inputSize / 2.5f));
		gridHalveButton.setLocation(x.get(), verticalSpacing + GraphicalConfig.inputSize / 2);
		gridHalveButton.setSize(GraphicalConfig.inputSize, GraphicalConfig.inputSize / 2);

		x.getAndAdd(GraphicalConfig.inputSize + horizontalSpacing);

		resizeIconButton(x, beatGridType);
		x.getAndAdd(horizontalSpacing / 2);
		resizeIconButton(x, noteGridType);
		x.getAndAdd(horizontalSpacing);
	}

	private void resizePlaybackSpeed(final AtomicInteger x) {
		playbackSpeed.label.setFont(playbackSpeed.label.getFont().deriveFont(GraphicalConfig.inputSize / 1.6f));
		final int labelWidth = (int) Math.ceil(BasicGraphicsUtils.getStringWidth(playbackSpeed.label,
				gridSize.label.getFontMetrics(playbackSpeed.label.getFont()), playbackSpeed.label.getText()));
		playbackSpeed.label.setSize(labelWidth, GraphicalConfig.inputSize);

		playbackSpeed.field.setFont(playbackSpeed.field.getFont().deriveFont(GraphicalConfig.inputSize / 1.6f));
		playbackSpeed.field.setLocation(playbackSpeed.label.getWidth(), 0);
		playbackSpeed.field.setSize(GraphicalConfig.inputSize * 3 / 2, GraphicalConfig.inputSize);

		playbackSpeed.setLocation(x.get(), verticalSpacing);
		playbackSpeed.setSize(playbackSpeed.label.getWidth() + playbackSpeed.field.getWidth(),
				GraphicalConfig.inputSize);
		x.getAndAdd(playbackSpeed.getWidth() + horizontalSpacing / 2);

		resizeIconButton(x, rewindButton);
		x.getAndAdd(horizontalSpacing / 2);
		resizeIconButton(x, playButton);
		x.getAndAdd(horizontalSpacing / 2);
		resizeIconButton(x, fastForwardButton);
		x.getAndAdd(horizontalSpacing);
	}

	public void recalculateSizes() {
		horizontalSpacing = GraphicalConfig.inputSize / 4;
		verticalSpacing = GraphicalConfig.inputSize / 4;

		final AtomicInteger x = new AtomicInteger(5);

		this.setSize(getWidth(), GraphicalConfig.inputSize * 3 / 2);

		resizeButton(x, midi);
		resizeButton(x, claps);
		resizeButton(x, metronome);

		x.addAndGet(horizontalSpacing * 2);
		resizeButton(x, waveformGraph);
		resizeButton(x, rms);

		x.addAndGet(horizontalSpacing * 2);
		resizeButton(x, repeater);

		x.addAndGet(horizontalSpacing * 2);
		resizeGridInputs(x);

		x.addAndGet(horizontalSpacing * 2);
		resizeButton(x, chartLock);

		x.addAndGet(horizontalSpacing * 2);
		volume.setLocation(x.getAndAdd(volume.getWidth() + horizontalSpacing), verticalSpacing);
		sfxVolume.setLocation(x.getAndAdd(sfxVolume.getWidth() + horizontalSpacing), verticalSpacing);

		x.addAndGet(horizontalSpacing * 2);
		resizePlaybackSpeed(x);

		x.addAndGet(horizontalSpacing * 2);
		resizeButton(x, lowPassFilter);
		resizeButton(x, bandPassFilter);
		resizeButton(x, highPassFilter);
	}
}
