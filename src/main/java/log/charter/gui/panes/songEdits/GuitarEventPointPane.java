package log.charter.gui.panes.songEdits;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.AutocompleteInput;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.CharterSelect.ItemHolder;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class GuitarEventPointPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartData data;
	private final UndoSystem undoSystem;

	private AutocompleteInput<String> phraseNameInput;
	private JLabel phraseHint;
	private JTextField phraseLevelInput;
	private JCheckBox phraseSoloInput;
	private JTable eventsTable;

	private final EventPoint eventPoint;

	private SectionType section;
	private int phraseLevel;
	private boolean phraseSolo;
	private final List<EventType> events;

	public GuitarEventPointPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final EventPoint eventPoint, final Runnable onCancel) {
		super(frame, Label.GUITAR_BEAT_PANE, 400);
		this.data = data;
		this.undoSystem = undoSystem;

		this.eventPoint = eventPoint;

		final Arrangement arrangement = data.currentArrangement();

		section = eventPoint.section;
		final Phrase phrase = arrangement.phrases.get(eventPoint.phrase);
		if (phrase != null) {
			phraseLevel = phrase.maxDifficulty;
			phraseSolo = phrase.solo;
		}
		events = new ArrayList<>(eventPoint.events);

		final AtomicInteger row = new AtomicInteger(0);
		prepareSectionInput(row);
		preparePhraseInputs(row, eventPoint.phrase == null ? "" : eventPoint.phrase);
		prepareEventList(row);

		setOnFinish(this::saveAndExit, onCancel);
		addDefaultFinish(row.incrementAndGet());
	}

	private void onSectionChange(final SectionType newSection) {
		section = newSection;

		phraseHint.setText(section == null ? "" : section.label.label());
		phraseHint.setVisible(!phraseHint.getText().isBlank() && phraseNameInput.getText().isBlank());
		phraseHint.repaint();
	}

	private void prepareSectionInput(final AtomicInteger row) {
		final List<SectionType> sectionTypes = new ArrayList<>();
		sectionTypes.add(null);
		for (final SectionType sectionType : SectionType.values()) {
			sectionTypes.add(sectionType);
		}

		final CharterSelect<SectionType> input = new CharterSelect<>(sectionTypes, section,
				v -> v == null ? "" : v.label.label(), this::onSectionChange);

		addLabel(row.get(), 20, Label.SECTION_TYPE, 0);
		this.add(input, 100, getY(row.getAndIncrement()), 200, 20);
	}

	private void preparePhraseInputs(final AtomicInteger row, final String phrase) {
		addLabel(row.get(), 20, Label.PHRASE_NAME, 0);

		phraseHint = new JLabel(section == null ? "" : section.label.label());
		phraseHint.setVisible(false);
		phraseHint.setForeground(ColorLabel.BASE_DARK_TEXT.color());
		phraseHint.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		this.add(phraseHint, 105, getY(row.get()), 100, 20);

		phraseNameInput = new AutocompleteInput<>(this, 100, phrase, this::getPossiblePhraseNames, s -> s,
				this::onPhraseNameSelected);
		this.add(phraseNameInput, 100, getY(row.getAndIncrement()) - 2, 100, 24);

		phraseNameInput.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				phraseHint.setVisible(!phraseHint.getText().isBlank() && phraseNameInput.getText().isBlank());
			}

			@Override
			public void focusGained(final FocusEvent e) {
				phraseHint.setVisible(false);
			}
		});

		addIntConfigValue(row.get(), 50, 45, Label.LEVEL, phraseLevel, 30, //
				new IntValueValidator(0, 100), v -> phraseLevel = v, false);
		phraseLevelInput = (JTextField) getPart(-1);

		addConfigCheckbox(row.getAndIncrement(), 150, 0, Label.GUITAR_BEAT_PANE_PHRASE_SOLO, phraseSolo,
				val -> phraseSolo = val);
		phraseSoloInput = (JCheckBox) getPart(-1);
	}

	private List<String> getPossiblePhraseNames(final String text) {
		final String query = text.toLowerCase();

		final List<String> phraseNames = data.currentArrangement().phrases.keySet().stream()//
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

	private void onPhraseNameSelected(final String phraseName) {
		phraseNameInput.setTextWithoutUpdate(phraseName);

		final Phrase phrase = data.currentArrangement().phrases.get(phraseName);
		phraseLevel = phrase.maxDifficulty;
		phraseLevelInput.setText(phraseLevel + "");
		phraseSolo = phrase.solo;
		phraseSoloInput.setSelected(phraseSolo);
	}

	private void prepareEventList(final AtomicInteger row) {
		final List<EventType> eventTypes = new ArrayList<>();
		eventTypes.add(null);
		for (final EventType type : EventType.values()) {
			eventTypes.add(type);
		}

		final CharterSelect<EventType> input = new CharterSelect<>(eventTypes, null, null, null);
		input.setMinimumSize(new Dimension(100, 20));

		final DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.setRowCount(events.size());
		tableModel.setColumnCount(1);
		eventsTable = new JTable(tableModel);
		eventsTable.setShowGrid(false);
		eventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		eventsTable.setRowHeight(20);
		eventsTable.setTableHeader(new JTableHeader());

		final TableColumn column = eventsTable.getColumnModel().getColumn(0);
		column.setCellEditor(new DefaultCellEditor(input));

		final CharterScrollPane scrollTable = new CharterScrollPane(eventsTable);
		scrollTable.setColumnHeader(null);
		scrollTable.setMinimumSize(new Dimension(100, 80));

		for (int tableRow = 0; tableRow < events.size(); tableRow++) {
			tableModel.setValueAt(events.get(tableRow), tableRow, 0);
		}

		this.add(scrollTable, 50, getY(row.getAndIncrement()), 170, 120);

		final JButton rowAddButton = new JButton(Label.EVENT_ADD.label());
		rowAddButton.addActionListener(e -> { tableModel.addRow(new Vector<Object>(1)); });
		this.add(rowAddButton, 230, getY(row.getAndAdd(2)), 150, 20);

		final JButton rowRemoveButton = new JButton(Label.GUITAR_BEAT_PANE_EVENT_REMOVE.label());
		rowRemoveButton.addActionListener(e -> {
			if (tableModel.getRowCount() == 0) {
				return;
			}

			int rowToRemove = eventsTable.getEditingRow();
			if (rowToRemove == -1) {
				rowToRemove = eventsTable.getSelectedRow();
				if (rowToRemove == -1) {
					tableModel.removeRow(tableModel.getRowCount() - 1);
					return;
				}
			}

			if (eventsTable.getCellEditor() != null) {
				eventsTable.getCellEditor().cancelCellEditing();
			}
			eventsTable.clearSelection();
			tableModel.removeRow(rowToRemove);
		});

		this.add(rowRemoveButton, 230, getY(row.getAndAdd(2)), 150, 20);
	}

	@SuppressWarnings("unchecked")
	private boolean hasValues() {
		if (section != null) {
			return true;
		}
		if (!phraseNameInput.getText().isBlank()) {
			return true;
		}
		if (eventsTable.getRowCount() == 0) {
			return false;
		}

		for (int row = 0; row < eventsTable.getRowCount(); row++) {
			final ItemHolder<EventType> value = (ItemHolder<EventType>) eventsTable.getValueAt(row, 0);
			if (value != null && value.item != null) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private void saveAndExit() {
		undoSystem.addUndo();

		final Arrangement arrangement = data.currentArrangement();
		String phraseName = phraseNameInput.getText();

		if (!hasValues()) {
			arrangement.eventPoints.remove(eventPoint);
			return;
		}

		if (phraseName.isBlank() && section != null) {
			phraseName = section.label.label();
		}

		eventPoint.section = section;
		eventPoint.phrase = phraseName.isBlank() ? null : phraseName;
		if (!phraseName.isBlank()) {
			Phrase phrase = arrangement.phrases.get(phraseName);
			if (phrase == null) {
				phrase = new Phrase(phraseLevel, phraseSolo);
				arrangement.phrases.put(phraseName, phrase);
			} else {
				phrase.maxDifficulty = phraseLevel;
				phrase.solo = phraseSolo;
			}
		}

		eventPoint.events.clear();
		for (int row = 0; row < eventsTable.getRowCount(); row++) {
			final ItemHolder<EventType> value = (ItemHolder<EventType>) eventsTable.getValueAt(row, 0);
			if (value != null && value.item != null) {
				eventPoint.events.add(value.item);
			}
		}
		eventPoint.events.sort(EventType::compareTo);

		arrangement.clearPhrases();
	}
}
