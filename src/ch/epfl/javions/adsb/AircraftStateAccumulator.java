package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.util.Objects;

/**
 * @param <T> Generic parameter, inherits from AircraftStateSetter.
 * @author Theo Le Fur (SCIPER: 363294)
 */
public class AircraftStateAccumulator<T extends AircraftStateSetter> {

    private final T stateSetter;

    // array of size 2 which will memorize the two most recent messages of opposite parity at each update
    private final AirbornePositionMessage[] previousMessageMemory;

    // POSITION_THRESHOLD_NS = 10 seconds
    private final double POSITION_THRESHOLD_NS = 10 * Math.pow(10, 9);

    /**
     * @param stateSetter state setter.
     *                    Returns an AircraftStateAccumulator object. We will be storing the previous messages of different parity in\
     *                    an array of size 2. This will be convenient for updates.
     */
    public AircraftStateAccumulator(T stateSetter) {
        this.stateSetter = Objects.requireNonNull(stateSetter);
        this.previousMessageMemory = new AirbornePositionMessage[2];
    }

    /**
     * @return state setter passed in the constructor.
     * Access the state setter
     */
    public T stateSetter() {
        return this.stateSetter;
    }

    /**
     * @param message message
     *                Updates the state of the aircraft using the data passed into the message. Verifies of which of the three
     *                type, AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage the message is, and then updates
     *                the state with the data carried by the message.
     */
    public void update(Message message) {

        this.stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {

            case AircraftIdentificationMessage aim -> {
                this.stateSetter.setCallSign(aim.callSign());
                this.stateSetter.setCategory(aim.category());
            }
            case AirbornePositionMessage aim -> {
                this.stateSetter.setAltitude(aim.altitude());

                AirbornePositionMessage prevMessage = this.oppParRecentMessage(aim);
                if (!Objects.isNull(prevMessage)) {
                    if (this.posMessageCondition(aim, prevMessage)) {
                        if (this.getPosition(aim, prevMessage) != null) {
                            this.stateSetter.setPosition(this.getPosition(aim, prevMessage));
                        }
                    }
                }

                this.addToMemory(aim);
            }
            case AirborneVelocityMessage aim -> {
                this.stateSetter.setVelocity(aim.speed());
                this.stateSetter.setTrackOrHeading(aim.trackOrHeading());
            }

            default -> {
                // do nothing
            }
        }
    }


    /**
     * @param message         message of whom we want to update the position
     * @param previousMessage previous position message of different parity
     * @return True if position can be updated
     * Verifies if position can be updated, by looking at the previous message of different parity
     * and checking whether it is sufficiently recent.
     */
    private boolean posMessageCondition(AirbornePositionMessage message, AirbornePositionMessage previousMessage) {
        return (message.timeStampNs() - previousMessage.timeStampNs()) <= POSITION_THRESHOLD_NS;
    }

    /**
     * @param x {0,1} variable, representing the parity of the message.
     * @return 0 if x is equal to 1, else returns 1.
     * g     * Negator applied on {0,1} integer.
     */
    private int oppositeParity(int x) {
        if (x == 1) {
            return 0;
        } else return 1;
    }


    /**
     * @param message     currently updated message
     * @param prevMessage most recent message of opposite parity to the current message, stored in the memory buffer.
     * @return an instance of GeoPos.
     * Evaluates the position of the aircraft based on the most recent pair of messages of opposite parity. Passes data carried
     * by the pair of messages to the static method CprDecoder.decodePosition.
     */
    private GeoPos getPosition(AirbornePositionMessage message, AirbornePositionMessage prevMessage) {
        int messageParity = message.parity();
        if (messageParity == 1) {
            return CprDecoder.decodePosition(prevMessage.x(), prevMessage.y(), message.x(), message.y(), messageParity);

        } else {
            return CprDecoder.decodePosition(message.x(), message.y(), prevMessage.x(), prevMessage.y(), messageParity);
        }
    }

    /**
     * @param message message we want to save
     *                Adds a message to the memory buffer at the index of its parity : if the message is even, it is stored at index 0,
     *                else it is stored at index 1.
     */

    private void addToMemory(AirbornePositionMessage message) {
        this.previousMessageMemory[message.parity()] = message;
    }

    /**
     * @param currentMessage Current message being updated
     * @return The most recent message of opposite parity to the current message, stored in the memory buffer.
     * @throws NullPointerException if there is no previous message of opposite parity (happens at the beginning of message stream).
     *                              Returns the most recent message of parity opposite to the parity of the current message. Useful for position calculations.
     */

    private AirbornePositionMessage oppParRecentMessage(AirbornePositionMessage currentMessage) throws NullPointerException {
        int oppPar = this.oppositeParity(currentMessage.parity());
        AirbornePositionMessage prevMessage = this.previousMessageMemory[oppPar];
        if (Objects.isNull(prevMessage)) {
            return null;
        }
        return prevMessage;
    }


}



