package log.charter.gui.panes.imports;

import javax.swing.JCheckBox;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.vocals.VocalPath;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.midi.MidiReader.MidiFileData;
import log.charter.services.data.files.SongFilesBackuper;

public class MidiImportPane extends RowedDialog {
	private static final long serialVersionUID = 1L;

	private final ChartData chartData;
	private final CharterMenuBar charterMenuBar;
	private final SongFilesBackuper songFilesBackuper;

	private final MidiFileData midiFileData;

	private boolean importBeatMap = true;
	private boolean importVocals = true;

	public MidiImportPane(final ChartData chartData, final CharterFrame frame, final CharterMenuBar charterMenuBar,
			final SongFilesBackuper songFilesBackuper, final MidiFileData midiFileData) {
		super(frame, Label.MIDI_IMPORT_PANE, 200);

		this.chartData = chartData;
		this.charterMenuBar = charterMenuBar;
		this.songFilesBackuper = songFilesBackuper;

		this.midiFileData = midiFileData;

		final RowedPosition position = new RowedPosition(50, panel.sizes);

		if (midiFileData.beats != null) {
			addImportTempoMapCheckbox(position);
			position.newRow();
		} else {
			importBeatMap = false;
		}

		if (midiFileData.vocals != null) {
			addImportVocalsCheckbox(position);
			position.newRow();
		} else {
			importVocals = false;
		}

		position.newRow();

		addDefaultFinish(position.y(), SaverWithStatus.defaultFor(this::onSave), null, true);
	}

	private void addImportTempoMapCheckbox(final RowedPosition position) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(importBeatMap);
		checkbox.addActionListener(a -> importBeatMap = checkbox.isSelected());

		final FieldWithLabel<JCheckBox> field = new FieldWithLabel<>(Label.MIDI_IMPORT_TEMPO_MAP, 100, 20, 20, checkbox,
				LabelPosition.LEFT);

		panel.add(field, position);
	}

	private void addImportVocalsCheckbox(final RowedPosition position) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(importVocals);
		checkbox.addActionListener(a -> importVocals = checkbox.isSelected());

		final FieldWithLabel<JCheckBox> field = new FieldWithLabel<>(Label.MIDI_IMPORT_VOCALS, 100, 20, 20, checkbox,
				LabelPosition.LEFT);

		panel.add(field, position);
	}

	private void onSave() {
		if (!importBeatMap && !importVocals) {
			return;
		}

		songFilesBackuper.makeDefaultBackups();

		if (importBeatMap) {
			chartData.songChart.beatsMap = midiFileData.beats;
		}
		if (importVocals) {
			chartData.addVocals(new VocalPath(midiFileData.vocals));
			charterMenuBar.refreshMenus();
		}
	}

}
