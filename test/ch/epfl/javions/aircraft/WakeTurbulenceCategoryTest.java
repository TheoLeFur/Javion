package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WakeTurbulenceCategoryTest {

    /**
     * @author Rudolf Yazbeck
     */
    @Test
    void of() {
        //testing all possible switch cases
        assertEquals(WakeTurbulenceCategory.of("L"), WakeTurbulenceCategory.LIGHT);
        assertEquals(WakeTurbulenceCategory.of("M"), WakeTurbulenceCategory.MEDIUM);
        assertEquals(WakeTurbulenceCategory.of("H"), WakeTurbulenceCategory.HEAVY);
        //random string for "UNKNOWN" because anything other than L,H,M works
        assertEquals(WakeTurbulenceCategory.of("KJSIasji"), WakeTurbulenceCategory.UNKNOWN);
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