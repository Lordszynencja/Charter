package log.charter.services.data.copy;

import static log.charter.util.CollectionUtils.getFromTo;
import static log.charter.util.CollectionUtils.map;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.thoughtworks.xstream.io.StreamException;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Phrase;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPosition;
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
import log.charter.services.data.copy.data.positions.Copied;
import log.charter.services.data.copy.data.positions.CopiedAnchorPosition;
import log.charter.services.data.copy.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.services.data.copy.data.positions.CopiedHandShapePosition;
import log.charter.services.data.copy.data.positions.CopiedSoundPosition;
import log.charter.services.data.copy.data.positions.CopiedToneChangePosition;
import log.charter.services.data.copy.data.positions.CopiedVocalPosition;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

public class CopyManager {
	private static interface CopyMaker<T, V extends Copied<T>> {
		V make(ImmutableBeatsMap beats, FractionalPosition basePosition, T item);
	}

	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private <T, V extends Copied<T>> List<V> makeCopy(final Stream<T> positions, final FractionalPosition basePosition,
			final CopyMaker<T, V> copyMaker) {
		return positions//
				.map(position -> copyMaker.make(chartData.beats(), basePosition, position))//
				.collect(Collectors.toList());
	}

	private <T extends IVirtualPosition, V extends Copied<T>> List<V> makeCopy(
			final List<Selection<T>> selectedPositions, final CopyMaker<T, V> copyMaker) {
		final ImmutableBeatsMap beats = chartData.beats();
		final FractionalPosition basePosition = selectedPositions.get(0).selectable.toFraction(beats)
				.fractionalPosition();

		return makeCopy(selectedPositions.stream().map(selected -> selected.selectable), basePosition, copyMaker);
	}

	private <T extends IVirtualPosition, V extends Copied<T>> List<V> copyPositionsFromTo(
			final IVirtualConstantPosition from, final IVirtualConstantPosition to,
			final FractionalPosition basePosition, final List<T> positions, final CopyMaker<T, V> copyMaker) {
		final ImmutableBeatsMap beats = chartData.beats();
		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(beats);
		final List<V> list = map(getFromTo(positions, from, to, comparator),
				p -> copyMaker.make(beats, basePosition, p));

		return list;
	}

	private FullCopyData getFullCopyData(final IVirtualConstantPosition from, final IVirtualConstantPosition to) {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return null;
		}

		final Arrangement arrangement = chartData.currentArrangement();
		final ImmutableBeatsMap beats = chartData.beats();
		final FractionalPosition basePosition = from.toFraction(beats).fractionalPosition();

		final Map<String, Phrase> copiedPhrases = map(arrangement.phrases, k -> k, Phrase::new);
		final List<CopiedArrangementEventsPointPosition> copiedArrangementEventsPoints = copyPositionsFromTo(from, to,
				basePosition, arrangement.eventPoints, CopiedArrangementEventsPointPosition::new);
		final List<ChordTemplate> copiedChordTemplates = map(chartData.currentChordTemplates(), ChordTemplate::new);
		final List<CopiedToneChangePosition> copiedToneChanges = copyPositionsFromTo(from, to, basePosition,
				arrangement.toneChanges, CopiedToneChangePosition::new);
		final List<CopiedAnchorPosition> copiedAnchors = copyPositionsFromTo(from, to, basePosition,
				chartData.currentArrangementLevel().anchors, CopiedAnchorPosition::new);
		final List<CopiedSoundPosition> copiedSounds = copyPositionsFromTo(from, to, basePosition,
				chartData.currentSounds(), CopiedSoundPosition::new);
		final List<CopiedHandShapePosition> copiedHandShapes = copyPositionsFromTo(from, to, basePosition,
				chartData.currentHandShapes(), CopiedHandShapePosition::new);

