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
 * Represents the state of an aircraft, this state is characterised by the fact
 * it's observable in the sence of the Observer design pattern
 *
 * @author Rudolf Yazbeck (SCIPER : 360700)
 * @author Theo Le Fur (SCIPER : 363294)
 */
public final class ObservableAircraftState implements AircraftStateSetter {
    public record AirbornePos(GeoPos position, double altitude) {
    }
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNs;
    private final IntegerProperty category;
    private final ObjectProperty<CallSign> callSign;
    private final ObjectProperty<GeoPos> position;
    private final ObservableList<AirbornePos> trajectory;
    private final ObservableList<AirbornePos> unmodifiableTrajectory;
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
        this.unmodifiableTrajectory = unmodifiableObservableList(trajectory);
        this.altitude = new SimpleDoubleProperty();
        this.velocity = new SimpleDoubleProperty();
        this.trackOrHeading = new SimpleDoubleProperty();
    }

    public IcaoAddress getIcaoAddress() {
        return this.icaoAddress;
    }

    public AircraftData getAircraftData() {
        return this.aircraftData;
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
        if (!Objects.isNull(this.getPosition())) {
            AirbornePos currentPosition = new AirbornePos(this.getPosition(), this.getAltitude());
            if (this.trajectory.isEmpty() ||  getPosition().equals(this.getLastPosition().position)) {
                trajectory.add(currentPosition);
                this.lastTimeStampAddedToTrajectory = this.getLastMessageTimeStampNs();
            } else if (lastTimeStampAddedToTrajectory == this.getLastMessageTimeStampNs()) this.setLastPosition(currentPosition);
        }
    }

    private AirbornePos getLastPosition() {
        return this.trajectory.get(this.trajectory.size() -1);
    }

    private void setLastPosition(AirbornePos newPosition) {
        this.trajectory.set(this.trajectory.size() - 1, newPosition);
    }

}
