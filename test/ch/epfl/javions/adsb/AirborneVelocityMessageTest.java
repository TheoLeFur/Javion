package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AirborneVelocityMessageTest {

    @Test
    void GenerallyWorksWithTypeCodeCondition() throws IOException {
        try (InputStream s = new FileInputStream(("/Users/theolefur/Downloads/Javions/resources/samples_20230304_1442.bin"))) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null){
                if(m.typeCode() == 19){
                    AirborneVelocityMessage avm = AirborneVelocityMessage.of(m);
                    if(avm != null){
                        System.out.println(avm);
                        ++i;
                    }
                }
            }
            System.out.println(i);
            assertEquals(147,i);
        }
    }

    @Test
    void GenerallyWorksWithNoCondition() throws IOException{
        try (InputStream s = new FileInputStream(("/Users/theolefur/Downloads/Javions/resources/samples_20230304_1442.bin"))) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null){
                AirborneVelocityMessage avm = AirborneVelocityMessage.of(m);
                if(avm != null){
                    System.out.println(avm);
                    ++i;
                }
            }
            System.out.println(i);
            assertEquals(230,i);
        }
    }

    @Test
    void SousType3Or4Works(){
        String message = "8DA05F219B06B6AF189400CBC33F";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timestamps = 0;
        RawMessage test = new RawMessage(timestamps, byteString);
        AirborneVelocityMessage avm = AirborneVelocityMessage.of(test);
        assertEquals(192.91666666666669 , avm.speed());
        assertEquals(4.25833066717054 , avm.trackOrHeading());
    }

    @Test
    void Subtype2Works() {
        String message =  "8D4B1A00EA0DC89E8F7C0857D5F5";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timeStampsNs = 0;
        RawMessage testMessage = new RawMessage(timeStampsNs, byteString);
        AirborneVelocityMessage avm = AirborneVelocityMessage.of(testMessage);
        System.out.println(avm);
    }

    @Test
    void testSubType12(){
        var message1 = RawMessage.of(100, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message1 != null;
        System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(1000, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5").getBytes());
        assert message2 != null;
        System.out.println(AirborneVelocityMessage.of(message2));
    }

    @Test
    void testSubType34() {

        var message1  = RawMessage.of(100, ByteString.ofHexadecimalString("8DA05F219B06B6AF189400CBC33F").t());
        assert message1 != null;
        System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(10000, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message2 != null;
        System.out.println(AirborneVelocityMessage.of(message2));
    }


}
