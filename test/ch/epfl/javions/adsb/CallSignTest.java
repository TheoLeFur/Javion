package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CallSignTest {

    /**
     * @author Rudolf Yazbeck
     */
    @Test
    void string() {
        //checking if the empty string doesn't return any errors
        new CallSign("");
        //checking if a correct callsign works
        new CallSign("ABCD1234");
        //checking if a string with the wrong number of elements returns an error
        assertThrows(IllegalArgumentException.class, () -> new CallSign("AAAAAAAAA"));

        //checking if a string with the wrong elements returns an error
        assertThrows(IllegalArgumentException.class, () -> new CallSign("AAAAAAA-"));
    }
    @Test
    void callSignConstructorThrowsWithInvalidCallSign() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CallSign("callsign");
        });
    }

    @Test
    void callSignConstructorAcceptsEmptyCallSign() {
        assertDoesNotThrow(() -> {
            new CallSign("");
        });
    }

    @Test
    void callSignConstructorAcceptsValidCallSign() {
        assertDoesNotThrow(() -> {
            new CallSign("AFR39BR");
        });
    }
}