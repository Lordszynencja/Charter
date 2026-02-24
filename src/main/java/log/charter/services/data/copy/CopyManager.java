package log.charter.services.data.copy;

import static log.charter.util.CollectionUtils.getFromTo;
import static log.charter.util.CollectionUtils.map;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.panes.songEdits.GuitarSpecialPastePane;
import log.charter.io.ClipboardHandler;
import log.charter.io.Logger;
import log.charter.io.rsc.xml.ChartProjectXStreamHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.copy.data.CopyData;
import log.charter.services.data.copy.data.EventPointsCopyData;
import log.charter.services.data.copy.data.FHPsCopyData;
import log.charter.services.data.copy.data.FullCopyData;
import log.charter.services.data.copy.data.FullGuitarCopyData;
import log.charter.services.data.copy.data.HandShapesCopyData;
import log.charter.services.data.copy.data.ICopyData;
import log.charter.services.data.copy.data.ShowlightsCopyData;
import log.charter.services.data.copy.data.SoundsCopyData;
import log.charter.services.data.copy.data.VocalsCopyData;
import log.charter.services.data.copy.data.positions.Copied;
import log.charter.services.data.copy.data.positions.CopiedEventPoint;
import log.charter.services.data.copy.data.positions.CopiedFHP;
import log.charter.services.data.copy.data.positions.CopiedHandShape;
import log.charter.services.data.copy.data.positions.CopiedShowlight;
import log.charter.services.data.copy.data.positions.CopiedSound;
import log.charter.services.data.copy.data.positions.CopiedToneChange;
import log.charter.services.data.copy.data.positions.CopiedVocalPosition;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

public class CopyManager {
	private static interface CopyMakerSimple<T, V extends Copied<T>> {
		V make(FractionalPosition basePosition, T item);
	}

	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private <T extends IConstantFractionalPosition, V extends Copied<T>> List<V> makeCopy(final List<T> selected,
			final CopyMakerSimple<T, V> copyMaker) {
		final FractionalPosition basePosition = selected.get(0).position();
		return map(selected, e -> copyMaker.make(basePosition, e));
	}

	private <T extends IConstantFractionalPosition, V extends Copied<T>> List<V> copyPositionsFromTo(
			final FractionalPosition from, final FractionalPosition to, final List<T> positions,
			final CopyMakerSimple<T, V> copyMaker) {
		return map(getFromTo(positions, from, to), p -> copyMaker.make(from, p));
	}

	private FullCopyData getFullCopyData(final IVirtualConstantPosition fromVirtual,
			final IVirtualConstantPosition toVirtual) {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return null;
		}

		final Arrangement arrangement = chartData.currentArrangement();
		final ImmutableBeatsMap beats = chartData.beats();
		final FractionalPosition from = fromVirtual.toFraction(beats).position();
		final FractionalPosition to = toVirtual.toFraction(beats).position();

		final Map<String, Phrase> copiedPhrases = map(arrangement.phrases, k -> k, Phrase::new);
		final List<CopiedEventPoint> copiedArrangementEventsPoints = copyPositionsFromTo(from, to,
				arrangement.eventPoints, CopiedEventPoint::new);
		final List<ChordTemplate> copiedChordTemplates = map(chartData.currentChordTemplates(), ChordTemplate::new);
		final List<CopiedToneChange> copiedToneChanges = copyPositionsFromTo(from, to, arrangement.toneChanges,
				CopiedToneChange::new);
		final List<CopiedFHP> copiedFHPs = copyPositionsFromTo(from, to, chartData.currentArrangementLevel().fhps,
				CopiedFHP::new);
		final List<CopiedSound> copiedSounds = copyPositionsFromTo(from, to, chartData.currentSounds(),
				CopiedSound::copy);
		final List<CopiedHandShape> copiedHandShapes = copyPositionsFromTo(from, to, chartData.currentHandShapes(),
				CopiedHandShape::new);

