package log.charter.data.copySystem;

import static java.util.stream.Collectors.toCollection;
import static log.charter.data.copySystem.data.positions.CopiedPosition.findBeatPositionForPosition;
import static log.charter.song.notes.IPosition.findFirstIdAfterEqual;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import java.util.function.Function;

import com.thoughtworks.xstream.io.StreamException;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.AnchorsCopyData;
import log.charter.data.copySystem.data.BeatsCopyData;
import log.charter.data.copySystem.data.CopyData;
import log.charter.data.copySystem.data.CopyDataXStreamHandler;
import log.charter.data.copySystem.data.FullCopyData;
import log.charter.data.copySystem.data.FullGuitarCopyData;
import log.charter.data.copySystem.data.HandShapesCopyData;
import log.charter.data.copySystem.data.ICopyData;
import log.charter.data.copySystem.data.SoundsCopyData;
import log.charter.data.copySystem.data.VocalsCopyData;
import log.charter.data.copySystem.data.positions.CopiedAnchorPosition;
import log.charter.data.copySystem.data.positions.CopiedEventPosition;
import log.charter.data.copySystem.data.positions.CopiedHandShapePosition;
import log.charter.data.copySystem.data.positions.CopiedOnBeatPosition;
import log.charter.data.copySystem.data.positions.CopiedPhraseIterationPosition;
import log.charter.data.copySystem.data.positions.CopiedPosition;
import log.charter.data.copySystem.data.positions.CopiedSectionPosition;
import log.charter.data.copySystem.data.positions.CopiedSoundPosition;
import log.charter.data.copySystem.data.positions.CopiedToneChangePosition;
import log.charter.data.copySystem.data.positions.CopiedVocalPosition;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.GuitarSpecialPastePane;
import log.charter.io.ClipboardHandler;
import log.charter.io.Logger;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.OnBeat;
import log.charter.song.Phrase;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;

public class CopyManager {
	private static interface CopiedPositionMaker<T extends IPosition, V extends CopiedPosition<T>> {
		V make(ArrayList2<Beat> beats, double basePositionInBeats, T position);
	}

	private static interface CopiedOnBeatPositionMaker<T extends OnBeat, V extends CopiedOnBeatPosition<T>> {
		V make(ArrayList2<Beat> beats, int baseBeat, T position);
	}

	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final ModeManager modeManager,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	private <T extends IPosition, V extends CopiedPosition<T>> ArrayList2<V> makeCopy(final ArrayList2<T> positions,
			final double basePositionInBeats, final CopiedPositionMaker<T, V> copiedPositionMaker) {
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

		return positions.map(position -> copiedPositionMaker.make(beats, basePositionInBeats, position));
	}

	private <T extends IPosition, V extends CopiedPosition<T>> ArrayList2<V> makeCopy(
			final ArrayList2<Selection<T>> selectedPositions, final CopiedPositionMaker<T, V> copiedPositionMaker) {
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
		final double basePositionInBeats = findBeatPositionForPosition(beats,
				selectedPositions.get(0).selectable.position());

		return makeCopy(selectedPositions.map(selected -> selected.selectable), basePositionInBeats,
				copiedPositionMaker);
	}

	private <T extends OnBeat, V extends CopiedOnBeatPosition<T>> ArrayList2<V> makeBeatsCopyFromToId(int fromId,
			int toId, final int baseBeatId, final ArrayList2<T> positions,
			final CopiedOnBeatPositionMaker<T, V> copiedOnBeatPositionMaker) {
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

		if (fromId == -1) {
			fromId = 0;
		}
		if (toId == -1) {
			toId = positions.size() - 1;
		}
		if (toId < fromId) {
			return new ArrayList2<>();
		}

		return positions.stream().skip(fromId).limit(toId - fromId + 1)//
				.map(section -> copiedOnBeatPositionMaker.make(beats, baseBeatId, section))//
				.collect(toCollection(ArrayList2::new));
	}

	private <T extends OnBeat, V extends CopiedOnBeatPosition<T>> ArrayList2<V> makeBeatsCopyFromToSectionsPhrases(
			final int from, final int to, final int baseBeatId, final ArrayList2<T> positions,
			final CopiedOnBeatPositionMaker<T, V> copiedOnBeatPositionMaker) {
		final int fromId = findLastIdBeforeEqual(positions, from);
		final int toId = findLastIdBeforeEqual(positions, to);
		return makeBeatsCopyFromToId(fromId, toId, baseBeatId, positions, copiedOnBeatPositionMaker);
	}

