package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftRegistrationTest {

    /**
     * @author Rudolf Yazbeck
     */
    @Test
    void string() {
        //checking wether the empty string works or not
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration(""));

        //testing some unallowed characters
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration("!!;;"));

        new AircraftRegistration("??AZBVHAK-+/");
    }
}