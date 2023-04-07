package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * @param timeStampNs time-stamp in nanoseconds
 * @param icaoAddress ICAO address of the message's expediter
 * @param altitude    of the aircraft at the time the message was sent
 * @param parity      of the message (0 or 1)
 * @param x           normalized local longitude
 * @param y           normalized local latitude
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x,
                                      double y) implements Message {


    /**
     * @param timeStampNs time-stamp in nanoseconds
     * @param icaoAddress ICAO address of the message's expediter
     * @param altitude    of the aircraft at the time the message was sent
     * @param parity      of the message (0 or 1)
     * @param x           normalized local longitude
     * @param y           normalized local latitude
     * @author Rudolf Yazbeck (SCIPER 360700)
     */

    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0 &&
                (parity == 0 || parity == 1) && (0 <= x && x < 1) &&
                (0 <= y && y < 1));
    }

    /**
     * method that decodes a message that has been encoded with Gray's algorithm
     *
     * @param grayCode int that has been encoded with Gray's algorithm
     * @param nbrBits  number of bits that the encoded message contains
     * @return decoded message
     */
    private static int grayDecoder(int grayCode, int nbrBits) {
        int decoded = grayCode;
        for (int i = 1; i < nbrBits; i++) {
            decoded = decoded ^ (grayCode >> i);
        }
        return decoded;
    }

    /**
     * @param rawMessage of the aircraft
     * @return the airborne position message corresponding to the raw message given, or null if
     * the altitude is invalid
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {

        int bitAltitude = Bits.extractUInt(rawMessage.payload(), 36, 12);
        int Q = Bits.extractUInt(bitAltitude, 4, 1);
        double altitude;

        if (Q == 1) {
            int byte1 = Bits.extractUInt(bitAltitude, 5, 7);
            int byte2 = Bits.extractUInt(bitAltitude, 0, 4);

            altitude = -1000 + ((byte1 << 4) | byte2) * 25;
        } else {
            //number of bits that contain the encoded altitude
            final int BYTE_SIZE = 12;

            //extracting the bits individually instead of an array/for loop to save complexity cost
            int bits0 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 1, 1);
            int bits1 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 2, 1);
            int bits2 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 3, 1);
            int bits3 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 4, 1);
            int bits4 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 5, 1);
            int bits5 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 6, 1);
            int bits6 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 7, 1);
            int bits7 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 8, 1);
            int bits8 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 9, 1);
            int bits9 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 10, 1);
            int bits10 = Bits.extractUInt(bitAltitude, BYTE_SIZE - 11, 1);
            int bits11 = Bits.extractUInt(bitAltitude, 0, 1);

            int demelage = (bits7 << 11 | bits9 << 10 | bits11 << 9 | bits1 << 8 | bits3 << 7 | bits5 << 6 |
                    bits6 << 5 | bits8 << 4 | bits10 << 3 | bits0 << 2 | bits2 << 1 | bits4);

            //100 feet
            int weakBits = grayDecoder(Bits.extractUInt(demelage, 0, 3), 3);
            //500 feet
            int strongBits = grayDecoder(Bits.extractUInt(demelage, 3, 9), 9);

            if (weakBits == 0 || weakBits == 5 || weakBits == 6) {
                return null;
            } else if (weakBits == 7) {
                weakBits = 5;
            }
            if (strongBits % 2 == 1) {
                weakBits = 6 - weakBits;
            }

            altitude = -1300 + 100 * weakBits + 500 * strongBits;
        }

        altitude = Units.convert(altitude, Units.Length.FOOT, Units.Length.METER);
        if (altitude < 0) {
            return null;
        }
        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitude,
                Bits.extractUInt(rawMessage.payload(), 34, 1), Bits.extractUInt(rawMessage.payload(), 0, 17) / Math.pow(2, 17),
                Bits.extractUInt(rawMessage.payload(), 17, 17) / Math.pow(2, 17));
    }
}

