package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.io.Logger;
import log.charter.io.gp.gp7.GP7PlusFileImporter;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.data.StemAddService;
import log.charter.util.RW;

public class FileDropHandler implements DropTargetListener, Initiable {
	private enum FileType {
		RS_ARRANGEMENT_XML, //
		RS_SHOWLIGHTS_XML, //
		RS_VOCALS_XML
	}

	private ChartData chartData;
	private CharterFrame charterFrame;
	private ExistingProjectImporter existingProjectImporter;
	private GP5FileImporter gp5FileImporter;
	private GP7PlusFileImporter gp7PlusFileImporter;
	private GpaXmlImporter gpaXmlImporter;
	private LRCImporter lrcImporter;
	private MidiImporter midiImporter;
	private ProjectAudioHandler projectAudioHandler;
	private RSXMLImporter rsXMLImporter;
	private StemAddService stemAddService;
	private final USCTxtImporter uscTxtImporter = new USCTxtImporter();

	private final Map<FileType, Consumer<File>> fileTypeHandlers = new HashMap<>();
	private final Map<String, Consumer<File>> fileExtensionHandlers = new HashMap<>();

	@Override
	public void init() {
		fileExtensionHandlers.put("flac", this::handleAudio);
		fileExtensionHandlers.put("gp3", gp5FileImporter::importGP5File);
		fileExtensionHandlers.put("gp4", gp5FileImporter::importGP5File);
		fileExtensionHandlers.put("gp5", gp5FileImporter::importGP5File);
		fileExtensionHandlers.put("gp", gp7PlusFileImporter::importGP7PlusFile);
		fileExtensionHandlers.put("lrc", lrcImporter::importLRCFile);
		fileExtensionHandlers.put("mid", midiImporter::importMidiTempo);
		fileExtensionHandlers.put("mp3", this::handleAudio);
		fileExtensionHandlers.put("ogg", this::handleAudio);
		fileExtensionHandlers.put("rscp", f -> existingProjectImporter.open(f.getAbsolutePath()));
		fileExtensionHandlers.put("txt", this::handleTXT);
		fileExtensionHandlers.put("wav", this::handleAudio);
		fileExtensionHandlers.put("xml", this::handleXML);

		fileTypeHandlers.put(FileType.RS_ARRANGEMENT_XML, rsXMLImporter::importAndAddRSArrangementXML);
		fileTypeHandlers.put(FileType.RS_SHOWLIGHTS_XML, rsXMLImporter::importAndAddRsShowlightsXml);
		fileTypeHandlers.put(FileType.RS_VOCALS_XML, rsXMLImporter::importAndAddRsVocalsXML);
	}

	private String readTag(final InputStream in) throws IOException {
		char c = (char) in.read();
		while (c != '<' && c != -1) {
			c = (char) in.read();
		}

		String s = "";
		while (c != '>' && c != -1) {
			c = (char) in.read();
			s += c;
		}

		return s;
	}

	private FileType readFileTypeFromXml(final File file) {
		try {
			final InputStream in = new FileInputStream(file);
			String tag = readTag(in);
			while (tag.startsWith("?") || tag.startsWith("!--")) {
				tag = readTag(in);
			}
			in.close();

			if (tag.startsWith("song ")) {
				return FileType.RS_ARRANGEMENT_XML;
			}
			if (tag.startsWith("vocals ")) {
				return FileType.RS_VOCALS_XML;
			}
			if (tag.startsWith("showlights ")) {
				return FileType.RS_SHOWLIGHTS_XML;
			}
		} catch (final Exception e) {
			Logger.error("Couldn't read XML type", e);
		}

		return null;
	}

	private void handleXML(final File file) {
		FileType fileType = readFileTypeFromXml(file);
		if (fileType == null) {
			final int optionChosen = ComponentUtils.showOptionsPopup(charterFrame, Label.XML_IMPORT_TYPE,
					Label.XML_IMPORT_AS, //
					Label.GUITAR_ARRANGEMENT, //
					Label.VOCAL_ARRANGEMENT, //
					Label.RS_SHOWLIGHTS/*
										 * , // Label.GO_PLAY_ALONG
										 */);

			fileType = switch (optionChosen) {
				case 0 -> FileType.RS_ARRANGEMENT_XML;
				case 1 -> FileType.RS_VOCALS_XML;
				case 2 -> FileType.RS_SHOWLIGHTS_XML;
				default -> null;
			};
		}

		if (fileType == null) {
			return;
		}

		fileTypeHandlers.get(fileType).accept(file);
	}

	private void handleTXT(final File file) {
		final String data = RW.read(file);

		if (USCTxtImporter.isUSCFile(data)) {
			uscTxtImporter.importUSCFile(file);
			return;
		}

		ComponentUtils.showPopup(charterFrame, Label.COULDNT_READ_TXT, file.getAbsolutePath());
	}

	private void handleAudio(final File file) {
		switch (askYesNo(charterFrame, Label.IMPORTING_AUDIO, Label.IMPORT_AUDIO_AS_STEM)) {
			case YES:
				stemAddService.addStem(file);
				break;
			case NO:
				projectAudioHandler.importAudio(file);
				break;
			default:
				return;
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
		if (!fileExtensionHandlers.containsKey(extension)) {
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
		if (chartData.isEmpty) {
			event.rejectDrop();
			ComponentUtils.showPopup(charterFrame, Label.CANT_DROP_WITHOUT_PROJECT);
			return;
		}

		final File file = getFile(event);
		if (file == null) {
			event.rejectDrop();
			return;
		}

		importFile(file);

		event.dropComplete(true);
	}

	public void importFile(final File file) {
		final String fileName = file.getName();
		final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		fileExtensionHandlers.get(extension).accept(file);
	}
}
