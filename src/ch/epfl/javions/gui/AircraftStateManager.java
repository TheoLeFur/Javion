package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.IntStream;


/**
 * @author Theo Le Fur
 * SCIPER : 363294
 * Class managing the states of the aircraft that will be subsequently displayed on the map.
 */
public final class AircraftStateManager {

    // One minute in nanoseconds
    private final long MINUTE_NS = (long) Units.convert(1, Units.Time.MINUTE, Units.Time.NANO_SECOND);
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> addressToAsmTable;
    private final ObservableSet<ObservableAircraftState> observableAircraftSet;
    private final ObservableSet<ObservableAircraftState> readOnlyAircraftSet;
    private final AircraftDatabase database;

    // attribute for storing the aircraft's previous message.
    private Message prevMessage;

    /**
     * Creates the state manager. Associates to each address a state accumulator composed of a set of observable states.
     * Stores the significant states in an observable set.
     *
     * @param database database containing the essential data on aircraft.
     */
    public AircraftStateManager(AircraftDatabase database) {
        this.addressToAsmTable = new HashMap<>();
        this.database = database;
        this.observableAircraftSet = FXCollections.observableSet();
        this.readOnlyAircraftSet = FXCollections.unmodifiableObservableSet(this.observableAircraftSet);
    }

    /**
     * Access an unmodifiable view on the set of states.
     *
     * @return view on set of states
     */
    public ObservableSet<ObservableAircraftState> states() {
        return this.readOnlyAircraftSet;
    }

    /**
     * Creates an instance of the accumulator for the address from which the message came from, if it is
     * the first message obtained from this particular address. Then, it updates the message with the state
     * accumulator that is associated to its issuance address.
     *
     * @param message sent by an aircraft
     */
    public void updateWithMessage(Message message) throws IOException {

        IcaoAddress address = message.icaoAddress();
        this.addressToAsmTable.computeIfAbsent(address, a -> {
            try {
                return new AircraftStateAccumulator<>(new ObservableAircraftState(address, this.database.get(a)));
            } catch (IOException e) {
                return null;
            }
        });
        AircraftStateAccumulator<ObservableAircraftState> stateAccumulator = this.addressToAsmTable.get(address);
        stateAccumulator.update(message);
        ObservableAircraftState stateSetter = stateAccumulator.stateSetter();
        if (!Objects.isNull(stateSetter.getPosition())) this.observableAircraftSet.add(stateSetter);
        this.prevMessage = message;
    }

    /**
     * Purges all the states and their corresponding accumulators, whose addresses have not issued any
     * message signal for more than one minute. This ensures that aircraft that cannot be tracked anymore due to loss
     * of signal are not displayed on the map anymore.
     **/


    public void purge() {
        addressToAsmTable.values().forEach(
                ac -> {
                    if (ac.stateSetter().getLastMessageTimeStampNs() - this.prevMessage.timeStampNs() >= MINUTE_NS) {
                        this.observableAircraftSet.remove(ac.stateSetter());
                        this.addressToAsmTable.remove(ac.stateSetter().getIcaoAddress());
                    }
                }
        );
    }

}
