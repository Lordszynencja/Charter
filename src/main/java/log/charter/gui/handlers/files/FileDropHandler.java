package log.charter.gui.handlers.files;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.io.Logger;

public class FileDropHandler implements DropTargetListener, Initiable {
	private CharterFrame charterFrame;
	private GP5FileImporter gp5FileImporter;
	private MidiImporter midiImporter;
	private ProjectAudioHandler projectAudioHandler;
	private RSXMLImporter rsXMLImporter;

	private final Map<String, Consumer<File>> fileTypeHandlers = new HashMap<>();

	@Override
	public void init() {
		fileTypeHandlers.put("xml", this::handleXML);
		fileTypeHandlers.put("gp3", gp5FileImporter::importGP5File);
		fileTypeHandlers.put("gp4", gp5FileImporter::importGP5File);
		fileTypeHandlers.put("gp5", gp5FileImporter::importGP5File);
		fileTypeHandlers.put("mp3", projectAudioHandler::importAudio);
		fileTypeHandlers.put("ogg", projectAudioHandler::importAudio);
		fileTypeHandlers.put("wav", projectAudioHandler::importAudio);
		fileTypeHandlers.put("mid", midiImporter::importMidiTempo);
	}

	private void handleXML(final File file) {
		final int optionChosen = ComponentUtils.showOptionsPopup(charterFrame, Label.XML_IMPORT_TYPE,
				Label.XML_IMPORT_AS, //
				Label.GUITAR_ARRANGEMENT, //
				Label.VOCAL_ARRANGEMENT);

		switch (optionChosen) {
			case 0:
				rsXMLImporter.importAndAddRSArrangementXML(file);
				break;
			case 1:
				rsXMLImporter.importRSVocalsXML(file);
				break;
			default:
				break;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean dragAccepted(final DropTargetDragEvent event) {
		if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}

		List<File> files;
		try {
			files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException | IOException e) {
			return false;
		}
		if (files.size() != 1) {
			return false;
		}

		final File file = files.get(0);
		final String fileName = file.getName();
		final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		if (!fileTypeHandlers.containsKey(extension)) {
			return false;
		}

		return true;
	}

	private void rejectDragIfWrongType(final DropTargetDragEvent event) {
		if (dragAccepted(event)) {
			return;
		}

		event.rejectDrag();
	}

	@Override
	public void dragEnter(final DropTargetDragEvent event) {
		rejectDragIfWrongType(event);
	}

	@Override
	public void dragOver(final DropTargetDragEvent event) {
		rejectDragIfWrongType(event);
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent event) {
		rejectDragIfWrongType(event);
	}

	@Override
	public void dragExit(final DropTargetEvent event) {
	}

	@SuppressWarnings("unchecked")
	private File getFile(final DropTargetDropEvent event) {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		try {
			final List<File> files = (List<File>) event.getTransferable()//
					.getTransferData(DataFlavor.javaFileListFlavor);
			return files.get(0);
		} catch (UnsupportedFlavorException | IOException e) {
			Logger.error("Couldn't get dropped file", e);
			return null;
		}
	}

	@Override
	public void drop(final DropTargetDropEvent event) {
		final File file = getFile(event);
		if (file == null) {
			event.rejectDrop();
			return;
		}

		final String fileName = file.getName();
		final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		fileTypeHandlers.get(extension).accept(file);

		event.dropComplete(true);
	}

}
