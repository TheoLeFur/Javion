package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress,
                                      double altitude, int parity, double x, double y) implements Message{

    public AirbornePositionMessage {
        if(icaoAddress == null) {
            throw new NullPointerException();
        }
        Preconditions.checkArgument(timeStampNs >= 0 &&
                (parity == 0 || parity == 1) && (0 <= x && x < 1) &&
                (0 <= y && y < 1));
    }

    private static int grayDecoder(int grayCode, int nbrBits) {
        int decoded = 0;
        for(int i = 0; i < nbrBits; i++) {
            decoded ^= grayCode >> i;
        }
        return decoded;
    }

    public static AirbornePositionMessage of(RawMessage rawMessage) {
        int bitAltitude = Bits.extractUInt(rawMessage.payload(), 36, 12);
        int Q = Bits.extractUInt(bitAltitude, 4, 1);
        double altitude;

        if(Q == 1) {
            int byte1 = Bits.extractUInt(bitAltitude, 5, 7);
            int byte2 = Bits.extractUInt(bitAltitude, 0, 4);

            altitude = -1000 + ((byte1 << 4) | byte2)*25;
        }
        else {
            int[] bits = new int[12];
            for(int j = 0; j < bits.length; j++) {
                bits[j] = Bits.extractUInt(bitAltitude, j, 1);
            }

            int demelage = (bits[7]<<11 | bits[9]<<10 | bits[11]<<9 | bits[1]<<8 | bits[3]<<7 | bits[5]<<6 |
                    bits[6]<<5 | bits[8]<<4 | bits[10]<<3 | bits[0]<<2 | bits[2]<<1 | bits[4]);

            //100 feet
            int weakBits = grayDecoder(Bits.extractUInt(demelage, 0, 3), 3);
            //500 feet
            int strongBits = grayDecoder(Bits.extractUInt(demelage, 3, 9), 9);

            if(weakBits == 0 || weakBits == 5 || strongBits == 6) {
                return null;
            } else if(weakBits == 7) {
                weakBits = 5;
            }
            if(strongBits%2 == 1) {
                weakBits = 6 - weakBits;
            }

            altitude = -1300 + 100*weakBits + 500*strongBits;
        }

        altitude = Units.convert(altitude, Units.Length.FOOT, Units.Length.METER);
        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitude,
                Bits.extractUInt(rawMessage.payload(), 34, 1), Bits.extractUInt(rawMessage.payload(), 0, 17)/Math.pow(2, 17),
                Bits.extractUInt(rawMessage.payload(), 17, 17)/Math.pow(2, 17));
    }
}

