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
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(parity == 0 || parity == 1);
        Preconditions.checkArgument(0 <= x && x < 1);
        Preconditions.checkArgument(0 <= y && y < 1);
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

        //number of bits that contain the encoded altitude
        final int BYTE_SIZE = 12;

        if (Q == 1) {
            int byte1 = Bits.extractUInt(bitAltitude, 5, 7);
            int byte2 = Bits.extractUInt(bitAltitude, 0, 4);

            altitude = -1000 + ((byte1 << 4) | byte2) * 25;
        } else {
            int demelage = 0;

            for (int i = 1; i <= 12; ++i) {
                int index = i <= 6 ? (8 + 2 * (i - 1)) % BYTE_SIZE : (7 + 2 * (i - 1)) % BYTE_SIZE;
                demelage |= Bits.extractUInt(bitAltitude, BYTE_SIZE - index, 1) << (BYTE_SIZE - i);
            }

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

