package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;


/**
 * @author Rudolf Yazbeck (360700)
 */
public class CprDecoder {
    final static int Zphi0 = 60;
    final static int Zphi1 = 59;
    final static double deltaPhi0 = 1 / (double) Zphi0;
    final static double deltaPhi1 = 1 / (double) Zphi1;

    /**
     * Takes in 2 pairs of longitude and latitude, decodes them, and returns a GeoPos with the decoded positions
     *
     * @param x0         normalized even longitude (divided by 2^17) in turn
     * @param y0         normalized even latitude (divided by 2^17) in turn
     * @param x1         normalized uneven longitude (divided by 2^17) in turn
     * @param y1         normalized uneven latitude (divided by 2^17) in turn
     * @param mostRecent 0 if the most recent of position of the plane is even, 1 if it is uneven
     * @return corresponding decoded latitude and longitude
     * @author Rudolf Yazbeck (SCIPER: 360700)
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);

        int zphi = (int) Math.rint(y0 * Zphi1 - y1 * Zphi0);
        int zphi0, zphi1;

        if (zphi < 0) {
            zphi0 = zphi + Zphi0;
            zphi1 = zphi + Zphi1;
        } else {
            zphi0 = zphi;
            zphi1 = zphi;
        }
        //phi0 -> even latitude, phi1 -> uneven latitude
        double phi0 = deltaPhi0 * (zphi0 + y0);
        double phi1 = deltaPhi1 * (zphi1 + y1);
        if (phi0 >= 0.5) {
            phi0 -= 1;
        }
        if (phi1 >= 0.5) {
            phi1 -= 1;
        }

        //two different constants are calculated here to calculate ZlambdaTest, the usage of which is explained below
        double A = Math.acos(1 - (1 - Math.cos(2 * Math.PI * deltaPhi0)) / Math.pow(Math.cos(Units.convert(phi0, Units.Angle.TURN, Units.Angle.RADIAN)), 2));
        double B = Math.acos(1 - (1 - Math.cos(2 * Math.PI * deltaPhi0)) / Math.pow(Math.cos(Units.convert(phi1, Units.Angle.TURN, Units.Angle.RADIAN)), 2));

        int Zlambda0;
        int ZlambdaTest; //variable to test whether A and B yield the same Zlambda0

        //in case acos is given as argument a value that is not within [-1;1] then by definition Zlambda0 is equal to 1
        if (((Double) A).isNaN()) {
            Zlambda0 = 1;
            ZlambdaTest = 1;
        } else {
            Zlambda0 = (int) Math.floor((2 * Math.PI) / A);
            ZlambdaTest = (int) Math.floor((2 * Math.PI) / B);
        }

        //in this case, the aircraft has changed its latitude band and its position cannot be determined, so we return null
        if (Zlambda0 != ZlambdaTest) {
            return null;
        }
        int Zlambda1 = Zlambda0 - 1;

        double lambda; //lambda that will be used in the return statement so as to not do double the calculations
        double phi; //same thing

        if (Zlambda0 == 1) {
            lambda = mostRecent == 1 ? x1 : x0;

        } else {
            int zlamdba = (int) Math.rint(x0 * Zlambda1 - x1 * Zlambda0);

            lambda = mostRecent == 0 ? longitudeLatitudeSetter(zlamdba, Zlambda0, x0) : longitudeLatitudeSetter(zlamdba, Zlambda1, x1);
        }

        phi = mostRecent == 0 ? phi0 : phi1;

        if (lambda >= 0.5) {
            lambda = lambda - 1;
        }
        if (phi >= 0.5) {
            phi = phi - 1;
        }

        try {
            return (new GeoPos((int) Math.rint(Units.convert(lambda, Units.Angle.TURN, Units.Angle.T32)), (int) Math.rint(Units.convert(phi, Units.Angle.TURN, Units.Angle.T32))));
        } catch (IllegalArgumentException i) {
            return null;
        }
    }

    /**
     * method used to calculate the longitude whether it be even or uneven
     * @param zLambda that is calcualted before
     * @param ZlambdaI can either be Zlambda0 or Zlambda1
     * @param xI the normalized longitude, I represents wether it is even (0) or uneven (1)
     * @return the final longitude
     */
    private static double longitudeLatitudeSetter(double zLambda, double ZlambdaI, double xI) {
        double zLamdbaI = zLambda < 0 ? zLambda : zLambda + ZlambdaI;

        double deltaLambdaI = 1 / ZlambdaI;

        return deltaLambdaI * (zLamdbaI + xI);
    }
}