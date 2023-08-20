// package log;
package log.charter.song;

import log.charter.gui.menuHandlers.FileMenuHandler;
import log.charter.io.gp.gp5.GP5File;
import log.charter.io.gp.gp5.GP5FileReader;
import log.charter.util.CollectionUtils.ArrayList2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;

public class TestArrangementChart {
    FileMenuHandler file_menu_handler;
    SongChart song;

    @BeforeEach
    void setUp() {
        file_menu_handler = new FileMenuHandler();
        song = new SongChart(30000, "test");
    }
    @Test
    void testFourOverFour() {
        final File file = new File("src/test/resources/gp_import_test_files/all_note_lengths_4_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        int beat_length = (60000/gp5File.tempo);
        int bar_length = beat_length * 4; // for 4/4

        int n_whole_notes = 1;
        int n_half_notes = 2;
        int n_quarter_notes = 4;
        int n_eighth_notes = 8;
        int n_sixteenth_notes = 16;
        int n_thirtysecond_notes = 32;
        int n_sixtyfourth_notes = 64;
    
        int i = 0;
        int j = n_whole_notes;

        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length, duration);
        }
        j += n_half_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/2, duration);
        }
        j += n_quarter_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/4, duration);
        }
        j += n_eighth_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/8, duration);
        }
        j += n_sixteenth_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/16, duration);
        }
        j += n_thirtysecond_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/32, duration);
        }
        j += n_sixtyfourth_notes;
        for (; i < j - 1; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((int)Math.round((bar_length/64)) == duration || (int)Math.round((bar_length/64)) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }
    @Test
    void testTwelveOverEight() {
        final File file = new File("src/test/resources/gp_import_test_files/all_note_lengths_12_8.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        int beat_length = (60000/gp5File.tempo);
        int bar_length = beat_length * 6; // for 12/8

        int n_half_notes = 3;
        int n_quarter_notes = 6;
        int n_eighth_notes = 12;
        int n_sixteenth_notes = 24;
        int n_thirtysecond_notes = 48;
        int n_sixtyfourth_notes = 96;
    
        int i = 0;
        int j = n_half_notes;

        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/3, duration);
        }
        j += n_quarter_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/6, duration);
        }
        j += n_eighth_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/12, duration);
        }
        j += n_sixteenth_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/24, duration);
        }
        j += n_thirtysecond_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertEquals(bar_length/48, duration);
        }
        j += n_sixtyfourth_notes;
        for (; i < j-1; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((bar_length/96) == duration || (bar_length/96) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }
    @Test
    void testTripletsFourOverFour() {
        final File file = new File("src/test/resources/gp_import_test_files/all_note_lengths_triplets_4_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        int beat_length = (60000/gp5File.tempo);
        int bar_length = beat_length * 4; // for 4/4
        int n_half_notes = 3;
        int n_quarter_notes = 6;
        int n_eighth_notes = 12;
        int n_sixteenth_notes = 24;
        int n_thirtysecond_notes = 48;
        int n_sixtyfourth_notes = 96;
    
        int i = 0;
        int j = n_half_notes;

        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((bar_length/3) == duration || (bar_length/3) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += n_quarter_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((bar_length/6) == duration || (bar_length/6) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += n_eighth_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((bar_length/12) == duration || (bar_length/12) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += n_sixteenth_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((bar_length/24) == duration || (bar_length/24) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += n_thirtysecond_notes;
        for (; i < j; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((bar_length/48) == duration || (bar_length/48) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += n_sixtyfourth_notes;
        for (; i < j-1; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue((bar_length/96) == duration || (bar_length/96) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }
    @Test
    void testCustomTupletSixteenOverFour() {
        final File file = new File("src/test/resources/gp_import_test_files/custom_tuplets_16_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }

        int beat_length = (60000/gp5File.tempo);
        int bar_length = beat_length * 16; // for 16/4
        int tuplet_duration = bar_length/13;
        for (int i = 0; i < note_positions.size()-1; i++) {
            int duration = note_positions.get(i+1) - note_positions.get(i);
            assertTrue(tuplet_duration == duration || tuplet_duration + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }

    @Test
    void testTempoChange1() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_1.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(0,500,        // 240 bpm (bar 1)
                               1000, 2000,   // 120 bpm (bar 2)
                               3000, 5000)); // 60 bpm (bar 3)

        assertTrue(note_positions.equals(expected_result));
    }
    @Test
    void testTempoChange2() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_2.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(0,250, 500, 750,          // 240 bpm (bar 1)
                               1000, 1500, 2000, 2500,   // 120 bpm (bar 2)
                               3000, 4000, 5000, 6000)); // 60 bpm (bar 3)

        assertTrue(note_positions.equals(expected_result));
    }
    @Test
    void testTempoChange3() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_3.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(0, 250, 500,              // 240 bpm (bar 1)
                               1000, 1500, 2000, 2500,   // 120 bpm (bar 1.5)
                               3500, 4500, 5500, 6500, 7500)); // 60 bpm (bar 2.5)

        assertTrue(note_positions.equals(expected_result));
    }
    @Test
    void testTempoChange4() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(0, 250, 500, 750,          // 240 bpm (bar 1)
                               1250, 1750, 2250, 2750,   // 120 bpm (bar 1.75)
                               3750, 4750, 5750, 6750)); // 60 bpm (bar 2.75)

        assertTrue(note_positions.equals(expected_result));
    }
    @Test
    void testTempoChange5() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_5.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(0, 250, 500, 750, 1000, 1250,          // 240 bpm (bar 1)
                               1750, // 120 bpm (bar 2.25)
                               2750, 3750, 4750, 5750, 6750)); // 60 bpm (bar 2.5)

        assertTrue(note_positions.equals(expected_result));
    }
    @Test
    void testTempoChange6() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_6.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(0, 250, 500, 750, // 240 bpm (bar 1)
                               1000, 1500, 2000, 2500, // 120 bpm (bar 2)
                               3000, 3250, 3500, 3750, // 240 bpm (bar 1)
                               4000, 4500, 5000, 5500)); // 120 bpm (bar 2)

        assertTrue(note_positions.equals(expected_result));
    }

    @Test
    void testGraceNote() {
        final File file = new File("src/test/resources/gp_import_test_files/grace_notes.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> note_positions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            note_positions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }

        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(0, 125,       // Grace note on beat
                               1875, 2000)); // Grace note before beat

        assertTrue(note_positions.equals(expected_result));
    }

}
