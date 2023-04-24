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
 * Keeps the states of a set of aircrafts up to date by using
 * the messages received from said aircrafts.
 * One of its instance will be used to manage all of the aircrafts visible on the map.
 *
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class AircraftStateManager {
    private final AircraftDatabase aircraftDatabase;
    private Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> accumulatorIcaoAddressMap;
    //Observable set of aircraft states whose position is known
    private ObservableSet<ObservableAircraftState> aircraftSet;
    private Message lastMessage;

    /**
     * @param aircraftDataBase that is read for info on the aircrafts
     */
    public AircraftStateManager(AircraftDatabase aircraftDataBase) {
        this.aircraftDatabase = aircraftDataBase;
        this.accumulatorIcaoAddressMap = new HashMap<>();
        this.aircraftSet = FXCollections.observableSet();
    }

    /**
     * @return the observable but unmodifiable set of observable aircraft states whose position is known
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
        IcaoAddress aircraftIcao = message.icaoAddress();

        if (!accumulatorIcaoAddressMap.containsKey(aircraftIcao)) {
            accumulatorIcaoAddressMap.put(aircraftIcao,
                    new AircraftStateAccumulator<>(
                            new ObservableAircraftState(aircraftIcao, aircraftDatabase.get(aircraftIcao))));
        }

        AircraftStateAccumulator<ObservableAircraftState> messageSenderState = accumulatorIcaoAddressMap.get(aircraftIcao);
        messageSenderState.update(message);

        //if the position isn't null, then we can put the observable state in the aforementioned set
        if (messageSenderState
                .stateSetter()
                .getPosition() != null) { // I'm not sure if the following is necessary: && message instanceof AirbornePositionMessage
            aircraftSet.add(messageSenderState.stateSetter());
        }

        lastMessage = message;
    }

    /**
     * Deletes from the set of observable states all those that correspond to aircraft from which
     * no message has been received during the minute preceding the reception
     * of the last message passed to updateWithMessage
     */
    public void purge() {
        long lastUpdateTime = lastMessage.timeStampNs();

        for (AircraftStateAccumulator<ObservableAircraftState> accumulator : accumulatorIcaoAddressMap.values()) {
            if (accumulator
                    .stateSetter()
                    .getLastMessageTimeStampNs() >= lastUpdateTime + Units.convert(1, Units.Time.MINUTE, Units.Time.NANO_SECOND)) {
                aircraftSet.remove(accumulator.stateSetter());
            }
        }
    }

}
