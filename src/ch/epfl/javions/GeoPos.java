package ch.epfl.javions;

public record GeoPos(int longitudeT32, int latitudeT32) {
    public GeoPos {
        if (!isValidLatitudeT32(latitudeT32)) throw new IllegalArgumentException();
    }
    /**
     * checks whether the latitude is in the correct T32 interval
     * @param latitudeT32 latitude that will be checked for validity
     * @author Theo Le Fur SCIPER : 363294
     * */

    /**
     * @param latitudeT32 Latitude in unit T32
     * @return True if latituudeT32 is within the bounds
     * @author Theo Le Fur SCIPER : 363294
     */

    public static boolean isValidLatitudeT32(int latitudeT32) {
        double limit = Math.pow(2, 30);
        return latitudeT32 >= -limit && latitudeT32 <= limit;
    }

    /**
     * Convert longitude in radians
     *
     * @return longitude converted in radians
     * @author Theo Le Fur SCIPER : 363294
     */

    public double longitude() {
        return Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.RADIAN);
    }

    /**
     * Converts latitude in radians
     *
     * @return latitude converted in radians
     * @author Theo Le Fur SCIPER : 363294
     */

    public double latitude() {
        return Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.RADIAN);
    }

    /**
     * @return String containing Longitude and Latitude in degrees
     * @author Theo Le Fur SCIPER : 363294
     */
    @Override
    public String toString() {
        return ("(" + Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°" + ", " + Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°" + ")");
    }
}
