package log.charter.gui.handlers;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;
import static java.awt.event.KeyEvent.VK_3;
import static java.awt.event.KeyEvent.VK_4;
import static java.awt.event.KeyEvent.VK_5;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_7;
import static java.awt.event.KeyEvent.VK_8;
import static java.awt.event.KeyEvent.VK_9;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_CAPS_LOCK;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_END;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_F3;
import static java.awt.event.KeyEvent.VK_F4;
import static java.awt.event.KeyEvent.VK_F5;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_HOME;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_M;
import static java.awt.event.KeyEvent.VK_N;
import static java.awt.event.KeyEvent.VK_NUMPAD0;
import static java.awt.event.KeyEvent.VK_NUMPAD1;
import static java.awt.event.KeyEvent.VK_NUMPAD2;
import static java.awt.event.KeyEvent.VK_NUMPAD3;
import static java.awt.event.KeyEvent.VK_NUMPAD4;
import static java.awt.event.KeyEvent.VK_NUMPAD5;
import static java.awt.event.KeyEvent.VK_NUMPAD6;
import static java.awt.event.KeyEvent.VK_NUMPAD7;
import static java.awt.event.KeyEvent.VK_NUMPAD8;
import static java.awt.event.KeyEvent.VK_NUMPAD9;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_OPEN_BRACKET;
import static java.awt.event.KeyEvent.VK_PERIOD;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_V;
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_Z;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static log.charter.data.config.Config.frets;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.song.notes.IPosition.findFirstAfter;
import static log.charter.song.notes.IPosition.findFirstIdAfter;
import static log.charter.song.notes.IPosition.findLastBefore;
import static log.charter.song.notes.IPosition.findLastIdBefore;
import static log.charter.song.notes.IPosition.getFromTo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.panes.ChordBendPane;
import log.charter.gui.panes.ChordOptionsPane;
import log.charter.gui.panes.GridPane;
import log.charter.gui.panes.HandShapePane;
import log.charter.gui.panes.NoteBendPane;
import log.charter.gui.panes.NoteOptionsPane;
import log.charter.gui.panes.SlidePane;
import log.charter.gui.panes.VocalPane;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.GuitarSound;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.IPositionWithLength;
import log.charter.song.notes.Note;
import log.charter.song.notes.Position;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.chordRecognition.ChordNameSuggester;
import log.charter.util.grid.GridPosition;

public class KeyboardHandler implements KeyListener {
	private static Consumer<KeyEvent> emptyHandler = e -> {
	};

	private static int modifierKeysValue(final boolean ctrl, final boolean alt, final boolean shift) {
		return ((ctrl ? 1 : 0) << 2)//
				+ ((alt ? 1 : 0) << 1)//
				+ (shift ? 1 : 0);
	}

	private class KeyHandlerBuilder {
		private final int key;
		private int modifiersValue = 0;
		private Consumer<KeyEvent> function = emptyHandler;

		public KeyHandlerBuilder(final int key) {
			this.key = key;
		}

		public KeyHandlerBuilder ctrl() {
			modifiersValue ^= 4;
			return this;
		}

		@SuppressWarnings("unused")
		public KeyHandlerBuilder alt() {
			modifiersValue ^= 2;
			return this;
		}

		public KeyHandlerBuilder shift() {
			modifiersValue ^= 1;
			return this;
		}

		public void singleFunction(final Runnable function) {
			this.function = e -> {
				function.run();
			};

			addSingle();
		}

		public void singleFunction(final Consumer<KeyEvent> function) {
			this.function = function;

			addSingle();
		}

		public void function(final Runnable function) {
			this.function = e -> {
				function.run();
			};

			add();
		}

		public void function(final Consumer<KeyEvent> function) {
			this.function = function;

			add();
		}

		private void addSingle() {
			keyHandlers.put(key, new SingleFunctionForKey(function));
		}

		private void add() {
			KeyHandler keyHandler = keyHandlers.get(key);
			if (keyHandler == null) {
				keyHandler = new ModifierBasedFunctionsForKey();
				keyHandlers.put(key, keyHandler);
			}

			keyHandler.setFunction(modifiersValue, function);
		}
	}

