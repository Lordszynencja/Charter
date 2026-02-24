package log.charter.gui.components.tabs.errorsTab;

import static log.charter.util.Utils.formatTime;

import log.charter.data.ChartData;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.types.PositionType;
import log.charter.gui.CharterFrame;
import log.charter.gui.CharterFrame.TabType;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.util.Utils.TimeUnit;

public class ChartPositionGenerator {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	public ChartPosition position() {
		return new ChartPosition();
	}

	public class ChartPosition {
		public String description;

		private Integer vocalPathId;
		private Integer arrangementId;
		private Integer levelId;
		private PositionType itemType;
		private Integer itemId;
		private Integer templateId;
		private IVirtualConstantPosition time;
		private TabType tab;

		private ChartPosition() {
		}

		private ChartPosition(final ChartPosition other) {
			description = other.description;
			vocalPathId = other.vocalPathId;
			arrangementId = other.arrangementId;
			levelId = other.levelId;
			itemType = other.itemType;
			itemId = other.itemId;
			templateId = other.templateId;
			time = other.time;
			tab = other.tab;
		}

		private ChartPosition item(final PositionType type, final int id) {
			itemType = type;
			itemId = id;
			return this;
		}

		public ChartPosition arrangement(final int arrangementId) {
			this.arrangementId = arrangementId;
			return this;
		}

		public ChartPosition chordTemplate(final int templateId) {
			this.templateId = templateId;
			tab = TabType.CHORD_TEMPLATES;
			return this;
		}

		public ChartPosition tempoMap(final int id) {
			itemType = PositionType.BEAT;
			itemId = id;
			time = chartData.beats().get(id);
			return this;
		}

		public ChartPosition event(final int id) {
			return item(PositionType.EVENT_POINT, id);
		}

		public ChartPosition fhp(final int id) {
			return item(PositionType.FHP, id);
		}

		public ChartPosition handShape(final int id) {
			return item(PositionType.HAND_SHAPE, id);
		}

		public ChartPosition level(final int levelId) {
			this.levelId = levelId;
			return this;
		}

		public ChartPosition sound(final int id) {
			return item(PositionType.GUITAR_NOTE, id);
		}

		public ChartPosition tab(final TabType tab) {
			this.tab = tab;
			return this;
		}

		public ChartPosition time(final IVirtualConstantPosition time) {
			this.time = time;
			return this;
		}

		public ChartPosition toneChange(final int id) {
			return item(PositionType.TONE_CHANGE, id);
		}

		public ChartPosition vocal(final int id) {
			return item(PositionType.VOCAL, id);
		}

		private void setTimeForEvent() {
			if (itemId == null) {
				return;
			}

			time = switch (itemType) {
				case BEAT -> chartData.beats().get(itemId);
				case EVENT_POINT -> chartData.songChart.arrangements.get(arrangementId).eventPoints.get(itemId);
				case FHP -> chartData.songChart.arrangements.get(arrangementId).getLevel(levelId).fhps.get(itemId);
				case HAND_SHAPE ->
					chartData.songChart.arrangements.get(arrangementId).getLevel(levelId).handShapes.get(itemId);
				case GUITAR_NOTE ->
					chartData.songChart.arrangements.get(arrangementId).getLevel(levelId).sounds.get(itemId);
				case TONE_CHANGE -> chartData.songChart.arrangements.get(arrangementId).toneChanges.get(itemId);
				case VOCAL -> chartData.songChart.vocalPaths.get(vocalPathId).vocals.get(itemId);
				default -> null;
			};
		}

		private void addVocalPathName(final StringBuilder description) {
			if (vocalPathId == null) {
				return;
			}

			description.append("[Vocal " + vocalPathId + "]");

			final String pathName = chartData.songChart.vocalPaths.get(vocalPathId).name;
			if (pathName != null && !pathName.isBlank()) {
				description.append(" ").append(pathName);
			}
		}

		private void addArrangementName(final StringBuilder description) {
			if (arrangementId == null) {
				return;
			}

			final String arrangementName = chartData.songChart.arrangements.get(arrangementId)
					.getTypeNameLabel(arrangementId);
			description.append(arrangementName);
		}

		private void addLevelName(final StringBuilder description) {
			if (levelId == null) {
				return;
			}

			description.append(", Level ").append(levelId);
		}

		private void addTime(final StringBuilder description) {
			if (time == null) {
				return;
			}

			final int timePosition = (int) time.toPosition(chartData.beats()).position();
			final String timeString = formatTime(timePosition, TimeUnit.MILISECONDS, TimeUnit.MILISECONDS,
					TimeUnit.HOURS);

			if (arrangementId != null) {
				description.append(": ");
			}
			description.append(timeString);
		}

		private String getPositionDescription() {
			final StringBuilder description = new StringBuilder();
			addVocalPathName(description);
			addArrangementName(description);
			addLevelName(description);
			addTime(description);

			return description.toString();
		}

		public ChartPosition build() {
			setTimeForEvent();

			description = getPositionDescription();

			return this;
		}

		public void goTo() {
			if (arrangementId != null) {
				modeManager.setArrangement(arrangementId);
				if (levelId != null) {
					modeManager.setLevel(levelId);
				}
			}
			if (time != null) {
				chartTimeHandler.nextTime(time);
			}
			if (itemId != null) {
				if (itemType == PositionType.BEAT) {
					modeManager.setMode(EditMode.TEMPO_MAP);
				} else {
					selectionManager.setSelection(itemType, itemId);
				}
			}
			if (tab != null) {
				charterFrame.setTab(tab);
			}
			if (templateId != null) {
				chordTemplatesEditorTab.selectChordTemplate(templateId);
			}
		}

		@Override
		public ChartPosition clone() {
			return new ChartPosition(this);
		}
	}
}
