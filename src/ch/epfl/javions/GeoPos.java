package ch.epfl.javions;

/**
 * @param longitudeT32 longitude in T32
 * @param latitudeT32  latitude in T32, throws Illegal Argument Exception if invalid
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {

    private static final float MAX_ABSOLUTE_LATITUDE_T32 = Math.scalb(1, 30);

    /**
     * Instantiates a GeoPos object.
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * checks whether the latitude is in the correct T32 interval
     *
     * @param latitudeT32 Latitude in unit T32
     * @return True if latitudeT32 is within the bounds
     */

    public static boolean isValidLatitudeT32(int latitudeT32) {
        return latitudeT32 >= -MAX_ABSOLUTE_LATITUDE_T32 && latitudeT32 <= MAX_ABSOLUTE_LATITUDE_T32;
    }

    /**
     * Convert longitude to radians
     *
     * @return longitude converted in radians
     */

    public double longitude() {

        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**
     * Converts latitude to radians
     *
     * @return latitude converted in radians
     */

    public double latitude() {
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    @Override
    public String toString() {
        return ("(" +
                Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE) +
                "°" +
                ", " +
                Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE) +
                "°" +
                ")");
    }
}
