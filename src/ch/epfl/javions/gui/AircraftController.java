package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import javafx.beans.property.Property;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.lang.reflect.AnnotatedArrayType;
import java.util.Objects;


public final class AircraftController {


    private final String AircraftStyleSheetPath = "resources/aircraft.css";
    private final MapParameters mapParams;
    private final ObservableSet<ObservableAircraftState> observablaAircraft;
    private final Property<ObservableAircraftState> stateProperty;


    private final Pane pane;

    public AircraftController(
            MapParameters mapParams,
            ObservableSet<ObservableAircraftState> observableAircraft,
            Property<ObservableAircraftState> stateProperty
    ) {
        // init the constructor params
        this.mapParams = mapParams;
        this.observablaAircraft = observableAircraft;
        this.stateProperty = stateProperty;

        // Build the scene graph

        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
        this.pane.getStylesheets().add(this.AircraftStyleSheetPath);


        // adds the groups of the initial set passed into construction.
        this.observablaAircraft.forEach(
                ss -> {
                    Group AnnotatedAircraftGroup = new Group();
                    AnnotatedAircraftGroup.setId(ss.getIcaoAddress().toString());
                    this.pane.getChildren().add(AnnotatedAircraftGroup);
                }
        );

        // track changes of the set of states
        this.observablaAircraft.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                    Group newGroup = new Group();
                    newGroup.setId(change.getElementAdded().getIcaoAddress().toString());
                    this.pane.getChildren().add(newGroup);
                    this.pane.getChildren().removeIf(
                            e -> Objects.equals(e.getId(),
                                    change.getElementRemoved().getIcaoAddress().toString()));
                });
    }

    /**
     * Create the trajectory group, which will be represented as line segments that link the points composed
     * by the positions in the trajectory list
     * @param parentGroup parent group
     */
    private void createTrajectoryGroup(Group parentGroup, ObservableAircraftState obsState) {
        Group trajectory = new Group();
        trajectory.setViewOrder(-obsState.getAltitude());

    }

    private void createLabelGroup(Group parentGroup, double altitude) {
        Group labelGroup = new Group();
        labelGroup.setViewOrder(-altitude);
    }

    private void createAircraftGroup(Group parentGroup, ObservableAircraftState obsState) {
        Group aircraftGroup = new Group();
        aircraftGroup.setViewOrder(-obsState.getAltitude());
        SVGPath icon = new SVGPath();
        icon.setContent(AircraftIcon.iconFor(
                obsState.getTypeDesignator(),
                obsState.getDescription(),
                obsState.getCategory(),
                obsState.getWakeTurbulenceCategory()
        ).svgPath());
        icon.setRotate(Units.convertTo(obsState.getTrackOrHeading(), Units.Angle.DEGREE));
        icon.setFill(ColorRamp.PLASMA.at(
                this.computeColorIndex(obsState.getAltitude())
        ));
    }

    /**
     * Computes the color index, according to the formula c = [altitude/12000] ^ (1/3).
     *
     * @param altitude altitude of the aircraft.
     * @return index c, which determines the color from the spectrum that will be chosen.
     */
    private double computeColorIndex(double altitude) {
        return Math.pow(Math.rint(altitude / 12000d), 1d / 3d);
    }

    /**
     * Access the main pane.
     *
     * @return pane superposed with background map.
     */
    public Pane pane() {
        return this.pane();
    }

}
