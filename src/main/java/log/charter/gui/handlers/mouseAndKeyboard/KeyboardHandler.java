package log.charter.gui.handlers.mouseAndKeyboard;

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
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_END;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_F11;
import static java.awt.event.KeyEvent.VK_F12;
import static java.awt.event.KeyEvent.VK_F2;
import static java.awt.event.KeyEvent.VK_F3;
import static java.awt.event.KeyEvent.VK_F4;
import static java.awt.event.KeyEvent.VK_F5;
import static java.awt.event.KeyEvent.VK_F6;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_HOME;
import static java.awt.event.KeyEvent.VK_L;
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
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_V;
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_Z;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static log.charter.data.ArrangementFixer.fixSoundLength;
import static log.charter.data.config.Config.frets;
import static log.charter.song.notes.IConstantPosition.findFirstAfter;
import static log.charter.song.notes.IConstantPosition.findFirstIdAfter;
import static log.charter.song.notes.IConstantPosition.findLastBefore;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;
import static log.charter.song.notes.IConstantPosition.getFromTo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.RepeatManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.gui.chartPanelDrawers.common.WaveFormDrawer;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.panes.songEdits.HandShapePane;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.song.Arrangement;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IConstantPosition;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.IPositionWithLength;
import log.charter.song.notes.Note;
import log.charter.song.notes.Position;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.chordRecognition.ChordNameSuggester;

public class KeyboardHandler implements KeyListener {
	private static Consumer<KeyEvent> emptyHandler = e -> {};

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

		public void function(final Runnable function) {
			this.function = e -> function.run();
			add();
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

	private WaveFormDrawer audioDrawer;
	private AudioHandler audioHandler;
	private ArrangementFixer arrangementFixer;
	private ChartToolbar chartToolbar;
	private CopyManager copyManager;
	private ChartData data;
	private CharterFrame frame;
	private Framer framer;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private RepeatManager repeatManager;
	private SelectionManager selectionManager;
	private SongFileHandler songFileHandler;
	private UndoSystem undoSystem;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;
	private Action heldAction = null;

	private int lastFretNumber = 0;
	private int fretNumberTimer = 0;

	public void init(final WaveFormDrawer audioDrawer, final AudioHandler audioHandler,
			final ArrangementFixer arrangementFixer, final ChartToolbar chartToolbar, final CopyManager copyManager,
			final ChartData data, final CharterFrame frame, final Framer framer, final ModeManager modeManager,
			final MouseHandler mouseHandler, final RepeatManager repeatManager, final SelectionManager selectionManager,
			final SongFileHandler songFileHandler, final UndoSystem undoSystem) {
		this.audioDrawer = audioDrawer;
		this.audioHandler = audioHandler;
		this.arrangementFixer = arrangementFixer;
		this.chartToolbar = chartToolbar;
		this.copyManager = copyManager;
		this.data = data;
		this.frame = frame;
		this.framer = framer;
		this.modeManager = modeManager;
		this.mouseHandler = mouseHandler;
		this.repeatManager = repeatManager;
		this.selectionManager = selectionManager;
		this.songFileHandler = songFileHandler;
		this.undoSystem = undoSystem;

		prepareHandlers();
	}

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		heldAction = null;
	}

	public void clearFretNumber() {
		lastFretNumber = 0;
		fretNumberTimer = 0;
	}

	private void moveFromArrowKeys() {
		if (heldAction == null) {
			return;
		}

		double speed;
		switch (heldAction) {
			case FAST_LEFT:
				speed = -framer.frameLength * 32;
				break;
			case FAST_RIGHT:
				speed = framer.frameLength * 32;
				break;
			case LEFT:
				speed = -framer.frameLength * 4;
				break;
			case RIGHT:
				speed = framer.frameLength * 4;
				break;
			case SLOW_LEFT:
				speed = -framer.frameLength;
				break;
			case SLOW_RIGHT:
				speed = framer.frameLength;
				break;
			default:
				return;
		}

		int nextTime = data.nextTime + (int) speed;
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

	private int getPrevious(final List<? extends IConstantPosition> positions) {
		final IConstantPosition position = findLastBefore(positions, data.time);
		if (position == null) {
			return data.time;
		}

		return position.position();
	}

	public void handlePreviousBeat() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(getPrevious(data.songChart.beatsMap.beats));
	}

