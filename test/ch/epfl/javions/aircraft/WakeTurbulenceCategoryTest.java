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
}