package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.unmodifiableObservableList;


/**
 * Represents the state of an aircraft, this state is characterised by the fact
 * it's observable in the sence of the Observer design pattern
 *
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private LongProperty lastMessageTimeStampNs;
    private IntegerProperty category; //readOnlyIntegerProperty
    private ObjectProperty<CallSign> callSign;
    private ObjectProperty<GeoPos> position;
    private ObservableList<AirbornePos> trajectory; //observable and modifiable
    private DoubleProperty altitude;       //in meters
    private DoubleProperty velocity;       //in m/s
    private DoubleProperty trackOrHeading; //cap of the aircraft in radians
    private ObservableList<AirbornePos> unmodifiableTrajectory; //observable and non modifiable used for getters
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
        lastMessageTimeStampNs = new SimpleLongProperty();
        category = new SimpleIntegerProperty();
        callSign = new SimpleObjectProperty<>();
        position = new SimpleObjectProperty<>();
        trajectory = observableArrayList();
        unmodifiableTrajectory = unmodifiableObservableList(trajectory);
        altitude = new SimpleDoubleProperty();
        velocity = new SimpleDoubleProperty();
        trackOrHeading = new SimpleDoubleProperty();
    }

    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }

    public AircraftData getAircraftData() {
        return aircraftData;
    }

    public AircraftRegistration getRegistration() {
        return aircraftData.registration();
    }

    public AircraftTypeDesignator getTypeDesignator() {
        return aircraftData.typeDesignator();
    }

    public String getModel() {
        return aircraftData.model();
    }

    public AircraftDescription getDescription() {
        return aircraftData.description();
    }

    public WakeTurbulenceCategory getWakeTurbulenceCategory() {
        return aircraftData.wakeTurbulenceCategory();
    }

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    public int getCategory() {
        return category.get();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public CallSign getCallSign() {
        return callSign.get();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.lastMessageTimeStampNs.set(timeStampNs);
    }

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    public ReadOnlyProperty<CallSign> callsignProperty() {
        return callSign;
    }

    public GeoPos getPosition() {
        return position.get();
    }

    /**
     * Changes the current position (latitude and longitude) that is registered in the state to the one given as a
     * parameter, if said position differs from the last element of trajectory (or if trajectory is null), a new element
     * is added to that list containing this position and the current altitude.
     *
     * @param position new position
     */
    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        updateTrajectory();
    }

    public ReadOnlyProperty<GeoPos> positionProperty() {
        return position;
    }

    public ObservableList<AirbornePos> getTrajectory() {
        return unmodifiableTrajectory;
    }

    public ObservableList<AirbornePos> trajectoryProperty() {
        return unmodifiableTrajectory;
    }

    public double getAltitude() {
        return altitude.get();
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        updateTrajectory();
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public double getVelocity() {
        return velocity.get();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    private void updateTrajectory() {
        if (getPosition() != null) {

            AirbornePos currPosition = new AirbornePos(getPosition(), getAltitude());
            if (trajectory.isEmpty() || !getPosition().equals(trajectory.get(trajectory.size() - 1).position)) {
                lastTimeStampAddedToTrajectory = getLastMessageTimeStampNs();
                trajectory.add(currPosition);
            }

            if (lastTimeStampAddedToTrajectory == getLastMessageTimeStampNs()) {
                if (trajectory.isEmpty()) {
                    trajectory.add(currPosition);
                } else {
                    trajectory.set(trajectory.size() - 1, currPosition);
                }
            }

        }
    }

    public record AirbornePos(GeoPos position, double altitude) {
    }
}
