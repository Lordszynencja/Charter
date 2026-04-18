package log.charter.gui.components.tabs.chordEditor;

import static log.charter.data.config.GraphicalConfig.inputSize;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.ScrollPaneConstants;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.containers.ScrollableRowedPanel;
import log.charter.gui.components.tabs.selectionEditor.chords.ChordTemplateEditor;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.PaneSizes;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;

public class ChordTemplatesEditorTab extends RowedPanel implements Initiable {
	static int listWidth = 450;

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

		final PaneSizes listSizes = new PaneSizesBuilder(listWidth - 20)//
				.verticalSpace(0)//
				.rowHeight(50)//
				.rowSpacing(0)//
				.build();

		chordTemplatesList = new ScrollableRowedPanel(listSizes, 1);
		chordTemplatesList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		chordTemplatesList.setBackground(ColorLabel.BASE_BG_4.color());
		chordTemplatesList.getVerticalScrollBar().setUnitIncrement(25);

		chordTemplateEditor = new ChordTemplateEditor(this, true);
	}

	@Override
	public void init() {
		final int height = sizes.getY(InstrumentConfig.maxStrings + 5);
		addWithSettingSize(chordTemplatesList, 0, 0, listWidth, height);

		chordTemplateEditor.init(chartData, charterFrame, keyboardHandler, () -> chordTemplate, this::templateEdited);

		chordTemplateEditor.addChordNameSuggestionButton(listWidth + 150, 0);

		chordTemplateEditor.addChordNameInput(listWidth + 70, 1);
		addCheckbox(2, listWidth + 70, 70, Label.ARPEGGIO, false, val -> {
			chordTemplate.arpeggio = val;
			templateEdited();
		});
		arpeggioLabel = (JLabel) getPart(-2);
		arpeggioCheckBox = (JCheckBox) getPart(-1);
		chordTemplateEditor.addChordTemplateEditor(listWidth + 70, 4);

		recalculateSizes();
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

	private void refreshChordTemplateInfoSizes(final List<ChordTemplate> chordTemplates) {
		int maxDifference = 5;
		for (final ChordTemplate template : chordTemplates) {
			final int bottom = template.getLowestNotOpenFret(chartData.currentArrangement().capo);
			final int top = template.getHighestFret();
			final int range = top - bottom + 1;
			if (maxDifference < range) {
				maxDifference = range;
			}
		}

		ChordTemplateInfo.maxFretsVisible = maxDifference;
		ChordTemplateInfo.recalculateSizes();
	}

	public void refreshTemplates() {
		chordTemplatesList.getPanel().removeAll();

		final List<ChordTemplate> chordTemplates = chartData.currentChordTemplates();
		chordTemplatesList.resizePanel(listWidth - 20, chordTemplates.size());
		refreshChordTemplateInfoSizes(chordTemplates);

		for (int i = 0; i < chordTemplates.size(); i++) {
			chordTemplatesList.add(new ChordTemplateInfo(chartData, chordTemplatesList, this, i), 0, i);
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

	public void recalculateSizes() {
		listWidth = inputSize * 20;
		chordTemplatesList.sizes.width = listWidth - inputSize;
		chordTemplatesList.sizes.rowHeight(inputSize * 5 / 2);
		chordTemplatesList.setSize(listWidth, sizes.getY(InstrumentConfig.maxStrings + 5));
		final JScrollBar scrollBar = chordTemplatesList.getVerticalScrollBar();
		scrollBar.setPreferredSize(new Dimension(inputSize, 1));

		refreshTemplates();

		ComponentUtils.resize(arpeggioLabel, arpeggioCheckBox, chordTemplatesList.sizes.width + inputSize * 5 / 2,
				inputSize * 7 / 2, inputSize * 4, inputSize);

		chordTemplateEditor.recalculateSizesWithReposition(chordTemplatesList.sizes.width + inputSize * 2, inputSize);
	}
}
