package ch.epfl.javions.adsb;

import java.util.Objects;

public final class MessageParser {
    /**
     * @param rawMessage Message we are parsing
     * @return an instance of AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage, or null,
     * in the case where the type code is different, or when the static method of returns null itself.
     * @author Theo Le Fur
     * Parses a raw message into an Identification Message, Position Message or Velocity Message, according to
     * the type code it carries.
     */
    public static Message parse(RawMessage rawMessage) {
        if (!Objects.isNull(AirbornePositionMessage.of(rawMessage)))
            return AirbornePositionMessage.of(rawMessage);
        else {
            if (!Objects.isNull(AircraftIdentificationMessage.of(rawMessage))){
                return AircraftIdentificationMessage.of(rawMessage);
            } else {
                if (!Objects.isNull(AirborneVelocityMessage.of(rawMessage))){
                    return AirborneVelocityMessage.of(rawMessage);
                } else return null;
            }
        }
    }
}