		return new FullGuitarCopyData(copiedPhrases, copiedArrangementEventsPoints, copiedChordTemplates,
				copiedToneChanges, copiedAnchors, copiedSounds, copiedHandShapes);
	}

	private CopyData getGuitarCopyDataEventPoints() {
		final ISelectionAccessor<EventPoint> selectedBeatsAccessor = selectionManager
				.accessor(PositionType.EVENT_POINT);

		final List<Selection<EventPoint>> selectedBeats = selectedBeatsAccessor.getSelected();
		final IVirtualConstantPosition from = selectedBeats.get(0).selectable;
		final IVirtualConstantPosition to = selectedBeats.get(selectedBeats.size() - 1).selectable;
		final FractionalPosition basePosition = from.toFraction(chartData.beats()).fractionalPosition();
		final Arrangement arrangement = chartData.currentArrangement();

		final Map<String, Phrase> copiedPhrases = map(arrangement.phrases, phraseName -> phraseName, Phrase::new);
		final List<CopiedArrangementEventsPointPosition> copiedArrangementEventsPoints = copyPositionsFromTo(from, to,
				basePosition, arrangement.eventPoints, CopiedArrangementEventsPointPosition::new);

		final ICopyData copyData = new EventPointsCopyData(copiedPhrases, copiedArrangementEventsPoints);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private CopyData getGuitarCopyDataGuitarNotes() {
		final ISelectionAccessor<ChordOrNote> selectedSoundsAccessor = selectionManager
				.accessor(PositionType.GUITAR_NOTE);
		final List<Selection<ChordOrNote>> selectedSounds = selectedSoundsAccessor.getSelected();

		final List<ChordTemplate> copiedChordTemplates = chartData.currentArrangement().chordTemplates//
				.stream().map(ChordTemplate::new).collect(Collectors.toList());
		final List<CopiedSoundPosition> copiedSounds = makeCopy(selectedSounds, CopiedSoundPosition::new);
		final IVirtualConstantPosition from = selectedSounds.get(0).selectable;
		final IVirtualConstantPosition to = selectedSounds.get(selectedSounds.size() - 1).selectable;

		final ICopyData copyData = new SoundsCopyData(copiedChordTemplates, copiedSounds);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private CopyData getGuitarCopyDataHandShapes() {
		final ISelectionAccessor<HandShape> selectedHandShapesAccessor = selectionManager
				.accessor(PositionType.HAND_SHAPE);
		final List<Selection<HandShape>> selectedHandShapes = selectedHandShapesAccessor.getSelected();

		final List<ChordTemplate> copiedChordTemplates = map(chartData.currentChordTemplates(), ChordTemplate::new);
		final List<CopiedHandShapePosition> copiedHandShapes = makeCopy(selectedHandShapes,
				CopiedHandShapePosition::new);
		final IVirtualConstantPosition from = selectedHandShapes.get(0).selectable;
		final IVirtualConstantPosition to = selectedHandShapes.get(selectedHandShapes.size() - 1).selectable;

		final ICopyData copyData = new HandShapesCopyData(copiedChordTemplates, copiedHandShapes);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private <T extends IVirtualPosition, V extends Copied<T>> CopyData getCopyData(final PositionType type,
			final CopyMaker<T, V> copiedPositionMaker, final Function<List<V>, ICopyData> copyDataMaker) {
		final ISelectionAccessor<T> selectionAccessor = selectionManager.accessor(type);
		if (!selectionAccessor.isSelected()) {
			return null;
		}

		final List<Selection<T>> selected = selectionAccessor.getSelected();
		final List<V> copied = makeCopy(selected, copiedPositionMaker);

		final IVirtualConstantPosition from = selected.get(0).selectable;
		final IVirtualConstantPosition to = selected.get(selected.size() - 1).selectable;

		final FullCopyData fullCopyData = getFullCopyData(from, to);

		return new CopyData(copyDataMaker.apply(copied), fullCopyData);
	}

	private CopyData getGuitarCopyData() {
		if (selectionManager.accessor(PositionType.ANCHOR).isSelected()) {
			return getCopyData(PositionType.ANCHOR, CopiedAnchorPosition::new, AnchorsCopyData::new);
		}
		if (selectionManager.accessor(PositionType.EVENT_POINT).isSelected()) {
			return getGuitarCopyDataEventPoints();
		}
		if (selectionManager.accessor(PositionType.GUITAR_NOTE).isSelected()) {
			return getGuitarCopyDataGuitarNotes();
		}
		if (selectionManager.accessor(PositionType.HAND_SHAPE).isSelected()) {
			return getGuitarCopyDataHandShapes();
		}
		if (selectionManager.accessor(PositionType.TONE_CHANGE).isSelected()) {
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
		selectedCopy.paste(chartData, selectionManager, chartTimeHandler.timeFractional(), true);
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
			new GuitarSpecialPastePane(chartData, charterFrame, selectionManager, undoSystem,
					chartTimeHandler.timeFractional(), (FullGuitarCopyData) fullCopy);
			return;
		}
	}
}
