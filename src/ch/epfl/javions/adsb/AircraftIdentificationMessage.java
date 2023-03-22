package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.nio.charset.StandardCharsets;

public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message{

    /**
     *
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
     *
     * @param rawMessage raw message that has been intercepted from the aircraft
     * @return the identification message corresponding to the raw message that has been given, or null if one of the
     * callSign characters if invalid
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        int codeType = rawMessage.typeCode();
        int CA = Bits.extractUInt(rawMessage.payload(), 0, 3);
        codeType -= 14;
        int category = 0b00000000;
        category = (byte)category | (byte)CA;
        codeType <<= 4;
        category = (byte)category | (byte)codeType;

        String callSignString = new String(new byte[] {(byte)Bits.extractUInt(rawMessage.payload(), 4, 48)});

        while(callSignString.length() < 8) {
            callSignString += " ";
        }

        try{
            CallSign callSign = new CallSign(callSignString);
            return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
        } catch(IllegalArgumentException i) {
            return null;
        }

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
