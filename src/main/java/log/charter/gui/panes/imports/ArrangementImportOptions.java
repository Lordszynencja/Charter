package log.charter.gui.panes.imports;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.services.data.fixers.ArrangementFixer;

public class ArrangementImportOptions extends ParamsPane {
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

	private final ArrangementFixer arrangementFixer;
	private final CharterMenuBar charterMenuBar;
	private final ChartData data;
	private final SongChart imported;

	private final ArrangementImportSetting[] arrangementImportSettings;
	private final List<ArrangementImportSetting> arrangementImportSettingsOptions;

	public ArrangementImportOptions(final CharterFrame frame, final ArrangementFixer arrangementFixer,
			final CharterMenuBar charterMenuBar, final ChartData data, final SongChart imported) {
		super(frame, Label.ARRANGEMENT_IMPORT_OPTIONS, 450);

		this.arrangementFixer = arrangementFixer;
		this.charterMenuBar = charterMenuBar;
		this.data = data;
		this.imported = imported;

		int row = 0;

		int arrangementId = 0;
		arrangementImportSettings = new ArrangementImportSetting[imported.arrangements.size()];
		arrangementImportSettingsOptions = prepareArrangementImportSettingsOptions();

		for (final Arrangement arrangement : imported.arrangements) {
			addArrangementOptions(row++, arrangementId++, arrangement);
		}

		row++;
		setOnFinish(this::saveAndExit, null);
		addDefaultFinish(row);
	}

	private List<ArrangementImportSetting> prepareArrangementImportSettingsOptions() {
		final List<ArrangementImportSetting> options = new ArrayList<>();
		options.add(new ArrangementImportSetting(false, Label.ARRANGEMENT_TO_NEW_ARRANGEMENT.label()));
		options.add(new ArrangementImportSetting(true, Label.ARRANGEMENT_SKIP_ARRANGEMENT.label()));
		for (int i = 0; i < data.songChart.arrangements.size(); i++) {
			final String arrangementTypeAndName = data.songChart.arrangements.get(i).getTypeNameLabel();
			final String optionName = Label.ARRANGEMENT_TO_EXISTING_ARRANGEMENT.format(i + 1, arrangementTypeAndName);
			options.add(new ArrangementImportSetting(i, optionName));
		}

		return options;
	}

	private void addArrangementOptions(final int row, final int id, final Arrangement arrangement) {
		final String name = Label.ARRANGEMENT_ID_NAME.label().formatted(id + 1, arrangement.getTypeNameLabel());

		addLabel(row, 10, name, 0);
		arrangementImportSettings[id] = arrangementImportSettingsOptions.get(0);

		final CharterSelect<ArrangementImportSetting> themeSelect = new CharterSelect<>(
				arrangementImportSettingsOptions, null, v -> v.name, v -> arrangementImportSettings[id] = v);

		this.add(themeSelect, 200, getY(row), 200, 20);
	}

	private void updateSongInformation() {
		if (data.songChart.artistName() == null || data.songChart.artistName().isBlank()) {
			data.songChart.artistName(imported.artistName());
		}
		if (data.songChart.title() == null || data.songChart.title().isBlank()) {
			data.songChart.title(imported.title());
		}
		if (data.songChart.albumName() == null || data.songChart.albumName().isBlank()) {
			data.songChart.albumName(imported.albumName());
		}
	}

	private void saveAndExit() {
		updateSongInformation();

		data.songChart.beatsMap = imported.beatsMap;

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
