package ch.epfl.javions.adsb;

import java.util.Objects;

public final class MessageParser {
    /**
     * @param rawMessage Message we are parsing
     * @return an instance of AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage, or null,
     * in the case where the type code is different, or when the static method of returns null itself.
     * @author Theo Le Fur SCIPER : 363294
     * Parses a raw message into an Identification Message, Position Message or Velocity Message, according to
     * the type code it carries.
     */
    public static Message parse(RawMessage rawMessage) {
        int typeCode = rawMessage.typeCode();
        if (1 <= typeCode && typeCode <= 4) {
            return AircraftIdentificationMessage.of(rawMessage);
        } else if ((9 <= typeCode && typeCode <= 18) || (20 <= typeCode && typeCode <= 22)) {
            return AirbornePositionMessage.of(rawMessage);
        } else if (typeCode == 19) {
            return AirborneVelocityMessage.of(rawMessage);
        } else return null;
    }
}



