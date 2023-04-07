package ch.epfl.javions;

/**
 * @param longitudeT32 longitude in T32
 * @param latitudeT32  latitude in T32, throws Illegal Argument Exception if invalid
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {

    /**
     * Instantiates a GeoPos object.
     */
    public GeoPos {
        if (!isValidLatitudeT32(latitudeT32)) throw new IllegalArgumentException();
    }

    /**
     * checks whether the latitude is in the correct T32 interval
     *
     * @param latitudeT32 Latitude in unit T32
     * @return True if latituudeT32 is within the bounds
     */

    public static boolean isValidLatitudeT32(int latitudeT32) {
        double limit = Math.pow(2, 30);
        return latitudeT32 >= -limit && latitudeT32 <= limit;
    }

    /**
     * Convert longitude to radians
     *
     * @return longitude converted in radians
     */

    public double longitude() {
        return Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.RADIAN);
    }

    /**
     * Converts latitude to radians
     *
     * @return latitude converted in radians
     */

    public double latitude() {
        return Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.RADIAN);
    }

    @Override
    public String toString() {
        return ("(" + Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°" + ", " + Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°" + ")");
    }
}
