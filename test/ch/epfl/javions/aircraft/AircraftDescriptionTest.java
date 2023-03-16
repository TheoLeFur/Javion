package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDescriptionTest {
    /**
     * @author Rudolf Yazbeck
     */

    @Test
    void string() {
        //checking if the empty string is a valid description as it should be
        new AircraftDescription("");

        //check if a string of length 4 gives an error
        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription("AAAA"));

        //check if wrong numbers/letters give an error
        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription("A07"));
    }
}