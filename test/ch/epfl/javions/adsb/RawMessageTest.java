package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class RawMessageTest {

    @org.junit.jupiter.api.Test
    void of() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        assertNull(RawMessage.of(0, new byte[]{(byte) 0b10001111}));
        long testVal = 5;
        assertEquals(new RawMessage(testVal, new ByteString(mAndC)), RawMessage.of(testVal, mAndC));
    }

    @org.junit.jupiter.api.Test
    void size() {
        assertEquals(14, RawMessage.size((byte) 0b10001000));
        assertEquals(14, RawMessage.size((byte) 0b10001110));
        assertEquals(0, RawMessage.size((byte) 0b10101000));
    }

    @org.junit.jupiter.api.Test
    void typeCode() {
        long expected = (long)(Math.pow(2, 55) + Math.pow(2, 53));
        assertEquals(0b00010100, RawMessage.typeCode(expected));
    }

    @org.junit.jupiter.api.Test
    void downLinkFormat() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        int expectedVal = 0x8D;
        expectedVal = Bits.extractUInt(expectedVal, 3, 5);
        assertEquals(expectedVal, testMsg.downLinkFormat());

    }

    @org.junit.jupiter.api.Test
    void icaoAddress() {

        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        assertEquals(new IcaoAddress("4B18F4"), testMsg.icaoAddress());
    }

    @org.junit.jupiter.api.Test
    void payload() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        assertEquals("231445F2DB63A0", Long.toHexString(testMsg.payload()).toUpperCase());
    }

    @org.junit.jupiter.api.Test
    void testTypeCode() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        Long longy = 9873914844636064L;
        assertEquals(Bits.extractUInt(longy, 51, 5), testMsg.typeCode());
    }

    @org.junit.jupiter.api.Test
    void timeStampNs() {

        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);

        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-2, new ByteString(mAndC)));
    }

    @org.junit.jupiter.api.Test
    void bytes() {

        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);

        assertThrows(IllegalArgumentException.class, () -> new RawMessage(2, new ByteString(new byte[] {0b10001})));
    }

    @Test
    void SizeTest(){
        byte test1 = 70;
        byte test2 = 0b01000110;
        byte test3 = 0x46;
        byte test4 = (byte) 0x8D;
        int Test1 = RawMessage.size(test1);
        int Test2 = RawMessage.size(test2);
        int Test3 = RawMessage.size(test3);
        int Test4 = RawMessage.size(test4);
        assertEquals(0, Test1);
        assertEquals(0,Test2);
        assertEquals(0,Test3);
        assertEquals(14,Test4);
    }

    @Test
    void AllMethodsInOneTest(){
        String message = "8D4B17E5F8210002004BB8B1F1AC";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        int timestamps = 0;
        RawMessage test = new RawMessage(timestamps , byteString);
        IcaoAddress icao = test.icaoAddress();
        int typecode = test.typeCode();
        int df = test.downLinkFormat();
        long payload = test.payload();
        int typecode2 = RawMessage.typeCode(payload);
        assertEquals( "4B17E5",icao.string());
        assertEquals(17,df);
        assertEquals(31,typecode);
        assertEquals(31,typecode2);

        String message2 = "8D49529958B302E6E15FA352306B";
        ByteString byteString2 = ByteString.ofHexadecimalString(message2);
        int timestamps2 = 0;
        RawMessage test2 = new RawMessage(timestamps2 , byteString2);
        IcaoAddress icao2 = test2.icaoAddress();
        int typecode22 = test2.typeCode();
        int df2 = test2.downLinkFormat();
        long payload2 = test2.payload();
        int typecode222 = RawMessage.typeCode(payload2);
        assertEquals( "495299",icao2.string());
        assertEquals(17,df2);
        assertEquals(11,typecode22);
        assertEquals(11,typecode222);
    }

    @Test
    void TestForAIM(){
        String message1 = "8F01024C233530F3CF6C60A19669";
        ByteString byteString = ByteString.ofHexadecimalString(message1);
        long timestamps = 2240535600L;
        RawMessage test = new RawMessage(timestamps , byteString);
        IcaoAddress icao = test.icaoAddress();
        System.out.println(icao);
    }
    String string1 = "RawMessage[timeStampNs=8096200, bytes=8D_4B17E5_F8210002004BB8_B1F1AC]";
    String string2 = "RawMessage[timeStampNs=75898000, bytes=8D_495299_58B30E6E15FA3_52306B]";
    String string3 = "RawMessage[timeStampNs=100775400, bytes=8D_39D300_990CE72C700890_58AD77]";
    String string4 = "RawMessage[timeStampNs=116538700, bytes=8D4241A9601B32DA4367C4C3965E]";
    String string5 = "RawMessage[timeStampNs=129268900, bytes=8D4B1A00EA0DC89E8F7C0857D5F5]";


    RawMessage rm1 = new RawMessage(8096200, ByteString.ofHexadecimalString("8D4B17E5F8210002004BB8B1F1AC"));
    RawMessage rm2 = new RawMessage(75898000, ByteString.ofHexadecimalString("8D49529958B302E6E15FA352306B"));
    RawMessage rm3 = new RawMessage(100775400, ByteString.ofHexadecimalString("8D39D300990CE72C70089058AD77"));
    RawMessage rm4 = new RawMessage(116538700, ByteString.ofHexadecimalString("8D4241A9601B32DA4367C4C3965E"));
    RawMessage rm5 = new RawMessage(129268900, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5"));


    @Test
    void RawMessageConstructorTest() {
        /*
        assertEquals(string1, rm1.toString());
        assertEquals(string2, rm2.toString());
        assertEquals(string3, rm3.toString());

         */
        assertEquals(string4, rm4.toString());
        assertEquals(string5, rm5.toString());

    }

    @Test
    void RawMessageConstructorThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RawMessage(-1246432432, ByteString.ofHexadecimalString("8D4B17E5F8210002004BB8B1F1AC"));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new RawMessage(1246432432, ByteString.ofHexadecimalString("8D4B17E5F8210002004BB8B1F1ACA"));
        });
    }

    @Test
    void ofTest() {
        assertEquals(rm1, RawMessage.of(8096200, HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC")));
        assertNull(RawMessage.of(8096200, HexFormat.of().parseHex("8D43B17E5F82100025004BB8B1F1AC")));

    }

    @Test
    void sizeTest() {

        assertEquals(14, RawMessage.size((byte) 0b1000_1000));
        assertEquals(0, RawMessage.size((byte) 0b1010_1000));
        assertEquals(14, RawMessage.size((byte) 0b1000_1011));

    }

    @Test
    void typeCodeStaticTest() {
        long me1 = HexFormat.fromHexDigitsToLong("F8210002004BB8");
        assertEquals(0b11111, RawMessage.typeCode(me1));

        long me2 = HexFormat.fromHexDigitsToLong("58B302E6E15FA3");
        assertEquals(0b01011, RawMessage.typeCode(me2));

        long me3 = HexFormat.fromHexDigitsToLong("990CE72C700890");
        assertEquals(0b10011, RawMessage.typeCode(me3));
    }

    @Test
    void downLinkFormatTest() {
        assertEquals(17, rm1.downLinkFormat());

    }

    @Test
    void icaoAddressTest() {
        IcaoAddress adress = rm1.icaoAddress();
        assertEquals(new IcaoAddress("4B17E5"), rm1.icaoAddress());
        assertEquals(new IcaoAddress("495299"), rm2.icaoAddress());
    }


    @Test
    void payloadTest() {
        assertEquals(HexFormat.fromHexDigitsToLong("F8210002004BB8") ,rm1.payload());
    }
    @Test
    void typeCodeTest(){
        assertEquals(0b11111, rm1.typeCode());
        assertEquals(0b01011, rm2.typeCode());
        assertEquals(0b10011, rm3.typeCode());
    }
}