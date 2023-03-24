package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.nio.charset.StandardCharsets;

public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message{

    /**
     * @author Rudolf Yazbeck
     * @param timeStampNs timestamp in nano seconds
     * @param icaoAddress of the aircraft
     * @param category category of the aircraft which indicates its type
     * @param callSign of the aircraft
     */
    public AircraftIdentificationMessage {
        if(icaoAddress == null || callSign == null) {
            throw new NullPointerException();
        }
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * @author Rudolf Yazbeck
     * @param rawMessage raw message that has been intercepted from the aircraft
     * @return the identification message corresponding to the raw message that has been given, or null if one of the
     * callSign characters if invalid
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        int codeType = rawMessage.typeCode();
        int CA = Bits.extractUInt(rawMessage.payload(), 48, 3);
        codeType = 14 - codeType;
        int category = 0b00000000;
        codeType <<= 4;
        category = ((codeType&0b11110000) | CA);

        String[] letters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
                "Y", "Z"};

        String callSignString = "";
        String stringToAdd;
        int callsignInt;
        for(int i = 0; i < 8; i++) {
            callsignInt = Bits.extractUInt(rawMessage.payload(), 42 - 6*i, 6);
            if(callsignInt >= 1 && callsignInt <= 26) {
                stringToAdd = letters[callsignInt - 1];
            } else if (callsignInt >= 48 && callsignInt <= 57) {
                stringToAdd = Integer.toString(callsignInt - 48);
            } else if(callsignInt == 32) {
                stringToAdd = " ";
            } else {
                return null;
            }

            callSignString += stringToAdd;
        }

        callSignString=callSignString.trim();

        CallSign callSign = new CallSign(callSignString);
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }

    /**
     *
     * @return the time stamp in nano seconds
     */
    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

    /**
     *
     * @return the icao address of the aircraft
     */
    @Override
    public IcaoAddress icaoAddress() {
        return icaoAddress;
    }
}
