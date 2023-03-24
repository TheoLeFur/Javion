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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AircraftIdentificationMessageTest {

    private final String URL = "/Users/theolefur/Downloads/Javions/resources/samples_20230304_1442.bin";
    @Test
    public void testAircraftIdentificationMessage() throws IOException {
        String stream2 = URLDecoder.decode(URL, StandardCharsets.UTF_8);
        try (InputStream s = new FileInputStream(stream2)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int count0 = 0;
            int count1 = 0;
            while ((m = d.nextMessage()) != null) {
                if (AircraftIdentificationMessage.of(m) != null) {
                    count0++;
                    if (m.typeCode() == 1 || m.typeCode() == 2 || m.typeCode() == 3 || m.typeCode() == 4) {
                        count1++;
                        System.out.println(AircraftIdentificationMessage.of(m));
                    }
                }
            }
            //number of messages before typeCode checking
            assertEquals(29, count0);
            //number of messages after typeCode checking
            assertEquals(14, count1);
        }
    }

    @Test
    public void constructorThrowsOnNullICAO() {
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(1499146900, null, 163, new CallSign("RYR7JD")));
    }

    @Test
    public void constructorThrowsOnNullCallSign() {
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(1499146900, new IcaoAddress("4D2228"), 163, null));
    }

    @Test
    public void constructorThrowsOnNegativeTimeStamp() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftIdentificationMessage(-1, new IcaoAddress("4D2228"), 163, new CallSign("RYR7JD")));
    }

    @Test
    void ofWorksOnTheGivenExamples() throws IOException {
        try (InputStream s = new FileInputStream((URL))) {
            String message = "8D4D2228234994B7284820323B81";
            ByteString byteString = ByteString.ofHexadecimalString(message);
            long timestamps = 1499146900L;
            RawMessage test = new RawMessage(timestamps , byteString);
            AircraftIdentificationMessage aim = AircraftIdentificationMessage.of(test);
            long timeStampNsEx = 1499146900L;
            IcaoAddress icaoEx = new IcaoAddress("4D2228");
            int categoryEx = 163;
            CallSign callSignEx = new CallSign("RYR7JD");
            assertEquals(timeStampNsEx , aim.timeStampNs());
            assertEquals(icaoEx , aim.icaoAddress());
            assertEquals(categoryEx , aim.category());
            assertEquals(callSignEx , aim.callSign());

            String message1 = "8F01024C233530F3CF6C60A19669";
            ByteString byteString1 = ByteString.ofHexadecimalString(message1);
            long timestamps1 = 2240535600L;
            RawMessage test1 = new RawMessage(timestamps1 , byteString1);
            AircraftIdentificationMessage aim1 = AircraftIdentificationMessage.of(test1);
            long timeStampNsEx1 = 2240535600L;
            IcaoAddress icaoEx1 = new IcaoAddress("01024C");
            int categoryEx1 = 163;
            CallSign callSignEx1 = new CallSign("MSC3361");
            assertEquals(timeStampNsEx1 , aim1.timeStampNs());
            assertEquals(icaoEx1 , aim1.icaoAddress());
            assertEquals(categoryEx1 , aim1.category());
            assertEquals(callSignEx1 , aim1.callSign());

            String message2 = "8D49529923501439CF1820419C55";
            ByteString byteString2 = ByteString.ofHexadecimalString(message2);
            long timestamps2 = 2698727800L;
            RawMessage test2 = new RawMessage(timestamps2 , byteString2);
            AircraftIdentificationMessage aim2 = AircraftIdentificationMessage.of(test2);
            long timeStampNsEx2 = 2698727800L;
            IcaoAddress icaoEx2 = new IcaoAddress("495299");
            int categoryEx2 = 163;
            CallSign callSignEx2 = new CallSign("TAP931");
            assertEquals(timeStampNsEx2 , aim2.timeStampNs());
            assertEquals(icaoEx2 , aim2.icaoAddress());
            assertEquals(categoryEx2 , aim2.category());
            assertEquals(callSignEx2 , aim2.callSign());

            String message3 = "8DA4F23925101331D73820FC8E9F";
            ByteString byteString3 = ByteString.ofHexadecimalString(message3);
            long timestamps3 = 3215880100L;
            RawMessage test3 = new RawMessage(timestamps3 , byteString3);
            AircraftIdentificationMessage aim3 = AircraftIdentificationMessage.of(test3);
            long timeStampNsEx3 = 3215880100L;
            IcaoAddress icaoEx3 = new IcaoAddress("A4F239");
            int categoryEx3 = 165;
            CallSign callSignEx3 = new CallSign("DAL153");
            assertEquals(timeStampNsEx3 , aim3.timeStampNs());
            assertEquals(icaoEx3 , aim3.icaoAddress());
            assertEquals(categoryEx3 , aim3.category());
            assertEquals(callSignEx3 , aim3.callSign());

            String message4 = "8D4B2964212024123E0820939C6F";
            ByteString byteString4 = ByteString.ofHexadecimalString(message4);
            long timestamps4 = 4103219900L;
            RawMessage test4 = new RawMessage(timestamps4 , byteString4);
            AircraftIdentificationMessage aim4 = AircraftIdentificationMessage.of(test4);
            long timeStampNsEx4 = 4103219900L;
            IcaoAddress icaoEx4 = new IcaoAddress("4B2964");
            int categoryEx4 = 161;
            CallSign callSignEx4 = new CallSign("HBPRO");
            assertEquals(timeStampNsEx4 , aim4.timeStampNs());
            assertEquals(icaoEx4 , aim4.icaoAddress());
            assertEquals(categoryEx4 , aim4.category());
            assertEquals(callSignEx4 , aim4.callSign());
        }
    }

    //Visually Check First 5.
    @Test
    void GenerallyWorksWithTypeCodeCondition() throws IOException{
        try (InputStream s = new FileInputStream((URL))) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null){
                if(m.typeCode()>=1 && m.typeCode()<=4){
                    AircraftIdentificationMessage aim = AircraftIdentificationMessage.of(m);
                    if(aim != null){
                        System.out.println(aim);
                        ++i;
                    }
                }
            }
            System.out.println(i);
            assertEquals(14,i);
        }
    }

    @Test
    void GenerallyWorksWithNoCondition() throws IOException{
        try (InputStream s = new FileInputStream(URL)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null){
                AircraftIdentificationMessage aim = AircraftIdentificationMessage.of(m);
                if(aim != null){
                    System.out.println(aim);
                    ++i;
                }
            }
            System.out.println(i);
            assertEquals(29,i);
        }
    }
}