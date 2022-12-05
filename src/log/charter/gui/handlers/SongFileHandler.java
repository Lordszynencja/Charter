package log.charter.gui.handlers;

import static log.charter.io.Logger.error;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import helliker.id3.MP3File;
import log.charter.data.Config;
import log.charter.gui.ChartEventsHandler;
import log.charter.io.IniWriter;
import log.charter.io.midi.reader.MidiReader;
import log.charter.io.midi.writer.MidiWriter;
import log.charter.song.IniData;
import log.charter.song.Song;
import log.charter.sound.MusicData;
import log.charter.util.RW;

public class SongFileHandler {

	private enum ID3v2Genre {
		BLUES(0, "Blues"), //
		CLASSIC_ROCK(1, "Classic Rock"), //
		COUNTRY(2, "Country"), //
		DANCE(3, "Dance"), //
		DISCO(4, "Disco"), //
		FUNK(5, "Funk"), //
		GRUNGE(6, "Grunge"), //
		HIP_HOP(7, "Hip-Hop"), //
		JAZZ(8, "Jazz"), //
		METAL(9, "Metal"), //
		NEW_AGE(10, "New Age"), //
		OLDIES(11, "Oldies"), //
		OTHER(12, "Other"), //
		POP(13, "Pop"), //
		R_AND_B(14, "R&B"), //
		RAP(15, "Rap"), //
		REGGAE(16, "Reggae"), //
		ROCK(17, "Rock"), //
		TECHNO(18, "Techno"), //
		INDUSTRIAL(19, "Industrial"), //
		ALTERNATIVE(20, "Alternative"), //
		SKA(21, "Ska"), //
		DEATH_METAL(22, "Death Metal"), //
		PRANKS(23, "Pranks"), //
		SOUNDTRACK(24, "Soundtrack"), //
		EURO_TECHNO(25, "Euro-Techno"), //
		AMBIENT(26, "Ambient"), //
		TRIP_HOP(27, "Trip-Hop"), //
		VOCAL(28, "Vocal"), //
		JAZZ_FUNK(29, "Jazz+Funk"), //
		FUSION(30, "Fusion"), //
		TRANCE(31, "Trance"), //
		CLASSICAL(32, "Classical"), //
		INSTRUMENTAL(33, "Instrumental"), //
		ACID(34, "Acid"), //
		HOUSE(35, "House"), //
		GAME(36, "Game"), //
		SOUND_CLIP(37, "Sound Clip"), //
		GOSPEL(38, "Gospel"), //
		NOISE(39, "Noise"), //
		ALTERNROCK(40, "AlternRock"), //
		BASS(41, "Bass"), //
		SOUL(42, "Soul"), //
		PUNK(43, "Punk"), //
		SPACE(44, "Space"), //
		MEDITATIVE(45, "Meditative"), //
		INSTRUMENTAL_POP(46, "Instrumental Pop"), //
		INSTRUMENTAL_ROCK(47, "Instrumental Rock"), //
		ETHNIC(48, "Ethnic"), //
		GOTHIC(49, "Gothic"), //
		DARKWAVE(50, "Darkwave"), //
		TECHNO_INDUSTRIAL(51, "Techno-Industrial"), //
		ELECTRONIC(52, "Electronic"), //
		POP_FOLK(53, "Pop-Folk"), //
		EURODANCE(54, "Eurodance"), //
		DREAM(55, "Dream"), //
		SOUTHERN_ROCK(56, "Southern Rock"), //
		COMEDY(57, "Comedy"), //
		CULT(58, "Cult"), //
		GANGSTA(59, "Gangsta"), //
		TOP_40(60, "Top 40"), //
		CHRISTIAN_RAP(61, "Christian Rap"), //
		POP_FUNK(62, "Pop/Funk"), //
		JUNGLE(63, "Jungle"), //
		NATIVE_AMERICAN(64, "Native American"), //
		CABARET(65, "Cabaret"), //
		NEW_WAVE(66, "New Wave"), //
		PSYCHADELIC(67, "Psychadelic"), //
		RAVE(68, "Rave"), //
		SHOWTUNES(69, "Showtunes"), //
		TRAILER(70, "Trailer"), //
		LO_FI(71, "Lo-Fi"), //
		TRIBAL(72, "Tribal"), //
		ACID_PUNK(73, "Acid Punk"), //
		ACID_JAZZ(74, "Acid Jazz"), //
		POLKA(75, "Polka"), //
		RETRO(76, "Retro"), //
		MUSICAL(77, "Musical"), //
		ROCK_AND_ROLL(78, "Rock & Roll"), //
		HARD_ROCK(79, "Hard Rock"), //
		FOLK(80, "Folk"), //
		FOLK_ROCK(81, "Folk-Rock"), //
		NATIONAL_FOLK(82, "National Folk"), //
		SWING(83, "Swing"), //
		FAST_FUSION(84, "Fast Fusion"), //
		BEBOB(85, "Bebob"), //
		LATIN(86, "Latin"), //
		REVIVAL(87, "Revival"), //
		CELTIC(88, "Celtic"), //
		BLUEGRASS(89, "Bluegrass"), //
		AVANTGARDE(90, "Avantgarde"), //
		GOTHIC_ROCK(91, "Gothic Rock"), //
		PROGRESSIVE_ROCK(92, "Progressive Rock"), //
		PSYCHEDELIC_ROCK(93, "Psychedelic Rock"), //
		SYMPHONIC_ROCK(94, "Symphonic Rock"), //
		SLOW_ROCK(95, "Slow Rock"), //
		BIG_BAND(96, "Big Band"), //
		CHORUS(97, "Chorus"), //
		EASY_LISTENING(98, "Easy Listening"), //
		ACOUSTIC(99, "Acoustic"), //
		HUMOUR(100, "Humour"), //
		SPEECH(101, "Speech"), //
		CHANSON(102, "Chanson"), //
		OPERA(103, "Opera"), //
		CHAMBER_MUSIC(104, "Chamber Music"), //
		SONATA(105, "Sonata"), //
		SYMPHONY(106, "Symphony"), //
		BOOTY_BASS(107, "Booty Bass"), //
		PRIMUS(108, "Primus"), //
		PORN_GROOVE(109, "Porn Groove"), //
		SATIRE(110, "Satire"), //
		SLOW_JAM(111, "Slow Jam"), //
		CLUB(112, "Club"), //
		TANGO(113, "Tango"), //
		SAMBA(114, "Samba"), //
		FOLKLORE(115, "Folklore"), //
		BALLAD(116, "Ballad"), //
		POWER_BALLAD(117, "Power Ballad"), //
		RHYTHMIC_SOUL(118, "Rhythmic Soul"), //
		FREESTYLE(119, "Freestyle"), //
		DUET(120, "Duet"), //
		PUNK_ROCK(121, "Punk Rock"), //
		DRUM_SOLO(122, "Drum Solo"), //
		A_CAPELLA(123, "A capella"), //
		EURO_HOUSE(124, "Euro-House"), //
		DANCE_HALL(125, "Dance Hall");

