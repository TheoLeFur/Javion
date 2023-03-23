package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.*;

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
        String stream2 = URLDecoder.decode("/home/rudolf/IdeaProjects/javion2/test/ch/epfl/javions/demodulation/samples_0304/samples_20230304_1442.bin", StandardCharsets.UTF_8);
        try (InputStream s = new FileInputStream(stream2)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int count0 = 0;
            int count1 = 0;

            while ((m = d.nextMessage()) != null) {
                //if (m.timeStampNs() == 75898000){
                //    System.out.println("");
                //}
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
    public void constructorThrowsOnNullICAO() {
        assertThrows(NullPointerException.class, () -> new AirbornePositionMessage(758980000, null, 1046.08, 0, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnNegativeTimeStamp() {
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(-1, new IcaoAddress("495299"), 1046.08, 1, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnInvalidParity() {
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 2, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnNegativeX() {
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 0, -1, 0.7254638671875));
    }

    @Test
    public void constructorThrowsOnNegativeY() {
        assertThrows(IllegalArgumentException.class, () -> new AirbornePositionMessage(758980000, new IcaoAddress("495299"), 1046.08, 0, 0.6867904663085938, -1));
    }

    @Test
    public void constructorThrowsOnLimitX() {
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
        assertEquals(7315.20, positionMessage2.altitude(), 10e-2);
    }


    /*@Test
    void permutationWorks(){
        long l = 0b100010100011;
        long perm = AirbornePositionMessage.permutation(l);
        assertEquals(0b1000101101 , perm);
    }

    @Test
    void GrayDecoderWorks(){
        long l = 0b110;
        int i = AirbornePositionMessage.GrayDecoder(l,3);
        assertEquals(4,i);
    }*/

    @Test
    void ofWorksOnTheGivenExamples() throws IOException {
        try (InputStream s = new FileInputStream(("/home/rudolf/IdeaProjects/JavionsVersion/Javion/test/ch/epfl/javions/demodulation/samples_0304/samples_20230304_1442.bin"))) {
            String message = "8D49529958B302E6E15FA352306B";
            ByteString byteString = ByteString.ofHexadecimalString(message);
            long timestamps = 75898000;
            RawMessage test = new RawMessage(timestamps, byteString);
            AirbornePositionMessage apm = AirbornePositionMessage.of(test);
            long timeStampNsEx = 75898000;
            int parityEx = 0;
            double altEx = 10546.08;
            double xEx = 0.6867904663085938;
            double yEx = 0.7254638671875;
            IcaoAddress icaoEx = new IcaoAddress("495299");
            assertEquals(timeStampNsEx, apm.timeStampNs());
            assertEquals(parityEx, apm.parity());
            assertEquals(altEx, apm.altitude());
            assertEquals(xEx, apm.x());
            assertEquals(yEx, apm.y());
            assertEquals(icaoEx, apm.icaoAddress());

            String message2 = "8D4241A9601B32DA4367C4C3965E";
            ByteString byteString2 = ByteString.ofHexadecimalString(message2);
            long timestamps2 = 116538700;
            RawMessage test2 = new RawMessage(timestamps2, byteString2);
            AirbornePositionMessage apm2 = AirbornePositionMessage.of(test2);
            long timeStampNsEx2 = 116538700;
            int parityEx2 = 0;
            double altEx2 = 1303.02;
            double xEx2 = 0.702667236328125;
            double yEx2 = 0.7131423950195312;
            IcaoAddress icaoEx2 = new IcaoAddress("4241A9");
            assertEquals(timeStampNsEx2, apm2.timeStampNs());
            assertEquals(parityEx2, apm2.parity());
            assertEquals(altEx2, apm2.altitude());
            assertEquals(xEx2, apm2.x());
            assertEquals(yEx2, apm2.y());
            assertEquals(icaoEx2, apm2.icaoAddress());

            String message3 = "8D4D222860B985F7F53FAB33CE76";
            ByteString byteString3 = ByteString.ofHexadecimalString(message3);
            long timestamps3 = 138560100 ;
            RawMessage test3 = new RawMessage(timestamps3, byteString3);
            AirbornePositionMessage apm3 = AirbornePositionMessage.of(test3);
            long timeStampNsEx3 = 138560100;
            int parityEx3 = 1;
            double altEx3 = 10972.800000000001;
            double xEx3 = 0.6243515014648438;
            double yEx3 = 0.4921417236328125;
            IcaoAddress icaoEx3 = new IcaoAddress("4D2228");
            assertEquals(timeStampNsEx3, apm3.timeStampNs());
            assertEquals(parityEx3, apm3.parity());
            assertEquals(altEx3, apm3.altitude());
            assertEquals(xEx3, apm3.x());
            assertEquals(yEx3, apm3.y());
            assertEquals(icaoEx3, apm3.icaoAddress());

            String message4 = "8D4D029F594B52EFDB7E94ACEAC8";
            ByteString byteString4 = ByteString.ofHexadecimalString(message4);
            long timestamps4 = 208135700;
            RawMessage test4 = new RawMessage(timestamps4, byteString4);
            AirbornePositionMessage apm4 = AirbornePositionMessage.of(test4);
            long timeStampNsEx4 = 208135700;
            int parityEx4 = 0;
            double altEx4 = 4244.34;
            double xEx4 = 0.747222900390625;
            double yEx4 = 0.7342300415039062;
            IcaoAddress icaoEx4 = new IcaoAddress("4D029F");
            assertEquals(timeStampNsEx4, apm4.timeStampNs());
            assertEquals(parityEx4, apm4.parity());
            assertEquals(altEx4, apm4.altitude());
            assertEquals(xEx4, apm4.x());
            assertEquals(yEx4, apm4.y());
            assertEquals(icaoEx4, apm4.icaoAddress());

            String message5 = "8D3C648158AF92F723BC275EC692";
            ByteString byteString5 = ByteString.ofHexadecimalString(message5);
            long timestamps5 = 233069800;
            RawMessage test5 = new RawMessage(timestamps5, byteString5);
            AirbornePositionMessage apm5 = AirbornePositionMessage.of(test5);
            long timeStampNsEx5 = 233069800;
            int parityEx5 = 0;
            double altEx5 = 10370.82;
            double xEx5 = 0.8674850463867188;
            double yEx5 = 0.7413406372070312;
            IcaoAddress icaoEx5 = new IcaoAddress("3C6481");
            assertEquals(timeStampNsEx5, apm5.timeStampNs());
            assertEquals(parityEx5, apm5.parity());
            assertEquals(altEx5, apm5.altitude());
            assertEquals(xEx5, apm5.x());
            assertEquals(yEx5, apm5.y());
            assertEquals(icaoEx5, apm5.icaoAddress());
        }
    }

    @Test
    void petitTest(){
        String message = "8D39203559B225F07550ADBE328F";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timestamps = 0;
        RawMessage test = new RawMessage(timestamps, byteString);
        AirbornePositionMessage apm = AirbornePositionMessage.of(test);
        assertEquals(3474.7200000000003 , apm.altitude());

        String message2 = "8DAE02C85864A5F5DD4975A1A3F5";
        ByteString byteString2 = ByteString.ofHexadecimalString(message2);
        long timestamps2 = 0;
        RawMessage test2 = new RawMessage(timestamps2, byteString2);
        AirbornePositionMessage apm2 = AirbornePositionMessage.of(test2);
        assertEquals(7315.200000000001 , apm2.altitude());
    }

    //Visually Check First 5.
    @Test
    void GenerallyWorksWithTypeCodeCondition() throws IOException{
        try (InputStream s = new FileInputStream(("/home/rudolf/IdeaProjects/JavionsVersion/Javion/test/ch/epfl/javions/demodulation/samples_0304/samples_20230304_1442.bin"))) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null){
                if((m.typeCode()>=9 && m.typeCode()<=18)||(m.typeCode()>=20 && m.typeCode()<=22)){
                    AirbornePositionMessage apm = AirbornePositionMessage.of(m);
                    if(apm != null){
                        System.out.println(apm);
                        ++i;
                    }
                }
            }
            System.out.println(i);
            assertEquals(137,i);
        }
    }

    @Test
    void GenerallyWorksWithNoCondition() throws IOException{
        try (InputStream s = new FileInputStream(("/home/rudolf/IdeaProjects/JavionsVersion/Javion/test/ch/epfl/javions/demodulation/samples_0304/samples_20230304_1442.bin"))) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null){
                AirbornePositionMessage apm = AirbornePositionMessage.of(m);
                if(apm != null){
                    System.out.println(apm);
                    ++i;
                }
            }
            System.out.println(i);
            assertEquals(311,i);
        }
    }
}