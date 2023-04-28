package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

/**
 * @param timeStampNs time stamp of the message in nanoseconds
 * @param bytes       14 bytes that make up an ADS-B  message
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {

    //Length in bytes of ADSB messages
    public static final int LENGTH = 14;
    private static Crc24 CRC_24 = new Crc24(Crc24.GENERATOR);
    private final static int MESSAGE_FORMAT_START = 0;
    private final static int MESSAGE_FORMAT_SIZE = 1;
    private final static int ICAO_ADDRESS_START = 1;
    private final static int ICAO_ADDRESS_SIZE = 3;
    private final static int ME_ATTRIBUTE_START = 4;
    private final static int ME_ATTRIBUTE_SIZE = 7;
    private final static int CRC_START = 11;
    private final static int CRC_SIZE = 4;

    public RawMessage {
        Preconditions.checkArgument(timeStampNs >= 0 && bytes.size() == LENGTH);
    }

    /**
     * @param timeStampsNs time stamp of the message in nanoseconds
     * @param bytes        14 bytes that make up an ADS-B  message
     * @return the ADS-B message with the same time stamps and bytes as the input
     * if the bytes have a CRC24 of 0
     */
    public static RawMessage of(long timeStampsNs, byte[] bytes) {
        if (CRC_24.crc(bytes) != 0) {
            return null;
        } else {
            return new RawMessage(timeStampsNs, new ByteString(bytes));
        }
    }

    /**
     * method used to get the size of a message
     *
     * @param byte0 first byte of the message
     * @return length of a message if it is of a known type, and 0 if that's not the case
     */
    public static int size(byte byte0) {
        if (Bits.extractUInt(byte0, 3, 5) == (byte) 17) {
            //extracting the DF part from byte0
            return LENGTH;
        } else {
            //returning 0 if the message is not known
            return 0;
        }
    }

    /**
     * @param payload long from which the ME attribute will be extracted
     * @return the ME attribute of the long
     */
    public static int typeCode(long payload) {
        payload = Bits.extractUInt(payload, 51, 5);
        return (int) payload;
    }

    /**
     * @return format of the message, which is the DF attribute stored in its first byte
     */
    public int downLinkFormat() {
        return Bits.extractUInt(bytes.bytesInRange(0, 1), 3, 5);
    }

    /**
     * @return ICAO address of the expediter of the message
     */
    public IcaoAddress icaoAddress() {
        String icaoHexString = HexFormat.of()
                .toHexDigits(bytes.bytesInRange(1, 4))
                .toUpperCase();
        return new IcaoAddress(icaoHexString.substring(icaoHexString.length() - ICAO_ADDRESS_SIZE));
    }

    /**
     * @return returns the ME attribute of the message
     */
    public long payload() {
        return bytes.bytesInRange(4, 11);
    }

    /**
     * @return the 5 most significant bits of the ME attribute
     */
    public int typeCode() {
        return typeCode(payload());
    }
}