package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;

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
        long expected = (long)(Math.pow(2, 56) + Math.pow(2, 54));
        assertEquals(0b00010100, RawMessage.typeCode(expected));
    }

    @org.junit.jupiter.api.Test
    void downLinkFormat() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        int expectedVal = 0x8D;
        expectedVal = Bits.extractUInt(expectedVal, 4, 5);
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
    }

    @org.junit.jupiter.api.Test
    void testTypeCode() {
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
}