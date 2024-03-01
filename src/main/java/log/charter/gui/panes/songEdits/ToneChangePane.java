package log.charter.gui.panes.songEdits;

import java.awt.Color;
import java.util.stream.Collectors;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.AutocompleteInputForPane;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.song.Arrangement;
import log.charter.song.ToneChange;
import log.charter.util.CollectionUtils.ArrayList2;

public class ToneChangePane extends ParamsPane implements DocumentListener {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartData data;
	private final UndoSystem undoSystem;

	private final AutocompleteInputForPane<String> toneNameInput;
	private boolean error;
	private Color toneNameInputBackgroundColor;

	private final ToneChange toneChange;

	private String toneName;

	public ToneChangePane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final ToneChange toneChange, final Runnable onCancel) {
		super(frame, Label.TONE_CHANGE_PANE, 250);
		this.data = data;
		this.undoSystem = undoSystem;

		this.toneChange = toneChange;

		toneName = toneChange.toneName;

		int row = 0;
		toneNameInput = new AutocompleteInputForPane<>(this, 100, toneName, this::getPossibleValues, s -> s,
				this::onSelect);
		toneNameInput.getDocument().addDocumentListener(this);
		final int labelWidth = addLabel(row, 20, Label.TONE_CHANGE_TONE_NAME, 0);
		add(toneNameInput, 20 + labelWidth + 3, getY(row++), 100, 20);

		row++;
		addDefaultFinish(row, this::saveAndExit, () -> {
			onCancel.run();
			return true;
		});
	}

	private ArrayList2<String> getPossibleValues(final String name) {
		return data.getCurrentArrangement().tones.stream()//
				.filter(toneName -> toneName.toLowerCase().contains(name.toLowerCase()))//
				.collect(Collectors.toCollection(ArrayList2::new));
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (error) {
			toneNameInput.setToolTipText(null);
			toneNameInput.setBackground(toneNameInputBackgroundColor);
			error = false;
		}

		final String name = toneNameInput.getText();

		final Arrangement arrangement = data.getCurrentArrangement();
		if (arrangement.tones.size() >= 4 && !arrangement.tones.contains(name) && !name.isEmpty()) {
			error = true;
			toneNameInputBackgroundColor = toneNameInput.getBackground();
			toneNameInput.setBackground(TextInputWithValidation.errorBackground);
			toneNameInput.setToolTipText(Label.TONE_NAME_PAST_LIMIT.label());

			return;
		}

		toneName = name;
	}

	private void onSelect(final String name) {
		toneNameInput.setTextWithoutUpdate(name);
	}

	private boolean saveAndExit() {
		if (error) {
			return false;
		}

		undoSystem.addUndo();

		final Arrangement arrangement = data.getCurrentArrangement();
		if (toneName.isEmpty()) {
			arrangement.toneChanges.remove(toneChange);
			if (!arrangement.toneChanges.contains(toneChange -> toneChange.toneName.equals(this.toneChange.toneName))) {
				arrangement.tones.remove(toneChange.toneName);
			}
			return true;
		}

		arrangement.tones.add(toneName);
		toneChange.toneName = toneName;
		return true;
	}

}
