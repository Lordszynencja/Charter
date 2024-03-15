package log.charter.io.rsc.xml;

import static log.charter.io.rs.xml.song.SongArrangementXStreamHandler.readSong;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.readVocals;
import static log.charter.util.CollectionUtils.map;

import java.io.File;
import java.util.ArrayList;

import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.Level;
import log.charter.data.song.ToneChange;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocals;
import log.charter.io.Logger;
import log.charter.io.rs.xml.RSXMLToArrangement;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.converters.AnchorConverter.TemporaryAnchor;
import log.charter.io.rsc.xml.converters.EventPointConverter.TemporaryEventPoint;
import log.charter.io.rsc.xml.converters.ToneChangeConverter.TemporaryToneChange;
import log.charter.io.rsc.xml.converters.VocalConverter.TemporaryVocal;
import log.charter.services.data.files.SongFileHandler;
import log.charter.util.RW;

public class ChartProjectVerion3Updater {
	private static void loadVocalsFromFile(final File dir, final ChartProject project, final ImmutableBeatsMap beats) {
		if (project.vocals != null) {
			return;
		}

		final File vocalsFile = new File(dir, SongFileHandler.vocalsFileName);
		project.vocals = new Vocals(beats, readVocals(RW.read(vocalsFile)));
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

	private static Vocal transformVocal(final ImmutableBeatsMap beats, final Vocal vocal) {
		if (vocal instanceof TemporaryVocal) {
			return ((TemporaryVocal) vocal).transform(beats);
		}

		return vocal;
	}

	private static EventPoint transformEventPoint(final ImmutableBeatsMap beats, final EventPoint eventPoint) {
		if (eventPoint instanceof TemporaryEventPoint) {
			return ((TemporaryEventPoint) eventPoint).transform(beats);
		}

		return eventPoint;
	}

	private static ToneChange transformToneChange(final ImmutableBeatsMap beats, final ToneChange toneChange) {
		if (toneChange instanceof TemporaryToneChange) {
			return ((TemporaryToneChange) toneChange).transform(beats);
		}

		return toneChange;
	}

	private static Anchor transformAnchor(final ImmutableBeatsMap beats, final Anchor anchor) {
		if (anchor instanceof TemporaryAnchor) {
			return ((TemporaryAnchor) anchor).transform(beats);
		}

		return anchor;
	}

	public static void update(final File file, final ChartProject project) {
		if (project.chartFormatVersion >= 3) {
			return;
		}

		final ImmutableBeatsMap beats = new BeatsMap(project.beats).immutable;
		loadVocalsFromFile(file.getParentFile(), project, beats);
		loadArrangementsFromFiles(file.getParentFile(), project, beats);

		project.vocals.vocals = map(project.vocals.vocals, e -> transformVocal(beats, e));

		for (final Arrangement arrangement : project.arrangements) {
			arrangement.eventPoints = map(arrangement.eventPoints, e -> transformEventPoint(beats, e));
			arrangement.toneChanges = map(arrangement.toneChanges, e -> transformToneChange(beats, e));
			for (final Level level : arrangement.levels) {
				level.anchors = map(level.anchors, e -> transformAnchor(beats, e));
			}
		}

		project.chartFormatVersion = 2;
	}
}
