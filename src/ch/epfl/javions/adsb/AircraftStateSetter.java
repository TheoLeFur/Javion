package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public interface AircraftStateSetter {
    /**
     * Changes the time stamp of the last message
     *
     * @param timeStampNs Value of the time stamp we have changed
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Sets the category of the aircraft
     *
     * @param category integer corresponding to a certain category
     */
    void setCategory(int category);


    /**
     * Changes the indicator of the aircraft to the given value
     *
     * @param callSign new callSign
     */
    void setCallSign(CallSign callSign);

    /**
     * Changes the position of the aircraft to the newly given value
     *
     * @param position new position
     */
    void setPosition(GeoPos position);

    /**
     * Changes the altitude of the aircraft to the newly given value
     *
     * @param altitude new altitude
     */
    void setAltitude(double altitude);

    /**
     * Changes the velocity of the aircraft to the newly given value
     *
     * @param velocity new velocity
     */
    void setVelocity(double velocity);

    /**
     * Changes the direction of the aircraft to the newly given value
     *
     * @param trackOrHeading new direction
     */
    void setTrackOrHeading(double trackOrHeading);

}
