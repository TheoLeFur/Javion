package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @param timeStampNs time stamp of the message in nanoseconds
 * @param bytes       14 bytes that make up an ADS-B  message
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {

    // number of bytes stored in ADS-B message
    public static final int LENGTH = 14;
    private static final Crc24 CRC_24 = new Crc24(Crc24.GENERATOR);
    private final static int DF_MESSAGE_LENGTH = 17;
    private final static int DF_START = 3;
    private final static int DF_SIZE = 5;
    private final static int TYPECODE_START = 51;
    private final static int TYPECOPE_LENGTH = 5;
    private final static int ME_ATTRIBUTE_START = 4;
    private final static int ME_ATTRIBUTE_SIZE = 7;
    private final static int MESSAGE_FORMAT_START = 0;
    private final static int MESSAGE_FORMAT_SIZE = 1;

    public RawMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(bytes.size() == LENGTH);
    }

    /**
     * @param timeStampsNs time stamp of the message in nanoseconds
     * @param bytes        14 bytes that make up an ADS-B  message
     * @return the ADS-B message with the same time stamps and bytes as the input
     * if the bytes have a CRC24 of 0
     */
    public static RawMessage of(long timeStampsNs, byte[] bytes) {
        return CRC_24.crc(bytes) != 0 ? null : new RawMessage(timeStampsNs, new ByteString(bytes));
    }

    /**
     * method used to get the size of a message
     *
     * @param byte0 first byte of the message
     * @return length of a message if it is of a known type, and 0 if that's not the case
     */
    public static int size(byte byte0) {
        return Bits.extractUInt(byte0, DF_START, DF_SIZE) == (byte) DF_MESSAGE_LENGTH ? LENGTH : 0;
    }

    /**
     * @param payload long from which the ME attribute will be extracted
     * @return the ME attribute of the long
     */
    public static int typeCode(long payload) {
        payload = Bits.extractUInt(payload, TYPECODE_START, TYPECOPE_LENGTH);
        return (int) payload;
    }

    /**
     * @return format of the message, which is the DF attribute stored in its first byte
     */
    public int downLinkFormat() {
        return Bits.extractUInt(bytes.bytesInRange(MESSAGE_FORMAT_START, MESSAGE_FORMAT_SIZE), DF_START, DF_SIZE);
    }

    /**
     * @return ICAO address of the expediter of the message
     */
    public IcaoAddress icaoAddress() {
        return new IcaoAddress(bytes.toString().substring(Short.BYTES, Short.BYTES + IcaoAddress.ICAO_ADDRESS_SIZE));
    }

    /**
     * @return returns the ME attribute of the message
     */
    public long payload() {
        return bytes.bytesInRange(ME_ATTRIBUTE_START, ME_ATTRIBUTE_SIZE + ME_ATTRIBUTE_START);
    }

    /**
     * @return the 5 most significant bits of the ME attribute
     */
    public int typeCode() {
        return typeCode(payload());
    }
}