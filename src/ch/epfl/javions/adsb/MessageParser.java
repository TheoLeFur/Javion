package ch.epfl.javions.adsb;

public final class MessageParser {

    /**
     * @author Theo Le Fur
     * Parses a raw message into an Identification Message, Position Message or Velocity Message, according to
     * the type code it carries.
     *
     * @param rawMessage Message we are parsing
     * @return an instance of AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage, or null,
     * in the case where the type code is different, or when the static method of returns null itself.
     */
    public static Message parse(RawMessage rawMessage) {
        int typeCode = rawMessage.typeCode();
        return switch (typeCode) {
            case 0 -> AircraftIdentificationMessage.of(rawMessage);
            case 1 -> AirbornePositionMessage.of(rawMessage);
            case 2 -> AirborneVelocityMessage.of(rawMessage);
            default -> null;
        };
    }
}
