package ch.epfl.javions.gui;

import ch.epfl.javions.WebMercator;
import com.sun.javafx.fxml.expression.LiteralExpression;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;

import java.util.Iterator;
import java.util.List;
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
     * Afterwards, it will be placed in the constructor at the correct place.
     */


    private final String AircraftStyleSheetPath = "resources/aircraft.css";
    private final MapParameters mapParams;
    private final ObservableSet<ObservableAircraftState> observablaAircraft;
    private final Property<ObservableAircraftState> stateProperty;
    private Group annotatedAircraftGroup;
    private Group trajectoryGroup;
    private Group labelIconGroup;
    private Group labelGroup;
    private SVGPath icon;


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
                    ObservableAircraftState s = change.getElementAdded();
                    this.pane.getChildren().removeIf(
                            e -> Objects.equals(e.getId(),
                                    change.getElementRemoved().getIcaoAddress().toString()));
                });
    }


    private void createAnnotatedAircraftGroup(ObservableAircraftState s) {
        this.annotatedAircraftGroup = new Group();
        this.labelIconGroup.setId(s.getIcaoAddress().toString());
        this.pane.getChildren().add(this.annotatedAircraftGroup);
        this.annotatedAircraftGroup.getStylesheets().add(AircraftStyleSheetPath);
        // This guarantees that we the display overlaps icon from the highest altitude to the lowest altitude
        this.annotatedAircraftGroup.viewOrderProperty().bind(s.altitudeProperty().negate());

    }


    /**
     * Creates the label-icon group in the scene graph that takes care of the positioning of the two subgroups
     * label and icon.
     *
     * @param s state setter
     */
    private void createLabelIconGroup(ObservableAircraftState s) {
        this.labelIconGroup = new Group();
        this.annotatedAircraftGroup.getChildren().add(this.labelIconGroup);
        this.labelIconGroup.layoutXProperty()
                .bind(
                        Bindings.createDoubleBinding(() -> {
                            double projectedX = WebMercator.x(
                                    this.mapParams.getZoomValue(), s.getPosition().longitude());
                            return projectedX - this.mapParams.getMinXValue();
                        }, this.mapParams.getZoom(), this.mapParams.getMinX())
                );

        this.labelIconGroup.layoutYProperty()
                .bind(
                        Bindings.createDoubleBinding(
                                () -> {
                                    double projectedY = WebMercator.y(
                                            this.mapParams.getZoomValue(), s.getPosition().latitude());
                                    return projectedY - this.mapParams.getMinYValue();
                                }, this.mapParams.getZoom(), this.mapParams.getMinY())
                );
    }

    /**
     * Create the icon element in the scene graph
     *
     * @param s state setter
     */
    private void createIcon(ObservableAircraftState s) {
        this.icon = new SVGPath();
        this.labelIconGroup.getChildren().add(this.icon);
        this.icon.getStyleClass().add("aircraft");

        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>(AircraftIcon.iconFor(
                s.getTypeDesignator(),
                s.getDescription(),
                s.getCategory(),
                s.getWakeTurbulenceCategory()
        ));

        // we bind the icon property to the category property, so that it tracks the changes.
        aircraftIconProperty.bind(s.categoryProperty().map(
                d -> AircraftIcon.iconFor(
                        s.getTypeDesignator(),
                        s.getDescription(),
                        d.intValue(),
                        s.getWakeTurbulenceCategory()
                )
        ));

        // bind both the content and the can rotate properties to the methods in AircraftIcon.
        // fills the icon with the proper color

        this.icon.contentProperty().bind(aircraftIconProperty.map(AircraftIcon::svgPath));
        this.icon.rotateProperty().bind(s.trackOrHeadingProperty().map(
                tohp -> (aircraftIconProperty.get().canRotate())
                        ? tohp.doubleValue()
                        : 0
        ));
        this.icon.fillProperty().bind(s.altitudeProperty().map(
                a -> ColorRamp.PLASMA.at(this.computeColorIndex(a.doubleValue()))
        ));


    }

    /**
     * Creates the trajectory subgroup in the scene graph. A trajectory is a group of lines connecting
     * the various positions contained in the list of positions accumulated by the state setter.
     *
     * @param s state setter
     */
    private void createTrajectoryGroup(ObservableAircraftState s) {

        this.trajectoryGroup = new Group();
        this.trajectoryGroup.getStyleClass().add("trajectory");
        this.annotatedAircraftGroup.getChildren().add(trajectoryGroup);

        List<ObservableAircraftState.AirbornePos> trajectory = s.getTrajectory();
        Iterator<ObservableAircraftState.AirbornePos> iterator = trajectory.iterator();
        iterator.next();
        trajectory.forEach(
                pos -> {
                    Line line = new Line();
                    double x = WebMercator.x(this.mapParams.getZoomValue(), pos.position().longitude());
                    double y = WebMercator.y(this.mapParams.getZoomValue(), pos.position().latitude());
                    line.setStartX(x);
                    line.setStartY(y);
                    if (iterator.hasNext()){
                        double x_next = WebMercator.x(this.mapParams.getZoomValue(), iterator.next().position().longitude());
                        double y_next = WebMercator.y(this.mapParams.getZoomValue(), iterator.next().position().latitude());
                        line.setEndX(x_next);
                        line.setEndY(y_next);
                    }
                    this.trajectoryGroup.getChildren().add(line);

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
        return Math.pow(Math.rint(altitude / 12000d), 1d / 3d);
    }

    /**
     * Access the main pane.
     *
     * @return pane superposed with background map.
     */
    public Pane pane() {
        return this.pane;
    }
    public static void main(String[] args){
         List<Integer> list = List.of(1,2,3,4,5);
         Iterator<Integer> it = list.iterator();
         it.next();
         list.forEach(
                 i -> {
                     System.out.println(i);
                     if (it.hasNext()) {
                         System.out.println(it.next());
                     }
                 }
         );
    }
}
