package ch.epfl.javions.adsb;
import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class AircraftStateAccumulator<T extends AircraftStateSetter> {

    private AircraftStateSetter stateSetter;
    private AirbornePositionMessage[] previousMessageMemory;
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
        this.previousMessageMemory = new AirbornePositionMessage[2];
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
                try {
                    AirbornePositionMessage prevMessage = this.oppParRecentMessage(aim);
                    if(this.posMessageCondition(aim, prevMessage)){
                        this.stateSetter.setPosition(this.getPosition(aim));
                    }
                } catch (NullPointerException ignored){}
                this.addToMemory(aim);
            }
            case AirborneVelocityMessage aim -> {
                this.stateSetter.setVelocity(aim.speed());
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
        return (message.timeStampNs() - previousMessage.timeStampNs()) <= POSITION_THRESHOLD_NS;
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


    /**
     * 0 pair, 1 impair
     * @param message
     * @return
     */
    private GeoPos getPosition(AirbornePositionMessage message) {

        int mostRecent = message.parity();
        if (mostRecent == 1) {
            AirbornePositionMessage previousMessage = previousMessageMemory[0];
            return CprDecoder.decodePosition(previousMessage.x(), previousMessage.y(), message.x(),  message.y(), mostRecent);
        } else {
            AirbornePositionMessage previousMessage = previousMessageMemory[1];
            return CprDecoder.decodePosition(message.x(), message.y(), previousMessage.x(), previousMessage.y(), mostRecent);
        }

    }

    private void addToMemory(AirbornePositionMessage message){
        this.previousMessageMemory[message.parity()] = message;
    }

    private AirbornePositionMessage oppParRecentMessage(AirbornePositionMessage currentMessage){
        int oppPar = this.oppositeParity(currentMessage.parity());
        AirbornePositionMessage prevMessage = this.previousMessageMemory[oppPar];
        if (Objects.isNull(prevMessage)){
            throw new NullPointerException();
        }
        return prevMessage;
    }






}



