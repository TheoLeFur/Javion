package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.AirbornePositionMessage;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

/**
 * Keeps the states of a set of aircrafts up to date by using
 * the messages received from said aircrafts.
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 *
 */
public final class AircraftStateManager {
    private Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> accumulatorIcaoAddressMap;
    //Observable set of aircraft states whose position is known
    private ObservableSet<AircraftStateAccumulator<ObservableAircraftState>> aircraftSet;
    private final AircraftDatabase aircraftDatabase;
    private Message lastMessage;

    public AircraftStateManager(AircraftDatabase aircraftDataBase) {
        this.aircraftDatabase = aircraftDataBase;
        this.accumulatorIcaoAddressMap = new HashMap<>();
        this.aircraftSet = FXCollections.observableSet();
    }

    /**
     * @return the observable but non modifiable set of observable aircraft states whose position is known
     */
    public ObservableSet<AircraftStateAccumulator<ObservableAircraftState>> states() {
        return FXCollections.unmodifiableObservableSet(aircraftSet);
    }

    /**
     * Uses the message argument given to update the state of the aircraft that sent it,
     * creating that state when said message is the first sent by the aircraft.
     * @param message sent by an aircraft
     */
    public void updateWithMessage(Message message) throws IOException {
        IcaoAddress aircraftIcao = message.icaoAddress();

        if(!accumulatorIcaoAddressMap.containsKey(aircraftIcao)) {
            ObservableAircraftState unregisteredState = new ObservableAircraftState(aircraftIcao, aircraftDatabase.get(aircraftIcao));
            accumulatorIcaoAddressMap.put(aircraftIcao,
                    new AircraftStateAccumulator<>(unregisteredState));

            //if the position isn't null, then we can put the observable state in the aforementioned set
            if(unregisteredState.getPosition() != null && message instanceof AirbornePositionMessage) {
                aircraftSet.add(new AircraftStateAccumulator<>(unregisteredState));
            }
        } else {
            accumulatorIcaoAddressMap.get(aircraftIcao).update(message);
        }

        lastMessage =  message;
    }

    /**
     * Deletes from the set of observable states all those that correspond to aircraft from which
     * no message has been received during the minute preceding the reception
     * of the last message passed to updateWithMessage
     */
    public void purge(){
        long lastUpdateTime = lastMessage.timeStampNs();
        for(AircraftStateAccumulator<ObservableAircraftState> accumulator : accumulatorIcaoAddressMap.values()) {
            if(accumulator.stateSetter().getLastMessageTimeStampNs() <= lastUpdateTime + Units.convert(1, Units.Time.MINUTE, Units.Time.NANO)) {
                aircraftSet.remove(accumulator);
            }
        }
    }

}
