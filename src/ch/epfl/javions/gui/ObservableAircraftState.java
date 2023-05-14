package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.Objects;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.unmodifiableObservableList;


/**
 * In this class, we represent the characteristic of an aircraft's state. It stores the
 * attributes of an aircraft that are susceptible to change in time in javaFX properties.
 * The class is implemented based on the javaFX bean pattern.
 *
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class ObservableAircraftState implements AircraftStateSetter {

    /**
     * Record of the position of the aircraft, which in 3d Euclidian space, is characterized by
     * (long, lat, A), where A is the aircraft's altitude, that is the distance between the aircraft and
     * the sphere.
     *
     * @param position position of the aircraft, characterised by an instance of GeoPos
     * @param altitude aircraft's altitude
     */
    public record AirbornePos(GeoPos position, double altitude) {
    }

    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNs;
    private final IntegerProperty category;
    private final ObjectProperty<CallSign> callSign;
    private final ObjectProperty<GeoPos> position;
    private final ObservableList<AirbornePos> trajectory;
    private final ObservableList<AirbornePos> trajectoryView;
    private final DoubleProperty altitude;
    private final DoubleProperty velocity;
    private final DoubleProperty trackOrHeading;
    private long lastTimeStampAddedToTrajectory;

    /**
     * Creates an instance of the state of the aircraft with states that can only be modified through setters (except the
     * trajectory that is calculated automatically)
     *
     * @param icaoAddress  of the aircraft
     * @param aircraftData of the aircraft
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {

        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;
        this.lastMessageTimeStampNs = new SimpleLongProperty();
        this.category = new SimpleIntegerProperty();
        this.callSign = new SimpleObjectProperty<>();
        this.position = new SimpleObjectProperty<>();
        this.trajectory = observableArrayList();
        this.trajectoryView = unmodifiableObservableList(this.trajectory);

        // both altitude and velocity are initialised to Nan, so that we can detect whenever
        // some data is absent from messages.

        this.altitude = new SimpleDoubleProperty(Double.NaN);
        this.velocity = new SimpleDoubleProperty(Double.NaN);
        this.trackOrHeading = new SimpleDoubleProperty();
    }

    /**
     * Getter to access IcaoAddress
     *
     * @return icaoAddress
     */
    public IcaoAddress getIcaoAddress() {
        return this.icaoAddress;
    }

    /**
     * Getter to access Aircraft Data
     *
     * @return Aircraft Data
     */

    public AircraftData getAircraftData() {
        return this.aircraftData;
    }

    /**
     * Getter to access the registration
     *
     * @return aircraft's registration no.
     */

    public AircraftRegistration getRegistration() {
        return aircraftData.registration();
    }

    /**
     * Getter to access the aircraft's type
     *
     * @return aircraft's type
     */

    public AircraftTypeDesignator getTypeDesignator() {
        return aircraftData.typeDesignator();
    }

    /**
     * Getter to access the aircraft's model
     *
     * @return aircraft's model
     */
    public String getModel() {
        return aircraftData.model();
    }

    /**
     * Getter to access the aircraft's description
     *
     * @return aircraft's description
     */

    public AircraftDescription getDescription() {
        return aircraftData.description();
    }

    /**
     * Getter to access the aircraft's turbulence category
     *
     * @return aircraft's turbulence category.
     */

    public WakeTurbulenceCategory getWakeTurbulenceCategory() {
        return aircraftData.wakeTurbulenceCategory();
    }

    /**
     * Access the category property. It makes sense to use a javaFX property for a placeholder
     * to the category's values, since the category may change through time in the situation when the
     * category is not available until some message will bring its value.
     *
     * @return category property
     */

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    /**
     * Access the category's value
     *
     * @return value stored in category property
     */

    public int getCategory() {
        return category.get();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    /**
     * Access the call sign property
     *
     * @return call sign property.
     */

    public ReadOnlyProperty<CallSign> callSignProperty() {
        return callSign;
    }

    /**
     * Access the call sign's value
     *
     * @return value stored in call sign property
     */


    public CallSign getCallSign() {
        return callSign.get();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    /**
     * Access the value of the timestamp of the last message.
     *
     * @return value of the last timestamp
     */

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.lastMessageTimeStampNs.set(timeStampNs);
    }

    /**
     * Access the property serving as placeholder to the value of the timestamp of the last message
     *
     * @return time stamp long property
     */

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return this.lastMessageTimeStampNs;
    }

    /**
     * Access the value held in the position property.
     * @return position in GeoPos coordinates.
     */
    public GeoPos getPosition() {
        return this.position.get();
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        if (!Double.isNaN(this.getAltitude())) {
            this.trajectory.add(new AirbornePos(position, this.altitudeProperty().getValue()));
        }
    }

    /**
     * Access the position property.
     * @return Position property
     */

    public ReadOnlyProperty<GeoPos> positionProperty() {
        return position;
    }

    /**
     * Access the trajectory as observable, unmodifiable list.
     * @return Position property
     */

    public ObservableList<AirbornePos> getTrajectory() {
        return trajectoryView;
    }

    /**
     * Access the value of the altitude held in the altitude property
     * @return value of the altitude
     */

    public double getAltitude() {
        return altitude.get();
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        if (!Objects.isNull(this.getPosition())) {
            AirbornePos pos = new AirbornePos(this.getPosition(), altitude);
            this.lastTimeStampAddedToTrajectory = this.getLastMessageTimeStampNs();
            if (trajectory.isEmpty()) {
                this.trajectory.add(pos);
            } else if (lastTimeStampAddedToTrajectory == this.getLastMessageTimeStampNs()) {
                this.setLastPosition(pos);
            }

        }
    }

    /**
     * Access the altitude property
     * @return altitude property
     */

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    /**
     * Access the value of the velocity
     * @return velocity
     */

    public double getVelocity() {
        return velocity.get();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    /**
     * Access the velocity property
     * @return velocity property
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    /**
     * Access the value held in the track or heading property
     * @return track or heading value
     */

    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    /**
     * Access the track or heading property
     * @return track or heading property
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }


    private AirbornePos getLastPosition() {
        return this.trajectory.get(this.trajectory.size() - 1);
    }

    private void setLastPosition(AirbornePos newPosition) {
        this.trajectory.set(this.trajectory.size() - 1, newPosition);
    }

}
