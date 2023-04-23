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
    private LongProperty lastMessageTimeStampNs;
    private IntegerProperty category; //readOnlyIntegerProperty
    private ObjectProperty<CallSign> callSign;
    private ObjectProperty<GeoPos> position;
    private ObservableList<AirbornePos> trajectory; //observable and modifiable
    private DoubleProperty altitude;       //in meters
    private DoubleProperty velocity;       //in m/s
    private DoubleProperty trackOrHeading; //cap of the aircraft in radians
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private ObservableList<AirbornePos> unmodifiableTrajectory; //observable and non modifiable used for getters

    /**
     * Creates an instance of the state of the aircraft with states that can only be modified through setters (except the
     * trajectory that is calculated automatically)
     *
     * @param icaoAddress of the aircraft
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
        unmodifiableTrajectory = unmodifiableObservableList(observableArrayList());
        altitude = new SimpleDoubleProperty();
        velocity = new SimpleDoubleProperty();
        trackOrHeading = new SimpleDoubleProperty();
    }

    public record AirbornePos(GeoPos position, double altitude) {}

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
    public AircraftDescription getDescription(){
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

    public CallSign getCallSign() {
        return callSign.get();
    }

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
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

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public double getVelocity() {
        return velocity.get();
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        if(timeStampNs == getLastMessageTimeStampNs()) {
            AirbornePos currPosition = new AirbornePos(getPosition(), getAltitude());

            if(trajectory.isEmpty()) {
                trajectory.add(currPosition);
            } else {
                trajectory.set(trajectory.size() - 1, currPosition);
            }
        }

        this.lastMessageTimeStampNs.set(timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    /**
     * Changes the current position (latitude and longitude) that is registered in the state to the one given as a
     * parameter, if said position differs from the last element of trajectory (or if trajectory is null), a new element
     * is added to that list containing this position and the current altitude.
     * @param position new position
     */
    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);

        //not sure if the get(trajectory.size() - 1) gives an error if the trajectory is empty since we check for that
        if(trajectory.isEmpty() || !position.equals(trajectory.get(trajectory.size() - 1))) {
            trajectory.add(new AirbornePos(position, getAltitude()));
        }
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }
}