		public static String getGenre(final String genre) {
			try {
				return valueOf(Integer.valueOf(genre.substring(genre.indexOf('(') + 1, genre.indexOf(')')))).name;
			} catch (final Exception e) {
				return genre;
			}
		}

		public static ID3v2Genre valueOf(final int id) {
			for (final ID3v2Genre val : values()) {
				if (val.id == id) {
					return val;
				}
			}
			return null;
		}

		public final int id;
		public final String name;

		private ID3v2Genre(final int id, final String name) {
			this.id = id;
			this.name = name;
		}
	}

	private static IniData extractNewSongData(final String path) {
		final IniData data = new IniData();
		try {
			final MP3File mp3File = new MP3File(path);

			try {
				data.artist = mp3File.getArtist();
			} catch (final Exception e) {
				error("Couldn't get artist from mp3 tags data", e);
			}
			try {
				data.name = mp3File.getTitle();
			} catch (final Exception e) {
				error("Couldn't get title from mp3 tags data", e);
			}
			try {
				data.album = mp3File.getAlbum();
			} catch (final Exception e) {
				error("Couldn't get album from mp3 tags data", e);
			}
			try {
				data.track = mp3File.getTrackString();
			} catch (final Exception e) {
				error("Couldn't get track from mp3 tags data", e);
			}
			try {
				final String year = mp3File.getYear();
				data.year = year;
			} catch (final Exception e) {
				error("Couldn't get year from mp3 tags data", e);
			}
			try {
				data.genre = ID3v2Genre.getGenre(mp3File.getGenre());
			} catch (final Exception e) {
				error("Couldn't get genre from mp3 tags data", e);
			}

		} catch (final Exception e) {
			error("Couldn't get artist from mp3 tags data", e);
		}

		return data;
	}

