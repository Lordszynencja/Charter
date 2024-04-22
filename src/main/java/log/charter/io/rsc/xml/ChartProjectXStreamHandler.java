package log.charter.io.rsc.xml;

import java.io.File;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.Phrase;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.ChordOrNote.ChordOrNoteForChord;
import log.charter.data.song.notes.ChordOrNote.ChordOrNoteForNote;
import log.charter.data.song.notes.GuitarSound;
import log.charter.data.song.notes.Note;
import log.charter.data.song.vocals.Vocal;
import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.services.data.copy.data.AnchorsCopyData;
import log.charter.services.data.copy.data.CopyData;
import log.charter.services.data.copy.data.EventPointsCopyData;
import log.charter.services.data.copy.data.FullGuitarCopyData;
import log.charter.services.data.copy.data.HandShapesCopyData;
import log.charter.services.data.copy.data.SoundsCopyData;
import log.charter.services.data.copy.data.VocalsCopyData;
import log.charter.services.data.copy.data.positions.CopiedAnchor;
import log.charter.services.data.copy.data.positions.CopiedEventPoint;
import log.charter.services.data.copy.data.positions.CopiedHandShape;
import log.charter.services.data.copy.data.positions.CopiedSound.CopiedSoundChord;
import log.charter.services.data.copy.data.positions.CopiedSound.CopiedSoundNote;
import log.charter.services.data.copy.data.positions.CopiedToneChange;
import log.charter.services.data.copy.data.positions.CopiedVocalPosition;
import log.charter.util.RW;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashMap2;

public class ChartProjectXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList2.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap2.class), 0);
		xstream.alias("beat", Beat.class);
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(ChartProject.class);
		xstream.processAnnotations(CopyData.class);
		xstream.allowTypes(new Class[] { //
				Anchor.class, //
				AnchorsCopyData.class, //
				Arrangement.class, //
				Beat.class, //
				BendValue.class, //
				ChordNote.class, //
				ChordOrNote.class, //
				ChordOrNoteForChord.class, //
				ChordOrNoteForNote.class, //
				ChordTemplate.class, //
				CopiedAnchor.class, //
				CopiedEventPoint.class, //
				CopiedHandShape.class, //
				CopiedSoundChord.class, //
				CopiedSoundNote.class, //
				CopiedToneChange.class, //
				CopiedVocalPosition.class, //
				CopyData.class, //
				EventPoint.class, //
				EventPointsCopyData.class, //
				FullGuitarCopyData.class, //
				GuitarSound.class, //
				HandShape.class, //
				HandShapesCopyData.class, //
				Level.class, //
				Note.class, //
				Phrase.class, //
				ChartProject.class, //
				SoundsCopyData.class, //
				ToneChange.class, //
				Vocal.class, //
				VocalsCopyData.class });

		return xstream;
	}

	public static ChartProject readChartProject(final File file) {
		String content = RW.read(file);
		content = content.replace(" class=\"log.charter.util.collections.HashSet2\"", "");

		final Object o = xstream.fromXML(content);
		if (!o.getClass().isAssignableFrom(ChartProject.class)) {
			return null;
		}

		final ChartProject project = (ChartProject) o;
		ChartProjectVerion2Updater.update(project);
		ChartProjectVerion3Updater.update(file, project);

		return project;
	}

	public static CopyData readCopyData(final String xml) {
		final Object o = xstream.fromXML(xml);
		if (!o.getClass().isAssignableFrom(CopyData.class)) {
			return null;
		}

		return (CopyData) o;
	}

	public static String writeChartProject(final ChartProject chartProject) {
		return XMLHandler.generateXML(xstream, chartProject);
	}

	public static String writeCopyData(final CopyData copyData) {
		return XMLHandler.generateXML(xstream, copyData);
	}
}
