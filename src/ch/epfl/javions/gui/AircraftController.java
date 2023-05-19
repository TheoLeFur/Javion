package ch.epfl.javions.gui;


import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Iterator;
import java.util.Objects;


/**
 * Class for controlling the display of the aircraft on the background map. It will display
 * the icons, associated information and trajectory : everything colored as a function of the altitude of
 * the aircraft.
 */
public final class AircraftController {

    /**
     * We will proceed the following way. Each part of the scene graph will be created using a private method :
     * create ... that will init the group param, take care of its placement in the graph realize the desired bindings.
     * Afterward, it will be placed in the constructor at the correct place.
     */

    // Style sheet that will be used
    private final String AircraftStyleSheetPath = "/aircraft.css";
    // Offset in for the margin in the rectangular label
    private final int LABEL_OFFSET = 4;
    // Minimal zoom level for making labels visible
    private final int VISIBLE_LABEL_ZOOM_THRESHOLD = 11;
    private final MapParameters mapParams;
    private final Property<ObservableAircraftState> selectedAircraft;
    private final ObservableSet<ObservableAircraftState> observableAircraft;


    private final Pane pane;

    /**
     * We instantiate an aircraft controller class, that will control the display of the aircraft on the map, their labels and their trajectories.
     *
     * @param mapParams          parameters of the visible map (minX, minY and zoom value)
     * @param observableAircraft set of aircraft that are currently observable
     */

