package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static java.lang.String.valueOf;

/**
 * @param timeStampNs timestamp in nano seconds
 * @param icaoAddress of the aircraft
 * @param category    category of the aircraft which indicates its type
 * @param callSign    of the aircraft
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {

    //constant that is the number of bits that represent the callSign characters and aircraft category
    static final int ME_ATTRIBUTE_SIZE = 48;
    static final int CA_SIZE = 3;
    static final int C1_START = 42;
    static final int CALLSIGN_CHARACTER_SIZE = 6;

    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * takes in a raw message and if that message corresponds to an aircraft identification message it will return it in
     * the corresponding type
     *
     * @param rawMessage raw message that has been intercepted from the aircraft
     * @return the identification message corresponding to the raw message that has been given, or null if one of the
     * callSign characters if invalid
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        int codeType = rawMessage.typeCode();
        int CA = Bits.extractUInt(rawMessage.payload(), ME_ATTRIBUTE_SIZE, CA_SIZE);

        //calculating the category of the aircraft
        codeType = 14 - codeType;
        int category;
        codeType <<= 4;
        category = ((codeType & 0b11110000) | CA);

        StringBuilder callSignString = new StringBuilder();
        String stringToAdd;
        int callsignInt;

        for (int i = 0; i < CallSign.CALLSIGN_MAX_LENGTH; i++) {
            callsignInt = Bits.extractUInt(rawMessage.payload(), C1_START - CALLSIGN_CHARACTER_SIZE * i, CALLSIGN_CHARACTER_SIZE);
            if (callsignInt >= 1 && callsignInt <= 26) {
                stringToAdd = valueOf((char) (callsignInt + 'A' - 1));
            } else if (callsignInt >= ME_ATTRIBUTE_SIZE && callsignInt <= 57) {
                stringToAdd = Integer.toString(callsignInt - ME_ATTRIBUTE_SIZE);
            } else if (callsignInt == 32) {
                stringToAdd = " ";
            } else {
                return null;
            }

            callSignString.append(stringToAdd);
        }

        CallSign callSign = new CallSign(callSignString.toString().stripTrailing());
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }
}