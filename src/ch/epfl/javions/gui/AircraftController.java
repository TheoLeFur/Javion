package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Iterator;
import java.util.Objects;

//TODO : make a method for creating the whole scene graph, so that it is simpler
//TODO : incorporate the visibility mechanisms in the label and the trajectory
//TODO : make the trajectory group
//TODO : formation & optimize the code.
//TODO : complete the docs

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


    private final String AircraftStyleSheetPath = "/aircraft.css";
    private final int LABEL_OFFSET = 4;
    private final int VISIBLE_LABEL_ZOOM_THRESHOLD = 11;
    private final MapParameters mapParams;
    private final ObservableSet<ObservableAircraftState> observablaAircraft;
    private final Property<ObservableAircraftState> stateProperty;
    private Group annotatedAircraftGroup;
    private Group trajectoryGroup;
    private Group labelIconGroup;
    private Group labelGroup;
    private SVGPath icon;


    private final Pane pane;

    public AircraftController(MapParameters mapParams, ObservableSet<ObservableAircraftState> observableAircraft, Property<ObservableAircraftState> stateProperty) {
        // init the constructor params
        this.mapParams = mapParams;
        this.observablaAircraft = observableAircraft;
        this.stateProperty = stateProperty;

        // Build the scene graph

        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
        this.pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource(this.AircraftStyleSheetPath)).toString());


        // adds the groups of the initial set passed into construction.
        this.observablaAircraft.forEach(ss -> {
            this.createAnnotatedAircraftGroup(ss);
            this.createLabelIconGroup(ss);
            this.createIcon(ss);
            this.createLabel(ss);
        });

        // track changes of the set of states
        this.observablaAircraft.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            ObservableAircraftState elementAdded = change.getElementAdded();
            if (!Objects.isNull(elementAdded)) {
                this.createAnnotatedAircraftGroup(elementAdded);
                this.createLabelIconGroup(elementAdded);
                this.createIcon(elementAdded);
                this.createLabel(elementAdded);
            }
            ObservableAircraftState elementRemoved = change.getElementRemoved();
            if (!Objects.isNull(elementRemoved)) {
                this.pane.getChildren().removeIf(e -> Objects.equals(e.getId(), elementRemoved.getIcaoAddress().toString()));
            }
        });


    }

    private void createAnnotatedAircraftGroup(ObservableAircraftState s) {
        this.annotatedAircraftGroup = new Group();
        this.annotatedAircraftGroup.setId(s.getIcaoAddress().toString());
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
        this.labelIconGroup.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
            double projectedX = WebMercator.x(this.mapParams.getZoomValue(), s.getPosition().longitude());
            return projectedX - this.mapParams.getMinXValue();
        }, this.mapParams.getZoom(), this.mapParams.getMinX(), s.positionProperty()));

        this.labelIconGroup.layoutYProperty().bind(Bindings.createDoubleBinding(() -> {
            double projectedY = WebMercator.y(this.mapParams.getZoomValue(), s.getPosition().latitude());
            return projectedY - this.mapParams.getMinYValue();
        }, this.mapParams.getZoom(), this.mapParams.getMinY(), s.positionProperty()));
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
        // fills the icon with the proper color

        this.icon.contentProperty().bind(aircraftIconProperty.map(AircraftIcon::svgPath));
        this.icon.rotateProperty().bind(Bindings.createDoubleBinding(() -> {
            if (aircraftIconProperty.get().canRotate()) {
                return Units.convertTo(s.getTrackOrHeading(), Units.Angle.DEGREE);
            } else {
                return 0d;
            }
        }, s.trackOrHeadingProperty(), aircraftIconProperty));
        this.icon.fillProperty().bind(s.altitudeProperty().map(a -> ColorRamp.PLASMA.at(this.computeColorIndex(a.doubleValue()))));

    }

    private void createLabel(ObservableAircraftState s) {

        this.labelGroup = new Group();
        this.labelGroup.getStyleClass().add("label");
        this.labelIconGroup.getChildren().add(this.labelGroup);
        this.labelGroup.visibleProperty().bind(
                this.mapParams.getZoom().map(
                        z -> (z.intValue() >= 11)
                )
        );
        System.out.println(this.mapParams.getZoomValue());


        Text text = new Text();
        Rectangle background = new Rectangle();
        this.labelGroup.getChildren().add(background);
        this.labelGroup.getChildren().add(text);

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
            return String.valueOf((int) (Units.convertTo(s.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
        }
        return "?";
    }


    /**
     * Evaluates the id used in the label.
     *
     * @param s state setter
     * @return registration if available, else call sign if available else icao address if neither is available. If the
     * aircraft data is null, it return the empty string.
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
    private void createTrajectoryGroup(ObservableAircraftState s) {

        this.trajectoryGroup = new Group();
        this.trajectoryGroup.getStyleClass().add("trajectory");
        this.annotatedAircraftGroup.getChildren().add(trajectoryGroup);

        ObservableList<ObservableAircraftState.AirbornePos> trajectory = s.getTrajectory();
        Iterator<ObservableAircraftState.AirbornePos> iterator = trajectory.iterator();

        // start wit an offset so that we can access the next point

        iterator.next();
        trajectory.forEach(pos -> {
            Line line = new Line();
            double x = WebMercator.x(this.mapParams.getZoomValue(), pos.position().longitude());
            double y = WebMercator.y(this.mapParams.getZoomValue(), pos.position().latitude());
            line.setStartX(x);
            line.setStartY(y);
            if (iterator.hasNext()) {
                double x_next = WebMercator.x(this.mapParams.getZoomValue(), iterator.next().position().longitude());
                double y_next = WebMercator.y(this.mapParams.getZoomValue(), iterator.next().position().latitude());
                line.setEndX(x_next);
                line.setEndY(y_next);
            }
            this.trajectoryGroup.getChildren().add(line);
        });


        this.trajectoryGroup.layoutXProperty().bind(this.mapParams.getMinX().negate());
        this.trajectoryGroup.layoutYProperty().bind(this.mapParams.getMinY().negate());

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

    public static void main(String[] args) {
        String str = String.format("%f meters, %f velocity", 12.00, 13.00);
        System.out.println(str);

    }
}
