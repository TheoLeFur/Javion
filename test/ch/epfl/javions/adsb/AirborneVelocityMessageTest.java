package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AirborneVelocityMessageTest {


    @Test
    void IllegalArgumentExceptionsInConstructorAreRaised() {
        long timeStampNs = -1;
        IcaoAddress icaoAddress = new IcaoAddress("09FA6E");
        double velocity = -1;
        double trackOrHeading = -1;
        assertThrows(IllegalArgumentException.class, () -> new AirborneVelocityMessage(timeStampNs, icaoAddress, velocity, trackOrHeading));
    }


    @Test
    void NullPointerExceptionInConstructorIsRaised() {
        long timeStampNs = 0;
        IcaoAddress icaoAddress = null;
        double velocity = 0;
        double trackOrHeading = 0;
        assertThrows(NullPointerException.class, () -> new AirborneVelocityMessage(timeStampNs, icaoAddress, velocity, trackOrHeading));
    }
}