	public void handlePreviousGrid() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(data.songChart.beatsMap.getPositionWithRemovedGrid(data.time, 1));
	}

	public void handlePreviousSound() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(getPrevious(data.getCurrentArrangementLevel().sounds));
	}

	private int getNext(final ArrayList2<? extends IConstantPosition> positions) {
		final IConstantPosition position = findFirstAfter(positions, data.time);
		if (position == null) {
			return data.time;
		}

		return position.position();
	}

	public void handleNextBeat() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(getNext(data.songChart.beatsMap.beats));
	}

	public void handleNextGrid() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(data.songChart.beatsMap.getPositionWithAddedGrid(data.time, 1));
	}

	public void handleNextSound() {
		if (data.isEmpty) {
			return;
		}

		frame.setNextTime(getNext(data.getCurrentArrangementLevel().sounds));
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

			if (chordTemplates.get(sound.chord.templateId()).frets.get(string) != null) {
				return true;
			}
		}

		return false;
	}

	private void moveChordNotesUp(final int strings, final Map<Integer, ChordNote> chordNotes) {
		chordNotes.remove(strings - 1);
		for (int string = strings - 2; string >= 0; string--) {
			final ChordNote movedChordNote = chordNotes.remove(string);
			if (movedChordNote != null) {
				chordNotes.put(string + 1, movedChordNote);
			}
		}
	}

	private void moveChordNotesDown(final int strings, final Map<Integer, ChordNote> chordNotes) {
		chordNotes.remove(0);
		for (int string = 1; string < strings; string++) {
			final ChordNote movedChordNote = chordNotes.remove(string);
			if (movedChordNote != null) {
				chordNotes.put(string - 1, movedChordNote);
			}
		}
	}

	public void moveNotesUpKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int strings = data.currentStrings();
		if (containsString(selectedSounds, strings - 1)) {
			return;
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final Arrangement arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string++;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesUp(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			for (int string = strings - 2; string >= 0; string--) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string + 1, fret);
				newChordTemplate.fingers.put(string + 1, newChordTemplate.fingers.remove(string));
			}

			moveChordNotesUp(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	public void moveNotesDownKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		if (containsString(selectedSounds, 0)) {
			return;
		}

		undoSystem.addUndo();

		final int strings = data.currentStrings();
		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final Arrangement arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string--;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesDown(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			for (int string = 1; string < strings; string++) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string - 1, fret);
				newChordTemplate.fingers.put(string - 1, newChordTemplate.fingers.remove(string));
			}

			moveChordNotesDown(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	public void moveNotesUp() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int strings = data.currentStrings();
		if (containsString(selectedSounds, strings - 1)) {
			return;
		}

		final Arrangement arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 0; i < strings; i++) {
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

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesUp(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = strings - 2; string >= 0; string--) {
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

			moveChordNotesUp(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	public void moveNotesDown() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		if (containsString(selectedSounds, 0)) {
			return;
		}

		final int strings = data.currentStrings();
		final Arrangement arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 1; i < strings; i++) {
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

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesDown(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);

				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = 1; string < strings; string++) {
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

			moveChordNotesDown(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	private <T> void singleToggleOnAllSelectedNotesWithBaseValue(final Function<ChordOrNote, T> baseValueGetter,
			final BiConsumer<ChordOrNote, T> handler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		final T baseValue = baseValueGetter.apply(selected.get(0).selectable);

		undoSystem.addUndo();
		selected.forEach(selectedValue -> handler.accept(selectedValue.selectable, baseValue));

		frame.selectionChanged(false);
	}

	public void toggleMute() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			Mute mute;
			if (sound.isNote()) {
				mute = sound.note.mute;
			} else {
				mute = sound.chord.chordNotesValue(n -> n.mute, Mute.NONE);
			}
			switch (mute) {
				case NONE:
					return Mute.PALM;
				case PALM:
					return Mute.FULL;
				case FULL:
					return Mute.NONE;
				default:
					return Mute.NONE;
			}
		}, (sound, mute) -> {
			if (sound.isNote()) {
				sound.note.mute = mute;
			} else {
				sound.chord.chordNotes.values().forEach(n -> n.mute = mute);
			}
		});
	}

	public void toggleHOPO() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			HOPO hopo;
			if (sound.isNote()) {
				hopo = sound.note.hopo;
			} else {
				hopo = sound.chord.chordNotesValue(n -> n.hopo, HOPO.NONE);
			}

			switch (hopo) {
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
		}, (sound, hopo) -> {
			if (sound.isNote()) {
				sound.note.hopo = hopo;
			} else {
				sound.chord.chordNotes.values().forEach(n -> n.hopo = hopo);
			}
		});
	}

	public void toggleHarmonic() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			Harmonic harmonic;
			if (sound.isNote()) {
				harmonic = sound.note.harmonic;
			} else {
				harmonic = sound.chord.chordNotesValue(n -> n.harmonic, Harmonic.NONE);
			}

			switch (harmonic) {
				case NONE:
					return Harmonic.NORMAL;
				case NORMAL:
					return Harmonic.PINCH;
				case PINCH:
					return Harmonic.NONE;
				default:
					return Harmonic.NONE;
			}
		}, (sound, harmonic) -> {
			if (sound.isNote()) {
				sound.note.harmonic = harmonic;
			} else {
				sound.chord.chordNotes.values().forEach(n -> n.harmonic = harmonic);
			}
		});
	}

	public void toggleAccent() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> !sound.asGuitarSound().accent,
				(sound, accent) -> sound.asGuitarSound().accent = accent);
	}

	public void toggleVibrato() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			if (sound.isNote()) {
				return !sound.note.vibrato;
			}

			return !sound.chord.chordNotesValue(n -> n.vibrato, false);
		}, (sound, vibrato) -> {
			if (sound.isNote()) {
				sound.note.vibrato = vibrato;
			} else {
				sound.chord.chordNotes.values().forEach(n -> n.vibrato = vibrato);
			}
		});
	}

	public void toggleTremolo() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> {
			if (sound.isNote()) {
				return !sound.note.tremolo;
			}

			return !sound.chord.chordNotesValue(n -> n.tremolo, false);
		}, (sound, tremolo) -> {
			if (sound.isNote()) {
				sound.note.tremolo = tremolo;
			} else {
				sound.chord.chordNotes.values().forEach(n -> n.tremolo = tremolo);
			}
		});
	}

	public void toggleLinkNext() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		final ChordOrNote sound = selected.get(0).selectable;
		final boolean newValue = sound.isNote() ? !sound.note.linkNext : !sound.chord.linkNext();
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;

		undoSystem.addUndo();
		selected.forEach(selectedValue -> {
			if (selectedValue.selectable.isNote()) {
				final Note note = selectedValue.selectable.note;
				note.linkNext = newValue;
				final ChordOrNote nextSound = ChordOrNote.findNextSoundOnString(note.string, selectedValue.id + 1,
						sounds);
				if (nextSound != null && nextSound.isNote()) {
					nextSound.note.fret = note.slideTo == null ? note.fret : note.slideTo;
				}
			} else {
				selectedValue.selectable.chord.chordNotes.values().forEach(n -> n.linkNext = newValue);
			}

			final int nextId = selectedValue.id + 1;
			if (nextId < sounds.size()) {
				final ChordOrNote nextSound = sounds.get(nextId);
				if (nextSound.isChord()) {
					nextSound.chord.splitIntoNotes = true;
				}
			}

			fixSoundLength(selectedValue.id, sounds);
		});

		frame.selectionChanged(false);
	}

	@SuppressWarnings("unchecked")
	public void delete() {
		boolean undoAdded = false;

		for (final PositionType type : PositionType.values()) {
			if (type == PositionType.NONE
					|| (type == PositionType.BEAT && modeManager.getMode() != EditMode.TEMPO_MAP)) {
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

	private void snapPositions(final Collection<? extends IPosition> positions) {
		for (final IPosition position : positions) {
			final int newPosition = data.songChart.beatsMap.getPositionFromGridClosestTo(position.position());
			position.position(newPosition);
		}
	}

	private void snapNotePositions(final Collection<ChordOrNote> positions) {
		snapPositions(positions);

		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;
		for (int i = 1; i < sounds.size(); i++) {
			while (i < sounds.size() && sounds.get(i).position() == sounds.get(i - 1).position()) {
				sounds.remove(i);
			}
		}

		arrangementFixer.fixNoteLengths(sounds);
	}

	private <T extends IPositionWithLength> void snapPositionsWithLength(final Collection<T> positions,
			final ArrayList2<T> allPositions) {
		snapPositions(positions);
		arrangementFixer.fixLengths(allPositions);
	}

	private void reselectAfterSnapping(final PositionType type, final Collection<Selection<IPosition>> selected) {
		final Set<Integer> selectedPositions = selected.stream()//
				.map(selection -> selection.selectable.position())//
				.collect(Collectors.toSet());

		selectionManager.clear();
		selectionManager.addSelectionForPositions(type, selectedPositions);
	}

	public void snapSelected() {
		final SelectionAccessor<IPosition> accessor = selectionManager.getCurrentlySelectedAccessor();
		if (!accessor.isSelected() || !asList(PositionType.EVENT_POINT, PositionType.TONE_CHANGE, PositionType.ANCHOR,
				PositionType.GUITAR_NOTE, PositionType.HAND_SHAPE, PositionType.VOCAL).contains(accessor.type)) {
			return;
		}

		undoSystem.addUndo();

		final HashSet2<Selection<IPosition>> selected = accessor.getSelectedSet();

		switch (accessor.type) {
			case EVENT_POINT:
			case ANCHOR:
			case TONE_CHANGE:
				snapPositions(selected.map(selection -> selection.selectable));
				break;
			case GUITAR_NOTE:
				snapNotePositions(selected.map(selection -> (ChordOrNote) selection.selectable));
				break;
			case HAND_SHAPE:
				snapPositionsWithLength(selected.map(selection -> (HandShape) selection.selectable),
						data.getCurrentArrangementLevel().handShapes);
				break;
			case VOCAL:
				snapPositionsWithLength(selected.map(selection -> (Vocal) selection.selectable),
						data.songChart.vocals.vocals);
				break;
			default:
				break;
		}

		reselectAfterSnapping(accessor.type, selected);
	}

	public void snapAll() {
		final SelectionAccessor<IPosition> accessor = selectionManager.getCurrentlySelectedAccessor();
		if (!accessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<IPosition>> selected = accessor.getSortedSelected();
		final int from = selected.get(0).selectable.position();
		final int to = selected.getLast().selectable.position();

		if (modeManager.getMode() == EditMode.TEMPO_MAP) {
			return;
		}

		if (modeManager.getMode() == EditMode.VOCALS) {
			undoSystem.addUndo();

			snapPositionsWithLength(getFromTo(data.songChart.vocals.vocals, from, to), data.songChart.vocals.vocals);

			reselectAfterSnapping(accessor.type, selected);
			return;
		}

		if (modeManager.getMode() == EditMode.GUITAR) {
			undoSystem.addUndo();

			final Arrangement arrangement = data.getCurrentArrangement();
			final Level level = data.getCurrentArrangementLevel();
			snapPositions(getFromTo(arrangement.eventPoints, from, to));
			snapPositions(getFromTo(arrangement.toneChanges, from, to));
			snapPositions(getFromTo(level.anchors, from, to));
			snapNotePositions(getFromTo(level.sounds, from, to));
			snapPositionsWithLength(getFromTo(level.handShapes, from, to), level.handShapes);

			reselectAfterSnapping(accessor.type, selected);
		}
	}

	public void editVocals() {
		if (data.isEmpty || modeManager.getMode() != EditMode.VOCALS) {
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
		if (data.isEmpty || modeManager.getMode() != EditMode.VOCALS) {
			return;
		}

		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> vocalSelection : selectedAccessor.getSelectedSet()) {
			vocalSelection.selectable.setPhraseEnd(false);
			vocalSelection.selectable.setWordPart(!vocalSelection.selectable.isWordPart());
		}

		frame.selectionChanged(false);
	}

	public void togglePhraseEnd() {
		if (data.isEmpty || modeManager.getMode() != EditMode.VOCALS) {
			return;
		}

		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> selectedVocal : selectedAccessor.getSelectedSet()) {
			selectedVocal.selectable.setWordPart(false);
			selectedVocal.selectable.setPhraseEnd(!selectedVocal.selectable.isPhraseEnd());
		}

		frame.selectionChanged(false);
	}

	public void markHandShape() {
		if (data.isEmpty || modeManager.getMode() != EditMode.GUITAR) {
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
			chordTemplate = data.getCurrentArrangement().chordTemplates
					.get(selected.get(0).selectable.chord.templateId());
		}

		final HandShape handShape = new HandShape(position, endPosition - position);
		handShape.templateId = data.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);

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

		if (modeManager.getMode() == EditMode.VOCALS) {
			togglePhraseEnd();
		}
	}

	private void handleL() {
		if (modeManager.getMode() == EditMode.VOCALS) {
			editVocals();
		}

		if (modeManager.getMode() == EditMode.GUITAR) {
			toggleLinkNext();
		}
	}

	private void handleW() {
		if (modeManager.getMode() == EditMode.VOCALS) {
			toggleWordPart();
		}
	}

	public void doubleGridSize() {
		if (Config.gridSize <= 512) {
			Config.gridSize *= 2;
			Config.markChanged();

			chartToolbar.updateValues();
		}
	}

	public void halveGridSize() {
		if (Config.gridSize % 2 == 0) {
			Config.gridSize /= 2;
			Config.markChanged();

			chartToolbar.updateValues();
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
						data.getCurrentArrangement().chordTemplates.get(chord.templateId()));
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

				final ArrayList2<String> suggestedNames = ChordNameSuggester
						.suggestChordNames(data.getCurrentArrangement().tuning, newTemplate.frets);

				if (!suggestedNames.isEmpty()) {
					newTemplate.chordName = suggestedNames.get(0);
				} else {
					newTemplate.chordName = "";
				}
				final int newTemplateId = data.getCurrentArrangement().getChordTemplateIdWithSave(newTemplate);
				chord.updateTemplate(newTemplateId, newTemplate);
			} else {
				selection.selectable.note.fret = fret;
			}
		}

		frame.selectionChanged(false);
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

	private void toggleBookmark(final int number) {
		if (data.isEmpty) {
			return;
		}

		final Integer currentBookmark = data.songChart.bookmarks.get(number);
		if (currentBookmark == null || currentBookmark != data.time) {
			data.songChart.bookmarks.put(number, data.time);
		} else {
			data.songChart.bookmarks.remove(number);
		}
	}

	private void moveToBookmark(final int number) {
		if (data.isEmpty) {
			return;
		}

		final Integer bookmark = data.songChart.bookmarks.get(number);
		if (bookmark == null) {
			return;
		}

		data.setNextTime(bookmark);
	}

	private final Map<Action, Runnable> actionHandlers = new HashMap<>();
	private final Map<Integer, KeyHandler> keyHandlers = new HashMap<>();

	private void prepareHandlers() {
		actionHandlers.put(Action.DELETE, this::delete);
		actionHandlers.put(Action.NEXT_BEAT, this::handleNextBeat);
		actionHandlers.put(Action.NEXT_GRID, this::handleNextGrid);
		actionHandlers.put(Action.NEXT_SOUND, this::handleNextSound);
		actionHandlers.put(Action.PLAY_AUDIO, audioHandler::togglePlaySetSpeed);
		actionHandlers.put(Action.PREVIOUS_BEAT, this::handlePreviousBeat);
		actionHandlers.put(Action.PREVIOUS_GRID, this::handlePreviousGrid);
		actionHandlers.put(Action.PREVIOUS_SOUND, this::handlePreviousSound);

		key(VK_ESCAPE).function(frame::exit);
		key(VK_HOME).function(this::moveToBeginning);
		key(VK_HOME).ctrl().function(this::handleCtrlHome);
		key(VK_END).function(this::moveToEnd);
		key(VK_END).ctrl().function(this::handleCtrlEnd);

		key(VK_UP).function(this::moveNotesUpKeepFrets);
		key(VK_UP).ctrl().function(this::moveNotesUp);
		key(VK_DOWN).function(this::moveNotesDownKeepFrets);
		key(VK_DOWN).ctrl().function(this::moveNotesDown);

		key(VK_A).function(this::toggleAccent);
		key(VK_A).ctrl().function(selectionManager::selectAllNotes);
		key(VK_C).ctrl().function(copyManager::copy);
		key(VK_E).function(this::handleE);
		key(VK_G).ctrl().function(this::snapSelected);
		key(VK_G).ctrl().shift().function(this::snapAll);
		key(VK_H).function(this::toggleHOPO);
		key(VK_H).shift().function(this::markHandShape);
		key(VK_L).function(this::handleL);
		key(VK_M).function(this::toggleMute);
		key(VK_N).ctrl().function(songFileHandler::newSong);
		key(VK_O).function(this::toggleHarmonic);
		key(VK_O).ctrl().function(songFileHandler::open);
		key(VK_R).ctrl().function(undoSystem::redo);
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
		key(VK_OPEN_BRACKET).function(() -> repeatManager.toggleRepeatStart(data.time));
		key(VK_CLOSE_BRACKET).function(() -> repeatManager.toggleRepeatEnd(data.time));

		final int[][] numberKeys = new int[10][];
		numberKeys[0] = new int[] { VK_0, VK_NUMPAD0 };
		numberKeys[1] = new int[] { VK_1, VK_NUMPAD1 };
		numberKeys[2] = new int[] { VK_2, VK_NUMPAD2 };
		numberKeys[3] = new int[] { VK_3, VK_NUMPAD3 };
		numberKeys[4] = new int[] { VK_4, VK_NUMPAD4 };
		numberKeys[5] = new int[] { VK_5, VK_NUMPAD5 };
		numberKeys[6] = new int[] { VK_6, VK_NUMPAD6 };
		numberKeys[7] = new int[] { VK_7, VK_NUMPAD7 };
		numberKeys[8] = new int[] { VK_8, VK_NUMPAD8 };
		numberKeys[9] = new int[] { VK_9, VK_NUMPAD9 };

		for (int i = 0; i <= 9; i++) {
			final int number = i;
			for (final int key : numberKeys[number]) {
				key(key).function(() -> handleNumber(number));
				key(key).ctrl().function(() -> toggleBookmark(number));
				key(key).shift().function(() -> moveToBookmark(number));
			}
		}

		key(VK_F2).function(audioHandler::toggleMidiNotes);
		key(VK_F3).function(audioHandler::toggleClaps);
		key(VK_F4).function(audioHandler::toggleMetronome);
		key(VK_F5).function(audioDrawer::toggle);
		key(VK_F6).function(repeatManager::toggle);
		key(VK_F11).function(frame::switchWindowedPreview);
		key(VK_F12).function(frame::switchBorderlessWindowedPreview);
	}

	private static final List<Integer> keysNotStoppingMusicOnPress = asList(//
			VK_F2, //
			VK_F3, //
			VK_F4, //
			VK_F5);

	private static final List<Action> actionsNotClearingMousePress = asList(//
			Action.FAST_LEFT, //
			Action.FAST_RIGHT, //
			Action.LEFT, //
			Action.PREVIOUS_BEAT, //
			Action.PREVIOUS_GRID, //
			Action.PREVIOUS_SOUND, //
			Action.RIGHT, //
			Action.SLOW_LEFT, //
			Action.SLOW_RIGHT, //

			Action.LEFT);
	private static final List<Action> actionsNotStoppingAudio = asList(//
			Action.PLAY_AUDIO);

	private void fireAction(final Action action) {
		if (!actionsNotClearingMousePress.contains(action)) {
			mouseHandler.cancelAllActions();
		}
		if (!actionsNotStoppingAudio.contains(action)) {
			audioHandler.stopMusic();
		}

		final Runnable actionHandler = actionHandlers.get(action);
		if (actionHandler != null) {
			actionHandler.run();
		}
	}

	private void keyUsed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UNDEFINED) {
			return;
		}
		if (keyCode == VK_CONTROL) {
			ctrl = true;
			return;
		}
		if (keyCode == VK_SHIFT) {
			shift = true;
			return;
		}
		if (keyCode == VK_ALT) {
			alt = true;
			return;
		}

		final Action action = ShortcutConfig.getAction(new Shortcut(ctrl, shift, alt, keyCode));
		if (action != null) {
			heldAction = action;
			fireAction(action);
			return;
		}

		final KeyHandler keyHandler = keyHandlers.get(keyCode);
		if (keyHandler != null) {
			if (!keysNotStoppingMusicOnPress.contains(keyCode)) {
				audioHandler.stopMusic();
			}
			keyHandler.fireFunction(e);
			return;
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		keyUsed(e);
		e.consume();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
				ctrl = false;
				break;
			case KeyEvent.VK_SHIFT:
				shift = false;
				break;
			case KeyEvent.VK_ALT:
				alt = false;
				break;
			default:
				break;
		}

		if (heldAction == ShortcutConfig.getAction(new Shortcut(ctrl, shift, alt, e.getKeyCode()))) {
			heldAction = null;
		}

		e.consume();
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
