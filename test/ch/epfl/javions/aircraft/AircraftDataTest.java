package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftDataTest {

    /**
     * @author Theo Le Fur
     * Verifies if NullPointerException is raised whenever null parameter is passed
     * into argument of the record.
     */

    @Test
    void ThrowsNullPointerExceptionCorrectly() {
        assertThrows(NullPointerException.class, () -> new AircraftData(null, null, null, null, null));
    }
}
