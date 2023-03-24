package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {



    @Test
    void decodePositionWorksOnTheGivenExample(){
        double x0 = Math.scalb(111600d, -17);
        double y0 = Math.scalb(94445d, -17);
        double x1 = Math.scalb(108865d, -17);
        double y1 = Math.scalb(77558d, -17);
        int mostRecent = 0;
        GeoPos actual = CprDecoder.decodePosition(x0,y0,x1,y1,mostRecent);
        GeoPos expected = new GeoPos(89192898,552659081);
        assertEquals(552659081 , actual.latitudeT32());
        assertEquals(89192898 , actual.longitudeT32());
        assertEquals(expected,actual);
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