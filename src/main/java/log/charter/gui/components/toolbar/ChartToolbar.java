package log.charter.gui.components.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleConsumer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import log.charter.data.GridType;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.RepeatManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.gui.components.TextInputWithValidation.IntegerValueSetter;
import log.charter.gui.components.TextInputWithValidation.IntegerValueValidator;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
//import log.charter.gui.lookAndFeel.CharterButtonUI;
import log.charter.gui.lookAndFeel.CharterSliderUI;
import log.charter.io.Logger;

public class ChartToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;

	private static final int checkboxLabelSpacing = 1;
	private static final int verticalSpacing = 8;
	private static final int elementHeight = 20;
	public static final int height = elementHeight + 2 * verticalSpacing;

	private static final int horizontalSpacing = 5;

	private AudioHandler audioHandler;
	private ModeManager modeManager;
	private RepeatManager repeatManager;
	private WaveFormDrawer waveFormDrawer;

	private JToggleButton midi;
	private JToggleButton claps;
	private JToggleButton metronome;
	private FieldWithLabel<JCheckBox> waveformGraph;
	private FieldWithLabel<JCheckBox> intensityRMSIndicator;
	private JToggleButton repeater;

	private FieldWithLabel<TextInputWithValidation> gridSize;
	private FieldWithLabel<TextInputWithValidation> slowedSpeed;

	private FieldWithLabel<JRadioButton> beatGridType;
	private FieldWithLabel<JRadioButton> noteGridType;

	private int newSpeed = Config.stretchedMusicSpeed;

	public ChartToolbar() {
		super();

		setLayout(null);
		setSize(getWidth(), height);

		setFocusable(true);
		setFloatable(false);
		setBackground(ColorLabel.BASE_BG_2.color()); // changed
	}

	private void setComponentBounds(final Component c, final int x, final int y, final int w, final int h) {
		final Dimension newSize = new Dimension(w, h);

		c.setMinimumSize(newSize);
		c.setPreferredSize(newSize);
		c.setMaximumSize(newSize);
		c.setBounds(x, y, w, h);
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

	private FieldWithLabel<JCheckBox> createCheckboxField(final Label label, final int separationWidth,
			final Runnable onClick) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.addActionListener(a -> onClick.run());
		checkbox.setBackground(getBackground());
		checkbox.setFocusable(false);

		final FieldWithLabel<JCheckBox> field = new FieldWithLabel<>(label, separationWidth, elementHeight,
				elementHeight, checkbox, LabelPosition.RIGHT_PACKED);
		field.setBackground(getBackground());

		return field;
	}

	private FieldWithLabel<JCheckBox> addCheckbox(final AtomicInteger x, final Label label, final Runnable onClick) {
		final FieldWithLabel<JCheckBox> field = createCheckboxField(label, checkboxLabelSpacing, onClick);
		add(x, field);

		return field;
	}

	private JToggleButton addToggleButton(final AtomicInteger x, final Label label, final Runnable onClick) {
		final JToggleButton toggleButton = new JToggleButton(label.label());
		toggleButton.addActionListener(a -> onClick.run());
		toggleButton.setFocusable(false);

		toggleButton.setBounds(x.get(), 0, toggleButton.getPreferredSize().width, elementHeight);
		add(x, toggleButton);

		return toggleButton;
	}

	private FieldWithLabel<JRadioButton> createRadioButtonField(final Label label, final int separationWidth,
			final Runnable onClick) {
		final JRadioButton radioButton = new JRadioButton();
		radioButton.addActionListener(a -> onClick.run());
		radioButton.setBackground(getBackground());
		radioButton.setFocusable(false);

		final FieldWithLabel<JRadioButton> field = new FieldWithLabel<>(label, separationWidth, elementHeight,
				elementHeight, radioButton, LabelPosition.RIGHT_PACKED);
		field.setBackground(getBackground());

		return field;
	}

	private FieldWithLabel<JRadioButton> addRadioButton(final AtomicInteger x, final Label label,
			final Runnable onClick) {
		final FieldWithLabel<JRadioButton> field = createRadioButtonField(label, 2, onClick);
		add(x, field);

		return field;
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
			final IntegerValueSetter onChange) {
		final TextInputWithValidation input = new TextInputWithValidation(value, inputWidth,
				new IntegerValueValidator(min, max, allowEmpty), onChange, false);

		final FieldWithLabel<TextInputWithValidation> field = //
				new FieldWithLabel<>(label, 0, inputWidth, elementHeight, input, labelPosition);
		field.setBackground(getBackground());

		return field;
	}

	private void addGridSizeInput(final AtomicInteger x) {
		gridSize = createNumberField(Label.GRID_SIZE, LabelPosition.LEFT_PACKED, 25, //
				Config.gridSize, 1, 128, false, newGridSize -> {
					Config.gridSize = newGridSize;
					Config.markChanged();
				});
		add(x, 1, gridSize);
	}

	private void addGridSizeButton(final AtomicInteger x, final int horizontalSpacing, final Font font,
			final String text, final ActionListener actionListener) {
		final JButton halveGridButton = new JButton(text);
		// halveGridButton.setUI(new CharterButtonUI());
		halveGridButton.setSize(24, elementHeight);
		halveGridButton.setFont(font);
		halveGridButton.setFocusable(false);
		halveGridButton.addActionListener(actionListener);
		add(x, horizontalSpacing, halveGridButton);
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
		final Font miniFont = new Font(Font.DIALOG, Font.PLAIN, 13);
		addGridSizeButton(x, 1, miniFont, "-", a -> halveGridSize());
		addGridSizeButton(x, horizontalSpacing, miniFont, "+", a -> doubleGridSize());
	}

	private void onGridTypeChange(final GridType newGridType) {
		Config.gridType = newGridType;
		Config.markChanged();
	}

	private void addGridTypes(final AtomicInteger x) {
		beatGridType = addRadioButton(x, Label.BEAT_GRID_TYPE, () -> onGridTypeChange(GridType.BEAT));
		noteGridType = addRadioButton(x, Label.NOTE_GRID_TYPE, () -> onGridTypeChange(GridType.NOTE));

		final ButtonGroup gridTypeGroup = new ButtonGroup();
		gridTypeGroup.add(beatGridType.field);
		gridTypeGroup.add(noteGridType.field);
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

	private void addVolumeSlider(final AtomicInteger x, final Label label, final double value,
			final DoubleConsumer volumeSetter) {
		final JSlider volumeSlider = new JSlider(0, 100, getVolumeAsInteger(value));
		volumeSlider.addChangeListener(e -> volumeSetter.accept(volumeSlider.getValue() / 100.0));
		volumeSlider.setSize(72, elementHeight);
		volumeSlider.setFocusable(false);
		volumeSlider.setBackground(getBackground());
		volumeSlider.setUI(new CharterSliderUI());

		final FieldWithLabel<JSlider> volume = new FieldWithLabel<>(label, 0, 72, elementHeight, volumeSlider,
				LabelPosition.LEFT_PACKED);
		add(x, volume);
	}

	public void init(final AudioHandler audioHandler, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final RepeatManager repeatManager, final WaveFormDrawer waveFormDrawer) {
		this.audioHandler = audioHandler;
		this.modeManager = modeManager;
		this.repeatManager = repeatManager;
		this.waveFormDrawer = waveFormDrawer;

		final AtomicInteger x = new AtomicInteger(5);

		midi = addToggleButton(x, Label.TOOLBAR_MIDI, audioHandler::toggleMidiNotes);
		claps = addToggleButton(x, Label.TOOLBAR_CLAPS, audioHandler::toggleClaps);
		metronome = addToggleButton(x, Label.TOOLBAR_METRONOME, audioHandler::toggleMetronome);

		addSeparator(x);

		waveformGraph = addCheckbox(x, Label.TOOLBAR_WAVEFORM_GRAPH, waveFormDrawer::toggle);
		intensityRMSIndicator = addCheckbox(x, Label.RMS_INDICATOR, waveFormDrawer::toggleRMS);
		intensityRMSIndicator.field.setEnabled(false);

		addSeparator(x);

		repeater = addToggleButton(x, Label.TOOLBAR_REPEATER, repeatManager::toggle);

		addSeparator(x);

		addGridSizeInput(x);
		addGridSizeButtons(x);
		addGridTypes(x);

		addSeparator(x);

		addVolumeSlider(x, Label.TOOLBAR_VOLUME, Config.volume, this::changeVolume);
		addVolumeSlider(x, Label.TOOLBAR_SFX_VOLUME, Config.sfxVolume, this::changeSFXVolume);
		addSlowedSpeed(x);

		updateValues();

		addKeyListener(keyboardHandler);
	}

	public void updateValues() {
		midi.setSelected(audioHandler.midiNotesPlaying);
		claps.setSelected(audioHandler.claps());
		metronome.setSelected(audioHandler.metronome());
		waveformGraph.field.setSelected(waveFormDrawer.drawing());
		waveformGraph.field.setEnabled(modeManager.getMode() != EditMode.TEMPO_MAP);
		intensityRMSIndicator.field.setEnabled(waveFormDrawer.drawing());
		intensityRMSIndicator.field.setSelected(waveFormDrawer.rms());
		repeater.setSelected(repeatManager.isOn());

		gridSize.field.setTextWithoutEvent(Config.gridSize + "");
		switch (Config.gridType) {
			case BEAT:
				beatGridType.field.setSelected(true);
				break;
			case NOTE:
				noteGridType.field.setSelected(true);
				break;
			case MEASURE:
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
