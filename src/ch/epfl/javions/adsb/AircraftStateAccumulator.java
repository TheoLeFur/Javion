package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class AircraftStateAccumulator<T extends AircraftStateSetter> {

    private AircraftStateSetter stateSetter;
    private ArrayList<AirbornePositionMessage> previousMessages;
    private final double POSITION_THRESHOLD_NS = 10 * Math.pow(10, 9);

    /**
     * @param stateSetter state Setter
     * @author Theo Le Fur
     * Returns an AircraftStateAccumulator object. We will be storing the previous messages of different parity in\
     * an array of size 2. This will be convenient for updates.
     */
    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
        this.previousMessages = new ArrayList<>(2);
    }

    /**
     * @return state setter passed in the constructor.
     * @author Theo Le Fur
     * Access the state setter
     */
    public T stateSetter() {
        return (T) this.stateSetter;
    }

    /**
     * @param message message
     * @author Theo Le Fur
     * Updates the message, according to its type
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
                if (this.previousMessages.size() == 2) {
                    if (this.posMessageCondition(aim, this.getPreviousMessageOfOppositeParity(aim))) {
                        this.stateSetter.setPosition(this.GeoPosFromNormalized(aim.x(), aim.y()));
                    }
                    this.previousMessages.set(aim.parity(), aim);
                } else this.previousMessages.add(aim);

            }

            case AirborneVelocityMessage aim -> {
                this.stateSetter.setVelocity(aim.velocity());
                this.stateSetter.setTrackOrHeading(aim.trackOrHeading());
            }

            case default -> {
            }
        }
    }


    /**
     * @param message         message of whom we want to update the position
     * @param previousMessage previous position message of different parity
     * @return True if position can be updated
     * @author Theo Le Fur
     * Verifies if position can be updated, by looking at the previous message of different parity
     */
    private boolean posMessageCondition(AirbornePositionMessage message, AirbornePositionMessage previousMessage) {
        return (message.timeStampNs() - previousMessage.timeStampNs() <= POSITION_THRESHOLD_NS);
    }


    /**
     * @param x {0,1} variable, representing the parity of the message.
     * @return 0 if x is equal to 1, else returns 1.
     * @author Theo Le Fur
     * Negator applied on {0,1} integer.
     */
    private int oppositeParity(int x) {
        if (x == 1) {
            return 0;
        } else return 1;
    }

    private AirbornePositionMessage getPreviousMessageOfOppositeParity(AirbornePositionMessage message) {
        return this.previousMessages.stream().filter(e -> e.parity() == this.oppositeParity(message.parity())).toList().get(0);
    }

    /**
     * @param x normalized latitude
     * @param y normalized longitude
     * @return A GeoPos object with recovered latitude and longitude from x and y
     * Creates a GeoPos obejct from normalized local coordinates x and y
     * @author Theo Le Fur
     */
    private GeoPos GeoPosFromNormalized(double x, double y) {
        System.out.println(y * Math.pow(2, 17));
        System.out.println(x * Math.pow(2, 17));
        return new GeoPos((int) (y * Math.pow(2, 17)), (int) (x * Math.pow(2, 17)));
    }

}