	private KeyHandlerBuilder key(final int key) {
		return new KeyHandlerBuilder(key);
	}

	private interface KeyHandler {
		void setFunction(final int modifiersValue, final Consumer<KeyEvent> function);

		void fireFunction(final KeyEvent e);
	}

	private class SingleFunctionForKey implements KeyHandler {
		public final Consumer<KeyEvent> function;

		public SingleFunctionForKey(final Consumer<KeyEvent> function) {
			this.function = function;
		}

		@Override
		public void setFunction(final int modifiersValue, final Consumer<KeyEvent> function) {
			throw new IllegalArgumentException("can't set function for modifiers for single function!");
		}

		@Override
		public void fireFunction(final KeyEvent e) {
			function.accept(e);
			e.consume();
		}
	}

	private class ModifierBasedFunctionsForKey implements KeyHandler {
		List<Consumer<KeyEvent>> functions = new ArrayList<>();

		public ModifierBasedFunctionsForKey() {
			for (int i = 0; i < 8; i++) {
				functions.add(emptyHandler);
			}
		}

		@Override
		public void setFunction(final int modifiersValue, final Consumer<KeyEvent> function) {
			functions.set(modifiersValue, function);
		}

		@Override
		public void fireFunction(final KeyEvent e) {
			functions.get(modifierKeysValue(ctrl, alt, shift)).accept(e);
			e.consume();
		}
	}

	private AudioDrawer audioDrawer;
	private AudioHandler audioHandler;
	private CopyManager copyManager;
	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private SelectionManager selectionManager;
	private SongFileHandler songFileHandler;
	private UndoSystem undoSystem;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;
	private boolean left = false;
	private boolean right = false;

	private int lastFretNumber = 0;
	private int fretNumberTimer = 0;

