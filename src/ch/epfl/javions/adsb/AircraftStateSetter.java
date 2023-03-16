package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

public interface AircraftStateSetter {
    /**
     * Changes the time stamp of the last message
     * @param timeStampNs Value of the time stamp we have changed
     */
    abstract void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Sets the category of the aircraft
     * @param category integer corresponding to a certain category
     */
    abstract void setCategory(int category);


    /**
     * Changes the indicator of the aircraft to the given value
     * @param callSign new callSign
     */
    abstract void setCallSign(CallSign callSign);

    /**
     * Changes the position of the aircraft to the newly given value
     * @param position new position
     */
    abstract void setPosition(GeoPos position);

    /**
     * Changes the altitude of the aircraft to the newly given value
     * @param altitude new altitude
     */
    abstract void setAltitude(double altitude);

    /**
     * Changes the velocity of the aircraft to the newly given value
     * @param velocity new velocity
     */
    abstract void setVelocity(double velocity);

    /**
     * Changes the direction of the aircraft to the newly given value
     * @param trackOrHeading new direction
     */
    abstract void setTrackOrHeading(double trackOrHeading);

}
