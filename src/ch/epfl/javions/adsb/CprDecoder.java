package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;


public class CprDecoder {
    final static int Zphi0 = 60;
    final static int Zphi1 = 59;
    final static double deltaPhi0 = 1 / (double) Zphi0;
    final static double deltaPhi1 = 1 / (double) Zphi1;

    /**
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
            phi0 = phi0 - 1;
        }
        if (phi1 >= 0.5) {
            phi1 = phi1 - 1;
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
            if (mostRecent == 1) {
                lambda = x1;
                phi = phi1;
            } else {
                phi = phi0;
                lambda = x0;
            }

        } else {
            double zLambda0;
            double zLamdba1;
            int zlamdba = (int) Math.rint(x0 * Zlambda1 - x1 * Zlambda0);

            if (mostRecent == 0) {
                if (zlamdba < 0) {
                    zLambda0 = zlamdba + Zlambda0;
                } else {
                    zLambda0 = zlamdba;
                }
                double deltaLambda0 = 1 / (double) Zlambda0;

                lambda = deltaLambda0 * (zLambda0 + x0);
                phi = phi0;
            } else {
                if (zlamdba < 0) {
                    zLamdba1 = zlamdba + Zlambda1;
                } else {
                    zLamdba1 = zlamdba;
                }
                double deltaLambda1 = 1 / (double) Zlambda1;

                lambda = deltaLambda1 * (zLamdba1 + x1);
                phi = phi1;
            }
        }


        if (lambda >= 0.5) {
            lambda = lambda - 1;
        }
        if (phi >= 0.5) {
            phi = phi - 1;
        }

        if (!GeoPos.isValidLatitudeT32((int) Math.rint(Units.convert(lambda, Units.Angle.TURN, Units.Angle.T32)))) {
            return null;
        }

        try {
            return (new GeoPos((int) Math.rint(Units.convert(lambda, Units.Angle.TURN, Units.Angle.T32)), (int) Math.rint(Units.convert(phi, Units.Angle.TURN, Units.Angle.T32))));
        } catch (IllegalArgumentException i) {
            return null;
        }
    }
}