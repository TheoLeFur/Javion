package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

// TODO : these methods seem extremely inefficient to me, I have to fix this
/**
 * Class that manages the states and their evolution of the aircraft sending messages. This factory manages the states of the aircraft that
 * will subsequently be visible on the map.
 */
public final class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> recentAircraftMessage;
    private final ObservableSet<ObservableAircraftState> knownAircraft;
    private final AircraftDatabase database;
    private long currentTimeStampNs;

    // Corresponds to a one minute duration.
    private final long MINUTE = 60000000000L;


    /**
     * Initialise the aircraft state manager.
     *
     * @param database access the aircraft's data through its address.
     */
    public AircraftStateManager(AircraftDatabase database) {
        this.database = database;
        this.recentAircraftMessage = new HashMap<>();
        this.knownAircraft = new SimpleSetProperty<>();
        currentTimeStampNs = 0;
    }

    /**
     * Access an unmodifiable copy of the set.
     *
     * @return copy of the original observable set, that cannot be modified.
     */

    public ObservableSet<ObservableAircraftState> states() {
        return FXCollections.unmodifiableObservableSet(this.knownAircraft);
    }

    public void updateWithMessage(RawMessage message) {
        Objects.requireNonNull(message);
        Message decodedMessage = MessageParser.parse(message);

        try {
            if (Objects.isNull(decodedMessage)) {
                throw new NullPointerException("Message not valid.");
            }
            currentTimeStampNs = decodedMessage.timeStampNs();

            if (!recentAircraftMessage.containsKey(decodedMessage.icaoAddress())) {
                AircraftData data = database.get(decodedMessage.icaoAddress());

                if (Objects.isNull(data)) {
                    throw new NullPointerException("ICAO adress not found in the file.");
                }
                recentAircraftMessage.put(decodedMessage.icaoAddress(), new AircraftStateAccumulator<>(
            new ObservableAircraftState(
                    decodedMessage.icaoAddress(), data)));
            }
            recentAircraftMessage.get(decodedMessage.icaoAddress()).update(decodedMessage);
            if (!Objects.isNull(recentAircraftMessage.get(decodedMessage.icaoAddress()).stateSetter().getPosition())) {
                knownAircraft.add(recentAircraftMessage.get(decodedMessage.icaoAddress()).stateSetter());
            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Stream error");
        }
    }


    public void purge() {
        ObservableAircraftState observableAircraftState;
        HashSet<ObservableAircraftState> toDelete = new HashSet<>();

        for (Map.Entry<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> mapEntries : recentAircraftMessage.entrySet()) {
            observableAircraftState = mapEntries.getValue().stateSetter();
            if (currentTimeStampNs - observableAircraftState.getLastMessageTimeStampNs() >= this.MINUTE) {      // Finds all the aircraft to remove from the list
                toDelete.add(observableAircraftState);
            }
        }
        knownAircraft.removeAll(toDelete);
        for (ObservableAircraftState obs : toDelete) {
            recentAircraftMessage.remove(obs.getIcaoAddress());
        }
    }
}