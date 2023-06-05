package log.charter.gui.panes;

import javax.swing.JScrollPane;

import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.BendEditor;
import log.charter.gui.components.ParamsPane;
import log.charter.song.BeatsMap;
import log.charter.song.notes.Note;

public class NoteBendPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 500;

		return sizes;
	}

	private final UndoSystem undoSystem;

	private final BendEditor bendEditor;

	private final Note note;

	public NoteBendPane(final BeatsMap beatsMap, final CharterFrame frame, final UndoSystem undoSystem, final Note note,
			final int strings) {
		super(frame, Label.BEND_OPTIONS_PANE, 10, getSizes());
		this.undoSystem = undoSystem;

		this.note = note;

		bendEditor = new BendEditor(beatsMap, note.position(), note.length(), note.string, note.bendValues, strings);

		final int maxWidth = frame.getWidth() - 40;
		if (bendEditor.getWidth() < maxWidth) {
			bendEditor.setLocation(20, 20);
			setWidthWithInsets(bendEditor.getWidth() + 40);
			this.add(bendEditor);
		} else {
			final JScrollPane scrollPane = new JScrollPane(bendEditor, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollPane.validate();
			final int height = scrollPane.getPreferredSize().height;
			setWidthWithInsets(maxWidth);
			this.add(scrollPane, 20, 20, maxWidth - 40, height);
		}

		addDefaultFinish(9, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		note.bendValues = bendEditor.getBendValues();
	}
}
