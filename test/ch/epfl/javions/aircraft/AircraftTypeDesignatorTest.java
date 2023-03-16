package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTypeDesignatorTest {

    /**
     * @author Rudolf Yazbeck
     */
    @Test
    void string() {

        //checking whether the empty string works or not
        new AircraftTypeDesignator("");

        //checking if length 2 strings work correctly
        new AircraftTypeDesignator("A2");
        //checking if length 4 strings work correctly
        new AircraftTypeDesignator("3RS7");

        //testing unallowed values
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("-+"));

    }
}