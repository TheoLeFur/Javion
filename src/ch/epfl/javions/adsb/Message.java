package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public interface Message {

    /**
     * Extract the time stamp in nanoseconds from the message
     *
     * @return time stamp (in ns)
     * @author Theo Le Fur SCIPER : 363294
     */
    public abstract long timeStampNs();

    /**
     * Extract the address from a message
     *
     * @return instance of IcaoAddress
     * @author Theo Le Fur SCIPER : 363294
     */
    public abstract IcaoAddress icaoAddress();
}
