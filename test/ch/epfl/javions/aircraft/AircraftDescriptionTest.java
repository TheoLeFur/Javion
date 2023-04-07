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
    @Test
    void aircraftDataConstructorThrowsWithNullAttribute() {
        var registration = new AircraftRegistration("HB-JAV");
        var typeDesignator = new AircraftTypeDesignator("B738");
        var model = "Boeing 737-800";
        var description = new AircraftDescription("L2J");
        var wakeTurbulenceCategory = WakeTurbulenceCategory.LIGHT;
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(null, typeDesignator, model, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, null, model, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, null, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, model, null, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, model, description, null);
        });
    }
}