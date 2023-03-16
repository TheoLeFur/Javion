package ch.epfl.javions;

public final class WebMercator {
    private WebMercator() {
    }

    /**
     * @return the x coordinate corresponding to the given longitude in radians and zoom level
     * @param zoomLevel Integer representing the zoom level
     * @param longitude double representing the longitude*/

    public static double x(int zoomLevel, double longitude) {
        return Math.pow(2, 8 + zoomLevel) * (Units.convertTo(longitude, Units.Angle.TURN) + 0.5);
    }


    /**
     * @return the y coordinate corresponding to the given latitude in radians and zoom level
     * @param zoomLevel integer representing the zoom level
     * @param latitude double representing the latitude */
     
    public static double y(int zoomLevel, double latitude) {

        return Math.pow(2, 8 + zoomLevel) *
                (Units.convertTo(-Math2.asinh(Math.tan(latitude)), Units.Angle.TURN) + 0.5);

    }
}
