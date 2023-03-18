package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {

    @Test
    void decodePosition() {
        assertEquals(CprDecoder.decodePosition(0.851440, 0.720558,0.830574, 0.591721, 0),
                new GeoPos((int)(Units.convert(46.323349, Units.Angle.DEGREE, Units.Angle.T32)), (int)(Units.convert(7.476062, Units.Angle.DEGREE, Units.Angle.T32))));
    }
}