package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class AircraftStateAccumulator<T extends AircraftStateSetter> {

    private AircraftStateSetter stateSetter;
    private ArrayList<AirbornePositionMessage> previousMessages;

    /**
     * Returns an AircraftStateAccumulator object. We will be storing the previous messages of different parity in\
     * an array of size 2. This will be convenient for updates.
     *
     * @param stateSetter state Setter
     */
    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
        this.previousMessages = new ArrayList<>(2);
    }

    /**
     * Access the state setter
     *
     * @return state setter passed in the constructor.
     */
    public T stateSetter() {
        return (T) this.stateSetter;
    }

    public void update(Message message) {

        this.stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {
            case AircraftIdentificationMessage aim -> {
                this.stateSetter.setCategory(aim.category());
                this.stateSetter.setCallSign(aim.callSign());
            }
            case AirbornePositionMessage aim -> {
                this.stateSetter.setAltitude(aim.altitude());
                if (this.posMessageCondition(aim, this.previousMessages.get(this.oppositeParity(aim.parity())))) {
                    this.stateSetter.setPosition(new GeoPos());
                }
                this.previousMessages.set(aim.parity(), aim);
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
     * Verifies if position can be updated, by looking at the previous message of different parity
     * @param message message of whom we want to update the position
     * @param previousMessage previous position message of different parity
     * @return True if position can be updated
     */
    private boolean posMessageCondition(AirbornePositionMessage message, AirbornePositionMessage previousMessage) {
        return (message.timeStampNs() - previousMessage.timeStampNs() <= 10) && this.previousMessages.size() == 2;
    }


    /**
     * Negator applied on {0,1} integer.
     * @param x {0,1} variable, representing the parity of the message.
     * @return 0 if x is equal to 1, else returns 1.
     */
    private int oppositeParity(int x) {
        if (x == 1) {
            return 0;
        } else return 1;
    }


}



