package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.nio.charset.StandardCharsets;

public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {

    /**
     * @param timeStampNs timestamp in nano seconds
     * @param icaoAddress of the aircraft
     * @param category    category of the aircraft which indicates its type
     * @param callSign    of the aircraft
     * @author Rudolf Yazbeck (SCIPER: 360700)
     */
    public AircraftIdentificationMessage {
        if (icaoAddress == null || callSign == null) {
            throw new NullPointerException();
        }
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * @param rawMessage raw message that has been intercepted from the aircraft
     * @return the identification message corresponding to the raw message that has been given, or null if one of the
     * callSign characters if invalid
     * @author Rudolf Yazbeck (SCIPER: 360700)
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        final int MEAttributeSize = 48; //constant that is the number of bits that represent the callSign characters and aircraft category
        int codeType = rawMessage.typeCode();
        int CA = Bits.extractUInt(rawMessage.payload(), MEAttributeSize, 3);

        //calculating the category of the aircraft
        codeType = 14 - codeType;
        int category;
        codeType <<= 4;
        category = ((codeType & 0b11110000) | CA);

        //array that has as its nth indexed element the (n+1)th letter of the alphabet used
        String[] letters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
                "Y", "Z"};

        StringBuilder callSignString = new StringBuilder();
        String stringToAdd;
        int callsignInt;

        for (int i = 0; i < 8; i++) {
            callsignInt = Bits.extractUInt(rawMessage.payload(), 42 - 6 * i, 6);
            if (callsignInt >= 1 && callsignInt <= 26) {
                stringToAdd = letters[callsignInt - 1];
            } else if (callsignInt >= 48 && callsignInt <= 57) {
                stringToAdd = Integer.toString(callsignInt - MEAttributeSize);
            } else if (callsignInt == 32) {
                stringToAdd = " ";
            } else {
                return null;
            }

            callSignString.append(stringToAdd);
        }

        callSignString = new StringBuilder(callSignString.toString().trim());

        CallSign callSign = new CallSign(callSignString.toString());
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }


    @Override
    public long timeStampNs() {
        return timeStampNs;
    }


    @Override
    public IcaoAddress icaoAddress() {
        return icaoAddress;
    }
}