		return new FullGuitarCopyData(copiedPhrases, copiedArrangementEventsPoints, copiedChordTemplates,
				copiedToneChanges, copiedFHPs, copiedSounds, copiedHandShapes);
	}

	private CopyData getGuitarCopyDataEventPoints() {
		final List<EventPoint> selected = selectionManager.getSelectedElements(PositionType.EVENT_POINT);
		final FractionalPosition from = selected.get(0).position();
		final FractionalPosition to = selected.get(selected.size() - 1).position();
		final Arrangement arrangement = chartData.currentArrangement();

		final Map<String, Phrase> copiedPhrases = map(arrangement.phrases, phraseName -> phraseName, Phrase::new);
		final List<CopiedEventPoint> copiedArrangementEventsPoints = copyPositionsFromTo(from, to,
				arrangement.eventPoints, CopiedEventPoint::new);

		final ICopyData copyData = new EventPointsCopyData(copiedPhrases, copiedArrangementEventsPoints);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private CopyData getGuitarCopyDataGuitarNotes() {
		final List<ChordOrNote> selected = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);

		final List<ChordTemplate> copiedChordTemplates = chartData.currentArrangement().chordTemplates//
				.stream().map(ChordTemplate::new).collect(Collectors.toList());
		final List<CopiedSound> copiedSounds = makeCopy(selected, CopiedSound::copy);
		final FractionalPosition from = selected.get(0).position();
		final FractionalPosition to = selected.get(selected.size() - 1).endPosition().position();

		final ICopyData copyData = new SoundsCopyData(copiedChordTemplates, copiedSounds);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private CopyData getGuitarCopyDataHandShapes() {
		final List<HandShape> selectedHandShapes = selectionManager.getSelectedElements(PositionType.HAND_SHAPE);

		final List<ChordTemplate> copiedChordTemplates = map(chartData.currentChordTemplates(), ChordTemplate::new);
		final List<CopiedHandShape> copiedHandShapes = makeCopy(selectedHandShapes, CopiedHandShape::new);
		final FractionalPosition from = selectedHandShapes.get(0).position();
		final FractionalPosition to = selectedHandShapes.get(selectedHandShapes.size() - 1).endPosition().position();

		final ICopyData copyData = new HandShapesCopyData(copiedChordTemplates, copiedHandShapes);
		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private <T extends IConstantFractionalPositionWithEnd, V extends Copied<T>> CopyData getCopyDataWithEnd(
			final PositionType type, final CopyMakerSimple<T, V> copiedPositionMaker,
			final Function<List<V>, ICopyData> copyDataMaker) {
		final ISelectionAccessor<T> selectionAccessor = selectionManager.accessor(type);
		if (!selectionAccessor.isSelected()) {
			return null;
		}

		final List<T> selected = selectionAccessor.getSelectedElements();
		final List<V> copied = makeCopy(selected, copiedPositionMaker);

		final FractionalPosition from = selected.get(0).position();
		final FractionalPosition to = selected.get(selected.size() - 1).endPosition();

		final FullCopyData fullCopyData = getFullCopyData(from, to);

		return new CopyData(copyDataMaker.apply(copied), fullCopyData);
	}

	private <T extends IConstantFractionalPosition, V extends Copied<T>> CopyData getCopyData(final PositionType type,
			final CopyMakerSimple<T, V> copiedPositionMaker, final Function<List<V>, ICopyData> copyDataMaker) {
		final ISelectionAccessor<T> selectionAccessor = selectionManager.accessor(type);
		if (!selectionAccessor.isSelected()) {
			return null;
		}

		final List<T> selected = selectionAccessor.getSelectedElements();
		final List<V> copied = makeCopy(selected, copiedPositionMaker);

		final FractionalPosition from = selected.get(0).position();
		final FractionalPosition to = selected.get(selected.size() - 1).position();

		final FullCopyData fullCopyData = getFullCopyData(from, to);

		return new CopyData(copyDataMaker.apply(copied), fullCopyData);
	}

	private CopyData getGuitarCopyData() {
		if (selectionManager.accessor(PositionType.FHP).isSelected()) {
			return getCopyData(PositionType.FHP, CopiedFHP::new, FHPsCopyData::new);
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
			return getCopyData(PositionType.TONE_CHANGE, CopiedFHP::new, FHPsCopyData::new);
		}

		return null;
	}

	private CopyData getCopyData() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return getGuitarCopyData();
			case SHOWLIGHTS:
				return getCopyData(PositionType.SHOWLIGHT, CopiedShowlight::new, ShowlightsCopyData::new);
			case VOCALS:
				return getCopyDataWithEnd(PositionType.VOCAL, CopiedVocalPosition::new, VocalsCopyData::new);
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
		try {
			ClipboardHandler.setClipboardBytes(xml.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			Logger.error("Couldn't copy data", e);
		}
	}

	private CopyData getDataFromClipboard() {
		String xml;
		try {
			xml = new String(ClipboardHandler.readClipboardBytes(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			Logger.error("Couldn't read clipboard data", e);
			return null;
		}
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

	private void pasteGuitar(final CopyData copyData) {
		final ICopyData selectedCopy = copyData.selectedCopy;
		if (selectedCopy.isEmpty()) {
			return;
		}
		switch (selectedCopy.type()) {
			case EVENT_POINT:
			case TONE_CHANGE:
			case FHP:
			case GUITAR_NOTE:
			case HAND_SHAPE:
				break;
			default:
				return;
		}

		undoSystem.addUndo();
		selectionManager.clear();

		final FractionalPosition currentTime = chartTimeHandler.timeFractional();
		if (selectedCopy.type() == PositionType.GUITAR_NOTE) {
			final FullCopyData fullCopy = copyData.fullCopy;
			if (fullCopy instanceof FullGuitarCopyData) {
				final FullGuitarCopyData fullGuitarCopyData = (FullGuitarCopyData) fullCopy;
				fullGuitarCopyData.toneChanges.paste(chartData, selectionManager, currentTime);
				fullGuitarCopyData.fhps.paste(chartData, selectionManager, currentTime);
				fullGuitarCopyData.handShapes.paste(chartData, selectionManager, currentTime);
			}
		}

		selectedCopy.paste(chartData, selectionManager, currentTime);

		chordTemplatesEditorTab.refreshTemplates();
	}

	private void pasteShowlights(final CopyData copyData) {
		final ICopyData selectedCopy = copyData.selectedCopy;
		if (selectedCopy.isEmpty() || selectedCopy.type() != PositionType.SHOWLIGHT) {
			return;
		}

		undoSystem.addUndo();
		selectionManager.clear();
		selectedCopy.paste(chartData, selectionManager, chartTimeHandler.timeFractional());
	}

	private void pasteVocals(final CopyData copyData) {
		final ICopyData selectedCopy = copyData.selectedCopy;
		if (selectedCopy.isEmpty() || selectedCopy.type() != PositionType.VOCAL) {
			return;
		}

		undoSystem.addUndo();
		selectionManager.clear();
		selectedCopy.paste(chartData, selectionManager, chartTimeHandler.timeFractional());
	}

	public void paste() {
		if (modeManager.getMode() == EditMode.EMPTY || modeManager.getMode() == EditMode.TEMPO_MAP) {
			return;
		}

		final CopyData copyData = getDataFromClipboard();
		if (copyData == null || copyData.selectedCopy == null) {
			return;
		}

		if (modeManager.getMode() == EditMode.GUITAR) {
			pasteGuitar(copyData);
			return;
		}
		if (modeManager.getMode() == EditMode.SHOWLIGHTS) {
			pasteShowlights(copyData);
			return;
		}
		if (modeManager.getMode() == EditMode.VOCALS) {
			pasteVocals(copyData);
			return;
		}
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
			new GuitarSpecialPastePane(chartData, charterFrame, chordTemplatesEditorTab, selectionManager, undoSystem,
					chartTimeHandler.timeFractional(), (FullGuitarCopyData) fullCopy);
			return;
		}
	}
}
