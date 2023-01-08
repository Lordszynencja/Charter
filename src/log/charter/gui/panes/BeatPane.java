package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import java.awt.Dimension;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.AutocompleteInput;
import log.charter.gui.components.ParamsPane;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.song.Event;
import log.charter.song.Event.EventType;
import log.charter.song.Phrase;
import log.charter.song.PhraseIteration;
import log.charter.song.Section;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;

public class BeatPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static class SectionTypeListValue {
		public final SectionType type;

		public SectionTypeListValue(final SectionType type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type == null ? "" : type.label;
		}
	}

	private static class EventTypeListValue {
		public final EventType type;

		public EventTypeListValue(final EventType type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type == null ? "" : type.label;
		}
	}

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 400;

		return sizes;
	}

	private final ChartData data;
	private final UndoSystem undoSystem;

	private AutocompleteInput<String> phraseNameInput;
	private JTextField phraseLevelInput;
	private JCheckBox phraseSoloInput;
	private JTable eventsTable;

	private final Beat beat;
	private Section section;
	private PhraseIteration phraseIteration;
	private final ArrayList2<Event> events = new ArrayList2<>();

	private SectionType sectionType;
	private String phraseName = "";
	private int phraseLevel;
	private boolean phraseSolo;

	public BeatPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem, final Beat beat) {
		super(frame, Label.BEAT_PANE, 10, getSizes());
		this.data = data;
		this.undoSystem = undoSystem;

		this.beat = beat;

		final ArrangementChart arrangement = data.getCurrentArrangement();
		prepareSection(arrangement);
		preparePhrase(arrangement);
		prepareEvents(arrangement);

		int row = 0;
		row = prepareSectionInput(row);
		row = preparePhraseInputs(row);
		row = prepareEventList(row);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void prepareSection(final ArrangementChart arrangement) {
		for (final Section section : arrangement.sections) {
			if (section.position() > beat.position()) {
				break;
			}
			if (section.beat == beat) {
				this.section = section;
				break;
			}
		}
		if (section != null) {
			sectionType = section.type;
		}
	}

	private void preparePhrase(final ArrangementChart arrangement) {
		for (final PhraseIteration phraseIteration : arrangement.phraseIterations) {
			if (phraseIteration.position() > beat.position()) {
				break;
			}
			if (phraseIteration.beat == beat) {
				this.phraseIteration = phraseIteration;
				break;
			}
		}
		if (phraseIteration != null) {
			phraseName = phraseIteration.phraseName;
			final Phrase phrase = arrangement.phrases.get(phraseName);
			phraseLevel = phrase.maxDifficulty;
			phraseSolo = phrase.solo;
		}
	}

	private void prepareEvents(final ArrangementChart arrangement) {
		for (final Event event : arrangement.events) {
			if (event.position() > beat.position()) {
				break;
			}
			if (event.beat == beat) {
				events.add(event);
			}
		}
	}

	private int prepareSectionInput(int row) {
		final JComboBox<SectionTypeListValue> sectionTypeInput = new JComboBox<>();
		sectionTypeInput.addItem(new SectionTypeListValue(null));
		for (final SectionType type : SectionType.values()) {
			sectionTypeInput.addItem(new SectionTypeListValue(type));
		}
		sectionTypeInput.setSelectedIndex(sectionType == null ? 0 : 1 + sectionType.ordinal());
		sectionTypeInput
				.addItemListener(e -> sectionType = ((SectionTypeListValue) sectionTypeInput.getSelectedItem()).type);

		addLabel(row, 20, Label.BEAT_PANE_SECTION_TYPE);
		this.add(sectionTypeInput, 100, getY(row++), 200, 20);

		return row;
	}

	private int preparePhraseInputs(int row) {
		addLabel(row, 20, Label.BEAT_PANE_PHRASE_NAME);
		phraseNameInput = new AutocompleteInput<>(this, 100, phraseName, this::getPossiblePhraseNames, s -> s,
				this::onPhraseNameSelected);
		this.add(phraseNameInput, 100, getY(row++), 100, 20);

		addIntegerConfigValue(row, 50, 45, Label.BEAT_PANE_PHRASE_LEVEL, phraseLevel, 30,
				createIntValidator(0, 100, false), val -> phraseLevel = val, false);
		phraseLevelInput = (JTextField) components.getLast();

		addConfigCheckbox(row++, 150, 0, Label.BEAT_PANE_PHRASE_SOLO, phraseSolo, val -> phraseSolo = val);
		phraseSoloInput = (JCheckBox) components.getLast();

		return row;
	}

	private ArrayList2<String> getPossiblePhraseNames(final String text) {
		return data.getCurrentArrangement().phrases.keySet().stream()//
				.filter(phraseName -> !phraseName.equals("COUNT") && !phraseName.equals("END")
						&& phraseName.toLowerCase().contains(text.toLowerCase()))//
				.sorted()//
				.collect(Collectors.toCollection(ArrayList2::new));
	}

	private void onPhraseNameSelected(final String phraseName) {
		phraseNameInput.setTextWithoutUpdate(phraseName);

		final Phrase phrase = data.getCurrentArrangement().phrases.get(phraseName);
		phraseLevel = phrase.maxDifficulty;
		phraseLevelInput.setText(phraseLevel + "");
		phraseSolo = phrase.solo;
		phraseSoloInput.setSelected(phraseSolo);
	}

	private int prepareEventList(final int row) {
		final JComboBox<EventTypeListValue> eventTypeComboBox = new JComboBox<>();
		eventTypeComboBox.addItem(new EventTypeListValue(null));
		for (final EventType type : EventType.values()) {
			eventTypeComboBox.addItem(new EventTypeListValue(type));
		}
		eventTypeComboBox.setMinimumSize(new Dimension(100, 20));

		final DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.setColumnCount(1);
		tableModel.setRowCount(events.size());
		eventsTable = new JTable(tableModel);
		eventsTable.setShowGrid(false);
		eventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		eventsTable.setRowHeight(20);
		eventsTable.setTableHeader(new JTableHeader());

		final TableColumn column = eventsTable.getColumnModel().getColumn(0);
		column.setCellEditor(new DefaultCellEditor(eventTypeComboBox));

		final JScrollPane scrollTable = new JScrollPane(eventsTable);
		scrollTable.setColumnHeader(null);
		scrollTable.setMinimumSize(new Dimension(100, 80));

		for (int tableRow = 0; tableRow < events.size(); tableRow++) {
			tableModel.setValueAt(eventTypeComboBox.getItemAt(events.get(tableRow).type.ordinal() + 1), tableRow, 0);
		}

		this.add(scrollTable, 50, getY(row), 170, 120);

		final JButton rowAddButton = new JButton(Label.BEAT_PANE_EVENT_ADD.label());
		rowAddButton.addActionListener(e -> {
			tableModel.addRow((Vector<?>) null);
		});
		this.add(rowAddButton, 230, getY(row + 1), 150, 20);

		final JButton rowRemoveButton = new JButton(Label.BEAT_PANE_EVENT_REMOVE.label());
		rowRemoveButton.addActionListener(e -> {
			if (tableModel.getRowCount() > 0) {
				eventsTable.clearSelection();
				final int rowToRemove = tableModel.getRowCount() - 1;
				tableModel.removeRow(rowToRemove);
				tableModel.setRowCount(rowToRemove);
			}
		});
		this.add(rowRemoveButton, 230, getY(row + 3), 150, 20);

		return row + 5;
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		final ArrangementChart arrangement = data.getCurrentArrangement();
		if (section == null && sectionType != null) {
			arrangement.sections.add(new Section(beat, sectionType));
			arrangement.sections.sort(null);
		} else if (section != null && sectionType == null) {
			arrangement.sections.remove(section);
		} else {
			section.type = sectionType;
		}

		Phrase phrase = arrangement.phrases.get(phraseName);
		if (phrase == null && !phraseName.isEmpty()) {
			phrase = new Phrase(phraseLevel, phraseSolo);
			arrangement.phrases.put(phraseName, phrase);
		} else if (phrase != null) {
			phrase.maxDifficulty = phraseLevel;
			phrase.solo = phraseSolo;
		}

		if (phraseIteration == null && !phraseName.isEmpty()) {
			arrangement.phraseIterations.add(new PhraseIteration(beat, phraseName));
			arrangement.phraseIterations.sort(null);
		} else if (phraseIteration != null && phraseName.isEmpty()) {
			arrangement.phraseIterations.remove(phraseIteration);
		} else {
			section.type = sectionType;
		}

		arrangement.events.removeAll(events);
		for (int row = 0; row < eventsTable.getRowCount(); row++) {
			final EventTypeListValue value = (EventTypeListValue) eventsTable.getValueAt(row, 0);
			if (value != null && value.type != null) {
				arrangement.events.add(new Event(beat, value.type));
			}
		}
		arrangement.events.sort(null);
	}
}
