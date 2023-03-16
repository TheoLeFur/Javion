package ch.epfl.javions;

public record GeoPos(int longitudeT32, int latitudeT32) {
    public GeoPos {
        if (!isValidLatitudeT32(latitudeT32)) throw new IllegalArgumentException();
    }
    /**
     * checks wether the latitude is in the correct T32 interval
     * @param latitudeT32 latitude that will be checked for validity
     * */

    /**
     *
     * @param latitudeT32 Latitude in unit T32
     * @return True if latituudeT32 is within the bounds
     */

    public static boolean isValidLatitudeT32(int latitudeT32){
        double limit = Math.pow(2, 30);
        return latitudeT32 >= -limit && latitudeT32 <= limit;
    }

    /**
     * Convert longitude in radians
     * @return longitude converted in radians
     */

    public double longitude(){
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**

     * Converts latitude in radians
     * @return latitude converted in radians
     */

    public double latitude(){
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    /**
     *
     * @return String containing Longitude and Latitude in degrees
     */
    @Override
    public String toString(){
        return ("(" + Units.convertTo(longitude(), Units.Angle.DEGREE) + "°" + ", " + Units.convertTo(latitude(), Units.Angle.DEGREE) + "°" + ")");
    }
}
