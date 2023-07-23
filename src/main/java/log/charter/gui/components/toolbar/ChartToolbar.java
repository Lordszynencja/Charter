package log.charter.gui.components.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.gui.components.TextInputWithValidation.IntegerValueSetter;
import log.charter.gui.components.TextInputWithValidation.IntegerValueValidator;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.lookAndFeel.CharterSliderUI;

public class ChartToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;

	private AudioDrawer audioDrawer;
	private AudioHandler audioHandler;

	private FieldWithLabel<JCheckBox> midi;
	private FieldWithLabel<JCheckBox> claps;
	private FieldWithLabel<JCheckBox> metronome;
	private FieldWithLabel<JCheckBox> waveformGraph;

	private FieldWithLabel<TextInputWithValidation> gridSize;

	public ChartToolbar() {
		super();
		setLayout(null);

		setFocusable(true);
		setFloatable(false);
		setBackground(ColorLabel.BASE_BG_3.color());
	}

	public void init(final AudioDrawer audioDrawer, final AudioHandler audioHandler,
			final KeyboardHandler keyboardHandler) {
		this.audioDrawer = audioDrawer;
		this.audioHandler = audioHandler;

		final AtomicInteger x = new AtomicInteger(0);

		addMidiClapsMetronomeWaveformGraph(x);

		x.addAndGet(5);
		addSeparator(x);

		x.addAndGet(5);
		addGridOptions(x);

		x.addAndGet(5);
		addSeparator(x);

		x.addAndGet(5);
		addPlaybackOptions(x);

		updateValues();

		addKeyListener(keyboardHandler);
	}

	private void addSeparator(final AtomicInteger x) {
		final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
		separator.setSize(2, 20);
		add(x, separator);
	}

	private void add(final AtomicInteger x, final Component c) {
		setComponentBounds(c, x.getAndAdd(c.getWidth()), 0, c.getWidth(), c.getHeight());
		add(c);
	}

	private void addMidiClapsMetronomeWaveformGraph(final AtomicInteger x) {
		midi = createCheckboxField(Label.TOOLBAR_MIDI, 2, audioHandler::toggleMidiNotes);
		add(x, midi);
		x.addAndGet(5);

		claps = createCheckboxField(Label.TOOLBAR_CLAPS, 2, audioHandler::toggleClaps);
		add(x, claps);
		x.addAndGet(5);

		metronome = createCheckboxField(Label.TOOLBAR_METRONOME, 2, audioHandler::toggleMetronome);
		this.add(x, metronome);
		x.addAndGet(5);

		waveformGraph = createCheckboxField(Label.TOOLBAR_WAVEFORM_GRAPH, 2, audioDrawer::toggle);
		this.add(x, waveformGraph);
	}

	private void addGridOptions(final AtomicInteger x) {
		gridSize = createNumberField(Label.GRID_PANE_GRID_SIZE, LabelPosition.LEFT_PACKED, 0, //
				Config.gridSize, 1, 1024, false, newGridSize -> Config.gridSize = newGridSize);
		add(x, gridSize);
		x.addAndGet(5);

		// TODO add grid type select
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

	private void addPlaybackOptions(final AtomicInteger x) {

		// TODO add current playback speed

		final JSlider volumeSlider = new JSlider(0, 100, getVolumeAsInteger(Config.volume));
		volumeSlider.addChangeListener(e -> Config.volume = volumeSlider.getValue() / 100.0);
		volumeSlider.setFocusable(false);
		volumeSlider.setBackground(getBackground());
		volumeSlider.setUI(new CharterSliderUI());

		final FieldWithLabel<JSlider> volume = new FieldWithLabel<JSlider>("Volume", 0, 101, 20, volumeSlider,
				LabelPosition.LEFT_PACKED);
		add(x, volume);
		x.addAndGet(5);

		final JSlider midiVolumeSlider = new JSlider(0, 100, getVolumeAsInteger(Config.midiVolume));
		midiVolumeSlider.addChangeListener(e -> Config.midiVolume = midiVolumeSlider.getValue() / 100.0);
		midiVolumeSlider.setFocusable(false);
		midiVolumeSlider.setBackground(getBackground());
		midiVolumeSlider.setUI(new CharterSliderUI());

		final FieldWithLabel<JSlider> midiVolume = new FieldWithLabel<JSlider>("Midi volume", 0, 101, 20,
				midiVolumeSlider, LabelPosition.LEFT_PACKED);
		add(x, midiVolume);
		x.addAndGet(5);
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

	private FieldWithLabel<JCheckBox> createCheckboxField(final Label label, final int labelWidth,
			final Runnable onClick) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.addActionListener(a -> onClick.run());
		checkbox.setBackground(getBackground());
		checkbox.setFocusable(false);

		final FieldWithLabel<JCheckBox> field = new FieldWithLabel<>(label, //
				labelWidth, 20, 20, checkbox, LabelPosition.RIGHT_PACKED);
		field.setBackground(getBackground());

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> createNumberField(final Label label,
			final LabelPosition labelPosition, final int labelWidth, //
			final Integer value, final int min, final int max, final boolean allowEmpty,
			final IntegerValueSetter onChange) {

		final TextInputWithValidation input = new TextInputWithValidation(value, 30,
				new IntegerValueValidator(min, max, allowEmpty), onChange, false);

		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(label, //
				labelWidth, 20, 20, input, labelPosition);
		field.setBackground(getBackground());

		return field;
	}

	public void updateValues() {
		midi.field.setSelected(audioHandler.midiNotesPlaying);
		claps.field.setSelected(audioHandler.claps());
		metronome.field.setSelected(audioHandler.metronome());
		waveformGraph.field.setSelected(audioDrawer.drawing());

		gridSize.field.setTextWithoutEvent(Config.gridSize + "");
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
	}
}
