// package log;
package log.charter.song;

import log.charter.gui.menuHandlers.FileMenuHandler;
import log.charter.io.gp.gp5.GP5File;
import log.charter.io.gp.gp5.GP5FileReader;
import log.charter.song.GPBarUnwrapper;
import log.charter.song.SongChart;
import log.charter.util.CollectionUtils.ArrayList2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;

public class TestGPBarUnwrapper {
    FileMenuHandler file_menu_handler;
    SongChart song;

    @BeforeEach
    void setUp() {
        file_menu_handler = new FileMenuHandler();
        song = new SongChart(30000, "test");
    }
    @Test
    void testRepeats1() {
        final File file = new File("src/test/resources/gp_import_test_files/repeat_test_1.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,1,2,1,1,2,3,1,1,2,1,1,2,3,1,1,2,1,1,2,3));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testRepeats2() {
        final File file = new File("src/test/resources/gp_import_test_files/repeat_test_2.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,1,2,2,3,2,2,3,4));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testAltEndings1() {
        final File file = new File("src/test/resources/gp_import_test_files/alt_ending_test_1.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,1,3,4));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testAltEndings2() {
        final File file = new File("src/test/resources/gp_import_test_files/alt_ending_test_2.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,1,3,1,2,1,4,5));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testAltEndings3() {
        final File file = new File("src/test/resources/gp_import_test_files/alt_ending_test_3.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,3,1,4,5));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testAltEndings4() {
        final File file = new File("src/test/resources/gp_import_test_files/alt_ending_test_4.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,3,4,1,2,5,6,7,8,9,10,7,8,11,12));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testDaCapo() {
        final File file = new File("src/test/resources/gp_import_test_files/da_capo.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,3,1,2,3));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testDaCapoAlFine() {
        final File file = new File("src/test/resources/gp_import_test_files/da_capo_al_fine.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,3,4,1,2,3,4,1,2));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testDaCoda() {
        final File file = new File("src/test/resources/gp_import_test_files/da_coda.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,3,4,1,2,3,4));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testDaDoubleCoda() {
        final File file = new File("src/test/resources/gp_import_test_files/da_double_coda.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,3,4,1,2,3,4));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testDalSegno() {
        final File file = new File("src/test/resources/gp_import_test_files/dal_segno.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,3,2,3));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testDalSegnoSegno() {
        final File file = new File("src/test/resources/gp_import_test_files/dal_segno_segno.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,3,4,1,2,3,2,3,4,1,2,3,4));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testRealComplexTabProgression1() {
        final File file = new File("src/test/resources/gp_import_test_files/real_complex_tab_progression_1.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,1,2,1,2,1,3,
                4,5,6,5,6,
                7,8,9,10,9,10,
                11,12,13,14,15,16,15,17,
                18,19,18,19,20,21,20,22,
                12,13,14,23,24,23,24,23,24,25,
                26,27,26,27,26,27,26,27,
                28,29,28,29,28,29,28,30));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
    @Test
    void testRealComplexTabProgression2() {
        final File file = new File("src/test/resources/gp_import_test_files/real_complex_tab_progression_2.gp5");
        final GP5File gp5File = GP5FileReader.importGPFile(file);
        final ArrayList2<GPBarUnwrapper> unwrapped = song.unwrapGP5File(gp5File);
        ArrayList<Integer> expected_result = new ArrayList<>(
            Arrays.asList(1,2,1,2,
                3,4,3,5,
                6,7,6,8,
                9,10,11,12,13,12,14,
                15,16,17,18,15,16,19,20,
                21,22,23,21,22,24,
                25,26,25,27,
                28,29,28,30,
                31,32,33,32,34,
                35,36,35,37, // Dal Segno al Coda
                21,22,23,21,22,24,
                25,26,25,27,
                28,29,28,30, // Da Coda
                38,39));

        assertTrue(unwrapped.get(0).bar_order.equals(expected_result));
    }
}
