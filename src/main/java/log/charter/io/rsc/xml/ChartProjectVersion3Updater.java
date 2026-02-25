package log.charter.io.rsc.xml;

import static log.charter.io.rs.xml.song.SongArrangementXStreamHandler.readSong;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.readVocals;
import static log.charter.util.CollectionUtils.transform;

import java.io.File;
import java.util.ArrayList;

import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.vocals.VocalPath;
import log.charter.io.Logger;
import log.charter.io.rs.xml.RSXMLToArrangement;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.converters.FHPConverter.TemporaryFHP;
import log.charter.io.rsc.xml.converters.ChordConverter.TemporaryChord;
import log.charter.io.rsc.xml.converters.EventPointConverter.TemporaryEventPoint;
import log.charter.io.rsc.xml.converters.HandShapeConverter.TemporaryHandShape;
import log.charter.io.rsc.xml.converters.NoteConverter.TemporaryNote;
import log.charter.io.rsc.xml.converters.ToneChangeConverter.TemporaryToneChange;
import log.charter.io.rsc.xml.converters.VocalConverter.TemporaryVocal;
import log.charter.services.data.files.SongFileHandler;
import log.charter.util.RW;

public class ChartProjectVersion3Updater {
	private static void loadVocalsFromFile(final File dir, final ChartProject project, final ImmutableBeatsMap beats) {
		if (project.vocals != null) {
			return;
		}

		final File vocalsFile = new File(dir, SongFileHandler.vocalsFileName);
		project.vocals = new VocalPath(beats, readVocals(RW.read(vocalsFile)));
	}

	private static void loadArrangementsFromFiles(final File dir, final ChartProject project,
			final ImmutableBeatsMap beats) {
		if (project.arrangements == null) {
			project.arrangements = new ArrayList<>();
		}

		for (final String filename : project.arrangementFiles) {
			try {
				final SongArrangement songArrangement = readSong(new File(dir, filename));
				project.arrangements.add(RSXMLToArrangement.toArrangement(songArrangement, beats));
			} catch (final Exception e) {
				Logger.error("Couldn't load arrangement file " + filename, e);
			}
		}
		project.arrangementFiles.clear();
	}

	private static ChordOrNote transformSound(final ImmutableBeatsMap beats, final ChordOrNote sound) {
		if (sound.isChord() && sound.chord() instanceof TemporaryChord) {
			return ChordOrNote.from(((TemporaryChord) sound.chord()).transform(beats));
		}
		if (sound.isNote() && sound.note() instanceof TemporaryNote) {
			return ChordOrNote.from(((TemporaryNote) sound.note()).transform(beats));
		}

		return sound;
	}

	public static void update(final File file, final ChartProject project) {
		if (project.chartFormatVersion >= 3) {
			return;
		}

		final ImmutableBeatsMap beats = new BeatsMap(project.beats).immutable;
		loadVocalsFromFile(file.getParentFile(), project, beats);
		loadArrangementsFromFiles(file.getParentFile(), project, beats);

		transform(project.vocals.vocals, TemporaryVocal.class, e -> e.transform(beats));

		for (final Arrangement arrangement : project.arrangements) {
			transform(arrangement.eventPoints, TemporaryEventPoint.class, e -> e.transform(beats));
			transform(arrangement.toneChanges, TemporaryToneChange.class, e -> e.transform(beats));
			for (final Level level : arrangement.levels) {
				transform(level.fhps, TemporaryFHP.class, e -> e.transform(beats));
				transform(level.sounds, e -> transformSound(beats, e));
				transform(level.handShapes, TemporaryHandShape.class, e -> e.transform(beats));
			}
		}

		project.chartFormatVersion = 3;
	}
}
