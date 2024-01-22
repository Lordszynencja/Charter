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
    FileMenuHandler fileMenuHandler;
    SongChart song;

    @BeforeEach
    void setUp() {
        fileMenuHandler = new FileMenuHandler();
        song = new SongChart(30000, "test");
    }
    @Test
    void testFourOverFour() {
        final File file = new File("src/test/resources/gp_import_test_files/all_note_lengths_4_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        int beatLength = (60000/gp5File.tempo);
        int barLength = beatLength * 4; // for 4/4

        int nWholeNotes = 1;
        int nHalfNotes = 2;
        int nQuarterNotes = 4;
        int nEighthNotes = 8;
        int nSixteenthNotes = 16;
        int nThirtysecondNotes = 32;
        int nSixtyfourthNotes = 64;
    
        int i = 0;
        int j = nWholeNotes;

        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength, duration);
        }
        j += nHalfNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/2, duration);
        }
        j += nQuarterNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/4, duration);
        }
        j += nEighthNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/8, duration);
        }
        j += nSixteenthNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/16, duration);
        }
        j += nThirtysecondNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/32, duration);
        }
        j += nSixtyfourthNotes;
        for (; i < j - 1; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((int)Math.round((barLength/64)) == duration || (int)Math.round((barLength/64)) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }
    @Test
    void testTwelveOverEight() {
        final File file = new File("src/test/resources/gp_import_test_files/all_note_lengths_12_8.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        int beatLength = (60000/gp5File.tempo);
        int barLength = beatLength * 6; // for 12/8

        int nHalfNotes = 3;
        int nQuarterNotes = 6;
        int nEighthNotes = 12;
        int nSixteenthNotes = 24;
        int nThirtysecondNotes = 48;
        int nSixtyfourthNotes = 96;
    
        int i = 0;
        int j = nHalfNotes;

        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/3, duration);
        }
        j += nQuarterNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/6, duration);
        }
        j += nEighthNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/12, duration);
        }
        j += nSixteenthNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/24, duration);
        }
        j += nThirtysecondNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertEquals(barLength/48, duration);
        }
        j += nSixtyfourthNotes;
        for (; i < j-1; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((barLength/96) == duration || (barLength/96) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }
    @Test
    void testTripletsFourOverFour() {
        final File file = new File("src/test/resources/gp_import_test_files/all_note_lengths_triplets_4_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        int beatLength = (60000/gp5File.tempo);
        int barLength = beatLength * 4; // for 4/4
        int nHalfNotes = 3;
        int nQuarterNotes = 6;
        int nEighthNotes = 12;
        int nSixteenthNotes = 24;
        int nThirtysecondNotes = 48;
        int nSixtyfourthNotes = 96;
    
        int i = 0;
        int j = nHalfNotes;

        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((barLength/3) == duration || (barLength/3) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += nQuarterNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((barLength/6) == duration || (barLength/6) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += nEighthNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((barLength/12) == duration || (barLength/12) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += nSixteenthNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((barLength/24) == duration || (barLength/24) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += nThirtysecondNotes;
        for (; i < j; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((barLength/48) == duration || (barLength/48) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
        j += nSixtyfourthNotes;
        for (; i < j-1; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue((barLength/96) == duration || (barLength/96) + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }
    @Test
    void testCustomTupletSixteenOverFour() {
        final File file = new File("src/test/resources/gp_import_test_files/custom_tuplets_16_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }

        int beatLength = (60000/gp5File.tempo);
        int barLength = beatLength * 16; // for 16/4
        int tupletDuration = barLength/13;
        for (int i = 0; i < notePositions.size()-1; i++) {
            int duration = notePositions.get(i+1) - notePositions.get(i);
            assertTrue(tupletDuration == duration || tupletDuration + 1 == duration); // Duration is 62,5, i.e. 62 or 63 depending on values
        }
    }

    @Test
    void testTempoChange1() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_1.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expectedResult = new ArrayList<>(
            Arrays.asList(0,500,        // 240 bpm (bar 1)
                               1000, 2000,   // 120 bpm (bar 2)
                               3000, 5000)); // 60 bpm (bar 3)

        assertTrue(notePositions.equals(expectedResult));
    }
    @Test
    void testTempoChange2() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_2.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expectedResult = new ArrayList<>(
            Arrays.asList(0,250, 500, 750,          // 240 bpm (bar 1)
                               1000, 1500, 2000, 2500,   // 120 bpm (bar 2)
                               3000, 4000, 5000, 6000)); // 60 bpm (bar 3)

        assertTrue(notePositions.equals(expectedResult));
    }
    @Test
    void testTempoChange3() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_3.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expectedResult = new ArrayList<>(
            Arrays.asList(0, 250, 500,              // 240 bpm (bar 1)
                               1000, 1500, 2000, 2500,   // 120 bpm (bar 1.5)
                               3500, 4500, 5500, 6500, 7500)); // 60 bpm (bar 2.5)

        assertTrue(notePositions.equals(expectedResult));
    }
    @Test
    void testTempoChange4() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expectedResult = new ArrayList<>(
            Arrays.asList(0, 250, 500, 750,          // 240 bpm (bar 1)
                               1250, 1750, 2250, 2750,   // 120 bpm (bar 1.75)
                               3750, 4750, 5750, 6750)); // 60 bpm (bar 2.75)

        assertTrue(notePositions.equals(expectedResult));
    }
    @Test
    void testTempoChange5() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_5.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expectedResult = new ArrayList<>(
            Arrays.asList(0, 250, 500, 750, 1000, 1250,          // 240 bpm (bar 1)
                               1750, // 120 bpm (bar 2.25)
                               2750, 3750, 4750, 5750, 6750)); // 60 bpm (bar 2.5)

        assertTrue(notePositions.equals(expectedResult));
    }
    @Test
    void testTempoChange6() {
        final File file = new File("src/test/resources/gp_import_test_files/tempo_change_6.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }
        ArrayList<Integer> expectedResult = new ArrayList<>(
            Arrays.asList(0, 250, 500, 750, // 240 bpm (bar 1)
                               1000, 1500, 2000, 2500, // 120 bpm (bar 2)
                               3000, 3250, 3500, 3750, // 240 bpm (bar 1)
                               4000, 4500, 5000, 5500)); // 120 bpm (bar 2)

        assertTrue(notePositions.equals(expectedResult));
    }

    @Test
    void testGraceNote() {
        final File file = new File("src/test/resources/gp_import_test_files/grace_notes.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        final ArrangementChart chart = new ArrangementChart(unwrapped,  gp5File.tracks.get(0));
        final ArrayList2<Integer> notePositions = new ArrayList2<>();
        for (int i = 0; i < chart.levels.get(0).chordsAndNotes.size(); i++) {
            notePositions.add(chart.levels.get(0).chordsAndNotes.get(i).note.position());
        }

        ArrayList<Integer> expectedResult = new ArrayList<>(
            Arrays.asList(0, 125,       // Grace note on beat
                               1875, 2000)); // Grace note before beat

        assertTrue(notePositions.equals(expectedResult));
    }

}
