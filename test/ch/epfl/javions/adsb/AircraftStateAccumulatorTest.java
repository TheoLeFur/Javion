package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftStateAccumulatorTest {

    @Test
    void ConstructorThrowsNullPointerExceptionCorrectly(){
        AircraftStateSetter setter = null;
        assertThrows(NullPointerException.class, ()-> new AircraftStateAccumulator<AircraftStateSetter>(setter));

    }

}
