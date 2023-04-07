package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.nio.charset.StandardCharsets;

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

    public AircraftIdentificationMessage {
        if (icaoAddress == null || callSign == null) {
            throw new NullPointerException();
        }
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
        final int ME_ATTRIBUTE_SIZE = 48; //constant that is the number of bits that represent the callSign characters and aircraft category
        int codeType = rawMessage.typeCode();
        int CA = Bits.extractUInt(rawMessage.payload(), ME_ATTRIBUTE_SIZE, 3);

        //calculating the category of the aircraft
        codeType = 14 - codeType;
        int category;
        codeType <<= 4;
        category = ((codeType & 0b11110000) | CA);

        StringBuilder callSignString = new StringBuilder();
        String stringToAdd;
        int callsignInt;

        for (int i = 0; i < 8; i++) {
            callsignInt = Bits.extractUInt(rawMessage.payload(), 42 - 6 * i, 6);
            if (callsignInt >= 1 && callsignInt <= 26) {
                stringToAdd = valueOf((char) (callsignInt + 64));
            } else if (callsignInt >= ME_ATTRIBUTE_SIZE && callsignInt <= 57) {
                stringToAdd = Integer.toString(callsignInt - ME_ATTRIBUTE_SIZE);
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