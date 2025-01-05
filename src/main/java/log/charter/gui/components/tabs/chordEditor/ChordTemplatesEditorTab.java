package log.charter.gui.components.tabs.chordEditor;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.containers.ScrollableRowedPanel;
import log.charter.gui.components.tabs.selectionEditor.chords.ChordTemplateEditor;
import log.charter.gui.components.utils.PaneSizes;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;

public class ChordTemplatesEditorTab extends RowedPanel implements Initiable {
	private static final long serialVersionUID = 1L;

	private final ScrollableRowedPanel chordTemplatesList;

	private JLabel arpeggioLabel;
	private JCheckBox arpeggioCheckBox;
	private final ChordTemplateEditor chordTemplateEditor;

	private ChartData chartData;
	private CharterFrame charterFrame;
	private KeyboardHandler keyboardHandler;
	private UndoSystem undoSystem;

	private Integer currentChordTemplateId = null;
	private ChordTemplate chordTemplate = new ChordTemplate();

	public ChordTemplatesEditorTab() {
		super(new PaneSizesBuilder(0).build());

		setOpaque(true);
		setBackground(ColorLabel.BASE_BG_2.color());

		final PaneSizes listSizes = new PaneSizesBuilder(250)//
				.verticalSpace(0)//
				.rowHeight(50)//
				.rowSpacing(0)//
				.build();

		chordTemplatesList = new ScrollableRowedPanel(listSizes, 1);
		chordTemplatesList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		chordTemplatesList.setBackground(ColorLabel.BASE_BG_4.color());
		chordTemplateEditor = new ChordTemplateEditor(this);
	}

	@Override
	public void init() {
		addWithSettingSize(chordTemplatesList, 0, 0, 270, 400);

		chordTemplateEditor.init(chartData, charterFrame, keyboardHandler, () -> chordTemplate, this::templateEdited);

		chordTemplateEditor.addChordNameSuggestionButton(400, 0);

		chordTemplateEditor.addChordNameInput(400, 1);
		addCheckbox(2, 320, 70, Label.ARPEGGIO, false, val -> {
			chordTemplate.arpeggio = val;
			templateEdited();
		});
		arpeggioLabel = (JLabel) getPart(getPartsSize() - 2);
		arpeggioCheckBox = (JCheckBox) getLastPart();
		chordTemplateEditor.addChordTemplateEditor(320, 4);

		refreshTemplates();
	}

	private void templateEdited() {
		if (currentChordTemplateId == null) {
			return;
		}

		undoSystem.addUndo();

		chartData.currentChordTemplates().set(currentChordTemplateId, new ChordTemplate(chordTemplate));

		for (final Level level : chartData.currentArrangement().levels) {
			for (final ChordOrNote sound : level.sounds) {
				if (sound.isNote() || sound.chord().templateId() != currentChordTemplateId) {
					continue;
				}

				sound.chord().updateTemplate(chordTemplate);
			}
		}
	}

	public void refreshTemplates() {
		chordTemplatesList.getPanel().removeAll();

		final List<ChordTemplate> chordTemplates = chartData.currentChordTemplates();
		chordTemplatesList.resizePanel(250, chordTemplates.size());
		for (int i = 0; i < chordTemplates.size(); i++) {
			chordTemplatesList.add(new ChordTemplateInfo(chartData, this, i), 0, i);
		}

		setTemplate();

		repaint();
	}

	private void setEditFieldsVisibility(final boolean visible) {
		arpeggioLabel.setVisible(visible);
		arpeggioCheckBox.setVisible(visible);
		if (visible) {
			chordTemplateEditor.showFields();
		} else {
			chordTemplateEditor.hideFields();
		}
	}

	private void setTemplate() {
		if (currentChordTemplateId == null || currentChordTemplateId >= chartData.currentChordTemplates().size()) {
			currentChordTemplateId = null;
			chordTemplate = new ChordTemplate();
			setEditFieldsVisibility(false);
		} else {
			chordTemplate = new ChordTemplate(chartData.currentChordTemplates().get(currentChordTemplateId));
			setEditFieldsVisibility(true);
		}

		chordTemplateEditor.setCurrentValuesInInputs();
		arpeggioCheckBox.setSelected(chordTemplate.arpeggio);
	}

	public void selectChordTemplate(final int id) {
		currentChordTemplateId = id;
		setTemplate();
	}

	public Integer getSelectedChordTemplateId() {
		return currentChordTemplateId;
	}
}
