
package ch.epfl.javions.gui;

import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.layout.Pane;

import java.util.HashSet;

public final class AircraftController {

    private final MapParameters mapParams;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private Property<ObservableAircraftState> stateProperty;

    public AircraftController(
            MapParameters mapParams,
            ObservableSet<ObservableAircraftState> aircraftStates,
            Property<ObservableAircraftState> stateProperty
    )
    {
        this.mapParams = mapParams;
        this.aircraftStates = FXCollections.observableSet(aircraftStates);
        this.stateProperty = stateProperty;

    }
    public Pane pane(){

        Pane aircraftPane = new Pane();
        aircraftPane.setPickOnBounds(false);


        return null;}
}