	public void init(final AudioDrawer audioDrawer, final AudioHandler audioHandler, final CopyManager copyManager,
			final ChartData data, final CharterFrame frame, final ModeManager modeManager,
			final MouseHandler mouseHandler, final SelectionManager selectionManager,
			final SongFileHandler songFileHandler, final UndoSystem undoSystem) {
		this.audioDrawer = audioDrawer;
		this.audioHandler = audioHandler;
		this.copyManager = copyManager;
		this.data = data;
		this.frame = frame;
		this.modeManager = modeManager;
		this.mouseHandler = mouseHandler;
		this.selectionManager = selectionManager;
		this.songFileHandler = songFileHandler;
		this.undoSystem = undoSystem;

		prepareHandlers();
	}

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		left = false;
		right = false;
	}

	public void clearFretNumber() {
		lastFretNumber = 0;
		fretNumberTimer = 0;
	}

	private void moveFromArrowKeys() {
		if (!left && !right) {
			return;
		}

		final int speed = (int) (Framer.frameLength * (shift ? 20 : 4) / (ctrl ? 4 : 1));
		int nextTime = data.nextTime - (left ? speed : 0) + (right ? speed : 0);
		nextTime = max(0, min(data.music.msLength(), nextTime));
		frame.setNextTime(nextTime);
	}

	private void decreaseNumberTimer() {
		fretNumberTimer--;
	}

	public void frame() {
		moveFromArrowKeys();
		decreaseNumberTimer();
	}

	public boolean alt() {
		return alt;
	}

	public boolean ctrl() {
		return ctrl;
	}

	public boolean shift() {
		return shift;
	}

	private void handleCtrl() {
		ctrl = true;
	}

	private void handleAlt(final KeyEvent e) {
		alt = true;
		e.consume();
	}

	private void handleShift() {
		shift = true;
	}

	private void handleLeft() {
		if (!alt) {
			left = true;
			return;
		}

		final int newTime = ctrl ? data.songChart.beatsMap.getPositionWithRemovedGrid(data.time, 1)//
				: findLastBefore(data.songChart.beatsMap.beats, data.time).position();
		frame.setNextTime(newTime);
	}

	private boolean containsString(final ArrayList2<Selection<ChordOrNote>> selectedSounds, final int string) {
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				if (sound.note.string == string) {
					return true;
				}
				continue;
			}

			if (chordTemplates.get(sound.chord.chordId).frets.get(string) != null) {
				return true;
			}
		}

		return false;
	}

	public void moveNotesUpKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, topString)) {
			return;
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string++;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			for (int string = topString - 1; string >= 0; string--) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string + 1, fret);
				newChordTemplate.fingers.put(string + 1, newChordTemplate.fingers.remove(string));
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}

		frame.selectionChanged();
	}

	public void moveNotesDownKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, 0)) {
			return;
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string--;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			for (int string = 1; string <= topString; string++) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string - 1, fret);
				newChordTemplate.fingers.put(string - 1, newChordTemplate.fingers.remove(string));
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}

		frame.selectionChanged();
	}

	public void moveNotesUp() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, topString)) {
			return;
		}

		final ArrangementChart arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 0; i <= topString - 1; i++) {
			stringDifferences.put(i, arrangement.tuning.getStringOffset(i) - arrangement.tuning.getStringOffset(i + 1));
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				final int newFret = sound.note.fret + stringDifferences.get(sound.note.string);
				if (newFret >= 0 && newFret <= Config.frets) {
					sound.note.fret = newFret;
					sound.note.string++;
				}
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = topString - 1; string >= 0; string--) {
				Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				fret = fret + stringDifferences.get(string);
				if (fret < 0 || fret > Config.frets) {
					wrongFret = true;
					break;
				}

				newChordTemplate.frets.put(string + 1, fret);
				newChordTemplate.fingers.put(string + 1, newChordTemplate.fingers.remove(string));
			}

			if (wrongFret) {
				continue;
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}

		frame.selectionChanged();
	}

	public void moveNotesDown() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, 0)) {
			return;
		}

		final ArrangementChart arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 1; i <= topString; i++) {
			stringDifferences.put(i, arrangement.tuning.getStringOffset(i) - arrangement.tuning.getStringOffset(i - 1));
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				final int newFret = sound.note.fret + stringDifferences.get(sound.note.string);
				if (newFret >= 0 && newFret <= Config.frets) {
					sound.note.fret = newFret;
					sound.note.string--;
				}
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = 1; string <= topString; string++) {
				Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				fret = fret + stringDifferences.get(string);
				if (fret < 0 || fret > Config.frets) {
					wrongFret = true;
					break;
				}

				newChordTemplate.frets.put(string - 1, fret);
				newChordTemplate.fingers.put(string - 1, newChordTemplate.fingers.remove(string));
			}

			if (wrongFret) {
				continue;
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}

		frame.selectionChanged();
	}

	private <T> void singleToggleOnAllSelectedNotesWithBaseValueNote(final Function<Note, T> baseValueGetter,
			final BiConsumer<Note, T> handler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		selected.removeIf(selection -> selection.selectable.isChord());
		final T baseValue = baseValueGetter.apply(selected.get(0).selectable.note);

		undoSystem.addUndo();
		selected.forEach(selectedValue -> handler.accept(selectedValue.selectable.note, baseValue));

		frame.selectionChanged();
	}

	private <T> void singleToggleOnAllSelectedNotesWithBaseValue(final Function<GuitarSound, T> baseValueGetter,
			final BiConsumer<GuitarSound, T> handler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		final T baseValue = baseValueGetter.apply(selected.get(0).selectable.asGuitarSound());

		undoSystem.addUndo();
		selected.forEach(selectedValue -> handler.accept(selectedValue.selectable.asGuitarSound(), baseValue));

		frame.selectionChanged();
	}

	public void toggleMute() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			switch (sound.mute) {
			case NONE:
				return Mute.PALM;
			case PALM:
				return Mute.FULL;
			case FULL:
				return Mute.NONE;
			default:
				return Mute.NONE;
			}
		}, (sound, mute) -> sound.mute = mute);
	}

	public void toggleHOPO() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			switch (sound.hopo) {
			case HAMMER_ON:
				return HOPO.PULL_OFF;
			case NONE:
				return HOPO.HAMMER_ON;
			case PULL_OFF:
				return HOPO.TAP;
			case TAP:
				return HOPO.NONE;
			default:
				return HOPO.NONE;
			}
		}, (sound, hopo) -> sound.hopo = hopo);
	}

	public void toggleHarmonic() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			switch (sound.harmonic) {
			case NONE:
				return Harmonic.NORMAL;
			case NORMAL:
				return Harmonic.PINCH;
			case PINCH:
				return Harmonic.NONE;
			default:
				return Harmonic.NONE;
			}
		}, (sound, harmonic) -> sound.harmonic = harmonic);
	}

	public void toggleAccent() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> !sound.accent,
				(sound, accent) -> sound.accent = accent);
	}

	public void toggleVibrato() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		this.singleToggleOnAllSelectedNotesWithBaseValueNote(note -> note.vibrato == null ? 80 : null,
				(note, vibrato) -> note.vibrato = vibrato);
	}

	public void toggleTremolo() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> !sound.tremolo,
				(sound, tremolo) -> sound.tremolo = tremolo);
	}

	public void toggleLinkNext() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> !sound.linkNext,
				(sound, linkNext) -> sound.linkNext = linkNext);
	}

	@SuppressWarnings("unchecked")
	public void delete() {
		boolean undoAdded = false;

		for (final PositionType type : PositionType.values()) {
			if (type == PositionType.NONE
					|| (type == PositionType.BEAT && modeManager.editMode != EditMode.TEMPO_MAP)) {
				continue;
			}

			final SelectionAccessor<Position> selectedTypeAccessor = selectionManager.getSelectedAccessor(type);
			if (selectedTypeAccessor.isSelected()) {
				if (!undoAdded) {
					undoSystem.addUndo();
					undoAdded = true;
				}

				final ArrayList2<Selection<IPosition>> selected = (ArrayList2<Selection<IPosition>>) (ArrayList2<?>) selectedTypeAccessor
						.getSortedSelected();
				final ArrayList2<IPosition> positions = type.getPositions(data);
				for (int i = selected.size() - 1; i >= 0; i--) {
					positions.remove(selected.get(i).id);
				}

				if (type == PositionType.BEAT) {
					data.songChart.beatsMap.fixFirstBeatInMeasures();
				}
				if (type == PositionType.TONE_CHANGE) {
					data.getCurrentArrangement().tones = data.getCurrentArrangement().toneChanges.stream()//
							.map(toneChange -> toneChange.toneName)//
							.collect(Collectors.toCollection(HashSet2::new));
				}

			}
		}

		selectionManager.clear();
	}

	private void moveToBeginning() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(0);
	}

	private void handleCtrlHome() {
		if (data.isEmpty) {
			return;
		}

		modeManager.getHandler().handleHome();
	}

	private void moveToEnd() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(data.songChart.beatsMap.songLengthMs);
	}

	private void handleCtrlEnd() {
		if (data.isEmpty) {
			return;
		}

		modeManager.getHandler().handleEnd();
	}

	private void handleRight() {
		if (!alt) {
			right = true;
			return;
		}

		if (data.isEmpty) {
			return;
		}

		final int newTime = ctrl ? data.songChart.beatsMap.getPositionWithAddedGrid(data.time, 1)//
				: findFirstAfter(data.songChart.beatsMap.beats, data.time).position();
		frame.setNextTime(newTime);
	}

	private int snap(final int position) {
		final GridPosition<Beat> gridPosition = GridPosition.create(data.songChart.beatsMap.beats, position);
		final int positionA = gridPosition.position();
		final int positionB = gridPosition.next().position();
		if (abs(position - positionA) < abs(position - positionB)) {
			return positionA;
		} else {
			return positionB;
		}
	}

	private void snapPositions(final Collection<? extends IPosition> positions) {
		for (final IPosition position : positions) {
			position.position(snap(position.position()));
		}
	}

	private void snapNotePositionsWithLength(final Collection<ChordOrNote> positions,
			final ArrayList2<ChordOrNote> allPositions) {
		for (final ChordOrNote position : positions) {
			position.position(snap(position.position()));
		}

		for (final ChordOrNote position : positions) {
			if (position.asGuitarSound().linkNext) {
				final ChordOrNote next = findFirstAfter(allPositions, position.position());
				position.length(next.position() - position.position());
				continue;
			}

			final int length = snap(position.endPosition()) - position.position();
			final ChordOrNote next = findFirstAfter(allPositions, position.position());
			if (next != null) {
				position.length(max(0, min(length, next.position() - position.position() - minNoteDistance)));
			} else {
				position.length(length);
			}
		}
	}

	private <T extends IPositionWithLength> void snapPositionsWithLength(final Collection<T> positions,
			final ArrayList2<T> allPositions) {
		for (final T position : positions) {
			position.position(snap(position.position()));

			final int length = snap(position.endPosition()) - position.position();
			final T next = findFirstAfter(allPositions, position.position());
			if (next != null) {
				position.length(max(0, min(length, next.position() - position.position() - minNoteDistance)));
			} else {
				position.length(length);
			}
		}
	}

	public void snapSelected() {
		final SelectionAccessor<IPosition> accessor = selectionManager.getCurrentlySelectedAccessor();

		switch (accessor.type) {
		case ANCHOR:
		case TONE_CHANGE:
			undoSystem.addUndo();
			snapPositions(accessor.getSelectedSet().map(selection -> selection.selectable));

			frame.selectionChanged();
			break;
		case GUITAR_NOTE:
			undoSystem.addUndo();
			snapNotePositionsWithLength(accessor.getSelectedSet().map(selection -> (ChordOrNote) selection.selectable),
					data.getCurrentArrangementLevel().chordsAndNotes);

			frame.selectionChanged();
			break;
		case HAND_SHAPE:
			undoSystem.addUndo();
			snapPositionsWithLength(accessor.getSelectedSet().map(selection -> (HandShape) selection.selectable),
					data.getCurrentArrangementLevel().handShapes);

			frame.selectionChanged();
			break;
		case VOCAL:
			undoSystem.addUndo();
			snapPositionsWithLength(accessor.getSelectedSet().map(selection -> (Vocal) selection.selectable),
					data.songChart.vocals.vocals);

			frame.selectionChanged();
			break;
		case BEAT:
		case NONE:
		default:
			break;
		}
	}

	public void snapAll() {
		final SelectionAccessor<IPosition> accessor = selectionManager.getCurrentlySelectedAccessor();
		if (!accessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<IPosition>> selected = accessor.getSortedSelected();
		final int from = selected.get(0).selectable.position();
		final int to = selected.getLast().selectable.position();

		if (modeManager.editMode == EditMode.TEMPO_MAP) {
			return;
		}

		if (modeManager.editMode == EditMode.VOCALS) {
			undoSystem.addUndo();
			snapPositionsWithLength(getFromTo(data.songChart.vocals.vocals, from, to), data.songChart.vocals.vocals);

			frame.selectionChanged();
			return;
		}

		if (modeManager.editMode == EditMode.GUITAR) {
			undoSystem.addUndo();
			final ArrangementChart arrangement = data.getCurrentArrangement();
			final Level level = data.getCurrentArrangementLevel();
			snapPositions(getFromTo(arrangement.toneChanges, from, to));
			snapPositions(getFromTo(level.anchors, from, to));
			snapNotePositionsWithLength(getFromTo(level.chordsAndNotes, from, to), level.chordsAndNotes);
			snapPositionsWithLength(getFromTo(level.handShapes, from, to), level.handShapes);

			frame.selectionChanged();
		}
	}

	public void editVocals() {
		if (data.isEmpty || modeManager.editMode != EditMode.VOCALS) {
			return;
		}

		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<Vocal>> selectedVocals = selectedAccessor.getSortedSelected();
		final Selection<Vocal> firstSelectedVocal = selectedVocals.remove(0);
		new VocalPane(firstSelectedVocal.id, firstSelectedVocal.selectable, data, frame, selectionManager, undoSystem,
				selectedVocals);
	}

	public void toggleWordPart() {
		if (data.isEmpty || modeManager.editMode != EditMode.VOCALS) {
			return;
		}

		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> vocalSelection : selectedAccessor.getSelectedSet()) {
			vocalSelection.selectable.setWordPart(!vocalSelection.selectable.isWordPart());
		}

		frame.selectionChanged();
	}

	public void togglePhraseEnd() {
		if (data.isEmpty || modeManager.editMode != EditMode.VOCALS) {
			return;
		}

		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> selectedVocal : selectedAccessor.getSelectedSet()) {
			selectedVocal.selectable.setPhraseEnd(!selectedVocal.selectable.isPhraseEnd());
		}

		frame.selectionChanged();
	}

	private void openChordOptionsPopup(final ArrayList2<ChordOrNote> selected) {
		new ChordOptionsPane(data, frame, undoSystem, selected);
	}

	private void openSingleNoteOptionsPopup(final ArrayList2<ChordOrNote> selected) {
		new NoteOptionsPane(data, frame, undoSystem, selected);
	}

	public void editNoteAsChord() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		openChordOptionsPopup(selectedAccessor.getSortedSelected().map(selection -> selection.selectable));
	}

	public void editNoteAsSingleNote() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		openSingleNoteOptionsPopup(selectedAccessor.getSortedSelected().map(selection -> selection.selectable));
	}

	public void editNote() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);

		final ArrayList2<ChordOrNote> selected = selectedAccessor.getSortedSelected()
				.map(selection -> selection.selectable);
		if (selected.isEmpty()) {
			return;
		}

		if (selected.get(0).isChord()) {
			openChordOptionsPopup(selected);
		} else {
			openSingleNoteOptionsPopup(selected);
		}
	}

	public void editSlide() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		new SlidePane(frame, undoSystem, selectedAccessor.getSortedSelected().get(0).selectable);
	}

	public void editBend() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ChordOrNote sound = selectedAccessor.getSortedSelected().get(0).selectable;
		if (sound.length() < 10) {
			return;
		}

		if (sound.isChord()) {
			new ChordBendPane(data.songChart.beatsMap, frame, undoSystem, sound.chord,
					data.getCurrentArrangement().chordTemplates.get(sound.chord.chordId));
		} else if (sound.isNote()) {
			new NoteBendPane(data.songChart.beatsMap, frame, undoSystem, sound.note);
		}
	}

	public void editHandShape() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<HandShape> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.HAND_SHAPE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		new HandShapePane(data, frame, selectedAccessor.getSortedSelected().get(0).selectable, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	public void markHandShape() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		final ArrayList2<Selection<ChordOrNote>> selected = selectionAccessor.getSortedSelected();
		final int position = selected.get(0).selectable.position();
		final int endPosition = selected.getLast().selectable.endPosition();

		int deleteFromId = findLastIdBefore(handShapes, position);
		if (deleteFromId == -1) {
			deleteFromId = 0;
		}
		if (handShapes.size() > deleteFromId && handShapes.get(deleteFromId).endPosition() < position) {
			deleteFromId++;
		}

		final int firstIdAfter = findFirstIdAfter(handShapes, endPosition);
		final int deleteToId = firstIdAfter == -1 ? handShapes.size() - 1 : firstIdAfter - 1;
		for (int i = deleteToId; i >= deleteFromId; i--) {
			handShapes.remove(i);
		}

		ChordTemplate chordTemplate = new ChordTemplate();
		if (selected.get(0).selectable.isChord()) {
			chordTemplate = data.getCurrentArrangement().chordTemplates.get(selected.get(0).selectable.chord.chordId);
		}

		final HandShape handShape = new HandShape(position, endPosition - position);
		handShape.chordId = data.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);

		handShapes.add(handShape);
		handShapes.sort(null);
		new HandShapePane(data, frame, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void handleE() {
		if (data.isEmpty) {
			return;
		}

		if (modeManager.editMode == EditMode.GUITAR) {
			editNoteAsSingleNote();
			return;
		}

		if (modeManager.editMode == EditMode.VOCALS) {
			togglePhraseEnd();
		}
	}

	private void handleL() {
		if (modeManager.editMode == EditMode.VOCALS) {
			editVocals();
		}

		if (modeManager.editMode == EditMode.GUITAR) {
			toggleLinkNext();
		}
	}

	private void handleW() {
		if (modeManager.editMode == EditMode.VOCALS) {
			toggleWordPart();
		}

		if (modeManager.editMode == EditMode.GUITAR) {
			editNote();
		}
	}

	public void doubleGridSize() {
		if (Config.gridSize <= 512) {
			Config.gridSize *= 2;
			Config.markChanged();
		}
	}

	public void halveGridSize() {
		if (Config.gridSize % 2 == 0) {
			Config.gridSize /= 2;
			Config.markChanged();
		}
	}

	public void setFret(final int fret) {
		if (data.isEmpty) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final ArrayList2<Selection<ChordOrNote>> selected = selectionAccessor.getSortedSelected();
		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isChord()) {
				final Chord chord = selection.selectable.chord;
				final ChordTemplate newTemplate = new ChordTemplate(
						data.getCurrentArrangement().chordTemplates.get(chord.chordId));
				int fretChange = 0;
				for (int i = 0; i < data.currentStrings(); i++) {
					if (newTemplate.frets.get(i) != null) {
						fretChange = fret - newTemplate.frets.get(i);
						break;
					}
				}
				if (fretChange == 0) {
					continue;
				}

				for (final int string : newTemplate.frets.keySet()) {
					final int oldFret = newTemplate.frets.get(string);
					final int newFret = max(0, min(Config.frets, oldFret + fretChange));

					newTemplate.frets.put(string, newFret);
					if (newFret == 0) {
						newTemplate.fingers.remove(string);
					}
				}

				newTemplate.chordName = ChordNameSuggester
						.suggestChordNames(data.getCurrentArrangement().tuning, newTemplate.frets).get(0);
				chord.chordId = data.getCurrentArrangement().getChordTemplateIdWithSave(newTemplate);
			} else {
				selection.selectable.note.fret = fret;
			}
		}

		frame.selectionChanged();
	}

	private void handleNumber(final int number) {
		if (data.isEmpty) {
			return;
		}

		if (nanoTime() / 1_000_000 <= fretNumberTimer && lastFretNumber * 10 + number <= frets) {
			fretNumberTimer = (int) (nanoTime() / 1_000_000 + 2000);
			lastFretNumber = lastFretNumber * 10 + number;
			setFret(lastFretNumber);
			return;
		}

		fretNumberTimer = (int) (nanoTime() / 1_000_000 + 2000);
		lastFretNumber = number;
		setFret(number);
	}

	private final Map<Integer, KeyHandler> keyHandlers = new HashMap<>();

	private void prepareHandlers() {
		key(VK_CONTROL).singleFunction(this::handleCtrl);
		key(VK_ALT).singleFunction(this::handleAlt);
		key(VK_SHIFT).singleFunction(this::handleShift);

		key(VK_ESCAPE).function(frame::exit);
		key(VK_DELETE).function(this::delete);
		key(VK_HOME).function(this::moveToBeginning);
		key(VK_HOME).ctrl().function(this::handleCtrlHome);
		key(VK_END).function(this::moveToEnd);
		key(VK_END).ctrl().function(this::handleCtrlEnd);

		key(VK_SPACE).function(audioHandler::togglePlayNormalSpeed);
		key(VK_SPACE).ctrl().function(audioHandler::togglePlaySetSpeed);
		key(VK_LEFT).singleFunction(this::handleLeft);
		key(VK_RIGHT).singleFunction(this::handleRight);
		key(VK_UP).function(this::moveNotesUpKeepFrets);
		key(VK_UP).ctrl().function(this::moveNotesUp);
		key(VK_DOWN).function(this::moveNotesDownKeepFrets);
		key(VK_DOWN).ctrl().function(this::moveNotesDown);

		key(VK_A).function(this::toggleAccent);
		key(VK_A).ctrl().function(selectionManager::selectAllNotes);
		key(VK_B).function(this::editBend);
		key(VK_C).ctrl().function(copyManager::copy);
		key(VK_E).function(this::handleE);
		key(VK_G).function(e -> new GridPane(frame));
		key(VK_G).ctrl().function(this::snapSelected);
		key(VK_G).ctrl().shift().function(this::snapAll);
		key(VK_H).function(this::toggleHOPO);
		key(VK_H).ctrl().function(this::editHandShape);
		key(VK_H).shift().function(this::markHandShape);
		key(VK_L).function(this::handleL);
		key(VK_M).function(this::toggleMute);
		key(VK_N).function(this::editNote);
		key(VK_N).ctrl().function(songFileHandler::newSong);
		key(VK_O).function(this::toggleHarmonic);
		key(VK_O).ctrl().function((Runnable) songFileHandler::open);
		key(VK_Q).ctrl().function(this::editNoteAsChord);
		key(VK_R).ctrl().function(undoSystem::redo);
		key(VK_S).function(this::editSlide);
		key(VK_S).ctrl().function(songFileHandler::save);
		key(VK_S).ctrl().shift().function(songFileHandler::saveAs);
		key(VK_T).function(this::toggleTremolo);
		key(VK_V).function(this::toggleVibrato);
		key(VK_V).ctrl().function(copyManager::paste);
		key(VK_V).ctrl().shift().function(copyManager::specialPaste);
		key(VK_W).function(this::handleW);
		key(VK_Z).ctrl().function(undoSystem::undo);

		key(VK_COMMA).function(this::halveGridSize);
		key(VK_PERIOD).function(this::doubleGridSize);

		key(VK_1).singleFunction(e -> handleNumber(1));
		key(VK_2).singleFunction(e -> handleNumber(2));
		key(VK_3).singleFunction(e -> handleNumber(3));
		key(VK_4).singleFunction(e -> handleNumber(4));
		key(VK_5).singleFunction(e -> handleNumber(5));
		key(VK_6).singleFunction(e -> handleNumber(6));
		key(VK_7).singleFunction(e -> handleNumber(7));
		key(VK_8).singleFunction(e -> handleNumber(8));
		key(VK_9).singleFunction(e -> handleNumber(9));
		key(VK_0).singleFunction(e -> handleNumber(0));

		key(VK_NUMPAD0).singleFunction(e -> handleNumber(0));
		key(VK_NUMPAD1).singleFunction(e -> handleNumber(1));
		key(VK_NUMPAD2).singleFunction(e -> handleNumber(2));
		key(VK_NUMPAD3).singleFunction(e -> handleNumber(3));
		key(VK_NUMPAD4).singleFunction(e -> handleNumber(4));
		key(VK_NUMPAD5).singleFunction(e -> handleNumber(5));
		key(VK_NUMPAD6).singleFunction(e -> handleNumber(6));
		key(VK_NUMPAD7).singleFunction(e -> handleNumber(7));
		key(VK_NUMPAD8).singleFunction(e -> handleNumber(8));
		key(VK_NUMPAD9).singleFunction(e -> handleNumber(9));

		key(VK_F3).singleFunction(audioHandler::toggleClaps);
		key(VK_F4).singleFunction(audioHandler::toggleMetronome);
		key(VK_F5).singleFunction(audioDrawer::toggle);
	}

	private static final List<Integer> keysNotClearingMousePressesOnPress = asList(//
			VK_CONTROL, //
			VK_ALT, //
			VK_SHIFT, //
			VK_CAPS_LOCK, //
			VK_LEFT, //
			VK_RIGHT);
	private static final List<Integer> keysNotStoppingMusicOnPress = asList(//
			VK_CONTROL, //
			VK_ALT, //
			VK_SHIFT, //
			VK_CAPS_LOCK, //
			VK_F3, //
			VK_F4, //
			VK_F5, //
			VK_SPACE, //
			VK_OPEN_BRACKET, //
			VK_CLOSE_BRACKET);

	private void keyUsed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UNDEFINED) {
			return;
		}

		if (!keysNotClearingMousePressesOnPress.contains(keyCode)) {
			mouseHandler.cancelAllActions();
		}

		if (!keysNotStoppingMusicOnPress.contains(keyCode)) {
			audioHandler.stopMusic();
		}

		final KeyHandler keyHandler = keyHandlers.get(keyCode);
		if (keyHandler == null) {
			return;
		}

		keyHandler.fireFunction(e);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F10) {
			return;
		}

		keyUsed(e);
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left = false;
			break;
		case KeyEvent.VK_RIGHT:
			right = false;
			break;
		case KeyEvent.VK_CONTROL:
			ctrl = false;
			break;
		case KeyEvent.VK_ALT:
			alt = false;
			break;
		case KeyEvent.VK_SHIFT:
			shift = false;
			break;
		case KeyEvent.VK_F10:
			keyUsed(e);
			break;
		default:
			break;
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
