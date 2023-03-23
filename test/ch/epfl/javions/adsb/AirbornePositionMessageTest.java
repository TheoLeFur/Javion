package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AirbornePositionMessageTest {
    private static final AirbornePositionMessage positionMessage1 = AirbornePositionMessage.of(new RawMessage(0, new ByteString(HexFormat.of().parseHex("8D39203559B225F07550ADBE328F"))));
    private static final AirbornePositionMessage positionMessage2 = AirbornePositionMessage.of(new RawMessage(1, new ByteString(HexFormat.of().parseHex("8DAE02C85864A5F5DD4975A1A3F5"))));
    @Test
    public void testAirbornePositionMessage() throws IOException {

        //String stream2 = Objects.requireNonNull(getClass().getResource("/home/rudolf/IdeaProjects/javion2/test/ch/epfl/javions/demodulation/samples_0304/samples_20230304_1442.bin")).getFile();
        String stream2 = URLDecoder.decode("/home/rudolf/IdeaProjects/javion2/test/ch/epfl/javions/demodulation/samples_0304/samples_20230304_1442.bin" , StandardCharsets.UTF_8);
        try (InputStream s = new FileInputStream(stream2)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int count0 = 0;
            int count1 = 0;

            while ((m = d.nextMessage()) != null) {
                if (m.timeStampNs() == 75898000){
                    System.out.println("");
                }
                if (AirbornePositionMessage.of(m) != null) {

                    count0++;
                    if ((m.typeCode() >= 9 && m.typeCode() <= 18) || (m.typeCode() >= 20 && m.typeCode() <= 22)) {
                        count1++;
                        System.out.println(AirbornePositionMessage.of(m));
                    }
                }
            }
            //number of messages before typeCode checking
            assertEquals(311, count0);
            //number of messages after typeCode checking
            assertEquals(137, count1);
        }
    }

    @Test
    public void constructorThrowsOnNullICAO(){
        assertThrows(NullPointerException.class, () -> new AirbornePositionMessage(758980000, null, 1046.08, 0, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnNegativeTimeStamp(){
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(-1, new IcaoAddress("495299"), 1046.08, 1, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnInvalidParity(){
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 2, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnNegativeX(){
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 0, -1, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnNegativeY(){
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 0, 0.6867904663085938, -1));
    }

    @Test
    public void constructorThrowsOnLimitX(){
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 0, 1., 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnLimitY() {
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 0, 0.6867904663085938, 1.));
    }

    @Test
    public void ofWorksOnSpecialMessages() {
        assert positionMessage1 != null;
        assertEquals(3474.72, positionMessage1.altitude(), 10e-2);
        assert positionMessage2 != null;
        assertEquals(7315.20 , positionMessage2.altitude(), 10e-2);
    }
}