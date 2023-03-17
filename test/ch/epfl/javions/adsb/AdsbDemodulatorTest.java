package ch.epfl.javions.adsb;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AdsbDemodulatorTest {

    @Test
    void AdsbDemodlatorWorksCorrectly() throws IOException {
        String f = "/Users/theolefur/Downloads/Javions/test/ch/epfl/test_ressources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null)
                System.out.println(m);
        }
    }
}
