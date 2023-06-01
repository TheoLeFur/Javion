package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
 * @author Theo Le Fur
 * SCIPER : 363294
 * Class for controlling the display of the aircraft on the background map. It will display
 * the icons, associated information and trajectory : everything colored as a function of the altitude of
 * the aircraft.
 */
public final class AircraftController {


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
     * This module will control the display of the aircraft on the map, their labels and trajectories.
     *
     * @param mapParams          parameters of the visible map (minX, minY and zoom value)
     * @param observableAircraft set of aircraft that are currently observable
     */

    public AircraftController(
            MapParameters mapParams,
            ObservableSet<ObservableAircraftState> observableAircraft,
            ObjectProperty<ObservableAircraftState> selectedAircraft) {


        // init the constructor params
        this.mapParams = mapParams;
        this.selectedAircraft = selectedAircraft;
        this.observableAircraft = observableAircraft;

        // Build the scene graph
        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
        this.pane.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource(
                                this.AircraftStyleSheetPath)).toString());

        // We construct a graph for every state in the initial set of values passed into the constructor
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
                this.pane.getChildren().removeIf(e -> e.getId().equals(elementRemoved.getIcaoAddress().string()));
            }
        });

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
        icon.setOnMouseClicked(event -> this.selectedAircraft.setValue(s));
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
                },
                this.mapParams.zoomProperty(), this.mapParams.minYProperty(), s.positionProperty()));

        return labelIconGroup;
    }


    /**
     * Create the icon element in the scene graph.
     *
     * @param s state setter
     */
    private SVGPath createIcon(ObservableAircraftState s, Group labelIconGroup) {

        SVGPath icon = new SVGPath();
        labelIconGroup.getChildren().add(icon);
        icon.getStyleClass().add("aircraft");

        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>(getOrDefaultIcon(s));
        aircraftIconProperty.bind(s.categoryProperty().map(d -> getOrDefaultIcon(s, d.intValue())));

        // we bind the icon property to the category property, so that it tracks the changes.
        // bind both the content and the can rotate properties to the methods in AircraftIcon.

        icon.contentProperty().bind(aircraftIconProperty.map(AircraftIcon::svgPath));
        icon.rotateProperty().bind(
                Bindings.createDoubleBinding(() -> aircraftIconProperty
                                .get()
                                .canRotate() ? Units.convertTo(s.getTrackOrHeading(), Units.Angle.DEGREE) : 0d,
                        s.trackOrHeadingProperty(), aircraftIconProperty));
        icon.fillProperty().bind(s.altitudeProperty().map(a -> ColorRamp.PLASMA.at(this.computeColorIndex(a.intValue()))));

        return icon;
    }

    /**
     * Create an aircraft icon according to the current aircraft state
     *
     * @param s observable aircraft state
     * @return icon if aircraft data is not null, else default icon
     */
    private AircraftIcon getOrDefaultIcon(ObservableAircraftState s) {
        return (s.getAircraftData() == null)
                ? AircraftIcon.iconFor(new AircraftTypeDesignator(""), new AircraftDescription(""), 0, WakeTurbulenceCategory.UNKNOWN)
                : AircraftIcon.iconFor(s.getTypeDesignator(), s.getDescription(), s.getCategory(), s.getWakeTurbulenceCategory());
    }

    /**
     * Create an aircraft icon according to the current aircraft state
     *
     * @param s        observable aircraft state
     * @param category cat. passed into parameters. useful for binding to the category property.
     * @return icon if aircraft data is not null, else default icon
     */

    private AircraftIcon getOrDefaultIcon(ObservableAircraftState s, int category) {
        return (s.getAircraftData() == null)
                ? AircraftIcon.iconFor(new AircraftTypeDesignator(""), new AircraftDescription(""), 0, WakeTurbulenceCategory.UNKNOWN)
                : AircraftIcon.iconFor(s.getTypeDesignator(), s.getDescription(), category, s.getWakeTurbulenceCategory());
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
        labelGroup.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> this.mapParams.zoomProperty().getValue() >= VISIBLE_LABEL_ZOOM_THRESHOLD ||
                        s.equals(this.selectedAircraft.getValue()),
                this.mapParams.zoomProperty(),
                this.selectedAircraft));

        Text text = new Text();
        Rectangle background = new Rectangle();
        labelGroup.getChildren().add(background);
        labelGroup.getChildren().add(text);

        text.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%s \n %s (km/h) \u2002 %s (m) ",
                        this.getAircraftIdForLabel(s),
                        this.getVelocityForLabel(s),
                        this.getAltitudeForLabel(s)),
                s.altitudeProperty(), s.velocityProperty(), s.callSignProperty()));

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
        return !Double.isNaN(s.getVelocity())
                ? String.valueOf((int) Units.convertTo(s.getVelocity(), Units.Speed.KILOMETER_PER_HOUR))
                : "?";
    }

    /**
     * Formats the altitude on the labels display
     *
     * @param s state setter
     * @return value of the altitude in m if available, else ?
     */
    private String getAltitudeForLabel(ObservableAircraftState s) {
        return !Double.isNaN(s.getAltitude())
                ? String.valueOf((int) s.getAltitude())
                : "?";
    }


    /**
     * Evaluates the id used in the label.
     *
     * @param s state setter
     * @return registration if available, else call sign if available else icao address if neither is available. If the
     * aircraft data is null, it returns the empty string.
     */
    private String getAircraftIdForLabel(ObservableAircraftState s) {

        String id = "";
        if (!(s.getAircraftData() == null)) {
            id = s.getRegistration().string();
            if (id.isEmpty())
                id = s.getCallSign().string();
            if (id.isEmpty())
                id = s.getIcaoAddress().string();
        }
        return (id.isEmpty()) ? s.getIcaoAddress().toString() : id;
    }


    /**
     * Creates the trajectory subgroup in the scene graph. A trajectory is a group of lines connecting
     * the various positions contained in the list of positions accumulated by the state setter. The trajectory is
     * computed only if necessary, that is, whenever an aircraft is selected.
     *
     * @param s state setter
     */
    private void createTrajectoryGroup(ObservableAircraftState s, Group annotatedAircraftGroup) {

        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");
        annotatedAircraftGroup.getChildren().add(trajectoryGroup);

        ObservableList<ObservableAircraftState.AirbornePos> trajectory = s.getTrajectory();
        trajectoryGroup.visibleProperty().bind(this.selectedAircraft.map(sp -> sp.equals(s)));

        // TODO : look at this again.
        trajectoryGroup.visibleProperty().addListener((o, ov, nv) -> {

            ChangeListener<? super Number> lambda = (old, oldv, newv) -> {
                trajectoryGroup.getChildren().clear();
                this.computeTrajectory(trajectoryGroup, trajectory, this.mapParams.getZoomValue());
            };

            ListChangeListener<? super ObservableAircraftState.AirbornePos> lambda2 = (change) -> {
                trajectoryGroup.getChildren().clear();
                while (change.next())
                    if (change.wasAdded())
                        this.computeTrajectory(trajectoryGroup, trajectory, this.mapParams.getZoomValue());
            };

            if (nv) {
                computeTrajectory(trajectoryGroup, trajectory, this.mapParams.getZoomValue());
                trajectory.addListener(lambda2);
                this.mapParams.zoomProperty().addListener(lambda);

            } else {
                trajectory.removeListener(lambda2);
                this.mapParams.zoomProperty().removeListener(lambda);
            }
        });

        trajectoryGroup.layoutXProperty().bind(this.mapParams.minXProperty().negate());
        trajectoryGroup.layoutYProperty().bind(this.mapParams.minYProperty().negate());
    }

    /**
     * Method for computing the trajectory. Propagates through the forward trajectory and creates lines
     * joining every two point succeeding each other
     *
     * @param list      list of positions
     * @param zoomValue current zoom value
     */
    private void computeTrajectory(Group trajectoryGroup, ObservableList<ObservableAircraftState.AirbornePos> list, int zoomValue) {

        Iterator<ObservableAircraftState.AirbornePos> it = list.iterator();
        // start wit an offset so that we can access the next point
        if (it.hasNext()) it.next();

        list.forEach(pos -> {
                    if (it.hasNext()) {
                        Line line = new Line();
                        line.setStartX(WebMercator.x(zoomValue, pos.position().longitude()));
                        line.setStartY(WebMercator.y(zoomValue, pos.position().latitude()));

                        ObservableAircraftState.AirbornePos nextPos = it.next();

                        line.setEndX(WebMercator.x(zoomValue, nextPos.position().longitude()));
                        line.setEndY(WebMercator.y(zoomValue, nextPos.position().latitude()));

                        // coloring of the trajectory
                        this.trajectoryColorHandler(line, pos, nextPos);
                        trajectoryGroup.getChildren().add(line);
                    }
                }
        );

    }

    /**
     * Handles the coloring of the trajectory. Creates a linear gradient controlled by a function of
     * the aircraft's altitude.
     *
     * @param line    line joining the points of coordinates pos and nextPos
     * @param pos     beginning of the line
     * @param nextPos end of the line
     */

    private void trajectoryColorHandler(Line line, ObservableAircraftState.AirbornePos pos, ObservableAircraftState.AirbornePos nextPos) {
        Stop s1 = new Stop(0, ColorRamp.PLASMA.at(this.computeColorIndex(pos.altitude())));
        Stop s2 = new Stop(1, ColorRamp.PLASMA.at(this.computeColorIndex(nextPos.altitude())));
        line.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, s1, s2));
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
