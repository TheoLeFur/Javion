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


/**
 * Class managing the states of the aircraft that will be subsequently displayed on the map.
 */
public final class AircraftStateManager {

    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> addresToAsmMap;
    private final ObservableSet<ObservableAircraftState> aircraftSet;
    private final ObservableSet<ObservableAircraftState> readOnlyAircraftSet;
    private Message prevMessage;
    private final AircraftDatabase database;
    private final long MINUTE_NS = (long) Units.convert(1, Units.Time.MINUTE, Units.Time.NANO_SECOND);

    /**
     * Creates the state manager. Associates to each address a state accumulator composed of a set of observable states.
     * Stores the significant states in an observable set.
     *
     * @param database database containing the essential data on aircrafts.
     */
    public AircraftStateManager(AircraftDatabase database) {
        this.database = database;
        this.addresToAsmMap = new HashMap<>();
        this.aircraftSet = FXCollections.observableSet();
        this.readOnlyAircraftSet = FXCollections.unmodifiableObservableSet(this.aircraftSet);
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
        if (!this.addresToAsmMap.containsKey(address)) {
            this.addresToAsmMap.put(address, new AircraftStateAccumulator<>(new ObservableAircraftState(
                            address,
                            this.database.get(address)
                    )
                    )
            );
        }
        AircraftStateAccumulator<ObservableAircraftState> stateAccumulator = this.addresToAsmMap.get(address);
        stateAccumulator.update(message);
        ObservableAircraftState stateSetter = stateAccumulator.stateSetter();
        if (!Objects.isNull(stateSetter.getPosition())) this.aircraftSet.add(stateSetter);
        this.prevMessage = message;
    }

    /**
     * Purges all the states and their corresponding accumulators, whose addresses have not issued any
     * message signal for more than one minute.
     **/


    public void purge() {
        addresToAsmMap.values().forEach(
                ac -> {
                    if (ac.stateSetter().getLastMessageTimeStampNs() - prevMessage.timeStampNs() >= MINUTE_NS) {
                        this.aircraftSet.remove(ac.stateSetter());
                        this.addresToAsmMap.remove(ac.stateSetter().getIcaoAddress());
                    }
                }
        );
    }

}