    public AircraftController(MapParameters mapParams, ObservableSet<ObservableAircraftState> observableAircraft, ObjectProperty<ObservableAircraftState> selectedAircraft) {

        // init the constructor params
        this.mapParams = mapParams;
        this.selectedAircraft = selectedAircraft;
        this.observableAircraft = observableAircraft;

        // Build the scene graph

        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
        this.pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource(this.AircraftStyleSheetPath)).toString());


        // We construct a graph for every state in the initial set of values passed into the constructor.

        this.observableAircraft.forEach(this::createSceneGraph);


        // track changes of the set of states, if element is added, we build its corresponding graph, if element
        // is removed, then we remove the graph of the removed states.

        this.observableAircraft.addListener((SetChangeListener<ObservableAircraftState>) change -> {
                    ObservableAircraftState elementAdded = change.getElementAdded();
                    if (!Objects.isNull(elementAdded)) {
                        this.createSceneGraph(elementAdded);
                    }
                    ObservableAircraftState elementRemoved = change.getElementRemoved();
                    if (!Objects.isNull(elementRemoved)) {
                        this.pane.getChildren().removeIf(e -> Objects.equals(e.getId(), elementRemoved.getIcaoAddress().toString()));
                    }
                }
        );


    }

    /**
     * Creates the whole scene graph for the aircraft controller
     *
     * @param s state setter
     */
    private void createSceneGraph(ObservableAircraftState s) {

        Group annotatedAircraftGroup = this.createAnnotatedAircraftGroup(s);
        this.createTrajectoryGroup(s, annotatedAircraftGroup);
        Group labelIconGroup = this.createLabelIconGroup(s, annotatedAircraftGroup);
        SVGPath icon = this.createIcon(s, labelIconGroup);
        this.aircraftSelectionEventHandler(s, icon);
        this.createLabel(s, labelIconGroup);


    }

    /**
     * Handles the click-on-icon event and places s in the stateProperty placeholder.
     *
     * @param s state setter
     */
    private void aircraftSelectionEventHandler(ObservableAircraftState s, SVGPath icon) {

        icon.setOnMouseClicked(
                (event) -> this.selectedAircraft.setValue(s)
        );
    }


    /**
     * Creates the annotated aircraft group. Handles the overlapping of aircraft, giving priority of display to the one
     * having the highest altitude
     *
     * @param s state setter
     */
    private Group createAnnotatedAircraftGroup(ObservableAircraftState s) {

        Group annotatedAircraftGroup = new Group();
        annotatedAircraftGroup.setId(s.getIcaoAddress().string());
        this.pane.getChildren().add(annotatedAircraftGroup);

        annotatedAircraftGroup.getStylesheets().add(AircraftStyleSheetPath);

        // This guarantees that we the display overlaps icon from the highest altitude to the lowest altitude

        annotatedAircraftGroup.viewOrderProperty().bind(s.altitudeProperty().negate());


        return annotatedAircraftGroup;

    }


    /**
     * Creates the label-icon group in the scene graph that takes care of the positioning of the two subgroups
     * label and icon.
     *
     * @param s state setter
     */
    private Group createLabelIconGroup(ObservableAircraftState s, Group annotatedAircraftGroup) {

        Group labelIconGroup = new Group();
        annotatedAircraftGroup.getChildren().add(labelIconGroup);

        labelIconGroup.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
                    double projectedX = WebMercator.x(this.mapParams.getZoomValue(), s.getPosition().longitude());
                    return projectedX - this.mapParams.getMinXValue();
                },
                this.mapParams.zoomProperty(), this.mapParams.minXProperty(), s.positionProperty()));

        labelIconGroup.layoutYProperty().bind(Bindings.createDoubleBinding(() -> {
            double projectedY = WebMercator.y(this.mapParams.getZoomValue(), s.getPosition().latitude());
            return projectedY - this.mapParams.getMinYValue();
        }, this.mapParams.zoomProperty(), this.mapParams.minYProperty(), s.positionProperty()));


        return labelIconGroup;
    }


    /**
     * Create the icon element in the scene graph
     *
     * @param s state setter
     */
    private SVGPath createIcon(ObservableAircraftState s, Group labelIconGroup) {

        SVGPath icon = new SVGPath();
        labelIconGroup.getChildren().add(icon);
        icon.getStyleClass().add("aircraft");
        AircraftData ad = s.getAircraftData();

        AircraftTypeDesignator atd;
        AircraftDescription ads;
        int cat;
        WakeTurbulenceCategory wtc;

        if (ad == null) {
            atd = new AircraftTypeDesignator("");
            ads = new AircraftDescription("");
            cat = 0;
            wtc = WakeTurbulenceCategory.UNKNOWN;
        } else {
            atd = s.getTypeDesignator();
            ads = s.getDescription();
            cat = s.getCategory();
            wtc = s.getWakeTurbulenceCategory();
        }

        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>(AircraftIcon.iconFor(atd, ads, cat, wtc));

        // we bind the icon property to the category property, so that it tracks the changes.

        aircraftIconProperty.bind(s.categoryProperty().map(d -> AircraftIcon.iconFor(atd, ads, d.intValue(), wtc)));

        // bind both the content and the can rotate properties to the methods in AircraftIcon.

        icon.contentProperty().bind(aircraftIconProperty.map(AircraftIcon::svgPath));
        icon.rotateProperty().bind(Bindings.createDoubleBinding(() -> {
            if (aircraftIconProperty.get().canRotate()) {
                return Units.convertTo(s.getTrackOrHeading(), Units.Angle.DEGREE);
            } else {
                return 0d;
            }
        }, s.trackOrHeadingProperty(), aircraftIconProperty));
        icon.fillProperty().bind(s.altitudeProperty().map(a -> ColorRamp.PLASMA.at(this.computeColorIndex(a.intValue()))));

        return icon;

    }

    /**
     * Create the aircraft label, visible with a zoom level greater or equal than 11. It is composed of a text placed in a rectangle,
     * holding information about the aircraft id, velocity and altitude.
     *
     * @param s state setter
     */
    private void createLabel(ObservableAircraftState s, Group labelIconGroup) {

        Group labelGroup = new Group();
        labelGroup.getStyleClass().add("label");
        labelIconGroup.getChildren().add(labelGroup);
        labelGroup.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () ->
                                this.mapParams.zoomProperty().getValue() >= VISIBLE_LABEL_ZOOM_THRESHOLD
                                        || s.equals(this.selectedAircraft.getValue())
                        ,
                        this.mapParams.zoomProperty(),
                        this.selectedAircraft
                )
        );


        Text text = new Text();
        Rectangle background = new Rectangle();
        labelGroup.getChildren().add(background);
        labelGroup.getChildren().add(text);


        text.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%s \n %s (km/h) \u2002 %d (m) ",
                        this.getAircraftIdForLabel(s),
                        this.getVelocityForLabel(s),
                        (int) (s.getAltitude())),
                s.altitudeProperty(),
                s.velocityProperty(),
                s.callSignProperty()));

        background.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + this.LABEL_OFFSET));
        background.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + this.LABEL_OFFSET));

    }


    /**
     * Formats the velocity on the labels display
     *
     * @param s state setter
     * @return value of the velocity in km/h if available, else ?
     */
    private String getVelocityForLabel(ObservableAircraftState s) {

        if (!Objects.isNull(s.getAircraftData())) {
            if (!Double.isNaN(s.velocityProperty().getValue())) {
                return String.valueOf((int) (Units.convertTo(s.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
            }
        }
        return "?";
    }


    /**
     * Evaluates the id used in the label.
     *
     * @param s state setter
     * @return registration if available, else call sign if available else icao address if neither is available. If the
     * aircraft data is null, it returns the empty string.
     */
    private String getAircraftIdForLabel(ObservableAircraftState s) {

        if (!Objects.isNull(s.getAircraftData())) {
            String id = s.getRegistration().string();
            if (id.isEmpty()) {
                id = s.getCallSign().string();
            }
            if (id.isEmpty()) {
                id = s.getIcaoAddress().string();
            }
            return id;
        }
        return "";
    }


    /**
     * Creates the trajectory subgroup in the scene graph. A trajectory is a group of lines connecting
     * the various positions contained in the list of positions accumulated by the state setter.
     *
     * @param s state setter
     */
    private void createTrajectoryGroup(ObservableAircraftState s, Group annotatedAircraftGroup) {

        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");
        annotatedAircraftGroup.getChildren().add(trajectoryGroup);
        ObservableList<ObservableAircraftState.AirbornePos> trajectory = s.getTrajectory();
        trajectoryGroup.visibleProperty().bind(
                this.selectedAircraft.map(sp -> sp.equals(s)));
        trajectoryGroup.visibleProperty().addListener((o, ov, nv) -> {

                    if (trajectoryGroup.isVisible()) {
                        trajectory.addListener(
                                (ListChangeListener<ObservableAircraftState.AirbornePos>) change ->
                                {
                                    trajectoryGroup.getChildren().clear();
                                    while (change.next()) {
                                        if (change.wasAdded()) {
                                            this.computeTrajectory(trajectoryGroup, s.getTrajectory(), this.mapParams.getZoomValue());
                                        }
                                    }
                                }
                        );

                        this.mapParams.zoomProperty().addListener((p, oldVal, newVal) -> {
                            trajectoryGroup.getChildren().clear();
                            this.computeTrajectory(trajectoryGroup, s.getTrajectory(), newVal.intValue());
                        });

                        trajectoryGroup.layoutXProperty().bind(this.mapParams.minXProperty().negate());
                        trajectoryGroup.layoutYProperty().bind(this.mapParams.minYProperty().negate());
                    }


                }
        );
    }

    /**
     * Method for computing the trajectory
     *
     * @param list      list of positions
     * @param zoomValue current zoom value
     */
    private void computeTrajectory(Group trajectoryGroup, ObservableList<ObservableAircraftState.AirbornePos> list, int zoomValue) {

        // start wit an offset so that we can access the next point

        Iterator<ObservableAircraftState.AirbornePos> iterator = list.iterator();
        iterator.next();

        list.forEach(pos -> {
                    if (iterator.hasNext()) {

                        Line line = new Line();

                        double x = WebMercator.x(zoomValue, pos.position().longitude());
                        double y = WebMercator.y(zoomValue, pos.position().latitude());

                        line.setStartX(x);
                        line.setStartY(y);

                        ObservableAircraftState.AirbornePos nextPos = iterator.next();

                        double x_next = WebMercator.x(zoomValue, nextPos.position().longitude());
                        double y_next = WebMercator.y(zoomValue, nextPos.position().latitude());

                        line.setEndX(x_next);
                        line.setEndY(y_next);

                        Stop s1 = new Stop(0, ColorRamp.PLASMA.at(this.computeColorIndex(pos.altitude())));
                        Stop s2 = new Stop(1, ColorRamp.PLASMA.at(this.computeColorIndex(nextPos.altitude())));

                        line.setStroke(new LinearGradient(x, y, x_next, y_next, true, CycleMethod.NO_CYCLE, s1, s2));
                        trajectoryGroup.getChildren().add(line);
                    }

                }
        );
    }


    /**
     * Computes the color index, according to the formula c = [altitude/12000] ^ (1/3).
     *
     * @param altitude altitude of the aircraft.
     * @return index c, which determines the color from the spectrum that will be chosen.
     */
    private double computeColorIndex(double altitude) {
        return Math.pow(altitude / 12000.0, 1d / 3d);
    }


    /**
     * Access the main pane.
     *
     * @return pane superposed with background map.
     */
    public Pane pane() {
        return this.pane;
    }
}
