package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

public record RawMessage(long timeStampNs, ByteString bytes) {
    public static final int LENGTH = 14;

    /**
     * @param timeStampNs time stamp of the message in nano seconds
     * @param bytes       14 bytes that make up an ADS-B  message
     * @author Rudolf Yazbeck
     */
    public RawMessage {
        Preconditions.checkArgument(timeStampNs >= 0 && bytes.size() == LENGTH);
    }

    /**
     * @param timeStampsNs time stamp of the message in nano seconds
     * @param bytes        14 bytes that make up an ADS-B  message
     * @return the ADS-B message as it is with the same time stamps and bytes as the input
     * if the bytes have a CRC24 of 0
     * @author Rudolf Yazbeck
     */
    public static RawMessage of(long timeStampsNs, byte[] bytes) {
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);

        if (crc24.crc(bytes) != 0) {
            return null;
        } else {
            return new RawMessage(timeStampsNs, new ByteString(bytes));
        }
    }

    /**
     * @param byte0 first byte of the message
     * @return length of a message if it is of a known type, and 0 if that's not the case
     * @author Rudolf Yazbeck
     */
    public static int size(byte byte0) {
        if (Bits.extractUInt(byte0, 3, 5) == (byte) 17) { //extracting the DF part from byte0
            return LENGTH;
        } else { //returning 0 if the message is not known
            return 0;
        }
    }

    /**
     * @param payload long from which the ME attribute will be extracted
     * @return the ME attribute of the long
     * @author Rudolf Yazbeck
     */
    public static int typeCode(long payload) {
        payload = Bits.extractUInt(payload, 51, 5);
        return (int) payload;
    }

    /**
     * @return format of the message, which is the DF attribute stored in its first byte
     * @author Rudolf Yazbeck
     */
    public int downLinkFormat() {
        return Bits.extractUInt(bytes.bytesInRange(0, 1), 3, 5);
    }

    /**
     * @return ICAO address of the expediter of the message
     * @author Rudolf Yazbeck
     */
    public IcaoAddress icaoAddress() {
        String icaoStr = Long.toHexString(bytes.bytesInRange(1, 4));

        //when turning a long to a string the zeros at the beginning are not accounted for so they have to be added back manually
        while (icaoStr.length() < 6) {
            icaoStr = "0" + icaoStr;
        }
        icaoStr = icaoStr.toUpperCase();
        return new IcaoAddress(icaoStr);
    }

    /**
     * @return returns the ME attribute of the message
     * @author Rudolf Yazbeck
     */
    public long payload() {
        return bytes.bytesInRange(4, 11);
    }

    /**
     * @return the 5 most significant bits of the ME attribute
     * @author Rudolf Yazbeck
     */
    public int typeCode() {
        return typeCode(payload());
    }
}