	private <T extends OnBeat, V extends CopiedOnBeatPosition<T>> ArrayList2<V> makeBeatsCopyFromTo(final int from,
			final int to, final int baseBeatId, final ArrayList2<T> positions,
			final CopiedOnBeatPositionMaker<T, V> copiedOnBeatPositionMaker) {
		final int fromId = findFirstIdAfterEqual(positions, from);
		final int toId = findLastIdBeforeEqual(positions, to);
		return makeBeatsCopyFromToId(fromId, toId, baseBeatId, positions, copiedOnBeatPositionMaker);
	}

	private <T extends OnBeat, V extends CopiedOnBeatPosition<T>> ArrayList2<V> makeBeatsCopyFor(
			final HashSet2<Beat> selectedBeatsSet, final int baseBeat, final ArrayList2<T> positions,
			final CopiedOnBeatPositionMaker<T, V> copiedOnBeatPositionMaker) {
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

		return positions.stream()//
				.filter(onBeat -> selectedBeatsSet.contains(onBeat.beat))//
				.map(section -> copiedOnBeatPositionMaker.make(beats, baseBeat, section))//
				.collect(toCollection(ArrayList2::new));
	}

	private <T extends IPosition, V extends CopiedPosition<T>> ArrayList2<V> copyPositionsFromTo(final int from,
			final int to, final double basePositionInBeats, final ArrayList2<T> positions,
			final CopiedPositionMaker<T, V> copiedPositionMaker) {
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

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
				.map(position -> copiedPositionMaker.make(beats, basePositionInBeats, position))//
				.collect(toCollection(ArrayList2::new));
	}

	private FullCopyData getFullCopyData(final int from, final int to) {
		if (modeManager.editMode != EditMode.GUITAR) {
			return null;
		}

		final ArrangementChart arrangement = data.getCurrentArrangement();
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
		final double basePositionInBeats = findBeatPositionForPosition(beats, from);
		final int baseBeatId = (int) basePositionInBeats;

		final ArrayList2<CopiedSectionPosition> copiedSections = makeBeatsCopyFromToSectionsPhrases(from, to,
				baseBeatId, arrangement.sections, CopiedSectionPosition::new);
		final HashMap2<String, Phrase> copiedPhrases = arrangement.phrases.map(phraseName -> phraseName, Phrase::new);
		final ArrayList2<CopiedPhraseIterationPosition> copiedPhraseIterations = makeBeatsCopyFromToSectionsPhrases(
				from, to, baseBeatId, arrangement.phraseIterations, CopiedPhraseIterationPosition::new);
		final ArrayList2<CopiedEventPosition> copiedEvents = makeBeatsCopyFromTo(from, to, baseBeatId,
				arrangement.events, CopiedEventPosition::new);
		final ArrayList2<ChordTemplate> copiedChordTemplates = data.getCurrentArrangement().chordTemplates
				.map(ChordTemplate::new);
		final ArrayList2<CopiedToneChangePosition> copiedToneChanges = copyPositionsFromTo(from, to,
				basePositionInBeats, data.getCurrentArrangement().toneChanges, CopiedToneChangePosition::new);
		final ArrayList2<CopiedAnchorPosition> copiedAnchors = copyPositionsFromTo(from, to, basePositionInBeats,
				data.getCurrentArrangementLevel().anchors, CopiedAnchorPosition::new);
		final ArrayList2<CopiedSoundPosition> copiedSounds = copyPositionsFromTo(from, to, basePositionInBeats,
				data.getCurrentArrangementLevel().chordsAndNotes, CopiedSoundPosition::new);
		final ArrayList2<CopiedHandShapePosition> copiedHandShapes = copyPositionsFromTo(from, to, basePositionInBeats,
				data.getCurrentArrangementLevel().handShapes, CopiedHandShapePosition::new);

		return new FullGuitarCopyData(copiedSections, copiedPhrases, copiedPhraseIterations, copiedEvents,
				copiedChordTemplates, copiedToneChanges, copiedAnchors, copiedSounds, copiedHandShapes);
	}

