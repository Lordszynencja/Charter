package log.charter.gui.components.toolbar;

import static java.lang.Math.abs;
import static log.charter.gui.components.utils.ComponentUtils.setIcon;
import static log.charter.gui.components.utils.validators.ValueValidator.notBlankValidator;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSlider;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Stem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.gui.lookAndFeel.CharterSliderUI;
import log.charter.gui.lookAndFeel.IconMaker;
import log.charter.services.data.ProjectAudioHandler;

public class AudioStemsSettings extends RowedDialog {
	private static final BufferedImage deleteIcon = IconMaker.createIcon(17, 17, //
			new int[][] { //
					{ 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0 }, //
					{ 0, 1, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 1, 0 }, //
					{ 1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 1 }, //
					{ 1, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 2, 2, 1 }, //
					{ 0, 1, 2, 2, 2, 2, 2, 1, 0, 1, 2, 2, 2, 2, 2, 1, 0 }, //
					{ 0, 0, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 0, 0 }, //
					{ 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0 }, //
					{ 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0 }, //
					{ 0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0 }, //
					{ 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0 }, //
					{ 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0 }, //
					{ 0, 0, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 0, 0 }, //
					{ 0, 1, 2, 2, 2, 2, 2, 1, 0, 1, 2, 2, 2, 2, 2, 1, 0 }, //
					{ 1, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 2, 2, 1 }, //
					{ 1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 1 }, //
					{ 0, 1, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 1, 0 }, //
					{ 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0 } },
			new Color[] { //
					new Color(0, 0, 0, 0), //
					new Color(64, 0, 0, 255), //
					new Color(255, 0, 0, 255) });

	private static final long serialVersionUID = -2133758312693862190L;

	private class AudioStemRow {
		private final int id;

		private String name;
		private double volume;
		private double offset;
		private boolean deleted;

		private JCheckBox selected;
		private TextInputWithValidation label;
		private FieldWithLabel<TextInputWithValidation> offsetField;
		private JSlider volumeSlider;

		public AudioStemRow(final RowedPosition position) {
			this(-1, Label.MAIN_AUDIO.label(), Config.audio.volume, 0, position);
		}

		public AudioStemRow(final int id, final Stem stem, final RowedPosition position) {
			this(id, stem.name, stem.volume, stem.offset, position);
		}

		private AudioStemRow(final int id, final String name, final double volume, final double offset,
				final RowedPosition position) {
			this.id = id;

			this.name = name;
			this.volume = volume;
			this.offset = offset;

			addCheckBox(position);
			addName(position);
			addVolume(position);
			if (id >= 0) {
				addOffset(position);
				addDelete(position);
			}
		}

		private void addCheckBox(final RowedPosition position) {
			selected = new JCheckBox();
			selected.setSelected(selectedStem == id);
			selected.addActionListener(e -> {
				selectedStem = id;

				for (int i = 0; i < stemRows.size(); i++) {
					stemRows.get(i).selected.setSelected((i - 1) == id);
				}
			});

			panel.addWithSettingSize(selected, position, 20, 10, 20);
		}

		private void addName(final RowedPosition position) {
			label = new TextInputWithValidation(name, 100, notBlankValidator(Label.NAME_CANT_BE_EMPTY), v -> name = v,
					false);
			if (id == -1) {
				label.setEditable(false);
			}

			panel.addWithSettingSize(label, position, 100, 10, 20);
		}

		private void addVolume(final RowedPosition position) {
			final int sliderPosition = volume <= 0 ? 0//
					: volume >= 1 ? 100//
							: (int) (volume * 100);

			volumeSlider = new JSlider(0, 100, sliderPosition);
			volumeSlider.addChangeListener(e -> volume = volumeSlider.getValue() / 100.0);
			volumeSlider.setFocusable(false);
			volumeSlider.setBackground(getBackground());

			panel.addWithSettingSize(volumeSlider, position, 100, 10, 20);
			volumeSlider.setUI(new CharterSliderUI());
		}

		private void addOffset(final RowedPosition position) {
			final BigDecimal value = BigDecimal.valueOf(offset * 1000).setScale(2, RoundingMode.HALF_UP);
			final BigDecimalValueValidator validator = new BigDecimalValueValidator(
					BigDecimal.valueOf(-projectAudioHandler.audioStemLength(id)),
					BigDecimal.valueOf(projectAudioHandler.audioLengthMs()), false);
			final TextInputWithValidation input = TextInputWithValidation.generateForBigDecimal(value, 50, validator,
					v -> offset = v.doubleValue() / 1000, false);

			offsetField = new FieldWithLabel<>(Label.OFFSET_MS_FIELD, 80, 50, 20, input, LabelPosition.LEFT_CLOSE);

			panel.add(offsetField, position);
		}

		@SuppressWarnings("unchecked")
		private void addDelete(final RowedPosition position) {
			final JButton deleteButton = new JButton();
			setIcon(deleteButton, deleteIcon);
			deleteButton.addActionListener(e -> {
				panel.remove(selected);
				panel.remove(offsetField);
				panel.remove(volumeSlider);
				panel.remove(deleteButton);

				final Map<TextAttribute, Object> fontAttributes = (Map<TextAttribute, Object>) label.getFont()
						.getAttributes();
				fontAttributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
				label.setFont(getFont().deriveFont(fontAttributes));
				label.setEnabled(false);
				label.setEditable(false);
				panel.repaint();

				deleted = true;
			});

			panel.addWithSettingSize(deleteButton, position, 20, 20, 20);
		}
	}

	private final ChartData chartData;
	private final ChartToolbar chartToolbar;
	private final ProjectAudioHandler projectAudioHandler;

	private final List<AudioStemRow> stemRows = new ArrayList<>();

	private int selectedStem = -1;

	public AudioStemsSettings(final ChartData chartData, final CharterFrame charterFrame,
			final ChartToolbar chartToolbar, final ProjectAudioHandler projectAudioHandler) {
		super(charterFrame, Label.AUDIO_STEM_SETTINGS, 0);

		this.chartData = chartData;
		this.chartToolbar = chartToolbar;
		this.projectAudioHandler = projectAudioHandler;

		selectedStem = projectAudioHandler.getSelectedStem();

		final RowedPosition position = new RowedPosition(20, panel.sizes);
		stemRows.add(new AudioStemRow(position));
		position.newRow();

		final List<Stem> stems = chartData.songChart.stems;
		for (int i = 0; i < stems.size(); i++) {
			stemRows.add(new AudioStemRow(i, stems.get(i), position));
			position.newRow();
		}

		addDefaultFinish(position.newRow().y(), SaverWithStatus.defaultFor(this::save), null, true);
	}

	private void save() {
		projectAudioHandler.selectStem(selectedStem);

		int deleted = 0;
		for (final AudioStemRow stemRow : stemRows) {
			final int id = stemRow.id - deleted;
			if (stemRow.deleted) {
				projectAudioHandler.removeStem(id);
				deleted++;
			} else {
				if (stemRow.id < 0) {
					Config.audio.volume = stemRow.volume;
				} else {
					final Stem stem = chartData.songChart.stems.get(id);
					stem.volume = stemRow.volume;
					stem.name = stemRow.name;
					if (abs(stemRow.offset - stem.offset) > 0.001) {
						projectAudioHandler.addStemOffset(id, stemRow.offset - stem.offset);
					}
				}
			}
		}

		chartToolbar.updateValues();
	}
}
