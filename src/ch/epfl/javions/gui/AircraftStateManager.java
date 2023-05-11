package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;


/**
 * Class managing the states of the aircraft that will be subsequently displayed on the map.
 */
public final class AircraftStateManager {
    private final AircraftDatabase database;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> accumulatorIcaoAddressMap;
    private final ObservableSet<ObservableAircraftState> aircraftSet;
    private Message prevMessage;
    private final long MINUTE = (long) Units.convert(1, Units.Time.MINUTE, Units.Time.NANO_SECOND);

    /**
     * Creates the state manager. Associates to each address a state accumulator composed of a set of observable states.
     * Stores the significant states in an observable set.
     *
     * @param database database containing the essential data on aircrafts.
     */
    public AircraftStateManager(AircraftDatabase database) {
        this.database = database;
        this.accumulatorIcaoAddressMap = new HashMap<>();
        this.aircraftSet = FXCollections.observableSet();
    }

    /**
     * Access an unmodifiable view on the set of states.
     *
     * @return view on set of states
     */
    public ObservableSet<ObservableAircraftState> states() {
        return FXCollections.unmodifiableObservableSet(aircraftSet);
    }

    /**
     * Uses the message argument given to update the state of the aircraft that sent it,
     * creating that state when said message is the first sent by the aircraft.
     *
     * @param message sent by an aircraft
     */
    public void updateWithMessage(Message message) throws IOException {
        IcaoAddress address = message.icaoAddress();
        if (!this.accumulatorIcaoAddressMap.containsKey(address)) {
            this.accumulatorIcaoAddressMap.put(
                    address,
                    new AircraftStateAccumulator<>(
                            new ObservableAircraftState(
                                    address,
                                    this.database.get(address)
                            )
                    )
            );
        }
        AircraftStateAccumulator<ObservableAircraftState> stateAccumulator = this.accumulatorIcaoAddressMap.get(address);
        stateAccumulator.update(message);
        ObservableAircraftState stateSetter = stateAccumulator.stateSetter();
        if (!Objects.isNull(stateSetter.getPosition())) this.aircraftSet.add(stateSetter);
        this.updatePrevMessage(message);
    }

    /**
     * Delete all the states associated to the addresses that have not sent any message for more than one minute.
     **/


    public void purge() {
        accumulatorIcaoAddressMap.values().forEach(
                ac -> {
                    if (ac.stateSetter().getLastMessageTimeStampNs() - prevMessage.timeStampNs() >= MINUTE) {
                        this.aircraftSet.remove(ac.stateSetter());
                        this.accumulatorIcaoAddressMap.remove(ac.stateSetter().getIcaoAddress());
                    }
                }
        );
    }

    /**
     * Updates the previous message in memory, which is used to access the previous time stamp.
     *
     * @param message new message update value.
     */
    private void updatePrevMessage(Message message) {
        this.prevMessage = message;
    }
}
