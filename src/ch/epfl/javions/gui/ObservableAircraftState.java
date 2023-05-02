package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class has the purpose of tracking the evolution of the aircraft, that will subsequently be visible on the map.
 * We track and update te relevant data, in particular, we track the trajectories of the aircraft after having received a
 * first message, so that we will be able to plot them on the map.
 */
public final class ObservableAircraftState implements AircraftStateSetter {

    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNs;
    private final IntegerProperty category;
    private CallSign callSign;
    private final DoubleProperty altitude;
    private GeoPos position;

    // Observable List of trajectories, that can therefore be modified if needed
    private final ObservableList<AirbornePos> trajectoryObservable;
    // immutable copy of the above that will be returned whenever trajectory list is accessed outside this class
    private final ObservableList<AirbornePos> trajectoryUnmodifiable;
    private final DoubleProperty velocity;
    private final DoubleProperty trackOrHeading;


    /**
     * Initialise the observable state tracker for the aircraft.
     *
     * @param icaoAddress  address of the aircraft
     * @param aircraftData record of te main data describing the aircraft, namely : {registration, typeDesignator, model, description, wakeTurbulenceCategory}
     */

    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;

        altitude = new SimpleDoubleProperty();
        category = new SimpleIntegerProperty();
        lastMessageTimeStampNs = new SimpleLongProperty();
        velocity = new SimpleDoubleProperty();
        trackOrHeading = new SimpleDoubleProperty();

        trajectoryObservable = FXCollections.observableArrayList();
        trajectoryUnmodifiable = FXCollections.unmodifiableObservableList(trajectoryObservable);
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        if (lastMessageTimeStampNs.get() == timeStampNs) {
            if (!trajectoryObservable.isEmpty()) {
                trajectoryObservable.remove(trajectoryObservable.size() - 1);
            }
            trajectoryObservable.add(new AirbornePos(this.position, this.altitude.get()));
        }

        lastMessageTimeStampNs.setValue(timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign = callSign;
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position = position;

        if (trajectoryObservable.isEmpty() || trajectoryObservable.get(trajectoryObservable.size() - 1).position != position) {
            trajectoryObservable.add(new AirbornePos(position, altitude.get()));
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

    /**
     * Getter for the last timestamp property
     *
     * @return property holding the timestamp of the most recently added message
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    /**
     * Access the value of the last timestamp
     *
     * @return value held in the last timestamp property
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.getValue();
    }

    /**
     * Access the category property
     *
     * @return categiry property
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    /**
     * Access the category property's value
     *
     * @return value held in the category property
     */
    public int getCategory() {
        return category.get();
    }

    public CallSign getCallSign() {
        return callSign;
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public double getAltitude() {
        return altitude.get();
    }

    public GeoPos getPosition() {
        return position;
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    public double getVelocity() {
        return velocity.get();
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    public AircraftData getAircraftData() {
        return aircraftData;
    }

    public AircraftRegistration getRegistration() {
        return aircraftData.registration();
    }

    public AircraftTypeDesignator typeDesignator() {
        return aircraftData.typeDesignator();
    }

    public String getModel() {
        return aircraftData.model();
    }

    public AircraftDescription getDescription() {
        return aircraftData.description();
    }

    public WakeTurbulenceCategory wakeTurbulenceCategory() {
        return aircraftData.wakeTurbulenceCategory();
    }

    public ObservableList<AirbornePos> trajectoryProperty() {
        return trajectoryObservable;
    }

    public ObservableList<AirbornePos> getTrajectory() {
        return trajectoryUnmodifiable;
    }

    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }

    /**
     * Record of the most up-to-date position and altitude of the aircraft.
     *
     * @param position recent position
     * @param altitude recent altitude
     */
    public record AirbornePos(GeoPos position, double altitude) {
    }
}