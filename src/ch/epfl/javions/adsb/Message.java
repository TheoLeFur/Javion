package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

public interface Message {

    /**
     * Extract the time stamp in nanoseconds from the message
     *
     * @return time stamp (in ns)
     * @author Theo Le Fur
     */
    public abstract long timeStampNs();

    /**
     * Extract the address from a message
     *
     * @return instance of IcaoAddress
     * @author Theo Le Fur
     */
    public abstract IcaoAddress icaoAddress();
}
