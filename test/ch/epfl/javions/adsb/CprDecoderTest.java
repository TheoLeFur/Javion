package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {

    @Test
    void decodePosition() {
        int a = 0b1001;
        System.out.println(Bits.extractUInt(a, 0, 1));
        assertEquals(CprDecoder.decodePosition(0.851440, 0.720558,0.830574, 0.591721, 0),
                new GeoPos((int)(Units.convert(46.323349, Units.Angle.DEGREE, Units.Angle.T32)), (int)(Units.convert(7.476062, Units.Angle.DEGREE, Units.Angle.T32))));
    }
    @Test
    void updateBug(){
        double x0 = 0.64007568359375;
        double y0 = 0.6192169189453125;
        double x1 = 0.6243515014648438;
        double y1 = 0.4921417236328125;
        int mostRecent = 0;
        GeoPos actual = CprDecoder.decodePosition(x0,y0,x1,y1,mostRecent);
        System.out.println(actual);
    }
}