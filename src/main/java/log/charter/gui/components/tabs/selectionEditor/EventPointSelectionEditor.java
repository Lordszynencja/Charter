package log.charter.gui.components.tabs.selectionEditor;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;
import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.types.PositionType;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.simple.AutocompleteInput;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.CharterSelect.ItemHolder;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class EventPointSelectionEditor extends SelectionEditorPart<EventPoint> {
	private ChartData chartData;

	private FieldWithLabel<CharterSelect<SectionType>> section;
	private FieldWithLabel<AutocompleteInput<String>> phrase;
	private FieldWithLabel<TextInputWithValidation> maxLevel;
	private FieldWithLabel<JCheckBox> solo;

	private DefaultTableModel tableModel;
	private JTable eventsTable;
	private CharterScrollPane eventsTableScroll;
	private JButton eventAddButton;
	private JButton eventRemoveButton;

	private boolean settingData = false;

	public EventPointSelectionEditor() {
		super(PositionType.EVENT_POINT);
	}

	@Override
	public void addTo(final CurrentSelectionEditor currentSelectionEditor) {
		final RowedPosition position = new RowedPosition(10, currentSelectionEditor.sizes);

		addSection(currentSelectionEditor, position);
		position.newRow();

		addPhrase(currentSelectionEditor, position);
		position.newRow();

		addMaxLevel(currentSelectionEditor, position);
		addSolo(currentSelectionEditor, position);
		position.newRow();

		addEvents(currentSelectionEditor, position);
		addNewEventButton(currentSelectionEditor, position.copy());
		addRemoveEventButton(currentSelectionEditor, position.newRowsInPlace(2));
	}

	private void onSectionChange(final SectionType newSection) {
		if (settingData) {
			return;
		}

		addUndo();

		for (final EventPoint eventPoint : getItems()) {
			eventPoint.section = newSection;
		}
	}

	private void addSection(final CurrentSelectionEditor currentSelectionEditor, final RowedPosition position) {
		final List<SectionType> sectionTypes = new ArrayList<>();
		sectionTypes.add(null);
		for (final SectionType sectionType : SectionType.values()) {
			sectionTypes.add(sectionType);
		}

		final CharterSelect<SectionType> input = new CharterSelect<>(sectionTypes, null,
				v -> v == null ? "" : v.label.label(), this::onSectionChange);

		section = new FieldWithLabel<>(Label.SECTION_TYPE, 100, 200, 20, input, LabelPosition.LEFT);
		currentSelectionEditor.add(section, position, 300);
	}

	private List<String> getPossiblePhraseNames(final String text) {
		final String query = text.toLowerCase();

		final List<String> phraseNames = chartData.currentArrangement().phrases.keySet().stream()//
				.filter(phraseName -> !phraseName.equals("COUNT") && !phraseName.equals("END")
						&& phraseName.toLowerCase().contains(query))//
				.collect(Collectors.toCollection(ArrayList::new));

		if ("count".contains(query)) {
			phraseNames.add("COUNT");
		}
		if ("end".contains(query)) {
			phraseNames.add("END");
		}
		phraseNames.sort(null);

		return phraseNames;
	}

	private void onPhraseChange(final String newPhrase) {
		if (settingData) {
			return;
		}

		addUndo();

		for (final EventPoint eventPoint : getItems()) {
			eventPoint.phrase = newPhrase;
		}

		if (!phrase.field.getText().equals(newPhrase)) {
			phrase.field.setTextWithoutUpdate(newPhrase);
		}
		if (!chartData.currentArrangement().phrases.containsKey(newPhrase)) {
			chartData.currentArrangement().phrases.put(newPhrase, new Phrase());
		}

		final Phrase phrase = chartData.currentArrangement().phrases.get(newPhrase);
		maxLevel.field.setTextWithoutEvent(phrase.maxDifficulty + "");
		solo.field.setSelected(phrase.solo);

		chartData.currentArrangement().clearPhrases();
	}

	private void addPhrase(final CurrentSelectionEditor currentSelectionEditor, final RowedPosition position) {
		final AutocompleteInput<String> input = new AutocompleteInput<>(currentSelectionEditor, 100, "",
				this::getPossiblePhraseNames, s -> s, this::onPhraseChange);
		input.setTextChangeListener(this::onPhraseChange);

		phrase = new FieldWithLabel<>(Label.PHRASE_NAME, 100, 100, 20, input, LabelPosition.LEFT);
		currentSelectionEditor.add(phrase, position, 200);
	}

	private void onMaxLevelChange(final int newMaxLevel) {
		if (settingData) {
			return;
		}

		addUndo();

		for (final EventPoint eventPoint : getItems()) {
			final Phrase phrase = chartData.currentArrangement().phrases.getOrDefault(eventPoint.phrase, new Phrase());
			phrase.maxDifficulty = newMaxLevel;
		}
	}

	private void addMaxLevel(final CurrentSelectionEditor currentSelectionEditor, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(0, 30, new IntValueValidator(0, 99),
				this::onMaxLevelChange, false);

		maxLevel = new FieldWithLabel<>(Label.LEVEL, 50, 30, 20, input, LabelPosition.LEFT);
		currentSelectionEditor.add(maxLevel, position, 80);
	}

	private void onSoloChange(final boolean newSolo) {
		if (settingData) {
			return;
		}

		addUndo();

		for (final EventPoint eventPoint : getItems()) {
			final Phrase phrase = chartData.currentArrangement().phrases.getOrDefault(eventPoint.phrase, new Phrase());
			phrase.solo = newSolo;
		}
	}

	private void addSolo(final CurrentSelectionEditor currentSelectionEditor, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(e -> { onSoloChange(input.isSelected()); });

		solo = new FieldWithLabel<>(Label.GUITAR_BEAT_PANE_PHRASE_SOLO, 50, 20, 20, input, LabelPosition.LEFT);
		currentSelectionEditor.add(solo, position.addX(10), 70);
	}

	@SuppressWarnings("unchecked")
	private List<EventType> readEventTypesFromTable() {
		final List<EventType> eventTypes = new ArrayList<>();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			final ItemHolder<EventType> eventType = (ItemHolder<EventType>) eventsTable.getValueAt(i, 0);
			if (eventType != null && eventType.item != null) {
				eventTypes.add(eventType.item);
			}
		}

		return eventTypes;
	}

	private void onEventsChange() {
		if (settingData) {
			return;
		}

		addUndo();

		final List<EventType> eventTypes = readEventTypesFromTable();

		for (final EventPoint eventPoint : getItems()) {
			eventPoint.events.clear();
			eventPoint.events.addAll(eventTypes);
		}
	}

	private void addEvents(final CurrentSelectionEditor currentSelectionEditor, final RowedPosition position) {
		final List<EventType> eventTypes = new ArrayList<>();
		eventTypes.add(null);
		for (final EventType type : EventType.values()) {
			eventTypes.add(type);
		}

		final CharterSelect<EventType> input = new CharterSelect<>(eventTypes, null, null, v -> onEventsChange());
		input.setMinimumSize(new Dimension(100, 20));

		tableModel = new DefaultTableModel();
		tableModel.setRowCount(0);
		tableModel.setColumnCount(1);
		eventsTable = new JTable(tableModel);
		eventsTable.setShowGrid(false);
		eventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		eventsTable.setRowHeight(20);
		eventsTable.setTableHeader(new JTableHeader());

		final TableColumn column = eventsTable.getColumnModel().getColumn(0);
		column.setCellEditor(new DefaultCellEditor(input));

		eventsTableScroll = new CharterScrollPane(eventsTable);
		eventsTableScroll.setColumnHeader(null);

		currentSelectionEditor.addWithSettingSize(eventsTableScroll, position, 300, 20, 200);
	}

	private void addNewEventButton(final CurrentSelectionEditor currentSelectionEditor, final RowedPosition position) {
		eventAddButton = new JButton(Label.EVENT_ADD.label());
		eventAddButton.addActionListener(e -> tableModel.addRow(new Vector<Object>(1)));

		currentSelectionEditor.addWithSettingSize(eventAddButton, position, 150, 10, 30);
	}

	private int getRowToRemove() {
		int rowToRemove = eventsTable.getEditingRow();
		if (rowToRemove >= 0) {
			return rowToRemove;
		}

		rowToRemove = eventsTable.getSelectedRow();
		if (rowToRemove >= 0) {
			return rowToRemove;
		}

		return tableModel.getRowCount() - 1;
	}

	private void addRemoveEventButton(final CurrentSelectionEditor currentSelectionEditor,
			final RowedPosition position) {
		eventRemoveButton = new JButton(Label.GUITAR_BEAT_PANE_EVENT_REMOVE.label());
		eventRemoveButton.addActionListener(e -> {
			if (tableModel.getRowCount() == 0) {
				return;
			}

			final int rowToRemove = getRowToRemove();

			if (eventsTable.getCellEditor() != null) {
				eventsTable.getCellEditor().cancelCellEditing();
			}
			eventsTable.clearSelection();
			tableModel.removeRow(rowToRemove);
			onEventsChange();
		});

		currentSelectionEditor.addWithSettingSize(eventRemoveButton, position, 150, 10, 30);
	}

	@Override
	public void hide() {
		section.setVisible(false);
		phrase.setVisible(false);
		maxLevel.setVisible(false);
		solo.setVisible(false);
		eventsTable.setVisible(false);
		eventAddButton.setVisible(false);
		eventRemoveButton.setVisible(false);
	}

	private void setEvents(final List<EventType> events) {
		tableModel.setRowCount(events.size());
		for (int i = 0; i < events.size(); i++) {
			tableModel.setValueAt(events.get(i), i, 0);
		}
	}

	@Override
	public void selectionChanged() {
		super.selectionChanged();

		final List<EventPoint> items = getItems();

		settingData = true;

		final SectionType section = getSingleValue(items, ep -> ep.section, null);
		this.section.field.setSelectedValue(section);
		this.section.setVisible(true);

		final String phrase = getSingleValue(items, ep -> ep.phrase, null);
		this.phrase.field.setTextWithoutUpdate(phrase == null ? "" : phrase);
		this.phrase.setVisible(true);

		final Set<Phrase> phrases = items.stream()//
				.map(ep -> chartData.currentArrangement().phrases.get(ep.phrase))//
				.collect(Collectors.toSet());

		final Integer maxLevel = getSingleValue(phrases, p -> p.maxDifficulty, null);
		this.maxLevel.field.setTextWithoutEvent(maxLevel == null ? "" : maxLevel.toString());
		this.maxLevel.setVisible(true);

		final boolean solo = getSingleValue(phrases, p -> p.solo, false);
		this.solo.field.setSelected(solo);
		this.solo.setVisible(true);

		if (items.size() == 1) {
			setEvents(items.get(0).events);
		} else {
			setEvents(new ArrayList<>());
		}
		eventsTable.setVisible(true);
		eventAddButton.setVisible(true);
		eventRemoveButton.setVisible(true);

		settingData = false;
	}

}
