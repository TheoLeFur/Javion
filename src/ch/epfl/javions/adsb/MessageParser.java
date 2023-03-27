package ch.epfl.javions.adsb;

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
        if (AirbornePositionMessage.of(rawMessage) != null) {
            return AirbornePositionMessage.of(rawMessage);
        } else {
            if (AircraftIdentificationMessage.of(rawMessage) != null) {
                return AircraftIdentificationMessage.of(rawMessage);
            } else {
                return AirborneVelocityMessage.of(rawMessage);
            }
        }
    }
}