	private final ChartEventsHandler handler;

	public SongFileHandler(final ChartEventsHandler handler) {
		this.handler = handler;
	}

	public void newSong() {
		if (!handler.checkChanged()) {
			return;
		}

		final JFileChooser chooser = new JFileChooser(new File(Config.musicPath));
		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(final File f) {
				final String name = f.getName().toLowerCase();
				return f.isDirectory() || name.endsWith(".mp3") || name.endsWith(".ogg");
			}

			@Override
			public String getDescription() {
				return "Mp3 (.mp3) or Ogg (.ogg) file";
			}
		});

		if (chooser.showOpenDialog(handler.frame) == JFileChooser.APPROVE_OPTION) {
			final File songFile = chooser.getSelectedFile();
			final IniData iniData = extractNewSongData(songFile.getAbsolutePath());
			final String songName = songFile.getName();
			final int dotIndex = songName.lastIndexOf('.');
			final String extension = songName.substring(dotIndex + 1).toLowerCase();
			if (!extension.equals("mp3") && !extension.equals("ogg")) {
				handler.showPopup("Not an Mp3 or Ogg file!");
				return;
			}

			String folderName = songName.substring(0, songName.lastIndexOf('.'));

			folderName = JOptionPane.showInputDialog(handler.frame, "Choose folder name", folderName);
			if (folderName == null) {
				return;
			}

			File f = new File(Config.songsPath + "/" + folderName);
			while (f.exists()) {
				folderName = JOptionPane.showInputDialog(handler.frame,
						"Given folder already exists, choose different name", folderName);
				if (folderName == null) {
					return;
				}
				f = new File(Config.songsPath + "/" + folderName);
			}
			f.mkdir();
			final String songDir = f.getAbsolutePath();
			final String musicPath = songDir + "/guitar." + extension;
			RW.writeB(musicPath, RW.readB(songFile));

			final MusicData musicData = MusicData.readFile(musicPath);
			if (musicData == null) {
				handler.showPopup(
						"Music file not found in song folder, something went wrong with copying or the file is invalid");
				return;
			}

			handler.data.setSong(songDir, new Song(), iniData, musicData);
			handler.data.s.tempoMap.isMs = true;
			handler.data.ini.charter = Config.charter;
			save();
		}
	}

	private File chooseFile(final String startingDir, final String[] extensions, final String[] descriptions) {
		final JFileChooser chooser = new JFileChooser(new File(startingDir));
		chooser.setAcceptAllFileFilterUsed(false);

		for (int i = 0; i < extensions.length; i++) {
			final String extension = extensions[i];
			final String description = descriptions[i];
			chooser.addChoosableFileFilter(new FileFilter() {

				@Override
				public boolean accept(final File f) {
					if (f.isDirectory()) {
						return true;
					}

					final String name = f.getName().toLowerCase();

					return f.isDirectory() || name.endsWith(extension);
				}

				@Override
				public String getDescription() {
					return description;
				}
			});
		}

		final int chosenOption = chooser.showOpenDialog(handler.frame);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFile();
	}

	public MusicData chooseMusicFile(final String startingDir) {
		final File musicFile = chooseFile(startingDir, new String[] { ".ogg", ".mp3" },
				new String[] { "Ogg file", "Mp3 file" });
		if (musicFile == null) {
			return null;
		}
		return MusicData.readFile(musicFile.getAbsolutePath());
	}

	public void open() {
		if (!handler.checkChanged()) {
			return;
		}

		String startingDir = handler.data.path;
		if (!new File(startingDir).exists()) {
			startingDir = Config.songsPath;
		}

		final File midiFileChosen = chooseFile(startingDir, new String[] { ".mid" }, new String[] { "Midi file" });
		if (midiFileChosen == null) {
			return;
		}

		final String dirPath = midiFileChosen.getParent();
		final String name = midiFileChosen.getName().toLowerCase();

		final Song s;
		if (name.endsWith(".mid")) {
			try {
				s = MidiReader.readMidi(midiFileChosen.getAbsolutePath());
			} catch (final InvalidMidiDataException e) {
				handler.showPopup("File is invalid:\n" + e.getLocalizedMessage());
				error("Error when opening midi", e);
				return;
			} catch (final IOException e) {
				handler.showPopup("File can't be read:\n" + e.getLocalizedMessage());
				error("Error when opening midi", e);
				return;
			}
		} else {
			s = null;
			handler.showPopup("This file type is not supported");
			error("unsupported file: " + midiFileChosen.getName());
			return;
		}

		final String[] fileNames = { "guitar.mp3", "guitar.ogg", "song.mp3", "song.ogg" };
		MusicData musicData = null;
		for (final String fileName : fileNames) {
			musicData = MusicData.readFile(dirPath + "\\" + fileName);
			if (musicData != null) {
				break;
			}
		}
		if (musicData == null) {
			handler.showPopup("Music file not found in song folder, please choose the music file");
			musicData = chooseMusicFile(startingDir);
		}
		if (musicData == null) {
			handler.showPopup("Music file couldn't be loaded");
			return;
		}

		final File iniFile = new File(dirPath + "/song.ini");
		final IniData iniData;
		if (iniFile.exists()) {
			iniData = new IniData(iniFile);
		} else {
			iniData = new IniData();
			error("No ini file found on path " + iniFile.getAbsolutePath());
		}

		if ((s != null) && (musicData != null) && (iniData != null)) {
			Config.lastPath = dirPath;
			Config.save();
			handler.data.setSong(dirPath, s, iniData, musicData);
			handler.data.changed = false;

			final String backupPath = midiFileChosen.getAbsolutePath() + "-"
					+ new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".mid";

			final byte[] bytes = RW.readB(backupPath);
			RW.writeB(backupPath, bytes);
		}
	}

	public void openAudioFile() {
		final MusicData musicData = chooseMusicFile(handler.data.path);
		if (musicData != null) {
			handler.data.music = musicData;
		}
	}

	public void save() {
		if (handler.data.isEmpty) {
			return;
		}

		MidiWriter.writeMidi(handler.data.path + "/notes.mid", handler.data.s);
		IniWriter.write(handler.data.path + "/song.ini", handler.data.ini);
		Config.save();
		handler.data.changed = false;
	}

	public void saveAs() {
		if (handler.data.isEmpty) {
			return;
		}

		final JFileChooser chooser = new JFileChooser(new File(handler.data.path));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		final int chosenOption = chooser.showOpenDialog(handler.frame);
		if (chosenOption != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File newDir = chooser.getSelectedFile();
		handler.data.path = newDir.getAbsolutePath();
		save();
		Config.save();
	}
}
