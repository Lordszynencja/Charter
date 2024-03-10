package log.charter.services.data.copy;

import static java.util.stream.Collectors.toCollection;
import static log.charter.data.song.position.IConstantPosition.findFirstIdAfterEqual;
import static log.charter.data.song.position.IConstantPosition.findLastIdBeforeEqual;

import java.util.function.Function;

import com.thoughtworks.xstream.io.StreamException;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Phrase;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.IPosition;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.GuitarSpecialPastePane;
import log.charter.io.ClipboardHandler;
import log.charter.io.Logger;
import log.charter.io.rsc.xml.ChartProjectXStreamHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.copy.data.AnchorsCopyData;
import log.charter.services.data.copy.data.CopyData;
import log.charter.services.data.copy.data.EventPointsCopyData;
import log.charter.services.data.copy.data.FullCopyData;
import log.charter.services.data.copy.data.FullGuitarCopyData;
import log.charter.services.data.copy.data.HandShapesCopyData;
import log.charter.services.data.copy.data.ICopyData;
import log.charter.services.data.copy.data.SoundsCopyData;
import log.charter.services.data.copy.data.VocalsCopyData;
import log.charter.services.data.copy.data.positions.CopiedAnchorPosition;
import log.charter.services.data.copy.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.services.data.copy.data.positions.CopiedHandShapePosition;
import log.charter.services.data.copy.data.positions.CopiedPosition;
import log.charter.services.data.copy.data.positions.CopiedSoundPosition;
import log.charter.services.data.copy.data.positions.CopiedToneChangePosition;
import log.charter.services.data.copy.data.positions.CopiedVocalPosition;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class CopyManager {
	private static interface CopiedPositionMaker<T extends IPosition, V extends CopiedPosition<T>> {
		V make(BeatsMap beatsMap, int basePosition, double basePositionInBeats, T position);
	}

	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private <T extends IPosition, V extends CopiedPosition<T>> ArrayList2<V> makeCopy(final ArrayList2<T> positions,
			final int basePosition, final double basePositionInBeats,
			final CopiedPositionMaker<T, V> copiedPositionMaker) {
		final BeatsMap beatsMap = chartData.songChart.beatsMap;

		return positions
				.map(position -> copiedPositionMaker.make(beatsMap, basePosition, basePositionInBeats, position));
	}

	private <T extends IPosition, V extends CopiedPosition<T>> ArrayList2<V> makeCopy(
			final ArrayList2<Selection<T>> selectedPositions, final CopiedPositionMaker<T, V> copiedPositionMaker) {
		final BeatsMap beatsMap = chartData.songChart.beatsMap;
		final int basePosition = selectedPositions.get(0).selectable.position();
		final double basePositionInBeats = beatsMap.getPositionInBeats(basePosition);

		return makeCopy(selectedPositions.map(selected -> selected.selectable), basePosition, basePositionInBeats,
				copiedPositionMaker);
	}

	private <T extends IPosition, V extends CopiedPosition<T>> ArrayList2<V> copyPositionsFromTo(final int from,
			final int to, final double basePositionInBeats, final ArrayList2<T> positions,
			final CopiedPositionMaker<T, V> copiedPositionMaker) {
		final BeatsMap beatsMap = chartData.songChart.beatsMap;

		int fromId = findFirstIdAfterEqual(positions, from);
		if (fromId == -1) {
			fromId = positions.size();
		}
		int toId = findLastIdBeforeEqual(positions, to);
		if (toId == -1) {
			toId = -1;
		}
		if (fromId > toId) {
			return new ArrayList2<>();
		}

		return positions.stream().skip(fromId).limit(toId - fromId + 1)//
				.map(position -> copiedPositionMaker.make(beatsMap, from, basePositionInBeats, position))//
				.collect(toCollection(ArrayList2::new));
	}

	private FullCopyData getFullCopyData(final int from, final int to) {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return null;
		}

		final Arrangement arrangement = chartData.getCurrentArrangement();
		final BeatsMap beatsMap = chartData.songChart.beatsMap;
		final double basePositionInBeats = beatsMap.getPositionInBeats(from);

		final HashMap2<String, Phrase> copiedPhrases = arrangement.phrases.map(phraseName -> phraseName, Phrase::new);
		final ArrayList2<CopiedArrangementEventsPointPosition> copiedArrangementEventsPoints = copyPositionsFromTo(from,
				to, basePositionInBeats, arrangement.eventPoints, CopiedArrangementEventsPointPosition::new);
		final ArrayList2<ChordTemplate> copiedChordTemplates = chartData.getCurrentArrangement().chordTemplates
				.map(ChordTemplate::new);
		final ArrayList2<CopiedToneChangePosition> copiedToneChanges = copyPositionsFromTo(from, to,
				basePositionInBeats, arrangement.toneChanges, CopiedToneChangePosition::new);
		final ArrayList2<CopiedAnchorPosition> copiedAnchors = copyPositionsFromTo(from, to, basePositionInBeats,
				chartData.getCurrentArrangementLevel().anchors, CopiedAnchorPosition::new);
		final ArrayList2<CopiedSoundPosition> copiedSounds = copyPositionsFromTo(from, to, basePositionInBeats,
				chartData.getCurrentArrangementLevel().sounds, CopiedSoundPosition::new);
		final ArrayList2<CopiedHandShapePosition> copiedHandShapes = copyPositionsFromTo(from, to, basePositionInBeats,
				chartData.getCurrentArrangementLevel().handShapes, CopiedHandShapePosition::new);

		return new FullGuitarCopyData(copiedPhrases, copiedArrangementEventsPoints, copiedChordTemplates,
				copiedToneChanges, copiedAnchors, copiedSounds, copiedHandShapes);
	}

	private CopyData getGuitarCopyDataEventPoints() {
		final SelectionAccessor<Beat> selectedBeatsAccessor = selectionManager
				.getSelectedAccessor(PositionType.EVENT_POINT);

		final ArrayList2<Selection<Beat>> selectedBeats = selectedBeatsAccessor.getSortedSelected();
		final int from = selectedBeats.get(0).selectable.position();
		final int to = selectedBeats.getLast().selectable.position();
		final double basePositionInBeats = chartData.songChart.beatsMap.getPositionInBeats(from);
		final Arrangement arrangement = chartData.getCurrentArrangement();

		final HashMap2<String, Phrase> copiedPhrases = arrangement.phrases.map(phraseName -> phraseName, Phrase::new);
		final ArrayList2<CopiedArrangementEventsPointPosition> copiedArrangementEventsPoints = copyPositionsFromTo(from,
				to, basePositionInBeats, arrangement.eventPoints, CopiedArrangementEventsPointPosition::new);

		final ICopyData copyData = new EventPointsCopyData(copiedPhrases, copiedArrangementEventsPoints);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private CopyData getGuitarCopyDataGuitarNotes() {
		final SelectionAccessor<ChordOrNote> selectedSoundsAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectedSoundsAccessor.getSortedSelected();

		final ArrayList2<ChordTemplate> copiedChordTemplates = chartData.getCurrentArrangement().chordTemplates
				.map(ChordTemplate::new);
		final ArrayList2<CopiedSoundPosition> copiedSounds = makeCopy(selectedSounds, CopiedSoundPosition::new);
		final int from = selectedSounds.get(0).selectable.position();
		final int to = selectedSounds.getLast().selectable.position();

		final ICopyData copyData = new SoundsCopyData(copiedChordTemplates, copiedSounds);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private CopyData getGuitarCopyDataHandShapes() {
		final SelectionAccessor<HandShape> selectedHandShapesAccessor = selectionManager
				.getSelectedAccessor(PositionType.HAND_SHAPE);
		final ArrayList2<Selection<HandShape>> selectedHandShapes = selectedHandShapesAccessor.getSortedSelected();

		final ArrayList2<ChordTemplate> copiedChordTemplates = chartData.getCurrentArrangement().chordTemplates
				.map(ChordTemplate::new);
		final ArrayList2<CopiedHandShapePosition> copiedHandShapes = makeCopy(selectedHandShapes,
				CopiedHandShapePosition::new);
		final int from = selectedHandShapes.get(0).selectable.position();
		final int to = selectedHandShapes.getLast().selectable.position();

		final ICopyData copyData = new HandShapesCopyData(copiedChordTemplates, copiedHandShapes);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private <T extends IPosition, V extends CopiedPosition<T>> CopyData getCopyData(final PositionType type,
			final CopiedPositionMaker<T, V> copiedPositionMaker,
			final Function<ArrayList2<V>, ICopyData> copyDataMaker) {
		final SelectionAccessor<T> selectionAccessor = selectionManager.getSelectedAccessor(type);
		if (!selectionAccessor.isSelected()) {
			return null;
		}

		final ArrayList2<Selection<T>> selectedVocals = selectionAccessor.getSortedSelected();
		final ArrayList2<V> copiedVocals = makeCopy(selectedVocals, copiedPositionMaker);

		final int from = selectedVocals.get(0).selectable.position();
		final int to = selectedVocals.getLast().selectable.position();

		final FullCopyData fullCopyData = getFullCopyData(from, to);

		return new CopyData(copyDataMaker.apply(copiedVocals), fullCopyData);
	}

	private CopyData getGuitarCopyData() {
		if (selectionManager.getSelectedAccessor(PositionType.ANCHOR).isSelected()) {
			return getCopyData(PositionType.ANCHOR, CopiedAnchorPosition::new, AnchorsCopyData::new);
		}
		if (selectionManager.getSelectedAccessor(PositionType.EVENT_POINT).isSelected()) {
			return getGuitarCopyDataEventPoints();
		}
		if (selectionManager.getSelectedAccessor(PositionType.GUITAR_NOTE).isSelected()) {
			return getGuitarCopyDataGuitarNotes();
		}
		if (selectionManager.getSelectedAccessor(PositionType.HAND_SHAPE).isSelected()) {
			return getGuitarCopyDataHandShapes();
		}
		if (selectionManager.getSelectedAccessor(PositionType.TONE_CHANGE).isSelected()) {
			return getCopyData(PositionType.TONE_CHANGE, CopiedAnchorPosition::new, AnchorsCopyData::new);
		}

		return null;
	}

	private CopyData getCopyData() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return getGuitarCopyData();
			case VOCALS:
				return getCopyData(PositionType.VOCAL, CopiedVocalPosition::new, VocalsCopyData::new);
			case TEMPO_MAP:
			default:
				return null;
		}
	}

	public void copy() {
		final CopyData copyData = getCopyData();
		if (copyData == null) {
			return;
		}

		final String xml = ChartProjectXStreamHandler.writeCopyData(copyData);
		ClipboardHandler.setClipboardBytes(xml.getBytes());
	}

	private CopyData getDataFromClipboard() {
		final String xml = new String(ClipboardHandler.readClipboardBytes());
		if (xml.isEmpty()) {
			return null;
		}

		try {
			return ChartProjectXStreamHandler.readCopyData(xml);
		} catch (final Exception e) {
			if (e instanceof StreamException) {
				return null;
			}

			Logger.debug("xml parse failed:\n" + xml, e);
			return null;
		}
	}

	public void paste() {
		final CopyData copyData = getDataFromClipboard();
		if (copyData == null || copyData.selectedCopy == null) {
			return;
		}

		final ICopyData selectedCopy = copyData.selectedCopy;
		if (selectedCopy.isEmpty()) {
			return;
		}
		if (modeManager.getMode() == EditMode.TEMPO_MAP) {
			return;
		}

		final boolean isVocalsEditMode = modeManager.getMode() == EditMode.VOCALS;
		final boolean isVocalsCopyData = selectedCopy instanceof VocalsCopyData;
		if (isVocalsEditMode != isVocalsCopyData) {
			return;
		}

		undoSystem.addUndo();
		selectionManager.clear();
		selectedCopy.paste(chartData, selectionManager, chartTimeHandler.time(), true);
	}

	public void specialPaste() {
		final CopyData copyData = getDataFromClipboard();
		if (copyData == null || copyData.selectedCopy == null) {
			return;
		}

		final FullCopyData fullCopy = copyData.fullCopy;
		if (fullCopy == null || fullCopy.isEmpty()) {
			return;
		}

		if (fullCopy instanceof FullGuitarCopyData) {
			new GuitarSpecialPastePane(chartData, charterFrame, selectionManager, undoSystem, chartTimeHandler.time(),
					(FullGuitarCopyData) fullCopy);
			return;
		}
	}
}
