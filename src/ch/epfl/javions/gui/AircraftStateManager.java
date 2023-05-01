package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> recentAircraftMessage;
    private final Set<ObservableAircraftState> knownAircraft;
    private final AircraftDatabase database;
    private long currentTimeStampNs;


    public AircraftStateManager(AircraftDatabase database){
        this.database = database;
        recentAircraftMessage = new HashMap<>();
        knownAircraft = new HashSet<>();
        currentTimeStampNs = 0;
    }

    public Set<ObservableAircraftState> states(){
        return Set.copyOf(knownAircraft);
    }

    public void updateWithMessage(RawMessage message){
        Objects.requireNonNull(message);
        Message decodedMessage = MessageParser.parse(message);

        try {

            if (Objects.isNull(decodedMessage)) {
                throw new NullPointerException("Message not valid.");
            }
            currentTimeStampNs = decodedMessage.timeStampNs();

            if(!recentAircraftMessage.containsKey(decodedMessage.icaoAddress())) {
                AircraftData data = database.get(decodedMessage.icaoAddress());

                if (Objects.isNull(data)) {
                    throw new NullPointerException("ICAO adress not found in the file.");
                }
                recentAircraftMessage.put(decodedMessage.icaoAddress(), new AircraftStateAccumulator<>(new ObservableAircraftState(decodedMessage.icaoAddress(), data)));
            }

            recentAircraftMessage.get(decodedMessage.icaoAddress()).update(decodedMessage);

            if(!Objects.isNull(recentAircraftMessage.get(decodedMessage.icaoAddress()).stateSetter().getPosition())){
                knownAircraft.add(recentAircraftMessage.get(decodedMessage.icaoAddress()).stateSetter());
            }

        }catch (NullPointerException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Stream error");
        }
    }

    public void purge(){
        ObservableAircraftState observableAircraftState;
        HashSet<ObservableAircraftState> toDelete = new HashSet<>();

        for(Map.Entry<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> mapEntries : recentAircraftMessage.entrySet()){
            observableAircraftState = mapEntries.getValue().stateSetter();
            if (currentTimeStampNs - observableAircraftState.getLastMessageTimeStampNs() >= 60000000000L){      // Finds all the aircrafts to remove from the list
                toDelete.add(observableAircraftState);
            }
        }

        knownAircraft.removeAll(toDelete);

        for(ObservableAircraftState obs : toDelete){
            recentAircraftMessage.remove(obs.getIcaoAddress());

        }
    }
}