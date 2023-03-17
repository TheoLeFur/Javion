package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

public class CprDecoder {
    final static int Zphi0 = 60;
    final static int Zphi1 = 59;
    final static double deltaPhi0 = 1/(double)Zphi0;
    final static double deltaPhi1 = 1/(double)Zphi1;

    /**
     *
     * @param x0 even longitude
     * @param y0 even latitude
     * @param x1 uneven longitude
     * @param y1 uneven latitude
     * @param mostRecent 0 if the most recent of position of the plane is even, 1 if it is uneven
     * @return corresponding latitude and longitude
     */
    public static GeoPos decodePosition(double x0, double y0,
                                        double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);
        double lambda0;
        double lambda1;

        int zphi = (int)Math.rint(y0*Zphi1 - y1*Zphi0);
        int zphi0, zphi1;
        if(zphi < 0) {
            zphi0 = zphi + Zphi0;
            zphi1 = zphi + Zphi1;
        } else {
            zphi0 = zphi;
            zphi1 = zphi;
        }
        //phi0 -> even latitude, phi1 -> uneven latitude
        double phi0 = Units.convert(deltaPhi0 * (zphi0 + y0), Units.Angle.TURN, Units.Angle.RADIAN);
        double phi1 = Units.convert(deltaPhi1 * (zphi1 + y1), Units.Angle.TURN ,Units.Angle.RADIAN);

        final double A = Math.acos(1 - (1 - Math.cos(2*Math.PI*deltaPhi0))/Math.pow(Math.cos(phi0), 2));
        int Zlambda0 = (int)Math.floor((2*Math.PI)/A);
        int Zlambda1 = Zlambda0 - 1;

        if(Zlambda1 == 1) {
            lambda1 = x1;
            lambda0 = x0;

            if(mostRecent == 0) {
                return (new GeoPos((int)Units.convert(phi0, Units.Angle.RADIAN, Units.Angle.T32), (int)Units.convert(lambda0, Units.Angle.TURN, Units.Angle.T32)) );
            } else {
                return (new GeoPos((int)Units.convert(phi1, Units.Angle.RADIAN, Units.Angle.T32), (int)Units.convert(lambda1, Units.Angle.TURN, Units.Angle.T32)) );
            }
        } else {
            double zLambda0;
            double zLamdba1;
            int zlamdba = (int)Math.rint(x0 * Zlambda1 - x1 * Zlambda0);

            if(zlamdba < 0) {
                zLamdba1 = zlamdba + Zlambda1;
                zLambda0 = zlamdba + Zlambda0;
            } else {
                zLambda0 = zlamdba;
                zLamdba1 = zlamdba;
            }
            double deltaLambda0 = 1/(double)Zlambda0;
            double deltaLambda1 = 1/(double)Zlambda1;

            lambda0 = deltaLambda0 * (zLambda0 + x0);
            lambda1 = deltaLambda1 * (zLamdba1 + x1);

            if(mostRecent == 0) {
                return (new GeoPos((int)Units.convert(phi0, Units.Angle.RADIAN, Units.Angle.T32), (int)Units.convert(lambda0, Units.Angle.TURN, Units.Angle.T32)) );
            } else {
                return (new GeoPos((int)Units.convert(phi1, Units.Angle.RADIAN, Units.Angle.T32), (int)Units.convert(lambda1, Units.Angle.TURN, Units.Angle.T32)) );
            }
        }

    }
}
