package ch.epfl.javions;

/**
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class WebMercator {

    /**
     * Private constructor, class cannot be instantiated
     */
    private WebMercator() {
    }

    /**
     * @param zoomLevel Integer representing the zoom level
     * @param longitude double representing the longitude
     * @return the x coordinate corresponding to the given longitude in radians and zoom level
     */

    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(1, 8 + zoomLevel) * (Units.convertTo(longitude, Units.Angle.TURN) + 0.5);
    }


    /**
     * @param zoomLevel integer representing the zoom level
     * @param latitude  double representing the latitude
     * @return the y coordinate corresponding to the given latitude in radians and zoom level
     */

    public static double y(int zoomLevel, double latitude) {

        return Math.pow(2, 8 + zoomLevel) *
                (Units.convertTo(-Math2.asinh(Math.tan(latitude)), Units.Angle.TURN) + 0.5);

    }
}
