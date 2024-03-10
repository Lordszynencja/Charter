package log.charter.gui.components.toolbar;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInteger;
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

import log.charter.data.GridType;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.validators.IntegerValueValidator;
import log.charter.gui.lookAndFeel.CharterSliderUI;
import log.charter.io.Logger;
import log.charter.services.AudioHandler;
import log.charter.services.RepeatManager;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.util.ImageUtils;

public class ChartToolbar extends JToolBar implements Initiable {
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

	private AudioHandler audioHandler;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private RepeatManager repeatManager;
	private WaveFormDrawer waveFormDrawer;

	private JToggleButton midi;
	private JToggleButton claps;
	private JToggleButton metronome;
	private JToggleButton waveformGraph;
	private JToggleButton intensityRMSIndicator;
	private JToggleButton repeater;

	private FieldWithLabel<TextInputWithValidation> gridSize;
	private FieldWithLabel<TextInputWithValidation> slowedSpeed;

	private JToggleButton beatGridType;
	private JToggleButton noteGridType;

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
		final JToggleButton toggleButton = addToggleButton(x, horizontalSpacing, label, onClick, buttonWidth);

		return toggleButton;
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

	private void changeSpeed(final int newSpeed) {
		this.newSpeed = newSpeed;
		new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
			}
			if (this.newSpeed != newSpeed) {
				return;
			}

			setSpeed(newSpeed);
		}).start();
	}

	private void setSpeed(final int newSpeed) {
		if (Config.stretchedMusicSpeed == newSpeed) {
			return;
		}

		Config.stretchedMusicSpeed = newSpeed;
		Config.markChanged();

		audioHandler.clear();
		audioHandler.addSpeedToStretch();
	}

	private void addSlowedSpeed(final AtomicInteger x) {
		slowedSpeed = createNumberField(Label.TOOLBAR_SLOWED_PLAYBACK_SPEED, LabelPosition.LEFT_PACKED, 30, //
				Config.stretchedMusicSpeed, 1, 500, false, this::changeSpeed);
		this.add(x, slowedSpeed);

		slowedSpeed.field.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
			}

			@Override
			public void focusLost(final FocusEvent e) {
				setSpeed(newSpeed);
			}

		});
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

	private void changeVolume(final double newVolume) {
		Config.volume = newVolume;
		Config.markChanged();
	}

	private void changeSFXVolume(final double newVolume) {
		Config.sfxVolume = newVolume;
		Config.markChanged();
	}

	private void addVolumeSlider(final AtomicInteger x, final Label label, final BufferedImage icon, final double value,
			final DoubleConsumer volumeSetter) {
		final JSlider volumeSlider = new JSlider(0, 100, getVolumeAsInteger(value));
		volumeSlider.addChangeListener(e -> volumeSetter.accept(volumeSlider.getValue() / 100.0));
		volumeSlider.setSize(72, elementHeight);
		volumeSlider.setFocusable(false);
		volumeSlider.setBackground(getBackground());
		volumeSlider.setUI(new CharterSliderUI());

		final FieldWithLabel<JSlider> volume = new FieldWithLabel<>(label, 0, 72, elementHeight, volumeSlider,
				LabelPosition.LEFT_PACKED);
		setIcon(volume.label, icon);

		add(x, volume);
	}

	@Override
	public void init() {
		final AtomicInteger x = new AtomicInteger(5);

		midi = addToggleButton(x, Label.TOOLBAR_MIDI, audioHandler::toggleMidiNotes);
		claps = addToggleButton(x, Label.TOOLBAR_CLAPS, audioHandler::toggleClaps);
		metronome = addToggleButton(x, Label.TOOLBAR_METRONOME, audioHandler::toggleMetronome);

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

		addVolumeSlider(x, Label.TOOLBAR_VOLUME, volumeIcon, Config.volume, this::changeVolume);
		addVolumeSlider(x, Label.TOOLBAR_SFX_VOLUME, sfxVolumeIcon, Config.sfxVolume, this::changeSFXVolume);
		addSlowedSpeed(x);

		updateValues();
		setSize(getWidth(), height);

		addKeyListener(keyboardHandler);
	}

	public void updateValues() {
		midi.setSelected(audioHandler.midiNotesPlaying);
		midi.setEnabled(modeManager.getMode() != EditMode.TEMPO_MAP);
		claps.setSelected(audioHandler.claps());
		metronome.setSelected(audioHandler.metronome());
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
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
	}
}
