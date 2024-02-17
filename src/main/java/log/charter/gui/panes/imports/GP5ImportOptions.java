package log.charter.gui.panes.imports;

import static java.util.Arrays.asList;

import java.util.Vector;

import javax.swing.JComboBox;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.song.Arrangement;
import log.charter.song.SongChart;

public class GP5ImportOptions extends ParamsPane {
	private static class ArrangementImportSetting {
		public final boolean skip;
		public final Integer arrangementId;
		public final String name;

		public ArrangementImportSetting(final boolean skip, final String name) {
			this.skip = skip;
			arrangementId = null;
			this.name = name;
		}

		public ArrangementImportSetting(final Integer arrangementId, final String name) {
			skip = false;
			this.arrangementId = arrangementId;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.width = 450;

		return sizes;
	}

	private final ArrangementFixer arrangementFixer;
	private final CharterMenuBar charterMenuBar;
	private final ChartData data;
	private final SongChart imported;

	private boolean importBeatMap = false;
	private final ArrangementImportSetting[] arrangementImportSettings;

	public GP5ImportOptions(final CharterFrame frame, final ArrangementFixer arrangementFixer,
			final CharterMenuBar charterMenuBar, final ChartData data, final SongChart imported) {
		super(frame, Label.GP5_IMPORT, getSizes());

		this.arrangementFixer = arrangementFixer;
		this.charterMenuBar = charterMenuBar;
		this.data = data;
		this.imported = imported;

		int row = 0;

		addBeatMapImportCheckbox(row++);
		int arrangementId = 0;
		arrangementImportSettings = new ArrangementImportSetting[imported.arrangements.size()];
		for (final Arrangement arrangement : imported.arrangements) {
			addArrangementOptions(row++, arrangementId++, arrangement);
		}

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void addBeatMapImportCheckbox(final int row) {
		addConfigCheckbox(row, 10, 110, Label.GP5_IMPORT_BEAT_MAP_CHANGE, importBeatMap, b -> importBeatMap = b);
	}

	private void addArrangementOptions(final int row, final int id, final Arrangement arrangement) {
		final String name = Label.GP5_IMPORT_ARRANGEMENT_NAME.label().formatted(id + 1, arrangement.getTypeNameLabel());

		addLabel(row, 10, name);
		final Vector<ArrangementImportSetting> options = new Vector<>(asList(//
				new ArrangementImportSetting(false, Label.GP5_IMPORT_TO_NEW_ARRANGEMENT.label()), //
				new ArrangementImportSetting(true, Label.GP5_IMPORT_SKIP_ARRANGEMENT.label())));
		for (int i = 0; i < data.songChart.arrangements.size(); i++) {
			final String optionName = Label.GP5_IMPORT_TO_EXISTING_ARRANGEMENT.label().formatted(i + 1,
					data.songChart.arrangements.get(i).getTypeNameLabel());
			options.add(new ArrangementImportSetting(i, optionName));
		}
		arrangementImportSettings[id] = options.get(0);

		final JComboBox<ArrangementImportSetting> themeSelect = new JComboBox<>(options);
		themeSelect.setSelectedIndex(0);
		themeSelect.addActionListener(
				e -> arrangementImportSettings[id] = ((ArrangementImportSetting) themeSelect.getSelectedItem()));
		this.add(themeSelect, 200, getY(row), 200, 20);
	}

	private void saveAndExit() {
		if (importBeatMap) {
			data.songChart.beatsMap = imported.beatsMap;
		}

		for (int i = 0; i < arrangementImportSettings.length; i++) {
			final ArrangementImportSetting setting = arrangementImportSettings[i];
			if (setting.skip) {
				continue;
			}

			final Arrangement arrangementToAdd = imported.arrangements.get(i);
			if (setting.arrangementId != null) {
				data.songChart.arrangements.set(setting.arrangementId, arrangementToAdd);
			} else {
				data.songChart.arrangements.add(arrangementToAdd);
			}
		}

		arrangementFixer.fixArrangements();

		charterMenuBar.refreshMenus();
	}
}
