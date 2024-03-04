package log.charter.io.rsc.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.data.managers.modes.EditMode;
import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.song.Anchor;
import log.charter.song.Arrangement;
import log.charter.song.Beat;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.EventPoint;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Phrase;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.ChordOrNote.ChordOrNoteForChord;
import log.charter.song.notes.ChordOrNote.ChordOrNoteForNote;
import log.charter.song.notes.Note;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;

public class ChartProjectXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList2.class));
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), HashSet2.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap2.class), 0);
		xstream.alias("beat", Beat.class);
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(ChartProject.class);
		xstream.allowTypes(new Class[] { //
				Anchor.class, //
				Arrangement.class, //
				Beat.class, //
				BendValue.class, //
				ChordNote.class, //
				ChordOrNote.class, //
				ChordOrNoteForChord.class, //
				ChordOrNoteForNote.class, //
				ChordTemplate.class, //
				EventPoint.class, //
				HandShape.class, //
				Level.class, //
				Note.class, //
				Phrase.class, //
				ChartProject.class, //
				ToneChange.class, //
				Vocal.class });

		return xstream;
	}

	public static ChartProject readProject(final String xml) {
		final ChartProject project = (ChartProject) xstream.fromXML(xml);

		if (project.chartFormatVersion == 1) {
			project.editMode = EditMode.GUITAR;
			project.arrangement = 0;
			project.level = 0;
			project.time = 0;

			project.chartFormatVersion = 2;
		}

		return project;
	}

	public static String saveProject(final ChartProject chartProject) {
		return XMLHandler.generateXML(prepareXStream(), chartProject);
	}
}
