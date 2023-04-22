package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Map;
import java.util.Set;

/**
 * Keeps the states of a set of aircrafts up to date by using
 * the messages received from said aircrafts.
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 *
 */
public final class AircraftStateManager {
    private Map<AircraftStateAccumulator, IcaoAddress> accumulatorIcaoAddressMap;
    //Observable set of aircraft states whose position is known
    private Set<ObservableAircraftState> aircraftSet;

    public AircraftStateManager(AircraftDatabase aircraftDataBase) {}

    public Set<ObservableAircraftState> states() {
        return null;
    }

    public void updateWithMessage(Message message) {}

    public void purge(){}

}