	private CopyData getGuitarCopyDataBeats() {
		final SelectionAccessor<Beat> selectedBeatsAccessor = selectionManager.getSelectedAccessor(PositionType.BEAT);

		final ArrayList2<Selection<Beat>> selectedBeats = selectedBeatsAccessor.getSortedSelected();
		final HashSet2<Beat> selectedBeatsSet = new HashSet2<>(selectedBeats).map(selection -> selection.selectable);
		final int baseBeat = selectedBeats.get(0).id;

		final ArrayList2<CopiedSectionPosition> copiedSections = makeBeatsCopyFor(selectedBeatsSet, baseBeat,
				data.getCurrentArrangement().sections, CopiedSectionPosition::new);
		final HashMap2<String, Phrase> copiedPhrases = data.getCurrentArrangement().phrases
				.map(phraseName -> phraseName, Phrase::new);
		final ArrayList2<CopiedPhraseIterationPosition> copiedPhraseIterations = makeBeatsCopyFor(selectedBeatsSet,
				baseBeat, data.getCurrentArrangement().phraseIterations, CopiedPhraseIterationPosition::new);
		final ArrayList2<CopiedEventPosition> copiedEvents = makeBeatsCopyFor(selectedBeatsSet, baseBeat,
				data.getCurrentArrangement().events, CopiedEventPosition::new);

		final ICopyData copyData = new BeatsCopyData(copiedSections, copiedPhrases, copiedPhraseIterations,
				copiedEvents);
		final int from = selectedBeats.get(0).selectable.position();
		final int to = selectedBeats.getLast().selectable.position();

		return new CopyData(copyData, getFullCopyData(from, to));
	}

	private CopyData getGuitarCopyDataGuitarNotes() {
		final SelectionAccessor<ChordOrNote> selectedSoundsAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectedSoundsAccessor.getSortedSelected();

		final ArrayList2<ChordTemplate> copiedChordTemplates = data.getCurrentArrangement().chordTemplates
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

		final ArrayList2<ChordTemplate> copiedChordTemplates = data.getCurrentArrangement().chordTemplates
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
		if (selectionManager.getSelectedAccessor(PositionType.BEAT).isSelected()) {
			return getGuitarCopyDataBeats();
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
		if (modeManager.editMode == EditMode.GUITAR) {
			return getGuitarCopyData();
		}

		if (modeManager.editMode == EditMode.TEMPO_MAP) {
			return null;
		}

		if (modeManager.editMode == EditMode.VOCALS) {
			return getCopyData(PositionType.VOCAL, CopiedVocalPosition::new, VocalsCopyData::new);
		}

		return null;
	}

	public void copy() {
		final CopyData copyData = getCopyData();
		if (copyData == null) {
			return;
		}

		final String xml = CopyDataXStreamHandler.saveProject(copyData);
		ClipboardHandler.setClipboardBytes(xml.getBytes());
	}

	private CopyData getDataFromClipboard() {
		final String xml = new String(ClipboardHandler.readClipboardBytes());
		if (xml.isEmpty()) {
			return null;
		}

		try {
			return CopyDataXStreamHandler.readProject(xml);
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
		if (modeManager.editMode == EditMode.TEMPO_MAP) {
			return;
		}
		final boolean isVocalsEditMode = modeManager.editMode == EditMode.VOCALS;
		final boolean isVocalsCopyData = selectedCopy instanceof VocalsCopyData;
		if (isVocalsEditMode != isVocalsCopyData) {
			return;
		}

		undoSystem.addUndo();
		selectionManager.clear();
		selectedCopy.paste(data);
	}

	public void specialPaste() {
		if (modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final CopyData copyData = getDataFromClipboard();
		if (copyData == null || copyData.selectedCopy == null) {
			return;
		}

		final FullCopyData fullCopy = copyData.fullCopy;
		if (fullCopy == null || fullCopy.isEmpty()) {
			return;
		}

		if (fullCopy instanceof FullGuitarCopyData) {
			new GuitarSpecialPastePane(data, frame, selectionManager, undoSystem, (FullGuitarCopyData) fullCopy);
			return;
		}
	}
}
